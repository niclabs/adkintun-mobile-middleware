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
	 * 
	 * @param roaming true if the device is roaming
	 */
	public void onMobileConnection(boolean roaming);
	
	/**
	 * Inform the listener when the service has started roaming or when the data roaming 
	 * state is changed
	 * 
	 * Should be called on starting the connectivity service if the device is roaming
	 * 
	 * @param dataRoamingEnabled true if data roaming is enabled in the device
	 */
	public void onRoaming(boolean dataRoamingEnabled);
	
	
	/**
	 * Used to inform the listener that the device has disconnected from the network.
	 */
	public void onNetworkDisconnection();
	
	/**
	 * Used to inform the listener that the device has connected to a network
	 * after being disconnected and the connection is available
	 * 
	 * @param boolean isMobileRoaming, true if the connection is mobile and on roaming
	 */
	public void onNetworkConnection(boolean isMobileRoaming);
}
