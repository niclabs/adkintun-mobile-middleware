package cl.niclabs.adkmobile.monitor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
import cl.niclabs.adkmobile.monitor.events.AbstractMonitorEvent;
import cl.niclabs.adkmobile.monitor.events.BasicMonitorEventResult;
import cl.niclabs.adkmobile.monitor.events.MonitorEvent;
import cl.niclabs.adkmobile.monitor.events.MonitorEventResult;
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
	
	public static class TrafficData implements DataFields {
		public static String NETWORK_TYPE = "network_type"; 
		public static String RX_BYTES = "rx_bytes";
		public static String TX_BYTES = "tx_bytes";
		public static String RX_PACKETS = "rx_packets";
		public static String TX_PACKETS = "tx_packets";
		
		public static String TCP_RX_BYTES = "tcp_rx_bytes";
		public static String TCP_TX_BYTES = "tcp_tx_bytes";
		public static String TCP_RX_SEGMENTS = "tcp_rx_segments";
		public static String TCP_TX_SEGMENTS = "tcp_tx_segments";
	}
	
	public class ServiceBinder extends Binder {
		public Traffic getService() {
			return Traffic.this;
		}
	}
	
	/**
	 * @var Frequency of sampling (in seconds)
	 */
	public static final int TRAFFIC_UPDATE_INTERVAL = 10;
	
	/**
	 * Extra key for configuring the traffic update interval
	 */
	public static final String TRAFFIC_UPDATE_INTERVAL_EXTRA = "traffic_update_interval";
	
	/**
	 * Represents a mobile network for storage
	 */
	public static final int NETWORK_TYPE_MOBILE = Connectivity.NetworkType.MOBILE.getValue();
	
	
	/**
	 * Represents a wifi network for storage
	 */
	public static final int NETWORK_TYPE_WIFI = Connectivity.NetworkType.WIFI.getValue();
	
	private Context mContext = this;
	
	private long mobileRxBytes;
	private long mobileTxBytes;
	private long mobileRxPackets;
	private long mobileTxPackets;
	
	private long wifiRxBytes;
	private long wifiTxBytes;
	private long wifiRxPackets;
	private long wifiTxPackets;
	
	private long mobileTcpRxBytes;
	private long mobileTcpTxBytes;
	private long mobileTcpRxSegments;
	private long mobileTcpTxSegments;
	
	private long wifiTcpRxBytes;
	private long wifiTcpTxBytes;
	private long wifiTcpRxSegments;
	private long wifiTcpTxSegments;
	
	private Runnable mobileTask = new Runnable() {
		@Override
		public void run() {
			
			
			DataObject mobileData = new ContentValuesDataObject();
			
			long newMobileRxBytes = TrafficStats.getMobileRxBytes();
			long newMobileTxBytes = TrafficStats.getMobileTxBytes();
			long newMobileRxPackets = TrafficStats.getMobileRxPackets();
			long newMobileTxPackets = TrafficStats.getMobileTxPackets();
			
			long[] tcpData = getTcpData(mContext);
			
 			long dMobileRxBytes = newMobileRxBytes - mobileRxBytes;
			long dMobileTxBytes = newMobileTxBytes - mobileTxBytes;
			long dMobileTxPackets = newMobileTxPackets - mobileTxPackets;
			long dMobileRxPackets = newMobileRxPackets - mobileRxPackets;
	
			mobileData.put(TrafficData.TIMESTAMP,System.currentTimeMillis());
			mobileData.put(TrafficData.NETWORK_TYPE, NETWORK_TYPE_MOBILE);
			mobileData.put(TrafficData.RX_BYTES, dMobileRxBytes);
			mobileData.put(TrafficData.TX_BYTES, dMobileTxBytes);
			mobileData.put(TrafficData.RX_PACKETS, dMobileRxPackets);
			mobileData.put(TrafficData.TX_PACKETS, dMobileTxPackets);
			
			
			// Only add protocol bytes if they are supported by the device
			if (tcpData[0] >= 0) {
				long dTcpRxBytes = 0;
				long dTcpTxBytes = 0;
				if (dMobileRxBytes > 0) {
					/* This calculation relies on the fact that wifi and mobile traffic are exclusive */
					dTcpRxBytes = tcpData[0] - mobileTcpRxBytes;
					dTcpTxBytes = tcpData[1] - mobileTcpTxBytes;
					
					/* Update state vars */
					mobileTcpRxBytes = tcpData[0];
					mobileTcpTxBytes = tcpData[1];
				}
				
				mobileData.put(TrafficData.TCP_RX_BYTES, dTcpRxBytes);
				mobileData.put(TrafficData.TCP_TX_BYTES, dTcpTxBytes);
			}
				
			if (tcpData[2] >= 0) {
				/* This calculation relies on the fact that wifi and mobile traffic are exclusive */
				long dTcpRxSegments = tcpData[2] - mobileTcpRxSegments;
				long dTcpTxSegments = tcpData[3] - mobileTcpTxSegments;
				
				if (dMobileRxBytes > 0) {
					dTcpRxSegments = tcpData[2] - mobileTcpRxSegments;
					dTcpTxSegments = tcpData[3] - mobileTcpTxSegments;
					
					/* Update state vars */
					mobileTcpRxSegments = tcpData[2];
					mobileTcpTxSegments = tcpData[3];
				}
				
				mobileData.put(TrafficData.TCP_RX_SEGMENTS, dTcpRxSegments);
				mobileData.put(TrafficData.TCP_TX_SEGMENTS, dTcpTxSegments);
			}
			
			/* Notify listeners and update state */
			notifyListeners(mobileTrafficEvent, new BasicMonitorEventResult(mobileData));
			
			/* Update state vars */
			mobileRxBytes = newMobileRxBytes;
			mobileTxBytes = newMobileTxBytes;
			mobileRxPackets = newMobileRxPackets;
			mobileTxPackets = newMobileTxPackets;
					
			/* Log the results */
			if(DEBUG) Log.d(TAG, mobileData.toString());
		}
	};
	
	private Runnable wifiTask = new Runnable() {
		@Override
		public void run() {
			long[] tcpData = getTcpData(mContext);
			
			DataObject wifiData = new ContentValuesDataObject();
			
			long newWifiRxBytes = TrafficStats.getTotalRxBytes() - TrafficStats.getMobileRxBytes();
			long newWifiTxBytes = TrafficStats.getTotalTxBytes() - TrafficStats.getMobileTxBytes();
			long newWifiRxPackets = TrafficStats.getTotalRxPackets() - TrafficStats.getMobileRxPackets();
			long newWifiTxPackets = TrafficStats.getTotalTxPackets() - TrafficStats.getMobileTxPackets();
			
			
			
			long dWifiRxBytes = newWifiRxBytes - wifiRxBytes;
			long dWifiTxBytes = newWifiTxBytes - wifiTxBytes;
			
			long dWifiRxPackets = newWifiRxPackets - wifiRxPackets;				
			long dWifiTxPackets = newWifiTxPackets - wifiTxPackets;
			
			wifiData.put(TrafficData.TIMESTAMP, System.currentTimeMillis());
			wifiData.put(TrafficData.NETWORK_TYPE, NETWORK_TYPE_WIFI);
			wifiData.put(TrafficData.RX_BYTES, dWifiRxBytes);
			wifiData.put(TrafficData.TX_BYTES, dWifiTxBytes);
			wifiData.put(TrafficData.RX_PACKETS, dWifiRxPackets);
			wifiData.put(TrafficData.TX_PACKETS, dWifiTxPackets);
			
			// Only add protocol bytes if they are supported by the device
				if (tcpData[0] >= 0) {
					long dTcpRxBytes = 0;
					long dTcpTxBytes = 0;
					if (dWifiRxBytes > 0) {
						/* This calculation relies on the fact that wifi and mobile traffic are exclusive */
						dTcpRxBytes = tcpData[0] - wifiTcpRxBytes;
						dTcpTxBytes = tcpData[1] - wifiTcpTxBytes;
						
						/* Update state vars */
						wifiTcpRxBytes = tcpData[0];
						wifiTcpTxBytes = tcpData[1];
					}
					
					wifiData.put(TrafficData.TCP_RX_BYTES, dTcpRxBytes);
					wifiData.put(TrafficData.TCP_TX_BYTES, dTcpTxBytes);
				}
				
				if (tcpData[2] >= 0) {
					/* This calculation relies on the fact that wifi and mobile traffic are exclusive */
					long dTcpRxSegments = 0;
					long dTcpTxSegments = 0;
					
					if (dWifiRxBytes > 0) {
						dTcpRxSegments = tcpData[2] - wifiTcpRxSegments;
						dTcpTxSegments = tcpData[3] - wifiTcpTxSegments;
						
						/* Update state vars */
						wifiTcpRxSegments = tcpData[2];
						wifiTcpTxSegments = tcpData[3];
					}
					
					wifiData.put(TrafficData.TCP_RX_SEGMENTS, dTcpRxSegments);
					wifiData.put(TrafficData.TCP_TX_SEGMENTS, dTcpTxSegments);
				}
			
			/* Notify listeners and update state */
			notifyListeners(mobileTrafficEvent, new BasicMonitorEventResult(wifiData));
			
			/* Update state vars */
			wifiRxBytes = newWifiRxBytes;
			wifiTxBytes = newWifiTxBytes;
			wifiRxPackets = newWifiRxPackets;
			wifiTxPackets = newWifiTxPackets;
			
			/* Log the results */
			if(DEBUG) Log.d(TAG, wifiData.toString());
		}
	};

	private MonitorEvent mobileTrafficEvent = new AbstractMonitorEvent() {
		ScheduledFuture<?> future = null;
		
		@Override
		public synchronized boolean activate() {
			if (!isActive()) {
				mobileRxBytes = TrafficStats.getMobileRxBytes();
				
				if( mobileRxBytes == TrafficStats.UNSUPPORTED ) {
					if (DEBUG) Log.d(TAG, "Device doesn't support traffic statistics! Disabling sensor...");
					stopSelf();
					
					return false;
				}
				
				mobileTxBytes = TrafficStats.getMobileTxBytes();
				mobileRxPackets = TrafficStats.getMobileRxPackets();
				mobileTxPackets = TrafficStats.getMobileTxPackets();
				
				
				long [] tcpData = getTcpData(mContext);
				if (tcpData[0] > 0) {
					mobileTcpRxBytes = tcpData[0];
					mobileTcpTxBytes = tcpData[1];
				}
				
				if (tcpData[2] > 0) {
					mobileTcpRxSegments = tcpData[2];
					mobileTcpTxSegments = tcpData[3];
				}
			
				future = taskThreadPool.scheduleAtFixedRate(mobileTask, 0, trafficUpdateInterval, TimeUnit.SECONDS);
				super.activate();
				
				if(DEBUG) Log.d(TAG, "Mobile traffic service has been activated");
			}
			return true;
		}

		@Override
		public synchronized void deactivate() {
			if (isActive()) {
				// Stop the task
				future.cancel(false);
				super.deactivate();
				
				if(DEBUG) Log.d(TAG, "Mobile traffic service has been deactivated");
			}
		}
		
		@Override
		public void onDataReceived(MonitorListener listener, MonitorEventResult result) {
			if (listener instanceof TrafficListener) {
				((TrafficListener) listener).onMobileTrafficChanged(result.getData());
			}
		}
	};
	
	private MonitorEvent wifiTrafficEvent = new AbstractMonitorEvent() {
		ScheduledFuture<?> future = null;
		
		@Override
		public synchronized boolean activate() {
			if (!isActive()) {
				long startTotalRxBytes = TrafficStats.getTotalRxBytes();
				long startTotalTxBytes = TrafficStats.getTotalTxBytes();
				long startTotalRxPackets = TrafficStats.getTotalRxPackets();
				long startTotalTxPackets = TrafficStats.getTotalTxPackets();
				
				if( startTotalRxBytes == TrafficStats.UNSUPPORTED ) {
					if (DEBUG) Log.d(TAG, "Device doesn't support traffic statistics! Disabling sensor...");
					stopSelf();
					
					return false;
				}
				
				wifiRxBytes = startTotalRxBytes - TrafficStats.getMobileRxBytes();
				wifiTxBytes = startTotalTxBytes - TrafficStats.getMobileTxBytes();
				wifiRxPackets = startTotalRxPackets - TrafficStats.getMobileRxPackets();
				wifiTxPackets = startTotalTxPackets - TrafficStats.getMobileTxPackets();
				
				long [] tcpData = getTcpData(mContext);
				if (tcpData[0] > 0) {
					wifiTcpRxBytes = tcpData[0];
					wifiTcpTxBytes = tcpData[1];
				}
				
				if (tcpData[2] > 0) {
					wifiTcpRxSegments = tcpData[2];
					wifiTcpTxSegments = tcpData[3];
				}
								
				future = taskThreadPool.scheduleAtFixedRate(wifiTask, 0, trafficUpdateInterval, TimeUnit.SECONDS);
				super.activate();
				
				if(DEBUG) Log.d(TAG, "WiFi traffic service has been activated");
			}
			return true;
		}

		@Override
		public synchronized void deactivate() {
			if (isActive()) {
				// Stop the task
				future.cancel(false);
				super.deactivate();
				
				if(DEBUG) Log.d(TAG, "WiFi traffic service has been deactivated");
			}
		}
		
		@Override
		public void onDataReceived(MonitorListener listener, MonitorEventResult result) {
			if (listener instanceof TrafficListener) {
				((TrafficListener) listener).onMobileTrafficChanged(result.getData());
			}
		}
	};
	
	private ScheduledThreadPoolExecutor taskThreadPool = new ScheduledThreadPoolExecutor(2);
	
	/**
	 * Activity-Service binder
	 */
	private final IBinder serviceBinder = new ServiceBinder();
	
	protected String TAG = "AdkintunMobile::Traffic";
	protected int trafficUpdateInterval = TRAFFIC_UPDATE_INTERVAL;
	
	@Override
	public boolean activate(int events, Bundle configuration) {
		/* Update the traffic update interval */
		trafficUpdateInterval = configuration.getInt(TRAFFIC_UPDATE_INTERVAL_EXTRA, TRAFFIC_UPDATE_INTERVAL);
		
		boolean mobileTrafficActivated = false;
		if ((events & MOBILE_TRAFFIC) == MOBILE_TRAFFIC) {
			mobileTrafficActivated = activate(mobileTrafficEvent);
		}
		
		boolean wifiTrafficActivated = false;
		if ((events & WIFI_TRAFFIC) == WIFI_TRAFFIC) {
			wifiTrafficActivated = activate(wifiTrafficEvent);
		}
		
		return mobileTrafficActivated && wifiTrafficActivated;
	}
	
	@Override
	public void deactivate(int events) {
		if ((events & MOBILE_TRAFFIC) == MOBILE_TRAFFIC) {
			deactivate(mobileTrafficEvent);
		}
		if ((events & WIFI_TRAFFIC) == WIFI_TRAFFIC) {
			deactivate(wifiTrafficEvent);
		}
	}
	
	/**
	 * Method that return an array with the total tcp bytes/segments transmitted/received 
	 * by all applications of the device 
	 *  
	 * The mobile API must be 12 or superior, otherwise the method
	 * will return {-1,-1,-1,-1}.
	 * @param context
	 * @return {TCP bytes received, TCP bytes transmitted, TCP segments transmitted, TCP segments received}
	 */

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	private long[] getTcpData(Context context) {
		if (VERSION.SDK_INT >= 12) {
			ArrayList<Integer> uids = getUids(context);

			long totalTcpRxBytes = 0;
			long totalTcpTxBytes = 0;
			long totalTcpRxSegments = 0;
			long totalTcpTxSegments = 0;
			
			boolean isTcpBytesSupported = false;
			boolean isTcpSegmentsSupported = false;

			long rxBytes, txBytes, rxSegments, txSegments;
			for (int uid: uids) {
				rxBytes = TrafficStats.getUidTcpRxBytes(uid);
				if (rxBytes >= 0) {
					isTcpBytesSupported = true;
					totalTcpRxBytes += rxBytes;
				}
				
				txBytes = TrafficStats.getUidTcpTxBytes(uid);
				if (txBytes >= 0) {
					isTcpBytesSupported = true;
					totalTcpTxBytes += txBytes;
				}
				
				rxSegments = TrafficStats.getUidTcpRxSegments(uid);
				if (rxSegments >= 0) {
					isTcpSegmentsSupported = true;
					totalTcpRxSegments += rxSegments;
				}
				
				txSegments = TrafficStats.getUidTcpTxSegments(uid);
				if (txSegments >= 0) {
					isTcpSegmentsSupported = true;
					totalTcpTxSegments += txSegments;
				}
			}
			
			if (!isTcpBytesSupported) {
				totalTcpRxBytes = -1;
				totalTcpTxBytes = -1;
			}
			
			if (!isTcpSegmentsSupported) {
				totalTcpRxSegments = -1;
				totalTcpTxSegments = -1;
			}
			
			return new long [] {totalTcpRxBytes, totalTcpTxBytes, totalTcpRxSegments, totalTcpTxSegments};
		}
		return new long [] {-1,-1, -1, -1};
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

	@Override
	public IBinder onBind(Intent intent) {
		return serviceBinder;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		/* Shutdown the thread pool */
		taskThreadPool.shutdown();
	}
}
