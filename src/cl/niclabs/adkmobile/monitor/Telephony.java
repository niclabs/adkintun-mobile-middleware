package cl.niclabs.adkmobile.monitor;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import cl.niclabs.adkmobile.monitor.data.CdmaObservation;
import cl.niclabs.adkmobile.monitor.data.GsmObservation;
import cl.niclabs.adkmobile.monitor.data.NeighborAntenna;
import cl.niclabs.adkmobile.monitor.data.Observation;
import cl.niclabs.adkmobile.monitor.data.StateChange;
import cl.niclabs.adkmobile.monitor.data.TelephonyObservation;
import cl.niclabs.adkmobile.monitor.events.AbstractMonitorEvent;
import cl.niclabs.adkmobile.monitor.events.MonitorEvent;
import cl.niclabs.adkmobile.monitor.listeners.TelephonyListener;

/**
 * Implements monitoring of Telephony services of the mobile.
 * 
 * Requires permissions
 * - android.permission.ACCESS_COARSE_LOCATION for obtaining the neighboring cell info
 * - android.permission.READ_PHONE_STATE to get network info
 * 
 * @author Mauricio Castro. Created 04-10-2013.
 */
public class Telephony extends AbstractMonitor<TelephonyListener> {	
	public enum Standard {
		GSM(1), CDMA(2);
		
		int value;
		private Standard(int value) {
			this.value = value;
		}
		
		public int value() {
			return value;
		}
	}
	
	/**
	 * SIM States as defined in android.telephony.TelephonyManager
	 *
	 * @author Mauricio Castro <mauricio@niclabs.cl>.
	 *         Created 17-10-2013.
	 */
	public static enum SimState {
		ABSENT(1), NETWORK_LOCKED(2), PIN_REQUIRED(3), PUK_REQUIRED(4), READY(5), UNKNOWN(6),
		OTHER(0);
		
		public static final int TYPE = 1;
		
		/**
		 * Get the SIM state from TelephonyManager SIM_STATE values
		 * @param value
		 * @return
		 */
		public static SimState valueOf(int value) {
			switch (value) {
			case TelephonyManager.SIM_STATE_ABSENT:
				return ABSENT;

			case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
				return NETWORK_LOCKED;

			case TelephonyManager.SIM_STATE_PIN_REQUIRED:
				return PIN_REQUIRED;

			case TelephonyManager.SIM_STATE_PUK_REQUIRED:
				return PUK_REQUIRED;

			case TelephonyManager.SIM_STATE_READY:
				return READY;

			case TelephonyManager.SIM_STATE_UNKNOWN:
				return UNKNOWN;
			}
			
			return UNKNOWN;
		}
		
		int value;
		
		private SimState(int value) {
			this.value = value;
		}

		public int value() {
			return this.value;
		}
	}
	
	/**
	 * ServiceState as defined in android.telephony.ServiceState
	 */
	public static enum ServiceState {
		EMERGENCY_ONLY(1), IN_SERVICE(2), OUT_OF_SERVICE(3), POWER_OFF(4), UNKNOWN(0);
		
		public static final int TYPE = 2;
		
		/**
		 * Get the SIM state from TelephonyManager SIM_STATE values
		 * @param value
		 * @return
		 */
		public static ServiceState valueOf(android.telephony.ServiceState state) {
			switch(state.getState()) {
			case android.telephony.ServiceState.STATE_EMERGENCY_ONLY:
				return EMERGENCY_ONLY;
			case android.telephony.ServiceState.STATE_IN_SERVICE:
				return IN_SERVICE;
			case android.telephony.ServiceState.STATE_OUT_OF_SERVICE:
				return EMERGENCY_ONLY;
			case android.telephony.ServiceState.STATE_POWER_OFF:
				return POWER_OFF;
			}
			return UNKNOWN;
		}
		
		int value;
		
		private ServiceState(int value) {
			this.value = value;
		}

