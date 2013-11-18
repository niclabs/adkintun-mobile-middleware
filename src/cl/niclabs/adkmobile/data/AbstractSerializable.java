package cl.niclabs.adkmobile.data;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic implementation for serialzable objects
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 *
 * @param <E>
 */
public abstract class AbstractSerializable<E extends AbstractSerializable<E>> implements Serializable<E> {
	/**
	 * Return the list of fields of the object
	 * @return
	 */
	@Override
	public List<Field> getSerializableFields() {
        List<Field> toStore = new ArrayList<Field>();
        for (Field field : this.getClass().getDeclaredFields()) {
			if (!field.isAnnotationPresent(DoNotSerialize.class)) {
                toStore.add(field);
            }
        }
        return toStore;
	}
}
