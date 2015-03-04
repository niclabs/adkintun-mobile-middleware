package cl.niclabs.adkmobile.data;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Allow Serializable objects to exclude fields from the serialization  
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 * @deprecated use cl.niclabs.android.data.DoNotSerialize instead
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DoNotSerialize {}
