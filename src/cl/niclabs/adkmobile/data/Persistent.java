package cl.niclabs.adkmobile.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.orm.SugarConfig;
import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

/**
 * Abstraction for all persistent objects. Keep in mind that the underlying implementation may
 * change, therefore it is not advisable to use any of the superclass methods.
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 *
 * @param <E> persistent object 
 */
public class Persistent<E extends Persistent<E>> extends SugarRecord<E> implements Serializable<E> {
	public Persistent() {
		super();
	}
		
	@Override
	public Long getId() {
		return super.getId();
	}

	/**
	 * Find an instance of the object of type `type` with the specified id
	 * @param type
	 * @param id
	 * @return an instance of the object with the specified id, or null if not found
	 */
	public static <T extends Persistent<T>> T findById(Class<T> type, Long id) {
		return SugarRecord.findById(type, id);
	}
	
	/**
	 * Find all the objects that match the specified criteria
	 * @param type
	 * @param whereClause
	 * @param whereArgs
	 * @return
	 */
	public static <T extends Persistent<T>> Iterator<T> find(Class<T> type, String whereClause, String... whereArgs) {
		return SugarRecord.findAsIterator(type, whereClause, whereArgs);
	} 
	
	/**
	 * Find all the objects for the specified type
	 * @param type
	 * @return
	 */
	public static <T extends Persistent<T>> Iterator<T> findAll(Class<T> type) {
		return SugarRecord.findAll(type);
	}
	
	/**
	 * Save the object to the database
	 */
	@Override
	public void save() {
		super.save();
	}
	
	/**
	 * Return the list of fields of the object
	 * @return
	 */
	@Override
	public List<Field> getSerializableFields() {
        List<Field> typeFields = new ArrayList<Field>();

        getAllFields(typeFields, getClass());

        List<Field> toStore = new ArrayList<Field>();
        for (Field field : typeFields) {
			if ((!field.isAnnotationPresent(Ignore.class)
					&& !field.isAnnotationPresent(DoNotSerialize.class))
					|| isListOf(Persistent.class, field)) {
                toStore.add(field);
            }
        }
        return toStore;
	}
	
	private boolean isListOf(Class<?> type, Field field) {
		if (List.class.isAssignableFrom(field.getType())) {
         	Type genericType = field.getGenericType();
         	if (genericType instanceof ParameterizedType) {
         		Type listType = ((ParameterizedType) genericType).getActualTypeArguments()[0];
         		if (type.isAssignableFrom((Class<?>)listType)) {
         			return true;
         		}
         	}
		}
		return false;
	}
	
	@Override
	public List<Field> getTableFields() {
        List<Field> fieldList = SugarConfig.getFields(getClass());
        if(fieldList != null) return fieldList;

        List<Field> typeFields = new ArrayList<Field>();

        getAllFields(typeFields, getClass());

        List<Field> toStore = new ArrayList<Field>();
        for (Field field : typeFields) {
        	// Ignore Persistent lists as well
            if (!field.isAnnotationPresent(Ignore.class) && !isListOf(Persistent.class, field)) {
                toStore.add(field);
            }
        }

        SugarConfig.setFields(getClass(), toStore);
        return toStore;
    }
	
	/**
	 * Get all the fields for the class, including those of the super classes
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
		} catch (IOException e) {
		}
		return out.toString();
	}
}
