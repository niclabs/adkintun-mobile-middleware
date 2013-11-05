package cl.niclabs.adkmobile.monitor;

import android.os.Binder;
import android.os.Bundle;
import cl.niclabs.adkmobile.data.DataObject;
import cl.niclabs.adkmobile.monitor.events.MonitorEvent;
import cl.niclabs.adkmobile.monitor.listeners.MonitorListener;

/**
 * Defines a manager of monitor events, i.e. a monitor.
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 * @param <E> listeners that the monitor handles
 */
public interface Monitor<E extends MonitorListener> {
	/**
	 * Define the debugging status of the application
	 */
	public static final boolean DEBUG = true;
	
	/**
	 * Intent action for activating monitor events
	 */
	public static final String ACTIVATE = "monitor_event_activate";
	
	/**
	 * Flag to represent all events
	 */
	public static final int ALL_EVENTS = 2147483647;
	
	/**
	 * Defines a connectivity event
	 */
	public static final int CONNECTIVITY = 1;
	
	/**
	 * Intent action for deactivating monitor events
	 */
	public static final String DEACTIVATE = "monitor_event_deactivate";
	
	/**
	 * Extra key for communicating the activated/deactivated events to the monitor
	 */
	public static final String EVENTS_EXTRA = "events_extra";

	/**
	 * Defines a mobile traffic event
	 */
	public static final int TRAFFIC_MOBILE = 2;
	
	/**
	 * Defines a wifi traffic event
	 */
	public static final int TRAFFIC_WIFI = 4;
	
	/**
	 * Defines a general traffic event (to monitor all traffic) 
	 */
	public static final int TRAFFIC = TRAFFIC_MOBILE | TRAFFIC_WIFI;
	
	/**
	 * Defines a telephony event
	 */
	public static final int TELEPHONY = 8;
	
	/**
	 * Defines a screen event
	 */
	public static final int SCREEN = 16;
	/**
	 * Activate the specified events.
	 * @param events
	 * @param configuration additional configurations for the events
	 * @return true if the event was activated correctly, false otherwise or if the event is not recongnized by the monitor
	 */
	public boolean activate(int events, Bundle configuration);
	
	/**
	 * Activates the monitor for a given event
	 * @param event
	 */
	public boolean activate(MonitorEvent<E> event);
	
	/**
	 * Deactivate the specified events
	 * @param events
	 */
	public void deactivate(int events);
	
	/**
	 * Deactivates the monitor for a given event
	 * @param event
	 */
	public void deactivate(MonitorEvent<E> event);
	
	/**
	 * Get the internal state of the specified event
	 * @param event
	 * @return
	 */
	public DataObject getState(MonitorEvent<E> event);
	
	/**
	 * Returns the activation status for a given event
	 * @param event
	 * @return
	 */
	public boolean isActive(MonitorEvent<E> event);
	
	/**
	 * Adds/remove a listener for a given event. 
	 * 
	 * If listen is false then the listener must be removed if active
	 * 
	 * @param listener
	 * @param listen true to add listener
	 */
	public void listen(E listener, boolean listen);
	
	/**
	 * Defines a generic service binder for all monitors
	 * @author Felipe Lalanne <flalanne@niclabs.cl>
	 *
	 * @param <E> the type of monitor for the service binder
	 */
	public static class ServiceBinder<E extends Monitor<?>> extends Binder {
		private E monitor;
		
		public ServiceBinder(E monitor) {
			super();
			this.monitor = monitor;
		}
		
		public E getService() {
			return monitor;
		}
	}
}
