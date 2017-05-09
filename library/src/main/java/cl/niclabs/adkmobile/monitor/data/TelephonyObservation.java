package cl.niclabs.adkmobile.monitor.data;

import cl.niclabs.adkmobile.monitor.Monitor;
import cl.niclabs.adkmobile.monitor.data.constants.NetworkType;
import cl.niclabs.adkmobile.monitor.data.constants.TelephonyStandard;
import cl.niclabs.android.utils.Time;

public abstract class TelephonyObservation<E extends TelephonyObservation<E>> extends AbstractObservation<E> {
	protected int mcc;
	protected int mnc;
	protected int networkType;
	
	protected int telephonyStandard;

	private Sample signalStrength;
	
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
	public NetworkType getNetworkType() {
		return NetworkType.getInstance(networkType);
	}
	
	/**
	 * Get the signal strength sample
	 * @return
	 */
	public Sample getSignalStrength() {
		return signalStrength;
	}

	@Override
	public void save() {
		if (signalStrength != null)
			signalStrength.save();
		
		super.save();
	}

	/**
	 * @return telephony standard (GPRS, CDMA).
	 */
	public TelephonyStandard getTelephonyStandard() {
		return TelephonyStandard.getInstance(telephonyStandard);
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

	public void setTelephonyStandard(TelephonyStandard telephonyStandard) {
		this.telephonyStandard = telephonyStandard.value();
	}
	
	/**
	 * Update signal strength with a new value
	 * @param signalStrength
	 */
	public void updateSignalStrength(int signalStrength) {
		this.timestamp = System.currentTimeMillis();

		if (this.signalStrength == null)
			this.signalStrength = new Sample();
		
		this.signalStrength.update(signalStrength);
	}
}
