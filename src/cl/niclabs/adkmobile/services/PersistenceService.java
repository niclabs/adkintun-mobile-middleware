package cl.niclabs.adkmobile.services;

import android.content.Intent;
import cl.niclabs.adkmobile.AdkintunMobileApp;
import cl.niclabs.adkmobile.data.Persistent;

public class PersistenceService extends ApplicationService {
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
