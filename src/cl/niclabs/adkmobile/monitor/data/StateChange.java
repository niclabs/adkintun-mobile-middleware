package cl.niclabs.adkmobile.monitor.data;

public class StateChange extends AbstractObservation<StateChange> {
	private int state;
	private Integer type;
	
	public StateChange(int eventType, long timestamp) {
		super(eventType, timestamp);
	}

	/**
	 * 
	 * @return state of the event
	 */
	public int getState() {
		return state;
	}

	/**
	 * Indicates the type of the state (e.g. SimState, ServiceState for Telephony) 
	 * when multiple states are being monitored by the same service
	 * 
	 * @return type of state
	 */
	public Integer geType() {
		return type;
	}

	public void setState(int state) {
		this.state = state;
	}

	public void setType(Integer stateType) {
		this.type = stateType;
	}
}
