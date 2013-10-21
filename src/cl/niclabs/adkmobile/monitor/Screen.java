package cl.niclabs.adkmobile.monitor;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import cl.niclabs.adkmobile.monitor.data.ContentValuesDataObject;
import cl.niclabs.adkmobile.monitor.data.DataFields;
import cl.niclabs.adkmobile.monitor.data.DataObject;
import cl.niclabs.adkmobile.monitor.events.AbstractMonitorEvent;
import cl.niclabs.adkmobile.monitor.events.BasicMonitorEventResult;
import cl.niclabs.adkmobile.monitor.events.MonitorEvent;
import cl.niclabs.adkmobile.monitor.events.MonitorEventResult;
import cl.niclabs.adkmobile.monitor.listeners.MonitorListener;
import cl.niclabs.adkmobile.monitor.listeners.ScreenListener;

/**
 * Implement monitoring of the Screen status change. Screen is
 * notified by the system as a broadcast for the ACTION_SCREEN_ON, ACTION_SCREEN_OFF,
 * ACTION_USER_PRESENT Intent.
 *
 * @author Mauricio Castro <mauricio@niclabs.cl>.
 * Created 17-10-2013.
 */
public class Screen extends AbstractMonitor{
	
	public static enum ScreenStatus {
		ON(1), OFF(2), LOCKED(3), UNLOCKED(4);
		
		int value;
		
		private ScreenStatus(int value) {
			this.value = value;
		}
		
		public int getValue() {
			return value;
		}
	}
	
	
	public static class ScreenData implements DataFields {
		
		/* Define the field name for the Screen status */
		public static String SCREEN_STATUS = "screen_status";
	}
	
	public class ServiceBinder extends Binder {
		public Screen getService() {
			return Screen.this;
		}
	}
	
	private BroadcastReceiver screenMonitor = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub.
			if (intent.getAction().equalsIgnoreCase(Intent.ACTION_SCREEN_ON)) {
				// assign the values to ContentValues variables
				DataObject data = new ContentValuesDataObject();

				data.put(ScreenData.EVENT_TYPE, SCREEN);
				data.put(ScreenData.TIMESTAMP, System.currentTimeMillis());
				data.put(ScreenData.SCREEN_STATUS, ScreenStatus.ON.getValue());

				/* Notify listeners and update internal state */
				notifyListeners(screenEvent, new BasicMonitorEventResult(data));
				Log.d(TAG, data.toString());

			}
			if (intent.getAction().equalsIgnoreCase(Intent.ACTION_SCREEN_OFF)) {
				// assign the values to ContentValues variables
				DataObject data = new ContentValuesDataObject();
				data.put(ScreenData.EVENT_TYPE, SCREEN);
				data.put(ScreenData.TIMESTAMP, System.currentTimeMillis());
				data.put(ScreenData.SCREEN_STATUS, ScreenStatus.OFF.getValue());

				/* Verify that phone is actually locked */
				KeyguardManager km = (KeyguardManager) context
						.getSystemService(KEYGUARD_SERVICE);
				if (km.inKeyguardRestrictedInputMode()) {
					// This is the correct status, OFF will never be displayed
					data.put(ScreenData.SCREEN_STATUS,
							ScreenStatus.LOCKED.getValue());
				}
				/* Notify listeners and update internal state */
				notifyListeners(screenEvent, new BasicMonitorEventResult(data));
				Log.d(TAG, data.toString());

			}
			if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
				// assign the values to ContentValues variables
				DataObject data = new ContentValuesDataObject();
				data.put(ScreenData.EVENT_TYPE, SCREEN);
				data.put(ScreenData.TIMESTAMP, System.currentTimeMillis());
				data.put(ScreenData.SCREEN_STATUS,
						ScreenStatus.UNLOCKED.getValue());

				/* Notify listeners and update internal state */
				notifyListeners(screenEvent, new BasicMonitorEventResult(data));
				Log.d(TAG, data.toString());
			}
		}		
	};

	private final IBinder serviceBinder = new ServiceBinder();
	protected String TAG = "AdkintunMobile::Screen";
	
	private MonitorEvent screenEvent = new AbstractMonitorEvent() {
		
		@Override
		public synchronized boolean activate() {
			if (!isActive()) {
				super.activate();
				IntentFilter filter = new IntentFilter();
				filter.addAction(Intent.ACTION_SCREEN_ON);
		        filter.addAction(Intent.ACTION_SCREEN_OFF);
		        filter.addAction(Intent.ACTION_USER_PRESENT);
				registerReceiver(screenMonitor, filter);
				
				
				Log.d(TAG, "Screen service has been activated");
			}
			return true;
		}
		
		@Override
		public synchronized void deactivate() {
			if (isActive()) {
				super.deactivate();
				unregisterReceiver(screenMonitor);
				Log.d(TAG, "Screen service has been deactivated");
			}
		}
		
		@Override
		public void onDataReceived(MonitorListener listener, MonitorEventResult result) {
			if (listener instanceof ScreenListener) {
				((ScreenListener) listener).onMobileTelephonyChanged(result.getData());
			}
		}
		
	};
	
	@Override
	public boolean activate(int events, Bundle configuration) {
		if ((events & SCREEN) == SCREEN) {
			return activate(screenEvent);
		}
		return false;
	}

	@Override
	public void deactivate(int events) {
		if ((events & SCREEN) == SCREEN) {
			deactivate(screenEvent);
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return serviceBinder;
	}

}
