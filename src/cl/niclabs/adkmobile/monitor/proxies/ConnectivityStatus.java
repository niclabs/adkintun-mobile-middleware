package cl.niclabs.adkmobile.monitor.proxies;

import static cl.niclabs.adkmobile.monitor.Monitor.DEBUG;
import android.util.Log;
import cl.niclabs.adkmobile.data.DataObject;
import cl.niclabs.adkmobile.dispatcher.Notifier;
import cl.niclabs.adkmobile.monitor.Connectivity.ConnectionType;
import cl.niclabs.adkmobile.monitor.Connectivity.ConnectivityData;
import cl.niclabs.adkmobile.monitor.listeners.ConnectivityListener;

public class ConnectivityStatus extends MonitorProxy<ConnectivityStatusListener> implements ConnectivityListener {
	private DataObject data;
	
	protected String TAG = "AdkintunMobile::ConectivityStatus";
	
	boolean switchedNetwork			= false;
	boolean switchedRoamingStatus	= false;
	
	/**
	 * Detect a change in network status and notify the listener
	 * 
	 * @param listener
	 * @param oldData
	 * @param newData
	 */
	private void detectNetworkStatusChange(DataObject oldData, DataObject newData) {
		ConnectionType newNetworkType = ConnectionType.getType(newData.getInt(ConnectivityData.NETWORK_TYPE));
		boolean isConnected = newData.getBoolean(ConnectivityData.IS_CONNECTED);
		boolean isAvailable = newData.getBoolean(ConnectivityData.IS_AVAILABLE);
		boolean isRoaming = newData.getBoolean(ConnectivityData.IS_ROAMING);
		
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
				
				notifyListeners(new Notifier<ConnectivityStatusListener>() {
					@Override
					public void notify(ConnectivityStatusListener listener) {
						listener.onMobileConnection();
					}
				});
			}
			else if (newNetworkType == ConnectionType.WIFI) {
				if (DEBUG) Log.d(TAG, "Connected to WiFi");
				notifyListeners(new Notifier<ConnectivityStatusListener>() {
					@Override
					public void notify(ConnectivityStatusListener listener) {
						listener.onWifiConnection();
					}
				});
			}
			
			/* Reset status */
			switchedNetwork	 = false;
		}
		
		// Detect change in roaming status
		if (switchedRoamingStatus && newNetworkType != ConnectionType.WIFI) {
			final boolean isDataRoamingEnabled = isConnected && isAvailable;
			
			notifyListeners(new Notifier<ConnectivityStatusListener>() {
				@Override
				public void notify(ConnectivityStatusListener listener) {
					listener.onRoaming(isDataRoamingEnabled);
				}
			});
			
			/* Reset status */
			switchedRoamingStatus = false;
		}
	}
	
	@Override
	public void onConnectivityChanged(DataObject connectivityState) {
		detectNetworkStatusChange(data, connectivityState);
		data = connectivityState;
	}
}
