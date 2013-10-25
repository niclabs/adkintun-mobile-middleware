package cl.niclabs.adkmobile.monitor.proxies;

public interface ConnectivityStatusListener extends MonitorProxyListener {
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
	 * @param dataRoamingEnabled true if data roaming is enabled in the device
	 */
	public void onRoaming(boolean dataRoamingEnabled);
}
