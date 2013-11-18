package cl.niclabs.adkmobile.monitor.data;

import cl.niclabs.adkmobile.monitor.Monitor;

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
		super(Monitor.LOCATION, System.currentTimeMillis());
	}
	
	public LocationObservation(long timestamp) {
		super(Monitor.LOCATION, timestamp);
	}

	public float getAccuracy() {
		return accuracy;
	}

	public double getAltitude() {
		return altitude;
	}

	public float getBearing() {
		return bearing;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public String getProvider() {
		return provider;
	}

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
}