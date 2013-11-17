package cl.niclabs.adkmobile.monitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import cl.niclabs.adkmobile.monitor.data.ConnectivityObservation;
import cl.niclabs.adkmobile.monitor.data.Observation;
import cl.niclabs.adkmobile.monitor.events.AbstractMonitorEvent;
import cl.niclabs.adkmobile.monitor.events.MonitorEvent;
import cl.niclabs.adkmobile.monitor.listeners.ConnectivityListener;

/**
 * Implements monitoring of Internet connectivity change. Connectivity is
 * notified by the system as a broadcast for the CONNECTIVITY_ACTION Intent.
 * Since this class needs to check the current NetworkState, it needs the
 * permission ACCESS_NETWORK_STATE.
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 */
public class Connectivity extends AbstractMonitor<ConnectivityListener> {	
	/**
	 * The network detailed state for recording on the database
	 * 
	 * @author Felipe Lalanne <flalanne@niclabs.cl>
	 */
	public static enum NetworkState {
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

	/**
	 * Network types as defined in android.net.ConnectivityManager
	 *
	 * 
	 * @author Felipe Lalanne <flalanne@niclabs.cl>
	 */
	public static enum ConnectionType {
		MOBILE(1), MOBILE_DUN(2), MOBILE_HIPRI(3), MOBILE_MMS(4), MOBILE_SUPL(5), OTHER(0), WIFI(
						6), WIMAX(7);

		/**
		 * Get the network type from the ConnectivityManager constants
		 * 
		 * @param value connectivity identifier according to ConnectivityManager constants
		 */
		public static ConnectionType valueOf(int value) {
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
		
		public static ConnectionType getInstance(int value) {
			for (ConnectionType n: ConnectionType.values()) {
				if (n.value() == value) {
					return n;
				}
			}
			return OTHER;
		}
		
		public boolean isMobile() {
			switch(this) {
				case MOBILE:
				case MOBILE_DUN:
				case MOBILE_HIPRI:
				case MOBILE_MMS:
				case MOBILE_SUPL:
					return true;
				default:
					return false;
			}
		}

		int value;

		private ConnectionType(int value) {
			this.value = value;
		}

		public int value() {
			return this.value;
		}
	};

	private MonitorEvent<ConnectivityListener> connectivityEvent = new AbstractMonitorEvent<ConnectivityListener>() {
		@Override
		public synchronized boolean activate() {
			if (!isActive()) {
				IntentFilter filter = new IntentFilter();
				filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
				registerReceiver(connectivityMonitor, filter);
				
				if(DEBUG) Log.d(TAG, "Connectivity service has been activated");
				
				// Do not forget
				super.activate();				
			}
			return true;
		}

		@Override
		public synchronized void deactivate() {
			if (isActive()) {
				// TODO: what happens if the event is not active and we call unregisterReceiver?
				unregisterReceiver(connectivityMonitor);
				
				if(DEBUG) Log.d(TAG, "Connectivity service has been deactivated");
				super.deactivate();
			}
		}
		
		@Override
		public void onDataReceived(ConnectivityListener listener, Observation result) {
			/* Notify result */
			listener.onConnectivityChange((ConnectivityObservation) result);
		}
	};

	private BroadcastReceiver connectivityMonitor = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO: On API level 17, the intent contains the extra EXTRA_NETWORK_TYPE 
			// that can be used with ConnectivityManager.getNetworkInfo(int) in order
			// to obtain the new network info
			// Get the NetworkInfo object
			NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
					
			ConnectivityObservation data = new ConnectivityObservation(System.currentTimeMillis());
			
			/* When no network is active
			 * the variable is null
			 * TODO: should we record this? */
			if (ni == null) {
				Log.w(TAG, "No active network");
				
				TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			    if (telephony.isNetworkRoaming()) {
			    	data.setConnected(false);
			    	data.setRoaming(true);
			    	data.setAvailable(false);  
			    	
			    	// Log new state
					if(DEBUG) Log.d(TAG, data.toString());

					/* Notify listeners and update internal state */
					notifyListeners(connectivityEvent, data);
			    }
				return; 
			}

			data.setConnected(ni.isConnectedOrConnecting());

			ConnectionType connectionType;
			if ((connectionType = ConnectionType.valueOf(ni.getType())) == ConnectionType.OTHER) {
				data.setConnectionTypeOther(ni.getType());
			}
			data.setConnectionType(connectionType);
			data.setRoaming(ni.isRoaming());
			data.setAvailable(ni.isAvailable());
			data.setDetailedState(NetworkState.valueOf(ni.getDetailedState()));

			// Log new state
			if(DEBUG) Log.d(TAG, data.toString());

			/* Notify listeners and update internal state */
			notifyListeners(connectivityEvent, data);
		}

	};

	/**
	 * Activity-Service binder
	 */
	private final IBinder serviceBinder = new ServiceBinder<Connectivity>(this);

	protected String TAG = "AdkintunMobile::Connectivity";
	
	private ConnectivityManager connectivityManager;

	@Override
	public boolean activate(int events, Bundle configuration) {
		if ((events & CONNECTIVITY) == CONNECTIVITY) {
			return activate(connectivityEvent);
		}
		return false;
	}

	@Override
	public void deactivate(int events) {
		if ((events & CONNECTIVITY) == CONNECTIVITY) {
			deactivate(connectivityEvent);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		if(DEBUG) Log.d(TAG, "Service has been bound");
		return serviceBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
	}
}
