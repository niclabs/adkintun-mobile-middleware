package cl.niclabs.adkmobile.data;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import cl.niclabs.android.data.DoNotSerialize;
import cl.niclabs.android.utils.ReflectionUtils;

import com.orm.dsl.Ignore;

/**
 * Generic implementation for serializable objects.
 * 
 * This is a stub class left for backwards compatibility
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 * 
 * @param <E> sub-class to serialize
 * @deprecated moved to cl.niclabs.android.data
 */
public abstract class AbstractSerializable<E extends AbstractSerializable<E>>
		extends cl.niclabs.android.data.AbstractSerializable<E> implements Serializable<E> {
	
	/**
	 * Return the list of fields of the object except for those with the @DoNotSerizalize
	 * annotation.
	 * 
	 * @return
	 */
	@Override
	public List<Field> getSerializableFields() {
		List<Field> typeFields = new ArrayList<Field>();

		ReflectionUtils.getAllFields(typeFields, getClass());

		List<Field> toStore = new ArrayList<Field>();
		for (Field field : typeFields) {
			if (!field.isAnnotationPresent(com.orm.dsl.Ignore.class)
					&& !field.isAnnotationPresent(Ignore.class)
					&& !field.isAnnotationPresent(DoNotSerialize.class)
					&& !field.isAnnotationPresent(cl.niclabs.adkmobile.data.DoNotSerialize.class)
					&& !Modifier.isStatic(field.getModifiers())
					&& !Modifier.isTransient(field.getModifiers())) {
				toStore.add(field);
			}
		}
		return toStore;
	}
}
