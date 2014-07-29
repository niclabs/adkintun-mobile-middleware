package cl.niclabs.becity;

import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import cl.niclabs.adkmobile.monitor.GlobalAccelerometer;
import cl.niclabs.adkmobile.monitor.Location;
import cl.niclabs.adkmobile.monitor.Monitor;
import cl.niclabs.adkmobile.monitor.Monitor.Controller;
import cl.niclabs.adkmobile.monitor.data.AccelerometerObservation;
import cl.niclabs.adkmobile.monitor.data.Journey;
import cl.niclabs.adkmobile.monitor.data.LocationObservation;
import cl.niclabs.adkmobile.monitor.data.StateChange;
import cl.niclabs.adkmobile.monitor.listeners.AccelerometerListener;
import cl.niclabs.adkmobile.monitor.listeners.LocationListener;
import cl.niclabs.adkmobile.services.PersistenceService;

public class SamplerService extends PersistenceService implements AccelerometerListener, LocationListener {
	private Controller<AccelerometerListener> accelerometer;
	
	private Controller<LocationListener> location;
	
	protected String TAG = "BeCity::SamplerService";
	
	private static Journey journey;
	
	private static boolean running = false;
	
	public static boolean isRunning() {
		return running;
	}
	
	public static Journey getJourney() {
		return journey;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void onCreate() {
		super.onCreate();
		running = true;
		
		Log.d(TAG, "Called onCreate");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		running = false;
		
		accelerometer.unbind();
		location.unbind();
		journey = null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
	
		long id = intent.getLongExtra("journey", -1);
		
		if (id >= 0)
			journey = Journey.findById(Journey.class, id);
		else {
			Log.e(TAG, "Could not start service, could not identify journey");
			stopSelf();
		}
		
		accelerometer = GlobalAccelerometer.bind(GlobalAccelerometer.class, this);
		accelerometer.listen(this, true);
		
		Bundle accelerometerOptions = new Bundle();
		accelerometerOptions.putInt(GlobalAccelerometer.SENSOR_DELAY_EXTRA, SensorManager.SENSOR_DELAY_UI);
		accelerometer.activate(Monitor.ACCELEROMETER, accelerometerOptions);
		
		location = Location.bind(Location.class, this);
		location.listen(this, true);
		
		Bundle locationOptions = new Bundle();
		locationOptions.putInt(Location.UPDATE_TIME_GPS_EXTRA, 0);
		locationOptions.putInt(Location.UPDATE_DISTANCE_GPS_EXTRA, 50);
		locationOptions.putInt(Location.UPDATE_TIME_NETWORK_EXTRA, 0);
		locationOptions.putInt(Location.UPDATE_DISTANCE_NETWORK_EXTRA, 50);
		locationOptions.putInt(Location.EXPIRATION_TIME_EXTRA, 100);
		location.activate(Monitor.LOCATION, locationOptions);
				
        return START_STICKY;
	}


	@Override
	public void onAccelerometerData(AccelerometerObservation data) {
		save(data);
	}


	@Override
	public void onLocationChanged(LocationObservation locationState) {
		save(locationState);
	}


	@Override
	public void onLocationStateChanged(StateChange state) {
		// TODO Auto-generated method stub
		
	}
}
