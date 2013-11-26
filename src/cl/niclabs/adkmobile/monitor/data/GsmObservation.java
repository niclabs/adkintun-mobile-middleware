package cl.niclabs.adkmobile.monitor.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cl.niclabs.adkmobile.monitor.data.constants.TelephonyStandard;

import com.orm.dsl.Ignore;

public class GsmObservation extends TelephonyObservation<GsmObservation> {
	private int gsmCid;
	private int gsmLac;
	private Integer gsmPsc;
	
	private Double signalBer;
	
	@Ignore
	private List<NeighborAntenna> neighborList;

	/**
	 * Required by Sugar ORM. Create instance with creation time as timestamp
	 */
	public GsmObservation() {
		this.setTelephonyStandard(TelephonyStandard.GSM);
	}
	
	public GsmObservation(long timestamp) {
		super(timestamp);
		this.setTelephonyStandard(TelephonyStandard.GSM);
	}

	/**
	 * @return cell-id of the antenna 
	 */
	public int getGsmCid() {
		return gsmCid;
	}

	/**
	 * @return location area code of the antenna
	 */
	public int getGsmLac() {
		return gsmLac;
	}

	/**
	 * @return primary scrambling code for UMTS, or -1 if unkown or GSM
	 */
	public Integer getGsmPsc() {
		return gsmPsc;
	}

	/**
	 * @return list of neighbors
	 */
	public List<NeighborAntenna> getNeighborList() {
		if (neighborList == null && this.getId() != null) {
			neighborList = new ArrayList<NeighborAntenna>();
			Iterator<NeighborAntenna> it = NeighborAntenna.find(NeighborAntenna.class, "gsm_observation = ?", getId().toString());
			while (it.hasNext()) {
				neighborList.add(it.next());
			}
		}
		
		return neighborList;
	}
	
	/**
	 * @return signal Bit-error rate
	 */
	public Double getSignalBer() {
		return signalBer;
	}
	
	@Override
	public void save() {
		super.save();
		
		if (this.getId() != null && neighborList != null) {
			for (NeighborAntenna neighbor: neighborList) {
				neighbor.setGsmObservation(this);
				neighbor.save();
			}
		}
	}
 
	public void setGsmCid(int gsmCid) {
		this.gsmCid = gsmCid;
	}

	public void setGsmLac(int gsmLac) {
		this.gsmLac = gsmLac;
	}

	public void setGsmPsc(Integer gsmPsc) {
		this.gsmPsc = gsmPsc;
	}

	public void setNeighborList(List<NeighborAntenna> neighborList) {
		this.neighborList = neighborList;
	}

	public void setSignalBer(Double signalBer) {
		this.signalBer = signalBer;
	}
}
