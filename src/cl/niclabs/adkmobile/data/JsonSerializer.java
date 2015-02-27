package cl.niclabs.adkmobile.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

/**
 * Serializes/deserializes an object or a list to/from JSON.
 * 
 * This is a stub class for backwards compatibility
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 * @deprecated use cl.niclabs.android.data.JsonSerializer
 */
public class JsonSerializer implements Serializer {

	@Override
	public <E extends Serializable<?>> E deserialize(Class<E> cls,
			InputStream in) throws IOException {
		return new cl.niclabs.android.data.JsonSerializer().deserialize(cls, in);
	}

	@Override
	public <E extends Serializable<?>> E deserialize(Class<E> cls, String input)
			throws IOException {
		return new cl.niclabs.android.data.JsonSerializer().deserialize(cls, input);
	}

	@Override
	public void serialize(OutputStream out, Serializable<?> object)
			throws IOException {
		new cl.niclabs.android.data.JsonSerializer().serialize(out, object);
	}

	@Override
	public void serialize(OutputStream out, List<Serializable<?>> list)
			throws IOException {
		serialize(out, list.iterator());
	}

	@Override
	public void serialize(OutputStream out, Iterator<Serializable<?>> iterator)
			throws IOException {	
		cl.niclabs.android.data.JsonSerializer serializer = new cl.niclabs.android.data.JsonSerializer();
		while (iterator.hasNext()) {
			Serializable<?> object = iterator.next();
			serializer.serialize(out, object);
		}
	}
}
