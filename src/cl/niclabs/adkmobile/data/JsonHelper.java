package cl.niclabs.adkmobile.data;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;

import android.util.Base64;

import com.google.gson.stream.JsonWriter;

public class JsonHelper {
	/**
	 * Open a JsonWriter and write the object provided as input. It is responsibility of the user to close 
	 * the writer later.
	 * 
	 * @param out
	 * @param object
	 * @return
	 * @throws IOException
	 */
	public static JsonWriter writeDataObject(OutputStream out, DataObject object) throws IOException {
		JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
		writeDataObject(writer, object);
		return writer;
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
	public static JsonWriter writeList(OutputStream out, List<DataObject> list) throws IOException {
		JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
		writeList(writer, list);
		return writer;
	}
	
	/**
	 * Write an object to the specified JsonWriter
	 * 
	 * @param writer
	 * @param object
	 * @throws IOException
	 */
	public static void writeDataObject(JsonWriter writer, DataObject object) throws IOException {
		writer.beginObject();
		for (Iterator<String> it = object.getFieldNames(); it.hasNext(); ) {
			String fieldName = it.next();
			Object obj = object.get(fieldName);
			if (obj instanceof List) {
				writer.name(fieldName);
				writeList(writer, object.getList(fieldName));
			}
			else if (obj instanceof Number) {
				writer.name(fieldName).value((Number)obj);
			}
			else if (obj instanceof Boolean) {
				writer.name(fieldName).value((Boolean)obj);
			}
			else if (obj instanceof byte[]) {
				writer.name(fieldName).value(Base64.encodeToString((byte [])obj, Base64.DEFAULT));
			}
			else {
				writer.name(fieldName).value(object.toString());
			}
		}
		writer.endObject();
	}
	
	/**
	 * Write a list to the specified writer
	 * 
	 * @param writer
	 * @param list
	 * @throws IOException
	 */
	public static void writeList(JsonWriter writer, List<DataObject> list) throws IOException {
		writer.beginArray();
		for (DataObject obj: list) {
			writeDataObject(writer, obj);
		}
		writer.endArray();		
	}
}
