package cl.niclabs.adkmobile.monitor;

import java.util.List;

import android.content.Intent;
import android.os.Binder;
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
import cl.niclabs.adkmobile.monitor.listeners.MonitorListener;
import cl.niclabs.adkmobile.monitor.listeners.TelephonyListener;

/**
 * TODO Put here a description of what this class does.
 * 
 * @author Mauricio Castro. Created 04-10-2013.
 */
public class Telephony extends Monitor {

	public class ServiceBinder extends Binder {
		public Telephony getService() {
			return Telephony.getService();
		}
	}

	private static class TelephonyData implements DataFields {
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
		public static String TELEPHONY_SIGNAL_STRENGTH = "signal_strength";
		public static String TELEPHONY_STANDARD = "telephony_std";
		
		public static String TELEPHONY_NEIGHBOR_GSM_CID = "neighbor_gsm_cid";
		public static String TELEPHONY_NEIGHBOR_GSM_LAC = "neighbor_gsm_lac";
		public static String TELEPHONY_NEIGHBOR_GMS_PSC = "neighbor_gsm_psc";
		public static String TELEPHONY_NEIGHBOR_SIGNAL_STRENGTH = "neighbor_signal_strenght";

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
	public class TelephonyStateListener extends PhoneStateListener {

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
					/* convert the Signal Strength from GSM to Dbm */
					if (lastSignalStrength.getGsmSignalStrength() != 99) {
						float signalStrengthDbm = (lastSignalStrength
								.getGsmSignalStrength() * 2) - 113;
						data.put(TelephonyData.TELEPHONY_SIGNAL_STRENGTH,
								signalStrengthDbm);
					}
					
					//TODO: add lastSignalStrength.getGsmBitErrorRate()
					
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
                        
                        
                        if(neighbor.getRssi() != neighbor.UNKNOWN_RSSI){
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
			
			setCurrentState(MonitorManager.TELEPHONY_CHANGE, data);
			
			/* Notify listeners */
			notifyListeners(MonitorManager.TELEPHONY_CHANGE, data);

			/* Log the results */
			Log.d(TAG, data.toString());
		}

		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			super.onSignalStrengthsChanged(signalStrength);
			lastSignalStrength = signalStrength;
			/* TODO: add signal now to DB? */
		}
		
		@Override
		public void onDataConnectionStateChanged(int state, int networkType){
    		
    	}
	}

	private SignalStrength lastSignalStrength = null;

	private TelephonyManager telephonyManager = null;
	
	/**
	 * Instance of the current service
	 */
	private static Telephony telephonyService;

	public static Telephony getService() {
		if (telephonyService == null)
			telephonyService = new Telephony();
		return telephonyService;
	}

	/**
	 * Activity-Service binder
	 */
	private final IBinder serviceBinder = new ServiceBinder();

	protected String TAG = "AdkintunMobile::Telephony";
	private TelephonyData telephonyDataFields;
	private TelephonyStateListener telephonyStateListener = new TelephonyStateListener();

	@Override
	public DataFields getDataFields(int eventType) {
		// TODO Auto-generated method stub.
		if (eventType == MonitorManager.MOBILE_TCP_TRAFFIC_CHANGE) {
			if (telephonyDataFields == null)
				telephonyDataFields = new TelephonyData();
			return telephonyDataFields;
		}
		return null;
	}

	@Override
	protected void onActivateEvent(int eventType) {
		// TODO Auto-generated method stub.
		if (eventType == MonitorManager.TELEPHONY_CHANGE) {
			telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
			telephonyManager.listen(telephonyStateListener,
					PhoneStateListener.LISTEN_CELL_LOCATION
							| PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return serviceBinder;
	}

	@Override
	protected void onDataReceived(MonitorListener listener, int eventType,
			DataObject data) {
		// TODO Auto-generated method stub.
		if (eventType == MonitorManager.TELEPHONY_CHANGE
				&& listener instanceof TelephonyListener) {
			((TelephonyListener) listener).onMobileTelephonyChanged(data);
		}
	}

	@Override
	protected void onDeactivateEvent(int eventType) {
		// TODO Auto-generated method stub.
		switch (eventType) {
		case MonitorManager.TELEPHONY_CHANGE:
			telephonyManager.listen(telephonyStateListener,
					PhoneStateListener.LISTEN_NONE);
			break;
		default:
			break;
		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		// Deactivate the event
		setActive(MonitorManager.TELEPHONY_CHANGE, false);
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);

		int event = intent.getExtras().getInt(MonitorManager.TELEPHONY_INTENT);

		switch (event) {
		case MonitorManager.TELEPHONY_CHANGE:

			setActive(MonitorManager.TELEPHONY_CHANGE, true);
			break;

		default:
			break;
		}

		return START_STICKY;
	}

}
