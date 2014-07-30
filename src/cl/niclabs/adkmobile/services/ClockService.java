package cl.niclabs.adkmobile.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import cl.niclabs.adkmobile.AdkintunMobileApp;
import cl.niclabs.adkmobile.monitor.Connectivity;
import cl.niclabs.adkmobile.monitor.Monitor;
import cl.niclabs.adkmobile.monitor.MonitorConnection;
import cl.niclabs.adkmobile.monitor.data.ClockSynchronizationState;
import cl.niclabs.adkmobile.monitor.data.constants.ClockState;
import cl.niclabs.adkmobile.monitor.proxies.ConnectivityStatus;
import cl.niclabs.adkmobile.monitor.proxies.ConnectivityStatusListener;
import cl.niclabs.adkmobile.net.SntpClient;

/**
 * Service to provide monotonically increasing real time from a clock synchronized
 * with an NTP server
 * 
 * The service listens to the Connectivity monitor and updates a clock using an
 * NTP server whenever the connection becomes available (when it was unavailable
 * before) or at the start of the application
 * 
 * If the service is not running the time provided by the function
 * currentTimeMillis reverts to System.currentTimeMillis()
 * 
 * In order to run the service each time target application is loaded, it is
 * necessary to add the Connectivity and ClockService to the manifest of the
 * target application and add set cl.niclabs.adkmobile.AdkintunMobileApp as the
 * application name in order to start the service automatically on application
 * load. 
 * 
 * The example below shows the configuration parameters required.
 * <code>
 * <!-- Required for accessing the NTP server -->
 * <uses-permission android:name="android.permission.INTERNET" />
 * 
 * <application
 *      ...
 *      android:name="cl.niclabs.adkmobile.AdkintunMobileApp"> <!-- To start the service automatically --> 
 *      
 *      <service android:name="cl.niclabs.adkmobile.monitor.Connectivity" />
 * 		<service android:name="cl.niclabs.adkmobile.monitor.ClockService" />
 * </application>
 * </code>
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 */
public final class ClockService extends Service implements ConnectivityStatusListener {
	/**
	 * Timeout for NTP server (in milliseconds)
	 */
	public static final int NTP_TIMEOUT = 5000;
	public static final int NTP_MAX_RETRY_FREQUENCY = 5000;
	public static final int NTP_MAX_FREQUENCY = 3600000; // 1 hour
	public static final String NTP_HOST = "ntp.shoa.cl";
	
	private static final String REAL_TIMESTAMP_PROPERTY = "cl.niclabs.adkmobile.time.real_timestamp";
	private static final String SYSTEM_TIMESTAMP_PROPERTY = "cl.niclabs.adkmobile.time.real_timestamp";
	
	private ConnectivityStatus connectivityProxy;
	private MonitorConnection<Connectivity> connection;
	private Connectivity connectivity;
	
	/** 
	 * Time for system boot in milliseconds
	 */
	private static Long bootTimeMillis = null;
	private static boolean realTime = false;
	private static long lastSynchronizationTime = 0L;
	private static long lastSynchronizationAttempt = 0L;
	
	
	private static boolean running = false;
	
	/**
	 * Return the time in milliseconds, if the service was able to synchronize
	 * correctly with an NTP service the returned time is the real time of the 
	 * device, otherwise the time is the one returned by System.currentTimeMillis()
	 * 
	 * @return
	 */
	public static long currentTimeMillis() {
		if (bootTimeMillis == null) // If the service is not started return System.currentTimeMillis()
			return System.currentTimeMillis();
		
		return bootTimeMillis + SystemClock.elapsedRealtime();
	}
	
	public static boolean isRealTime() {
		return realTime;
	}
	
	public static boolean isRunning() {
		return running;
	}
	
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		running = true;
		
		// Restore clock status as early as possible
		if (!restoreClockStatus() && AdkintunMobileApp.isPersistenceAvailable()) {
			// save time state to persistence
			ClockSynchronizationState state = new ClockSynchronizationState(System.currentTimeMillis());
			state.setState(ClockState.UNSYNCHRONIZED);
			state.save();
		}
		
		final ClockService context = this;
		
