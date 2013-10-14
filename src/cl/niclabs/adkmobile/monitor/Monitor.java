package cl.niclabs.adkmobile.monitor;

import android.os.Bundle;
import cl.niclabs.adkmobile.monitor.data.DataObject;
import cl.niclabs.adkmobile.monitor.events.MonitorEvent;
import cl.niclabs.adkmobile.monitor.listeners.MonitorListener;

/**
 * Defines a manager of monitor events, i.e. a monitor.
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 *
 */
public interface Monitor {
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
	public static final int MOBILE_TRAFFIC = 2;
	
	/**
	 * Defines a wifi traffic event
	 */
	public static final int WIFI_TRAFFIC = 4;
	
	/**
	 * Defines a general traffic event (to monitor all traffic) 
	 */
	public static final int TRAFFIC = MOBILE_TRAFFIC | WIFI_TRAFFIC;
	
	/**
	 * Defines a telephony event
	 */
	public static final int TELEPHONY = 8;
	
	/**
	 * Activate the specified events.
	 * @param events
	 * @param configuration additional configurations for the events
	 */
	public void activate(int events, Bundle configuration);
	
	/**
	 * Activates the monitor for a given event
	 * @param event
	 */
	public void activate(MonitorEvent event);
	
	/**
	 * Deactivate the specified events
	 * @param events
	 */
	public void deactivate(int events);
	
	/**
	 * Deactivates the monitor for a given event
	 * @param event
	 */
	public void deactivate(MonitorEvent event);
	
	/**
	 * Get the internal state of the specified event
	 * @param event
	 * @return
	 */
	public DataObject getState(MonitorEvent event);
	
	/**
	 * Returns the activation status for a given event
	 * @param event
	 * @return
	 */
	public boolean isActive(MonitorEvent event);
	
	/**
	 * Adds/remove a listener for a given event. 
	 * 
	 * If listen is false then the listener must be removed if active
	 * 
	 * @param listener
	 * @param listen true to add listener
	 */
	public void listen(MonitorListener listener, boolean listen);
}
