package cl.niclabs.adkmobile.services;

import cl.niclabs.adkmobile.AdkintunMobileApp;
import cl.niclabs.adkmobile.data.Persistent;

public class PersistenceService extends ApplicationService {
	/**
	 * Save the persistent object if persistence is enabled
	 * @param persistent
	 */
	protected void save(Persistent<?> persistent) {
		if (AdkintunMobileApp.isPersistenceAvailable())
			persistent.save();
	}
}
