package cl.niclabs.adkmobile.data;

import cl.niclabs.adkmobile.AdkintunMobileApp;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class PersistenceService extends Service {
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}
	
	/**
	 * Save the persistent object if persistence is enabled
	 * @param persistent
	 */
	protected void save(Persistent<?> persistent) {
		if (AdkintunMobileApp.isPersistenceAvailable())
			persistent.save();
	}
}
