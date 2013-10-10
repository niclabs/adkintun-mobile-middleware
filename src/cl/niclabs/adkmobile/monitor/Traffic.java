package cl.niclabs.adkmobile.monitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Build.VERSION;
import android.util.Log;
import cl.niclabs.adkmobile.monitor.data.ContentValuesDataObject;
import cl.niclabs.adkmobile.monitor.data.DataFields;
import cl.niclabs.adkmobile.monitor.data.DataObject;
import cl.niclabs.adkmobile.monitor.listeners.MonitorListener;
import cl.niclabs.adkmobile.monitor.listeners.TrafficListener;

/**
 * Implements monitoring of Rx & Tx bytes. Traffic is
 * notified by the system as a listener. This class will listen
 * for each 10 seconds.
 * @author Mauricio Castro.
 *         Created 27-09-2013.
 */
public class Traffic extends Monitor {
	
	public class ServiceBinder extends Binder {
		public Traffic getService() {
			return Traffic.getService();
		}
	}
	
	public static class TrafficData implements DataFields {
		
		public static String MOBILE_RECEIVED = "mobile_bytes_received";
		public static String MOBILE_TCP_RECEIVED = "mobile_tcp_bytes_received";
		
		public static String MOBILE_TCP_TRANSMITTED = "mobile_tcp_bytes_transmitted";
		public static String MOBILE_TRANSMITTED = "mobile_bytes_transmitted";
		
		public static String WIFI_RECEIVED = "wifi_bytes_received";
		public static String WIFI_TRANSMITTED = "wifi_bytes_transmitted";
		 
	}
	
	public static int TRAFFIC_UPDATE_INTERVAL = 10;
	
	
	/**
	 * Instance of the current service
	 */
	private static Traffic trafficService;
	public static Traffic getService() {
		if (trafficService == null)
			trafficService = new Traffic();
		return trafficService;
	}
	
	private Context mContext = this;

	private TimerTask mobile_task = new TimerTask() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub.
			long[] bytes = getMobileTxRxBytes(mContext);
			// assign the values to ContentValues variables
			DataObject data = new ContentValuesDataObject();
			data.put(TrafficData.TIMESTAMP,System.currentTimeMillis());
			data.put(TrafficData.MOBILE_RECEIVED,bytes[0]);
			data.put(TrafficData.MOBILE_TRANSMITTED,bytes[1]);
			
			setCurrentState(MonitorManager.MOBILE_TRAFFIC_CHANGE, data);

			/* Notify listeners */
			notifyListeners(MonitorManager.MOBILE_TRAFFIC_CHANGE, data);
			
