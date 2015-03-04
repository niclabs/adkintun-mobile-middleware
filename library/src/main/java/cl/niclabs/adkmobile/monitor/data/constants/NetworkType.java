package cl.niclabs.adkmobile.monitor.data.constants;

import android.telephony.TelephonyManager;

/**
 * Network types as defined in android.telephony.TelephonyManager
 *
 * @author Mauricio Castro <mauricio@niclabs.cl>.
 *         Created 17-10-2013.
 */
public enum NetworkType {
	RTT(1), CDMA(2), EDGE(3), EHRPD(4), EVDO_0(5), EVDO_A(6), EVDO_B(7), GPRS(
			8), HSDPA(9), HSPA(10), HSPAP(11), HSUPA(12), IDEN(13), LTE(14), UMTS(
			15), UNKNOWN(16), OTHER(0);
	
	/**
	 * Get the network type from the TelephonyManager constants
	 * 
	 * @param value connectivity identifier according to TelephonyManager constants
	 */
	public static NetworkType valueOf(int value) {
		switch (value) {
			case TelephonyManager.NETWORK_TYPE_1xRTT:
				return RTT;
			case TelephonyManager.NETWORK_TYPE_CDMA:
				return CDMA;
			case TelephonyManager.NETWORK_TYPE_EDGE:
				return EDGE;
			case TelephonyManager.NETWORK_TYPE_EHRPD:
				return EHRPD;
			case TelephonyManager.NETWORK_TYPE_EVDO_0:
				return EVDO_0;
			case TelephonyManager.NETWORK_TYPE_EVDO_A:
				return EVDO_A;
			case TelephonyManager.NETWORK_TYPE_EVDO_B:
				return EVDO_B;
			case TelephonyManager.NETWORK_TYPE_GPRS:
				return GPRS;
			case TelephonyManager.NETWORK_TYPE_HSDPA:
				return HSDPA;
			case TelephonyManager.NETWORK_TYPE_HSPA:
				return HSPA;
			case TelephonyManager.NETWORK_TYPE_HSPAP:
				return HSPAP;
			case TelephonyManager.NETWORK_TYPE_HSUPA:
				return HSUPA;
			case TelephonyManager.NETWORK_TYPE_IDEN:
				return IDEN;
			case TelephonyManager.NETWORK_TYPE_LTE:
				return LTE;
			case TelephonyManager.NETWORK_TYPE_UMTS:
				return UMTS;
			case TelephonyManager.NETWORK_TYPE_UNKNOWN:
				return UNKNOWN;
		}
		return OTHER;
	}
	
	public static NetworkType getInstance(int value) {
		for (NetworkType n: NetworkType.values()) {
			if (n.value() == value) {
				return n;
			}
		}
		return OTHER;
	}
	
	int value;

	private NetworkType(int value) {
		this.value = value;
	}

	public int value() {
		return this.value;
	}	
}