package cl.niclabs.adkmobile.utils;

import java.util.Date;

import cl.niclabs.adkmobile.services.ClockService;

public class Time {
	/**
	 * Get current time in milliseconds since Jan. 1, 1970 GMT.
	 * @return
	 */
	public static long currentTimeMillis() {
		return ClockService.currentTimeMillis();
	}
	
	/**
	 * Get current time in UTC as a date object
	 * @return
	 */
	public static Date currentTime() {
		return new Date(ClockService.currentTimeMillis());
	}
}
