package cl.niclabs.adkmobile.utils;

import java.util.Date;

import cl.niclabs.android.utils.Clock;

/**
 * Helper class to access the methods of Clock
 * 
 * It will probably be deprecated in the near future
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 */
public class Time {
	/**
	 * Get current time in milliseconds since Jan. 1, 1970 GMT.
	 * @return
	 */
	public static long currentTimeMillis() {
		return Clock.currentTimeMillis();
	}
	
	/**
	 * Get current time in UTC as a date object
	 * @return
	 */
	public static Date currentTime() {
		return new Date(Clock.currentTimeMillis());
	}
}
