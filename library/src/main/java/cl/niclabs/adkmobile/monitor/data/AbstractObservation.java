package cl.niclabs.adkmobile.monitor.data;

import cl.niclabs.android.data.Persistent;

/**
 * Base class for all observations by monitors
 * 
 * Implements cl.niclabs.adkmobile.data.Serializable for backwards compatibility
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 * 
 * @param <E>
 */
public abstract class AbstractObservation<E extends AbstractObservation<E>>
		extends Persistent<E> implements Observation {
	protected int eventType;
	protected long timestamp;

	/**
	 * Required by Sugar ORM.
	 */
	public AbstractObservation() {
	}

	public AbstractObservation(int eventType, long timestamp) {
		this.eventType = eventType;
		this.timestamp = timestamp;
	}

	@Override
	public Integer getEventType() {
		return eventType;
	}

	@Override
	public Long getTimestamp() {
		return timestamp;
	}
}
