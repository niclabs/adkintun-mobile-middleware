package cl.niclabs.adkmobile.monitor.listeners;


import cl.niclabs.adkmobile.monitor.data.TrafficObservation;

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
	public void onMobileTrafficChange(TrafficObservation trafficState);
	
	/**
	 * Inform the listener of a WiFi traffic change
	 * @param trafficState the new traffic data
	 */
	public void onWiFiTrafficChange(TrafficObservation trafficState);
	
	/**
	 * Inform the listener of a WiFi traffic change
	 * @param uid UID of a running process
	 * @param trafficState the new traffic data of the process with uid UID
	 */
	public void onApplicationTrafficChange(TrafficObservation trafficState);
}
