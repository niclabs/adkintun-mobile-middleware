package cl.niclabs.adkmobile.monitor.data.constants;

/**
 * State of Airplane Mode
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 *
 */
public enum AirplaneModeState {
	ON(1), OFF(2);

	int value;
	
	private AirplaneModeState(int value){
		this.value = value;
	}
			
	public int value() {
		return value;
	}
}