package cl.niclabs.adkmobile.monitor;

/**
 * Defines the monitoring events implemented by the library. These constants are used
 * when calling the methods of the Monitor subclasses
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 */
public final class MonitorManager {
	/**
	 * Defines a change in connectivity status
	 */
	public static final int CONNECTIVITY_CHANGE = 1;
	/**
	 * Defines a change in mobile tcp traffic rate
	 */
	public static final int MOBILE_TCP_TRAFFIC_CHANGE = 2;
	/**
	 * Defines a change in mobile traffic rate
	 */
	public static final int MOBILE_TRAFFIC_CHANGE = 3;
}
