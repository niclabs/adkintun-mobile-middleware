package cl.niclabs.adkmobile.monitor.events;

import cl.niclabs.adkmobile.monitor.data.DataObject;

public interface MonitorEventResult {
	/**
	 * Return the internal data of the result
	 * @return
	 */
	public DataObject getData();
}
