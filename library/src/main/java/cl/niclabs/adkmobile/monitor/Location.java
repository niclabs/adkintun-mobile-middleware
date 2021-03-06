package cl.niclabs.adkmobile.monitor;

import android.content.Context;
import android.content.Intent;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import cl.niclabs.adkmobile.AdkintunMobileApp;
import cl.niclabs.adkmobile.monitor.data.LocationObservation;
import cl.niclabs.adkmobile.monitor.data.Observation;
import cl.niclabs.adkmobile.monitor.data.StateChange;
import cl.niclabs.adkmobile.monitor.data.constants.LocationState;
import cl.niclabs.adkmobile.monitor.data.constants.StateType;
import cl.niclabs.adkmobile.monitor.events.AbstractMonitorEvent;
import cl.niclabs.adkmobile.monitor.events.MonitorEvent;
import cl.niclabs.adkmobile.monitor.listeners.LocationListener;
import cl.niclabs.android.utils.Time;

/**
 * Implements monitoring of GPS and Network Location. Location is notified by
 * the system as a listener. Version adapted from Locations.java in the Aware
 * Framework
 * 
 * It requires permissions android.permission.ACCESS_COARSE_LOCATION to use
 * LOCATION_NETWORK event, and android.permission.ACCESS_FINE_LOCATION to use
 * LOCATION_GPS event
 * 
 * @author nico <nicolas@niclabs.cl>. Created 5-11-2013.
 */
public class Location extends AbstractMonitor<LocationListener> {
	private static LocationManager locationManager = null;

	private static android.location.Location bestLocation = null;
	private android.location.Location lastGps = null;
	private android.location.Location lastNetwork = null;

	protected static LocationObservation getObservationFromLocation(
			android.location.Location loc) {
		LocationObservation locData = new LocationObservation(
				Time.currentTimeMillis());

		locData.setLatitude(loc.getLatitude());
		locData.setLongitude(loc.getLongitude());
		locData.setBearing(loc.getBearing());
		locData.setSpeed(loc.getSpeed());
		locData.setAltitude(loc.getAltitude());
		locData.setProvider(loc.getProvider());
		locData.setAccuracy(loc.getAccuracy());

		return locData;
	}

	/**
	 * Listens to GPS status change events and notifies StateChange if necessary
	 */
	private final GpsStatus.Listener gpsStatusListener = new GpsStatus.Listener() {
		@Override
		public void onGpsStatusChanged(int event) {
			StateChange stateChange;
			switch (event) {
			case GpsStatus.GPS_EVENT_FIRST_FIX:
				break;
			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
				break;
			case GpsStatus.GPS_EVENT_STARTED:
				stateChange = new StateChange(LOCATION_GPS,
						Time.currentTimeMillis());
				stateChange.setStateType(StateType.LOCATION);
				stateChange.setState(LocationState.ENABLED.value());
				notifyListeners(gpsLocationEvent, stateChange);

				if (DEBUG)
					Log.v(TAG, stateChange.toString());
				break;
			case GpsStatus.GPS_EVENT_STOPPED:
				if (bestLocation == null)
					return;

				Observation locationData = getObservationFromLocation(bestLocation);

				/* Notify listeners and update state */
				notifyListeners(gpsLocationEvent, locationData);

				/* Notify state change */
				stateChange = new StateChange(LOCATION_GPS,
						Time.currentTimeMillis());
				stateChange.setStateType(StateType.LOCATION);
				stateChange.setState(LocationState.DISABLED.value());
				notifyListeners(gpsLocationEvent, stateChange);

				/* Log the results */
				if (DEBUG) {
					Log.v(TAG, locationData.toString());
					Log.v(TAG, stateChange.toString());
				}

				break;
			}
		}
	};

	/**
	 * Update interval for GPS, in seconds (default = 180) 0 = realtime updates
	 */
	public static int UPDATE_TIME_GPS = 10;

	/**
	 * Extra for configuring the update interval for gps
	 */
	public static String UPDATE_TIME_GPS_EXTRA = "update_time_gps";

