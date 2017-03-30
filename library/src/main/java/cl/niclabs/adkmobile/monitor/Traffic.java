package cl.niclabs.adkintunmobile.services.monitors;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.TrafficStats;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.SparseArray;

import cl.niclabs.adkmobile.monitor.AbstractMonitor;
import cl.niclabs.adkmobile.monitor.data.Observation;
import cl.niclabs.adkmobile.monitor.data.TrafficObservation;
import cl.niclabs.adkmobile.monitor.data.constants.ConnectionType;
import cl.niclabs.adkmobile.monitor.events.AbstractMonitorEvent;
import cl.niclabs.adkmobile.monitor.events.MonitorEvent;
import cl.niclabs.adkmobile.monitor.listeners.TrafficListener;
import cl.niclabs.android.utils.Scheduler;
import cl.niclabs.android.utils.Time;

/**
 * Implements monitoring of Rx & Tx bytes. Traffic is notified by the system as
 * a listener. This class will listen for each 10 seconds.
 *
 * @author Mauricio Castro. Created 27-09-2013.
 */
public class Traffic extends AbstractMonitor<TrafficListener> {
	/**
	 * @var Frequency of sampling (in seconds)
	 */
	public static int TRAFFIC_UPDATE_INTERVAL = 10;

	/**
	 * @var Frequency of sampling for check NetworkStatsManager for android version > 6 (in seconds)
	 */
	public static int NEW_TRAFFIC_UPDATE_INTERVAL = 600;

	/**
	 * Extra key for configuring the traffic update interval
	 */
	public static final String TRAFFIC_UPDATE_INTERVAL_EXTRA = "traffic_update_interval";

	/**
	 * Represents a mobile network for storage
	 */
	public static final int NETWORK_TYPE_MOBILE = ConnectionType.MOBILE.value();

	/**
	 * Represents a wifi network for storage
	 */
	public static final int NETWORK_TYPE_WIFI = ConnectionType.WIFI.value();

    /**
     * Preference file to save timestamp of the last NetworkStats.Bucket object for each UID
     */
    public static final string PREFERENCE_FILE = "cl.niclabs.adkintunmobile.PREFERENCE_FILE";

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

	private long appWifiRxBytes;

	private Runnable mobileTask = new Runnable() {
		@Override
		public void run() {
			long newMobileRxBytes = TrafficStats.getMobileRxBytes();
			long newMobileTxBytes = TrafficStats.getMobileTxBytes();
			long newMobileRxPackets = TrafficStats.getMobileRxPackets();
			long newMobileTxPackets = TrafficStats.getMobileTxPackets();

			long[] tcpData = getTcpData(mContext);

			long dMobileRxBytes = newMobileRxBytes - mobileRxBytes;
			long dMobileTxBytes = newMobileTxBytes - mobileTxBytes;
			long dMobileTxPackets = newMobileTxPackets - mobileTxPackets;
			long dMobileRxPackets = newMobileRxPackets - mobileRxPackets;

			TrafficObservation mobileData = new TrafficObservation(
					TRAFFIC_MOBILE, Time.currentTimeMillis());
			mobileData.setNetworkType(NETWORK_TYPE_MOBILE);
			mobileData.setRxBytes(dMobileRxBytes);
			mobileData.setTxBytes(dMobileTxBytes);
			mobileData.setRxPackets(dMobileRxPackets);
			mobileData.setTxPackets(dMobileTxPackets);

			// Only add protocol bytes if they are supported by the device
			if (tcpData[0] >= 0) {
				long dTcpRxBytes = 0;
				long dTcpTxBytes = 0;
				if (dMobileRxBytes > 0) {
					/*
					 * This calculation relies on the fact that wifi and mobile
					 * traffic are exclusive
					 */
					dTcpRxBytes = tcpData[0] - mobileTcpRxBytes;
					dTcpTxBytes = tcpData[1] - mobileTcpTxBytes;

					/* Update state vars */
					mobileTcpRxBytes = tcpData[0];
					mobileTcpTxBytes = tcpData[1];
				}

				mobileData.setTcpRxBytes(dTcpRxBytes);
				mobileData.setTcpTxBytes(dTcpTxBytes);
			}

			if (tcpData[2] >= 0) {
				/*
				 * This calculation relies on the fact that wifi and mobile
				 * traffic are exclusive
				 */
				long dTcpRxSegments = tcpData[2] - mobileTcpRxSegments;
				long dTcpTxSegments = tcpData[3] - mobileTcpTxSegments;

				if (dMobileRxBytes > 0) {
					dTcpRxSegments = tcpData[2] - mobileTcpRxSegments;
					dTcpTxSegments = tcpData[3] - mobileTcpTxSegments;

					/* Update state vars */
					mobileTcpRxSegments = tcpData[2];
					mobileTcpTxSegments = tcpData[3];
				}

				mobileData.setTcpRxSegments(dTcpRxSegments);
				mobileData.setTcpRxSegments(dTcpTxSegments);
			}

			/* Notify listeners and update state */
			notifyListeners(mobileTrafficEvent, mobileData);

			/* Update state vars */
			mobileRxBytes = newMobileRxBytes;
			mobileTxBytes = newMobileTxBytes;
			mobileRxPackets = newMobileRxPackets;
			mobileTxPackets = newMobileTxPackets;

			/* Log the results */
			if (DEBUG)
				Log.v(TAG, mobileData.toString());
		}
	};

