package cl.niclabs.adkmobile.monitor.httpclient;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

import android.util.Log;

/**
 * This class open the connection to the server and send
 * the data of the local Data Base on JSON zipped format.
 * Then, it should delete the data envoy from the local Data Base.
 *
 * @author Mauricio Castro.
 *         Created 09-10-2013.
 */
public class HttpNicClient {
	
	public HttpNicClient(){
		
	}
	
	/**
	 * Uploads a specified file to a server through HTTP POST method.
	 *
	 * @param filePath path to the file. In format /someDir/someDir/file.x
	 * @param postUrl URL of the server.
	 * @return true if the file was send. false if an error occurs.
	 * @throws IOException 
	 */
	public static boolean postFile(String filePath, String postUrl) throws IOException{
		
		/* TODO the JSON object (or array?) must be generated */
		JSONObject json = new JSONObject();
		
		HttpURLConnection connection = null;
		DataOutputStream outputStream = null;
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		
		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1 * 1024 * 1024;
		
		File file = new File(filePath);
		
		FileInputStream fileInputStream = new FileInputStream(file);

		URL url = new URL(postUrl);
		connection = (HttpURLConnection) url.openConnection();
		
		// Allow Inputs & Outputs
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setUseCaches(false);
					
		// Enable POST method
		connection.setRequestMethod("POST");
		
		connection.setRequestProperty("Connection", "Keep-Alive");
		connection.setRequestProperty("Content-Type",
				"multipart/form-data;boundary=" + boundary);

		outputStream = new DataOutputStream(connection.getOutputStream());
		outputStream.writeBytes(twoHyphens + boundary + lineEnd);
		outputStream
				.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\""
						+ filePath + "\"" + lineEnd);
		outputStream.writeBytes(lineEnd);

		bytesAvailable = fileInputStream.available();
		bufferSize = Math.min(bytesAvailable, maxBufferSize);
		buffer = new byte[bufferSize];
		
		// Read file
		bytesRead = fileInputStream.read(buffer, 0, bufferSize);
		
		while (bytesRead > 0) {
			outputStream.write(buffer, 0, bufferSize);
			bytesAvailable = fileInputStream.available();
			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			bytesRead = fileInputStream.read(buffer, 0, bufferSize);
		}
		
		outputStream.writeBytes(lineEnd);
		outputStream.writeBytes(twoHyphens + boundary + twoHyphens
				+ lineEnd);
		
		// Responses from the server (code and message)
		int serverResponseCode = connection.getResponseCode();
		String serverResponseMessage = connection.getResponseMessage();
		Log.d("FileUtils", "Response code: " + serverResponseCode + "");
		Log.d("FileUtils", "Server response: " + serverResponseMessage);
			if (serverResponseCode != 200) {
				fileInputStream.close();
				outputStream.flush();
				outputStream.close();
				return false;
			}
		fileInputStream.close();
		outputStream.flush();
		outputStream.close();
		return true;
		
	}

}
