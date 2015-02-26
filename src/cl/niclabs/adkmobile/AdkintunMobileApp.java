package cl.niclabs.adkmobile;

import cl.niclabs.adkmobile.monitor.ClockSynchronization;
import cl.niclabs.adkmobile.monitor.Monitor;
import cl.niclabs.android.NicLabsApp;

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
public class AdkintunMobileApp extends NicLabsApp {	
	/**
	 * Library version
	 */
	public static final String VERSION = "1.3.1b";
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		// Start the ClockSynchronization monitor
		ClockSynchronization
			.bind(ClockSynchronization.class, this)
			.activate(Monitor.CLOCK);
	}
}
