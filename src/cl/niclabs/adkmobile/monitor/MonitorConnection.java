package cl.niclabs.adkmobile.monitor;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import cl.niclabs.adkmobile.monitor.Monitor.ServiceBinder;

public abstract class MonitorConnection<E extends Monitor> implements ServiceConnection {
	private E monitor;
	
	/**
	 * Is called when the monitor is bound
	 * @param monitor
	 */
	public abstract void onServiceConnected(E monitor);
	
	/**
	 * Called on service crash
	 * @param monitor
	 */
	public abstract void onServiceDisconnected(E monitor);
	
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		@SuppressWarnings("unchecked")
		ServiceBinder<E> binder = (ServiceBinder<E>) service;
		monitor = binder.getService();
		onServiceConnected(binder.getService());
	}

	/* Is only called on service crash */
	@Override
	public void onServiceDisconnected(ComponentName name) {
		onServiceDisconnected(monitor);
	}
};
