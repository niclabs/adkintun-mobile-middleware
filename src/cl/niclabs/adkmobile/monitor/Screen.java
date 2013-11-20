package cl.niclabs.adkmobile.monitor;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import cl.niclabs.adkmobile.monitor.data.Observation;
import cl.niclabs.adkmobile.monitor.data.StateChange;
import cl.niclabs.adkmobile.monitor.data.constants.ScreenState;
import cl.niclabs.adkmobile.monitor.data.constants.StateType;
import cl.niclabs.adkmobile.monitor.events.AbstractMonitorEvent;
import cl.niclabs.adkmobile.monitor.events.MonitorEvent;
import cl.niclabs.adkmobile.monitor.listeners.ScreenListener;

/**
 * Implement monitoring of the Screen status change. Screen is
 * notified by the system as a broadcast for the ACTION_SCREEN_ON, ACTION_SCREEN_OFF,
 * ACTION_USER_PRESENT Intent.
 *
 * @author Mauricio Castro <mauricio@niclabs.cl>.
 * Created 17-10-2013.
 */
public class Screen extends AbstractMonitor<ScreenListener> {
	
	private BroadcastReceiver screenMonitor = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub.
			if (intent.getAction().equalsIgnoreCase(Intent.ACTION_SCREEN_ON)) {
				StateChange data = new StateChange(SCREEN, System.currentTimeMillis());
				data.setStateType(StateType.SCREEN);
				data.setState(ScreenState.ON.value());

				/* Notify listeners and update internal state */
				notifyListeners(screenEvent, data);
				if (DEBUG) Log.v(TAG, data.toString());

			}
			if (intent.getAction().equalsIgnoreCase(Intent.ACTION_SCREEN_OFF)) {
				StateChange data = new StateChange(SCREEN, System.currentTimeMillis());
				data.setStateType(StateType.SCREEN);
				data.setState(ScreenState.OFF.value());

				/* Verify that phone is actually locked */
				KeyguardManager km = (KeyguardManager) context
						.getSystemService(KEYGUARD_SERVICE);
				if (km.inKeyguardRestrictedInputMode()) {
					// This is the correct status, OFF will never be displayed
					data.setState(ScreenState.LOCKED.value());
				}
				/* Notify listeners and update internal state */
				notifyListeners(screenEvent, data);
				if (DEBUG) Log.v(TAG, data.toString());

			}
			if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
				// assign the values to ContentValues variables
				StateChange data = new StateChange(SCREEN, System.currentTimeMillis());
				data.setStateType(StateType.SCREEN);
				data.setState(ScreenState.UNLOCKED.value());

				/* Notify listeners and update internal state */
				notifyListeners(screenEvent, data);
				if (DEBUG) Log.v(TAG, data.toString());
			}
		}		
	};

	private final IBinder serviceBinder = new ServiceBinder<Screen>(this);
	protected String TAG = "AdkintunMobile::Screen";
	
	private MonitorEvent<ScreenListener> screenEvent = new AbstractMonitorEvent<ScreenListener>() {
		
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
		public void onDataReceived(ScreenListener listener, Observation result) {
			listener.onScreenStateChange((StateChange)result);
		}
		
	};
	
	@Override
	public void activate(int events, Bundle configuration) {
		if ((events & SCREEN) == SCREEN) {
			activate(screenEvent);
		}
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
