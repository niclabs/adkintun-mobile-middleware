package cl.niclabs.becity;

import java.util.HashMap;
import java.util.Iterator;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import cl.niclabs.adkmobile.DeviceInfo;
import cl.niclabs.adkmobile.monitor.Connectivity;
import cl.niclabs.adkmobile.monitor.Monitor;
import cl.niclabs.adkmobile.monitor.Monitor.Controller;
import cl.niclabs.adkmobile.monitor.data.AccelerometerObservation;
import cl.niclabs.adkmobile.monitor.data.Journey;
import cl.niclabs.adkmobile.monitor.data.LocationObservation;
import cl.niclabs.adkmobile.monitor.listeners.ConnectivityListener;
import cl.niclabs.adkmobile.monitor.proxies.ConnectivityStatus;
import cl.niclabs.adkmobile.monitor.proxies.ConnectivityStatusListener;
import cl.niclabs.adkmobile.net.HttpResponse;
import cl.niclabs.adkmobile.net.HttpUtils;
import cl.niclabs.adkmobile.utils.Scheduler;

public class SynchronizationService extends Service implements ConnectivityStatusListener {
	private Controller<ConnectivityListener> connectivity;
	private ConnectivityStatus connectivityProxy;
	
	protected final String URL = "http://www.adkintunmobile.cl:8080/upload";
	
	private static boolean running = false;
	
	public static boolean isRunning() {
		return running;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		running = true;

		connectivity = Connectivity.bind(Connectivity.class, this);

		// Create proxy and add this class as listener
		connectivityProxy = ConnectivityStatus.getInstance();
		connectivityProxy.listen(this, true);

		// Add proxy to the monitor
		connectivity.listen(connectivityProxy, true);
		
		// Activate the monitor
		connectivity.activate(Monitor.CONNECTIVITY);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		running = false;

		// Stop proxy from listening and unbind
		connectivityProxy.listen(this, false);
		connectivity.unbind();
	}
	
	@Override
	public void onWifiConnection() {
		// Cleanup already synchronized data
		Journey.deleteAll(Journey.class, "finished = 1 AND synced_acceleration = 1 AND synced_location = 1");
		
		// Send data
		synchronize();
	}

	@Override
	public void onMobileConnection(boolean roaming) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRoaming(boolean dataRoamingEnabled) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNetworkDisconnection() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNetworkConnection(boolean isMobileRoaming) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Synchronized data with server
	 */
	private void synchronize() {
		final String deviceId = DeviceInfo.getInstance(this)
				.getDeviceId();

		final Iterator<Journey> it = Journey.find(Journey.class, "finished = 1");
		while (it.hasNext()) {
			final Journey journey = it.next();
			
			// Send location data
			Scheduler.getInstance().execute(new Runnable() {
				public void run() {
					if (journey.isSyncedLocation())
						return;
					
					Iterator locationEvents = LocationObservation.find(
							LocationObservation.class,
							"timestamp >= ? and timestamp <= ?",
							String.valueOf(journey.getStartTime()),
							String.valueOf(journey.getEndTime()));

					HashMap<String, String> locationEventsMap = new HashMap<String, String>();
					locationEventsMap.put("device_id", deviceId);
					locationEventsMap.put("data_type", "loc");
					locationEventsMap.put("journey_id", String.valueOf(journey.getId()));
					locationEventsMap.put("transport", journey.getTransport()
							.toString());

					HttpResponse response = HttpUtils.sendList(locationEvents,
							URL, locationEventsMap);

					if (response.getCode() >= 200 && response.getCode() < 300) {
						LocationObservation.deleteAll(
								LocationObservation.class,
								"timestamp >= ? AND timestamp <= ?",
								String.valueOf(journey.getStartTime()),
								String.valueOf(journey.getEndTime()));
						
						journey.setSyncedLocation(true);
						journey.save();
					}
				}
			});

			// Send acceleration data
			Scheduler.getInstance().execute(new Runnable() {
				public void run() {
					if (journey.isSyncedAcceleration()) 
						return;
					
					Iterator accelerometerEvents = AccelerometerObservation
							.find(AccelerometerObservation.class,
									"timestamp >= ? AND timestamp <= ?",
									String.valueOf(journey.getStartTime()),
									String.valueOf(journey.getEndTime()));

					HashMap<String, String> accelEventsMap = new HashMap<String, String>();
					accelEventsMap.put("device_id", deviceId);
					accelEventsMap.put("data_type", "accel");
					accelEventsMap.put("journey_id", String.valueOf(journey.getId()));
					accelEventsMap.put("transport", journey.getTransport()
							.toString());

					HttpResponse response = HttpUtils.sendList(accelerometerEvents, URL,
							accelEventsMap);

					if (response.getCode() >= 200 && response.getCode() < 300) {

						AccelerometerObservation.deleteAll(
								AccelerometerObservation.class,
								"timestamp >= ? AND timestamp <= ?",
								String.valueOf(journey.getStartTime()),
								String.valueOf(journey.getEndTime()));
						
						journey.setSyncedAcceleration(true);
						journey.save();
					}
				}
			});
		}
	}

}
