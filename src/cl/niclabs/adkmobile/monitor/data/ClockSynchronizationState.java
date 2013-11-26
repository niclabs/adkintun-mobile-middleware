package cl.niclabs.adkmobile.monitor.data;

import cl.niclabs.adkmobile.monitor.Monitor;
import cl.niclabs.adkmobile.monitor.data.constants.ClockState;

/**
 * Saves the clock synchronization state. If the clock is synchronized realTime
 * will return a value different than 0. getTimestamp will always return
 * System.currentTimeMillis() no matter what the synchronization status is
 * 
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 */
public class ClockSynchronizationState extends AbstractObservation<ClockSynchronizationState> {
	private long realTime;
	private int state;
	
	/**
	 * Required by Sugar ORM. 
	 */
	public ClockSynchronizationState() {
		super(Monitor.CLOCK, System.currentTimeMillis());
	}
	
	public ClockSynchronizationState(long timestamp) {
		super(Monitor.CLOCK, timestamp);
	}

	public long getRealTime() {
		return realTime;
	}

	public ClockState getState() {
		return ClockState.getInstance(state);
	}

	public void setRealTime(long realTime) {
		this.realTime = realTime;
	}

	public void setState(ClockState state) {
		this.state = state.value();
	}
}
