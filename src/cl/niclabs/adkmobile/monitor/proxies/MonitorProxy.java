package cl.niclabs.adkmobile.monitor.proxies;

import cl.niclabs.adkmobile.utils.Dispatcher;
import cl.niclabs.adkmobile.utils.Notifier;

/**
 * A monitor proxy is a processor for raw monitor data in order to 
 * generate more complex events. 
 * 
 * For instance a processor can monitor battery or traffic and notify 
 * the listeners when the battery level has gone below a certain threshold
 * or traffic has reached a certain level.
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 */
public abstract class MonitorProxy<E extends MonitorProxyListener> {
	private Dispatcher<E> dispatcher = new Dispatcher<E>();
	
	/**
	 * Adds/remove a listener for a given event. 
	 * 
	 * If listen is false then the listener must be removed if active
	 * 
	 * @param listener
	 * @param listen true to add listener
	 */
	public void listen(E listener, boolean listen) {
		dispatcher.listen(listener, listen);
	}
	
	/**
	 * Notifies the listeners of 
	 * @param notifier
	 */
	protected void notifyListeners(Notifier<E> notifier) {
		dispatcher.notifyListeners(notifier);
	}
}
