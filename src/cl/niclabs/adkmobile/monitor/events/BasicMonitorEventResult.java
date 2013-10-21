package cl.niclabs.adkmobile.monitor.events;

import cl.niclabs.adkmobile.data.DataObject;

public class BasicMonitorEventResult implements MonitorEventResult {
	
	protected DataObject data;
	
	public BasicMonitorEventResult(DataObject data) {
		this.data = data;
	}

	@Override
	public DataObject getData() {
		return data;
	}

}
