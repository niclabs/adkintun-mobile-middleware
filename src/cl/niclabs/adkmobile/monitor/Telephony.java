package cl.niclabs.adkmobile.monitor;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import cl.niclabs.adkmobile.monitor.data.ContentValuesDataObject;
import cl.niclabs.adkmobile.monitor.data.DataFields;
import cl.niclabs.adkmobile.monitor.data.DataObject;
import cl.niclabs.adkmobile.monitor.events.AbstractMonitorEvent;
import cl.niclabs.adkmobile.monitor.events.BasicMonitorEventResult;
import cl.niclabs.adkmobile.monitor.events.MonitorEvent;
import cl.niclabs.adkmobile.monitor.events.MonitorEventResult;
import cl.niclabs.adkmobile.monitor.listeners.MonitorListener;
import cl.niclabs.adkmobile.monitor.listeners.TelephonyListener;

/**
 * TODO Put here a description of what this class does.
 * 
 * @author Mauricio Castro. Created 04-10-2013.
 */
public class Telephony extends AbstractMonitor {

	public class ServiceBinder extends Binder {
		public Telephony getService() {
			return Telephony.this;
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
		public static String TELEPHONY_NEIGHBOR_GMS_PSC = "neighbor_gsm_psc";
		public static String TELEPHONY_NEIGHBOR_GSM_CID = "neighbor_gsm_cid";
		public static String TELEPHONY_NEIGHBOR_GSM_LAC = "neighbor_gsm_lac";
		
		public static String TELEPHONY_NEIGHBOR_SIGNAL_STRENGTH = "neighbor_signal_strength";
		public static String TELEPHONY_SIGNAL_BER = "signal_ber";
		public static String TELEPHONY_SIGNAL_STRENGTH = "signal_strength";
		public static String TELEPHONY_STANDARD = "telephony_std";

		public static String TOWER_TYPE = "tower_type";
		public static String TELEPHONY_NETWORK_TYPE = "network_type";
		public static String TELEPHONY_OPERATOR_NAME = "operator_name";
		// TODO: TELEPHONY_STANDARD and TOWER_TYPE are not used, same thing for CDMA_ECIO, EVDO_ECIO
	}
	
	/**
	 * Network types as defined in android.telephony.TelephonyManager
	 *
	 * @author Administrador.
	 *         Created 17-10-2013.
	 */
	public static enum NetworkType{
		UMTS(1), HSDPA(2),EDGE(3),CDMA(4),GPRS(5),LTE(6),HSPA(7),HSPAP(8
				), OTHER(9);
		
