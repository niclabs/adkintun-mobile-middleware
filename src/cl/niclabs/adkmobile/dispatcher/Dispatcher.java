package cl.niclabs.adkmobile.dispatcher;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Dispatcher<E extends Listener> {
	/** 
	 * List of listeners by event type 
	 */
	private List<E> listeners = new CopyOnWriteArrayList<E>();
	
	public void listen(E listener, boolean listen) {
		if (listen) {
			listeners.add(listener);
		}
		else {
			listeners.remove(listener);
		}
	}
	
	public void notifyListeners(Notifier<E> notifier) {
		for (E listener: listeners) {
			notifier.notify(listener);
		}
	}
}
