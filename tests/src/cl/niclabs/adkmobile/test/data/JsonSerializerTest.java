package cl.niclabs.adkmobile.test.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

import cl.niclabs.adkmobile.data.AbstractSerializable;
import cl.niclabs.adkmobile.data.DoNotSerialize;
import cl.niclabs.adkmobile.data.JsonSerializer;

/**
 * Tests Json serialization and deserialization methods
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 */
public class JsonSerializerTest extends TestCase {	
	protected static class SerializableTestObject extends AbstractSerializable<SerializableTestObject> {
		String field1;
		int field2;
		boolean field3;
		
		@DoNotSerialize
		String field4;
		
		public SerializableTestObject(String field1, int field2, boolean field3) {
			super();
			this.field1 = field1;
			this.field2 = field2;
			this.field3 = field3;
		}
	}
	
	/**
	 * Test object serialization
	 * 
	 * @throws IOException
	 * @throws JSONException 
	 */
	public void testSerialization() throws IOException, JSONException {
		JsonSerializer serializer = new JsonSerializer();
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		
		String field1 = "hola";
		int field2 = 123;
		boolean field3 = false;
		String field4 = "chao";
		
		// Create the object to serialize
		SerializableTestObject object = new SerializableTestObject(field1, field2, field3);
		
		// Add the field4 that must not be serialized
		object.field4 = field4;
		
		// Serialize the object to a byte array
		serializer.serialize(output, object);
		
		// Obtain the result
		String result = new String(output.toByteArray());
		
		// Deserialize the object
		JSONObject resultObject = new JSONObject(result);
		
		assertEquals(field1, resultObject.get("field1"));
		assertEquals(field2, resultObject.get("field2"));
		assertEquals(field3, resultObject.get("field3"));
		assertTrue(!resultObject.has("field4"));
	}
}
