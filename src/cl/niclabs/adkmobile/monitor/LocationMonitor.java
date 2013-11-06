package cl.niclabs.adkmobile.monitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import cl.niclabs.adkmobile.data.ContentValuesDataObject;
import cl.niclabs.adkmobile.data.DataFields;
import cl.niclabs.adkmobile.data.DataObject;
import cl.niclabs.adkmobile.monitor.events.AbstractMonitorEvent;
import cl.niclabs.adkmobile.monitor.events.MonitorEvent;
import cl.niclabs.adkmobile.monitor.listeners.LocationsListener;

/**
 * Implements monitoring of GPS and Network Location. Location is notified by
 * the system as a listener. Version adapted from Locations in the Aware
 * Framework
 * 
 * @author nico <nicolas@niclabs.cl>. Created 5-11-2013.
 */
public class LocationMonitor extends AbstractMonitor<LocationsListener> {

	/**
	 * Broadcasted event: New location available
	 */
	public static final String ACTION_NEW_LOCATION = "ACTION_NEW_LOCATION";

	/**
	 * Broadcasted event: GPS location is active
	 */
	public static final String ACTION_GPS_LOCATION_ENABLED = "ACTION_GPS_LOCATION_ENABLED";

	/**
	 * Broadcasted event: Network location is active
	 */
	public static final String ACTION_NETWORK_LOCATION_ENABLED = "ACTION_NETWORK_LOCATION_ENABLED";

	/**
	 * Broadcasted event: GPS location disabled
	 */
	public static final String ACTION_GPS_LOCATION_DISABLED = "ACTION_GPS_LOCATION_DISABLED";

	/**
	 * Broadcasted event: Network location disabled
	 */
	public static final String ACTION_NETWORK_LOCATION_DISABLED = "ACTION_NETWORK_LOCATION_DISABLED";

	public static class LocationData implements DataFields {
		public static final String LATITUDE = "LATITUDE";
		public static final String LONGITUDE = "LONGITUDE";
		public static final String BEARING = "BEARING";
		public static final String SPEED = "SPEED";
		public static final String ALTITUDE = "ALTITUDE";
		public static final String PROVIDER = "PROVIDER";
		public static final String ACCURACY = "ACCURACY";
	}

	private static LocationManager locationManager = null;
	private static Location bestLocation = null;
	private static Location lastGPS = null;
	private static Location lastNetwork = null;

	/**
	 * This listener will keep track for failed GPS location requests
	 */
	private final GpsStatus.Listener gps_status_listener = new GpsStatus.Listener() {
		@Override
		public void onGpsStatusChanged(int event) {
			switch (event) {
			case GpsStatus.GPS_EVENT_FIRST_FIX:
				break;
			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
				// TODO: extend to log satellite information
				break;
			case GpsStatus.GPS_EVENT_STARTED:
				break;
			case GpsStatus.GPS_EVENT_STOPPED:
				// Save best location, could be GPS or network
				// This covers the case when the GPS stopped and we did not get
				// a location fix.

				if (bestLocation == null)
					break;

				DataObject locationData = new ContentValuesDataObject();

				locationData.put(LocationData.EVENT_TYPE, LOCATION);
				locationData.put(LocationData.TIMESTAMP,
						System.currentTimeMillis());
				locationData.put(LocationData.LATITUDE,
						bestLocation.getLatitude());
				locationData.put(LocationData.LONGITUDE,
						bestLocation.getLongitude());
				locationData.put(LocationData.BEARING,
						bestLocation.getBearing());
				locationData.put(LocationData.SPEED, bestLocation.getSpeed());
				locationData.put(LocationData.ALTITUDE,
						bestLocation.getAltitude());
				locationData.put(LocationData.PROVIDER,
						bestLocation.getProvider());
				locationData.put(LocationData.ACCURACY,
						bestLocation.getAccuracy());

				/* Notify listeners and update state */
				notifyListeners(locationEvent, locationData);

				/* Log the results */
				if (DEBUG)
					Log.d(TAG, "GPS_STATUS_LISTENER");
				break;
			}
		}
	};

