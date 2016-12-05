package com.hh.communication;

import com.hh.listeners.MyCallback;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class PcHttpClient2 {


	public enum RequestMethod {GET,POST,PUT}

	private int _mConnexionTimeOut=7000;
	private int _mConnexionMaxTimeOut=7000;

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
		params = new ArrayList<NameValuePair>();
		headers = new ArrayList<NameValuePair>();
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

	public void AddParam(String name, String value)
	{
		params.add(new BasicNameValuePair(name, value));
	}

	public void AddParam(ArrayList<NameValuePair> pListOfParams)
	{
		params=pListOfParams;
	}

	public void AddHeader(String name, String value)
	{
		/*
		 * client.AddHeader("Content-Type", "application/json");
		 */
		if(!isHeaderContainValue(name))
			headers.add(new BasicNameValuePair(name, value));
	}

	public void AddHeader(BasicNameValuePair headerParam)
	{
		if(!isHeaderContainValue(headerParam.getName()))
			headers.add(headerParam);
	}

	public void execute(String url,RequestMethod method) throws IOException
	{
		applyRequest(url, null, method);
	}

	public void execute(String url,RequestMethod method,MyCallback callback) throws JSONException {
		try {
			applyRequest(url, callback, method);
		} catch (Exception e) {
			e.printStackTrace();
			if(callback!=null) callback.onError(_mResponse);
		}
	}


	private void applyRequest(String myURL,MyCallback callback,RequestMethod requestMethod) throws IOException  {

		HttpURLConnection conn=null;
		if(requestMethod==RequestMethod.GET)
			myURL=myURL+getQuery(params);

		try {
			URL url = new URL(myURL);
			conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(_mConnexionMaxTimeOut);
			conn.setConnectTimeout(_mConnexionTimeOut);
			conn.setDoInput(true);
			switch (requestMethod){
				case GET:
					conn.setDoOutput(false);
					conn.setRequestMethod("GET");
					break;
				case PUT:
					conn.setDoOutput(true);
					conn.setRequestMethod("PUT");
					// for the post and the PUT
					OutputStream os = conn.getOutputStream();
					BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

					writer.write(getQuery(params));
					writer.flush();
					writer.close();
					os.close();

					// For json Object
					DataOutputStream printout;
					if(_mJsonObject!=null){
						// Send POST output.
						printout = new DataOutputStream(conn.getOutputStream ());
						printout.writeBytes(URLEncoder.encode(_mJsonObject.toString(),"UTF-8"));
						printout.flush ();
						printout.close ();
					}
					break;
				case POST:
					conn.setDoOutput(true);
					conn.setRequestMethod("POST");
					// for the post and the PUT
					OutputStream os2 = conn.getOutputStream();
					BufferedWriter writer2 = new BufferedWriter(new OutputStreamWriter(os2, "UTF-8"));
					writer2.write(getQuery(params));
					writer2.flush();
					writer2.close();
					os2.close();

					// For json Object
					DataOutputStream printout2;
					if(_mJsonObject!=null){
						// Send POST output.
						printout2 = new DataOutputStream(conn.getOutputStream ());
						printout2.writeBytes(URLEncoder.encode(_mJsonObject.toString(),"UTF-8"));
						printout2.flush ();
						printout2.close ();
					}
					break;
			}

			for(NameValuePair h : headers)
				conn.setRequestProperty(h.getName(), h.getValue());

			//conn.connect();
			_mResponseCode =conn.getResponseCode();
			_mMessageStatus =conn.getResponseMessage();
			_mResponse = convertStreamToString(conn.getInputStream());

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

	public void sendMultipartImage(String myURL,File imageFile,String multipartKeyName){




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
