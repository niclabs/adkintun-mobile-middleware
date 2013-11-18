package cl.niclabs.adkmobile.net;

import java.io.DataInputStream;

public class HttpResponse {
	protected int code;
	protected DataInputStream stream;
	
	public HttpResponse(int code, DataInputStream content) {
		this.code = code;
		this.stream = content;
	}
	
	public int getCode() {
		return code;
	}
	
	/**
	 * 
	 * @return the content of the response
	 */
	public DataInputStream getStream() {
		return stream;
	}
}
