package cl.niclabs.adkmobile.monitor;

import java.util.List;

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
 * the system as a listener. Version adapted from Locations.java in the Aware
 * Framework
 * 
 * @author nico <nicolas@niclabs.cl>. Created 5-11-2013.
 */
public class Locations extends AbstractMonitor<LocationsListener> {

	/**
	 * Broadcasted event: New location available
	 */
	public static final String ACTION_LOCATION_UPDATE = "ACTION_LOCATION_UPDATE";

	/**
	 * Broadcasted event: New gps location available
	 */
	public static final String ACTION_GPS_LOCATION_UPDATE = "ACTION_GPS_LOCATION_UPDATE";

	/**
	 * Broadcasted event: New network location available
	 */
	public static final String ACTION_NETWORK_LOCATION_UPDATE = "ACTION_NETWORK_LOCATION_UPDATE";

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
	private static Location gpsLocation = null;
	private static Location networkLocation = null;

	private DataObject getLocationDataObject(Location loc) {
		DataObject locData = new ContentValuesDataObject();

		locData.put(LocationData.EVENT_TYPE, LOCATION);
		locData.put(LocationData.TIMESTAMP, System.currentTimeMillis());
		locData.put(LocationData.LATITUDE, loc.getLatitude());
		locData.put(LocationData.LONGITUDE, loc.getLongitude());
		locData.put(LocationData.BEARING, loc.getBearing());
		locData.put(LocationData.SPEED, loc.getSpeed());
		locData.put(LocationData.ALTITUDE, loc.getAltitude());
		locData.put(LocationData.PROVIDER, loc.getProvider());
		locData.put(LocationData.ACCURACY, loc.getAccuracy());

		return locData;
	}
	
	private String locDOtoString(DataObject loc){
		StringBuilder sb = new StringBuilder();
		
		sb.append("{");
		sb.append(loc.get(LocationData.EVENT_TYPE));
		sb.append(", ");
		sb.append(loc.get(LocationData.TIMESTAMP));
		sb.append(", ");
		sb.append(loc.get(LocationData.LATITUDE));
		sb.append(", ");
		sb.append(loc.get(LocationData.LONGITUDE));
		sb.append(", ");
		sb.append(loc.get(LocationData.BEARING));
		sb.append(", ");
		sb.append(loc.get(LocationData.SPEED));
		sb.append(", ");
		sb.append(loc.get(LocationData.ALTITUDE));
		sb.append(", ");
		sb.append(loc.get(LocationData.PROVIDER));
		sb.append(", ");
		sb.append(loc.get(LocationData.ACCURACY));
		sb.append('}');
		
		return sb.toString();
	}

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
				// extend to log satellite information
				break;
			case GpsStatus.GPS_EVENT_STARTED:
				if (DEBUG)
					Log.d(TAG, "GPS_STATUS_STARTED");
				break;
			case GpsStatus.GPS_EVENT_STOPPED:
				if (gpsLocation == null)
					return;

				DataObject locationData = getLocationDataObject(gpsLocation);

				/* Notify listeners and update state */
				notifyListeners(gpsLocationEvent, locationData);

				/* Log the results */
				if (DEBUG)
					Log.d(TAG, "GPS_STATUS_STOPPED");
				if (DEBUG)
					Log.d(TAG, locDOtoString(locationData));
				
