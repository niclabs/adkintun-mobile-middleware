package cl.niclabs.adkmobile.monitor.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.ContentValues;

public class ContentValuesDataObject extends DataObject {
	private ContentValues contentValues;
	private Set<String> fieldNames;
	private Map<String, List<DataObject>> listElements;
	
	public ContentValuesDataObject() {
		contentValues = new ContentValues();
		fieldNames = new HashSet<String>();
		listElements = new HashMap<String,List<DataObject>>();
	}

	@Override
	public Object get(String fieldName) {
		Object obj = contentValues.get(fieldName);
		if (obj != null) {
			return obj;
		}
		return listElements.get(fieldName);
	}
	
	@Override
	public boolean getBoolean(String fieldName) {
		Boolean result = contentValues.getAsBoolean(fieldName);
		if (result != null) {
			return result;
		}
		return false;
	}

	@Override
	public byte[] getByteArray(String fieldName) {
		return contentValues.getAsByteArray(fieldName);
	}

	@Override
	public double getDouble(String fieldName) {
		Double result = contentValues.getAsDouble(fieldName);
		if (result != null) {
			return result.doubleValue();
		}
		return 0.0;
	}

	@Override
	public Iterator<String> getFieldNames() {
		return fieldNames.iterator();
	}

	@Override
	public float getFloat(String fieldName) {
		Float result = contentValues.getAsFloat(fieldName);
		if (result != null) {
			return result.floatValue();
		}
		return 0.0f;
	}

	@Override
	public int getInt(String fieldName) {
		Integer result = contentValues.getAsInteger(fieldName);
		if (result != null) {
			return result.intValue();
		}
		return 0;
	}

	@Override
	public long getLong(String fieldName) {
		Long result = contentValues.getAsLong(fieldName);
		if (result != null) {
			return result.longValue();
		}
		return 0L;
	}

	@Override
	public String getString(String fieldName) {
		String value = contentValues.getAsString(fieldName);
		
		/*
		 * This is here to be consistent with content values implementation. In
		 * ContentValues, even if the fieldName is of type long,
		 * getString(fieldName) will return the string representation. The same
		 * has to occur with the list.
		 */
		if (value == null && listElements.containsKey(fieldName)) {
			StringBuffer b = new StringBuffer();
			Iterator<DataObject> iterator = listElements.get(fieldName).iterator();
			b.append('[');
			while (iterator.hasNext()) {
				DataObject obj = iterator.next();
				obj.toString(b);
				if (iterator.hasNext()){
					b.append(',');
				}
			}
			b.append(']');
			value = b.toString();
		}
		
		return value;
	}
	
	@Override
	public List<DataObject> getList(String fieldName) {
		return listElements.get(fieldName);
	}

	@Override
	public void put(String fieldName, boolean v) {
		fieldNames.add(fieldName);
		contentValues.put(fieldName, v);
	}

	@Override
	public void put(String fieldName, byte[] v) {
		fieldNames.add(fieldName);
		contentValues.put(fieldName, v);
	}

	@Override
	public void put(String fieldName, double v) {
		fieldNames.add(fieldName);
		contentValues.put(fieldName, v);
	}

	@Override
	public void put(String fieldName, float v) {
		fieldNames.add(fieldName);
		contentValues.put(fieldName, v);
	}

	@Override
	public void put(String fieldName, int v) {
		fieldNames.add(fieldName);
		contentValues.put(fieldName, v);
	}

	@Override
	public void put(String fieldName, long v) {
		fieldNames.add(fieldName);
		contentValues.put(fieldName, v);
	}

	@Override
	public void put(String fieldName, String v) {
		fieldNames.add(fieldName);
		contentValues.put(fieldName, v);
	}

	@Override
	public void put(String fieldName, List<DataObject> v) {
		// TODO Auto-generated method stub.
		fieldNames.add(fieldName);
		listElements.put(fieldName, v);
	}

}
