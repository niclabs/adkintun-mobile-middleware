package cl.niclabs.adkmobile.monitor.data;

import java.lang.reflect.Field;
import java.util.List;

import cl.niclabs.android.data.DoNotSerialize;
import cl.niclabs.android.data.Persistent;

/**
 * Mantain statistics on a sample  
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 */
public class Sample extends Persistent<Sample> {
	private int size;
	private double mean;
	private double variance;
	
	public Sample() {
		size = 0;
		mean = 0.0;
		variance = 0.0;
	}
	
	public void update(double x) {
		size = 1;
		mean = x;
	}
	
	@Override
	public List<Field> getSerializableFields() {
		List<Field> fields = super.getSerializableFields();
		
		int index = 0;
		for (Field field: fields) {
			if (field.getName().equalsIgnoreCase("id")) {
				fields.remove(index);
				break;
			}
			index++;
		}
		
		return fields;
	}

	/**
	 * Get the mean of the sample
	 * @return
	 */
	public double mean() {
		return mean;
	}
	
	/**
	 * Get the variance of the sample
	 * @return
	 */
	public double variance() {
		return variance;
	}
	
	/**
	 * Return sample size
	 * @return
	 */
	public int size() {
		return size;
	}
}
