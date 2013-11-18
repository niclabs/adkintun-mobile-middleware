package cl.niclabs.adkmobile.monitor.data.constants;

public enum DeviceBootState {
	BOOT(1), SHUTDOWN(2);
	
	int value;
	
	private DeviceBootState(int value){
		this.value = value;
	}
			
	public int value() {
		return value;
	}
}