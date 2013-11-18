package cl.niclabs.adkmobile.monitor.data.constants;

import android.net.NetworkInfo;

/**
 * The network detailed state for recording on the database
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 */
public enum NetworkState {
	AUTHENTICATING(1), BLOCKED(2), CAPTIVE_PORTAL_CHECK(3), CONNECTED(4), CONNECTING(
			5), DISCONNECTED(6), DISCONNECTING(7), FAILED(8), IDLE(9), OBTAINING_IP_ADDRESS(
			10), OTHER(0), SCANNING(11), SUSPENDED(12), VERIFYING_POOR_LINK(13);
	
	public static NetworkState valueOf(NetworkInfo.DetailedState value) {
		switch(value) {
		case AUTHENTICATING:
			return AUTHENTICATING;
		case BLOCKED:
			return BLOCKED;
		case CAPTIVE_PORTAL_CHECK:
			return CAPTIVE_PORTAL_CHECK;
		case CONNECTED:
			return CONNECTED;
		case CONNECTING:
			return CONNECTING;
		case DISCONNECTED:
			return DISCONNECTED;
		case DISCONNECTING:
			return DISCONNECTING;
		case FAILED:
			return FAILED;
		case IDLE:
			return IDLE;
		case OBTAINING_IPADDR:
			return OBTAINING_IP_ADDRESS;
		case SCANNING:
			return SCANNING;
		case SUSPENDED:
			return SUSPENDED;
		case VERIFYING_POOR_LINK:
			return VERIFYING_POOR_LINK;
		default:
			return OTHER;
		}
	}
	
	private int value;
	
	private NetworkState(int value) {
		this.value = value;
	}
	
	public static NetworkState getInstance(int value) {
		for (NetworkState n: NetworkState.values()) {
			if (n.value() == value) {
				return n;
			}
		}
		return OTHER;
	}
	
	public int value() {
		return this.value;
	}
}