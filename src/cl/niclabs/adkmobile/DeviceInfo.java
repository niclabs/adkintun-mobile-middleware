package cl.niclabs.adkmobile;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

public class DeviceInfo extends Build {

	// private static final String TAG = "AdkintunMobile::Device";
	private static TelephonyManager telephonyManager;

	private static DeviceInfo instance = null;

	private DeviceInfo(Context context) {
		telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
	}

	public DeviceInfo getInstance(Context context) {
		if (instance == null)
			instance = new DeviceInfo(context);
		return instance;
	}

	public int getVersionSDK() {
		return VERSION.SDK_INT;
	}

	public String getVersionRelease() {
		return VERSION.RELEASE;
	}

	public String getID() {
		return telephonyManager.getDeviceId();
	}

	public String getDeviceSoftwareVersion() {
		return telephonyManager.getDeviceSoftwareVersion();
	}

	public String getNetworkCountryIso() {
		return telephonyManager.getNetworkCountryIso();
	}

	public String getNetworkOperator() {
		return telephonyManager.getNetworkOperator();
	}

	public String getNetworkOperatorName() {
		return telephonyManager.getNetworkOperatorName();
	}

	public String getSubscriberId() {
		return telephonyManager.getSubscriberId();
	}

	public String getSimCountryIso() {
		return telephonyManager.getSimCountryIso();
	}

	public String getSimOperator() {
		return telephonyManager.getSimOperator();
	}

	public String getSimOperatorName() {
		return telephonyManager.getSimOperatorName();
	}

	public String getSimSerialNumber() {
		return telephonyManager.getSimSerialNumber();
	}

	public int getPhoneType() {
		return telephonyManager.getPhoneType();
	}

	public String getOsArchitecture() {
		return System.getProperty("os.arch");
	}

	public String getOsName() {
		return System.getProperty("os.name");
	}

	public String getOsVersion() {
		return System.getProperty("os.version");
	}

}
