package cl.niclabs.adkmobile;


import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import cl.niclabs.adkmobile.data.DoNotSerialize;
import cl.niclabs.adkmobile.data.AbstractSerializable;
import cl.niclabs.android.utils.Time;

/**
 * Singleton class to obtain information about the device (brand, manufacturer, etc)
 * 
 * An instance can be obtained with getInstance(context)
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 * @deprecated use cl.niclabs.android.DeviceInfo
 */
public class DeviceInfo extends AbstractSerializable<DeviceInfo> {
	@DoNotSerialize
	private static TelephonyManager telephonyManager;

	@DoNotSerialize
	private static DeviceInfo instance = null;
	
	private long timestamp;
	private String deviceId;
	private String board;
	private String brand;
	private String device;
	private String buildId;
	private String hardware;
	private String manufacturer;
	private String model;
	private String product;
	private String release;
	private String releaseType;
	private Integer sdk;
	private Integer simMnc;
	private Integer simMcc;
	
	protected DeviceInfo(Context context) {
		telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		
		timestamp = Time.currentTimeMillis();
		deviceId = telephonyManager.getDeviceId();
		board = Build.BOARD;
		brand = Build.BRAND;
		device = Build.DEVICE;
		buildId = Build.DISPLAY;
		hardware = Build.HARDWARE;
		manufacturer = Build.MANUFACTURER;
		model = Build.MODEL;
		product = Build.PRODUCT;
		release = Build.VERSION.RELEASE;
		releaseType = Build.TYPE;
		sdk = Build.VERSION.SDK_INT;
		
		try {
			String operator = telephonyManager.getSimOperator();
			simMcc = Integer.valueOf(operator.substring(0,3));			
			simMnc = Integer.valueOf(operator.substring(3));
		}
		catch (IndexOutOfBoundsException e) {}
		
	}

	/**
	 * 
	 * @param context of the application
	 * @return an instance of this device info
	 */
	public static DeviceInfo getInstance(Context context) {
		if (instance == null)
			instance = new DeviceInfo(context);
		return instance;
	}

	/**
	 * Returns an unique device id (IMEI for GSM, ESN for CDMA)
	 * @return unique device id
	 */
	public String getDeviceId() {
		return deviceId;
	}

	/**
	 * @return Name of the underlying board
	 */
	public String getBoard() {
		return board;
	}

	/**
	 * @return carrier that the device is customized to
	 */
	public String getBrand() {
		return brand;
	}

	/**
	 * 
	 * @return name of the industrial design
	 */
	public String getDevice() {
		return device;
	}
	
	/**
	 * 
	 * @return build id 
	 */
	public String getBuildId() {
		return buildId;
	}

	/**
	 * 
	 * @return name of the hardware
	 */
	public String getHardware() {
		return hardware;
	}

	/**
	 * 
	 * @return manufacturer of the device
	 */
	public String getManufacturer() {
		return manufacturer;
	}

	/**
	 * 
	 * @return model of the device
	 */
	public String getModel() {
		return model;
	}

	/**
	 * 
	 * @return name of the product
	 */
	public String getProduct() {
		return product;
	}

	/**
	 * 
	 * @return version release string
	 */
	public String getRelease() {
		return release;
	}

	/**
	 * 
	 * @return release type
	 */
	public String getReleaseType() {
		return releaseType;
	}

	/**
	 * 
	 * @return SDK number
	 */
	public Integer getSdk() {
		return sdk;
	}

	/**
	 * Get the SIM card mobile network code (null if unavailable)
	 * @return
	 */
	public Integer getSimMnc() {
		return simMnc;
	}

	/**
	 * Get the SIM card mobile country code (null if unavailable)
	 * @return
	 */
	public Integer getSimMcc() {
		return simMcc;
	}

	/**
	 * 
	 * @return time that this instance was taken
	 */
	public long getTimestamp() {
		return timestamp;
	}
}
