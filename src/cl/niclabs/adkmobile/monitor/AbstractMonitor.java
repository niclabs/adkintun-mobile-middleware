package cl.niclabs.adkmobile.monitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import cl.niclabs.adkmobile.monitor.data.DataObject;
import cl.niclabs.adkmobile.monitor.events.MonitorEvent;
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
	/**
	 * Current state by eventType
	 */
	private Map<MonitorEvent, DataObject> currentStates = new ConcurrentHashMap<MonitorEvent,DataObject>();
	
	protected boolean DEBUG = true;
	
	/** 
	 * List of listeners by event type 
	 */
	private Map<MonitorEvent, List<MonitorListener>> listeners = new ConcurrentHashMap<MonitorEvent, List<MonitorListener>>();
	
	protected String TAG = "AdkintunMobile";
	
	protected class MonitorEventController extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ACTIVATE_EVENT)) {
				activate(intent.getIntExtra(EVENTS_EXTRA, 0), intent.getExtras());
			}
			else if (intent.getAction().equals(DEACTIVATE_EVENT)) {
				deactivate(intent.getIntExtra(EVENTS_EXTRA, 0));
			}
		}
		
	}
	
	private MonitorEventController eventController = new MonitorEventController();
	
	@Override
	public void activate(MonitorEvent eventType) {
		eventType.activate();
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
	public void listen(MonitorEvent eventType, MonitorListener listener, boolean listen) {
		List<MonitorListener> list;
		if ((list = listeners.get(eventType)) == null) list = new ArrayList<MonitorListener>();
		
		if (listen) {
			list.add(listener);
			listeners.put(eventType, list);
		}
		else {
			list.remove(listener);
		}

	}
	
	/**
	 * Must be called by sub-classes to notify listeners of data received 
	 * @param eventType the event to which the data is related  
	 * @param data 
	 */
	protected void notifyListeners(MonitorEvent eventType, DataObject data) {
		List<MonitorListener> list;
		if ((list = listeners.get(eventType)) != null) {
			for (MonitorListener listener: list) {
				/* Notify the listener */
				eventType.onDataReceived(listener, data);
			}
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
		filter.addAction(ACTIVATE_EVENT);
		filter.addAction(DEACTIVATE_EVENT);
		
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
		 * Deactivate all the events
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
	protected void setState(MonitorEvent eventType, DataObject state) {
		currentStates.put(eventType, state);
	}
}
