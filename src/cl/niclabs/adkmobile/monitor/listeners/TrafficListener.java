package cl.niclabs.adkmobile.monitor.listeners;


import cl.niclabs.adkmobile.data.DataObject;

/**
 * TODO Put here a description of what this class does.
 *
 * @author Administrador.
 *         Created 27-09-2013.
 */
public interface TrafficListener extends MonitorListener {
	/**
	 * Inform the listener of a mobile traffic change
	 * @param trafficState the new traffic data
	 */
	public void onMobileTrafficChanged(DataObject trafficState);
	
	/**
	 * Inform the listener of a WiFi traffic change
	 * @param trafficState the new traffic data
	 */
	public void onWiFiTrafficChanged(DataObject trafficState);
}
