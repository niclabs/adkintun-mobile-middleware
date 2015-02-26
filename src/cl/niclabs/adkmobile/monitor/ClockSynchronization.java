package cl.niclabs.adkmobile.monitor;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import cl.niclabs.adkmobile.monitor.data.ClockSynchronizationState;
import cl.niclabs.adkmobile.monitor.data.Observation;
import cl.niclabs.adkmobile.monitor.data.constants.ClockState;
import cl.niclabs.adkmobile.monitor.events.AbstractMonitorEvent;
import cl.niclabs.adkmobile.monitor.events.MonitorEvent;
import cl.niclabs.adkmobile.monitor.listeners.ClockSynchronizationListener;
import cl.niclabs.adkmobile.monitor.listeners.ConnectivityListener;
import cl.niclabs.adkmobile.monitor.proxies.ConnectivityStatus;
import cl.niclabs.adkmobile.monitor.proxies.ConnectivityStatusListener;
import cl.niclabs.adkmobile.utils.Clock;

/**
 * Service to monitor and maintain synchronization status of the library clock
 * 
 * The service listens to the Connectivity monitor and updates the Clock service using an
 * whenever the connection becomes available (when it was unavailable
 * before) or at the start of the application
 * 
 * 
 * In order to run the service each time target application is loaded, it is
 * necessary to add the Connectivity, ClockSynchronization and Clock services to the manifest of the
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
 * 		<service android:name="cl.niclabs.adkmobile.utils.Clock" />
 * 		<service android:name="cl.niclabs.adkmobile.monitor.ClockSynchronization" />
 * </application>
 * </code>
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 */
public class ClockSynchronization extends AbstractMonitor<ClockSynchronizationListener> implements ConnectivityStatusListener {
	
	public static final String NTP_HOST_EXTRA = "cl.niclabs.clock_monitor.ntp_host";
	
	public static final String NTP_RETRY_FREQUENCY_EXTRA = "cl.niclabs.clock_monitor.ntp_retry_frequency";
	public static final String NTP_SYNC_FREQUENCY_EXTRA = "cl.niclabs.clock_monitor.ntp_sync_frequency";
	public static final String NTP_TIMEOUT_EXTRA = "cl.niclabs.clock_monitor.ntp_timeout";
	public static final String TAG = "AdkintunMobile::ClockSynchronization";
	
	private Clock clock;
	/**
	 * Accelerometer event
	 */
	private MonitorEvent<ClockSynchronizationListener> clockEvent = new AbstractMonitorEvent<ClockSynchronizationListener>() {
		@Override
		public synchronized boolean activate() {
			if (!isActive()) {		
				// Bind to the clock
				Intent intent = new Intent(context, Clock.class);
				if (bindService(intent, clockServiceConnection, Context.BIND_AUTO_CREATE)) {
					/* Listen to connectivity events */
					connectivity = Connectivity.bind(Connectivity.class, context);

					// Create connectivity status proxy
					connectivityProxy = ConnectivityStatus.getInstance();
					connectivity.listen(connectivityProxy, true);

					// Listen this to the proxy events
					connectivityProxy.listen(context, true);
					
					if(DEBUG) Log.d(TAG, "Clock Synchronization service has been activated");
					
					// Activate the event
					super.activate();
				}
			}
			return true;
		}

		@Override
		public synchronized void deactivate() {
			if (isActive()) {
				// Unbind from the service
				unbindService(clockServiceConnection);
				
				/* Remove the proxy as listener for the connectivity service */
				connectivity.listen(connectivityProxy, false);
				
				/* Stop listening to proxy events */
				connectivityProxy.listen(context, false);
				
				/* Unbind from connectivity */
				connectivity.unbind();
				
				clock = null;

				// Unregister the listener
				if (DEBUG) Log.d(TAG, "Clock synchronization service has been deactivated");
				super.deactivate();
			}
		}
		
		@Override
		public void onDataReceived(ClockSynchronizationListener listener, Observation result) {
			/* Notify result */
			listener.onClockStateChange((ClockSynchronizationState) result);
		}
	};
	
	// Service connection to the Clock
	private ServiceConnection clockServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Clock.ServiceBinder binder = (Clock.ServiceBinder)service;
			clock = binder.getService();
			
			// Configure the clock
			clock.setNtpHost(ntpHost);
			clock.setNtpTimeout(ntpTimeout);
			clock.setNtpSyncFrequency(ntpSyncFrequency);
			clock.setNtpRetryFrequency(ntpRetryFrequency);
			
			if (!clock.hasSynchronizedInLastDay()) {
				ClockSynchronizationState state = new ClockSynchronizationState(System.currentTimeMillis());
				state.setRealTime(Clock.currentTimeMillis());
				state.setState(ClockState.UNSYNCHRONIZED);
				
				Log.v(TAG, state.toString());
				
				// Notify the listeners 
				notifyListeners(clockEvent, state);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			clock = null;
		}
	};
	private Controller<ConnectivityListener> connectivity;
	
	private ConnectivityStatus connectivityProxy;
	
	
	private ClockSynchronization context = this;
	private String ntpHost = Clock.NTP_HOST;
	private int ntpRetryFrequency = Clock.NTP_RETRY_FREQUENCY;
	private int ntpSyncFrequency = Clock.NTP_SYNC_FREQUENCY;
	
	private int ntpTimeout = Clock.NTP_TIMEOUT;
	
	/**
	 * Activity-Service binder
	 */
	private final IBinder serviceBinder = new ServiceBinder<ClockSynchronization>(this);

	@Override
	public void activate(int events, Bundle configuration) {
		if ((events & CLOCK) == CLOCK) {
			// Configure the monitor
			if ((ntpHost = configuration.getString(NTP_HOST_EXTRA)) == null) 
				ntpHost = Clock.NTP_HOST;
			
			ntpTimeout = configuration.getInt(NTP_TIMEOUT_EXTRA, ntpTimeout);
			ntpSyncFrequency = configuration.getInt(NTP_SYNC_FREQUENCY_EXTRA, ntpSyncFrequency);
			ntpRetryFrequency = configuration.getInt(NTP_RETRY_FREQUENCY_EXTRA, ntpRetryFrequency);
			
			activate(clockEvent);
		}
	}

	@Override
	public void deactivate(int events) {
		if ((events & CLOCK) == CLOCK) {
			deactivate(clockEvent);
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return serviceBinder;
	}

	@Override
	public void onMobileConnection(boolean roaming) {
	}

	@Override
	public void onNetworkConnection(boolean isMobileRoaming) {
		if (!isMobileRoaming && clock.synchronize()) {
			ClockSynchronizationState state = new ClockSynchronizationState(System.currentTimeMillis());
			state.setRealTime(Clock.currentTimeMillis());
			state.setState(ClockState.SYNCHRONIZED);
			
			Log.v(TAG, state.toString());
			
			// Notify the listeners 
			notifyListeners(clockEvent, state);
		}
	}

	@Override
	public void onNetworkDisconnection() {
	}

	@Override
	public void onRoaming(boolean dataRoamingEnabled) {
	}

	@Override
	public void onWifiConnection() {
	}

	
}
