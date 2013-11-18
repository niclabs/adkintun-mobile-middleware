package cl.niclabs.adkmobile.data;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

public interface Serializer {
	/**
	 * Write a serializable object to the specified output stream, closing the stream afterwards
	 * @param out
	 * @param object
	 */
	public void serialize(OutputStream out, Serializable<?> object) throws IOException;

	/**
	 * Write a serializable list to the specified output stream, closing the stream afterwards
	 * @param out
	 * @param list
	 */
	public void serialize(OutputStream out, List<Serializable<?>> list) throws IOException;
	
	/**
	 * Write a serializable iterator to the specified output stream, closing the stream afterwards
	 * @param out
	 * @param iterator
	 * @throws IOException
	 */
	public void serialize(OutputStream out, Iterator<Serializable<?>> iterator) throws IOException;
}