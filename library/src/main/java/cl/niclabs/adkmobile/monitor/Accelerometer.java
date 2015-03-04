package cl.niclabs.adkmobile.monitor;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import cl.niclabs.adkmobile.monitor.data.AccelerometerObservation;
import cl.niclabs.adkmobile.monitor.data.Observation;
import cl.niclabs.adkmobile.monitor.events.AbstractMonitorEvent;
import cl.niclabs.adkmobile.monitor.events.MonitorEvent;
import cl.niclabs.adkmobile.monitor.listeners.AccelerometerListener;
import cl.niclabs.android.utils.Time;

/**
 * Implements monitoring of accelerometer data and returns the values
 * in the device coordinate system. 
 * 
 * The data is returned in m/s^2
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 */
public class Accelerometer extends AbstractMonitor<AccelerometerListener> implements SensorEventListener {
	/**
	 * Sensor delay for accelerometer measurement. 
	 * 
	 * One of {@link SensorManager.SENSOR_DELAY_NORMAL}, {@link SensorManager.SENSOR_DELAY_UI}, 
	 * {@link SensorManager.SENSOR_DELAY_GAME}, {@link SensorManager.SENSOR_DELAY_FASTEST}
	 */
	public static int SENSOR_DELAY = SensorManager.SENSOR_DELAY_NORMAL;
	
	/**
	 * Sensor delay extra for configuring the monitor
	 */
	public static final String SENSOR_DELAY_EXTRA = "sensor_delay";
	
	private Sensor accelerometer;
	
	/**
	 * Accelerometer event
	 */
	private MonitorEvent<AccelerometerListener> accelerometerEvent = new AbstractMonitorEvent<AccelerometerListener>() {
		@Override
		public synchronized boolean activate() {
			if (!isActive()) {
				if (!sensorManager.registerListener(Accelerometer.this, accelerometer, SENSOR_DELAY)) {
					Log.e(TAG, "Device does not support accelerometer tracking. Stopping service.");
					stopSelf();
				}
				
				if(DEBUG) Log.d(TAG, "Global accelerometer service has been activated");
				
				// Do not forget
				super.activate();				
			}
			return true;
		}

		@Override
		public synchronized void deactivate() {
			if (isActive()) {
				// Unregister the listener
				sensorManager.unregisterListener(Accelerometer.this);
				
				if(DEBUG) Log.d(TAG, "Global accelerometer service has been deactivated");
				super.deactivate();
			}
		}
		
		@Override
		public void onDataReceived(AccelerometerListener listener, Observation result) {
			/* Notify result */
			listener.onAccelerometerData((AccelerometerObservation) result);
		}
	};
	
	/**
	 * Sensor manager
	 */
	private SensorManager sensorManager;
	
	/**
	 * Activity-Service binder
	 */
	private final IBinder serviceBinder = new ServiceBinder<Accelerometer>(this);
	
	protected String TAG = "AdkintunMobile::GlobalAccelerometer";

	@Override
	public void activate(int events, Bundle configuration) {
		/* Update the sensor delay */
		SENSOR_DELAY = configuration.getInt(
				SENSOR_DELAY_EXTRA, SENSOR_DELAY);
		
		if ((events & ACCELEROMETER) == ACCELEROMETER) {
			activate(accelerometerEvent);
		}
	}

	@Override
	public void deactivate(int events) {
		if ((events & ACCELEROMETER) == ACCELEROMETER) {
			deactivate(accelerometerEvent);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO do nothing
	}

	@Override
	public IBinder onBind(Intent intent) {
		return serviceBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
	    accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			AccelerometerObservation data = new AccelerometerObservation(Time.currentTimeMillis());
			data.setAccuracy(event.accuracy);
			data.setX(event.values[0]);
			data.setY(event.values[1]);
			data.setZ(event.values[2]);
			
			if (DEBUG) Log.v(TAG, data.toString()); 
			
			notifyListeners(accelerometerEvent, data);
		}
	}
}
