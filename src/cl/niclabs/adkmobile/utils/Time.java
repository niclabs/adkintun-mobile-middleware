package cl.niclabs.adkmobile.utils;

import cl.niclabs.adkmobile.services.ClockService;

public class Time {
	public static long currentTimeMillis() {
		return ClockService.currentTimeMillis();
	}
}
