package cl.niclabs.adkmobile.monitor;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.Matrix;
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
 * in the earth coordinate system. This monitor requires the magnetometer
 * sensor in addition to the accelerometer, therefore it wont work if
 * the device does not support that kind of sensor.
 * 
 * This accelerometer uses the AndroidDeveloper Low Pass filter to isolate the 
 * gravity influence from real accelerometer data.
 * 
 * The data is returned in m/s^2
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 */
public class GlobalAccelerometer extends AbstractMonitor<AccelerometerListener> implements SensorEventListener {
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
	
	/**
	 * Extra for configuring LPF alpha
	 */
	public static final String LPF_ALPHA_EXTRA = "sensor_lpf_alpha";
	
	/**
	 * Default alpha value
	 */
	public static float LPF_ALPHA = 0.9f;
	
	/**
	 * If true, alpha will be kept static (default false)
	 */
	public static boolean LPF_ALPHA_STATIC = false;
	
	/**
	 * Extra to define if alpha will be kept static
	 */
	public static final String LPF_ALPHA_STATIC_EXTRA = "sensor_lpf_alpha_static";
		
	private Sensor accelerometer;
	
	/**
	 * Accelerometer event
	 */
	private MonitorEvent<AccelerometerListener> accelerometerEvent = new AbstractMonitorEvent<AccelerometerListener>() {
		@Override
		public synchronized boolean activate() {
			if (!isActive()) {
				if (!sensorManager.registerListener(GlobalAccelerometer.this, accelerometer, SENSOR_DELAY)) {
					Log.e(TAG, "Device does not support accelerometer tracking. Stopping service.");
					stopSelf();
				}
				
				if (!sensorManager.registerListener(GlobalAccelerometer.this, magnetometer, SENSOR_DELAY)) {
					Log.e(TAG, "Device does not support magnetometer tracking. Stopping service.");
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
				sensorManager.unregisterListener(GlobalAccelerometer.this);
				
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
	
	// Constants for the low-pass filters
	private float timeConstant = 0.18f;
	private float dt = 0;
	
	/**
	 * Gravity acceleration
	 */
	private float [] gravity = new float[] {0, 0, 0, 0};
	
	/**
	 * Raw accelerometer data
	 */
	private float [] input = new float[] {0, 0, 0, 0};
	/**
	 * Linear acceleration
	 */
	private float [] linearAcceleration = new float[] {0, 0, 0, 0};
	
	/**
	 * Magnetic field measurements
	 */
	private float [] geomagnetic = new float[] {0, 0, 0, 0};
	
	/**
	 * Rotation matrix
	 */
	private float [] R = new float[16];

	// Timestamps for the low-pass filters
	private float timestamp = System.nanoTime();
	private float timestampOld = System.nanoTime();
	
	
	private int count = 0;
	
	/**
	 * Magnetic sensor
	 */
	private Sensor magnetometer;
	
	/**
	 * Sensor manager
	 */
	private SensorManager sensorManager;
	
	/**
	 * Activity-Service binder
	 */
	private final IBinder serviceBinder = new ServiceBinder<GlobalAccelerometer>(this);
	
	protected String TAG = "AdkintunMobile::GlobalAccelerometer";

	@Override
	public void activate(int events, Bundle configuration) {
		/* Update the sensor delay */
		SENSOR_DELAY = configuration.getInt(
				SENSOR_DELAY_EXTRA, SENSOR_DELAY);
		
		/* Update the LPF alpha */
		LPF_ALPHA = configuration.getFloat(
				LPF_ALPHA_EXTRA, LPF_ALPHA);
		
		/* Set alpha static */
		LPF_ALPHA_STATIC = configuration.getBoolean(
				LPF_ALPHA_STATIC_EXTRA, LPF_ALPHA_STATIC);
		
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
	    magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		switch (event.sensor.getType()) {
			case Sensor.TYPE_MAGNETIC_FIELD:
				System.arraycopy(event.values, 0, geomagnetic, 0, event.values.length);
				break;
			case Sensor.TYPE_ACCELEROMETER:
				updateAcceleration(event.values);
				break;
		}
		
		// When acceleration has been calibrated
		if (count > 5 && SensorManager.getRotationMatrix(R, null, gravity, geomagnetic)) {
			float [] earthAcceleration = new float[] {0, 0, 0, 0};
			
			// Transposed matrix
			float[] Rt = new float[16];
			
			// TODO: http://developer.android.com/reference/android/hardware/SensorManager.html#getRotationMatrix(float[], float[], float[], float[])
    		// says that R*gravity = [0 0 g] but the calculation only seems to work when we invert the Rotation matrix
    		Matrix.transposeM(Rt, 0, R, 0);
			
    		// Apply rotation to the linear acceleration
			Matrix.multiplyMV(earthAcceleration, 0, Rt, 0, linearAcceleration, 0);
						
			AccelerometerObservation data = new AccelerometerObservation(Time.currentTimeMillis());
			data.setAccuracy(event.accuracy);
			data.setX(earthAcceleration[0]);
			data.setY(earthAcceleration[1]);
			data.setZ(earthAcceleration[2]);
			
			if (DEBUG) Log.v(TAG, data.toString()); 
			
			notifyListeners(accelerometerEvent, data);
		}
	}

	/**
	 * Add a sample.
	 * @param acceleration The acceleration data.
	 * @return Returns the output of the filter.
	 */
	private void updateAcceleration(float [] acceleration) {
		// Get a local copy of the sensor values
		System.arraycopy(acceleration, 0, this.input, 0, acceleration.length);
	
		if (!LPF_ALPHA_STATIC) {
			timestamp = System.nanoTime();
		
			// Find the sample period (between updates).
			// Convert from nanoseconds to seconds
			dt = 1 / (count / ((timestamp - timestampOld) / 1000000000.0f));
		
			// Calculate the LPF alpha
			LPF_ALPHA = timeConstant / (timeConstant + dt);
		}
	
		count++;
	
		if (count > 5) {
			gravity[0] = LPF_ALPHA * gravity[0] + (1 - LPF_ALPHA) * input[0];
			gravity[1] = LPF_ALPHA * gravity[1] + (1 - LPF_ALPHA) * input[1];
			gravity[2] = LPF_ALPHA * gravity[2] + (1 - LPF_ALPHA) * input[2];		
	
			linearAcceleration[0] = input[0] - gravity[0];
			linearAcceleration[1] = input[1] - gravity[1];
			linearAcceleration[2] = input[2] - gravity[2];
		}
	}
}
