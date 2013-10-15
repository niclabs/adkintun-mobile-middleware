package cl.niclabs.adkmobile.monitor.listeners;

import cl.niclabs.adkmobile.monitor.data.DataObject;

/**
 * TODO Put here a description of what this class does.
 *
 * @author Administrador.
 *         Created 04-10-2013.
 */
public interface TelephonyListener extends MonitorListener{
	/**
	 * Inform the listener of a telephony change
	 * @param trafficState the new telephony data
	 */
	public void onMobileTelephonyChanged(DataObject telephonyState);
}
