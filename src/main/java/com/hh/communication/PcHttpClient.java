package com.hh.communication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import com.hh.listeners.MyCallback;
import com.hh.utility.PuException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;

public class PcHttpClient {


	public enum RequestMethod {GET,POST,PUT}

	private int _mConnexionTimeOut=7000;
	private int _mConnexionMaxTimeOut=7000;

	private ArrayList <NameValuePair> params;
	private ArrayList <NameValuePair> headers;
	private int _mResponseCode;
	private String _mMessageStatus;
	private String _mResponse;
	private HttpEntity _mHttpEntity;
	private JSONObject _mJsonObject;
	private HttpClient httpClient;


	public String getResponse() {
		return _mResponse;
	}

	public PcHttpClient(){

		httpClient = new DefaultHttpClient();
		HttpParams Httpparams = httpClient.getParams();
		HttpConnectionParams.setConnectionTimeout(Httpparams, _mConnexionTimeOut);
		HttpConnectionParams.setSoTimeout(Httpparams, _mConnexionMaxTimeOut);

		params = new ArrayList<NameValuePair>();
		headers = new ArrayList<NameValuePair>();
	}

	public void setConnexionTimeOut(int _mConnexionTimeOut) {
		this._mConnexionTimeOut = _mConnexionTimeOut;
		HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), _mConnexionTimeOut);

	}

	public void setConnexionMaxTimeOut(int _mConnexionMaxTimeOut) {
		this._mConnexionMaxTimeOut = _mConnexionMaxTimeOut;
		HttpConnectionParams.setSoTimeout(httpClient.getParams(), _mConnexionMaxTimeOut);
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

				HttpGet request = new HttpGet(url + combinedParams);

				//add headers
				for(NameValuePair h : headers)
				{
					request.addHeader(h.getName(), h.getValue());
				}

				executeRequest(request, url,callback);
				break;
			}
			case POST:
			{
				HttpPost request = new HttpPost(url);

				//add headers
				for(NameValuePair h : headers)
				{
					request.addHeader(h.getName(), h.getValue());
				}

				if(!params.isEmpty()){
					request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
				}

				if(_mJsonObject!=null){
					StringEntity se = new StringEntity( _mJsonObject.toString(),"UTF-8");
					request.setEntity(se);
				}

				executeRequest(request, url,callback);
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

					request.setEntity(new StringEntity(_mJsonObject.toString()));
				}

				executeRequest(request, url,callback);
				break;
			}
		}
	}
	public HttpEntity getHttpEntity(){

		return _mHttpEntity;
	}
	private void executeRequest(HttpUriRequest request, String url,MyCallback callback)
	{

		HttpResponse httpResponse;

		try {
			httpResponse = httpClient.execute(request);
			_mResponseCode = httpResponse.getStatusLine().getStatusCode();
			_mMessageStatus = httpResponse.getStatusLine().getReasonPhrase();

			_mHttpEntity= httpResponse.getEntity();

			if (_mHttpEntity != null) {

				InputStream instream = _mHttpEntity.getContent();
				_mResponse = convertStreamToString(instream);

				instream.close();

				if(callback!=null) callback.onSuccess(_mResponse);
			}

		} catch (ClientProtocolException e)  {
			Log.e("EXxception : ClientProtocolException :", PuException.getExceptionMessage(e));
			httpClient.getConnectionManager().shutdown();
			e.printStackTrace();
		} catch (IOException e) {
			Log.e("EXxception : IOException :", PuException.getExceptionMessage(e));
			httpClient.getConnectionManager().shutdown();
			e.printStackTrace();
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

	public JSONObject getJsonObject() {
		return _mJsonObject;
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
}
