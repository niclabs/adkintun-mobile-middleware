package cl.niclabs.adkmobile.monitor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import cl.niclabs.adkmobile.monitor.data.Observation;
import cl.niclabs.adkmobile.monitor.events.MonitorEvent;
import cl.niclabs.adkmobile.monitor.listeners.MonitorListener;
import cl.niclabs.adkmobile.utils.Dispatcher;
import cl.niclabs.adkmobile.utils.Notifier;

/**
 * Base class for all monitoring services. It allows implementing classes to
 * handle multiple types of events and data types, as well as listeners of the
 * monitoring events to be notified of status changes.
 * 
 * TODO: Verify thread safety of the methods in this class
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 * @param <E> listeners that the monitor handles
 */
public abstract class AbstractMonitor<E extends MonitorListener> extends Service implements Monitor<E> {	
	protected class MonitorEventController extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ACTIVATE)) {
				activate(intent.getIntExtra(EVENTS_EXTRA, 0), intent.getExtras());
			}
			else if (intent.getAction().equals(DEACTIVATE)) {
				deactivate(intent.getIntExtra(EVENTS_EXTRA, 0));
			}
		}	
	}
	
	/**
	 * Current state by eventType
	 */
	private Map<MonitorEvent<E>, Observation> currentStates = new ConcurrentHashMap<MonitorEvent<E>,Observation>(4);
	
	
	private MonitorEventController eventController = new MonitorEventController();
	
	/**
	 * Dispatcher for monitor events
	 */
	private Dispatcher<E> dispatcher = new Dispatcher<E>();
	
	protected String TAG = "AdkintunMobile";
	
	@Override
	public boolean activate(MonitorEvent<E> eventType) {
		return eventType.activate();
	}
	
	
	@Override
	public void deactivate(MonitorEvent<E> eventType) {
		eventType.deactivate();
	}
	
	/**
	 * Get the current state data for the specified event type. Returns null if there is no data 
	 * @param eventType
	 * @return
	 */
	@Override
	public Observation getState(MonitorEvent<E> eventType) {
		return currentStates.get(eventType);
	}
	
	/**
	 * Get the activation status of the specified event type
	 * @param eventType
	 * @return
	 */
	public boolean isActive(MonitorEvent<E> eventType) {
		return eventType.isActive();
	}
	
	@Override
	public void listen(E listener, boolean listen) {
		dispatcher.listen(listener, listen);
	}
	
	/**
	 * Notifies listeners of the monitor of new data received and updates 
	 * the internal state of the monitor that can be obtained with getState()
	 * 
	 * Must be called by sub-classes to notify listeners of data received 
	 * 
	 * @param eventType the event to which the data is related
	 * @param result the result from the event
	 */
	protected void notifyListeners(final MonitorEvent<E> eventType, final Observation result) {
		/* Update the internal state */
		setState(eventType, result);
		
		dispatcher.notifyListeners(new Notifier<E>() {
			@Override
			public void notify(E listener) {
				// TODO: Execute the method on a runnable and schedule on a ScheduledThreadPoolExecutor?
				eventType.onDataReceived(listener, result);
			}
			
		});
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTIVATE);
		filter.addAction(DEACTIVATE);
		
		/* Listen for activation messages */
		registerReceiver(eventController, filter);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		unregisterReceiver(eventController);
		
		/**
		 * Deactivate all the events for this monitor
		 */
		deactivate(ALL_EVENTS);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		
		/* Activate the events specified on the intent */
		activate(intent.getIntExtra(EVENTS_EXTRA, 0), intent.getExtras());
		
        return START_STICKY;
	}

	/**
	 * Set the current status data for the specified event type
	 * @param eventType
	 * @param state
	 */
	private void setState(MonitorEvent<E> eventType, Observation state) {
		currentStates.put(eventType, state);
	}
	
	/**
	 * Defines a controller to be bound with AbstractService.bind(). 
	 * The monitor will re-bind automatically on service crash
	 * @author Felipe Lalanne <flalanne@niclabs.cl>
	 *
	 * @param <M> type of monitor to bind
	 * @param <L> type of listener the monitor takes
	 */
	private static class BindableController<M extends Monitor<L>, L extends MonitorListener> implements Controller<L> {
		Boolean connected = false;
		Context context;
		int events = 0;
		Bundle extras = new Bundle();
		List<L> listeners = new CopyOnWriteArrayList<L>(); //TODO: here it would probably suffice with an ArrayList
		M monitor;
		Class<M> cls;
		
		/**
		 * Service connection to the monitor
		 */
		ServiceConnection serviceConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				@SuppressWarnings("unchecked") // Necessary to remove unchecked cast warning
				ServiceBinder<M> binder = (ServiceBinder<M>) service;
				onConnect(binder.getService());
			}

			/* Is only called on service crash */
			@Override
			public void onServiceDisconnected(ComponentName name) {
				onCrash(monitor);
			}
		};
		
		/**
		 * Update the activation configuration for the monitor
		 * @param events
		 * @param configuration
		 */
		public void activate(int events, Bundle extras) {
			this.events |= events;
			
			if (extras != null)  
				this.extras.putAll(extras);
		
			synchronized(connected) {
				if (connected) {
					monitor.activate(this.events, this.extras);
				}
			}
		}
		
		
		/**
		 * Update the activation configuration
		 */
		public void activate(int events) {
			activate(events, null);
		}
		
		/**
		 * Bind the service to the provided context. If returns immediately if the
		 * service is already connected
		 * 
		 * @param cls
		 * @param c
		 */
		public void bind(Class<M> cls, Context c) {
			c.bindService(new Intent(c, cls), serviceConnection, Context.BIND_AUTO_CREATE);
			this.context = c;	
			this.cls = cls;
		}
		
		/**
		 * Deactivate the specified events for the monitor of this controller
		 * @param events
		 */
		public void deactivate(int events) {
			/* Disable the event */
			if (this.events != 0) 
				this.events ^= events;
					
			synchronized(connected) {
				if (connected) {
					monitor.deactivate(events);
				}
			}
		}
		
		/**
		 * Adds a listener to listen when the service is bound
		 * @param listener
		 */
		public void listen(L listener, boolean listen) {
			if (listen) {
				listeners.add(listener);
			}
			else {
				listeners.remove(listener);
			}
			
			/* Add the listener directly to the monitor */
			synchronized(connected) {
				if (connected) {
					monitor.listen(listener, listen);
				}
			}
		}
		
		/**
		 * Add the listeners of this controller to the specified monitor 
		 * @param monitor
		 * @param listen
		 */
		void listen(M monitor, boolean listen) {
			for (L listener: listeners) {
				monitor.listen(listener, listen);
			}
		}
		
		/**
		 * Called when the monitor is bound. It performs the following
		 * tasks.
		 * 
		 * - Adds listeners to monitor
		 * - Activate the monitor if activate() was called previously
		 * 
		 * @param monitor
		 */
		void onConnect(M monitor) {
			synchronized (connected) {
				if (!connected) {
					listen(monitor, true);
					monitor.activate(events, extras);

					this.monitor = monitor;
					this.connected = true;
				}
			}
		}
		
		/**
		 * Called on service crash 
		 * @param monitor
		 */
		void onCrash(M monitor) {
			listen(monitor, false);
			this.connected = false;
			this.monitor = null;
			
			/* Re-bind and reactivate the service */
			bind(cls, context);
			if (events != 0)
				activate(events, extras);
		}
		
		/**
		 * Called on service disconnection
		 * @param monitor
		 */
		void onDisconnect(M monitor) {
			synchronized (connected) {
				if (!connected) {
					listen(monitor, false);
					this.connected = false;
					this.monitor = null;
				}
			}
		}
		
		/**
		 * Unbind from the service
		 */
		public void unbind() {
			if (connected) {
				context.unbindService(serviceConnection);
				onDisconnect(monitor);
			}
		}
	}
	
	/**
	 * Bind the service to a specific context and return a controller for the
	 * service. Multiple calls to bind will return a different instance of the
	 * controller, so precautions must be taken to unbind the services after
	 * usage.
	 * 
	 * The returned controller will re-bind and reactivate the service if a
	 * service crash occurs
	 * 
	 * @return a controller for the service
	 */
	public static <M extends Monitor<L>, L extends MonitorListener> Controller<L> bind(Class<M> cls, Context context) {
		BindableController<M,L> controller = new BindableController<M,L>();
		controller.bind(cls, context);
		return controller;
	}
}
