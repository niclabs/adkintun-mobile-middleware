package cl.niclabs.adkmobile.monitor;

import cl.niclabs.adkmobile.monitor.data.DataFields;
import cl.niclabs.adkmobile.monitor.data.DataObject;

public interface MonitorListener {
	/**
	 * Perform an action on a sensor event. This method is called at the same time of onDataReceived()
	 * @param eventType 
	 */
	public void onSensorEvent(int eventType);
	public void onDataReceived(int eventType, DataFields columns, DataObject values);
}
