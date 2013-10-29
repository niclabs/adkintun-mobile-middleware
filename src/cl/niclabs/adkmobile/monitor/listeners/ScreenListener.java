package cl.niclabs.adkmobile.monitor.listeners;

import cl.niclabs.adkmobile.data.DataObject;

/**
 * TODO Put here a description of what this class does.
 *
 * @author Administrador.
 *         Created 17-10-2013.
 */
public interface ScreenListener  extends MonitorListener {
	/**
	 * Inform the listener of a screen change
	 * @param Screen state the new telephony data
	 */
	public void onMobileScreenChanged(DataObject telephonyState);
}