	/**
	 * Update interval for Network, in seconds (default = 300) 0 = realtime
	 * updates
	 */
	public static int UPDATE_TIME_NETWORK = 300;

	/**
	 * Extra for configuring the update interval for network location
	 */
	public static String UPDATE_TIME_NETWORK_EXTRA = "update_time_network";

	/**
	 * Minimum accuracy value acceptable for GPS, in meters (default = 150)
	 */
	public static int UPDATE_DISTANCE_GPS = 150;

	/**
	 * Extra for configuring the update distance for gps
	 */
	public static String UPDATE_DISTANCE_GPS_EXTRA = "update_distance_gps";

	/**
	 * Minimum accuracy value acceptable for Network location, in meters
	 * (default = 1500)
	 */
	public static int UPDATE_DISTANCE_NETWORK = 1500;

	/**
	 * Extra for configuring the update distance for network
	 */
	public static String UPDATE_DISTANCE_NETWORK_EXTRA = "update_distance_network";

	/**
	 * For how long is the last best location considered valid, in seconds (
	 * default = 300 )
	 */
	public static int EXPIRATION_TIME = 300;

	/**
	 * Extra for configuring the update distance for gps
	 */
	public static String EXPIRATION_TIME_EXTRA = "expiration_time";

	/**
	 * Compares two android.location.Location events according to accuracy and
	 * expiration time parameters and returns true if the new location is better
	 * than the previous one.
	 * 
	 * @param newLocation
	 * @param lastLocation
	 * @return
	 */
	private static boolean isBetterLocation(android.location.Location newLocation,
			android.location.Location lastLocation) {
		if (newLocation != null && lastLocation == null) {
			return true;
		}

		if (lastLocation != null && newLocation == null) {
			return false;
		}

		long timeDelta = newLocation.getTime() - lastLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > 1000 * EXPIRATION_TIME;
		boolean isSignificantlyOlder = timeDelta < -(1000 * EXPIRATION_TIME);
		boolean isNewer = timeDelta > 0;

		if (isSignificantlyNewer) {
			return true;
		} else if (isSignificantlyOlder) {
			return false;
		}

		int accuracyDelta = (int) (newLocation.getAccuracy() - lastLocation
				.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;
		boolean isFromSameProvider = newLocation.getProvider().equals(
				lastLocation.getProvider());

		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate
				&& isFromSameProvider) {
			return true;
		}
		return false;
	}

	/**
	 * Listen to location and provider change events
	 */
	private android.location.LocationListener locationListener = new android.location.LocationListener() {

		/**
		 * Get notified on new location
		 */
		@Override
		public void onLocationChanged(android.location.Location newLocation) {

			lastGps = locationManager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			lastNetwork = locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

			if (isBetterLocation(lastNetwork, lastGps)) {
				if (DEBUG)
					Log.d(TAG, "Best location is lastNetwork so far");

				if (isBetterLocation(newLocation, lastNetwork)) {
					bestLocation = newLocation;
					if (DEBUG)
						Log.d(TAG,
								"Changed from lastNetwork to new location as it is better");
				} else {
					bestLocation = lastNetwork;
					if (DEBUG)
						Log.d(TAG, "Kept lastNetwork as it is better");
				}
			} else {
				if (DEBUG)
					Log.d(TAG, "Best location is lastGps so far");

				if (isBetterLocation(newLocation, lastGps)) {
					bestLocation = newLocation;
					if (DEBUG)
						Log.d(TAG,
								"Changed from lastGps to new location as it is better");
				} else {
					bestLocation = lastGps;
					if (DEBUG)
						Log.d(TAG, "Kept lastGps as it is better");
				}
			}

			Observation observation = getObservationFromLocation(bestLocation);

			if (bestLocation.getProvider().equals(LocationManager.GPS_PROVIDER)) {
				notifyListeners(gpsLocationEvent, observation);
			} else {
				notifyListeners(networkLocationEvent, observation);
			}

			if (DEBUG)
				Log.v(TAG, observation.toString());
		}

		/**
		 * Get notified on user de-activation of a location provider
		 */
		@Override
		public void onProviderDisabled(String provider) {
			StateChange stateChange;
			if (provider.equals(LocationManager.GPS_PROVIDER)) {
				if (DEBUG)
					Log.d(TAG, "Gps location provider is disabled");

				/* Notify state change */
				stateChange = new StateChange(LOCATION_GPS,
						Time.currentTimeMillis());
				stateChange.setState(LocationState.DISABLED.value());
				stateChange.setStateType(StateType.LOCATION);
				notifyListeners(gpsLocationEvent, stateChange);
				if (DEBUG)
					Log.v(TAG, stateChange.toString());
			} else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
				if (DEBUG)
					Log.d(TAG, "Network location provider is disabled");

				/* Notify state change */
				stateChange = new StateChange(LOCATION_NETWORK,
						Time.currentTimeMillis());
				stateChange.setState(LocationState.DISABLED.value());
				stateChange.setStateType(StateType.LOCATION);
				notifyListeners(networkLocationEvent, stateChange);
				if (DEBUG)
					Log.v(TAG, stateChange.toString());
			}
		}

