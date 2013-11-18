package cl.niclabs.adkmobile.monitor.data.constants;

/**
 * Defines the screen state (on, off, locked, etc)
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 *
 */
public enum ScreenState {
	ON(1), OFF(2), LOCKED(3), UNLOCKED(4);
	
	int value;
	
	private ScreenState(int value) {
		this.value = value;
	}
	
	public int value() {
		return value;
	}
}