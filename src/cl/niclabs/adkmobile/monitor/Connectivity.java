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
import cl.niclabs.adkmobile.monitor.data.constants.ConnectionType;
import cl.niclabs.adkmobile.monitor.data.constants.NetworkState;
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
			if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {		
				ConnectivityObservation data = new ConnectivityObservation(System.currentTimeMillis());
				
				/* When no network is active
				 * the variable is null
				 * TODO: should we record this? */
				if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)) {					
					TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			    	data.setConnected(false);
			    	if (telephony.isNetworkRoaming()) {
			    		data.setRoaming(true);
			    	}
			    	data.setAvailable(false);  
			    	data.setConnectionType(ConnectionType.NONE);
		
			    	if (!data.equals(lastObservation)) {// Do not send the same observation twice in a row
						if (DEBUG) Log.d(TAG, "No active data connection");
			    		
						/* Notify listeners and update internal state */
						notifyListeners(connectivityEvent, data);
						
						// Log new state
						if(DEBUG) Log.d(TAG, data.toString());
					}
					lastObservation = (ConnectivityObservation) data;
					return; 
				}
				
				// Get the NetworkInfo object
				NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
				
//				// This code was disabled to normalize the behavior across devices
//				// otherwise multiple instances of a same event would be recorded 
//				if (VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//					// On API level 17, the intent contains the extra EXTRA_NETWORK_TYPE 
//					// that can be used with ConnectivityManager.getNetworkInfo(int) in order
//					// to obtain the new network info 
//					ni = connectivityManager.getNetworkInfo(intent.getIntExtra(
//							ConnectivityManager.EXTRA_NETWORK_TYPE, ni.getType()));
//				}
//				
//				if (VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
//					ni = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
//				}
				
				data.setConnected(ni.isConnected());
			
				ConnectionType connectionType;
				if ((connectionType = ConnectionType.valueOf(ni.getType())) == ConnectionType.OTHER) {
					data.setConnectionTypeOther(ni.getType());
				}
				data.setConnectionType(connectionType);
				data.setRoaming(ni.isRoaming());
				data.setAvailable(ni.isAvailable());
				data.setDetailedState(NetworkState.valueOf(ni.getDetailedState()));
			
				if (!data.equals(lastObservation)) {// Do not send the same observation twice in a row
					/* Notify listeners and update internal state */
					notifyListeners(connectivityEvent, data);
					
					// Log new state
					if(DEBUG) Log.d(TAG, data.toString());
				}
				lastObservation = (ConnectivityObservation) data;
			}
		}

	};

	/**
	 * Activity-Service binder
	 */
	private final IBinder serviceBinder = new ServiceBinder<Connectivity>(this);

	protected String TAG = "AdkintunMobile::Connectivity";
	
	private ConnectivityManager connectivityManager;
	
	private ConnectivityObservation lastObservation = null;

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
		return serviceBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
	}
}
