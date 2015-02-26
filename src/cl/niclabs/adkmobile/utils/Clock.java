package cl.niclabs.adkmobile.utils;

import java.util.Date;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import cl.niclabs.adkmobile.AdkintunMobileApp;
import cl.niclabs.adkmobile.net.SntpClient;

/**
 * Service to provide monotonically increasing real time from a clock synchronized
 * with an NTP server
 * 
 * The class provides the static methods currentTimeMillis and currentTime that return the 
 * current NTP time if the service has correctly synchronized with a remote service 
 * (calling the method synchronize())
 * 
 * If the service is not running or it has not synchronized the time provided by the function
 * currentTimeMillis reverts to System.currentTimeMillis()
 * 
 * In order to run the service each time target application is loaded, it is
 * necessary to add the Clock service to the manifest of the
 * target application.
 * 
 * The example below shows the configuration parameters required.
 * <code>
 * <!-- Required for accessing the NTP server -->
 * <uses-permission android:name="android.permission.INTERNET" />
 * 
 * <application ...>       
 * 		<service android:name="cl.niclabs.adkmobile.utils.Clock" />
 * </application>
 * </code>
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 */
public class Clock extends Service {
	/**
	 * Binder for this clock
	 * 
	 * @author Felipe Lalanne <flalanne@niclabs.cl>
	 */
	public static class ServiceBinder extends android.os.Binder {
		private Clock service;

		public ServiceBinder(Clock service) {
			super();
			this.service = service;
		}

		public Clock getService() {
			return service;
		}
	}
	/** 
	 * Time for system boot in milliseconds
	 */
	private static Long bootTimeMillis = null;
	
	/**
	 * Default NTP HOST
	 */
	public static final String NTP_HOST = "ntp.shoa.cl";
	
	/**
	 * Default NTP retry frequency
	 */
	public static final int NTP_RETRY_FREQUENCY = 5000;
	
	/**
	 * Default NTP synchronization frequency
	 */
	public static final int NTP_SYNC_FREQUENCY = 3600000; // 1 hour
	
	/**
	 * Timeout for NTP server (in milliseconds)
	 */
	public static final int NTP_TIMEOUT = 5000;
	
	private static boolean running = false;
	
	private static final String REAL_TIMESTAMP_PROPERTY = "cl.niclabs.adkmobile.time.real_timestamp";
	private static final String SYSTEM_TIMESTAMP_PROPERTY = "cl.niclabs.adkmobile.time.real_timestamp";
	private static final String LAST_SYNC_PROPERTY = "cl.niclabs.adkmobile.time.last_sync";
	
	private static final String TAG = "AdkintunMobile::ClockService";
	
	/**
	 * Get current time in UTC as a date object
	 * @return
	 */
	public static Date currentTime() {
		return new Date(currentTimeMillis());
	}
	
	/**
	 * Return the time in milliseconds, if the service was able to synchronize
	 * correctly with an NTP service the returned time is the real time of the 
	 * device, otherwise the time is the one returned by System.currentTimeMillis()
	 * 
	 * @return
	 */
	public static long currentTimeMillis() {
		// If the service is not started return system time
		if (bootTimeMillis == null) 
			return System.currentTimeMillis();
		
		return bootTimeMillis + SystemClock.elapsedRealtime();
	}
	
	public static boolean isRunning() {
		return running;
	}
	
	private boolean isSynchronized = false;
	
	private String ntpHost =  NTP_HOST;

	private int ntpRetryFrequency = NTP_RETRY_FREQUENCY;
	private int ntpSyncFrequency = NTP_SYNC_FREQUENCY;
	private int ntpTimeout = NTP_TIMEOUT;
	
	/**
	 * Last synchronization attempt
	 */
	private long lastSyncAttempt = 0L;
	
	/**
	 * Last synchronization time
	 */
	private long lastSyncTime = 0L;
	
	private final IBinder serviceBinder = new ServiceBinder(this);
	
	/**
	 * Restore the status of the clock from the 
	 * application settings
	 */
	private boolean fromSharedPreferences() {
		SharedPreferences settings = getSharedPreferences(AdkintunMobileApp.LIBRARY_SETTINGS, Context.MODE_PRIVATE);
		long lastRealTime = settings.getLong(REAL_TIMESTAMP_PROPERTY, 0L);
		long lastSystemTime = settings.getLong(SYSTEM_TIMESTAMP_PROPERTY, 0L);
		lastSyncTime = settings.getLong(LAST_SYNC_PROPERTY, 0L);
		
		if (lastRealTime == 0L || lastSystemTime == 0) {
			isSynchronized = false;
			bootTimeMillis = System.currentTimeMillis() - SystemClock.elapsedRealtime();
			return false;
		}
		
		// We assume that the offset (lastRealTime - lastSystemTime) is kept since last synchronization
		bootTimeMillis = System.currentTimeMillis() + (lastRealTime - lastSystemTime) - SystemClock.elapsedRealtime();
		isSynchronized = true;
		
		return true;
	}

