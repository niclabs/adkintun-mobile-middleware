package cl.niclabs.adkmobile.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Generic dispatcher for the observer design pattern.
 * 
 * This class receives listeners to be notified upon a specific event. Listeners
 * are notified through a notifier class in order to allow for different methods
 * to be defined by listener.
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 * 
 * @param <E>
 *            type of listener that this dispatcher takes
 */
public class Dispatcher<E extends Listener> {
	/**
	 * List of listeners by event type
	 */
	private List<E> listeners = new CopyOnWriteArrayList<E>();

	/**
	 * Add a listener to be notified by the dispatcher.
	 * 
	 * @param listener
	 * @param listen
	 *            true in order to add the listener, false in order to remove it
	 */
	public void listen(E listener, boolean listen) {
		if (listen) {
			listeners.add(listener);
		} else {
			listeners.remove(listener);
		}
	}

	/**
	 * Notify the listeners
	 * 
	 * @param notifier
	 *            controller to perform the notification actions
	 */
	public void notifyListeners(final Notifier<E> notifier) {
		Set<E> listenersSet = new HashSet<E>(listeners);
		for (final E listener : listenersSet) {
			// Notify the listener in a new thread
			Scheduler.getInstance().execute(new Runnable() {
				@Override
				public void run() {
					notifier.notify(listener);
				}
			});

		}
	}
}
