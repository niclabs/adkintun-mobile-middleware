package cl.niclabs.adkmobile.monitor.listeners;

import cl.niclabs.adkmobile.monitor.data.StateChange;

/**
 * TODO Put here a description of what this class does.
 *
 * @author Mauricio Castro <mauricio@niclabs.cl>.
 *         Created 17-10-2013.
 */
public interface ScreenListener extends MonitorListener {
	/**
	 * Inform the listener of a telephony change
	 * @param screenState the new screen state data
	 */
	public void onScreenStateChange(StateChange screenState);
}
