package cl.niclabs.adkmobile.monitor.events;

import android.os.Bundle;
import cl.niclabs.adkmobile.monitor.data.DataObject;
import cl.niclabs.adkmobile.monitor.listeners.MonitorListener;

/**
 * Defines a manager of monitor events, i.e. a monitor.
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 *
 */
public interface Monitor {
	/**
	 * Flag to represent all events
	 */
	public static final int ALL_EVENTS = 2147483647;
	
	/**
	 * Defines a connectivity event
	 */
	public static final int CONNECTIVITY = 1;
	
	/**
	 * Defines a mobile traffic event
	 */
	public static final int MOBILE_TRAFFIC = 2;

	/**
	 * Defines a telephony event
	 */
	public static final int TELEPHONY = 4;
	
	/**
	 * Intent action for activating monitor events
	 */
	public static final String ACTIVATE_EVENT = "monitor_event_activate";
	
	/**
	 * Intent action for deactivating monitor events
	 */
	public static final String DEACTIVATE_EVENT = "monitor_event_deactivate";
	
	/**
	 * Extra key for communicating the activated/deactivated events to the monitor
	 */
	public static final String EVENTS_EXTRA = "events_extra";
	
	/**
	 * Returns the activation status for a given event
	 * @param event
	 * @return
	 */
	public boolean isActive(MonitorEvent event);
	
	/**
	 * Activates the monitor for a given event
	 * @param event
	 */
	public void activate(MonitorEvent event);
	
	/**
	 * Activate the specified events 
	 * @param events
	 * @param configuration additional configurations for the events
	 */
	public void activate(int events, Bundle configuration);
	
	/**
	 * Deactivates the monitor for a given event
	 * @param event
	 */
	public void deactivate(MonitorEvent event);
	
	/**
	 * Deactivate the specified events
	 * @param events
	 */
	public void deactivate(int events);
	
	/**
	 * Adds a listener for a given event
	 * @param listener
	 * @param event
	 */
	public void listen(MonitorListener listener, MonitorEvent event);
	
	/**
	 * Get the internal state of the specified event
	 * @param event
	 * @return
	 */
	public DataObject getState(MonitorEvent event);
}
