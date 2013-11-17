package cl.niclabs.adkmobile;

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
 * <application android:label="@string/app_name" android:icon="@drawable/icon"
android:name="cl.niclabs.adkmobile.AdkintunMobileApp">
 * </code>
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 */
public class AdkintunMobileApp extends SugarApp {
	public static final boolean DEBUG = true;
}
