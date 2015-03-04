package cl.niclabs.adkmobile.monitor.data.constants;

/**
 * Defines the location state
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 */
public enum LocationState {
	ENABLED(1), DISABLED(0);
	
	private int value;
	
	private LocationState(int value) {
		this.value = value;
	}
	
	public int value() {
		return this.value;
	}
}