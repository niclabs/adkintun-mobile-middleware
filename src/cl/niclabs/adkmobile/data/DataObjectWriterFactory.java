package cl.niclabs.adkmobile.data;

/**
 * Construct elements of type DataObjectWriter, it is preferrable to 
 * use this class instead of creating individual instances of DataObjectWriter
 * to encapsulate the implementation of the writer
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 */
public final class DataObjectWriterFactory {
	private static DataObjectWriterFactory self;
	
	private DataObjectWriterFactory() {
	}
	
	/**
	 * Get an instance of this factory
	 * @return
	 */
	public static DataObjectWriterFactory getInstance() {
		if (self == null) {
			self = new DataObjectWriterFactory();
		}
		return self;
	}
	
	/**
	 * Get an instance of a DataObjectWriter
	 * @return
	 */
	public DataObjectWriter getDataObjectWriter() {
		return new JsonDataObjectWriter();
	}
}
