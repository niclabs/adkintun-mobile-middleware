package cl.niclabs.adkmobile.monitor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import cl.niclabs.adkmobile.monitor.data.Observation;
import cl.niclabs.adkmobile.monitor.events.MonitorEvent;
import cl.niclabs.adkmobile.monitor.listeners.MonitorListener;
import cl.niclabs.adkmobile.utils.Dispatcher;
import cl.niclabs.adkmobile.utils.Notifier;

/**
 * Base class for all monitoring services. It allows implementing classes to
 * handle multiple types of events and data types, as well as listeners of the
 * monitoring events to be notified of status changes.
 * 
 * TODO: Verify thread safety of the methods in this class
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 * @param <E> listeners that the monitor handles
 */
public abstract class AbstractMonitor<E extends MonitorListener> extends Service implements Monitor<E> {	
	protected class MonitorEventController extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ACTIVATE)) {
				activate(intent.getIntExtra(EVENTS_EXTRA, 0), intent.getExtras());
			}
			else if (intent.getAction().equals(DEACTIVATE)) {
				deactivate(intent.getIntExtra(EVENTS_EXTRA, 0));
			}
		}	
	}
	
	/**
	 * Current state by eventType
	 */
	private Map<MonitorEvent<E>, Observation> currentStates = new ConcurrentHashMap<MonitorEvent<E>,Observation>(4);
	
	
	private MonitorEventController eventController = new MonitorEventController();
	
	/**
	 * Dispatcher for monitor events
	 */
	private Dispatcher<E> dispatcher = new Dispatcher<E>();
	
	protected String TAG = "AdkintunMobile";
	
	@Override
	public boolean activate(MonitorEvent<E> eventType) {
		return eventType.activate();
	}
	
	
	@Override
	public void deactivate(MonitorEvent<E> eventType) {
		eventType.deactivate();
	}
	
	/**
	 * Get the current state data for the specified event type. Returns null if there is no data 
	 * @param eventType
	 * @return
	 */
	@Override
	public Observation getState(MonitorEvent<E> eventType) {
		return currentStates.get(eventType);
	}
	
	/**
	 * Get the activation status of the specified event type
	 * @param eventType
	 * @return
	 */
	public boolean isActive(MonitorEvent<E> eventType) {
		return eventType.isActive();
	}
	
	@Override
	public void listen(E listener, boolean listen) {
		dispatcher.listen(listener, listen);
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
	protected void notifyListeners(final MonitorEvent<E> eventType, final Observation result) {
		/* Update the internal state */
		setState(eventType, result);
		
		dispatcher.notifyListeners(new Notifier<E>() {
			@Override
			public void notify(E listener) {
				// TODO: Execute the method on a runnable and schedule on a ScheduledThreadPoolExecutor?
				eventType.onDataReceived(listener, result);
			}
			
		});
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
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		unregisterReceiver(eventController);
		
		/**
		 * Deactivate all the events for this monitor
		 */
		deactivate(ALL_EVENTS);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		
		/* Activate the events specified on the intent */
		activate(intent.getIntExtra(EVENTS_EXTRA, 0), intent.getExtras());
		
        return START_STICKY;
	}

	/**
	 * Set the current status data for the specified event type
	 * @param eventType
	 * @param state
	 */
	private void setState(MonitorEvent<E> eventType, Observation state) {
		currentStates.put(eventType, state);
	}
}
