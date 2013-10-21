package cl.niclabs.adkmobile.monitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import cl.niclabs.adkmobile.data.ContentValuesDataObject;
import cl.niclabs.adkmobile.data.DataFields;
import cl.niclabs.adkmobile.data.DataObject;
import cl.niclabs.adkmobile.monitor.events.AbstractMonitorEvent;
import cl.niclabs.adkmobile.monitor.events.BasicMonitorEventResult;
import cl.niclabs.adkmobile.monitor.events.MonitorEvent;
import cl.niclabs.adkmobile.monitor.events.MonitorEventResult;
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
public class Connectivity extends AbstractMonitor {
	public static class ConnectivityData implements DataFields {
		public static final String DETAILED_STATE = "detailed_state";
		
		public static final String IS_AVAILABLE = "is_available";
		public static final String IS_CONNECTED = "is_connected";
		public static final String IS_ROAMING = "is_roaming";
		public static final String NETWORK_TYPE = "network_type";
		/**
		 * For devices with API level 13.
		 */
		public static final String NETWORK_TYPE_OTHER = "network_type_other";
	}
	
	protected class ConnectivityEventResult extends BasicMonitorEventResult {
		protected boolean hasConnectedToWifi = false;
		protected boolean hasConnectedToMobile = false;
		protected boolean hasStartedRoaming = false;
		protected boolean isDataRoamingEnabled = false;
		
		public ConnectivityEventResult(DataObject data) {
			super(data);
		}
		
		/**
		 * 
		 * @return true if the device has changed its connection to wifi
		 */
		public boolean hasConnectedToWifi() {
			return hasConnectedToWifi;
		}
		
		/**
		 * 
		 * @return true if the device has changed its connection to mobile 
		 */
		public boolean hasConnectedToMobile() {
			return hasConnectedToMobile;
		}
		
		/**
		 * 
		 * @return true if the device has started roaming
		 */
		public boolean hasStartedRoaming() {
			return hasStartedRoaming;
		}
		
		/**
		 * 
		 * @return true if data roaming is enabled 
		 */
		public boolean isDataRoamingEnabled() {
			return isDataRoamingEnabled;
		}
		
	}
	
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
		
