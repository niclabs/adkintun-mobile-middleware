package cl.niclabs.adkmobile.data;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public interface DataObjectWriter {
	/**
	 * Write a data object to the specified output stream, closing the stream afterwards
	 * @param out
	 * @param object
	 */
	public void writeDataObject(OutputStream out, DataObject object) throws IOException;
	
	/**
	 * Write a data object list to the specified output stream, closing the stream afterwards
	 * @param out
	 * @param list
	 */
	public void writeList(OutputStream out, List<DataObject> list) throws IOException;
}
