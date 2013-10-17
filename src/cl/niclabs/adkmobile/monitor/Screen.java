package cl.niclabs.adkmobile.monitor;

import cl.niclabs.adkmobile.monitor.events.AbstractMonitorEvent;
import cl.niclabs.adkmobile.monitor.events.MonitorEvent;
import cl.niclabs.adkmobile.monitor.events.MonitorEventResult;
import cl.niclabs.adkmobile.monitor.listeners.MonitorListener;
import cl.niclabs.adkmobile.monitor.listeners.ScreenListener;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * TODO Put here a description of what this class does.
 *
 * @author Administrador.
 *         Created 17-10-2013.
 */
public class Screen extends AbstractMonitor{
	
	public class ServiceBinder extends Binder {
		public Screen getService() {
			return Screen.this;
		}
	}
	
	

	private final IBinder serviceBinder = new ServiceBinder();
	protected String TAG = "AdkintunMobile::Screen";
	
	private MonitorEvent screenEvent = new AbstractMonitorEvent() {
		
		@Override
		public synchronized boolean activate() {
			if (!isActive()) {
				
				super.activate();
				
				Log.d(TAG, "Screen service has been activated");
			}
			return true;
		}
		
		@Override
		public synchronized void deactivate() {
			if (isActive()) {
				super.deactivate();
				
				Log.d(TAG, "Telephony service has been deactivated");
			}
		}
		
		@Override
		public void onDataReceived(MonitorListener listener,
				MonitorEventResult result) {
			if (listener instanceof ScreenListener) {
				((ScreenListener) listener).onMobileTelephonyChanged(result.getData());
			}
		}
		
	};
	
	@Override
	public boolean activate(int events, Bundle configuration) {
		if ((events & CONNECTIVITY) == CONNECTIVITY) {
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
