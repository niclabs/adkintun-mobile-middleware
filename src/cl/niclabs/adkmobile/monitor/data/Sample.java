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
	
	@DoNotSerialize
	private double kurtosis;
	
	@DoNotSerialize
	private double m2;
	
	@DoNotSerialize
	private double m3;
	
	@DoNotSerialize
	private double m4;
	
	public Sample() {
		size = 0;
		mean = 0.0;
		m2 = 0.0;
		m3 = 0.0;
		m4 = 0.0;
		variance = 0.0;
		kurtosis = 0.0;
	}
	
	/**
	 * Update the sample with a new value
	 * 
	 * Source http://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Online_algorithm
	 * 
	 * @param x
	 */
	public void update(double x) {
		int n1 = size;
		size = size + 1;
		
		double delta = x - mean;
		double delta_n = delta / size;
		double delta_n2 = delta_n * delta_n;
		double term1 = delta * delta_n * n1;
		
		mean = mean + delta_n;
		
		m4 = m4 + term1 * delta_n2 * (size*size - 3*size + 3) + 6 * delta_n2 * m2 - 4 * delta_n * m3;
		m3 = m3 + term1 * delta_n * (size - 2) - 3 * delta_n * m2;
		m2 = m2 + term1;
		
		if (m2*m2 > 3)
			kurtosis = (size * m4) / (m2 * m2) - 3;
		
		if (size > 1)
			variance = m2 / (size - 1);
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
	 * Return the kurtosis of the sample
	 * @return
	 */
	public double kurtosis() {
		return kurtosis;
	}
	
	/**
	 * Return sample size
	 * @return
	 */
	public int size() {
		return size;
	}
}
