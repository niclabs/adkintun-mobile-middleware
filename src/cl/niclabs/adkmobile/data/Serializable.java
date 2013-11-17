package cl.niclabs.adkmobile.data;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Interface to represent all serializable objects. 
 * 
 * Serializable objects can be exported into different formats as XML or JSON for transmission
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 *
 * @param <E> serializable
 */
public interface Serializable<E extends Serializable<E>> {
	public List<Field> getSerializableFields();
}
