package cl.niclabs.adkmobile.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Defines method isRunning() to allow applications to check the status of the
 * service
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 */
public abstract class ApplicationService extends Service {
	private static boolean running = false;
	
	/**
	 * 
	 * @return true if the service is running
	 */
	public static boolean isRunning() {
		return running;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	/**
	 * Must be called by extending classes
	 */
	public void onCreate() {
		super.onCreate();
		
		running = true;
	}

	@Override
	/**
	 * Must be called by extending classes
	 */
	public void onDestroy() {
		super.onDestroy();
		
		running = false;
	}
}
