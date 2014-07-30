package cl.niclabs.adkmobile;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import cl.niclabs.adkmobile.services.ClockService;
import cl.niclabs.adkmobile.utils.Scheduler;

import com.orm.SugarApp;

/**
 * Base class for Adkintun Mobile Applications. 
 * 
 * It must be added as <code>android:name</code> in the manifest of application using the library
 * in order to use the data storage implemented in the library. 
 * 
 * Even though this class extends SugarApp, usage of the superclass methods is not recommended
 * since the underlying implementation of data storage may change in the future.
 * 
 * <code>
 * <application android:label="@string/app_name" android:icon="@drawable/icon" android:name="cl.niclabs.adkmobile.AdkintunMobileApp">
 * 		<!-- Configures persistence status (enabled by default) -->
 * 		<meta-data android:name="PERSISTENT" android:value="false" />
 * </application>
 * </code>
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 */
public class AdkintunMobileApp extends SugarApp {
	/**
	 * Debugging status of the application
	 */
	public static boolean DEBUG = true;
	
	/**
	 * Library version
	 */
	public static final String VERSION = "1.0b";
	
	/**
	 * Define persistence state for the whole application
	 */
	public static boolean PERSISTENCE_ENABLED = true;
	
	/**
	 * Library settings file
	 */
	public static final String LIBRARY_SETTINGS = "cl.niclabs.adkmobile.settings"; 
	
	/**
	 * Application context, will be null if the application is not added to the manifest
	 */
	private static AdkintunMobileApp adkintunMobileContext;

	/**
	 * Get application context (null if the application is not added in manifest)
	 * @return
	 */
	public static Context getContext() {
		return adkintunMobileContext;
	}
	
	@Override
	public void onTerminate() {
		super.onTerminate();
		
		// Shutdown the scheduler
		Scheduler.getInstance().shutdown();
	}
	
	/**
	 * Get PERSISTENT key value from manifest metadata if available 
	 * @return
	 */
	private boolean isPersistenceEnabled() {
		try {
			ApplicationInfo ai = getPackageManager().getApplicationInfo(this.getPackageName(), PackageManager.GET_META_DATA);
			return ai.metaData.getBoolean("PERSISTENT", PERSISTENCE_ENABLED);
		} catch (NameNotFoundException e) {
		}
		return PERSISTENCE_ENABLED;
	}
	
	/**
	 * Get ADK_DEBUG ley from manifest metadata if available, otherwise use default
	 * value in variable DEBUG
	 * @return
	 */
	private boolean inDebugEnabled() {
		try {
			ApplicationInfo ai = getPackageManager().getApplicationInfo(this.getPackageName(), PackageManager.GET_META_DATA);
			return ai.metaData.getBoolean("ADK_DEBUG", DEBUG);
		} catch (NameNotFoundException e) {
		}
		return DEBUG;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		PERSISTENCE_ENABLED = isPersistenceEnabled();
		DEBUG = inDebugEnabled();
		
		if (!ClockService.isRunning())
			startService(new Intent(this, ClockService.class));
		
		/* Set context (it will be null if application is not added to manifest_ */
		adkintunMobileContext = this;
	}


	/**
	 * 
	 * @return true if persistence is available for this application
	 */
	public static boolean isPersistenceAvailable() {
		return adkintunMobileContext != null && PERSISTENCE_ENABLED;
	}
}
