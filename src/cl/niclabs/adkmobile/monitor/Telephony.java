package cl.niclabs.adkmobile.monitor;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import cl.niclabs.adkmobile.data.ContentValuesDataObject;
import cl.niclabs.adkmobile.data.DataFields;
import cl.niclabs.adkmobile.data.DataObject;
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
	public enum TelephonyStandard {
		GSM(1), CDMA(2);
		
		int value;
		private TelephonyStandard(int value) {
			this.value = value;
		}
		
		public int getValue() {
			return value;
		}
	}

	public static class TelephonyData implements DataFields {
		/* TODO: Document fields */
		public static String CDMA_BASE_LATITUDE = "cdma_base_latitude";
		public static String CDMA_BASE_LONGITUDE = "cdma_base_longitude";
		public static String CDMA_BASE_STATION = "cdma_base_station";
		public static String CDMA_ECIO = "cdma_ecio";
		public static String CDMA_NETWORK_ID = "cdma_network_id";

		public static String EVDO_DBM = "evdo_dbm";
		public static String EVDO_ECIO = "evdo_ecio";
		public static String EVDO_SNR = "evdo_snr";
		
		public static String TELEPHONY_NEIGHBOR_LIST = "neighbor_list";
		
		public static String TELEPHONY_GSM_CID = "gsm_cid";
		public static String TELEPHONY_GSM_LAC = "gsm_lac";
		public static String TELEPHONY_GSM_PSC = "gsm_psc";
		
		public static String TELEPHONY_SIGNAL_BER = "signal_ber";
		public static String TELEPHONY_SIGNAL_STRENGTH = "signal_strength";
		public static String TELEPHONY_STANDARD = "telephony_std";

		public static String TELEPHONY_NETWORK_TYPE = "network_type";
		public static String TELEPHONY_OPERATOR_MCC = "operator_mcc";
		public static String TELEPHONY_OPERATOR_MNC = "operator_mnc";
		
		public static String TELEPHONY_SIM_STATE = "sim_state";
	}
	
	
	/**
	 * SIM States as defined in android.telephony.TelephonyManager
	 *
	 * @author Mauricio Castro <mauricio@niclabs.cl>.
	 *         Created 17-10-2013.
	 */
	public static enum SIMState {
		ABSENT(1), NETWORK_LOCKED(2), PIN_REQUIRED(3), PUK_REQUIRED(4), READY(5), UNKNOWN(6),
		OTHER(0);
		
		public static SIMState valueOf(int value) {
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
			
			return null;
		}
		
		public static SIMState getType(int value) {
			for (SIMState n: SIMState.values()) {
				if (n.getValue() == value) {
					return n;
				}
			}
			return OTHER;
		}
		
		int value;
		
		private SIMState(int value) {
			this.value = value;
		}

		public int getValue() {
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

			// assign the values to ContentValues variable
			DataObject data = new ContentValuesDataObject();
			
			if (location instanceof GsmCellLocation) {
				GsmCellLocation loc = (GsmCellLocation) location;

				data.put(TelephonyData.EVENT_TYPE, TELEPHONY);
				data.put(TelephonyData.TIMESTAMP, System.currentTimeMillis());
				data.put(TelephonyData.TELEPHONY_STANDARD, TelephonyStandard.GSM.getValue());
				data.put(TelephonyData.TELEPHONY_GSM_CID, loc.getCid());
				data.put(TelephonyData.TELEPHONY_GSM_LAC, loc.getLac());
				
				/* TODO: This will probably fail if there is no network */
				String operator = telephonyManager.getNetworkOperator();
				int mcc = Integer.valueOf(operator.substring(0,3));
				int mnc = Integer.valueOf(operator.substring(3));
				
				data.put(TelephonyData.TELEPHONY_OPERATOR_MCC, mcc);
				data.put(TelephonyData.TELEPHONY_OPERATOR_MNC, mnc);
				
				if (lastSignalStrength != null) {
					synchronized(syncSignalStrength) {
						/* convert the Signal Strength from GSM to Dbm */
						if (lastSignalStrength.getGsmSignalStrength() != 99) {
							float signalStrengthDbm = (lastSignalStrength
									.getGsmSignalStrength() * 2) - 113;
							data.put(TelephonyData.TELEPHONY_SIGNAL_STRENGTH,
									signalStrengthDbm);
						}
						if (lastSignalStrength.getGsmBitErrorRate() != 99) {
							double gsmBerPercent = gsmBerTable[lastSignalStrength.getGsmBitErrorRate()] / 100;
							data.put(TelephonyData.TELEPHONY_SIGNAL_BER,
									gsmBerPercent);
						}
					}
				}
				
				if (lastNetworkType != null){
					data.put(TelephonyData.TELEPHONY_NETWORK_TYPE, lastNetworkType.getValue());
					
				}
				
				data.put(TelephonyData.TELEPHONY_GSM_PSC, loc.getPsc());

				// Add list of neighbors
				List<NeighboringCellInfo> neighbors = telephonyManager.getNeighboringCellInfo();
				if( neighbors.size() > 0 ) {
					List<DataObject> dataNeighborlist = new ArrayList<DataObject>();
                    for(NeighboringCellInfo neighbor : neighbors) {
                    	DataObject neighborData = new ContentValuesDataObject();
                    	neighborData.put(TelephonyData.TELEPHONY_GSM_CID, neighbor.getCid());
                    	neighborData.put(TelephonyData.TELEPHONY_GSM_LAC, neighbor.getLac());
                    	neighborData.put(TelephonyData.TELEPHONY_GSM_PSC, neighbor.getPsc());
                        
                        if(neighbor.getRssi() != NeighboringCellInfo.UNKNOWN_RSSI){
                        		float neighborSignalStrength = (neighbor.getRssi()*2) - 113;
                        		neighborData.put(TelephonyData.TELEPHONY_SIGNAL_STRENGTH, neighborSignalStrength);
                        }
                        
                        // Only add to the list if we have neighbor info
                        if (neighbor.getCid() != NeighboringCellInfo.UNKNOWN_CID
								&& neighbor.getLac() != NeighboringCellInfo.UNKNOWN_CID) {
                        	dataNeighborlist.add(neighborData);
                    	}
                    }
                    
                    if (dataNeighborlist.size() > 0) {
                    	data.put(TelephonyData.TELEPHONY_NEIGHBOR_LIST, dataNeighborlist);
                    }
                }

			} else {
				CdmaCellLocation loc = (CdmaCellLocation) location;
				
				data.put(TelephonyData.EVENT_TYPE, TELEPHONY);
				data.put(TelephonyData.TIMESTAMP, 
						System.currentTimeMillis());
				data.put(TelephonyData.TELEPHONY_STANDARD, TelephonyStandard.CDMA.getValue());
				data.put(TelephonyData.CDMA_BASE_STATION,
						loc.getBaseStationId());
				data.put(TelephonyData.CDMA_BASE_LONGITUDE,
						loc.getBaseStationLongitude());
				data.put(TelephonyData.CDMA_BASE_LATITUDE,
						loc.getBaseStationLatitude());
				data.put(TelephonyData.CDMA_NETWORK_ID, 
						loc.getNetworkId());
				
				/* TODO: This will probably fail if there is no network */
				String operator = telephonyManager.getSimOperator();
				int mcc = Integer.valueOf(operator.substring(0,3));
				int mnc = Integer.valueOf(operator.substring(3));
				
				data.put(TelephonyData.TELEPHONY_OPERATOR_MCC, 
						mcc);
				data.put(TelephonyData.TELEPHONY_OPERATOR_MNC, 
						mnc);
				
				if (lastSignalStrength != null) {
					synchronized(syncSignalStrength) {
						data.put(TelephonyData.TELEPHONY_SIGNAL_STRENGTH,
								lastSignalStrength.getCdmaDbm());
						data.put(TelephonyData.EVDO_DBM,
								lastSignalStrength.getEvdoDbm());
						data.put(TelephonyData.EVDO_DBM,
								lastSignalStrength.getEvdoDbm());
						data.put(TelephonyData.EVDO_SNR,
								lastSignalStrength.getEvdoSnr());
					}
				}
				
				if (lastNetworkType != null){
					data.put(TelephonyData.TELEPHONY_NETWORK_TYPE, lastNetworkType.getValue());
				}
			}
			
			/* Notify listeners and update internal state */
			notifyListeners(telephonyEvent, data);

			/* Log the results */
			if (DEBUG) Log.d(TAG, data.toString());
		}

		@Override
		public void onDataConnectionStateChanged(int state, int networkType){
    		super.onDataConnectionStateChanged(state, networkType);
			lastNetworkType = NetworkType.valueOf(networkType);
    	}
		
		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			super.onSignalStrengthsChanged(signalStrength);
			synchronized(syncSignalStrength) {
				lastSignalStrength = signalStrength;
			}
		}
		
	}
	
	private void setSIMState(int sim_state){
		simState = SIMState.getType(sim_state);
		
		DataObject data = new ContentValuesDataObject();
		data.put(TelephonyData.TIMESTAMP, System.currentTimeMillis());
		data.put(TelephonyData.TELEPHONY_SIM_STATE,simState.getValue());
		
		notifyListeners(telephonyEvent, data);
		
	}
	
	/**
	 * Return the SIM state of the phone when the service is on.
	 * The state is returned by the Telephony.SIMState enum class.
	 *
	 * @return Enum with the state of the SIM
	 */
	public static SIMState getSIMState(){
		if(simState != null)
			return simState;
		return null;
	}
	
	public void checkSIMstate(){
		int simState = TelephonyManager.SIM_STATE_UNKNOWN;
		if (telephonyManager != null)
			simState = telephonyManager.getSimState();
		setSIMState(simState);
		
	}
	
	public static SIMState simState = null;

	private SignalStrength lastSignalStrength = null;
	private Object syncSignalStrength = new Object();
	private NetworkType lastNetworkType = null;

	/**
	 * Activity-Service binder
	 */
	private final IBinder serviceBinder = new ServiceBinder<Telephony>(this);
	
	protected String TAG = "AdkintunMobile::Telephony";
	
	private Context mContext = this;

	private MonitorEvent<TelephonyListener> telephonyEvent = new AbstractMonitorEvent<TelephonyListener>() {
		@Override
		public synchronized boolean activate() {
			if (!isActive()) {
				telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
				telephonyManager.listen(telephonyStateListener,
						PhoneStateListener.LISTEN_CELL_LOCATION	| PhoneStateListener.LISTEN_SIGNAL_STRENGTHS 
						| PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
				
				/* Get the initial network type */
				ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
				if (ni != null) {
					lastNetworkType = NetworkType.valueOf(ni.getType());
				}
				
				checkSIMstate();
				
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
				
				if (DEBUG) Log.d(TAG, "Telephony service has been deactivated");
				super.deactivate();
			}
		}
		
		@Override
		public void onDataReceived(TelephonyListener listener, DataObject result) {
			listener.onMobileTelephonyChanged(result);
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
