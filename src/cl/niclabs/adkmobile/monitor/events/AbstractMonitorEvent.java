package cl.niclabs.adkmobile.monitor.events;

import cl.niclabs.adkmobile.monitor.listeners.MonitorListener;


/**
 * Base implementation of the activation methods of monitor event.
 * 
 * The methods activate() and deactivate() must be overriden by sub-classes
 * in order to perform the necessary activation and deactivation tasks.
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 *
 */
public abstract class AbstractMonitorEvent<E extends MonitorListener> implements MonitorEvent<E> {
	protected Boolean active = false;
	
	/**
	 * Activates the MonitorEvent. 
	 * 
	 * It must be called by overriding methods in order to keep track of the activation
	 * status. 
	 * 
	 * This method is thread-safe 
	 * 
	 */
	@Override
	public boolean activate() {
		synchronized(active) {
			active = true;
		}
		return true;
	}

	/**
	 * Deactivates the MonitorEvent. 
	 * 
	 * It must be called by overriding methods in order to keep track of the activation
	 * status. 
	 * 
	 * This method is thread-safe 
	 * 
	 */
	@Override
	public void deactivate() {
		synchronized(active) {
			active = false;
		}
	}

	@Override
	public boolean isActive() {
		synchronized(active) {
			return active;
		}
	}
}
