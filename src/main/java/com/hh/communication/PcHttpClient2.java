package com.hh.communication;

import com.hh.listeners.MyCallback;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class PcHttpClient2 {


	public enum RequestMethod {GET,POST,PUT,DELETE,MULTIPART}

	private static final String LINE_FEED = "\r\n";
	private int _mConnexionTimeOut=10000;
	private int _mConnexionMaxTimeOut=15000;

	private ArrayList <NameValuePair> params;
	private ArrayList <NameValuePair> headers;
	private int _mResponseCode;
	private String _mMessageStatus;
	private String _mResponse;
	private JSONObject _mJsonObject;


	public String getResponse() {
		return _mResponse;
	}

	public PcHttpClient2(){
		params = new ArrayList<>();
		headers = new ArrayList<>();
	}

	public ArrayList<NameValuePair> getParams(){
		return params;
	}
	public ArrayList<NameValuePair> getHeaders(){
		return headers;
	}

	public void setConnexionTimeOut(int _mConnexionTimeOut) {
		this._mConnexionTimeOut = _mConnexionTimeOut;
	}

	public void setConnexionMaxTimeOut(int _mConnexionMaxTimeOut) {
		this._mConnexionMaxTimeOut = _mConnexionMaxTimeOut;
	}

	public String getMessageStatus() {
		return _mMessageStatus;
	}

	public int getResponseCode() {
		return _mResponseCode;
	}

	public void addParameter(String name, String value)
	{
		params.add(new BasicNameValuePair(name, value));
	}

	public void addParameters(ArrayList<NameValuePair> pListOfParams)
	{
		params=pListOfParams;
	}

	public void addHeader(String name, String value)
	{
		/*
		 * client.AddHeader("Content-Type", "application/json");
		 */
		if(!isHeaderContainValue(name))
			headers.add(new BasicNameValuePair(name, value));
	}

	public void addHeader(BasicNameValuePair headerParam)
	{
		if(!isHeaderContainValue(headerParam.getName()))
			headers.add(headerParam);
	}

	public void execute(String url,RequestMethod method) throws IOException
	{
		applyRequest(url, null, method,null,null);
	}

	public void execute(String url,RequestMethod method,MyCallback callback) throws JSONException {
		try {
			applyRequest(url, callback, method,null,null);
		} catch (Exception e) {
			e.printStackTrace();
			if(callback!=null) callback.onError(_mResponse);
		}
	}


	public void executeMultipart(String url,File fileToUpload,String multipartKeyName) throws IOException
	{
		applyRequest(url, null, RequestMethod.MULTIPART,multipartKeyName,fileToUpload);
	}

	public void executeMultipart(String url,File fileToUpload,String multipartKeyName,MyCallback callback) throws JSONException {
		try {
			applyRequest(url, callback, RequestMethod.MULTIPART,multipartKeyName,fileToUpload);
		} catch (Exception e) {
			e.printStackTrace();
			if(callback!=null) callback.onError(_mResponse);
		}
	}


	private void applyRequest(String myURL,MyCallback callback,RequestMethod requestMethod,String multiPartFiledName, File fileToUpload) throws IOException  {

		HttpURLConnection conn=null;
		myURL=myURL+getQuery(params);

		try {
			URL url = new URL(myURL);
			conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(_mConnexionMaxTimeOut);
			conn.setConnectTimeout(_mConnexionTimeOut);
			conn.setDoInput(true);

			/**
			 * Header values
			 */
			for(NameValuePair h : headers)
				conn.setRequestProperty(h.getName(), h.getValue());

			/**
			 * Methods Type requesst
			 */
			switch (requestMethod){
				case GET:
					conn.setDoOutput(false);
					conn.setRequestMethod("GET");
					break;
				case DELETE:
					conn.setDoOutput(true);
					conn.setRequestMethod("DELETE");
					break;
				case PUT:
					conn.setDoOutput(true);
					conn.setRequestMethod("PUT");

					// Write serialized JSON data to output stream.
					OutputStream outPut = new BufferedOutputStream(conn.getOutputStream());
					BufferedWriter writerPut = new BufferedWriter(new OutputStreamWriter(outPut, "UTF-8"));
					//writerPost.write(getQuery(params));
					if(_mJsonObject!=null)
						writerPut.write(_mJsonObject.toString());

					// Close streams and disconnect.
					writerPut.flush();
					writerPut.close();
					outPut.close();
					break;

				case POST:
					conn.setDoOutput(true);
					conn.setRequestMethod("POST");

					// Write serialized JSON data to output stream.
					OutputStream outPost = new BufferedOutputStream(conn.getOutputStream());
					BufferedWriter writerPost = new BufferedWriter(new OutputStreamWriter(outPost, "UTF-8"));
					//writerPost.write(getQuery(params));
					if(_mJsonObject!=null)
						writerPost.write(_mJsonObject.toString());

					// Close streams and disconnect.
					writerPost.flush();
					writerPost.close();
					outPost.close();
					break;
				case MULTIPART:
					conn.setDoOutput(true); // indicates POST method

					String  boundary = "===" + System.currentTimeMillis() + "===";
					conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
					conn.setRequestProperty("User-Agent", "CodeJava Agent");
					OutputStream outMultipart = conn.getOutputStream();
					PrintWriter writer = new PrintWriter(new OutputStreamWriter(outMultipart, "UTF-8"), true);

					String fileName = fileToUpload.getName();
					writer.append("--" + boundary).append(LINE_FEED);
					writer.append("Content-Disposition: form-data; name=\"" + multiPartFiledName + "\"; filename=\"" + fileName + "\"").append(LINE_FEED);
					writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(fileName)).append(LINE_FEED);
					writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
					writer.append(LINE_FEED);
					writer.flush();

					FileInputStream inputStream = new FileInputStream(fileToUpload);
					byte[] buffer = new byte[4096];
					int bytesRead = -1;
					while ((bytesRead = inputStream.read(buffer)) != -1) {
						outMultipart.write(buffer, 0, bytesRead);
					}
					outMultipart.flush();
					inputStream.close();

					writer.append(LINE_FEED).flush();
					writer.append("--" + boundary + "--").append(LINE_FEED);
					writer.close();
					break;
			}

			//Returned string
			_mResponseCode =conn.getResponseCode();
			_mMessageStatus =conn.getResponseMessage();

			InputStream in;
			if(_mResponseCode >= HttpStatus.SC_BAD_REQUEST)
				in = conn.getErrorStream();
			else
				in = conn.getInputStream();

			_mResponse = convertStreamToString(in);

			if(callback!=null) callback.onSuccess(_mResponse);

		}catch (IOException e) {
			throw new IOException(e);
		}  finally {
			if ( conn!= null) {
				conn.disconnect();
			}
		}
	}


	private static String convertStreamToString(InputStream is) {

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}


	public void setJsonObjectToPost(JSONObject _mJsonObject) {
		this._mJsonObject = _mJsonObject;
	}

	private boolean isHeaderContainValue(String valueName){

		for (NameValuePair item:headers){
			if(item.getName().equals(valueName))
				return true;

		}
		return false;
	}

	public boolean removeHeaderParam(String valueName){

		int index=0;
		for (NameValuePair item:headers){
			if(item.getName().equals(valueName)){
				headers.remove(index);
				return true;
			}
			index++;
		}
		return false;
	}

	private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
	{

		if(params.isEmpty())
			return "";


		StringBuilder result = new StringBuilder();
		result.append("?");
		boolean first = true;

		for (NameValuePair pair : params)
		{
			if (first)
				first = false;
			else
				result.append("&");

			result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
		}

		return result.toString();
	}
}
