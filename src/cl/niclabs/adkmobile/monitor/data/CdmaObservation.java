package cl.niclabs.adkmobile.monitor.data;

import android.telephony.cdma.CdmaCellLocation;
import cl.niclabs.adkmobile.monitor.data.constants.TelephonyStandard;

public class CdmaObservation extends TelephonyObservation<CdmaObservation> {
	private int cdmaBaseLatitude;
	private int cdmaBaseLongitude;
	private int cdmaBaseStationId;

	private int networkId;
	private int systemId;
	
	
	private Sample cdmaEcio;	
	private Sample evdoDbm;
	private Sample evdoEcio;
	private Sample evdoSnr;

	/**
	 * Required by Sugar ORM. Create instance with creation time as timestamp
	 */
	public CdmaObservation() {
		this.setTelephonyStandard(TelephonyStandard.CDMA);
	}

	public CdmaObservation(long timestamp) {
		super(timestamp);
		this.setTelephonyStandard(TelephonyStandard.CDMA);
	}

	/**
	 * 
	 * @return CDMA base station latitude. See {@link CdmaCellLocation} for more info.
	 */
	public int getCdmaBaseLatitude() {
		return cdmaBaseLatitude;
	}

	/**
	 * 
	 * @return CDMA base station longitude. See {@link CdmaCellLocation} for more info.
	 */
	public int getCdmaBaseLongitude() {
		return cdmaBaseLongitude;
	}

	/**
	 * 
	 * @return CDMA base station id (-1 if unknown)
	 */
	public int getCdmaBaseStationId() {
		return cdmaBaseStationId;
	}

	/**
	 * 
	 * @return CDMA network identification number (-1 if unknown)
	 */
	public int getNetworkId() {
		return networkId;
	}

	/**
	 * 
	 * @return CDMA system identification number (-1 if unknown)
	 */
	public int getSystemId() {
		return systemId;
	}
	
	@Override
	public long save() {
		if (cdmaEcio != null)
			cdmaEcio.save();
		
		if (evdoDbm != null)
			evdoDbm.save();
		
		if (evdoEcio != null)
			evdoEcio.save();
		
		if (evdoSnr != null)
			evdoSnr.save();
		
		return super.save();
	}

	public void setCdmaBaseLatitude(int cdmaBaseLatitude) {
		this.cdmaBaseLatitude = cdmaBaseLatitude;
	}
	
	public void setCdmaBaseLongitude(int cdmaBaseLongitude) {
		this.cdmaBaseLongitude = cdmaBaseLongitude;
	}
	
	public void setCdmaBaseStationId(int cdmaBaseStationId) {
		this.cdmaBaseStationId = cdmaBaseStationId;
	}
	
	public void setNetworkId(int networkId) {
		this.networkId = networkId;
	}

	public void setSystemId(int systemId) {
		this.systemId = systemId;
	}
	
	
	/**
	 * 
	 * @return CDMA Ec/Io in dB*10
	 */
	public Sample getCdmaEcio() {
		return cdmaEcio;
	}

	/**
	 * 
	 * @return mean EVDO signal strength in dbm
	 */
	public Sample getEvdoDbm() {
		return evdoDbm;
	}

	/**
	 * 
	 * @return mean EVDO Ec/Io in dB*10
	 */
	public Sample getEvdoEcio() {
		return evdoEcio;
	}

	/**
	 * 
	 * @return mean EVDO signal-to-noise ratio (0-8)
	 */
	public Sample getEvdoSnr() {
		return evdoSnr;
	}
	
	/**
	 * Update cdmaEcio with new value. 
	 * 
	 * @param cdmaEcio
	 */
	public void updateCdmaEcio(int cdmaEcio) {
		if (this.cdmaEcio == null) this.cdmaEcio = new Sample();
		this.cdmaEcio.update(cdmaEcio);
	}
	
	/**
	 * Update evdoDbm with new value. 
	 * 
	 * @param evdoDbm
	 */
	public void updateEvdoDbm(int evdoDbm) {
		if (this.evdoDbm == null) this.evdoDbm = new Sample();
		this.evdoDbm.update(evdoDbm);
	}

	/**
	 * Update evdoEcio with new value. 
	 * 
	 * @param evdoEcio
	 */
	public void updateEvdoEcio(Integer evdoEcio) {
		if (this.evdoEcio == null) this.evdoEcio = new Sample();
		this.evdoEcio.update(evdoEcio);
	}

	/**
	 * Update evdoSnr with new value. 
	 * 
	 * @param evdoSnr
	 */
	public void updateEvdoSnr(Integer evdoSnr) {
		if (this.evdoSnr == null) this.evdoSnr = new Sample();
		this.evdoSnr.update(evdoSnr);
	}
}