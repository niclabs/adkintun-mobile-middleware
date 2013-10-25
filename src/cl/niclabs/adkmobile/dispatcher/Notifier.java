package cl.niclabs.adkmobile.dispatcher;

/**
 * Defines the action to perform on notification of the listener
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 *
 */
public interface Notifier<E extends Listener> {
	/**
	 * Notify the listener of a specific action. Is the duty of implementors
	 * to check that the type of the listener is correct.
	 * 
	 * @param listener
	 */
	public void notify(E listener);
}
