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
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import cl.niclabs.adkmobile.monitor.data.ContentValuesDataObject;
import cl.niclabs.adkmobile.monitor.data.DataFields;
import cl.niclabs.adkmobile.monitor.data.DataObject;
import cl.niclabs.adkmobile.monitor.events.BaseMonitorEvent;
import cl.niclabs.adkmobile.monitor.events.MonitorEvent;
import cl.niclabs.adkmobile.monitor.listeners.MonitorListener;
import cl.niclabs.adkmobile.monitor.listeners.TrafficListener;

/**
 * Implements monitoring of Rx & Tx bytes. Traffic is
 * notified by the system as a listener. This class will listen
 * for each 10 seconds.
 * @author Mauricio Castro.
 *         Created 27-09-2013.
 */
public class Traffic extends AbstractMonitor {
	
	public class ServiceBinder extends Binder {
		public Traffic getService() {
			return Traffic.this;
		}
	}
	
	public static class MobileTrafficData implements DataFields {
		
		public static String MOBILE_RX_BYTES = "mobile_rx_bytes";
		public static String MOBILE_TX_BYTES = "mobile_tx_bytes";
		
		public static String MOBILE_TCP_RX_BYTES = "mobile_tcp_rx_bytes";
		public static String MOBILE_TCP_TX_BYTES = "mobile_tcp_tx_bytes";
	}
	
	public static class WifiTrafficData implements DataFields {
		public static String WIFI_RX_BYTES = "wifi_rx_bytes";
		public static String WIFI_TX_BYTES = "wifi_tx_bytes";
	}
	
	/**
	 * @var Frequency of sampling (in seconds)
	 */
	public static final int TRAFFIC_UPDATE_INTERVAL = 10;
	
	/**
	 * Extra key for configuring the traffic update interval
	 */
	public static final String TRAFFIC_UPDATE_INTERVAL_EXTRA = "traffic_update_interval";
	
	protected int trafficUpdateInterval = TRAFFIC_UPDATE_INTERVAL;
	
	private Context mContext = this;

	private TimerTask mobileTask = new TimerTask() {
		@Override
		public void run() {
			// TODO Auto-generated method stub.
			long[] bytes = getMobileTxRxBytes(mContext);
			long[] bytesTcp = getMobileTcpTxRxBytes(mContext);
			// assign the values to ContentValues variables
			DataObject data = new ContentValuesDataObject();
			data.put(MobileTrafficData.TIMESTAMP,System.currentTimeMillis());
			data.put(MobileTrafficData.MOBILE_RX_BYTES,bytes[0]);
			data.put(MobileTrafficData.MOBILE_TX_BYTES,bytes[1]);
			
			// Only add TCP bytes only if they are available
			if (bytesTcp[0] > 0 && bytesTcp[1] >= 0) {
				data.put(MobileTrafficData.MOBILE_TCP_RX_BYTES, bytesTcp[0]);
				data.put(MobileTrafficData.MOBILE_TCP_TX_BYTES, bytesTcp[1]);
			}
			
			setState(mobileTrafficEvent, data);

			/* Notify listeners */
			notifyListeners(mobileTrafficEvent, data);
			
			/* Log the results */
			Log.d(TAG, data.toString());
		}
	};
	
	private MonitorEvent mobileTrafficEvent = new BaseMonitorEvent() {
		@Override
		public synchronized void activate() {
			if (!isActive()) {
				Log.d(TAG, "Active Listeners");
				mTimerTraffic.schedule(mobileTask, 0, 1000 * trafficUpdateInterval);
				super.activate();
				
				Log.d(TAG, "Traffic service has been activated");
			}
		}

		@Override
		public synchronized void deactivate() {
			if (isActive()) {
				// Stop the task
				mTimerTraffic.cancel();
				super.deactivate();
				
				Log.d(TAG, "Traffic service has been deactivated");
			}
		}

		@Override
		public synchronized void onDataReceived(MonitorListener listener, DataObject data) {
			if (listener instanceof TrafficListener) {
				((TrafficListener) listener).onMobileTrafficChanged(data);
			}
		}		
	};
	
	private Timer mTimerTraffic = new Timer();
	
	/**
	 * Activity-Service binder
	 */
	private final IBinder serviceBinder = new ServiceBinder();
	protected String TAG = "AdkintunMobile::Traffic";
	
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
	private long[] getMobileTcpTxRxBytes(Context context) {
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
				
			// TODO: What type of exceptions? Why? Missing documentation
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
	private long[] getMobileTxRxBytes(Context context){
		try {
			long mobileRxBytes = TrafficStats.getMobileRxBytes();
			long mobileTxBytes = TrafficStats.getMobileTxBytes();
			
			return new long[]{mobileRxBytes, mobileTxBytes};
		}
		// TODO: What type of exceptions? Why? Missing documentation
		catch (Exception e) { 
			return new long[]{-1,-1};
		}
		
	}
	
	/**
	 * Method that returns an arrayList with the UIDs of
	 * all the applications on the mobile. 
	 * @param context
	 * @return An ArrayList with the UIDs
	 */
	private ArrayList<Integer> getUids(Context context) {
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
	
	/**
	 * Set the interval period of traffic measure in seconds.
	 * For default is set on 10 seconds.
	 * @param interval in seconds
	 */
	protected void setTrafficInterval(int interval){
		trafficUpdateInterval = interval;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return serviceBinder;
	}
	
	@Override
	public void activate(int events, Bundle configuration) {
		if ((events & MOBILE_TRAFFIC) == MOBILE_TRAFFIC) {
			trafficUpdateInterval = configuration.getInt(TRAFFIC_UPDATE_INTERVAL_EXTRA, TRAFFIC_UPDATE_INTERVAL);
			activate(mobileTrafficEvent);
		}
	}

	@Override
	public void deactivate(int events) {
		if ((events & MOBILE_TRAFFIC) == MOBILE_TRAFFIC) {
			deactivate(mobileTrafficEvent);
		}
	}
}
