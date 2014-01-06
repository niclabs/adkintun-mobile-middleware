package cl.niclabs.adkmobile.monitor.listeners;

import cl.niclabs.adkmobile.monitor.data.StateChange;
import cl.niclabs.adkmobile.monitor.data.TelephonyObservation;

/**
 * TODO Put here a description of what this class does.
 *
 * @author Mauricio Castro.
 *         Created 04-10-2013.
 */
public interface TelephonyListener extends MonitorListener{
	/**
	 * Inform the listener of a telephony change. 
	 * 
	 * This method is called each time the antenna, signal strength or network type changes.
	 * However, if signal strength changes the same instance of TelephonyObservation is used
	 * changing only the TelephonyObservation.signalStrength and GsmObservation.signalBer instance
	 * variables. 
	 * 
	 * This allows storage services to avoid saving different entries for the same observation when
	 * all that changes is a Sample variable 
	 * 
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
	 * @deprecated not used
	 */
	public void onDataConnectionStateChange(StateChange stateChange);
	
	/**
	 * Notified when the airplane mode changes
	 * @param stateChange
	 */
	public void onAirplaneModeChange(StateChange stateChange);
}
