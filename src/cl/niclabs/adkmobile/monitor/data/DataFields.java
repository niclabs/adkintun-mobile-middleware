package cl.niclabs.adkmobile.monitor.data;

import android.provider.BaseColumns;


/**
 * Provides the field structure of a DataObject 
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 *
 */
public interface DataFields extends BaseColumns {
	public static final String EVENT_TYPE = "event_type";
	public static final String TIMESTAMP = "timestamp";
}
