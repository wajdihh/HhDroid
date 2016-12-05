package com.hh.communication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import com.hh.listeners.MyCallback;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

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
		executeRequest(url, method, null);
	}

	public void execute(String url,RequestMethod method,MyCallback callback) throws JSONException {
		try {
			executeRequest(url, method, callback);
		} catch (Exception e) {
			e.printStackTrace();
			if(callback!=null) callback.onError(_mResponse);
		}
	}

	private void executeRequest(String url,RequestMethod method,MyCallback callback) throws IOException
	{
		switch(method) {
			case GET:
			{
				//add parameters
				String combinedParams = "";
				if(!params.isEmpty()){
					combinedParams += "?";
					for(NameValuePair p : params)
					{
						String paramString = p.getName() + "=" + URLEncoder.encode(p.getValue(),"UTF-8");
						if(combinedParams.length() > 1)
						{
							combinedParams  +=  "&" + paramString;
						}
						else
						{
							combinedParams += paramString;
						}
					}
				}
				apply(url,callback,RequestMethod.GET);
				break;
			}
			case POST:
			{

				if(!params.isEmpty()){
					request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
				}

				if(_mJsonObject!=null){
					StringEntity se = new StringEntity( _mJsonObject.toString(),"UTF-8");
					request.setEntity(se);
				}

				apply(url, callback, RequestMethod.POST);

				break;
			}
			case PUT:
			{
				HttpPut request = new HttpPut(url);

				//add headers
				for(NameValuePair h : headers)
				{
					request.addHeader(h.getName(), h.getValue());
				}

				if(!params.isEmpty()){
					request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
				}

				if(_mJsonObject!=null){

					request.setEntity(new StringEntity(_mJsonObject.toString(),"UTF-8"));
				}

				apply(url,callback,RequestMethod.PUT);
				break;
			}
		}
	}

	private void apply(String myURL,MyCallback callback,RequestMethod requestMethod) throws IOException {

		URL url = new URL(myURL);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setReadTimeout(_mConnexionMaxTimeOut);
		conn.setConnectTimeout(_mConnexionTimeOut);

		switch (requestMethod){
			case GET:
				conn.setRequestMethod("GET");
				break;
			case PUT:
				conn.setRequestMethod("PUT");
				break;
			case POST:
				conn.setRequestMethod("POST");
				break;
		}

		for(NameValuePair h : headers)
		{
			conn.setRequestProperty(h.getName(), h.getValue());
		}

		conn.connect();
		_mResponseCode =conn.getResponseCode();
		_mMessageStatus =conn.getResponseMessage();
		_mResponse = convertStreamToString(conn.getInputStream());

		conn.disconnect();

		if(callback!=null) callback.onSuccess(_mResponse);
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

	public void sendMultipartImage(String url,File imageFile,String multipartKeyName){

		HttpPost httppost = new HttpPost(url);
		//add headers
		for(NameValuePair h : headers)
		{
			httppost.addHeader(h.getName(), h.getValue());
		}

		httppost.removeHeaders("Content-Type");


		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 75, bos);
			byte[] data = bos.toByteArray();
			ByteArrayBody bab = new ByteArrayBody(data, imageFile.getName());
			reqEntity.addPart(multipartKeyName, bab);

			httppost.setEntity(reqEntity);
			HttpResponse response = httpClient.execute(httppost);
			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
			String sResponse;
			StringBuilder s = new StringBuilder();
			while ((sResponse = reader.readLine()) != null) {
				s = s.append(sResponse);
			}
			_mResponseCode = response.getStatusLine().getStatusCode();
			_mMessageStatus = response.getStatusLine().getReasonPhrase();
			_mResponse=s.toString();
		}catch (Exception e) {
			Log.e("EX", e.getLocalizedMessage(), e);
		}
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
}
