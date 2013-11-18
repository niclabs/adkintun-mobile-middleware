package cl.niclabs.adkmobile.monitor;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import cl.niclabs.adkmobile.monitor.Monitor.ServiceBinder;

/**
 * Defines a ServiceConnection targeted to Monitors, in order to simplify binding
 * and unbinding tasks
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 *
 * @param <E> type of monitor for which this connection is meant  
 */
public abstract class MonitorConnection<E extends Monitor<?>> implements ServiceConnection {
	private E monitor;
	
	/**
	 * Is called when the monitor is bound
	 * @param monitor
	 */
	public abstract void onServiceConnected(E service);
	
	/**
	 * Called on service crash 
	 * @param monitor
	 */
	public abstract void onServiceCrash(E service);
	
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		@SuppressWarnings("unchecked") // Necesary to remove unchecked cast warning
		ServiceBinder<E> binder = (ServiceBinder<E>) service;
		monitor = binder.getService();
		onServiceConnected(binder.getService());
	}

	/* Is only called on service crash */
	@Override
	public void onServiceDisconnected(ComponentName name) {
		onServiceCrash(monitor);
	}
};