		public int value() {
			return this.value;
		}
	}
	
	public static enum DataConnectionState {
		DISCONNECTED(1), CONNECTING(2), CONNECTED(3), SUSPENDED(4), UNKNOWN(0); 
		
		public static final int TYPE = 3;
		
		/**
		 * Get the SIM state from TelephonyManager SIM_STATE values
		 * @param value
		 * @return
		 */
		public static DataConnectionState valueOf(int value) {
			switch(value) {
			case TelephonyManager.DATA_DISCONNECTED:
				return DISCONNECTED;
			case TelephonyManager.DATA_CONNECTING:
				return CONNECTING;
			case TelephonyManager.DATA_CONNECTED:
				return CONNECTED;
			case TelephonyManager.DATA_SUSPENDED:
				return SUSPENDED;
			}
			return UNKNOWN;
		}
		
		int value;
		
		private DataConnectionState(int value) {
			this.value = value;
		}

		public int value() {
			return this.value;
		}
	}
	
	/**
	 * Network types as defined in android.telephony.TelephonyManager
	 *
	 * @author Mauricio Castro <mauricio@niclabs.cl>.
	 *         Created 17-10-2013.
	 */
	public static enum NetworkType {
		RTT(1), CDMA(2), EDGE(3), EHRPD(4), EVDO_0(5), EVDO_A(6), EVDO_B(7), GPRS(
				8), HSDPA(9), HSPA(10), HSPAP(11), HSUPA(12), IDEN(13), LTE(14), UMTS(
				15), UNKNOWN(16), OTHER(0);
		
		/**
		 * Get the network type from the TelephonyManager constants
		 * 
		 * @param value connectivity identifier according to TelephonyManager constants
		 */
		public static NetworkType valueOf(int value) {
			switch (value) {
				case TelephonyManager.NETWORK_TYPE_1xRTT:
					return RTT;
				case TelephonyManager.NETWORK_TYPE_CDMA:
					return CDMA;
				case TelephonyManager.NETWORK_TYPE_EDGE:
					return EDGE;
				case TelephonyManager.NETWORK_TYPE_EHRPD:
					return EHRPD;
				case TelephonyManager.NETWORK_TYPE_EVDO_0:
					return EVDO_0;
				case TelephonyManager.NETWORK_TYPE_EVDO_A:
					return EVDO_A;
				case TelephonyManager.NETWORK_TYPE_EVDO_B:
					return EVDO_B;
				case TelephonyManager.NETWORK_TYPE_GPRS:
					return GPRS;
				case TelephonyManager.NETWORK_TYPE_HSDPA:
					return HSDPA;
				case TelephonyManager.NETWORK_TYPE_HSPA:
					return HSPA;
				case TelephonyManager.NETWORK_TYPE_HSPAP:
					return HSPAP;
				case TelephonyManager.NETWORK_TYPE_HSUPA:
					return HSUPA;
				case TelephonyManager.NETWORK_TYPE_IDEN:
					return IDEN;
				case TelephonyManager.NETWORK_TYPE_LTE:
					return LTE;
				case TelephonyManager.NETWORK_TYPE_UMTS:
					return UMTS;
				case TelephonyManager.NETWORK_TYPE_UNKNOWN:
					return UNKNOWN;
			}
			return OTHER;
		}
		
		public static NetworkType getType(int value) {
			for (NetworkType n: NetworkType.values()) {
				if (n.getValue() == value) {
					return n;
				}
			}
			return OTHER;
		}
		
		int value;

		private NetworkType(int value) {
			this.value = value;
		}

		public int getValue() {
			return this.value;
		}	
	};
	
	public static enum AirplaneModeState {
		ON(1), OFF(2);
		
		public static final int TYPE = 4;
		
		int value;
		
		private AirplaneModeState(int value){
			this.value = value;
		}
				
		public int value() {
			return value;
		}
	}
	

