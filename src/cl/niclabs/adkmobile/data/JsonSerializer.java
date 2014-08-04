package cl.niclabs.adkmobile.data;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import android.util.Base64;
import cl.niclabs.adkmobile.utils.StringUtil;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;


/**
 * Serializes/deserializes an object or a list to/from JSON.
 * 
 * Note that this is a single usage serializer and two consecutive calls to an
 * open stream will render invalid JSON
 * 
 * For example the code below will render
 * <code>{<contentes of object1>}{<contents of object2>}</code> which will be
 * parsed correctly as JSON <code>
 * JsonSerializer serializer = new JsonSerializer();
 * OutputStream out = new FileOutputStream(new File("file.json"));
 * serializer.serialize(out, object1);
 * serializer.serialize(out, object2);
 * </code>
 * 
 * For deserialization, given an object
 * 
 * <code>
 * public class Post extends AbstractSeriablizable<Post> {
 * 		String title;
 * 		String body;
 *
 *		// Empty constructor is required 
 *		public Post() {}
 * }
 * </code>
 * 
 * Deserialization is performed the following way
 * 
 * <code>
 * JsonSerializer serializer = new JsonSerializer();
 * Post p = serializer.deserialize(Post.class, "{\"title\":\"this is the title\",\"body\":\"this is the body\"}");
 * 
 * Log.i(TAG, p.title); // Prints 'this is the title'
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 */
public class JsonSerializer implements Serializer {
	/**
	 * GSON adapter to parse Serializable objects
	 * @author Felipe Lalanne <flalanne@niclabs.cl>
	 *
	 * @param <E>
	 */
	protected class SerializableTypeAdapter<E extends Serializable<?>> extends TypeAdapter<E> {
		private Class<E> cls;
		
		public SerializableTypeAdapter(Class<E> cls) {
			this.cls = cls;
		}
		
		@Override
		public E read(JsonReader reader) throws IOException {
			try {
				// Asume is an object
				return readObject(reader);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} 
			
			return null;
		}
		
