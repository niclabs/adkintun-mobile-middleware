package cl.niclabs.adkmobile.utils;

import cl.niclabs.adkmobile.utils.Listener;

/**
 * Generic dispatcher for the observer design pattern.
 * 
 * This is a stub class for backwards compatibility
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 * @deprecated Use cl.niclabs.android.utils.Dispatcher
 */
public class Dispatcher<L extends Listener> extends cl.niclabs.android.utils.Dispatcher<L> {
	/**
	 * Defines a notification behavior for the dispatcher
	 * 
	 * @author Felipe Lalanne <flalanne@niclabs.cl>
	 * @deprecated This is a replacement enum to provide backwards compatibility
	 * projects must use cl.niclabs.android.utils.Dispatcher.Behavior instead
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
	 * Constructor provided for backwards compatibility
	 * @param behavior
	 * @deprecated the constructor from cl.niclabs.android.utils.Dispatcher must be used
	 */
	public Dispatcher(Behavior behavior) {
		super(fromBehavior(behavior));
	}
	
	/**
	 * Constructor provided for backwards compatibility
	 * 
	 * @deprecated the constructor from cl.niclabs.android.utils.Dispatcher must be used
	 */
	public Dispatcher() {}
	
	/**
	 * Produce a cl.niclabs.android.utils.Dispatcher.Behavior from a cl.niclabs.adkintunmobile.utils.Dispatcher.Behavior
	 * @param behavior
	 * @return
	 */
	private static cl.niclabs.android.utils.Dispatcher.Behavior fromBehavior(Behavior behavior) {
		switch (behavior) {
		case RUN_ON_MAIN_THREAD:
			return cl.niclabs.android.utils.Dispatcher.Behavior.RUN_ON_MAIN_THREAD;
		case RUN_ON_NEW_THREAD:
			return cl.niclabs.android.utils.Dispatcher.Behavior.RUN_ON_NEW_THREAD;
		case RUN_ON_SAME_THREAD:
			return cl.niclabs.android.utils.Dispatcher.Behavior.RUN_ON_SAME_THREAD;
		default:
			return cl.niclabs.android.utils.Dispatcher.Behavior.RUN_ON_SAME_THREAD;
		}
	}
}