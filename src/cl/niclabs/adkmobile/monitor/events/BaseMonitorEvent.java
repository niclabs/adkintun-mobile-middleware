package cl.niclabs.adkmobile.monitor.events;


/**
 * Base implementation of the activation methods of monitor event.
 * 
 * The methods activate() and deactivate() must be overriden by sub-classes
 * in order to perform the necessary activation and deactivation tasks.
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 *
 */
public abstract class BaseMonitorEvent implements MonitorEvent {
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
	public void activate() {
		synchronized(active) {
			active = true;
		}
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
			active = true;
		}
	}

	@Override
	public boolean isActive() {
		synchronized(active) {
			return active;
		}
	}
}
