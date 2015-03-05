package cl.niclabs.adkmobile.net;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import android.util.Log;
import cl.niclabs.android.data.Serializable;
import cl.niclabs.android.data.Serializer;
import cl.niclabs.android.data.SerializerFactory;

/**
 * This class defines the methods to send serializable objects to a server.
 * 
 * Requires the permission android.permission.INTERNET
 *
 * @author Mauricio Castro.
 *         Created 09-10-2013.
 */
public class HttpUtils {
	protected static final String TAG = "AdkintunMobile::Http";
	
	protected static final String POST_FILE_NAME = "measurements.json.gz";
	protected static final String POST_FILE_TYPE = "application/x-gzip";
	protected static final String POST_FILE_FIELD = "measurements";
	
	public static final String CRLF = "\r\n";
	public static final String GET = "GET";
	public static final String POST = "POST";
	public static final String BOUNDARY = "a23bc126f";
	private static final String TWO_HYPHENS = "--";
	
	private HttpURLConnection connection;
	private DataOutputStream urlStream;
	private URL url;
	
	protected HttpUtils(String url) throws MalformedURLException {
		this.url = new URL(url);
	}
	
	/**
	 * Open a new connection to the url
	 * @return false if the url is not HTTP or HTTPS
	 * @throws IOException
	 */
	protected boolean open() throws IOException {
		if (url.getProtocol().equals("http") || url.getProtocol().equals("https")) {
			connection = (HttpURLConnection)url.openConnection();
			
			// Allow Inputs & Outputs
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
					
			return true;
		}
		return false;
	}
	
	/**
	 * Prepares the connection to send a file and returns the OutputStream for
	 * writing the file
	 * 
	 * @param fileName name of the file to post
	 * @param fileType content type of the file
	 * @param fileField field name for the posted file
	 * @param postParameters extra post parameters
	 * @return the result from HttpURLConnection.getOutputStream()
	 * @throws IOException 
	 */
	protected DataOutputStream prepareFilePost(String fileName, String fileType, String fileField,  Map<String,String> postParameters) throws IOException {
		// Enable POST method
		connection.setRequestMethod(POST);
		connection.setChunkedStreamingMode(0); //Use the default
		connection.setRequestProperty("Connection", "Keep-Alive");
		connection.setRequestProperty("Content-Type",
				"multipart/form-data;boundary=" + BOUNDARY);

		urlStream = new DataOutputStream(connection.getOutputStream());
		
		if (postParameters != null) {
			for (String key: postParameters.keySet()) {
				// Writer parameter
				urlStream.writeBytes(TWO_HYPHENS + BOUNDARY + CRLF);
				urlStream.writeBytes("Content-Disposition: form-data; name=\""+key+"\"" + CRLF);
				urlStream.writeBytes(CRLF);
				urlStream.writeBytes(postParameters.get(key));
				urlStream.writeBytes(CRLF);
			}
		}
		
		// Write file
		urlStream.writeBytes(TWO_HYPHENS + BOUNDARY + CRLF);
		urlStream.writeBytes("Content-Disposition: form-data; name=\""+fileField+"\";filename=\""+ fileName + "\"" + CRLF);
		urlStream.writeBytes("Content-Type: "+fileType + CRLF);
		urlStream.writeBytes("Content-Transfer-Encoding: binary" + CRLF);
		urlStream.writeBytes(CRLF);
		
		return urlStream;
	}
	
	protected HttpResponse finishFilePost() throws IOException {
		urlStream.writeBytes(CRLF);
		urlStream.writeBytes(TWO_HYPHENS + BOUNDARY + TWO_HYPHENS + CRLF);
		
		urlStream.flush();
		urlStream.close();
		
		HttpResponse response;
		if (connection.getResponseCode() == 200) {
			response = new HttpResponse(connection.getResponseCode(), connection.getInputStream(), connection);
		}
		else {
			response = new HttpResponse(connection.getResponseCode(), connection.getErrorStream(), connection);
		}
		
		return response;
	}
	
	/**
	 * Send a serializable as an attachment to the provided url
	 * 
	 * @param serializable
	 * @param url
	 * @param postParameters extra parameters to add to the POST (can be null)
	 * @return
	 */
	public static HttpResponse sendObject(String url, Serializable<?> serializable, Map<String,String> postParameters) {
		try {
			HttpUtils http = new HttpUtils(url);		
			if (http.open()) {
				Log.d(TAG, "Opened URL "+url);
				GZIPOutputStream out = new GZIPOutputStream(http.prepareFilePost(POST_FILE_NAME, POST_FILE_TYPE, POST_FILE_FIELD, postParameters));
				Serializer serializer = SerializerFactory.getInstance().getSerializer();
				serializer.serialize(out, serializable);
				out.finish();
				
				HttpResponse response = http.finishFilePost();
				Log.d(TAG, "Server responded with code "+response.getCode());
				return response;
			}
		}
		catch (MalformedURLException e) {
			Log.e(TAG, "Malformed url "+url);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Send a serializable data object as an attachment to the provided url
	 * @param list
	 * @param url
	 * @param postParameters extra parameters to add to the POST (can be null) 
	 * @return
	 */
	public static HttpResponse sendList(Iterator<Serializable<?>> list, String url, Map<String,String> postParameters) {
		try {
			HttpUtils http = new HttpUtils(url);		
			if (http.open()) {
				Log.d(TAG, "Opened URL "+url);
				GZIPOutputStream out = new GZIPOutputStream(http.prepareFilePost(POST_FILE_NAME, POST_FILE_TYPE, POST_FILE_FIELD, postParameters));
				Serializer serializer = SerializerFactory.getInstance().getSerializer();
				serializer.serialize(out, list);
				out.finish();
				
				HttpResponse response = http.finishFilePost();
				Log.d(TAG, "Server responded with code "+response.getCode());
				return response;
			}
		}
		catch (MalformedURLException e) {
			Log.e(TAG, "Malformed url "+url);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
