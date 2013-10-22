package cl.niclabs.adkmobile.data;

import java.io.OutputStream;

/**
 * Defines the structure of all synchronizable objects.
 * 
 * A synchronizable object is any object that can be serialized 
 * into a standarized structured format (e.g. JSON, XML) for transmitting
 * through a network connection.
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 */
public interface Synchronizable {
	/**
	 * Writes the object in the output format onto the
	 * provided output stream, closing the stream afterwards
	 * @param out
	 */
	public void writeObject(OutputStream out);
}
