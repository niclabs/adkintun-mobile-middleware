package cl.niclabs.adkmobile.monitor.data;

import android.location.Location;
import cl.niclabs.adkmobile.monitor.Monitor;
import cl.niclabs.adkmobile.utils.Time;

public class LocationObservation extends AbstractObservation<LocationObservation> {	
	private float accuracy;

	private double altitude;

	private float bearing;

	private double latitude;

	private double longitude;

	private String provider;

	private float speed;
	
	/**
	 * Required by Sugar ORM. Create instance with creation time as timestamp
	 */
	public LocationObservation() {
		super(Monitor.LOCATION, Time.currentTimeMillis());
	}
	
	public LocationObservation(long timestamp) {
		super(Monitor.LOCATION, timestamp);
	}

	/**
	 * 
	 * @return accuracy of the observation in meters
	 */
	public float getAccuracy() {
		return accuracy;
	}

	/**
	 * 
	 * @return altitude of the device, in meters above sea level
	 */
	public double getAltitude() {
		return altitude;
	}

	/**
	 * 
	 * @return bearing, in degrees
	 */
	public float getBearing() {
		return bearing;
	}

	/**
	 * 
	 * @return latitude, in degrees
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * 
	 * @return longitude, in degrees
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * 
	 * @return name of the provider that generated this observation
	 */
	public String getProvider() {
		return provider;
	}

	/**
	 * 
	 * @return speed, in m/s
	 */
	public float getSpeed() {
		return speed;
	}
	
	public void setAccuracy(float accuracy) {
		this.accuracy = accuracy;
	}
	
	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}
	
	public void setBearing(float bearing) {
		this.bearing = bearing;
	}
	
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	public void setProvider(String provider) {
		this.provider = provider;
	}
	
	public void setSpeed(float speed) {
		this.speed = speed;
	}
	
	/**
	 * Returns the distance to the provided observation
	 * @param location
	 * @return distance between the two observations, in meters
	 */
	public float distanceTo(LocationObservation location) {
		float [] results = new float[3];
		Location.distanceBetween(location.getLatitude(), location.getLongitude(), latitude, longitude, results);
		
		return results[0];
	}
}
