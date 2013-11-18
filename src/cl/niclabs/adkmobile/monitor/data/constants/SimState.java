package cl.niclabs.adkmobile.monitor.data.constants;

import android.telephony.TelephonyManager;

/**
 * SIM States as defined in android.telephony.TelephonyManager
 *
 * @author Mauricio Castro <mauricio@niclabs.cl>.
 *         Created 17-10-2013.
 */
public enum SimState {
	ABSENT(1), NETWORK_LOCKED(2), PIN_REQUIRED(3), PUK_REQUIRED(4), READY(5), UNKNOWN(6),
	OTHER(0);
	
	/**
	 * Get the SIM state from TelephonyManager SIM_STATE values
	 * @param value
	 * @return
	 */
	public static SimState valueOf(int value) {
		switch (value) {
		case TelephonyManager.SIM_STATE_ABSENT:
			return ABSENT;

		case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
			return NETWORK_LOCKED;

		case TelephonyManager.SIM_STATE_PIN_REQUIRED:
			return PIN_REQUIRED;

		case TelephonyManager.SIM_STATE_PUK_REQUIRED:
			return PUK_REQUIRED;

		case TelephonyManager.SIM_STATE_READY:
			return READY;

		case TelephonyManager.SIM_STATE_UNKNOWN:
			return UNKNOWN;
		}
		
		return UNKNOWN;
	}
	
	int value;
	
	private SimState(int value) {
		this.value = value;
	}

	public int value() {
		return this.value;
	}
}