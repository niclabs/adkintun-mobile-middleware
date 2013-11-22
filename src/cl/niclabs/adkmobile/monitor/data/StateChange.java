package cl.niclabs.adkmobile.monitor.data;

import cl.niclabs.adkmobile.monitor.data.constants.StateType;
import cl.niclabs.adkmobile.utils.Time;

public class StateChange extends AbstractObservation<StateChange> {
	private int state;
	private Integer stateType;
	
	/**
	 * Required by Sugar ORM. Create instance with creation time as timestamp
	 * and eventType as 0. Usage of this constructor is not recommended.
	 */
	public StateChange() {
		super(0, Time.currentTimeMillis());
	}
	
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
	public StateType getStateType() {
		return StateType.getInstance(stateType);
	}

	public void setState(int state) {
		this.state = state;
	}

	public void setStateType(StateType stateType) {
		this.stateType = stateType.value();
	}
}
