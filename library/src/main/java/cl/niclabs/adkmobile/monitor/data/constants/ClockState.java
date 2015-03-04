package cl.niclabs.adkmobile.monitor.data.constants;

public enum ClockState {
	SYNCHRONIZED(1), UNSYNCHRONIZED(0), UNKNOWN(2);
	
	private int value;
	private ClockState(int value) {
		this.value = value;
	}
	
	public int value() {
		return value;
	}
	
	/**
	 * The instance of StateType that corresponds to the given value
	 * 
	 * @param value
	 * @return the instance of StateType corresponding to the value or UNKNOWN if it does not match anything
	 */
	public static ClockState getInstance(int value) {
		for (ClockState n: ClockState.values()) {
			if (n.value() == value) {
				return n;
			}
		}
		return UNSYNCHRONIZED;
	}
}