	/**
	 * TODO Class that got all the listener of PhoneStateListener Interface.
	 * Describe what to do when a event occurs. This events can be signal
	 * strength change, tower location change.
	 * 
	 * @author Mauricio Castro. Created 04-10-2013.
	 */
	private class TelephonyStateListener extends PhoneStateListener {
		/**
		 * Equivalences between RXQUAL and BER(%).
		 * Source TS 45.008 (8.2.4)
		 */
		double gsmBerTable [] = {0.14, 0.28, 0.57, 1.13, 2.26, 4.53, 9.05, 18.10};
			
		@Override
		public void onCellLocationChanged(CellLocation location) {
			super.onCellLocationChanged(location);
			
			// TODO: What if the telephony service is disabled
			if (location instanceof GsmCellLocation) {
				GsmCellLocation loc = (GsmCellLocation) location;

				GsmObservation data = new GsmObservation(System.currentTimeMillis());
				
				data.setTelephonyStandard(Standard.GSM);
				data.setGsmCid(loc.getCid());
				data.setGsmLac(loc.getLac());
				data.setGsmPsc(loc.getPsc());
				
				/* TODO: This will probably fail if there is no network */
				String operator = telephonyManager.getNetworkOperator();
				if (operator.length() == 6) {
					int mcc = Integer.valueOf(operator.substring(0,3));
					int mnc = Integer.valueOf(operator.substring(3));
					
					data.setMcc(mcc);
					data.setMnc(mnc);
				}
				
				if (lastSignalStrength != null) {
					synchronized(syncSignalStrength) {
						/* convert the Signal Strength from GSM to Dbm */
						if (lastSignalStrength.getGsmSignalStrength() != 99) {
							int signalStrengthDbm = (lastSignalStrength
									.getGsmSignalStrength() * 2) - 113;
							data.setSignalStrength(signalStrengthDbm);
						}
						if (lastSignalStrength.getGsmBitErrorRate() != 99) {
							double gsmBerPercent = gsmBerTable[lastSignalStrength.getGsmBitErrorRate()] / 100;
							data.setSignalBer(gsmBerPercent);
						}
					}
				}
				
				if (lastNetworkType != null){
					data.setNetworkType(lastNetworkType);
					
				}

				// Add list of neighbors
				List<NeighboringCellInfo> neighbors = telephonyManager.getNeighboringCellInfo();
				if( neighbors.size() > 0 ) {
					List<NeighborAntenna> dataNeighborlist = new ArrayList<NeighborAntenna>();
                    for(NeighboringCellInfo neighbor : neighbors) {
                    	NeighborAntenna neighborData = new NeighborAntenna();
                    	neighborData.setGsmCid(neighbor.getCid());
                    	neighborData.setGsmLac(neighbor.getLac());
                    	neighborData.setGsmPsc(neighbor.getPsc());
                        
                        if(neighbor.getRssi() != NeighboringCellInfo.UNKNOWN_RSSI){
                        		int neighborSignalStrength = (neighbor.getRssi()*2) - 113;
                        		neighborData.setSignalStrength(neighborSignalStrength);
                        }
                        
                        // Only add to the list if we have neighbor info
                        if (neighbor.getCid() != NeighboringCellInfo.UNKNOWN_CID
								&& neighbor.getLac() != NeighboringCellInfo.UNKNOWN_CID) {
                        	dataNeighborlist.add(neighborData);
                    	}
                    }
                    
                    if (dataNeighborlist.size() > 0) {
                    	data.setNeighborList(dataNeighborlist);
                    }
                }
				
				/* Notify listeners and update internal state */
				notifyListeners(telephonyEvent, data);

				/* Log the results */
				if (DEBUG) Log.d(TAG, data.toString());
			} 
			else {
				CdmaCellLocation loc = (CdmaCellLocation) location;
				
				// assign the values to ContentValues variable
				CdmaObservation data = new CdmaObservation(System.currentTimeMillis());
				
				data.setTelephonyStandard(Standard.CDMA);
				data.setCdmaBaseStationId(loc.getBaseStationId());
				data.setCdmaBaseLongitude(loc.getBaseStationLongitude());
				data.setCdmaBaseLatitude(loc.getBaseStationLatitude());
				data.setNetworkId(loc.getNetworkId());
				data.setSystemId(loc.getSystemId());
				
				/* TODO: This will probably fail if there is no network */
				String operator = telephonyManager.getSimOperator();
				if (operator.length() == 6) {
					int mcc = Integer.valueOf(operator.substring(0,3));
					int mnc = Integer.valueOf(operator.substring(3));
					
					data.setMcc(mcc);
					data.setMnc(mnc);
				}
				
				if (lastSignalStrength != null) {
					synchronized(syncSignalStrength) {
						data.setSignalStrength(lastSignalStrength.getCdmaDbm());
						data.setCdmaEcio(lastSignalStrength.getCdmaEcio());
						data.setEvdoDbm(lastSignalStrength.getEvdoDbm());
						data.setEvdoEcio(lastSignalStrength.getEvdoEcio());
						data.setEvdoSnr(lastSignalStrength.getEvdoSnr());
					}
				}
				
				if (lastNetworkType != null){
					data.setNetworkType(lastNetworkType);
				}
				
				/* Notify listeners and update internal state */
				notifyListeners(telephonyEvent, data);

				/* Log the results */
				if (DEBUG) Log.d(TAG, data.toString());
			}
			
			
			/**
			 * Check the SIM state for changes
			 */
			int simState = telephonyManager.getSimState();
			
			if (simState != lastSimState.value()) {
				StateChange stateChange = new StateChange(TELEPHONY, System.currentTimeMillis());
				stateChange.setType(SimState.TYPE);
				stateChange.setState(lastSimState.value());
				
				/* Notify the state */
				notifyListeners(telephonyEvent, stateChange);
				
				if (DEBUG) Log.d(TAG, stateChange.toString());
				
				lastSimState = SimState.valueOf(simState);
			}
		}

