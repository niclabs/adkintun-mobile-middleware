package cl.niclabs.adkmobile.monitor;

import android.content.Context;
import cl.niclabs.adkmobile.monitor.listeners.MonitorListener;

/**
 * Defines a connection between a monitor and a listener for simplifying binding
 * tasks
 * 
 * For instance, the following code activates and binds to the Connectivity service
 * <code>
 * MonitorListenerConnection<Connectivity,ConnectivityListener> connection;
 * bindService(new Intent(this, Connectivity.class), 
 * 		(connection = new MonitorListenerConnection<Connectivity, ConnectivityListener>(this) {
 *			@Override
 * 			public void onServiceConnected(Connectivity service) {
 * 				super.onServiceConnected(service);
 *
 *			 	Intent intent = new Intent(Monitor.ACTIVATE);
 *				intent.putExtra(Monitor.EVENTS_EXTRA,
 *						Monitor.CONNECTIVITY);
 *				sendBroadcast(intent);
 * 			}
 *
 *		}), Context.BIND_AUTO_CREATE);
 * </code>
 * 
 * And to disconnect
 * <code>
 * connection.disconnect(getContext());
 * </code>
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 * 
 * @param <M>
 *            the type of monitor for the connection
 * @param <L>
 *            the type of listener for the connection
 */
public class MonitorListenerConnection<M extends Monitor<L>, L extends MonitorListener> extends MonitorConnection<M> {
	private L listener;
	private M monitor;
	private Boolean connected = false;
	
	public MonitorListenerConnection(L listener) {
		this.listener = listener;
	}

	/**
	 * Must be called when overriding the method
	 */
	@Override
	public void onServiceConnected(M service) {
		synchronized(connected) {
			monitor = service;
			monitor.listen(listener, true);
			connected = true;
		}
	}
	
	/**
	 * Called after calling disconnect()
	 * @param service
	 */
	public void onServiceDisconnected(M service) {
		
	}

	/**
	 * Must be called when overriding the method
	 */
	@Override
	public void onServiceCrash(M service) {
		synchronized(connected) {
			monitor.listen(listener, false);
			monitor = null;
			connected = false;
		}
		
	}

	/**
	 * Returns true if the service is connected
	 * @return
	 */
	public boolean isConnected() {
		synchronized(connected) {
			return connected;
		}
	}
	
	/**
	 * Remove listeners and unbind from the service
	 * @param c context for the connection
	 */
	public void disconnect(Context c) {
		if (connected) {
			monitor.listen(listener, false);
			c.unbindService(this);
			
			onServiceDisconnected(monitor);
			monitor = null;
			connected = false;
		}
	}
}
