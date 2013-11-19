package cl.niclabs.adkmobile.data;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Allow Serializable objects to exclude fields from the serialization  
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DoNotSerialize {
}
