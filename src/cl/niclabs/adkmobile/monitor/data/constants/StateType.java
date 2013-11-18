package cl.niclabs.adkmobile.monitor.data.constants;

import cl.niclabs.adkmobile.monitor.data.StateChange;

/**
 * Defines the state types for defining in {@link StateChange}
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 */
public enum StateType {
	SIM(1), SERVICE(2), AIRPLANE_MODE(4), SCREEN(5), DEVICE_BOOT(6), LOCATION(7), UNKNOWN(0);
	
	private int value;
	
	private StateType(int value) {
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
	public static StateType getInstance(int value) {
		for (StateType n: StateType.values()) {
			if (n.value() == value) {
				return n;
			}
		}
		return UNKNOWN;
	}
}