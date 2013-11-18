package cl.niclabs.adkmobile.monitor.data.constants;

/**
 * ServiceState as defined in android.telephony.ServiceState
 */
public enum ServiceState {
	EMERGENCY_ONLY(1), IN_SERVICE(2), OUT_OF_SERVICE(3), POWER_OFF(4), UNKNOWN(0);
	
	/**
	 * Get the SIM state from TelephonyManager SIM_STATE values
	 * @param value
	 * @return
	 */
	public static ServiceState valueOf(android.telephony.ServiceState state) {
		switch(state.getState()) {
		case android.telephony.ServiceState.STATE_EMERGENCY_ONLY:
			return EMERGENCY_ONLY;
		case android.telephony.ServiceState.STATE_IN_SERVICE:
			return IN_SERVICE;
		case android.telephony.ServiceState.STATE_OUT_OF_SERVICE:
			return EMERGENCY_ONLY;
		case android.telephony.ServiceState.STATE_POWER_OFF:
			return POWER_OFF;
		}
		return UNKNOWN;
	}
	
	int value;
	
	private ServiceState(int value) {
		this.value = value;
	}

	public int value() {
		return this.value;
	}
}