		/**
		 * Read a list from the specified reader
		 * @param reader
		 * @return
		 * @throws IOException
		 */
		public List<E> readList(JsonReader reader) throws IOException {
			reader.beginArray();
			
			List<E> list = new ArrayList<E>();
			
			while (reader.hasNext()) {
				try {
					list.add(readObject(reader));
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			
			reader.endArray();
			
			return list;
		}
		
		/**
		 * Read a list of objects the specified reader
		 * @param reader
		 * @return
		 * @throws IOException
		 */
		protected List<?> readPrimitiveDataTypeList(Class<?> type, JsonReader reader) throws IOException {
			List<Object> list = null;
			
			reader.beginArray();
			if ((type.equals(Integer.class)) || (type.equals(Integer.TYPE))) {
				list = new ArrayList<Object>();
				while (reader.hasNext()) {
					list.add(reader.nextInt());
				}
			}
			else if ((type.equals(Long.class)) ||
	                (type.equals(Long.TYPE))) {
				list = new ArrayList<Object>();
				while (reader.hasNext()) {
					list.add(reader.nextLong());
				}
			}
			else if ((type.equals(Double.class)) ||
	                (type.equals(Double.TYPE)) ||
	                (type.equals(Float.class)) ||
	                (type.equals(Float.TYPE))) {
				
				list = new ArrayList<Object>();
				while (reader.hasNext()) {
					list.add(reader.nextDouble());
				}
			}
			else if (type.equals(Boolean.class)
					|| type.equals(boolean.class)) {
				
				list = new ArrayList<Object>();
				while (reader.hasNext()) {
					list.add(reader.nextBoolean());
				}
			} 
			else if (type.getName().equals("[B")) {
				list = new ArrayList<Object>();
				while (reader.hasNext()) {
					list.add(Base64.decode(reader.nextString(), Base64.DEFAULT));
				}
			} 
			else if (type.equals(String.class)) {
				list = new ArrayList<Object>();
				while (reader.hasNext()) {
					list.add(reader.nextString());
				}
			}
			else {
				// Read until reaching the end of the array
				while (reader.hasNext()) {
					reader.nextString();
				}
			}
			reader.endArray();
			
			return list;
		}
		
		/**
		 * Read an object from the specified reader
		 * @param reader
		 * @return
		 * @throws IOException
		 * @throws InstantiationException
		 * @throws IllegalAccessException
		 */
		@SuppressWarnings("unchecked")
		public E readObject(JsonReader reader) throws IOException, InstantiationException, IllegalAccessException {
			reader.beginObject();
			
			// Create a new instance of the object. Requires that all serializable objects have an empty constructor
			E obj = cls.newInstance();
			
			List<Field> fields = obj.getSerializableFields();
			
			while (reader.hasNext()) {
				String javaName = StringUtil.fromSQLName(reader.nextName());
				
				Field field;
				try {
					field = obj.getClass().getDeclaredField(javaName);
				} catch (NoSuchFieldException e) {
					reader.skipValue();
					continue;
				}
				
				if (fields.contains(field)) {
					Class<?> fieldType = field.getType();
					
					// Save accessibility for restoring later
					boolean accessible = field.isAccessible();
					
					// Change accessibility of the field
					field.setAccessible(true);
					
					if (Serializable.class.isAssignableFrom(fieldType)) {
						field.set(obj, self.deserialize(fieldType.asSubclass(Serializable.class), reader));
					}
					else if (List.class.isAssignableFrom(fieldType)) {
		             	Type genericType = field.getGenericType();
		             	if (genericType instanceof ParameterizedType) {
		             		Type listType = ((ParameterizedType) genericType).getActualTypeArguments()[0];
		             		if (Serializable.class.isAssignableFrom((Class<?>)listType)) {
		             			field.set(obj, self.deserializeList(((Class<?>)listType).asSubclass(Serializable.class), reader));
		             		}
		             		else {
		             			// Deserialize lists from basic types
		             			field.set(obj, readPrimitiveDataTypeList((Class<?>)listType, reader));
		             		}
		             	}
					}
					else if ((fieldType.equals(Integer.class)) || 
							 (fieldType.equals(Integer.TYPE))) {
						field.setInt(obj, reader.nextInt());
					}
					else if ((fieldType.equals(Long.class)) ||
			                (fieldType.equals(Long.TYPE))) {
						field.setLong(obj, reader.nextLong());
					}
					else if ((fieldType.equals(Double.class)) ||
			                (fieldType.equals(Double.TYPE)) ||
			                (fieldType.equals(Float.class)) ||
			                (fieldType.equals(Float.TYPE))) {
						
						field.setDouble(obj, reader.nextDouble());
					}
					else if (fieldType.equals(Boolean.class)
							|| fieldType.equals(boolean.class)) {
						field.setBoolean(obj, reader.nextBoolean());
					} 
					else if (fieldType.getName().equals("[B")) {
						field.set(obj, Base64.decode(reader.nextString(), Base64.DEFAULT));
					} 
					else if (fieldType.equals(String.class)) {
						field.set(obj, reader.nextString());
					}
					
					// Revert accessibility
					field.setAccessible(accessible);
				}
				else {
					reader.skipValue();
				}
			}
			reader.endObject();
			
			return obj;
		}

		@Override
		public void write(JsonWriter writer, E value) throws IOException {
			new JsonSerializer().serialize(writer, value);
		}	
	}
	
	private JsonSerializer self;
	
	public JsonSerializer() {
		self = this;
	}
	
	/**
	 * Read an object of the specified class from the provided input stream
	 * @param cls
	 * @param in
	 * @return
	 * @throws IOException
	 */
	@Override
	public <E extends Serializable<?>> E deserialize(Class<E> cls, InputStream in) throws IOException {
		JsonReader reader = new JsonReader(new InputStreamReader(in));
		return deserialize(cls, reader);
	}
	
	/**
	 * Read an object of the specified class from the provided json reader
	 * @param cls
	 * @param in
	 * @return
	 * @throws IOException
	 */
	protected <E extends Serializable<?>> E deserialize(Class<E> cls, JsonReader reader) throws IOException {
		return new SerializableTypeAdapter<E>(cls).read(reader);
	}
	
	/**
	 * Read an object of the specified class from the provided input string
	 * @param cls
	 * @param in
	 * @return
	 * @throws IOException
	 */
	@Override
	public <E extends Serializable<?>> E deserialize(Class<E> cls, String input) throws IOException {
		return deserialize(cls, new ByteArrayInputStream(input.getBytes()));
	}
	
	/**
	 * Read list of elements of the specified class from the provided json reader
	 * @param cls
	 * @param in
	 * @return
	 * @throws IOException
	 */
	protected <E extends Serializable<E>> List<E> deserializeList(Class<E> cls, JsonReader reader) throws IOException {
		return new SerializableTypeAdapter<E>(cls).readList(reader);
	}
	
	/**
	 * Write an iterator to the specified writer
	 * 
	 * @param writer
	 * @param list
	 * @throws IOException
	 */
	protected void serialize(JsonWriter writer, Iterator<Serializable<?>> iterator) throws IOException {
		writer.beginArray();
		while (iterator.hasNext()) {
			serialize(writer, iterator.next());
		}
		writer.endArray();		
	}
	
	
	/**
	 * Write an object to the specified JsonWriter
	 * 
	 * @param writer
	 * @param object
	 * @throws IOException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void serialize(JsonWriter writer, Serializable<?> object) throws IOException {
		writer.beginObject();
		for (Field field : object.getSerializableFields()) {
			try {
				field.setAccessible(true);
				//TODO: StringUtil depends of sugar. It would be advisable to remove it
				String fieldName = StringUtil.toSQLName(field.getName()).toLowerCase(Locale.US);
				Object fieldValue = field.get(object);
				Class<?> fieldType = field.getType();
				
				// Ignore null values
				if (fieldValue == null) {
					continue;
				}
				
				if (Serializable.class.isAssignableFrom(fieldType)) {
					writer.name(fieldName);
					
					serialize(writer, (Serializable)fieldValue);
				}
				else if (List.class.isAssignableFrom(fieldType)) {
	             	Type genericType = field.getGenericType();
	             	if (genericType instanceof ParameterizedType) {
	             		Type listType = ((ParameterizedType) genericType).getActualTypeArguments()[0];
	             		if (Serializable.class.isAssignableFrom((Class<?>)listType)) {
	             			// If it is a list of Serializable objects write the list
	             			writer.name(fieldName);
	             			serialize(writer, ((List) fieldValue).iterator());	             			
	             		}
	             		else {
	             			serialize((Class<?>)listType, fieldName, writer, (List)fieldValue);
	             		}
	             	}
				}
				else if ((Number.class.isAssignableFrom(fieldType)) ||
						(fieldType.equals(Integer.class)) ||
		                (fieldType.equals(Integer.TYPE)) ||
		                (fieldType.equals(Long.class)) ||
		                (fieldType.equals(Long.TYPE)) ||
		                (fieldType.equals(Double.class)) ||
		                (fieldType.equals(Double.TYPE)) ||
		                (fieldType.equals(Float.class)) ||
		                (fieldType.equals(Float.TYPE))) {
					writer.name(fieldName).value((Number) fieldValue);
				}
				else if (fieldType.equals(Boolean.class)
						|| fieldType.equals(boolean.class)) {
					writer.name(fieldName)
							.value((Boolean) fieldValue);
				} 
				else if (fieldType.getName().equals("[B")) {
					writer.name(fieldName).value(
							Base64.encodeToString((byte[]) fieldValue,
									Base64.NO_WRAP));
				} 
				else {
					writer.name(fieldName).value(fieldValue.toString());
				}
			} catch (IllegalAccessException e) {
			}
		}
		writer.endObject();
	}
	
	
	/**
	 * Serializes the list if the element type for the list (given by listType)
	 * is one of Number, Boolean, String, or byte array
	 * @param listType
	 * @param name
	 * @param writer
	 * @param list
	 * @throws IOException
	 */
	public void serialize(Class<?> listType, String name, JsonWriter writer, List<?> list) throws IOException {
		if ((Number.class.isAssignableFrom(listType)) ||
				(listType.equals(Integer.class)) ||
                (listType.equals(Integer.TYPE)) ||
                (listType.equals(Long.class)) ||
                (listType.equals(Long.TYPE)) ||
                (listType.equals(Double.class)) ||
                (listType.equals(Double.TYPE)) ||
                (listType.equals(Float.class)) ||
                (listType.equals(Float.TYPE))) {
			
			
			writer.name(name);
			writer.beginArray();
			for (Object o: list) {
				writer.value((Number)o);
			}
			writer.endArray();
		}
		else if (listType.equals(Boolean.class)
				|| listType.equals(boolean.class)) {
			
			writer.name(name);
			writer.beginArray();
			for (Object o: list) {
				writer.value((Boolean)o);
			}
			writer.endArray();
		} 
		else if (listType.getName().equals("[B")) {
			writer.name(name);
			writer.beginArray();
			for (Object o: list) {
				writer.value(Base64.encodeToString((byte[]) o,
						Base64.NO_WRAP));
			}
			writer.endArray();
		} 
		else {
			writer.name(name);
			writer.beginArray();
			for (Object o: list) {
				writer.value(o.toString());
			}
			writer.endArray();
		}
	}
	
	/**
	 * Open a JsonWriter and write the contents of the iterator. It is responsibility of the user to close
	 * the writer later
	 * 
	 * @param out
	 * @param list
	 * @return
	 * @throws IOException
	 */
	@Override
	public void serialize(OutputStream out, Iterator<Serializable<?>> iterator) throws IOException {
		JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
		serialize(writer, iterator);
		writer.flush();
	}
	
	/**
	 * Open a JsonWriter and write the object provided as input. It is responsibility of the user to close
	 * the writer later
	 * 
	 * @param out
	 * @param list
	 * @return
	 * @throws IOException
	 */
	@Override
	public void serialize(OutputStream out, List<Serializable<?>> list) throws IOException {
		JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
		serialize(writer, list.iterator());
		writer.flush();
	}
	
	/**
	 * Open a JsonWriter and write the object provided as input. It is responsibility of the user to close 
	 * the writer later.
	 * 
	 * @param out
	 * @param object
	 * @throws IOException
	 */
	@Override
	public void serialize(OutputStream out, Serializable<?> object) throws IOException {
		JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
		serialize(writer, object);
		writer.flush();
	}
}