		// Bind the service
		bindService(new Intent(this, Connectivity.class), connection = new MonitorConnection<Connectivity>() {
			@Override
			public void onServiceConnected(Connectivity service) {
				// Add time service service to the proxy
				connectivityProxy = ConnectivityStatus.getInstance();
				connectivityProxy.listen(context, true);
				
				// Add the proxy as a listener to the connectivity service
				service.listen(connectivityProxy, true);
				
				// Activate the service if not active
				service.activate(Monitor.CONNECTIVITY);
				
				// Save the reference to the monitor
				connectivity = service;
			}

			@Override
			public void onServiceCrash(Connectivity service) {
				// Stop listening
				connectivityProxy.listen(context, false);
				service.listen(connectivityProxy, false);
				
				// Restart the service
				context.bindService(new Intent(context, Connectivity.class), this, BIND_AUTO_CREATE);
			}
			
		}, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		running = false;
		
		// Stop listening
		connectivityProxy.listen(this, false);
		connectivity.listen(connectivityProxy, false);
		
		// Unbind the service
		unbindService(connection);
	}

	@Override
	public void onWifiConnection() {
		// Do nothing
	}

	@Override
	public void onMobileConnection(boolean isRoaming) {
		// Do nothing
	}

	@Override
	public void onRoaming(boolean dataRoamingEnabled) {
		// Do nothing
	}

	@Override
	public void onNetworkDisconnection() {
		// Update the system
		saveClockStatus();
	}
	
	/**
	 * Restore the saved settings if any
	 */
	private boolean restoreClockStatus() {
		SharedPreferences settings = getSharedPreferences(AdkintunMobileApp.LIBRARY_SETTINGS, Context.MODE_PRIVATE);
		long lastRealTime = settings.getLong(REAL_TIMESTAMP_PROPERTY, 0L);
		long lastSystemTime = settings.getLong(SYSTEM_TIMESTAMP_PROPERTY, 0L);
		
		if (lastRealTime == 0L || lastSystemTime == 0) {
			// TODO: set the state on the StateChange
			realTime = false;
			bootTimeMillis = System.currentTimeMillis() - SystemClock.elapsedRealtime();
			return false;
		}
		
		// We assume that the offset (lastRealTime - lastSystemTime) is kept after  
		bootTimeMillis = System.currentTimeMillis() + (lastRealTime - lastSystemTime) - SystemClock.elapsedRealtime();
		realTime = true;
		
		return true;
	}
	
	/**
	 * Update the system properties with the system and real time
	 */
	private void saveClockStatus() {
		if (realTime) {
			SharedPreferences.Editor editor = getSharedPreferences(AdkintunMobileApp.LIBRARY_SETTINGS, Context.MODE_PRIVATE).edit();
			editor.putLong(REAL_TIMESTAMP_PROPERTY, currentTimeMillis());
			editor.putLong(SYSTEM_TIMESTAMP_PROPERTY, System.currentTimeMillis());
			editor.commit();
		}
	}
	
	/**
	 * Synchronize time with the NTP server
	 * @return true if synchronization was performed successfully
	 */
	private boolean synchronizeClock() {
		SntpClient client = new SntpClient();
		if (SystemClock.elapsedRealtime() - lastSynchronizationTime >= NTP_MAX_FREQUENCY &&
				SystemClock.elapsedRealtime() - lastSynchronizationAttempt >= NTP_MAX_RETRY_FREQUENCY
				&& client.requestTime(NTP_HOST, NTP_TIMEOUT)) {
			bootTimeMillis = client.getNtpTime() - client.getNtpTimeReference();
			lastSynchronizationTime = SystemClock.elapsedRealtime();
			realTime = true;
			
			Log.d("AdkintunMobile::ClockService", "Synchronized time "+currentTimeMillis());
			
			return true;
		}
		lastSynchronizationAttempt = SystemClock.elapsedRealtime();
		return false;
	}

	@Override
	public void onNetworkConnection(boolean isMobileRoaming) {
		if (!isMobileRoaming && synchronizeClock()) {
			saveClockStatus();
			
			if (AdkintunMobileApp.isPersistenceAvailable()) {
				ClockSynchronizationState state = new ClockSynchronizationState(System.currentTimeMillis());
				state.setRealTime(currentTimeMillis());
				state.setState(ClockState.SYNCHRONIZED);
				state.save();
			}
		}
	}
}
