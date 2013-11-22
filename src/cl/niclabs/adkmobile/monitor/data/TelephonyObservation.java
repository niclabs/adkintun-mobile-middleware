package cl.niclabs.adkmobile.monitor.data;

import cl.niclabs.adkmobile.monitor.Monitor;
import cl.niclabs.adkmobile.monitor.data.constants.NetworkType;
import cl.niclabs.adkmobile.monitor.data.constants.TelephonyStandard;
import cl.niclabs.adkmobile.utils.Time;

public abstract class TelephonyObservation<E extends TelephonyObservation<E>> extends AbstractObservation<E> {
	protected int mcc;
	protected int mnc;
	protected int networkType;
	protected Integer signalStrength;
	protected int telephonyStandard;
	
	public TelephonyObservation() {
		super(Monitor.TELEPHONY, Time.currentTimeMillis());
	}

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
	 * @return network type (GPRS, UMTS, EDGE). See {@link NetworkType} for more info.
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

	public void setNetworkType(NetworkType networkType) {
		this.networkType = networkType.value();
	}

	public void setSignalStrength(int signalStrength) {
		this.signalStrength = signalStrength;
	}

	public void setTelephonyStandard(TelephonyStandard telephonyStandard) {
		this.telephonyStandard = telephonyStandard.value();
	}
}
