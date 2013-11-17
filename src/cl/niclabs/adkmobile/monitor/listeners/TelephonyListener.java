package cl.niclabs.adkmobile.monitor.listeners;

import cl.niclabs.adkmobile.monitor.data.StateChange;
import cl.niclabs.adkmobile.monitor.data.TelephonyObservation;

/**
 * TODO Put here a description of what this class does.
 *
 * @author Administrador.
 *         Created 04-10-2013.
 */
public interface TelephonyListener extends MonitorListener{
	/**
	 * Inform the listener of a telephony change
	 * @param trafficState the new telephony data
	 */
	public void onMobileTelephonyChange(TelephonyObservation<?> telephonyState);
	
	/**
	 * Notified when the SIM state changes
	 * @param stateChange
	 */
	public void onSimStateChange(StateChange stateChange);
	
	/**
	 * Notified when the Service state changes
	 * @param stateChange
	 */
	public void onServiceStateChange(StateChange stateChange);
	
	/**
	 * Notified when the Data Connection state changes
	 * @param stateChange
	 */
	public void onDataConnectionStateChange(StateChange stateChange);
	
	/**
	 * Notified when the airplane mode changes
	 * @param stateChange
	 */
	public void onAirplaneModeChange(StateChange stateChange);
}
