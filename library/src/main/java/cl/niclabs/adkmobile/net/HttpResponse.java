package cl.niclabs.adkmobile.net;

import java.io.InputStream;
import java.net.HttpURLConnection;

public class HttpResponse {
	protected int code;
	protected InputStream stream;
	protected HttpURLConnection connection;
	
	public HttpResponse(int code, InputStream stream, HttpURLConnection connection) {
		this.code = code;
		this.stream = stream;
		this.connection = connection;
	}
	
	public int getCode() {
		return code;
	}
	
	/**
	 * 
	 * @return the content of the response
	 */
	public InputStream getStream() {
		return stream;
	}
	
	/**
	 * Close the connection for this responses
	 */
	public void close() {
		connection.disconnect();
	}
}
