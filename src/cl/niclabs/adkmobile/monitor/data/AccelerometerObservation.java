package cl.niclabs.adkmobile.monitor.data;

import cl.niclabs.adkmobile.monitor.Monitor;
import cl.niclabs.adkmobile.utils.Time;

/**
 * Returns accelerometer data in the coordinate system specified by the monitor.
 * 
 * For instance, the normal accelerometer readings would return data in the coordinate  
 * system of the phone (x-axis along the width of the device, y-axis along the height 
 * of the device, and z-axis perpendicular to the screen)
 * 
 * However other accelerometer data, such as the one returned by the GlobalAccelerometer
 * monitor will return data in the earth-coordinates (z-axis on the direction of gravity
 * and y-axis pointing to the magnetic north tangential to the surface of the earth) 
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 *
 */
public class AccelerometerObservation extends AbstractObservation<AccelerometerObservation> {
	private int accuracy;
	private float x;
	private float y;
	private float z;
	
	/**
	 * Required by Sugar ORM. Creates a new connectivity observation with
	 * time of creation as the event timestamp 
	 */
	public AccelerometerObservation() {
		super(Monitor.ACCELEROMETER, Time.currentTimeMillis());
	}

	public AccelerometerObservation(long timestamp) {
		super(Monitor.ACCELEROMETER, timestamp);
	}

	/**
	 * Return sensor accuracy
	 * @return
	 */
	public int getAccuracy() {
		return accuracy;
	}

	/**
	 * Get acceleration in the x-axis 
	 * @return
	 */
	public float getX() {
		return x;
	}

	/**
	 * Get acceleration in the y-axis
	 * @return
	 */
	public float getY() {
		return y;
	}

	/**
	 * Get acceleration in the z-axis
	 * @return
	 */
	public float getZ() {
		return z;
	}

	public void setAccuracy(int accuracy) {
		this.accuracy = accuracy;
	}

	public void setX(float x) {
		this.x = x;
	}

	public void setY(float y) {
		this.y = y;
	}
	
	public void setZ(float z) {
		this.z = z;
	}
}
