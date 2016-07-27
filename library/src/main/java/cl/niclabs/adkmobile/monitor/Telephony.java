package cl.niclabs.adkmobile.monitor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
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
import cl.niclabs.adkmobile.monitor.data.constants.AirplaneModeState;
import cl.niclabs.adkmobile.monitor.data.constants.NetworkType;
import cl.niclabs.adkmobile.monitor.data.constants.ServiceState;
import cl.niclabs.adkmobile.monitor.data.constants.SimState;
import cl.niclabs.adkmobile.monitor.data.constants.StateType;
import cl.niclabs.adkmobile.monitor.data.constants.TelephonyStandard;
import cl.niclabs.adkmobile.monitor.events.AbstractMonitorEvent;
import cl.niclabs.adkmobile.monitor.events.MonitorEvent;
import cl.niclabs.adkmobile.monitor.listeners.TelephonyListener;
import cl.niclabs.android.utils.Time;

/**
 * Implements monitoring of Telephony services of the mobile.
 * 
 * Requires permissions - android.permission.ACCESS_COARSE_LOCATION for
 * obtaining the neighboring cell info - android.permission.READ_PHONE_STATE to
 * get network info
 * 
 * @author Mauricio Castro. Created 04-10-2013.
 */
public class Telephony extends AbstractMonitor<TelephonyListener> {
	/**
	 * Listens to telephony events and notifies the listeners
	 * 
	 * @author Mauricio Castro. Created 04-10-2013.
	 */
	private class TelephonyStateListener extends PhoneStateListener {
		/**
		 * Equivalences between RXQUAL and BER(%). Source TS 45.008 (8.2.4)
		 */
		double gsmBerTable[] = { 0.14, 0.28, 0.57, 1.13, 2.26, 4.53, 9.05,
				18.10 };

