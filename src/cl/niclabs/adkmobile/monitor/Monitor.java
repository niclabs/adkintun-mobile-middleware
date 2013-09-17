package cl.niclabs.adkmobile.monitor;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import cl.niclabs.adkmobile.monitor.data.DataFields;
import cl.niclabs.adkmobile.monitor.data.DataObject;

/**
 * Base class for all monitoring services. It allows for implementing classes to handle multiple types 
 * of events and data types, as well as listeners of the monitoring events to be notified of status changes.
 * 
 * TODO: Verify thread safety of the methods in this class
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 */
public abstract class Monitor extends Service {
	protected String TAG = "AdkintunMobile";
	
	protected boolean DEBUG = true;
	
	/**
	 * Current status by eventType
	 */
	private SparseArray<DataObject> currentStates = new SparseArray<DataObject>();
	
	/** 
	 * List of listeners by event type 
	 */
	private SparseArray<List<MonitorListener>> listeners = new SparseArray<List<MonitorListener>>();
	
	/**
	 * Event activity status by event type
	 */
	private SparseBooleanArray activeEvents = new SparseBooleanArray();
	
	/**
	 * Get the current state data for the specified event type. Returns null if there is no data 
	 * @param eventType
	 * @return
	 */
	public DataObject getCurrentState(int eventType) {
		return currentStates.get(eventType);
	}
	
	/**
	 * Return column structure for a given data type
	 * @param eventType
	 * @return
	 */
	public abstract DataFields getDataFields(int eventType);
	
	/**
	 * Get the activation status of the specified event type
	 * @param eventType
	 * @return
	 */
	public boolean isActive(int eventType) {
		synchronized(activeEvents) {
			Boolean result = activeEvents.get(eventType);
			if (result != null) {
				return result;
			}
			return false;
		}
	}
	
	public void listen(MonitorListener listener, int eventType) {
		List<MonitorListener> list;
		if ((list = listeners.get(eventType)) == null) list = new ArrayList<MonitorListener>();
		listeners.put(eventType, list);
	}
	
	/**
	 * Must be called by sub-classes to notify listeners of data received 
	 * @param eventType the event to which the data is related  
	 * @param data 
	 */
	protected void notifyListeners(int eventType, DataObject data) {
		List<MonitorListener> list;
		if ((list = listeners.get(eventType)) != null) {
			for (MonitorListener listener: list) {
				/* Notify the listener */
				listener.onSensorEvent(eventType);
				listener.onDataReceived(eventType, getDataFields(eventType), data);
			}
		}
		/* Ignore if there are no listeners for the data type */
	}
	
	/**
	 * Method triggered on activation of the specified event type. 
	 * @param eventType
	 */
	public abstract void onActivateEvent(int eventType);
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		/* TODO: Apply configuration from general preferences and listen to general broadcasts */
		if(DEBUG) Log.d(TAG, TAG + " sensor started...");
	} 
	
	/**
	 * Method triggered on deactivation of the specified event type.
	 * @param eventType
	 */
	public abstract void onDeactivateEvent(int eventType);
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		/* TODO: Unregister broadcasts */
		if(DEBUG) Log.d(TAG, TAG + " sensor terminated...");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		
		/* TODO: Read configurations from preferences and perform tasks for starting the service */
		
		if(DEBUG) Log.d(TAG, TAG + " sensor active...");
        return START_STICKY;
	}

	/**
	 * Set the activation status of the specified event type
	 * @param eventType
	 * @param active
	 */
	public void setActive(int eventType, boolean active) {
		synchronized(activeEvents) {
			if (active) {
				onActivateEvent(eventType);
			}
			else {
				onDeactivateEvent(eventType);
			}
			activeEvents.put(eventType, active);
		}
	}

	/**
	 * Set the current status data for the specified event type
	 * @param eventType
	 * @param state
	 */
	public void setCurrentState(int eventType, DataObject state) {
		currentStates.put(eventType, state);
	}
}
