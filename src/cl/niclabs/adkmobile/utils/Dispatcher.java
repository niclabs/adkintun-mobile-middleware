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
	private List<E> listenersList = new CopyOnWriteArrayList<E>();
	private Set<E> listenersSet = new HashSet<E>();
	
	private boolean runOnNewThread = true;
	
	/**
	 * Creates a new dispatcher that notifies the new listeners on a new thread by default
	 */
	public Dispatcher() {}
	
	/**
	 * Creates a new dispatcher
	 * @param runOnNewThread defines if the listeners are notified on a new thread or immediately
	 */
	public Dispatcher(boolean runOnNewThread) {
		this.runOnNewThread = runOnNewThread;
	}
 
	/**
	 * Add a listener to be notified by the dispatcher.
	 * 
	 * @param listener
	 * @param listen true in order to add the listener, false in order to remove it
	 */
	public void listen(E listener, boolean listen) {
		if (listen) {
			listenersList.add(listener);
			listenersSet.add(listener);
		} else {
			listenersList.remove(listener);
			if (!listenersList.contains(listener)) {
				listenersSet.remove(listener);
			}
		}
	}

	/**
	 * Notify the listeners
	 * 
	 * @param notifier
	 *            controller to perform the notification actions
	 */
	public void notifyListeners(final Notifier<E> notifier) {
		for (final E listener : listenersSet) {
			if (runOnNewThread) {
				// Notify the listener in a new thread
				Scheduler.getInstance().execute(new Runnable() {
					@Override
					public void run() {
						notifier.notify(listener);
					}
				});
			}
			else {
				notifier.notify(listener);
			}
		}
	}
	
	/**
	 * Return number of listeners
	 * @return
	 */
	public int size() {
		return listenersSet.size();
	}
}