	private Runnable wifiTask = new Runnable() {
		@Override
		public void run() {
			long[] tcpData = getTcpData(mContext);

			long newWifiRxBytes = TrafficStats.getTotalRxBytes()
					- TrafficStats.getMobileRxBytes();
			long newWifiTxBytes = TrafficStats.getTotalTxBytes()
					- TrafficStats.getMobileTxBytes();
			long newWifiRxPackets = TrafficStats.getTotalRxPackets()
					- TrafficStats.getMobileRxPackets();
			long newWifiTxPackets = TrafficStats.getTotalTxPackets()
					- TrafficStats.getMobileTxPackets();

			long dWifiRxBytes = newWifiRxBytes - wifiRxBytes;
			long dWifiTxBytes = newWifiTxBytes - wifiTxBytes;

			long dWifiRxPackets = newWifiRxPackets - wifiRxPackets;
			long dWifiTxPackets = newWifiTxPackets - wifiTxPackets;

			TrafficObservation wifiData = new TrafficObservation(TRAFFIC_WIFI,
					Time.currentTimeMillis());
			wifiData.setNetworkType(NETWORK_TYPE_WIFI);
			wifiData.setRxBytes(dWifiRxBytes);
			wifiData.setTxBytes(dWifiTxBytes);
			wifiData.setRxPackets(dWifiRxPackets);
			wifiData.setTxPackets(dWifiTxPackets);

			// Only add protocol bytes if they are supported by the device
			if (tcpData[0] >= 0) {
				long dTcpRxBytes = 0;
				long dTcpTxBytes = 0;
				if (dWifiRxBytes > 0) {
					/*
					 * This calculation relies on the fact that wifi and mobile
					 * traffic are exclusive
					 */
					dTcpRxBytes = tcpData[0] - wifiTcpRxBytes;
					dTcpTxBytes = tcpData[1] - wifiTcpTxBytes;

					/* Update state vars */
					wifiTcpRxBytes = tcpData[0];
					wifiTcpTxBytes = tcpData[1];
				}

				wifiData.setTcpRxBytes(dTcpRxBytes);
				wifiData.setTcpTxBytes(dTcpTxBytes);
			}

			if (tcpData[2] >= 0) {
				/*
				 * This calculation relies on the fact that wifi and mobile
				 * traffic are exclusive
				 */
				long dTcpRxSegments = 0;
				long dTcpTxSegments = 0;

				if (dWifiRxBytes > 0) {
					dTcpRxSegments = tcpData[2] - wifiTcpRxSegments;
					dTcpTxSegments = tcpData[3] - wifiTcpTxSegments;

					/* Update state vars */
					wifiTcpRxSegments = tcpData[2];
					wifiTcpTxSegments = tcpData[3];
				}

				wifiData.setTcpRxSegments(dTcpRxSegments);
				wifiData.setTcpRxSegments(dTcpTxSegments);
			}

			/* Notify listeners and update state */
			notifyListeners(wifiTrafficEvent, wifiData);

			/* Update state vars */
			wifiRxBytes = newWifiRxBytes;
			wifiTxBytes = newWifiTxBytes;
			wifiRxPackets = newWifiRxPackets;
			wifiTxPackets = newWifiTxPackets;

			/* Log the results */
			if (DEBUG)
				Log.v(TAG, wifiData.toString());
		}
	};

