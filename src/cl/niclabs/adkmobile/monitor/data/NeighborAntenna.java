package cl.niclabs.adkmobile.monitor.data;

import cl.niclabs.adkmobile.data.DoNotSerialize;
import cl.niclabs.adkmobile.data.Persistent;

public class NeighborAntenna extends Persistent<NeighborAntenna> {
	private int gsmCid;
	private int gsmLac;
	
	@DoNotSerialize
	private GsmObservation gsmObservation;
	
	private int gsmPsc;
	
	private Integer signalStrength;
	
	/**
	 * @return cell-id of the neighbor or -1 if unknown
	 */
	public int getGsmCid() {
		return gsmCid;
	}

	/**
	 * 
	 * @return location area code of the neighbor or -1 if unknown
	 */
	public int getGsmLac() {
		return gsmLac;
	}

	/**
	 * @return the observation to which this neighbor antenna is associated
	 */
	public GsmObservation getGsmObservation() {
		return gsmObservation;
	}
	
	/**
	 * 
	 * @return primary scrambling code for UMTS, or -1 if unknown or GSM
	 */
	public int getGsmPsc() {
		return gsmPsc;
	}
	
	/**
	 * 
	 * @return signal strength of the neigbor in dbm or null if unknown
	 */
	public Integer getSignalStrength() {
		return signalStrength;
	}
	
	public void setGsmCid(int gsmCid) {
		this.gsmCid = gsmCid;
	}
	
	public void setGsmLac(int gsmLac) {
		this.gsmLac = gsmLac;
	}
	
	public void setGsmObservation(GsmObservation gsmObservation) {
		this.gsmObservation = gsmObservation;
	}
	
	public void setGsmPsc(int gsmPsc) {
		this.gsmPsc = gsmPsc;
	}
	
	public void setSignalStrength(Integer signalStrength) {
		this.signalStrength = signalStrength;
	}
}
