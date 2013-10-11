package cl.niclabs.adkmobile.monitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import cl.niclabs.adkmobile.monitor.data.ContentValuesDataObject;
import cl.niclabs.adkmobile.monitor.data.DataFields;
import cl.niclabs.adkmobile.monitor.data.DataObject;
import cl.niclabs.adkmobile.monitor.listeners.ConnectivityListener;
import cl.niclabs.adkmobile.monitor.listeners.MonitorListener;

/**
 * Implements monitoring of Internet connectivity change. Connectivity is
 * notified by the system as a broadcast for the CONNECTIVITY_ACTION Intent.
 * Since this class needs to check the current NetworkState, it needs the
 * permission ACCESS_NETWORK_STATE.
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 */
public class Connectivity extends Monitor {
	public static class ConnectivityData implements DataFields {
		public static final String DETAILED_STATE = "detailed_state";
		
		public static final String IS_CONNECTED = "is_connected";
		public static final String IS_ROAMING = "is_roaming";
		public static final String NETWORK_TYPE = "network_type";
		/**
		 * For devices with API level 13.
		 */
		public static final String NETWORK_TYPE_OTHER = "network_type_other";

		private ConnectivityData() {

		}
	}
	
	/**
	 * The network detailed state for recording on the database
	 * 
	 * @author Felipe Lalanne <flalanne@niclabs.cl>
	 */
	public static enum NetworkState {
		AUTHENTICATING(1), BLOCKED(1), CAPTIVE_PORTAL_CHECK(3), CONNECTED(4), CONNECTING(
				5), DISCONNECTED(6), DISCONNECTING(7), FAILED(8), IDLE(9), OBTAINING_IP_ADDRESS(
				10), OTHER(0), SCANNING(11), SUSPENDED(12), VERIFYING_POOR_LINK(13);
		
		public static NetworkState getType(NetworkInfo.DetailedState value) {
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
		
		public int getValue() {
			return this.value;
		}
	}

	/**
	 * Network types as defined in android.net.ConnectivityManager
	 *
	 * 
	 * @author Felipe Lalanne <flalanne@niclabs.cl>
	 */
	public static enum NetworkType {
		MOBILE(1), MOBILE_DUN(2), MOBILE_HIPRI(3), MOBILE_MMS(4), MOBILE_SUPL(5), OTHER(0), WIFI(
						6), WIMAX(7);

		/**
		 * Get the network type from the ConnectivityManager constants
		 */
		public static NetworkType getType(int value) {
			switch (value) {
			case ConnectivityManager.TYPE_MOBILE:
				return MOBILE;
			case ConnectivityManager.TYPE_MOBILE_DUN:
				return MOBILE_DUN;
			case ConnectivityManager.TYPE_MOBILE_HIPRI:
				return MOBILE_HIPRI;
			case ConnectivityManager.TYPE_MOBILE_MMS:
				return MOBILE_MMS;
			case ConnectivityManager.TYPE_MOBILE_SUPL:
				return MOBILE_SUPL;
			case ConnectivityManager.TYPE_WIFI:
				return WIFI;
			case ConnectivityManager.TYPE_WIMAX:
				return WIMAX;
			}
			return OTHER;
		}

		int value;

		private NetworkType(int value) {
			this.value = value;
		}

		public int getValue() {
			return this.value;
		}
	};

	public class ServiceBinder extends Binder {
		public Connectivity getService() {
			return Connectivity.getService();
		}
	}

	/**
	 * Instance of the current service
	 */
	private static Connectivity connectivityService;

	/**
	 * Get service statically
	 * @return
	 */
	public static Connectivity getService() {
		if (connectivityService == null)
			connectivityService = new Connectivity();
		return connectivityService;
	}

	private ConnectivityData connectivityDataFields;

	private BroadcastReceiver connectivityMonitor = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (!action.equals(ConnectivityManager.CONNECTIVITY_ACTION)
					|| !isActive(MonitorManager.CONNECTIVITY_CHANGE)) {
				Log.w(TAG, "onReceived() called with intent " + intent);
				return;
			}
			// Get the NetworkInfo object
			ConnectivityManager connectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
			
			/* When no network is active
			 * the variable is null
			 * TODO: should we record this? */
			if (ni == null) {
				Log.d(TAG, "No active network");
				return; 
			}

			DataObject data = new ContentValuesDataObject();
			data.put(ConnectivityData.TIMESTAMP, System.currentTimeMillis());
			data.put(ConnectivityData.IS_CONNECTED,
					ni.isConnectedOrConnecting());

			NetworkType networkType;
			if ((networkType = NetworkType.getType(ni.getType())) == NetworkType.OTHER) {
				data.put(ConnectivityData.NETWORK_TYPE_OTHER, ni.getType());
			}
			data.put(ConnectivityData.NETWORK_TYPE, networkType.getValue());
			data.put(ConnectivityData.IS_ROAMING, ni.isRoaming());
			
			data.put(ConnectivityData.DETAILED_STATE, NetworkState.getType(ni.getDetailedState()).getValue());

			// Log new state
			Log.d(TAG, data.toString());

			/* Update the current state */
			setCurrentState(MonitorManager.CONNECTIVITY_CHANGE, data);

			/* Notify listeners */
			notifyListeners(MonitorManager.CONNECTIVITY_CHANGE, data);
		}

	};

	/**
	 * Activity-Service binder
	 */
	private final IBinder serviceBinder = new ServiceBinder();

	protected String TAG = "AdkintunMobile::Connectivity";

	@Override
	public synchronized DataFields getDataFields(int eventType) {
		if (eventType == MonitorManager.CONNECTIVITY_CHANGE) {
			if (connectivityDataFields == null)
				connectivityDataFields = new ConnectivityData();
			return connectivityDataFields;
		}
		return null;
	}

	@Override
	protected synchronized void onActivateEvent(int eventType) {
		// TODO: What is a better way to do this?
		if (eventType == MonitorManager.CONNECTIVITY_CHANGE) {
			IntentFilter filter = new IntentFilter();
			filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
			registerReceiver(connectivityMonitor, filter);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		/* TODO: do we need this method? */
		return serviceBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		/* Activate the event connectivity change */
		/* TODO: activate the event if activated on the preferences */
		setActive(MonitorManager.CONNECTIVITY_CHANGE, true);
	}

	@Override
	protected void onDataReceived(MonitorListener listener, int eventType,
			DataObject data) {
		// Only notify the listeners of the appropriate type
		if (eventType == MonitorManager.CONNECTIVITY_CHANGE
				&& listener instanceof ConnectivityListener) {
			((ConnectivityListener) listener).onConnectivityChanged(data);
			
			// TODO: detect WiFi connected and notify the listener
			// TODO: detect mobile connected
		}
		
	}

	@Override
	protected synchronized void onDeactivateEvent(int eventType) {
		if (eventType == MonitorManager.CONNECTIVITY_CHANGE) {
			unregisterReceiver(connectivityMonitor);
		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		// Deactivate the event
		// TODO: what happens if the event is not active and we call unregisterReceiver?
		setActive(MonitorManager.CONNECTIVITY_CHANGE, false);
	}
}
