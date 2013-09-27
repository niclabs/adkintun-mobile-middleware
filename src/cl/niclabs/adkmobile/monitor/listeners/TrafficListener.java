package cl.niclabs.adkmobile.monitor.listeners;

import cl.niclabs.adkmobile.monitor.data.DataObject;

/**
 * TODO Put here a description of what this class does.
 *
 * @author Administrador.
 *         Created 27-09-2013.
 */
public interface TrafficListener extends MonitorListener {
	/**
	 * Inform the listener of a traffic change
	 * @param trafficState the new traffic data
	 */
	public void onTrafficChanged(DataObject connectivityState);
}
