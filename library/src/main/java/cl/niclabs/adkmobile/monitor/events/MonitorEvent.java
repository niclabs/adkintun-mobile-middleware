package cl.niclabs.adkmobile.monitor.events;

import cl.niclabs.adkmobile.monitor.data.Observation;
import cl.niclabs.adkmobile.monitor.listeners.MonitorListener;

/**
 * Defines a specific event to be observed by monitors.
 * 
 * Sub-classes of Monitor must create an instance of this interface 
 * in order to support the monitoring of multiple types of events. 
 * 
 * For a base implementation of this interface, @see BaseMonitorEvent
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 * @param <E> listeners associated to the monitor event
 */
public interface MonitorEvent<E extends MonitorListener> {
	/**
	 * Activates the event
	 * 
	 * @return true if the event was activated correctly or is already active
	 */
	public boolean activate();
	
	/**
	 * Deactivates the event
	 */
	public void deactivate();
	
	/**
	 * 
	 * @return true if the event is active
	 */
	public boolean isActive();
	
	/**
	 * Notifies the given listener that new data is available.
	 * 
	 * It is called by the monitor upon receiving new data
	 * 
	 * @param listener
	 * @param result the result from the event
	 */
	public void onDataReceived(E listener, Observation result);
}
