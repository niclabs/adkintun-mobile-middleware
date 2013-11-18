package cl.niclabs.adkmobile.monitor.data.constants;

import android.net.ConnectivityManager;

/**
 * Network types as defined in android.net.ConnectivityManager
 *
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 */
public enum ConnectionType {
	MOBILE(1), MOBILE_DUN(2), MOBILE_HIPRI(3), MOBILE_MMS(4), MOBILE_SUPL(5), OTHER(0), WIFI(
					6), WIMAX(7);

	/**
	 * Get the network type from the ConnectivityManager constants
	 * 
	 * @param value connectivity identifier according to ConnectivityManager constants
	 */
	public static ConnectionType valueOf(int value) {
		switch (value) {
			case ConnectivityManager.TYPE_MOBILE:
				return MOBILE;
			case ConnectivityManager.TYPE_MOBILE_DUN:
				return MOBILE_DUN;
			case ConnectivityManager.TYPE_MOBILE_HIPRI:
				return MOBILE_HIPRI;
			case ConnectivityManager.TYPE_MOBILE_MMS:
				return MOBILE_MMS;
			case ConnectivityManager.TYPE_MOBILE_SUPL:
				return MOBILE_SUPL;
			case ConnectivityManager.TYPE_WIFI:
				return WIFI;
			case ConnectivityManager.TYPE_WIMAX:
				return WIMAX;
		}
		return OTHER;
	}
	
	public static ConnectionType getInstance(int value) {
		for (ConnectionType n: ConnectionType.values()) {
			if (n.value() == value) {
				return n;
			}
		}
		return OTHER;
	}
	
	public boolean isMobile() {
		switch(this) {
			case MOBILE:
			case MOBILE_DUN:
			case MOBILE_HIPRI:
			case MOBILE_MMS:
			case MOBILE_SUPL:
				return true;
			default:
				return false;
		}
	}

	int value;

	private ConnectionType(int value) {
		this.value = value;
	}

	public int value() {
		return this.value;
	}
}