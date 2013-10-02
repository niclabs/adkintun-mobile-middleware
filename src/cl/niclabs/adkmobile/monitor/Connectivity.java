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
		public static final String NETWORK_TYPE = "network_type";
		
		/**
		 * For devices with API level 13.
		 */
		public static final String NETWORK_TYPE_OTHER = "network_type_other";
		public static final String IS_CONNECTED = "is_connected";
		public static final String IS_ROAMING = "is_roaming";
		public static final String DETAILED_STATE = "detailed_state";

		private ConnectivityData() {

		}
	}

	/**
	 * Network types as defined in android.net.ConnectivityManager
	 * 
	 * TODO: this is probably much more complicated than it needs to be
	 * 
	 * @author Felipe Lalanne <flalanne@niclabs.cl>
	 */
	public static enum NetworkType {
		MOBILE(1), MOBILE_DUN(2), MOBILE_HIPRI(3), MOBILE_MMS(4), MOBILE_SUPL(5), WIFI(
				6), WIMAX(7), OTHER(0);

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

		public String toString() {
			switch (this) {
			case MOBILE:
				return "mobile";
			case MOBILE_DUN:
				return "mobile_dun";
			case MOBILE_HIPRI:
				return "mobile_hipri";
			case MOBILE_MMS:
				return "mobile_mms";
			case MOBILE_SUPL:
				return "mobile_supl";
			case WIFI:
				return "wifi";
			case WIMAX:
				return "wimax";
			case OTHER:
				return "other";
			}
			/* This is never reached, left here only to avoid compiling errors */
			return null;
		}
	};

	public class ServiceBinder extends Binder {
		Connectivity getService() {
			return Connectivity.getService();
		}
	}

	/**
	 * Instance of the current service
	 */
	private static Connectivity connectivityService;

	public static Connectivity getService() {
		if (connectivityService == null)
			connectivityService = new Connectivity();
		return connectivityService;
	}

	private ConnectivityData connectivityDataFields;

	protected String TAG = "AdkintunMobile::Connectivity";

	/**
	 * Activity-Service binder
	 */
	private final IBinder serviceBinder = new ServiceBinder();

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
			/* When the network goes from Wi-Fi to somekind of network, 
			 * the variable is null */
			if (ni == null){return;}

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
			// TODO: add detailed state

			// Log new state
			Log.d(TAG, data.toString());

			/* Update the current state */
			setCurrentState(MonitorManager.CONNECTIVITY_CHANGE, data);

			/* Notify listeners */
			notifyListeners(MonitorManager.CONNECTIVITY_CHANGE, data);
		}

	};

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
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);

		/* Activate the event connectivity change */
		/* TODO: activate the event if activated on the preferences */
		setActive(MonitorManager.CONNECTIVITY_CHANGE, true);
		return START_STICKY;
	}

	@Override
	protected void onDataReceived(MonitorListener listener, int eventType,
			DataObject data) {
		// Only notify the listeners of the appropriate type
		if (eventType == MonitorManager.CONNECTIVITY_CHANGE
				&& listener instanceof ConnectivityListener) {
			((ConnectivityListener) listener).onConnectivityChanged(data);
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
