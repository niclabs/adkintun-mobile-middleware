package cl.niclabs.adkmobile.monitor.data;

import cl.niclabs.adkmobile.monitor.Monitor;
import cl.niclabs.adkmobile.utils.Time;

public class TrafficSummaryObservation extends
		AbstractObservation<TrafficSummaryObservation> {

	private long rxBytesScreenOn;
	private long rxBytesScreenOff;

	private Integer uid;

	/**
	 * Required by Sugar ORM. Create instance with creation time as timestamp
	 * and eventType as Monitor.TRAFFIC. Usage of this constructor is not
	 * recommended.
	 */

	public TrafficSummaryObservation() {
		super(Monitor.TRAFFIC_APPLICATION, Time.currentTimeMillis());
	}

	public TrafficSummaryObservation(int eventType, long timestamp) {
		super(eventType, timestamp);
	}

	public TrafficSummaryObservation(long timestamp, int uid,
			long rxBytesScreenOn, long rxBytesScreenOff) {
		super(Monitor.TRAFFIC_APPLICATION, timestamp);
		this.uid = uid;
		this.rxBytesScreenOn = rxBytesScreenOn;
		this.rxBytesScreenOff = rxBytesScreenOff;
	}

	/**
	 * 
	 * @return uid of the application monitored, null if eventType !=
	 *         TRAFFIC_UID
	 */

	public Integer getUid() {
		return uid;
	}

	public long getRxBytesScreenOn() {
		return rxBytesScreenOn;
	}

	public void setRxBytesScreenOn(long rxBytesScreenOn) {
		this.rxBytesScreenOn = rxBytesScreenOn;
	}

	public void updateRxBytesScreenOn(long rxBytesScreenOn) {
		this.rxBytesScreenOn += rxBytesScreenOn;
	}

	public long getRxBytesScreenOff() {
		return rxBytesScreenOff;
	}

	public void setRxBytesScreenOff(long rxBytesScreenOff) {
		this.rxBytesScreenOff = rxBytesScreenOff;
	}

	public void updateRxBytesScreenOff(long rxBytesScreenOff) {
		this.rxBytesScreenOff += rxBytesScreenOff;
	}

	public void setUid(Integer uid) {
		this.uid = uid;
	}

}
