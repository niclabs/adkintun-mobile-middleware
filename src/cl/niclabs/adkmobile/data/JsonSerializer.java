package cl.niclabs.adkmobile.data;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import android.util.Base64;

import com.google.gson.stream.JsonWriter;
import com.orm.StringUtil;


/**
 * Serializes an object or a list to JSON.
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
 * 
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 * 
 */
public class JsonSerializer implements Serializer {
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
}