		/**
		 * Get the network type from the TelephonyManager constants
		 * 
		 * @param value connectivity identifier according to TelephonyManager constants
		 */
		public static NetworkType valueOf(int value) {
			switch (value) {
				case TelephonyManager.NETWORK_TYPE_UMTS:
					return UMTS;
				case TelephonyManager.NETWORK_TYPE_HSDPA:
					return HSDPA;
				case TelephonyManager.NETWORK_TYPE_EDGE:
					return EDGE;
				case TelephonyManager.NETWORK_TYPE_CDMA:
					return CDMA;
				case TelephonyManager.NETWORK_TYPE_GPRS:
					return GPRS;
				case TelephonyManager.NETWORK_TYPE_LTE:
					return LTE;
				case TelephonyManager.NETWORK_TYPE_HSPA:
					return HSPA;
				case TelephonyManager.NETWORK_TYPE_HSPAP:
					return HSPAP;
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

			// assign the values to ContentValues variables
			DataObject data = new ContentValuesDataObject();
			
			if (location instanceof GsmCellLocation) {
				GsmCellLocation loc = (GsmCellLocation) location;

				data.put(TelephonyData.TIMESTAMP, System.currentTimeMillis());
				data.put(TelephonyData.TELEPHONY_STANDARD, "gsm");
				data.put(TelephonyData.TELEPHONY_GSM_CID, loc.getCid());
				data.put(TelephonyData.TELEPHONY_GSM_LAC, loc.getLac());
				data.put(TelephonyData.TELEPHONY_OPERATOR_NAME, telephonyManager.getNetworkOperatorName());
				
				if (lastSignalStrength != null) {
					synchronized(lastSignalStrength) {
						/* convert the Signal Strength from GSM to Dbm */
						if (lastSignalStrength.getGsmSignalStrength() != 99) {
							float signalStrengthDbm = (lastSignalStrength
									.getGsmSignalStrength() * 2) - 113;
							data.put(TelephonyData.TELEPHONY_SIGNAL_STRENGTH,
									signalStrengthDbm);
						}
						if (lastSignalStrength.getGsmBitErrorRate() != 99) {
							double gsmBerPercent = gsmBerTable[lastSignalStrength.getGsmBitErrorRate()] / 100.0;
							data.put(TelephonyData.TELEPHONY_SIGNAL_BER,
									gsmBerPercent);
						}
					}
				}
				
				if (lastNetworkType != null){
					synchronized (lastNetworkType) {
						data.put(TelephonyData.TELEPHONY_NETWORK_TYPE, lastNetworkType.getValue());
					}
					
				}
				
				data.put(TelephonyData.TELEPHONY_GSM_PSC, loc.getPsc());

				/* TODO add neighbor list? */
				List<NeighboringCellInfo> neighbors = telephonyManager.getNeighboringCellInfo();
				if( neighbors.size() > 0 ) {
					List<DataObject> dataNeighborlist = new ArrayList<DataObject>();
                    for(NeighboringCellInfo neighbor : neighbors ) {
                    	
                    	DataObject dataNeighbor = new ContentValuesDataObject();
                    	// TODO: This does not work, it has to go inside a list
                    	dataNeighbor.put(TelephonyData.TIMESTAMP, System.currentTimeMillis());
                    	dataNeighbor.put(TelephonyData.TELEPHONY_NEIGHBOR_GSM_CID, neighbor.getCid());
                    	dataNeighbor.put(TelephonyData.TELEPHONY_NEIGHBOR_GSM_LAC, neighbor.getLac());
                    	dataNeighbor.put(TelephonyData.TELEPHONY_NEIGHBOR_GMS_PSC, neighbor.getPsc());
                        
                        if(neighbor.getRssi() != NeighboringCellInfo.UNKNOWN_RSSI){
                        		float signalNeighborStrength = (neighbor.getRssi()*2) - 113;
                        		dataNeighbor.put(TelephonyData.TELEPHONY_NEIGHBOR_SIGNAL_STRENGTH,signalNeighborStrength);
                        }
                        dataNeighborlist.add(dataNeighbor);
                    }
                    data.put(TelephonyData.TELEPHONY_NEIGHBOR_LIST,dataNeighborlist);
                }

			} else {
				CdmaCellLocation loc = (CdmaCellLocation) location;
				
				data.put(TelephonyData.TIMESTAMP, 
						System.currentTimeMillis());
				data.put(TelephonyData.TELEPHONY_STANDARD,
						"cdma");
				data.put(TelephonyData.CDMA_BASE_STATION,
						loc.getBaseStationId());
				data.put(TelephonyData.CDMA_BASE_LONGITUDE,
						loc.getBaseStationLongitude());
				data.put(TelephonyData.CDMA_BASE_LATITUDE,
						loc.getBaseStationLatitude());
				data.put(TelephonyData.CDMA_NETWORK_ID, 
						loc.getNetworkId());
				data.put(TelephonyData.TELEPHONY_OPERATOR_NAME, 
						telephonyManager.getNetworkOperatorName());
				
				
				if (lastSignalStrength != null) {
					synchronized(lastSignalStrength) {
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
					synchronized (lastNetworkType) {
						data.put(TelephonyData.TELEPHONY_NETWORK_TYPE, lastNetworkType.getValue());
					}
					
				}
			}
			
			/* Notify listeners and update internal state */
			notifyListeners(telephonyEvent, new BasicMonitorEventResult(data));

			/* Log the results */
			Log.d(TAG, data.toString());
		}

		@Override
		public void onDataConnectionStateChanged(int state, int networkType){
    		super.onDataConnectionStateChanged(state, networkType);
			synchronized (lastNetworkType) {
				lastNetworkType = NetworkType.valueOf(networkType);
			}
    	}
		
		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			super.onSignalStrengthsChanged(signalStrength);
			synchronized (lastSignalStrength) {
				lastSignalStrength = signalStrength;
			}
			/* TODO: add signal now to DB? */
		}
	}

	private SignalStrength lastSignalStrength = null;
	private NetworkType lastNetworkType = null;

	/**
	 * Activity-Service binder
	 */
	private final IBinder serviceBinder = new ServiceBinder();
	
	protected String TAG = "AdkintunMobile::Telephony";

	private MonitorEvent telephonyEvent = new AbstractMonitorEvent() {
		@Override
		public synchronized boolean activate() {
			if (!isActive()) {
				telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
				telephonyManager.listen(telephonyStateListener,
						PhoneStateListener.LISTEN_CELL_LOCATION	| PhoneStateListener.LISTEN_SIGNAL_STRENGTHS 
						| PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
				
				super.activate();
				
				Log.d(TAG, "Telephony service has been activated");
			}
			return true;
		}

		@Override
		public synchronized void deactivate() {
			if (isActive()) {
				telephonyManager.listen(telephonyStateListener,
						PhoneStateListener.LISTEN_NONE);
				super.deactivate();
				
				Log.d(TAG, "Telephony service has been deactivated");
			}
		}
		
		@Override
		public void onDataReceived(MonitorListener listener, MonitorEventResult result) {
			if (listener instanceof TelephonyListener) {
				((TelephonyListener) listener).onMobileTelephonyChanged(result.getData());
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
