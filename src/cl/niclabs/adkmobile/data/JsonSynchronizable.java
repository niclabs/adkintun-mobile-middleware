package cl.niclabs.adkmobile.data;

import java.io.IOException;
import java.io.OutputStream;

import com.google.gson.stream.JsonWriter;

public abstract class JsonSynchronizable implements Synchronizable {
	/**
	 * Write the provided object to the specified OutputStream
	 * 
	 * @param out
	 * @param object
	 * @throws IOException
	 */
	protected void writeObject(OutputStream out, DataObject object) throws IOException {
		JsonWriter writer = JsonHelper.writeDataObject(out, object);
		writer.close();
	}
}
