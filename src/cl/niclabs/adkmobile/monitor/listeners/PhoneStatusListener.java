package cl.niclabs.adkmobile.monitor.listeners;

import cl.niclabs.adkmobile.data.DataObject;

/**
 * TODO Put here a description of what this class does.
 *
 * @author Administrador.
 *         Created 30-10-2013.
 */
public interface PhoneStatusListener extends MonitorListener {
	
	public void onMobilePhoneStatusChanged(DataObject telephonyState);

}
