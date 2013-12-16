package cl.niclabs.adkmobile.data;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

/**
 * Define the methods and behavior that must be implemented by all serializers 
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 */
public interface Serializer {
	/**
	 * Write a serializable object to the specified output stream
	 * @param out
	 * @param object
	 */
	public void serialize(OutputStream out, Serializable<?> object) throws IOException;

	/**
	 * Write a serializable list to the specified output stream
	 * @param out
	 * @param list
	 */
	public void serialize(OutputStream out, List<Serializable<?>> list) throws IOException;
	
	/**
	 * Write a serializable iterator to the specified output stream
	 * @param out
	 * @param iterator
	 * @throws IOException
	 */
	public void serialize(OutputStream out, Iterator<Serializable<?>> iterator) throws IOException;
}
