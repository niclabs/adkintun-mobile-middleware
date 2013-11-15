package cl.niclabs.adkmobile.monitor.listeners;

import cl.niclabs.adkmobile.data.DataObject;

/**
 *  TODO Put here a description of what this class does.
 *
 * @author nico <nicolas@niclabs.cl>
 *         Created 5-11-2013.
 */
public interface LocationsListener extends MonitorListener{
	
	public void onGPSLocationChanged(DataObject locationState);
	public void onNetworkLocationChanged(DataObject locationState);

}