		/**
		 * Get notified on user activation of a location provider
		 */
		@Override
		public void onProviderEnabled(String provider) {
			StateChange stateChange;
			if (provider.equals(LocationManager.GPS_PROVIDER)) {
				if (DEBUG)
					Log.d(TAG, "Gps location provider is enabled");

				/* Notify state change */
				stateChange = new StateChange(LOCATION_GPS,
						Time.currentTimeMillis());
				stateChange.setState(LocationState.ENABLED.value());
				stateChange.setStateType(StateType.LOCATION);
				notifyListeners(gpsLocationEvent, stateChange);
				if (DEBUG)
					Log.v(TAG, stateChange.toString());
			} else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
				if (DEBUG)
					Log.d(TAG, "Network location provider is enabled");

				/* Notify state change */
				stateChange = new StateChange(LOCATION_NETWORK,
						Time.currentTimeMillis());
				stateChange.setState(LocationState.ENABLED.value());
				stateChange.setStateType(StateType.LOCATION);
				notifyListeners(networkLocationEvent, stateChange);
				if (DEBUG)
					Log.v(TAG, stateChange.toString());
			}
		}

		/**
		 * Get notified on a provided information status change
		 */
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			if (status == LocationProvider.AVAILABLE) {
				// Save best location, could be GPS or network
				// This covers the case when the GPS stopped and we did not get
				// a location fix.
				if (bestLocation == null)
					return;

				Observation locationData = getObservationFromLocation(bestLocation);
				if (provider.equals(LocationManager.GPS_PROVIDER)) {
					notifyListeners(gpsLocationEvent, locationData);
				} else {
					notifyListeners(networkLocationEvent, locationData);
				}

			}
		}
	};

	private final IBinder serviceBinder = new ServiceBinder<Location>(this);

	protected String TAG = "AdkintunMobile::Location";

	/**
	 * Control gps location events
	 */
	private MonitorEvent<LocationListener> gpsLocationEvent = new AbstractMonitorEvent<LocationListener>() {

		@Override
		public synchronized boolean activate() {
			if (!isActive()) {
				locationManager.addGpsStatusListener(gpsStatusListener);
				locationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER, UPDATE_TIME_GPS * 1000,
						UPDATE_DISTANCE_GPS, locationListener);

				Log.d(TAG, "Location GPS service has been activated");

				super.activate();
			}
			return true;
		}

		@Override
		public synchronized void deactivate() {
			if (isActive()) {
				super.deactivate();

				locationManager.removeUpdates(locationListener);
				locationManager.removeGpsStatusListener(gpsStatusListener);

				if (DEBUG)
					Log.d(TAG, "Location GPS service terminated...");
			}
		}

		@Override
		public void onDataReceived(LocationListener listener, Observation result) {
			if (result instanceof LocationObservation) {
				listener.onLocationChanged((LocationObservation) result);
			} else if (result instanceof StateChange) {
				listener.onLocationStateChanged((StateChange) result);
			}
		}

	};

	/**
	 * Control network location events
	 */
	private MonitorEvent<LocationListener> networkLocationEvent = new AbstractMonitorEvent<LocationListener>() {

		@Override
		public synchronized boolean activate() {
			if (!isActive()) {
				super.activate();

				locationManager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER,
						UPDATE_TIME_NETWORK * 1000, UPDATE_DISTANCE_NETWORK,
						locationListener);
				if (DEBUG)
					Log.d(TAG, "Location tracking with Network is active");
			}
			return true;
		}

		@Override
		public synchronized void deactivate() {
			if (isActive()) {
				super.deactivate();

				locationManager.removeUpdates(locationListener);

				if (DEBUG)
					Log.d(TAG, "Locations network service terminated...");
			}
		}

		@Override
		public void onDataReceived(LocationListener listener, Observation result) {
			if (result instanceof LocationObservation) {
				listener.onLocationChanged((LocationObservation) result);
			} else if (result instanceof StateChange) {
				listener.onLocationStateChanged((StateChange) result);
			}
		}

	};

	@Override
	public void activate(int events, Bundle configuration) {
		EXPIRATION_TIME = configuration.getInt(EXPIRATION_TIME_EXTRA,
				EXPIRATION_TIME);

		if ((events & LOCATION_GPS) == LOCATION_GPS) {
			UPDATE_TIME_GPS = configuration.getInt(UPDATE_TIME_GPS_EXTRA,
					UPDATE_TIME_GPS);
			UPDATE_DISTANCE_GPS = configuration.getInt(
					UPDATE_DISTANCE_GPS_EXTRA, UPDATE_DISTANCE_GPS);

			activate(gpsLocationEvent);
		}
		if ((events & LOCATION_NETWORK) == LOCATION_NETWORK) {
			UPDATE_TIME_NETWORK = configuration.getInt(
					UPDATE_TIME_NETWORK_EXTRA, UPDATE_TIME_NETWORK);
			UPDATE_DISTANCE_NETWORK = configuration.getInt(
					UPDATE_DISTANCE_NETWORK_EXTRA, UPDATE_DISTANCE_NETWORK);

			activate(networkLocationEvent);
		}
	}

	@Override
	public void deactivate(int events) {
		if ((events & LOCATION_GPS) == LOCATION_GPS) {
			deactivate(gpsLocationEvent);
		}
		if ((events & LOCATION_NETWORK) == LOCATION_NETWORK) {
			deactivate(networkLocationEvent);
		}
	}

	/**
	 * Return true if high accuracy (GPS + Network) location is enabled on the
	 * device
	 * 
	 * @param context
	 *            android context to perform the request
	 * @return
	 */
	public static boolean isHighAccuracyEnabled(Context context) {
		LocationManager locationManager = (LocationManager) context
				.getSystemService(LOCATION_SERVICE);
		return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
				&& locationManager
						.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
	}

	/**
	 * Return true if location is enabled on the device
	 * 
	 * @param context
	 *            android context to perform the request
	 * @return
	 */
	public static boolean isEnabled(Context context) {
		LocationManager locationManager = (LocationManager) context
				.getSystemService(LOCATION_SERVICE);
		return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
				|| locationManager
						.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
	}

	public static LocationObservation getLastLocation() {
		if (bestLocation != null) {
			return getObservationFromLocation(bestLocation);
		}
		
		LocationManager lm = locationManager;
		if (lm == null && AdkintunMobileApp.getContext() != null) {
			lm = (LocationManager) AdkintunMobileApp.getContext().getSystemService(LOCATION_SERVICE);
		}
	
		if (lm != null) {
			android.location.Location lastNetwork = lm
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			android.location.Location lastGps = lm
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (lastGps != null && isBetterLocation(lastGps, lastNetwork)) {
				return getObservationFromLocation(lastGps);
			}
			if (lastNetwork != null) return getObservationFromLocation(lastNetwork);
		}
		return null;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return serviceBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
	}

}
