package cl.niclabs.adkmobile.monitor.listeners;

import cl.niclabs.adkmobile.data.DataObject;


public interface ConnectivityListener extends MonitorListener {
	/**
	 * Inform the listener of a connectivity change
	 * @param connectivityState the new connectivity data
	 */
	public void onConnectivityChanged(DataObject connectivityState);
}
