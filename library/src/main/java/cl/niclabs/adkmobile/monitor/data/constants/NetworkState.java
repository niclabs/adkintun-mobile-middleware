package cl.niclabs.adkmobile.monitor.data.constants;

import android.annotation.SuppressLint;
import android.net.NetworkInfo;
import android.os.Build;

/**
 * The network detailed state for recording on the database
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 */
public enum NetworkState {
	AUTHENTICATING(1), BLOCKED(2), CAPTIVE_PORTAL_CHECK(3), CONNECTED(4), CONNECTING(
			5), DISCONNECTED(6), DISCONNECTING(7), FAILED(8), IDLE(9), OBTAINING_IP_ADDRESS(
			10), UNKNOWN(0), SCANNING(11), SUSPENDED(12), VERIFYING_POOR_LINK(13);

    @SuppressLint("NewApi")
	public static NetworkState valueOf(NetworkInfo.DetailedState value) {
        switch(value) {
            case AUTHENTICATING:
                return AUTHENTICATING;
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
            default:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH
                        && value == NetworkInfo.DetailedState.BLOCKED) {
                    return BLOCKED;
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
                        && value == NetworkInfo.DetailedState.CAPTIVE_PORTAL_CHECK) {
                    return CAPTIVE_PORTAL_CHECK;
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                        && value == NetworkInfo.DetailedState.VERIFYING_POOR_LINK) {
                    return VERIFYING_POOR_LINK;
                }
                return UNKNOWN;
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
		return UNKNOWN;
	}
	
	public int value() {
		return this.value;
	}
}