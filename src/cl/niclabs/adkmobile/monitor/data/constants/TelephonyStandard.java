package cl.niclabs.adkmobile.monitor.data.constants;

/**
 * Defines the telephony standard (gsm, cdma)
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 *
 */
public enum TelephonyStandard {
	GSM(1), CDMA(2);
	
	int value;
	private TelephonyStandard(int value) {
		this.value = value;
	}
	
	public int value() {
		return value;
	}
}