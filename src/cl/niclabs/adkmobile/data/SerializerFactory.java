package cl.niclabs.adkmobile.data;

/**
 * Construct elements of type Serializer, it is preferable to 
 * use this class instead of creating individual instances of Serializer
 * to encapsulate the implementation of the writer
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 */
public final class SerializerFactory {
	private static SerializerFactory self;
	
	private SerializerFactory() {
	}
	
	/**
	 * Get an instance of this factory
	 * @return
	 */
	public static SerializerFactory getInstance() {
		if (self == null) {
			self = new SerializerFactory();
		}
		return self;
	}
	
	/**
	 * Get an instance of a DataObjectWriter
	 * @return
	 */
	public Serializer getSerializer() {
		return new JsonSerializer();
	}
}
