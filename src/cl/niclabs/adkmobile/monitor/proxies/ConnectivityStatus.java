package cl.niclabs.adkmobile.monitor.proxies;

import static cl.niclabs.adkmobile.AdkintunMobileApp.DEBUG;
import android.util.Log;
import cl.niclabs.adkmobile.monitor.data.ConnectivityObservation;
import cl.niclabs.adkmobile.monitor.data.constants.ConnectionType;
import cl.niclabs.adkmobile.monitor.listeners.ConnectivityListener;
import cl.niclabs.adkmobile.utils.Notifier;

public class ConnectivityStatus extends MonitorProxy<ConnectivityStatusListener> implements ConnectivityListener {
	private ConnectivityObservation data;
	
	protected String TAG = "AdkintunMobile::ConnectivityStatus";
	
	boolean switchedNetwork			= false;
	boolean switchedRoamingStatus	= false;
	
	/**
	 * Detect a change in network status and notify the listener
	 * 
	 * @param listener
	 * @param oldData
	 * @param newData
	 */
	private void detectNetworkStatusChange(ConnectivityObservation oldData, ConnectivityObservation newData) {
		ConnectionType newNetworkType = newData.getConnectionType();
		boolean isConnected = newData.isConnected();
		boolean isAvailable = newData.isAvailable();
		boolean isRoaming = newData.isRoaming();		
		boolean disconnectedFromNetwork = false;
		
		if (oldData != null) { // Connectivity status has changed
			ConnectionType oldNetworkType = oldData.getConnectionType();
			boolean wasRoaming = oldData.isRoaming();
			
			// Detect WiFi connection
			switchedNetwork = (oldNetworkType != ConnectionType.WIFI && newNetworkType == ConnectionType.WIFI)
					|| !oldNetworkType.isMobile() && newNetworkType.isMobile();
			disconnectedFromNetwork = oldNetworkType != ConnectionType.NONE && newNetworkType == ConnectionType.NONE;
			switchedRoamingStatus = !wasRoaming && isRoaming;
		}
		else { // Service has just started
			switchedNetwork = newNetworkType == ConnectionType.WIFI || newNetworkType.isMobile();
			disconnectedFromNetwork = newNetworkType == ConnectionType.NONE;
			switchedRoamingStatus = isRoaming;
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
		
		if (disconnectedFromNetwork) {
			notifyListeners(new Notifier<ConnectivityStatusListener>() {
				@Override
				public void notify(ConnectivityStatusListener listener) {
					listener.onNetworkDisconnection();
				}
			});
			
			/* Reset status */
			disconnectedFromNetwork = false;
		}
	}
	
	@Override
	public void onConnectivityChange(ConnectivityObservation connectivityState) {
		detectNetworkStatusChange(data, connectivityState);
		data = connectivityState;
	}
}
