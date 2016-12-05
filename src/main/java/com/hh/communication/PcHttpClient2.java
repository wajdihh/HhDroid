package com.hh.communication;

import com.hh.listeners.MyCallback;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
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


	private void applyRequest(String myURL,MyCallback callback,RequestMethod requestMethod) throws IOException {

		if(requestMethod==RequestMethod.GET)
			myURL=myURL+getQuery(params);

		URL url = new URL(myURL);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setReadTimeout(_mConnexionMaxTimeOut);
		conn.setConnectTimeout(_mConnexionTimeOut);
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setUseCaches(false);

		// for the post and the PUT
		OutputStream os = conn.getOutputStream();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
		// For json Object
		DataOutputStream printout;


		switch (requestMethod){
			case GET:
				conn.setRequestMethod("GET");
				break;
			case PUT:
				conn.setRequestMethod("PUT");
				writer.write(getQuery(params));
				writer.flush();
				writer.close();
				os.close();

				// For Json Object
				if(_mJsonObject!=null){
					// Send POST output.
					printout = new DataOutputStream(conn.getOutputStream ());
					printout.writeBytes(URLEncoder.encode(_mJsonObject.toString(),"UTF-8"));
					printout.flush ();
					printout.close ();
				}
				break;
			case POST:
				// for Params
				conn.setRequestMethod("POST");
				writer.write(getQuery(params));
				writer.flush();
				writer.close();
				os.close();

				// For Json Object
				if(_mJsonObject!=null){
					// Send POST output.
					printout = new DataOutputStream(conn.getOutputStream ());
					printout.writeBytes(URLEncoder.encode(_mJsonObject.toString(),"UTF-8"));
					printout.flush ();
					printout.close ();
				}
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

//		HttpPost httppost = new HttpPost(url);
//		//add headers
//		for(NameValuePair h : headers)
//		{
//			httppost.addHeader(h.getName(), h.getValue());
//		}
//
//		httppost.removeHeaders("Content-Type");
//
//
//		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
//
//		try {
//			BitmapFactory.Options options = new BitmapFactory.Options();
//			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//			Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
//
//			ByteArrayOutputStream bos = new ByteArrayOutputStream();
//			bitmap.compress(Bitmap.CompressFormat.JPEG, 75, bos);
//			byte[] data = bos.toByteArray();
//			ByteArrayBody bab = new ByteArrayBody(data, imageFile.getName());
//			reqEntity.addPart(multipartKeyName, bab);
//
//			httppost.setEntity(reqEntity);
//			HttpResponse response = httpClient.execute(httppost);
//			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
//			String sResponse;
//			StringBuilder s = new StringBuilder();
//			while ((sResponse = reader.readLine()) != null) {
//				s = s.append(sResponse);
//			}
//			_mResponseCode = response.getStatusLine().getStatusCode();
//			_mMessageStatus = response.getStatusLine().getReasonPhrase();
//			_mResponse=s.toString();
//		}catch (Exception e) {
//			Log.e("EX", e.getLocalizedMessage(), e);
//		}

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