		public static ConnectionType getType(int value) {
			for (ConnectionType n: ConnectionType.values()) {
				if (n.getValue() == value) {
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

		public int getValue() {
			return this.value;
		}
	};

	public class ServiceBinder extends Binder {
		public Connectivity getService() {
			return Connectivity.this;
		}
	}

	private MonitorEvent connectivityEvent = new AbstractMonitorEvent() {
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
		public void onDataReceived(MonitorListener listener, MonitorEventResult result) {
			if (listener instanceof ConnectivityListener) {
				ConnectivityListener connectivityListener = (ConnectivityListener) listener;
				
				/* Notify result */
				connectivityListener.onConnectivityChanged(result.getData());
				
				if (result instanceof ConnectivityEventResult) {
					ConnectivityEventResult connectivityResult = (ConnectivityEventResult) result;
					if (connectivityResult.hasConnectedToWifi()) {
						connectivityListener.onWifiConnection();
					}
					else if (connectivityResult.hasConnectedToMobile()) {
						connectivityListener.onMobileConnection();
					}
					
					if (connectivityResult.hasStartedRoaming()) {
						connectivityListener.onRoaming(connectivityResult.isDataRoamingEnabled());
					}
				}
			}
		}
	};

	private BroadcastReceiver connectivityMonitor = new BroadcastReceiver() {
		boolean switchedNetwork			= false;
		boolean switchedRoamingStatus	= false;
		
		/**
		 * Detect a change in network status and notify the listener
		 * 
		 * @param listener
		 * @param oldData
		 * @param newData
		 */
		public void notifyNetworkStatusChange(DataObject oldData, DataObject newData) {
			ConnectionType newNetworkType = ConnectionType.getType(newData.getInt(ConnectivityData.NETWORK_TYPE));
			boolean isConnected = newData.getBoolean(ConnectivityData.IS_CONNECTED);
			boolean isAvailable = newData.getBoolean(ConnectivityData.IS_AVAILABLE);
			boolean isRoaming = newData.getBoolean(ConnectivityData.IS_ROAMING);
			
			ConnectivityEventResult result = new ConnectivityEventResult(newData);
			
			if (oldData != null) { // Connectivity status has changed
				ConnectionType oldNetworkType = ConnectionType.getType(oldData.getInt(ConnectivityData.NETWORK_TYPE));
				boolean wasRoaming = oldData.getBoolean(ConnectivityData.IS_ROAMING);
				
				// Detect WiFi connection
				if (oldNetworkType != ConnectionType.WIFI && newNetworkType == ConnectionType.WIFI) {
					if (DEBUG) Log.d(TAG, "Switched to WiFi");
					switchedNetwork = true;
				}
				// Detect mobile connection
				else if (!oldNetworkType.isMobile() && newNetworkType.isMobile()) {
					if (DEBUG) Log.d(TAG, "Switched to mobile");
					switchedNetwork = true;
				}			
				
				if (!wasRoaming && isRoaming) {
					switchedRoamingStatus = true;
				}
			}
			else { // Service has just started
				if (newNetworkType == ConnectionType.WIFI || newNetworkType.isMobile()) switchedNetwork = true;
				if (isRoaming) switchedRoamingStatus = true;
			}

			
			// Detect change in connection status to IS_CONNECTED (it might happen on a different call to the method)
			if (switchedNetwork && isConnected && isAvailable) {
				if (newNetworkType.isMobile()) {
					if (DEBUG) Log.d(TAG, "Connected to mobile");
					result.hasConnectedToMobile = true;
				}
				else if (newNetworkType == ConnectionType.WIFI) {
					if (DEBUG) Log.d(TAG, "Connected to WiFi");
					result.hasConnectedToWifi = true;
				}
				
				/* Reset status */
				switchedNetwork	 = false;
			}
			
			// Detect change in roaming status
			if (switchedRoamingStatus && newNetworkType != ConnectionType.WIFI) {
				//((ConnectivityListener) listener).onRoaming(isConnected && isAvailable);
				result.hasStartedRoaming = true;
				result.isDataRoamingEnabled = isConnected && isAvailable;
				
				/* Reset status */
				switchedRoamingStatus = false;
			}
			
			/* Notify the listeners */
			notifyListeners(connectivityEvent, result);
		}
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// Get the NetworkInfo object
			ConnectivityManager connectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			
			// TODO: On API level 17, the intent contains the extra EXTRA_NETWORK_TYPE 
			// that can be used with ConnectivityManager.getNetworkInfo(int) in order
			// to obtain the new network info
			NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
					
			/* When no network is active
			 * the variable is null
			 * TODO: should we record this? */
			
			/* Get old data */
			DataObject oldData = getState(connectivityEvent);
			
			DataObject data = new ContentValuesDataObject();
			
			if (ni == null) {
				Log.w(TAG, "No active network");
				
				TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			    if (telephony.isNetworkRoaming()) {
			    	data.put(ConnectivityData.EVENT_TYPE,  CONNECTIVITY);
			    	data.put(ConnectivityData.TIMESTAMP, System.currentTimeMillis());
			    	data.put(ConnectivityData.IS_CONNECTED, false);
			    	data.put(ConnectivityData.IS_ROAMING, true);
			    	data.put(ConnectivityData.IS_AVAILABLE, false);
			    	
			    	// Log new state
					if(DEBUG) Log.d(TAG, data.toString());

					/* Notify listeners and update internal state */
					notifyNetworkStatusChange(oldData, data);
			    }
				return; 
			}

			data.put(ConnectivityData.EVENT_TYPE,  CONNECTIVITY);
			data.put(ConnectivityData.TIMESTAMP, System.currentTimeMillis());
			data.put(ConnectivityData.IS_CONNECTED,
					ni.isConnectedOrConnecting());

			ConnectionType networkType;
			if ((networkType = ConnectionType.valueOf(ni.getType())) == ConnectionType.OTHER) {
				data.put(ConnectivityData.NETWORK_TYPE_OTHER, ni.getType());
			}
			data.put(ConnectivityData.NETWORK_TYPE, networkType.getValue());
			data.put(ConnectivityData.IS_ROAMING, ni.isRoaming());
			data.put(ConnectivityData.IS_AVAILABLE, ni.isAvailable());
			
			data.put(ConnectivityData.DETAILED_STATE, NetworkState.valueOf(ni.getDetailedState()).getValue());

			// Log new state
			if(DEBUG) Log.d(TAG, data.toString());

			/* Notify listeners and update internal state */
			notifyNetworkStatusChange(oldData, data);
		}

	};

	/**
	 * Activity-Service binder
	 */
	private final IBinder serviceBinder = new ServiceBinder();

	protected String TAG = "AdkintunMobile::Connectivity";

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
}
