package cl.niclabs.adkmobile.monitor.data;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import android.content.ContentValues;

public class ContentValuesDataObject extends DataObject {
	private ContentValues contentValues;
	private Set<String> fieldNames;
	
	public ContentValuesDataObject() {
		contentValues = new ContentValues();
		fieldNames = new HashSet<String>();
	}

	@Override
	public boolean getBoolean(String fieldName) {
		Boolean result = contentValues.getAsBoolean(fieldName);
		if (result != null) {
			return result.booleanValue();
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
		return contentValues.getAsString(fieldName);
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
}