				break;
			}
		}
	};

	/**
	 * Update interval for GPS, in seconds (default = 180) 0 = realtime updates
	 */
	public static int UPDATE_TIME_GPS = 10;

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

	public boolean isBetterLocation(Location newLocation, Location lastLocation) {
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

	private LocationListener gpsLocationListener = new LocationListener() {

		@Override
		public void onLocationChanged(Location newLocation) {

			gpsLocation = locationManager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);

			Intent locationEvent = new Intent(ACTION_GPS_LOCATION_UPDATE);
			sendBroadcast(locationEvent);
		}

		@Override
		public void onProviderDisabled(String provider) {
			if (provider.equals(LocationManager.GPS_PROVIDER)) {
				Intent gps = new Intent(ACTION_GPS_LOCATION_DISABLED);
				sendBroadcast(gps);
			}
		}

		@Override
		public void onProviderEnabled(String provider) {
			if (provider.equals(LocationManager.GPS_PROVIDER)) {
				Intent gps = new Intent(ACTION_GPS_LOCATION_ENABLED);
				sendBroadcast(gps);
			}
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			if (DEBUG)
				Log.d(TAG, "onStatusChanged: " + provider + " Status:" + status
						+ " Extras:" + extras.toString());

			if (status == LocationProvider.AVAILABLE
					&& LocationManager.GPS_PROVIDER.equals(provider)) {
				Intent locationEvent = new Intent(ACTION_GPS_LOCATION_UPDATE);
				sendBroadcast(locationEvent);
			}

		}
	};

	private LocationListener networkLocationListener = new LocationListener() {

		@Override
		public void onLocationChanged(Location newLocation) {

			networkLocation = locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

			Intent locationEvent = new Intent(ACTION_NETWORK_LOCATION_UPDATE);
			sendBroadcast(locationEvent);
		}

		@Override
		public void onProviderDisabled(String provider) {
			if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
				Intent network = new Intent(ACTION_NETWORK_LOCATION_DISABLED);
				sendBroadcast(network);
			}
		}

		@Override
		public void onProviderEnabled(String provider) {
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

			if (status == LocationProvider.AVAILABLE
					&& LocationManager.NETWORK_PROVIDER.equals(provider)) {
				Intent locationEvent = new Intent(
						ACTION_NETWORK_LOCATION_UPDATE);
				sendBroadcast(locationEvent);
			}
		}
	};

	private BroadcastReceiver gpsLocationMonitor = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equalsIgnoreCase(
					Locations.ACTION_GPS_LOCATION_UPDATE)) {

				// assign the values to ContentValues variables
				DataObject locationData = getLocationDataObject(gpsLocation);

				/* Notify listeners and update state */
				notifyListeners(gpsLocationEvent, locationData);
				if (DEBUG)
					Log.d(TAG, locDOtoString(locationData));
			}

			if (intent.getAction().equalsIgnoreCase(
					Locations.ACTION_GPS_LOCATION_ENABLED)) {
				if (DEBUG)
					Log.d(TAG, ACTION_GPS_LOCATION_ENABLED);
			}
			if (intent.getAction().equalsIgnoreCase(
					Locations.ACTION_GPS_LOCATION_DISABLED)) {
				if (DEBUG)
					Log.d(TAG, ACTION_GPS_LOCATION_DISABLED);
			}
		}
	};

	private BroadcastReceiver networkLocationMonitor = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equalsIgnoreCase(
					Locations.ACTION_NETWORK_LOCATION_UPDATE)) {

				// assign the values to ContentValues variables
				DataObject locationData = getLocationDataObject(networkLocation);

				/* Notify listeners and update state */
				notifyListeners(networkLocationEvent, locationData);
				if (DEBUG)
					Log.d(TAG, locDOtoString(locationData));
			}
			if (intent.getAction().equalsIgnoreCase(
					Locations.ACTION_NETWORK_LOCATION_ENABLED)) {
				if (DEBUG)
					Log.d(TAG, ACTION_GPS_LOCATION_ENABLED);
			}
			if (intent.getAction().equalsIgnoreCase(
					Locations.ACTION_NETWORK_LOCATION_DISABLED)) {
				if (DEBUG)
					Log.d(TAG, ACTION_GPS_LOCATION_DISABLED);
			}
		}
	};

	private final IBinder serviceBinder = new ServiceBinder<Locations>(
			this);

	protected String TAG = "AdkintunMobile::Location";

	private MonitorEvent<LocationsListener> gpsLocationEvent = new AbstractMonitorEvent<LocationsListener>() {

		@Override
		public synchronized boolean activate() {
			if (!isActive()) {
				List<String> providers = locationManager.getProviders(true);

				if (!providers.contains(LocationManager.GPS_PROVIDER)) {
					if (DEBUG)
						Log.d(TAG, "Location tracking with GPS is unavailable");
					return false;
				}
				
				super.activate();

				locationManager.addGpsStatusListener(gps_status_listener);

				locationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER, UPDATE_TIME_GPS * 1000,
						UPDATE_DISTANCE_GPS, gpsLocationListener);
				if (DEBUG)
					Log.d(TAG, "Location tracking with GPS is active");

				IntentFilter filter = new IntentFilter();
				filter.addAction(Locations.ACTION_GPS_LOCATION_UPDATE);
				filter.addAction(Locations.ACTION_GPS_LOCATION_ENABLED);
				filter.addAction(Locations.ACTION_GPS_LOCATION_DISABLED);
				registerReceiver(gpsLocationMonitor, filter);

				Log.d(TAG, "Location gps service has been activated");
			}
			return true;
		}

		@Override
		public synchronized void deactivate() {
			if (isActive()) {
				super.deactivate();

				locationManager.removeUpdates(gpsLocationListener);
				locationManager.removeGpsStatusListener(gps_status_listener);

				if (DEBUG)
					Log.d(TAG, "Locations gps service terminated...");

				unregisterReceiver(gpsLocationMonitor);
				Log.d(TAG, "Location gps service has been deactivated");
			}
		}

		@Override
		public void onDataReceived(LocationsListener listener, DataObject result) {
			listener.onGPSLocationChanged(result);
		}

	};

	private MonitorEvent<LocationsListener> networkLocationEvent = new AbstractMonitorEvent<LocationsListener>() {

		@Override
		public synchronized boolean activate() {
			if (!isActive()) {
				List<String> providers = locationManager.getProviders(true);

				if (!providers.contains(LocationManager.NETWORK_PROVIDER)) {
					if (DEBUG)
						Log.d(TAG, "Location tracking with Network is unavailable");
					return false;
				}

				super.activate();

				locationManager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER,
						UPDATE_TIME_NETWORK * 1000, UPDATE_DISTANCE_NETWORK,
						networkLocationListener);
				if (DEBUG)
					Log.d(TAG, "Location tracking with Network is active");

				IntentFilter filter = new IntentFilter();
				filter.addAction(Locations.ACTION_NETWORK_LOCATION_UPDATE);
				filter.addAction(Locations.ACTION_NETWORK_LOCATION_ENABLED);
				filter.addAction(Locations.ACTION_NETWORK_LOCATION_DISABLED);
				registerReceiver(networkLocationMonitor, filter);

				Log.d(TAG, "Location network service has been activated");
			}
			return true;
		}

		@Override
		public synchronized void deactivate() {
			if (isActive()) {
				super.deactivate();

				locationManager.removeUpdates(networkLocationListener);
				locationManager.removeGpsStatusListener(gps_status_listener);

				if (DEBUG)
					Log.d(TAG, "Locations network service terminated...");

				unregisterReceiver(networkLocationMonitor);
				Log.d(TAG, "Location network service has been deactivated");
			}
		}

		@Override
		public void onDataReceived(LocationsListener listener, DataObject result) {
			listener.onNetworkLocationChanged(result);
		}

	};

	@Override
	public boolean activate(int events, Bundle configuration) {
		if ((events & LOCATION) == LOCATION) {
			return activate(gpsLocationEvent) && activate(networkLocationEvent);
		}
		if ((events & LOCATION_GPS) == LOCATION_GPS) {
			return activate(gpsLocationEvent);
		}
		if ((events & LOCATION_NETWORK) == LOCATION_NETWORK) {
			return activate(networkLocationEvent);
		}
		return false;
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

	@Override
	public IBinder onBind(Intent intent) {
//		super.onBind(intent);
		if(DEBUG) Log.d(TAG, "Service has been bound");
		return serviceBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
	}

}