	private Runnable appTask = new Runnable() {
		@Override
		public void run() {
			if (VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH) {
				uids = geRunningProcessesUids(mContext);
			} else {
				uids = getUids(mContext);
			}

			if (VERSION.SDK_INT >= Build.VERSION_CODES.M){
				calculateApplicationTrafficForAllUids(uids);
			}
			else {
				long newWifiRxBytes = TrafficStats.getTotalRxBytes()
						- TrafficStats.getMobileRxBytes();
				long dWifiRxBytes = newWifiRxBytes - appWifiRxBytes;

				int networkType = NETWORK_TYPE_MOBILE;
				if (dWifiRxBytes > 0) {
					networkType = NETWORK_TYPE_WIFI;
				}
				for (int uid : uids) {
					calculateApplicationTrafficForUid(networkType, uid);
				}

				appWifiRxBytes = newWifiRxBytes;
			}
		}
	};

	private SparseArray<Long> appRxBytes;
	private SparseArray<Long> appTxBytes;
	private SparseArray<Long> appRxPackets;
	private SparseArray<Long> appTxPackets;

	private ArrayList<Integer> uids;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	private void calculateApplicationTrafficForUid(int networkType, int uid) {

		TrafficObservation appData = new TrafficObservation(
				TRAFFIC_APPLICATION, Time.currentTimeMillis());
		appData.setUid(uid);

		long newAppRxBytes = TrafficStats.getUidRxBytes(uid);
		long newAppTxBytes = TrafficStats.getUidTxBytes(uid);

		// IF the entry does not exist, the delta is 0
		long dAppRxBytes = newAppRxBytes
				- (appRxBytes.indexOfKey(uid) < 0 ? newAppRxBytes : appRxBytes
				.get(uid));
		long dAppTxBytes = newAppTxBytes
				- (appTxBytes.indexOfKey(uid) < 0 ? newAppTxBytes : appTxBytes
				.get(uid));

		/* Update state vars */
		appRxBytes.put(uid, newAppRxBytes);
		appTxBytes.put(uid, newAppTxBytes);

		if (dAppRxBytes <= 0 && dAppTxBytes <= 0)
			return;

		appData.setNetworkType(networkType);
		appData.setRxBytes(dAppRxBytes);
		appData.setTxBytes(dAppTxBytes);

		if (VERSION.SDK_INT >= 12) {
			long newAppRxPackets = TrafficStats.getUidRxPackets(uid);
			long newAppTxPackets = TrafficStats.getUidTxPackets(uid);

			// If the entry does not exist, the delta is 0
			long dAppRxPackets = newAppRxPackets
					- (appRxPackets.indexOfKey(uid) < 0 ? newAppRxPackets
					: appRxPackets.get(uid));
			long dAppTxPackets = newAppTxPackets
					- (appTxPackets.indexOfKey(uid) < 0 ? newAppRxPackets
					: appTxPackets.get(uid));

			if (dAppRxPackets >= 0) {
				appData.setRxPackets(dAppRxPackets);

				/* Update state vars */
				appRxPackets.put(uid, newAppRxPackets);
			}

			if (dAppTxPackets >= 0) {
				appData.setTxPackets(dAppTxPackets);
				appTxPackets.put(uid, newAppTxPackets);
			}
		}

		/* Notify listeners and update state */
		notifyListeners(appTrafficEvent, appData);

		/* Log the results */
		if (DEBUG)
			Log.v(TAG, appData.toString());
	}