	/**
	 * 
	 * @return time for last synchronization attempt, in millis or 0L if no synchronization has been performed
	 */
	public long getLastSyncAttempt() {
		return lastSyncAttempt;
	}

	/**
	 * 
	 * @return time of last synchronization, in millis, or 0L if no synchronization has been performed
	 */
	public long getLastSyncTime() {
		return lastSyncTime;
	}

	/**
	 * Get the configured NTP host
	 * @return
	 */
	public String getNtpHost() {
		return ntpHost;
	}

	/**
	 * Get the configured retry frequency between 
	 * NTP failed synchronization attempts
	 * @return
	 */
	public int getNtpRetryFrequency() {
		return ntpRetryFrequency;
	}

	/**
	 * Get the configured maximum frequency between 
	 * successful synchronizations in milliseconds
	 * @return
	 */
	public int getNtpSyncFrequency() {
		return ntpSyncFrequency;
	}

	/**
	 * Get the maximum time to wait until 
	 * a timeout with the server is pronounced
	 * @return
	 */
	public int getNtpTimeout() {
		return ntpTimeout;
	}

	/**
	 * Return true if the clock has synchronized with the NTP server in the last day
	 * @return
	 */
	public boolean hasSynchronizedInLastDay() {
		return isSynchronized && currentTimeMillis() - getLastSyncTime() <= 24 * 3600 * 1000;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return serviceBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		running = true;
		
		// Restore clock status as early as possible
		if (!fromSharedPreferences()) {
			Log.d(TAG, "Could not restore clock status");
			isSynchronized = false;
		}
	}

	/**
	 * Modify the NTP host for synchronization
	 * 
	 * Defaults to NTP_HOST
	 * 
	 * @param ntpHost
	 */
	public void setNtpHost(String ntpHost) {
		this.ntpHost = ntpHost;
	}

	/**
	 * Set the configured retry frequency between 
	 * NTP failed synchronization attempts
	 * @param ntpRetryFrequency
	 */
	public void setNtpRetryFrequency(int ntpRetryFrequency) {
		this.ntpRetryFrequency = ntpRetryFrequency;
	}
	
	/**
	 * Set the maximum frequency between successful
	 * synchronizations (in seconds)
	 * 
	 * @param ntpSyncFrequency
	 */
	public void setNtpSyncFrequency(int ntpSyncFrequency) {
		this.ntpSyncFrequency = ntpSyncFrequency;
	}
	
	/**
	 * Set the maximum time to wait until 
	 * a timeout with the server is pronounced
	 * 
	 * @param ntpTimeout
	 */
	public void setNtpTimeout(int ntpTimeout) {
		this.ntpTimeout = ntpTimeout;
	}
	
	/**
	 * Synchronize time with the NTP server
	 * 
	 * This method blocks until the NTP server responds or timeout is reached
	 * 
	 * @return true if synchronization was performed successfully
	 */
	public boolean synchronize() {
		SntpClient client = new SntpClient();
		if (SystemClock.elapsedRealtime() - getLastSyncTime() >= getNtpSyncFrequency() &&
				SystemClock.elapsedRealtime() - getLastSyncAttempt() >= getNtpRetryFrequency()
				&& client.requestTime(getNtpHost(), getNtpTimeout())) {
			bootTimeMillis = client.getNtpTime() - client.getNtpTimeReference();
			lastSyncTime = SystemClock.elapsedRealtime();
			isSynchronized = true;
			
			Log.d(TAG, "Synchronized time "+currentTimeMillis());
			
			// Update shared preferences 
			toSharedPreferences();
			return true;
		}
		lastSyncAttempt = SystemClock.elapsedRealtime();
		return false;
	}
	
	/**
	 * Update the system properties with the system and real time
	 */
	private void toSharedPreferences() {
		if (isSynchronized) {
			SharedPreferences.Editor editor = getSharedPreferences(AdkintunMobileApp.LIBRARY_SETTINGS, Context.MODE_PRIVATE).edit();
			editor.putLong(REAL_TIMESTAMP_PROPERTY, currentTimeMillis());
			editor.putLong(SYSTEM_TIMESTAMP_PROPERTY, System.currentTimeMillis());
			editor.putLong(LAST_SYNC_PROPERTY, getLastSyncTime());
			editor.commit();
		}
	}
}
