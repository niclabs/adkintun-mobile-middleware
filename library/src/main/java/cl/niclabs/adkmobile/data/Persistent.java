package cl.niclabs.adkmobile.data;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import cl.niclabs.android.data.Ignore;
import cl.niclabs.android.utils.ReflectionUtils;


/**
 * Abstraction for all persistent objects.
 * 
 * This is a stub class for backwards compatibility
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 * @deprecated moved to cl.niclabs.android.data
 * @param <E> object that is desired to make persistent
 */
public class Persistent<E extends Persistent<E>> extends cl.niclabs.android.data.Persistent<E> implements Serializable<E> {
	/**
	 * Return the list of fields of the object for serialization
	 * 
	 * @return
	 */
	@Override
	public List<Field> getSerializableFields() {
		List<Field> typeFields = new ArrayList<Field>();

		ReflectionUtils.getAllFields(typeFields, getClass());

		List<Field> toStore = new ArrayList<Field>();
		for (Field field : typeFields) {
			if ((!field.getName().equals("id")
					&& !field.isAnnotationPresent(com.orm.dsl.Ignore.class)
					&& !field.isAnnotationPresent(Ignore.class)
					&& !field.isAnnotationPresent(cl.niclabs.android.data.DoNotSerialize.class)
					&& !field.isAnnotationPresent(DoNotSerialize.class)
					&& !Modifier.isStatic(field.getModifiers()) 
					&& !Modifier.isTransient(field.getModifiers()))) {
				toStore.add(field);
			}
		}
		return toStore;
	}
}