	/**
	 * Update interval for GPS, in seconds (default = 180) 0 = realtime updates
	 */
	public static int UPDATE_TIME_GPS = 180;

	/**
	 * Update interval for Network, in seconds (default = 300) 0 = realtime
	 * updates
	 */
	public static int UPDATE_TIME_NETWORK = 300;

	/**
	 * Minimum accuracy value acceptable for GPS, in meters (default = 150)
	 */
	public static int UPDATE_DISTANCE_GPS = 150;

	/**
	 * Minimum accuracy value acceptable for Network location, in meters
	 * (default = 1500)
	 */
	public static int UPDATE_DISTANCE_NETWORK = 1500;

	/**
	 * For how long is the last best location considered valid, in seconds (
	 * default = 300 )
	 */
	public static int EXPIRATION_TIME = 300;

	private boolean isBetterLocation(Location newLocation, Location lastLocation) {
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

	private LocationListener locationListener = new LocationListener() {

		@Override
		public void onLocationChanged(Location newLocation) {

			lastGPS = locationManager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			lastNetwork = locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

			if (isBetterLocation(lastNetwork, lastGPS)) {
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
					Log.d(TAG, "Best location is lastGPS so far");

				if (isBetterLocation(newLocation, lastGPS)) {
					bestLocation = newLocation;
					if (DEBUG)
						Log.d(TAG,
								"Changed from lastGPS to new location as it is better");
				} else {
					bestLocation = lastGPS;
					if (DEBUG)
						Log.d(TAG, "Kept lastGPS as it is better");
				}
			}

			Intent locationEvent = new Intent(ACTION_NEW_LOCATION);
			sendBroadcast(locationEvent);
		}

		/**
		 * Determines whether one Location reading is better than the current
		 * Location fix
		 * 
		 * @param newLocation
		 *            The new Location that you want to evaluate
		 * @param lastLocation
		 *            The last location fix, to which you want to compare the
		 *            new one
		 */
		@Override
		public void onProviderDisabled(String provider) {
			if (provider.equals(LocationManager.GPS_PROVIDER)) {
				Intent gps = new Intent(ACTION_GPS_LOCATION_DISABLED);
				sendBroadcast(gps);
			}
			if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
				Intent network = new Intent(ACTION_NETWORK_LOCATION_DISABLED);
				sendBroadcast(network);
			}
		}

		@Override
		public void onProviderEnabled(String provider) {
			if (provider.equals(LocationManager.GPS_PROVIDER)) {
				Intent gps = new Intent(ACTION_GPS_LOCATION_ENABLED);
				sendBroadcast(gps);
			}

			if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
				Intent network = new Intent(ACTION_NETWORK_LOCATION_ENABLED);
				sendBroadcast(network);
			}
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			if (DEBUG)
				Log.d(TAG, "onStatusChanged: " + provider + " Status:" + status
						+ " Extras:" + extras.toString());

			if (status == LocationProvider.AVAILABLE) {
				Intent locationEvent = new Intent(ACTION_NEW_LOCATION);
				sendBroadcast(locationEvent);
			}

		}

	};

	private BroadcastReceiver locationMonitor = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equalsIgnoreCase(
					LocationMonitor.ACTION_NEW_LOCATION)) {
				if (bestLocation == null)
					return;

				// assign the values to ContentValues variables
				DataObject locationData = new ContentValuesDataObject();

				locationData.put(LocationData.EVENT_TYPE, LOCATION);
				locationData.put(LocationData.TIMESTAMP,
						System.currentTimeMillis());
				locationData.put(LocationData.LATITUDE,
						bestLocation.getLatitude());
				locationData.put(LocationData.LONGITUDE,
						bestLocation.getLongitude());
				locationData.put(LocationData.BEARING,
						bestLocation.getBearing());
				locationData.put(LocationData.SPEED, bestLocation.getSpeed());
				locationData.put(LocationData.ALTITUDE,
						bestLocation.getAltitude());
				locationData.put(LocationData.PROVIDER,
						bestLocation.getProvider());
				locationData.put(LocationData.ACCURACY,
						bestLocation.getAccuracy());

				/* Notify listeners and update state */
				notifyListeners(locationEvent, locationData);
				if (DEBUG)
					Log.d(TAG, locationData.getString(LocationData.PROVIDER));
			}

			if (intent.getAction().equalsIgnoreCase(
					LocationMonitor.ACTION_GPS_LOCATION_ENABLED)) {
				if (DEBUG)
					Log.d(TAG, ACTION_GPS_LOCATION_ENABLED);
			}
			if (intent.getAction().equalsIgnoreCase(
					LocationMonitor.ACTION_GPS_LOCATION_DISABLED)) {
				if (DEBUG)
					Log.d(TAG, ACTION_GPS_LOCATION_DISABLED);
			}
			if (intent.getAction().equalsIgnoreCase(
					LocationMonitor.ACTION_NETWORK_LOCATION_ENABLED)) {
				if (DEBUG)
					Log.d(TAG, ACTION_GPS_LOCATION_ENABLED);
			}
			if (intent.getAction().equalsIgnoreCase(
					LocationMonitor.ACTION_NETWORK_LOCATION_DISABLED)) {
				if (DEBUG)
					Log.d(TAG, ACTION_GPS_LOCATION_DISABLED);
			}
		}
	};

	private final IBinder serviceBinder = new ServiceBinder<LocationMonitor>(
			this);

	protected String TAG = "AdkintunMobile::Location";

	private MonitorEvent<LocationsListener> locationEvent = new AbstractMonitorEvent<LocationsListener>() {

		@Override
		public synchronized boolean activate() {
			if (!isActive()) {
				super.activate();

				locationManager.addGpsStatusListener(gps_status_listener);

				locationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER, UPDATE_TIME_GPS * 1000,
						UPDATE_DISTANCE_GPS, locationListener);
				if (DEBUG)
					Log.d(TAG, "Location tracking with GPS is active");

				locationManager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER,
						UPDATE_TIME_NETWORK * 1000, UPDATE_DISTANCE_NETWORK,
						locationListener);
				if (DEBUG)
					Log.d(TAG, "Location tracking with Network is active");

				IntentFilter filter = new IntentFilter();
				filter.addAction(LocationMonitor.ACTION_NEW_LOCATION);
				filter.addAction(LocationMonitor.ACTION_GPS_LOCATION_ENABLED);
				filter.addAction(LocationMonitor.ACTION_GPS_LOCATION_DISABLED);
				filter.addAction(LocationMonitor.ACTION_NETWORK_LOCATION_ENABLED);
				filter.addAction(LocationMonitor.ACTION_NETWORK_LOCATION_DISABLED);
				registerReceiver(locationMonitor, filter);

				Log.d(TAG, "Location service has been activated");
			}
			return true;
		}

		@Override
		public synchronized void deactivate() {
			if (isActive()) {
				super.deactivate();

				locationManager.removeUpdates(locationListener);
				locationManager.removeGpsStatusListener(gps_status_listener);

				if (DEBUG)
					Log.d(TAG, "Locations service terminated...");

				unregisterReceiver(locationMonitor);
				Log.d(TAG, "Location service has been deactivated");
			}
		}

		@Override
		public void onDataReceived(LocationsListener listener, DataObject result) {
			listener.onLocationChanged(result);
		}

	};

	@Override
	public boolean activate(int events, Bundle configuration) {
		if ((events & LOCATION) == LOCATION) {
			return activate(locationEvent);
		}
		return false;
	}

	@Override
	public void deactivate(int events) {
		if ((events & LOCATION) == LOCATION) {
			deactivate(locationEvent);
		}
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
