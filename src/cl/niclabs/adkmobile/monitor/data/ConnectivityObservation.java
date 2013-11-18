package cl.niclabs.adkmobile.monitor.data;

import cl.niclabs.adkmobile.monitor.Connectivity;
import cl.niclabs.adkmobile.monitor.Monitor;

/**
 * Defines an observation for the connectivity monitor
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 */
public class ConnectivityObservation extends AbstractObservation<ConnectivityObservation> {	
	private int detailedState;
	private boolean available;
	private boolean connected;
	private boolean roaming;
	private int connectionType;
	private Integer connectionTypeOther;
	
	/**
	 * Required by Sugar ORM. Creates a new connectivity observation with
	 * time of creation as the event timestamp 
	 */
	public ConnectivityObservation() {
		super(Monitor.CONNECTIVITY, System.currentTimeMillis());
	}
	
	public ConnectivityObservation(long timestamp) {
		super(Monitor.CONNECTIVITY, timestamp);
	}
	
	/**
	 * @return the detailed network state. See {@link Connectivity.NetworkState} for more details.
	 */
	public Connectivity.NetworkState getDetailedState() {
		return Connectivity.NetworkState.getInstance(detailedState);
	}
	
	public void setDetailedState(Connectivity.NetworkState detailedState) {
		this.detailedState = detailedState.value();
	}
	
	/**
	 * 
	 * @return true if connectivity is available
	 */
	public boolean isAvailable() {
		return available;
	}
	
	public void setAvailable(boolean available) {
		this.available = available;
	}
	
	/**
	 * 
	 * @return true if data transmission is possible
	 */
	public boolean isConnected() {
		return connected;
	}
	
	public void setConnected(boolean connected) {
		this.connected = connected;
	}
	
	/**
	 * @return true if the connectivity status is roaming (note that this does not imply data availability)
	 */
	public boolean isRoaming() {
		return roaming;
	}
	
	public void setRoaming(boolean roaming) {
		this.roaming = roaming;
	}
	
	/**
	 * @return the network type (mobile, wifi, etc). See Connectivity.ConnectionType for more details
	 */
	public Connectivity.ConnectionType getConnectionType() {
		return Connectivity.ConnectionType.getInstance(connectionType);
	}
	
	public void setConnectionType(Connectivity.ConnectionType connectionType) {
		this.connectionType = connectionType.value();
	}
	
	/**
	 * @return the code for the network type if not listed in Connectivity.ConnectionType
	 */
	public Integer getConnectionTypeOther() {
		return connectionTypeOther;
	}
	
	public void setConnectionTypeOther(Integer connectionTypeOther) {
		this.connectionTypeOther = connectionTypeOther;
	}
}
