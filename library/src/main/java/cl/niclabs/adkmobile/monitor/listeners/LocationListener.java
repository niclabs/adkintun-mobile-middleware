package cl.niclabs.adkmobile.monitor.listeners;

import cl.niclabs.adkmobile.monitor.data.LocationObservation;
import cl.niclabs.adkmobile.monitor.data.StateChange;

/**
 *  TODO Put here a description of what this class does.
 *
 * @author nico <nicolas@niclabs.cl>
 *         Created 5-11-2013.
 */
public interface LocationListener extends MonitorListener {
	/**
	 * Called on positioning change
	 * @param locationState
	 */
	public void onLocationChanged(LocationObservation locationState);
	
	/**
	 * Called on location state change (ENABLED/DISABLED)
	 * @param state
	 */
	public void onLocationStateChanged(StateChange state);
}
