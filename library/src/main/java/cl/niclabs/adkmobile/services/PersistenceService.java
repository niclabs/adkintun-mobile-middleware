package cl.niclabs.adkmobile.services;

import android.app.Service;
import cl.niclabs.adkmobile.AdkintunMobileApp;
import cl.niclabs.android.data.Persistent;

public abstract class PersistenceService extends Service {
	/**
	 * Save the persistent object if persistence is enabled
	 * @param persistent
	 */
	protected void save(Persistent<?> persistent) {
		if (AdkintunMobileApp.isPersistenceAvailable())
			persistent.save();
	}
}
