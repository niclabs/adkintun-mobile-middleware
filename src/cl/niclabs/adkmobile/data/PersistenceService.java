package cl.niclabs.adkmobile.data;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import cl.niclabs.adkmobile.monitor.listeners.ConnectivityListener;
import cl.niclabs.adkmobile.monitor.listeners.TelephonyListener;
import cl.niclabs.adkmobile.monitor.listeners.TrafficListener;

public class PersistenceService extends Service implements ConnectivityListener, TrafficListener, TelephonyListener {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onConnectivityChanged(DataObject connectivityState) {
		// TODO: Save connectivity data
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}

	@Override
	public void onMobileTelephonyChanged(DataObject telephonyState) {
		// TODO: Save telephony state
	}

	@Override
	public void onMobileTrafficChanged(DataObject trafficState) {
		// TODO: Save traffic state
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO: bind to services and define listeners
		
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onWiFiTrafficChanged(DataObject trafficState) {
		// TODO: Save traffic state
	}
}
