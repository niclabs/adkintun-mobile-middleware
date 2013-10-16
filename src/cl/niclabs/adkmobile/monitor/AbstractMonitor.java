package cl.niclabs.adkmobile.monitor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import cl.niclabs.adkmobile.monitor.data.DataObject;
import cl.niclabs.adkmobile.monitor.events.MonitorEvent;
import cl.niclabs.adkmobile.monitor.events.MonitorEventResult;
import cl.niclabs.adkmobile.monitor.listeners.MonitorListener;

/**
 * Base class for all monitoring services. It allows implementing classes to
 * handle multiple types of events and data types, as well as listeners of the
 * monitoring events to be notified of status changes.
 * 
 * TODO: Verify thread safety of the methods in this class
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 */
public abstract class AbstractMonitor extends Service implements Monitor {	
	protected class MonitorEventController extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ACTIVATE)) {
				if(DEBUG) Log.d(TAG, "Received activation broadcast");
				activate(intent.getIntExtra(EVENTS_EXTRA, 0), intent.getExtras());
			}
			else if (intent.getAction().equals(DEACTIVATE)) {
				if(DEBUG) Log.d(TAG, "Received deactivation broadcast");
				deactivate(intent.getIntExtra(EVENTS_EXTRA, 0));
			}
		}
		
	}
	
	/**
	 * Current state by eventType
	 */
	private Map<MonitorEvent, DataObject> currentStates = new ConcurrentHashMap<MonitorEvent,DataObject>(4);
	
	protected boolean DEBUG = true;
	
	private MonitorEventController eventController = new MonitorEventController();
	
	/** 
	 * List of listeners by event type 
	 */
	private List<MonitorListener> listeners = new CopyOnWriteArrayList<MonitorListener>();
	
	protected String TAG = "AdkintunMobile";
	
	@Override
	public boolean activate(MonitorEvent eventType) {
		return eventType.activate();
	}
	
	
	@Override
	public void deactivate(MonitorEvent eventType) {
		eventType.deactivate();
	}
	
	/**
	 * Get the current state data for the specified event type. Returns null if there is no data 
	 * @param eventType
	 * @return
	 */
	@Override
	public DataObject getState(MonitorEvent eventType) {
		return currentStates.get(eventType);
	}
	
	/**
	 * Get the activation status of the specified event type
	 * @param eventType
	 * @return
	 */
	public boolean isActive(MonitorEvent eventType) {
		return eventType.isActive();
	}
	
	@Override
	public void listen(MonitorListener listener, boolean listen) {
		if (listen) {
			listeners.add(listener);
		}
		else {
			listeners.remove(listener);
		}

	}
	
	/**
	 * Notifies listeners of the monitor of new data received and updates 
	 * the internal state of the monitor that can be obtained with getState()
	 * 
	 * Must be called by sub-classes to notify listeners of data received 
	 * 
	 * @param eventType the event to which the data is related
	 * @param result the result from the event
	 */
	protected void notifyListeners(MonitorEvent eventType, MonitorEventResult result) {
		/* Update the internal state */
		setState(eventType, result.getData());
		
		for (MonitorListener listener: listeners) {
			/* Notify the listener */
			
			// TODO: Execute the method on a runnable and schedule on a ScheduledThreadPoolExecutor?
			eventType.onDataReceived(listener, result);
		}
		/* Ignore if there are no listeners for the data type */
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTIVATE);
		filter.addAction(DEACTIVATE);
		
		/* Listen for activation messages */
		registerReceiver(eventController, filter);
		
		/* TODO: Apply configuration from general preferences and listen to general broadcasts */
		if(DEBUG) Log.d(TAG, TAG + " sensor started...");
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		unregisterReceiver(eventController);
		
		/**
		 * Deactivate all the events for this monitor
		 */
		deactivate(ALL_EVENTS);
		
		/* TODO: Unregister broadcasts */
		if(DEBUG) Log.d(TAG, TAG + " sensor terminated...");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		
		/* Activate the events specified on the intent */
		activate(intent.getIntExtra(EVENTS_EXTRA, 0), intent.getExtras());
		
		if(DEBUG) Log.d(TAG, TAG + " sensor active...");
		
        return START_STICKY;
	}

	/**
	 * Set the current status data for the specified event type
	 * @param eventType
	 * @param state
	 */
	private void setState(MonitorEvent eventType, DataObject state) {
		currentStates.put(eventType, state);
	}
}
