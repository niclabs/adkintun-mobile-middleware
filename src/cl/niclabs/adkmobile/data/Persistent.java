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
 * Abstraction for all persistent objects.
 * 
 * This class currently depends of the Sugar library
 * https://github.com/niclabs/sugar However, keep in mind that the underlying
 * implementation may change, therefore it is highly discouraged to use any of
 * the superclass methods.
 * 
 * In order to enable persistance in the application using the library, it is
 * necessary to add the class {@link cl.niclabs.adkmobile.AdkintunMobileApp} to
 * the <code>android:name</code> argument in the manifest of the implementing
 * class.
 * 
 * In order to create persistent objects, it suffices with extending this class.
 * For instance, the following code creates a Note entity that is persistable in
 * a database
 * 
 * <code>
 * class Note {
 * 		public String title;
 * 		public String body;
 * 
 * 		// The default constructor is mandatory
 * 		public Note() {}
 * }
 * </code>
 * 
 * In order to save the note <code>
 * Note note = new Note();
 * note.title = "Hello";
 * note.body =  "Hello world!!";
 * 
 * // Saves the note to storage
 * note.save();
 * </code>
 * 
 * To find all the notes in storage
 * 
 * <code>
 * Iterator<Note> notes = Note.findAll(Note.class);
 * while (notes.hasNext()) {
 * 		Note n = notes.next();
 * 		Log.d("Example", n.title);
 * }
 * </code>
 * 
 * If there is a schema change in the db over different releases of an app, to
 * upgrade the database without losing data, you have to create a folder named
 * sugar_upgrades in your assets folder, then create a file named <version>.sql
 * wich corresponds to the database version eg 1.sql, 2.sql. this file would
 * contain all the create/update/alter/etc queries for that particular version.
 * finally change the version metadata field in AndroidManifest.xml to the
 * appropiate version
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 * 
 * @param <E>
 *            object that is desired to make persistable
 */
public class Persistent<E extends Persistent<E>> extends SugarRecord<E>
		implements Serializable<E> {
	/**
	 * IMPORTANT: Extending methods must add the default constructor in order to
	 * create the table associated with the Persitent object
	 */
	public Persistent() {
		super();
	}

	@Override
	public void delete() {
		super.delete();
	}

	/**
	 * Delete all records for the persistent type
	 * 
	 * @param type
	 */
	public static <T extends Persistent<?>> void deleteAll(Class<T> type) {
		SugarRecord.deleteAll(type);
	}

	/**
	 * Delete all records that fulfill the where condition
	 * 
	 * @param type
	 * @param whereClause
	 * @param whereArgs
	 */
	public static <T extends Persistent<?>> void deleteAll(Class<T> type,
			String whereClause, String... whereArgs) {
		SugarRecord.deleteAll(type, whereClause, whereArgs);
	}

	@Override
	public Long getId() {
		return super.getId();
	}

	/**
	 * Find an instance of the object of type `type` with the specified id
	 * 
	 * @param type
	 * @param id
	 * @return an instance of the object with the specified id, or null if not
	 *         found
	 */
	public static <T extends Persistent<T>> T findById(Class<T> type, Long id) {
		return SugarRecord.findById(type, id);
	}

	/**
	 * Find all the objects that match the specified criteria
	 * 
	 * @param type
	 * @param whereClause
	 * @param whereArgs
	 * @return
	 */
	public static <T extends Persistent<T>> Iterator<T> find(Class<T> type,
			String whereClause, String... whereArgs) {
		return SugarRecord.findAsIterator(type, whereClause, whereArgs);
	}

	/**
	 * Find all the objects that match the specified criteria
	 * 
	 * @param type
	 * @param whereClause
	 * @param whereArgs
	 * @return
	 */
	public static <T extends Persistent<T>> Iterator<T> find(Class<T> type,
			String whereClause, String[] whereArgs, String orderBy) {
		return SugarRecord.findAsIterator(type, whereClause, whereArgs, null,
				orderBy, null);
	}

	/**
	 * Find all the objects for the specified type
	 * 
	 * @param type
	 * @return
	 */
	public static <T extends Persistent<T>> Iterator<T> findAll(Class<T> type) {
		return SugarRecord.findAll(type);
	}

	/**
	 * Save the object to the database
	 * @return id of the recently saved record
	 */
	@Override
	public long save() {
		return super.save();
	}

	/**
	 * Return the list of fields of the object
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
					.isAnnotationPresent(DoNotSerialize.class))
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
				Type listType = ((ParameterizedType) genericType)
						.getActualTypeArguments()[0];
				if (type.isAssignableFrom((Class<?>) listType)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public List<Field> getTableFields() {
		List<Field> fieldList = SugarConfig.getFields(getClass());
		if (fieldList != null)
			return fieldList;

		List<Field> typeFields = new ArrayList<Field>();

		getAllFields(typeFields, getClass());

		List<Field> toStore = new ArrayList<Field>();
		for (Field field : typeFields) {
			// Ignore Persistent lists as well
			if (!field.isAnnotationPresent(Ignore.class)
					&& !isListOf(Persistent.class, field)) {
				toStore.add(field);
			}
		}

		SugarConfig.setFields(getClass(), toStore);
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
}
