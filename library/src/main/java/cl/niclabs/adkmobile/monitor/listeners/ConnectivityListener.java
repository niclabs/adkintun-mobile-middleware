package cl.niclabs.adkmobile.monitor.listeners;

import cl.niclabs.adkmobile.monitor.data.ConnectivityObservation;


public interface ConnectivityListener extends MonitorListener {
	/**
	 * Inform the listener of a connectivity change
	 * @param connectivityState the new connectivity data
	 */
	public void onConnectivityChange(ConnectivityObservation connectivityState);
}