		@Override
		public void onDataConnectionStateChanged(int state, int networkType){
			super.onDataConnectionStateChanged(state, networkType);
			
			lastNetworkType = NetworkType.valueOf(networkType);
			
			// Report the new DataConnectionState
			StateChange stateChange = new StateChange(TELEPHONY, System.currentTimeMillis());
			stateChange.setType(DataConnectionState.TYPE);
			stateChange.setState(DataConnectionState.valueOf(state).value());
			
			/* Notify the state */
			notifyListeners(telephonyEvent, stateChange);
			
			if (DEBUG) Log.d(TAG, stateChange.toString());
    	}
		
		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			super.onSignalStrengthsChanged(signalStrength);
			synchronized(syncSignalStrength) {
				lastSignalStrength = signalStrength;
			}
		}

		@Override
		public void onServiceStateChanged(
				android.telephony.ServiceState serviceState) {
			super.onServiceStateChanged(serviceState);
			
			// Report the new ServiceState
			StateChange stateChange = new StateChange(TELEPHONY, System.currentTimeMillis());
			stateChange.setType(ServiceState.TYPE);
			stateChange.setState(ServiceState.valueOf(serviceState).value());
			
			/* Notify the state */
			notifyListeners(telephonyEvent, stateChange);
			
			if (DEBUG) Log.d(TAG, stateChange.toString());
		}
		
	}
	
	private BroadcastReceiver airplaneModeMonitor = new BroadcastReceiver(){

		@SuppressWarnings("deprecation")
		@SuppressLint("NewApi")
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)){
				StateChange data = new StateChange(TELEPHONY, System.currentTimeMillis());
				data.setType(AirplaneModeState.TYPE);
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
					data.setState(AirplaneModeState.ON.value());
				}
				else {
					data.setState(AirplaneModeState.OFF.value());
				}
				
				notifyListeners(telephonyEvent, data);
				
				if (DEBUG) Log.d(TAG, data.toString());
			}
			
		}		
	};
	
	private SignalStrength lastSignalStrength = null;
	private Object syncSignalStrength = new Object();
	private NetworkType lastNetworkType = null;

	/**
	 * Activity-Service binder
	 */
	private final IBinder serviceBinder = new ServiceBinder<Telephony>(this);
	
	protected String TAG = "AdkintunMobile::Telephony";
	
	private Context mContext = this;
	
	private SimState lastSimState = null;

	private MonitorEvent<TelephonyListener> telephonyEvent = new AbstractMonitorEvent<TelephonyListener>() {
		@Override
		public synchronized boolean activate() {
			if (!isActive()) {
				telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
				telephonyManager.listen(telephonyStateListener,
						PhoneStateListener.LISTEN_CELL_LOCATION	| PhoneStateListener.LISTEN_SIGNAL_STRENGTHS 
						| PhoneStateListener.LISTEN_DATA_CONNECTION_STATE | PhoneStateListener.LISTEN_SERVICE_STATE);
				
				/* Get the initial network type */
				ConnectivityManager connectivityManager = (ConnectivityManager) mContext
						.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
				if (ni != null) {
					lastNetworkType = NetworkType.valueOf(ni.getType());
				}
				
				/* Activate the airplane mode monitor */
				IntentFilter filter = new IntentFilter();
				filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
				registerReceiver(airplaneModeMonitor, filter);
				
				/**
				 * Check the SIM state immediately and notify the listeners
				 */
				int simState = TelephonyManager.SIM_STATE_UNKNOWN;
				if (telephonyManager != null)
					simState = telephonyManager.getSimState();
				
				lastSimState = SimState.valueOf(simState);
				
				StateChange stateChange = new StateChange(TELEPHONY, System.currentTimeMillis());
				stateChange.setType(SimState.TYPE);
				stateChange.setState(lastSimState.value());
				
				/* Notify the initial state */
				notifyListeners(this, stateChange);
				
				if (DEBUG) Log.d(TAG, "Telephony service has been activated");
				super.activate();
			}
			return true;
		}

		@Override
		public synchronized void deactivate() {
			if (isActive()) {
				telephonyManager.listen(telephonyStateListener,
						PhoneStateListener.LISTEN_NONE);
				
				unregisterReceiver(airplaneModeMonitor);
				
				if (DEBUG) Log.d(TAG, "Telephony service has been deactivated");
				super.deactivate();
			}
		}
		
		@Override
		public void onDataReceived(TelephonyListener listener, Observation result) {
			if (result instanceof TelephonyObservation) {
				listener.onMobileTelephonyChange((TelephonyObservation<?>) result);
			}
			else if (result instanceof StateChange) {
				StateChange stateChange = (StateChange)result;
				switch(stateChange.geType()) {
				case ServiceState.TYPE:
					listener.onServiceStateChange(stateChange);
					break;
				case DataConnectionState.TYPE:
					listener.onDataConnectionStateChange(stateChange);
					break;
				case SimState.TYPE:
					listener.onSimStateChange(stateChange);
					break;
				case AirplaneModeState.TYPE:
					listener.onAirplaneModeChange(stateChange);
					break;
				}
			}
		}
	};

	private TelephonyManager telephonyManager = null;
	
	private TelephonyStateListener telephonyStateListener = new TelephonyStateListener();

	@Override
	public boolean activate(int events, Bundle configuration) {
		if ((events & TELEPHONY) == TELEPHONY) {
			return activate(telephonyEvent);
		}
		return false;
	}

	@Override
	public void deactivate(int events) {
		if ((events & TELEPHONY) == TELEPHONY) {
			deactivate(telephonyEvent);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return serviceBinder;
	}
}
