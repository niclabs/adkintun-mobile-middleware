package cl.niclabs.adkmobile.monitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import cl.niclabs.adkmobile.AdkintunMobileApp;
import cl.niclabs.adkmobile.monitor.data.StateChange;
import cl.niclabs.adkmobile.monitor.data.constants.DeviceBootState;
import cl.niclabs.adkmobile.monitor.data.constants.StateType;

/**
 * BroadcastReceiver for {@link Intent.ACTION_BOOT_COMPLETED} and
 * {@link Intent.ACTION_SHUTDOWN}
 * 
 * Will save the boot/shutdown events into persistence and call
 * onBootCompleted/onShutdown depending on the event. Users of the library can
 * extend to this class in order to perform tasks on device boot or shutdown.
 * Note that AdkintunMobileApplication must be added to application:name in
 * order to be able to save the state.
 * 
 * The class must be registered to the manifest as a broadcast receiver in order
 * to register the event.
 * 
 * <code>
 * <receiver android:name="cl.niclabs.adkmobile.monitor.Device"
 * 			android:enabled="true"
 *      	android:permission="android.permission.RECEIVE_BOOT_COMPLETED">
 *          <intent-filter>
 *              <action android:name="android.intent.action.BOOT_COMPLETED" />
 *              <action android:name="android.intent.action.ACTION_SHUTDOWN" />
 *              <category android:name="android.intent.category.DEFAULT" />
 *          </intent-filter>
 *      </receiver>
 * </code>
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 */
public class Device extends BroadcastReceiver {
	protected String TAG = "AdkintunMobile::Device";
	
	/**
	 * Called when the boot is completed. The default action is to
	 * do nothing, it must be overriden by extending classes in order
	 * to perform boot actions.
	 */
	public void onBootCompleted() {
		
	}
	
	/**
	 * Called onShutdown. The default action is to
	 * do nothing, it must be overriden by extending classes in order
	 * to perform shutdown actions.
	 */
	public void onShutdown() {
		
	}

	@Override
	public final void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			StateChange stateChange = new StateChange(Monitor.DEVICE, System.currentTimeMillis());
			stateChange.setStateType(StateType.DEVICE_BOOT);
			stateChange.setState(DeviceBootState.BOOT.value());
			
			if (AdkintunMobileApp.DEBUG) Log.d(TAG, stateChange.toString());
			try {
				stateChange.save();
			}
			catch (NullPointerException e) { //Returned by sugar if the SugarApp is not defined in the manifest 
			}
			
			onBootCompleted();
		}
		else if (intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {
			StateChange stateChange = new StateChange(Monitor.DEVICE, System.currentTimeMillis());
			stateChange.setStateType(StateType.DEVICE_BOOT);
			stateChange.setState(DeviceBootState.SHUTDOWN.value());
			
			if (AdkintunMobileApp.DEBUG) Log.d(TAG, stateChange.toString());
			try {
				stateChange.save();
			}
			catch (NullPointerException e) { //Returned by sugar if the SugarApp is not defined in the manifest 
			}
			
			onShutdown();
		}
	}

}
