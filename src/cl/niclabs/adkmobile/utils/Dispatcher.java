package cl.niclabs.adkmobile.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import android.os.Handler;
import android.os.Looper;

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
	 * Defines a notification behavior for the dispatcher
	 * 
	 * Depending on Behavior value, the dispatcher will
	 * deliver notifications on the same thread as the calling
	 * class, on a new thread or on the main thread.
	 * 
	 * @author Felipe Lalanne <flalanne@niclabs.cl>
	 */
	public static enum Behavior {
		/**
		 * Deliver the notification on a new thread
		 * by calling {@link Scheduler.execute()}
		 */
		RUN_ON_NEW_THREAD, 
		
		/**
		 * Deliver the notification on the same thread
		 * as the calling object, by running the notification
		 * immediately
		 */
		RUN_ON_SAME_THREAD, 
		
		/**
		 * Deliver the notification on the main thread, by passing
		 * the object to the main Looper. Use with caution since
		 * long executions may block the UI thread and affect
		 * responsiveness of the application.
		 */
		RUN_ON_MAIN_THREAD;
	}
	
	
	/**
	 * List of listeners by event type
	 */
	private List<E> listenersList = new CopyOnWriteArrayList<E>();
	private Set<E> listenersSet = new HashSet<E>();
		
	private Behavior behavior = Behavior.RUN_ON_NEW_THREAD;
	private Handler mainHandler = new Handler(Looper.getMainLooper());
	
	/**
	 * Creates a new dispatcher that notifies the new listeners on a new thread by default
	 */
	public Dispatcher() {}
		
	/**
	 * Create a new dispatcher with the specified behavior
	 * @param behavior
	 */
	public Dispatcher(Behavior behavior) {
		this.behavior = behavior;
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
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					notifier.notify(listener);
				}
			};
			
			switch (behavior) {
			case RUN_ON_MAIN_THREAD:
				mainHandler.post(runnable);
				break;
			case RUN_ON_NEW_THREAD:
				Scheduler.getInstance().execute(runnable);
				break;
			case RUN_ON_SAME_THREAD:
				runnable.run();
				break;
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