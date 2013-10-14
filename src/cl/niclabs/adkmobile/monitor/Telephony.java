package cl.niclabs.adkmobile.monitor;

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
import cl.niclabs.adkmobile.monitor.events.BaseMonitorEvent;
import cl.niclabs.adkmobile.monitor.events.MonitorEvent;
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
		
		public static String TELEPHONY_GSM_CID = "gsm_cid";
		public static String TELEPHONY_GSM_LAC = "gsm_lac";
		public static String TELEPHONY_GSM_PSC = "gsm_psc";
		public static String TELEPHONY_NEIGHBOR_GMS_PSC = "neighbor_gsm_psc";
		public static String TELEPHONY_NEIGHBOR_GSM_CID = "neighbor_gsm_cid";
		public static String TELEPHONY_NEIGHBOR_GSM_LAC = "neighbor_gsm_lac";
		
		public static String TELEPHONY_NEIGHBOR_SIGNAL_STRENGTH = "neighbor_signal_strenght";
		public static String TELEPHONY_SIGNAL_BER = "signal_ber";
		public static String TELEPHONY_SIGNAL_STRENGTH = "signal_strength";
		public static String TELEPHONY_STANDARD = "telephony_std";

		public static String TOWER_TYPE = "tower_type";
		// TODO: TELEPHONY_STANDARD and TOWER_TYPE are not used, same thing for CDMA_ECIO, EVDO_ECIO
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
				data.put(TelephonyData.TELEPHONY_GSM_PSC, loc.getPsc());

				/* TODO add neighbor list? */
				List<NeighboringCellInfo> neighbors = telephonyManager.getNeighboringCellInfo();
				if( neighbors.size() > 0 ) {
                    for(NeighboringCellInfo neighbor : neighbors ) {
                    	
                        data.put(TelephonyData.TIMESTAMP, System.currentTimeMillis());
                        data.put(TelephonyData.TELEPHONY_NEIGHBOR_GSM_CID, neighbor.getCid());
                        data.put(TelephonyData.TELEPHONY_NEIGHBOR_GSM_LAC, neighbor.getLac());
                        data.put(TelephonyData.TELEPHONY_NEIGHBOR_GMS_PSC, neighbor.getPsc());
                        
                        if(neighbor.getRssi() != NeighboringCellInfo.UNKNOWN_RSSI){
                        		float signalNeighborStrength = (neighbor.getRssi()*2) - 113;
                        		data.put(TelephonyData.TELEPHONY_NEIGHBOR_SIGNAL_STRENGTH,signalNeighborStrength);
                        }                        
                    }
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
				data.put(TelephonyData.CDMA_NETWORK_ID, loc.getNetworkId());
				
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
			}
			
			/* Notify listeners and update internal state */
			notifyListeners(telephonyEvent, data);

			/* Log the results */
			Log.d(TAG, data.toString());
		}

		@Override
		public void onDataConnectionStateChanged(int state, int networkType){
    		
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

	/**
	 * Activity-Service binder
	 */
	private final IBinder serviceBinder = new ServiceBinder();
	
	protected String TAG = "AdkintunMobile::Telephony";

	private MonitorEvent telephonyEvent = new BaseMonitorEvent() {
		@Override
		public synchronized void activate() {
			if (!isActive()) {
				telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
				telephonyManager.listen(telephonyStateListener,
						PhoneStateListener.LISTEN_CELL_LOCATION
								| PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
				
				super.activate();
				
				Log.d(TAG, "Telephony service has been activated");
			}
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
		public void onDataReceived(MonitorListener listener, DataObject oldData, DataObject newData) {
			if (listener instanceof TelephonyListener) {
				((TelephonyListener) listener).onMobileTelephonyChanged(newData);
			}
		}
	};

	private TelephonyManager telephonyManager = null;
	
	private TelephonyStateListener telephonyStateListener = new TelephonyStateListener();

	@Override
	public void activate(int events, Bundle configuration) {
		if ((events & TELEPHONY) == TELEPHONY) {
			activate(telephonyEvent);
		}
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
