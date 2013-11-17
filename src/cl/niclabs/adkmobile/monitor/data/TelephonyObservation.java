package cl.niclabs.adkmobile.monitor.data;

import cl.niclabs.adkmobile.monitor.Monitor;
import cl.niclabs.adkmobile.monitor.Telephony;

public abstract class TelephonyObservation<E extends TelephonyObservation<E>> extends AbstractObservation<E> {
	protected int mcc;
	protected int mnc;
	protected int networkType;
	protected Integer signalStrength;
	protected int telephonyStandard;

	public TelephonyObservation(long timestamp) {
		super(Monitor.TELEPHONY, timestamp);
	}
	/**
	 * 
	 * @return Mobile Country Code
	 */
	public int getMcc() {
		return mcc;
	}
	
	/**
	 * 
	 * @return Mobile Network Code
	 */
	public int getMnc() {
		return mnc;
	}

	/**
	 * 
	 * @return network type (GPRS, UMTS, EDGE). See {@link Telephony.NetworkType} for more info.
	 */
	public int getNetworkType() {
		return networkType;
	}

	/**
	 * 
	 * @return signal strength in dBm or null if not available
	 */
	public Integer getSignalStrength() {
		return signalStrength;
	}

	/**
	 * @return telephony standard (GPRS, CDMA).
	 */
	public int getTelephonyStandard() {
		return telephonyStandard;
	}

	public void setMcc(int mcc) {
		this.mcc = mcc;
	}

	public void setMnc(int mnc) {
		this.mnc = mnc;
	}

	public void setNetworkType(Telephony.NetworkType networkType) {
		this.networkType = networkType.getValue();
	}

	public void setSignalStrength(int signalStrength) {
		this.signalStrength = signalStrength;
	}

	public void setTelephonyStandard(Telephony.Standard telephonyStandard) {
		this.telephonyStandard = telephonyStandard.value();
	}
}
