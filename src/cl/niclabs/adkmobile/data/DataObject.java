package cl.niclabs.adkmobile.data;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;

import android.util.Log;

public abstract class DataObject {
	public final String TAG = "AdkintunMobile::DataObject";
	
	/**
	 * Get the value of the object with fieldName
	 * 
	 * @param fieldName
	 * @return the value of the object or null if the object does not exist
	 */
	public abstract Object get(String fieldName);
	
	/**
	 * Get the value of the element with the specified fieldName as a boolean. 
	 * 
	 * @param fieldName
	 * @return
	 */
	public abstract boolean getBoolean(String fieldName);
	
	/**
	 * Get the value of the element with the specified fieldName as a byte array. 
	 * 
	 * @param fieldName
	 * @return
	 */
	public abstract byte[] getByteArray(String fieldName);
	
	/**
	 * Get the value of the element with the specified fieldName as a double 
	 * 
	 * 
	 * @param fieldName
	 * @return
	 */
	public abstract double getDouble(String fieldName);
	
	/**
	 * Get an iterator for the list of field names
	 * @return an iterator for the list of field names
	 */
	public abstract Iterator<String> getFieldNames();
	
	/**
	 * Get the value of the element with the specified fieldName as a float.
	 * 
	 * @param fieldName
	 * @return
	 */
	public abstract float getFloat(String fieldName);
	
	/**
	 * Get the value of the element with the specified fieldName as an int. s
	 * 
	 * @param fieldName
	 * @return
	 */
	public abstract int getInt(String fieldName);
	
	/**
	 * Get the value of the element with the specified fieldName as a long. 
	 * 
	 * @param fieldName
	 * @return
	 */
	public abstract long getLong(String fieldName);
	
	/**
	 * Get the value of the element with the specified fieldName as a String. 
	 * 
	 * @param fieldName
	 * @return
	 */
	public abstract String getString(String fieldName);
	
	/**
	 * Get the value of the element with the specified fielName as a List of DataObjects.
	 *
	 * @param fieldName
	 * @return
	 */
	public abstract List<DataObject> getList(String fieldName);
	
    /**
     * Set the field with name fieldName to the specified boolean value 
     * @param fieldName
     * @param v
     */
	public abstract void put(String fieldName, boolean v);
	
	/**
	 * Set the field with name fieldName to the specified byte array value
	 * @param fieldName
	 * @param v
	 */
	public abstract void put(String fieldName, byte[] v); 
	
	/**
	 * Set the field with name fieldName to the specified double value
	 * @param fieldName
	 * @param v
	 */
	public abstract void put(String fieldName, double v); 
    
	/**
	 * Set the field with name fieldName to the specified float value
	 * @param fieldName
	 * @param v
	 */
	public abstract void put(String fieldName, float v); 
    
	/**
	 * Set the field with name fieldName to the specified int value
	 * @param fieldName
	 * @param v
	 */
	public abstract void put(String fieldName, int v);
     
	/**
	 * Set the field with name fieldName to the specified long value
	 * @param fieldName
	 * @param v
	 */
	public abstract void put(String fieldName, long v); 
	
	/**
	 * Set the field with name fieldName to the specified list value.
	 *
	 * @param fieldname
	 * @param v
	 */
	public abstract void put(String fieldName, List<DataObject> v);
	
	/**
	 * Set the field with name fieldName to the specified String value
	 * @param fieldName
	 * @param v
	 */
	public abstract void put(String fieldName, String v); 
	
	/**
	 * Write the object to the specified string buffer
	 * @param b
	 */
	protected void toString(StringBuffer b) {
		Iterator<String> fieldNames = getFieldNames();
		b.append('(');
		while (fieldNames.hasNext()) {
			String name = fieldNames.next();
			b.append(name);
			b.append('=');
			b.append(getString(name)); // TODO: depends on the implementation

			if (fieldNames.hasNext()) {
				b.append(',');
			}
		}
		b.append(')');
	}
	
	public String toString() {
		StringBuffer b = new StringBuffer();
		toString(b);
		return b.toString();
	}
	
	/**
	 * Write JSON to OutputStrem
	 */
	public void writeObject(OutputStream out) {
		try {
			DataObjectWriterFactory.getInstance().getDataObjectWriter()
					.writeDataObject(out, this);
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
	}
}
