package cl.niclabs.adkmobile.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.orm.dsl.Ignore;

/**
 * Generic implementation for serializable objects.
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 * 
 * @param <E>
 *            sub-class to serialize
 */
public abstract class AbstractSerializable<E extends AbstractSerializable<E>>
		implements Serializable<E> {
	/**
	 * Return the list of fields of the object except for those with the @DoNotSerizalize
	 * annotation.
	 * 
	 * @return
	 */
	@Override
	public List<Field> getSerializableFields() {
		List<Field> typeFields = new ArrayList<Field>();

		getAllFields(typeFields, getClass());

		List<Field> toStore = new ArrayList<Field>();
		for (Field field : typeFields) {
			if ((!field.isAnnotationPresent(Ignore.class) && !field
					.isAnnotationPresent(DoNotSerialize.class))) {
				toStore.add(field);
			}
		}
		return toStore;
	}

	/**
	 * Get all the fields for the class, including those of the super classes
	 * 
	 * @param fields
	 * @param type
	 * @return
	 */
	private static List<Field> getAllFields(List<Field> fields, Class<?> type) {
		Collections.addAll(fields, type.getDeclaredFields());

		if (type.getSuperclass() != null) {
			fields = getAllFields(fields, type.getSuperclass());
		}

		return fields;
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

	/**
	 * Deserialize object from a string
	 * 
	 * Uses the default serializer
	 * 
	 * @param type
	 * @param serializedObject
	 * @return
	 */
	public static <E extends Serializable<?>> E fromString(Class<E> type,
			String serializedObject) {
		Serializer s = SerializerFactory.getInstance().getSerializer();
		try {
			return s.deserialize(type, serializedObject);
		} catch (IOException e) {
			// Parsing error: safe to ignore
		}
		return null;
	}
}