			/* Log the results */
			Log.d(TAG,data.toString());
		}
	};
	
	private TimerTask mobile_tcp_task = new TimerTask() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub.
			long[] bytes = getMobileTcpTxRxBytes(mContext);
			// assign the values to ContentValues variables
			DataObject data = new ContentValuesDataObject();
			data.put(TrafficData.TIMESTAMP,System.currentTimeMillis());
			data.put(TrafficData.MOBILE_TCP_RECEIVED,bytes[0]);
			data.put(TrafficData.MOBILE_TCP_TRANSMITTED,bytes[1]);
			
			/* Update the current state */
			setCurrentState(MonitorManager.MOBILE_TCP_TRAFFIC_CHANGE, data);

			/* Notify listeners */
			notifyListeners(MonitorManager.MOBILE_TCP_TRAFFIC_CHANGE, data);
			
			/* Log the results */
			Log.d(TAG,data.toString());
		}
	};
	private Timer mTimer_Traffic = new Timer();

	private Timer mTimer_Traffic_TCP = new Timer();
	
	/**
	 * Activity-Service binder
	 */
	private final IBinder serviceBinder = new ServiceBinder();
	protected String TAG = "AdkintunMobile::Traffic";
	private TrafficData trafficDataFields;
	
	@Override
	public DataFields getDataFields(int eventType) {
		// TODO Auto-generated method stub.
		if (eventType == MonitorManager.MOBILE_TCP_TRAFFIC_CHANGE) {
			if (trafficDataFields == null)
				trafficDataFields = new TrafficData();
			return trafficDataFields;
		}
		return null;
	}
	
	/**
	 * Method that return an array with the downloaded/transmitted
	 * bytes of whole applications of the mobile. 
	 * The mobile API must be 12 or superior, otherwise the method
	 * will return {-1,-1}.
	 * @param context
	 * @return [0] = Bytes received;
	 * 		   [1] = Bytes transmitted
	 */

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	public long[] getMobileTcpTxRxBytes(Context context) {
		if (VERSION.SDK_INT >= 12) {
			try {
				ArrayList<Integer> uids = getUids(context);

				long totalRxBytes = 0;
				long totalTxBytes = 0;

				for (int i = 0; i < uids.size(); i++) {
					long bytesRx = TrafficStats.getUidTcpRxBytes(uids.get(i));
					long bytesTx = TrafficStats.getUidTcpTxBytes(uids.get(i));
					if (bytesRx > 0) {
						totalRxBytes += bytesRx;
					}
					if (bytesTx > 0) {
						totalTxBytes += bytesTx;
					}
				}
				return new long[]{totalRxBytes, totalTxBytes};
			} catch (Exception e) {
				return new long[]{-1,-1};
			}
		}
		return new long[]{-1,-1};
		//TODO: Should we return a different value if a different version? */		
	}
	
	/**
	 * Method that return an array with the downloaded/transmitted
	 * bytes of the mobile. 
	 * @param context
	 * @return [0] = Bytes received;
	 * 		   [1] = Bytes transmitted
	 */
	public long[] getMobileTxRxBytes(Context context){
		
		try{
			long mobileRxBytes = TrafficStats.getMobileRxBytes();
			long mobileTxBytes = TrafficStats.getMobileTxBytes();
			
			return new long[]{mobileRxBytes,mobileTxBytes};
		}catch(Exception e){ //TODO: what kind of exception?
			return new long[]{-1,-1};
		}
		
	}
	
	/**
	 * Method that returns an arrayList with the UIDs of
	 * all the applications on the mobile. 
	 * @param context
	 * @return An ArrayList with the UIDs
	 */
	public ArrayList<Integer> getUids(Context context) {
		List<ApplicationInfo> appsInfo = context.getPackageManager()
				.getInstalledApplications(
						PackageManager.GET_UNINSTALLED_PACKAGES);
		ArrayList<Integer> uids = new ArrayList<Integer>();

		for (int i = 0; i < appsInfo.size(); i++) {
			if (!uids.contains(appsInfo.get(i).uid)) {
				uids.add(appsInfo.get(i).uid);
			}
		}
		return uids;
	}
	
	@Override
	protected void onActivateEvent(int eventType) {
		// TODO see how to implement the listener.
		
		if (eventType == MonitorManager.MOBILE_TRAFFIC){
			Log.d(TAG, "Active Listeners");
			mTimer_Traffic_TCP.schedule(mobile_tcp_task,0,1000 * TRAFFIC_UPDATE_INTERVAL);
			mTimer_Traffic.schedule(mobile_task,0,1000 * TRAFFIC_UPDATE_INTERVAL);
		}
		
	}

	@Override
	protected void onDataReceived(MonitorListener listener, int eventType,
			DataObject data) {
		// TODO Auto-generated method stub.
		if (eventType == MonitorManager.MOBILE_TRAFFIC
				&& listener instanceof TrafficListener) {
			((TrafficListener) listener).onMobileTrafficChanged(data);
		}
	}
	
	@Override
	protected void onDeactivateEvent(int eventType) {

		switch (eventType) {
		case MonitorManager.MOBILE_TRAFFIC:
			mTimer_Traffic.cancel();
			mTimer_Traffic_TCP.cancel();
			break;

		default:
			break;
		}
		
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		// Deactivate the event
		// TODO: what happens if the event is not active and we call unregisterReceiver?
		setActive(MonitorManager.MOBILE_TRAFFIC, false);
		//setActive(MonitorManager.MOBILE_TRAFFIC_CHANGE, false);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		int event = intent.getExtras().getInt(MonitorManager.TRAFFIC_INTENT);
		/* Activate the event traffic_change */
		/* TODO: activate the event if activated on the preferences */
		switch (event) {
		case MonitorManager.MOBILE_TRAFFIC:
			setActive(MonitorManager.MOBILE_TRAFFIC, true);
			break;
			
		default:
			break;
		}
		
		return START_STICKY;
	}
	
	/**
	 * Set the interval period of traffic measure in seconds.
	 * For default is set on 10 seconds.
	 * @param interval in seconds
	 */
	public void setTrafficInterval(int interval){
		TRAFFIC_UPDATE_INTERVAL= interval;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return serviceBinder;
	}

}
