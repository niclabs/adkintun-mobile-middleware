package cl.niclabs.adkmobile.monitor;

import cl.niclabs.adkmobile.data.ContentValuesDataObject;
import cl.niclabs.adkmobile.data.DataFields;
import cl.niclabs.adkmobile.data.DataObject;
import cl.niclabs.adkmobile.monitor.events.AbstractMonitorEvent;
import cl.niclabs.adkmobile.monitor.events.MonitorEvent;
import cl.niclabs.adkmobile.monitor.listeners.PhoneStatusListener;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

/**
 * Service that detect the phone status (Airplane Mode, Booting process
 * and Shut Down processs), then notify the current status.
 *
 * @author Mauricio Castro.
 *         Created 30-10-2013.
 */
public class PhoneStatus extends AbstractMonitor<PhoneStatusListener> {
	
	/**
	 * Phone States as defines in Intent.action .
	 *
	 * @author Mauricio Castro.
	 *         Created 30-10-2013.
	 */
	public static enum PhoneState {
		BOOT(1), SHUTDOWN(2), AIRPLANEMODE_ON(3), AIRPLANEMODE_OFF(4),UNKNOWN(5);
		
		int value;
		
		PhoneState(int value){
			this.value = value;
		}
				
		public int getValue() {
			return value;
		}
	}
	
	public static class PhoneStatusData implements DataFields {
		
		/* Define the field name for the Phone State status */
		public static String PHONE_STATUS = "phone_status";
	}
	
	private BroadcastReceiver phoneStatusMonitor = new BroadcastReceiver(){

		@SuppressLint("NewApi")
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_BOOT_COMPLETED)){
				/* this if is probably never entered.
				 * This data should be generated in the AutoStart bradcastReciever.
				 */
				DataObject data = new ContentValuesDataObject();
				data.put(PhoneStatusData.TIMESTAMP, System.currentTimeMillis());
				data.put(PhoneStatusData.PHONE_STATUS, PhoneState.BOOT.getValue());
				
			}
			if (action.equals(Intent.ACTION_SHUTDOWN)){
				DataObject data = new ContentValuesDataObject();
				data.put(PhoneStatusData.TIMESTAMP, System.currentTimeMillis());
				data.put(PhoneStatusData.PHONE_STATUS, PhoneState.SHUTDOWN.getValue());
			}
			if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)){
				int mode;
				
				/* Check the version of the API, if is below to Jelly_Bean,
				 * the Airplane mode must be accessed with a deprecated variable
				 */
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
			        mode = Settings.System.getInt(context.getContentResolver(), 
			                Settings.System.AIRPLANE_MODE_ON, 0);          
			    } else {
			        mode = Settings.Global.getInt(context.getContentResolver(), 
			                Settings.Global.AIRPLANE_MODE_ON, 0);
			    }
				
				if (mode != 0){
					//Airplane mode is enabled
					DataObject data = new ContentValuesDataObject();
					data.put(PhoneStatusData.TIMESTAMP, System.currentTimeMillis());
					data.put(PhoneStatusData.PHONE_STATUS, PhoneState.AIRPLANEMODE_ON.getValue());
				}else{
					//Airplane mode is disabled
					DataObject data = new ContentValuesDataObject();
					data.put(PhoneStatusData.TIMESTAMP, System.currentTimeMillis());
					data.put(PhoneStatusData.PHONE_STATUS, PhoneState.AIRPLANEMODE_OFF.getValue());
				}
				
			}
			
		}		
	};
	
	private final IBinder serviceBinder = new ServiceBinder<PhoneStatus>(this);
	protected String TAG = "AdkintunMobile::PhoneStatus";
	
	private MonitorEvent<PhoneStatusListener> phoneStatusEvent = new AbstractMonitorEvent<PhoneStatusListener>(){
		
		@Override
		public synchronized boolean activate() {
			if (!isActive()) {
				super.activate();
				IntentFilter filter = new IntentFilter();
				filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		        filter.addAction(Intent.ACTION_BOOT_COMPLETED);
		        filter.addAction(Intent.ACTION_SHUTDOWN);
				registerReceiver(phoneStatusMonitor, filter);
				
				
				Log.d(TAG, "PhoneStatus service has been activated");
			}
			return true;
		}
		
		@Override
		public synchronized void deactivate() {
			if (isActive()) {
				super.deactivate();
				unregisterReceiver(phoneStatusMonitor);
				Log.d(TAG, "PhoneStatus service has been deactivated");
			}
		}
		
		@Override
		public void onDataReceived(PhoneStatusListener listener,
				DataObject result) {
			listener.onMobilePhoneStatusChanged(result);
		}
		
	};
	
	@Override
	public boolean activate(int events, Bundle configuration) {
		if ((events & PHONESTATUS) == PHONESTATUS) {
			return activate(phoneStatusEvent);
		}
		return false;
	}

	@Override
	public void deactivate(int events) {
		if ((events & PHONESTATUS) == PHONESTATUS) {
			deactivate(phoneStatusEvent);
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return serviceBinder;
	}
	
	/**
	 * This receiver is executed when the boot process of the phone (when it's on)
	 * and execute Services and check for the SIM Status.
	 *
	 * @author Mauricio Castro.
	 *         Created 30-10-2013.
	 */
	public static class AutoStart extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			/* TODO What this receiver should do when the BOOT action is received.
			 * The BOOT process data should be generate and stored here.
			 */
			
			
		}

	}

}