    /**
     * Starting in Android 6, the method calculateApplicationTrafficForUid doesn't work,
     * because isn't possible to get TrafficStats for other UIDs (for privacy reasons).
     * This method uses NetworkStatsManager to access historical network statistics belonging
     * to other UIDs, getting how many bytes have been received and transmitted over WiFi or
     * Mobile Network.
     *
     * @param uids  Current installed applications UIDs
     */
	@TargetApi(Build.VERSION_CODES.M)
	private void calculateApplicationTrafficForAllUids(ArrayList<Integer> uids){
		TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		String subscriberID = tm.getSubscriberId();
		SharedPreferences sharedPreferences = getSharedPreferences(
				PREFERENCE_FILE, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();

		NetworkStats.Bucket bucketOut = new NetworkStats.Bucket();
		NetworkStatsManager networkStatsManager =
				(NetworkStatsManager) getSystemService(Context.NETWORK_STATS_SERVICE);
		NetworkStats stats;
		long currentTimeMillis = System.currentTimeMillis();

		Calendar calendar = Calendar.getInstance(Locale.getDefault());
		calendar.setTimeInMillis(currentTimeMillis);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		long startOfActualDay = calendar.getTimeInMillis();

		for (int uid : uids){
            //Get wifi traffic
			String uidWifiKey = Integer.toString(uid) + "_wifi";
			long lastEndTimestamp;
			if (sharedPreferences.contains(uidWifiKey))
				lastEndTimestamp = sharedPreferences.getLong(uidWifiKey, 0);
			else {
				editor.putLong(uidWifiKey, startOfActualDay);
				lastEndTimestamp = startOfActualDay;
				editor.commit();
			}
			try {
				stats = networkStatsManager.queryDetailsForUid(ConnectivityManager.TYPE_WIFI, "",
						lastEndTimestamp, currentTimeMillis, uid);
				while (stats.hasNextBucket()){
					stats.getNextBucket(bucketOut);
					TrafficObservation appData = new TrafficObservation(
							TRAFFIC_APPLICATION, bucketOut.getEndTimeStamp());
					appData.setUid(bucketOut.getUid());
					appData.setNetworkType(NETWORK_TYPE_WIFI);
					appData.setRxBytes(bucketOut.getRxBytes());
					appData.setTxBytes(bucketOut.getTxBytes());
					appData.setRxPackets(bucketOut.getRxPackets());
					appData.setTxPackets(bucketOut.getTxPackets());
					editor.putLong(uidWifiKey, bucketOut.getEndTimeStamp());
					notifyListeners(appTrafficEvent, appData);
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			} finally {
                editor.commit();
            }

            //Get mobile traffic
			String uidMobileKey = Integer.toString(uid) + "_mobile";
			if (sharedPreferences.contains(uidMobileKey))
				lastEndTimestamp = sharedPreferences.getLong(uidMobileKey, 0);
			else {
				editor.putLong(uidMobileKey, startOfActualDay);
				lastEndTimestamp = startOfActualDay;
				editor.commit();
			}
			try {
				stats = networkStatsManager.queryDetailsForUid(ConnectivityManager.TYPE_MOBILE,
						subscriberID, lastEndTimestamp, currentTimeMillis, uid);
				while (stats.hasNextBucket()){
					stats.getNextBucket(bucketOut);
					TrafficObservation appData = new TrafficObservation(
							TRAFFIC_APPLICATION, bucketOut.getEndTimeStamp());
					appData.setUid(bucketOut.getUid());
					appData.setNetworkType(NETWORK_TYPE_MOBILE);
					appData.setRxBytes(bucketOut.getRxBytes());
					appData.setTxBytes(bucketOut.getTxBytes());
					appData.setRxPackets(bucketOut.getRxPackets());
					appData.setTxPackets(bucketOut.getTxPackets());
					editor.putLong(uidMobileKey, bucketOut.getEndTimeStamp());
					notifyListeners(appTrafficEvent, appData);
				}
			} catch (RemoteException e) {
				e.printStackTrace();
            } finally {
                editor.commit();
            }
		}
	}

	private MonitorEvent<TrafficListener> mobileTrafficEvent = new AbstractMonitorEvent<TrafficListener>() {
		ScheduledFuture<?> future = null;

		@Override
		public synchronized boolean activate() {
			if (!isActive()) {

				if (uids == null) {
					uids = new ArrayList<Integer>();
				}

				mobileRxBytes = TrafficStats.getMobileRxBytes();

				if (mobileRxBytes == TrafficStats.UNSUPPORTED) {
					if (DEBUG)
						Log.d(TAG,
								"Device doesn't support traffic statistics! Disabling sensor...");
					stopSelf();

					return false;
				}

				mobileTxBytes = TrafficStats.getMobileTxBytes();
				mobileRxPackets = TrafficStats.getMobileRxPackets();
				mobileTxPackets = TrafficStats.getMobileTxPackets();

				long[] tcpData = getTcpData(mContext);
				if (tcpData[0] > 0) {
					mobileTcpRxBytes = tcpData[0];
					mobileTcpTxBytes = tcpData[1];
				}

				if (tcpData[2] > 0) {
					mobileTcpRxSegments = tcpData[2];
					mobileTcpTxSegments = tcpData[3];
				}

				future = Scheduler.getInstance().scheduleAtFixedRate(
						mobileTask, 0, TRAFFIC_UPDATE_INTERVAL,
						TimeUnit.SECONDS);
				super.activate();

				if (DEBUG)
					Log.d(TAG, "Mobile traffic service has been activated");
			}
			return true;
		}

		@Override
		public synchronized void deactivate() {
			if (isActive()) {
				// Stop the task
				future.cancel(false);
				super.deactivate();

				if (DEBUG)
					Log.d(TAG, "Mobile traffic service has been deactivated");
			}
		}

		@Override
		public void onDataReceived(TrafficListener listener, Observation result) {
			listener.onMobileTrafficChange((TrafficObservation) result);
		}
	};

	private MonitorEvent<TrafficListener> wifiTrafficEvent = new AbstractMonitorEvent<TrafficListener>() {
		ScheduledFuture<?> future = null;

		@Override
		public synchronized boolean activate() {
			if (!isActive()) {
				long startTotalRxBytes = TrafficStats.getTotalRxBytes();
				long startTotalTxBytes = TrafficStats.getTotalTxBytes();
				long startTotalRxPackets = TrafficStats.getTotalRxPackets();
				long startTotalTxPackets = TrafficStats.getTotalTxPackets();

				if (startTotalRxBytes == TrafficStats.UNSUPPORTED) {
					if (DEBUG)
						Log.d(TAG,
								"Device doesn't support traffic statistics! Disabling sensor...");
					stopSelf();

					return false;
				}

				wifiRxBytes = startTotalRxBytes
						- TrafficStats.getMobileRxBytes();
				wifiTxBytes = startTotalTxBytes
						- TrafficStats.getMobileTxBytes();
				wifiRxPackets = startTotalRxPackets
						- TrafficStats.getMobileRxPackets();
				wifiTxPackets = startTotalTxPackets
						- TrafficStats.getMobileTxPackets();

				if (uids == null) {
					uids = new ArrayList<Integer>();
				}

				long[] tcpData = getTcpData(mContext);
				if (tcpData[0] > 0) {
					wifiTcpRxBytes = tcpData[0];
					wifiTcpTxBytes = tcpData[1];
				}

				if (tcpData[2] > 0) {
					wifiTcpRxSegments = tcpData[2];
					wifiTcpTxSegments = tcpData[3];
				}

				future = Scheduler.getInstance().scheduleAtFixedRate(wifiTask,
						0, TRAFFIC_UPDATE_INTERVAL, TimeUnit.SECONDS);
				super.activate();

				if (DEBUG)
					Log.d(TAG, "WiFi traffic service has been activated");
			}
			return true;
		}

		@Override
		public synchronized void deactivate() {
			if (isActive()) {
				// Stop the task
				future.cancel(false);
				super.deactivate();

				if (DEBUG)
					Log.d(TAG, "WiFi traffic service has been deactivated");
			}
		}

		@Override
		public void onDataReceived(TrafficListener listener, Observation result) {
			listener.onWiFiTrafficChange((TrafficObservation) result);
		}
	};

	private MonitorEvent<TrafficListener> appTrafficEvent = new AbstractMonitorEvent<TrafficListener>() {
		ScheduledFuture<?> future = null;

		@Override
		public synchronized boolean activate() {
			if (!isActive()) {
				if (TrafficStats.getTotalRxBytes() == TrafficStats.UNSUPPORTED) {
					if (DEBUG)
						Log.d(TAG,
								"Device doesn't support traffic statistics! Disabling sensor...");
					stopSelf();
					return false;
				}

				/**
				 * Save the total WiFi bytes to detect the connection type
				 */
				appWifiRxBytes = TrafficStats.getTotalRxBytes()
						- TrafficStats.getMobileRxBytes();
				if (appRxBytes == null) {
					appRxBytes = new SparseArray<Long>();
					appTxBytes = new SparseArray<Long>();
					appRxPackets = new SparseArray<Long>();
					appTxPackets = new SparseArray<Long>();
				}

				if (uids == null) {
					uids = new ArrayList<Integer>();
				}

				int interval = (VERSION.SDK_INT >= Build.VERSION_CODES.M) ?
						NEW_TRAFFIC_UPDATE_INTERVAL : TRAFFIC_UPDATE_INTERVAL;
				future = Scheduler.getInstance().scheduleAtFixedRate(appTask,
						0, interval, TimeUnit.SECONDS);
				super.activate();

				if (DEBUG)
					Log.d(TAG, "Application traffic service has been activated");
			}
			return true;
		}

		@Override
		public synchronized void deactivate() {
			if (isActive()) {
				// Stop the task
				future.cancel(false);
				super.deactivate();

				if (DEBUG)
					Log.d(TAG,
							"Application traffic service has been deactivated");
			}
		}

		@Override
		public void onDataReceived(TrafficListener listener, Observation result) {
			listener.onApplicationTrafficChange((TrafficObservation) result);
		}
	};

	/**
	 * Activity-Service binder
	 */
	private final IBinder serviceBinder = new ServiceBinder<Traffic>(this);

	protected String TAG = "AdkintunMobile::Traffic";

	@Override
	public void activate(int events, Bundle configuration) {
		/* Update the traffic update interval */
		TRAFFIC_UPDATE_INTERVAL = configuration.getInt(
				TRAFFIC_UPDATE_INTERVAL_EXTRA, TRAFFIC_UPDATE_INTERVAL);

		if ((events & TRAFFIC_MOBILE) == TRAFFIC_MOBILE) {
			activate(mobileTrafficEvent);
		}

		if ((events & TRAFFIC_WIFI) == TRAFFIC_WIFI) {
			activate(wifiTrafficEvent);
		}

		if ((events & TRAFFIC_APPLICATION) == TRAFFIC_APPLICATION) {
			activate(appTrafficEvent);
		}
	}

	@Override
	public void deactivate(int events) {
		if ((events & TRAFFIC_MOBILE) == TRAFFIC_MOBILE) {
			deactivate(mobileTrafficEvent);
		}
		if ((events & TRAFFIC_WIFI) == TRAFFIC_WIFI) {
			deactivate(wifiTrafficEvent);
		}
		if ((events & TRAFFIC_APPLICATION) == TRAFFIC_APPLICATION) {
			deactivate(appTrafficEvent);
		}
	}

	/**
	 * Method that return an array with the total tcp bytes/segments
	 * transmitted/received by all applications of the device
	 *
	 * The mobile API must be 12 or superior, otherwise the method will return
	 * {-1,-1,-1,-1}.
	 *
	 * @param context
	 * @return {TCP bytes received, TCP bytes transmitted, TCP segments
	 *         transmitted, TCP segments received}
	 */

	@SuppressWarnings("deprecation")
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
			for (int uid : uids) {
				rxBytes = TrafficStats.getUidRxBytes(uid);
				if (rxBytes >= 0) {
					isTcpBytesSupported = true;
					totalTcpRxBytes += rxBytes;
				}

				txBytes = TrafficStats.getUidTxBytes(uid);
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

			return new long[] { totalTcpRxBytes, totalTcpTxBytes,
					totalTcpRxSegments, totalTcpTxSegments };
		}
		return new long[] { -1, -1, -1, -1 };
	}

	/**
	 * Method that returns an arrayList with the UIDs of all the applications on
	 * the mobile.
	 *
	 * @param context
	 * @return An ArrayList with the UIDs
	 */
	private ArrayList<Integer> getUids(Context context) {
		List<ApplicationInfo> appsInfo = context.getPackageManager()
				.getInstalledApplications(
						PackageManager.GET_UNINSTALLED_PACKAGES);

		for (ApplicationInfo info : appsInfo) {
			if (!uids.contains(info.uid)) {
				uids.add(info.uid);
			}
		}
		return uids;
	}

	/**
	 * Method that returns an arrayList with the UIDs of the applications
	 * running on the mobile.
	 *
	 * @param context
	 * @return An ArrayList with the UIDs
	 */
	private ArrayList<Integer> geRunningProcessesUids(Context context) {
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> runningProcesses = manager
				.getRunningAppProcesses();
		for (RunningAppProcessInfo ra : runningProcesses) {
			if (!uids.contains(ra.uid)) {
				uids.add(ra.uid);
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
	}
}
