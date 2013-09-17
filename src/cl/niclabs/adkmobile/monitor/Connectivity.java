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

public class Connectivity extends Monitor {
	public static class ConnectivityData implements DataFields {
		public static final String NETWORK_TYPE = "network_type";

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
				return "unknown";
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
					|| !isActive(MonitorEventManager.CONNECTIVITY)) {
				Log.w(TAG, "onReceived() called with intent " + intent);
				return;
			}
			// Get the NetworkInfo object
			ConnectivityManager connectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

			DataObject data = new ContentValuesDataObject();
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
			setCurrentState(MonitorEventManager.CONNECTIVITY, data);

			/* Notify listeners */
			notifyListeners(MonitorEventManager.CONNECTIVITY, data);
		}

	};

	@Override
	public DataFields getDataFields(int eventType) {
		if (eventType == MonitorEventManager.CONNECTIVITY) {
			if (connectivityDataFields == null)
				connectivityDataFields = new ConnectivityData();
			return connectivityDataFields;
		}
		return null;
	}

	@Override
	public void onActivateEvent(int eventType) {

	}

	@Override
	public IBinder onBind(Intent intent) {
		/* TODO: do we need this method? */
		return serviceBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(connectivityMonitor, filter);
	}

	@Override
	public void onDeactivateEvent(int eventType) {
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		unregisterReceiver(connectivityMonitor);
	}

}
