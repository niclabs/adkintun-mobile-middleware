package cl.niclabs.adkmobile.monitor.listeners;

import cl.niclabs.adkmobile.monitor.data.DataObject;


public interface ConnectivityListener extends MonitorListener {
	/**
	 * Inform the listener of a connectivity change
	 * @param connectivityState the new connectivity data
	 */
	public void onConnectivityChanged(DataObject connectivityState);
	
	/**
	 * Inform the listener when the service has detected a wifi connection.
	 * 
	 * Should be called on starting the connectivity service if the WiFi connection
	 * is active
	 */
	public void onWifiConnection();
	
	/**
	 * Inform the listener when the service has detected a mobile connection
	 * 
	 * Should be called on starting the connectivity service if the Mobile connection
	 * is active
	 */
	public void onMobileConnection();
	
	/**
	 * Inform the listener when the service has started roaming or when the data roaming 
	 * state is changed
	 * 
	 * Should be called on starting the connectivity service if the device is roaming
	 * 
	 * @param dataRoamingEnabled if the roaming status has been changed
	 */
	public void onRoaming(boolean dataRoamingEnabled);
}