		@SuppressLint("NewApi")
		@Override
		public void onCellLocationChanged(CellLocation location) {
			super.onCellLocationChanged(location);
			// TODO: What if the telephony service is disabled
			if (location instanceof GsmCellLocation) {
				GsmCellLocation loc = (GsmCellLocation) location;

				GsmObservation data = new GsmObservation(
						Time.currentTimeMillis());

				data.setTelephonyStandard(TelephonyStandard.GSM);
				data.setGsmCid(loc.getCid());
				data.setGsmLac(loc.getLac());

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
					data.setGsmPsc(loc.getPsc());
				}

				/* TODO: This will probably fail if there is no network */
				String operator = telephonyManager.getNetworkOperator();
				try {
					int mcc = Integer.valueOf(operator.substring(0, 3));
					data.setMcc(mcc);

					int mnc = Integer.valueOf(operator.substring(3));
					data.setMnc(mnc);
				} catch (IndexOutOfBoundsException e) {
				}

				if (lastNetworkType != null) {
					data.setNetworkType(lastNetworkType);

				}

				// Add list of neighbors
				List<NeighboringCellInfo> neighbors = telephonyManager
						.getNeighboringCellInfo();
				if (neighbors.size() > 0) {
					List<NeighborAntenna> dataNeighborlist = new ArrayList<NeighborAntenna>();
					for (NeighboringCellInfo neighbor : neighbors) {
						NeighborAntenna neighborData = new NeighborAntenna();
						neighborData.setGsmCid(neighbor.getCid());
						neighborData.setGsmLac(neighbor.getLac());
						neighborData.setGsmPsc(neighbor.getPsc());

						if (neighbor.getRssi() != NeighboringCellInfo.UNKNOWN_RSSI) {
							int neighborSignalStrength = (neighbor.getRssi() * 2) - 113;
							neighborData
									.setSignalStrength(neighborSignalStrength);
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
				if (DEBUG)
					Log.v(TAG, data.toString());

				/* Update last observation */
				lastObservation = data;
			} else {
				CdmaCellLocation loc = (CdmaCellLocation) location;

				// assign the values to ContentValues variable
				CdmaObservation data = new CdmaObservation(
						Time.currentTimeMillis());

				data.setTelephonyStandard(TelephonyStandard.CDMA);
				data.setCdmaBaseStationId(loc.getBaseStationId());
				data.setCdmaBaseLongitude(loc.getBaseStationLongitude());
				data.setCdmaBaseLatitude(loc.getBaseStationLatitude());
				data.setNetworkId(loc.getNetworkId());
				data.setSystemId(loc.getSystemId());

				/* TODO: This will probably fail if there is no network */
				String operator = telephonyManager.getSimOperator();
				try {
					int mcc = Integer.valueOf(operator.substring(0, 3));
					data.setMcc(mcc);

					int mnc = Integer.valueOf(operator.substring(3));
					data.setMnc(mnc);
				} catch (IndexOutOfBoundsException e) {
				}

				if (lastNetworkType != null) {
					data.setNetworkType(lastNetworkType);
				}

				/* Notify listeners and update internal state */
				notifyListeners(telephonyEvent, data);

				/* Log the results */
				if (DEBUG)
					Log.v(TAG, data.toString());

				/* Update last observation */
				lastObservation = data;
			}
		}

		@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
		@Override
		public void onCellInfoChanged(List<CellInfo> cellInfo) {
			super.onCellInfoChanged(cellInfo);
			// TODO Add cell info detection
		}

		@Override
		public void onDataConnectionStateChanged(int state, int networkType) {
			super.onDataConnectionStateChanged(state, networkType);
			lastNetworkType = NetworkType.valueOf(networkType);

			if (lastObservation != null) {
				if (lastObservation instanceof GsmObservation) {
					GsmObservation data = new GsmObservation(
							System.currentTimeMillis());
					GsmObservation old = (GsmObservation) lastObservation;

					data.setNetworkType(old.getNetworkType());
					data.setTelephonyStandard(old.getTelephonyStandard());
					data.setGsmCid(old.getGsmCid());
					data.setGsmLac(old.getGsmLac());
					data.setGsmPsc(old.getGsmPsc());
					data.setMcc(old.getMcc());
					data.setMnc(old.getMnc());
					data.setNeighborList(old.getNeighborList());

					/* Notify listeners and update internal state */
					notifyListeners(telephonyEvent, data);

					/* Log the results */
					if (DEBUG)
						Log.v(TAG, data.toString());

					lastObservation = data;
				} else if (lastObservation instanceof CdmaObservation) {
					CdmaObservation data = new CdmaObservation(
							System.currentTimeMillis());
					CdmaObservation old = (CdmaObservation) lastObservation;

					data.setNetworkType(old.getNetworkType());
					data.setTelephonyStandard(old.getTelephonyStandard());
					data.setMcc(old.getMcc());
					data.setMnc(old.getMnc());
					data.setCdmaBaseLatitude(old.getCdmaBaseLatitude());
					data.setCdmaBaseLongitude(old.getCdmaBaseLongitude());
					data.setCdmaBaseStationId(old.getCdmaBaseStationId());
					data.setNetworkId(old.getNetworkId());
					data.setSystemId(old.getSystemId());

					/* Notify listeners and update internal state */
					notifyListeners(telephonyEvent, data);

					/* Log the results */
					if (DEBUG)
						Log.v(TAG, data.toString());

					lastObservation = data;
				}
			}

			/**
			 * Check the SIM state for changes
			 */
			int simState = telephonyManager.getSimState();
			if (simState != lastSimState.value()) {
				StateChange simStateChange = new StateChange(TELEPHONY,
						Time.currentTimeMillis());
				simStateChange.setStateType(StateType.SIM);
				simStateChange.setState(lastSimState.value());

				/* Notify the state */
				notifyListeners(telephonyEvent, simStateChange);

				if (DEBUG)
					Log.v(TAG, simStateChange.toString());

				lastSimState = SimState.valueOf(simState);
			}
		}

		@Override
		public void onSignalStrengthsChanged(
				android.telephony.SignalStrength signalStrength) {
			super.onSignalStrengthsChanged(signalStrength);
			if (lastObservation != null) {
				boolean changed = false;

                /* Try reflection to use the 'getDbm' method, else use the classic method */
                try {
                    Method currentSignalStrengthMethod = signalStrength.getClass().getDeclaredMethod("getDbm");
                    int currentSignalStrength = (int)currentSignalStrengthMethod.invoke(signalStrength);

                    GsmObservation updatedObservation = (GsmObservation) lastObservation;
                    updatedObservation
                            .updateSignalStrength(currentSignalStrength);

                    changed = true;
                    Log.d("DIEGO", "Using Reflection ");

                } catch (Exception e) {
                    if (signalStrength.isGsm()) {
                        GsmObservation updatedObservation = (GsmObservation) lastObservation;

					/* convert the Signal Strength from GSM to Dbm */
                        if (signalStrength.getGsmSignalStrength() != 99) {
                            int signalStrengthDbm = (signalStrength
                                    .getGsmSignalStrength() * 2) - 113;
                            updatedObservation
                                    .updateSignalStrength(signalStrengthDbm);

                            changed = true;
                        }

                        // Check that bit error rate is in correct range, since not
                        // all devices return the correct value
                        if (signalStrength.getGsmBitErrorRate() >= 0
                                && signalStrength.getGsmBitErrorRate() <= 7) {
                            double gsmBerPercent = gsmBerTable[signalStrength
                                    .getGsmBitErrorRate()] / 100;
                            updatedObservation.updateSignalBer(gsmBerPercent);

                            changed = true;
                        }
                    } else {
                        CdmaObservation updatedObservation = (CdmaObservation) lastObservation;

                        updatedObservation.updateSignalStrength(signalStrength
                                .getCdmaDbm());
                        updatedObservation.updateCdmaEcio(signalStrength
                                .getCdmaEcio());
                        updatedObservation.updateEvdoDbm(signalStrength
                                .getEvdoDbm());
                        updatedObservation.updateEvdoEcio(signalStrength
                                .getEvdoEcio());
                        updatedObservation.updateEvdoSnr(signalStrength
                                .getEvdoSnr());

                        changed = true;
                    }
                }

				if (changed) {
					/* Notify listeners and update internal state */
					notifyListeners(telephonyEvent, lastObservation);

					/* Log the results */
					if (DEBUG)
						Log.v(TAG, lastObservation.toString());
				}
			}
		}

		@Override
		public void onServiceStateChanged(
				android.telephony.ServiceState serviceState) {
			super.onServiceStateChanged(serviceState);

			// Report the new ServiceState
			StateChange stateChange = new StateChange(TELEPHONY,
					Time.currentTimeMillis());
			stateChange.setStateType(StateType.SERVICE);
			stateChange.setState(ServiceState.valueOf(serviceState).value());

			/* Notify the state */
			notifyListeners(telephonyEvent, stateChange);

			if (DEBUG)
				Log.v(TAG, stateChange.toString());

			/**
			 * Check the SIM state for changes
			 */
			int simState = telephonyManager.getSimState();
			if (simState != lastSimState.value()) {
				StateChange simStateChange = new StateChange(TELEPHONY,
						Time.currentTimeMillis());
				simStateChange.setStateType(StateType.SIM);
				simStateChange.setState(lastSimState.value());

				/* Notify the state */
				notifyListeners(telephonyEvent, simStateChange);

				if (DEBUG)
					Log.v(TAG, simStateChange.toString());

				lastSimState = SimState.valueOf(simState);
			}
		}
	}

	private BroadcastReceiver airplaneModeMonitor = new BroadcastReceiver() {
		@SuppressWarnings("deprecation")
		@SuppressLint("NewApi")
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
				StateChange data = new StateChange(TELEPHONY,
						Time.currentTimeMillis());
				data.setStateType(StateType.AIRPLANE_MODE);
				int mode;

				/*
				 * Check the version of the API, if is below to Jelly_Bean, the
				 * Airplane mode must be accessed with a deprecated variable
				 */
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
					mode = Settings.System.getInt(context.getContentResolver(),
							Settings.System.AIRPLANE_MODE_ON, 0);
				} else {
					mode = Settings.Global.getInt(context.getContentResolver(),
							Settings.Global.AIRPLANE_MODE_ON, 0);
				}

				if (mode != 0) {
					// Airplane mode is enabled
					data.setState(AirplaneModeState.ON.value());
				} else {
					data.setState(AirplaneModeState.OFF.value());
				}

				notifyListeners(telephonyEvent, data);

				if (DEBUG)
					Log.v(TAG, data.toString());
			}

		}
	};

	private NetworkType lastNetworkType = null;
	private TelephonyObservation<?> lastObservation = null;

	/**
	 * Activity-Service binder
	 */
	private final IBinder serviceBinder = new ServiceBinder<Telephony>(this);

	protected String TAG = "AdkintunMobile::Telephony";

	private Context mContext = this;

	private SimState lastSimState = null;

	private MonitorEvent<TelephonyListener> telephonyEvent = new AbstractMonitorEvent<TelephonyListener>() {
		@SuppressLint("InlinedApi")
		@Override
		public synchronized boolean activate() {
			if (!isActive()) {
				telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

				int events = PhoneStateListener.LISTEN_CELL_LOCATION
						| PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
						| PhoneStateListener.LISTEN_SERVICE_STATE
						| PhoneStateListener.LISTEN_DATA_CONNECTION_STATE;

				if (VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
					events |= PhoneStateListener.LISTEN_CELL_INFO;
				}

				telephonyManager.listen(telephonyStateListener, events);

				/* Get the initial network type */
				ConnectivityManager connectivityManager = (ConnectivityManager) mContext
						.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
				if (ni != null) {
					lastNetworkType = NetworkType.valueOf(telephonyManager
							.getNetworkType());
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

				StateChange stateChange = new StateChange(TELEPHONY,
						Time.currentTimeMillis());
				stateChange.setStateType(StateType.SIM);
				stateChange.setState(lastSimState.value());

				/* Notify the initial state */
				notifyListeners(this, stateChange);

				if (DEBUG)
					Log.d(TAG, "Telephony service has been activated");
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

				if (DEBUG)
					Log.d(TAG, "Telephony service has been deactivated");
				super.deactivate();
			}
		}

		@Override
		public void onDataReceived(TelephonyListener listener,
				Observation result) {
			if (result instanceof TelephonyObservation) {
				listener.onMobileTelephonyChange((TelephonyObservation<?>) result);
			} else if (result instanceof StateChange) {
				StateChange stateChange = (StateChange) result;
				StateType type = stateChange.getStateType();
				switch (type) {
				case SERVICE:
					listener.onServiceStateChange(stateChange);
					break;
				case SIM:
					listener.onSimStateChange(stateChange);
					break;
				case AIRPLANE_MODE:
					listener.onAirplaneModeChange(stateChange);
					break;
				default:
					break;
				}
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
