package cl.niclabs.adkmobile.monitor.events;

import cl.niclabs.adkmobile.monitor.data.DataObject;
import cl.niclabs.adkmobile.monitor.listeners.MonitorListener;

/**
 * Defines a manager of monitor events, i.e. a monitor.
 * 
 * 
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 *
 */
public interface MonitorEventManager {
	/**
	 * Defines a change in connectivity status
	 */
	public static final int CONNECTIVITY_CHANGE = 1;
	
	/**
	 * Defines a change in mobile traffic rate
	 */
	public static final int MOBILE_TRAFFIC_CHANGE = 2;

	/**
	 * Define a change in Telephony status
	 */
	public static final int TELEPHONY_CHANGE = 3;
	/**
	 * Define the key intent for Mobile Traffic
	 */
	public static final String TRAFFIC_INTENT = "traffic_change";
	/**
	 * Define the key intent for Connectivity
	 */
	public static final String CONNECTIVITY_INTENT = "connectivity_change";
	/**
	 * Define the key intent fo telephony
	 */
	public static final String TELEPHONY_INTENT = "telephony_change";
	
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
	 * Deactivates the monitor for a given event
	 * @param event
	 */
	public void deactivate(MonitorEvent event);
	
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
