package cl.niclabs.adkmobile.test.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import cl.niclabs.adkmobile.data.AbstractSerializable;
import cl.niclabs.adkmobile.data.DoNotSerialize;
import cl.niclabs.adkmobile.data.JsonSerializer;

/**
 * Tests Json serialization and deserialization methods
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 */
public class JsonSerializerTest extends TestCase {	
	protected static class Tag extends AbstractSerializable<Tag> {
		String name;
		
		public Tag() {
		}
		
		public Tag(String value) {
			this.name = value;
		}
	}
	
	protected static class Reader {
		int id;
		
		public Reader() {
			
		}
		
		public Reader(int id) {
			this.id = id;
		}
	}
	
	protected static class Post extends AbstractSerializable<Post> {
		String title;
		Long visits;
		boolean read;
		double rate;
		
		Date date;
		
		int integer;
		
		@DoNotSerialize
		String body;
		
		List<Tag> tags;
		
		List<Integer> readers;
		
		List<Reader> readerObjects; 
		
		@DoNotSerialize
		List<Integer> values;
		
		public Post() {
			
		}
		
		public Post(String title, String body) {
			this.title = title;
			this.body = body;
		}
	}
	
	private static final String TAG = "AdkintunMobile::JsonTest";
	
	/**
	 * Test object serialization
	 * 
	 * @throws IOException
	 * @throws JSONException 
	 */
	public void testSerializationAndDeserialization() throws IOException, JSONException {
		JsonSerializer serializer = new JsonSerializer();
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		
		String title = "title", body = "this is the body", tag1 = "new", tag2 = "info";
		Long visits = 2147483648912L;
		double rate = 0.54;
		boolean read = false;
		List<Integer> readers = Arrays.asList(new Integer[]{1,2,3});
		Date now = new Date();
		
		List<Tag> tags = new ArrayList<Tag>();
		
		// Add elements
		tags.add(new Tag(tag1));
		tags.add(new Tag(tag2));
		
		
		List<Reader> readerObjects = new ArrayList<Reader>();
		readerObjects.add(new Reader(1));
		readerObjects.add(new Reader(2));
		
		// Create the object to serialize
		Post object = new Post(title, body);
		object.visits = visits;
		object.read = read;
		object.rate = rate;
		object.tags = tags;
		object.readers = readers;
		object.readerObjects = readerObjects;
		object.date = now;
		
		// Serialize the object to a byte array
		serializer.serialize(output, object);
		
		// Obtain the result
		String json = new String(output.toByteArray());
		
		Log.d(TAG, "Serialized Object: " + json);
		
		// Deserialize the object
		JSONObject testObject = new JSONObject(json);
		
		assertEquals(title, testObject.get("title"));
		assertEquals(visits, testObject.get("visits"));
		assertEquals(read, testObject.get("read"));
		assertEquals(rate, testObject.get("rate"));
		assertTrue("Serialized date must equal now timestamp", now.getTime() == testObject.getLong("date"));
		
		
		// The body should not have been serialized
		assertTrue(!testObject.has("body"));
		
		JSONArray tagsArray = testObject.getJSONArray("tags");
		
		assertEquals(2, tagsArray.length());
		assertEquals(tag1, tagsArray.getJSONObject(0).get("name"));
		assertEquals(tag2, tagsArray.getJSONObject(1).get("name"));
		
		JSONArray readersArray = testObject.getJSONArray("readers");
		assertEquals(readers.size(), readersArray.length());
		for (int i = 0; i < readers.size(); i++) {
			assertEquals(readers.get(i), readersArray.get(i));
		}
		
		// Test deserialization
		Post deserializedPost = serializer.deserialize(Post.class, json);
		
		Log.d(TAG, "DeSerialized Object: " + deserializedPost);
		
		// Result must not be null
		assertNotSame(null, deserializedPost);
		
		assertEquals(title, deserializedPost.title);
		assertEquals(visits, deserializedPost.visits);
		assertEquals(read, deserializedPost.read);
		assertEquals(rate, deserializedPost.rate);
		
		assertNotSame(null, deserializedPost.tags);
		
		assertTrue("Deserialized date must not be null", deserializedPost.date != null);
		assertTrue("Deserialized date must equal the original date timestamp", deserializedPost.date.getTime() == now.getTime());
		
		assertEquals(2, deserializedPost.tags.size());
		assertEquals(tag1, deserializedPost.tags.get(0).name);
		assertEquals(tag2, deserializedPost.tags.get(1).name);
		
		// Check that the readers got deserialized
		assertNotSame(null, deserializedPost.readers);
		assertEquals(readers.size(), deserializedPost.readers.size());
		for (int i = 0; i < readers.size(); i++) {
			assertEquals(readers.get(i), deserializedPost.readers.get(i));
		}
		
		assertEquals(null, deserializedPost.readerObjects);
	}
	
	public void testDeserialize() throws IOException {
		String json = "{\"title\":\"this is the title\", \"extra\":{\"value\":1},\"visits\":100}";
		
		JsonSerializer s = new JsonSerializer();
		Post post = s.deserialize(Post.class, json);
		
		assertEquals("this is the title", post.title);
		assertEquals(new Long(100L), post.visits);
	}
	
	
	public void testDeserialize2() throws IOException {
		String json = "{\"title\":\"this is the title\", \"values\":[1,2,3],\"visits\":100}";
		
		JsonSerializer s = new JsonSerializer();
		Post post = s.deserialize(Post.class, json);
		
		assertEquals("this is the title", post.title);
		assertEquals(null, post.values);
		assertEquals(new Long(100), post.visits);
	}
	
	public void testDeserialize3() throws IOException {
		String json = "{\"title\":null, \"values\":[1,2,3],\"visits\":0}";
		
		JsonSerializer s = new JsonSerializer();
		Post post = s.deserialize(Post.class, json);
		
		assertEquals(null, post.title);
		assertEquals(null, post.values);
		assertEquals(new Long(0), post.visits);
	}
	
	public void testDeserializeWronglyDefinedElement() throws IOException {
		String json = "{\"tags\":{\"name\":\"tagname\"}, \"values\":[1,2,3],\"visits\":0}";
		
		JsonSerializer s = new JsonSerializer();
		try {
			Post post = s.deserialize(Post.class, json);
			
			assertTrue("Deserializer should not fail", true);
			assertTrue("Post must be null", post == null);
		}
		catch (IllegalStateException e) {
			assertTrue("Deserializer should not fail", false);
		}
	}
}
