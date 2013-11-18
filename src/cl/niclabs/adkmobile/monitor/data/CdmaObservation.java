package cl.niclabs.adkmobile.monitor.data;

import cl.niclabs.adkmobile.monitor.data.constants.TelephonyStandard;

public class CdmaObservation extends TelephonyObservation<CdmaObservation> {
	private int cdmaBaseLatitude;
	private int cdmaBaseLongitude;
	private int cdmaBaseStationId;
	
	private Integer cdmaEcio;
	
	private Integer evdoDbm;
	private Integer evdoEcio;
	private Integer evdoSnr;

	private int networkId;
	private int systemId;

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
	 * @return CDMA Ec/Io in dB*10
	 */
	public Integer getCdmaEcio() {
		return cdmaEcio;
	}

	/**
	 * 
	 * @return EVDO signal strength in dbm
	 */
	public Integer getEvdoDbm() {
		return evdoDbm;
	}

	/**
	 * 
	 * @return EVDO Ec/Io in dB*10
	 */
	public Integer getEvdoEcio() {
		return evdoEcio;
	}

	/**
	 * 
	 * @return EVDO signal-to-noise ratio (0-8)
	 */
	public Integer getEvdoSnr() {
		return evdoSnr;
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

	public void setCdmaBaseLatitude(int cdmaBaseLatitude) {
		this.cdmaBaseLatitude = cdmaBaseLatitude;
	}
	
	public void setCdmaBaseLongitude(int cdmaBaseLongitude) {
		this.cdmaBaseLongitude = cdmaBaseLongitude;
	}
	
	public void setCdmaBaseStationId(int cdmaBaseStationId) {
		this.cdmaBaseStationId = cdmaBaseStationId;
	}
	
	public void setCdmaEcio(Integer cdmaEcio) {
		this.cdmaEcio = cdmaEcio;
	}
	
	public void setEvdoDbm(Integer evdoDbm) {
		this.evdoDbm = evdoDbm;
	}
	
	public void setEvdoEcio(Integer evdoEcio) {
		this.evdoEcio = evdoEcio;
	}
	
	public void setEvdoSnr(Integer evdoSnr) {
		this.evdoSnr = evdoSnr;
	}
	
	public void setNetworkId(int networkId) {
		this.networkId = networkId;
	}
	
	public void setSystemId(int systemId) {
		this.systemId = systemId;
	}
}