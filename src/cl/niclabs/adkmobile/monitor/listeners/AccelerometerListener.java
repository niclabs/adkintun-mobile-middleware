package cl.niclabs.adkmobile.monitor.listeners;

import cl.niclabs.adkmobile.monitor.data.AccelerometerObservation;

public interface AccelerometerListener extends MonitorListener {
	/**
	 * Called when new accelerometer data is available
	 * @param data
	 */
	public void onAccelerometerData(AccelerometerObservation data);
}
