package cl.niclabs.adkmobile.monitor.data;

import cl.niclabs.adkmobile.monitor.Monitor;
import cl.niclabs.android.utils.Time;

public class TrafficObservation extends AbstractObservation<TrafficObservation> {
	private int networkType;
	private long rxBytes;
	private Long rxPackets;
	
	private Long tcpRxBytes;
	private Long tcpRxSegments;
	private Long tcpTxBytes;
	private Long tcpTxSegments;
	
	private long txBytes;
	private Long txPackets;
	
	private Integer uid;
	
	/**
	 * Required by Sugar ORM. Create instance with creation time as timestamp
	 * and eventType as Monitor.TRAFFIC. Usage of this constructor is not
	 * recommended.
	 */
	public TrafficObservation() {
		super(Monitor.TRAFFIC, Time.currentTimeMillis());
	}
	
	public TrafficObservation(int eventType, long timestamp) {
		super(eventType, timestamp);
	}

	/**
	 * 
	 * @return network type (wifi/mobile) 
	 */
	public int getNetworkType() {
		return networkType;
	}

	/**
	 * Received bytes for the interface being monitored (wifi/mobile). In the case of application traffic, this method
	 * returns the total of received bytes in all interfaces
	 * 
	 * @return total received bytes
	 */
	public long getRxBytes() {
		return rxBytes;
	}

	/**
	 * Received packets for the interface being monitored (wifi/mobile). In the case of application traffic, this method
	 * returns the total of received packets in all interfaces
	 * 
	 * @return total received packets
	 */
	public Long getRxPackets() {
		return rxPackets;
	}

	/**
	 * TCP received bytes for the interface being monitored (wifi/mobile). In
	 * the case of application traffic, this method returns null.
	 * 
	 * @return tcp received bytes
	 */
	public Long getTcpRxBytes() {
		return tcpRxBytes;
	}

	/**
	 * TCP received segments for the interface being monitored (wifi/mobile). In
	 * the case of application traffic, this method returns null.
	 * 
	 * @return received segments
	 */
	public Long getTcpRxSegments() {
		return tcpRxSegments;
	}

	/**
	 * TCP sent segments for the interface being monitored (wifi/mobile). In
	 * the case of application traffic, this method returns null.
	 * 
	 * @return transmitted segments
	 */
	public Long getTcpTxBytes() {
		return tcpTxBytes;
	}

	/**
	 * TCP sent segments for the interface being monitored (wifi/mobile). In
	 * the case of application traffic, this method returns null.
	 * 
	 * @return transmitted segments
	 */
	public Long getTcpTxSegments() {
		return tcpTxSegments;
	}

	/**
	 * Transmitted bytes for the interface being monitored (wifi/mobile). In the case of application traffic, this method
	 * returns the total of transmitted bytes in all interfaces
	 * 
	 * @return transmitted bytes
	 */
	public long getTxBytes() {
		return txBytes;
	}

	/**
	 * Transmitted packets for the interface being monitored (wifi/mobile). In the case of application traffic, this method
	 * returns the total of transmitted packets in all interfaces
	 * 
	 * @return transmitted packets
	 */
	public Long getTxPackets() {
		return txPackets;
	}

	/**
	 * 
	 * @return uid of the application monitored, null if eventType != TRAFFIC_UID
	 */
	public Integer getUid() {
		return uid;
	}

	public void setNetworkType(int networkType) {
		this.networkType = networkType;
	}

	public void setRxBytes(long rxBytes) {
		this.rxBytes = rxBytes;
	}

	public void setRxPackets(Long rxPackets) {
		this.rxPackets = rxPackets;
	}

	public void setTcpRxBytes(long tcpRxBytes) {
		this.tcpRxBytes = tcpRxBytes;
	}

	public void setTcpRxSegments(Long tcpRxSegments) {
		this.tcpRxSegments = tcpRxSegments;
	}

	public void setTcpTxBytes(Long tcpTxBytes) {
		this.tcpTxBytes = tcpTxBytes;
	}

	public void setTcpTxSegments(Long tcpTxSegments) {
		this.tcpTxSegments = tcpTxSegments;
	}

	public void setTxBytes(long txBytes) {
		this.txBytes = txBytes;
	}

	public void setTxPackets(Long txPackets) {
		this.txPackets = txPackets;
	}

	public void setUid(Integer uid) {
		this.uid = uid;
	}

}
