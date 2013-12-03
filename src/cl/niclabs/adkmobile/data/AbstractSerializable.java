package cl.niclabs.adkmobile.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
	
	public String toString() {
		Serializer serializer = SerializerFactory.getInstance().getSerializer();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			serializer.serialize(out, this);
			out.close();
		} catch (IOException e) {
		}
		return out.toString();
	}
}
