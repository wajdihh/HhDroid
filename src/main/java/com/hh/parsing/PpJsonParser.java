package com.hh.parsing;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.hh.clientdatatable.ClientDataTable;
import com.hh.clientdatatable.TCell.ValueType;
import com.hh.clientdatatable.TColumn;
import com.hh.clientdatatable.TRow;
import com.hh.utility.PuUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class PpJsonParser {


	/**@author wajdihh
	 * Get the list of key names froma JsonArray
	 * @param pJsonObject
	 * @return key names list
	 */


	private static JSONObject jsonChild;
	public static ArrayList<String> getKeysNames(JSONObject pJsonObject){

		ArrayList<String> lArrays=new ArrayList<String>();

		Iterator<?> keys = pJsonObject.keys();

		while(keys.hasNext()) {
			String lCurrentDynamicKey = (String)keys.next();
			lArrays.add(lCurrentDynamicKey);
		}

		return lArrays;
	}

	static void parseJsonArrayIntoCDT(JSONArray pJsArray,ClientDataTable pCDT,boolean saveInDataBase) throws JSONException{

		if(pJsArray==null || pJsArray.length()==0)
			return;

		int lSizeJsArray = pJsArray.length();

		JSONObject lFirstJsObject=pJsArray.getJSONObject(0);
		ArrayList<String> lJsArrayColumnsNames = getKeysNames(lFirstJsObject);

		int lNbrColumnsJsArray= lJsArrayColumnsNames.size();

		if (pCDT.getColumnsCount() == 0) {
			for (int i = 0; i < lNbrColumnsJsArray; i++) {
				pCDT.addColumn(new TColumn(lJsArrayColumnsNames.get(i),ValueType.TEXT));
			}
		}

		for (int k = 0; k < lSizeJsArray; k++) {

			JSONObject lJsObject=pJsArray.getJSONObject(k);
			TRow lRow = new TRow();

			for (TColumn lColumn :pCDT.getListOfColumns()) {
				boolean lIsColumnFound=false;

				for (String jsonColumnName:lJsArrayColumnsNames) {
					if (lColumn != null && lColumn.getName().equals(jsonColumnName)) {
						lRow.addCell(pCDT.getContext(),optStringValue(lJsObject, lColumn.getName()),lColumn.getValueType(),lColumn.getCellType(),lColumn.getName(),pCDT.getCDTStatus(),lColumn.getCDTColumnListener());
						lIsColumnFound=true;
						break;
					}
				}
				if(!lIsColumnFound) {
					assert lColumn != null;
					String jsonParent=lColumn.getJsonParent();
					String str="";
					if(jsonParent!=null && !jsonParent.equals("") && !jsonParent.equals("MAIN"))
						str=lRow.cellByName(jsonParent).asString();

					if(!str.isEmpty()){
						JSONObject sub=new JSONObject(str);
						lRow.addCell(pCDT.getContext(),optStringValue(sub, lColumn.getName()),lColumn.getValueType(),lColumn.getCellType(),lColumn.getName(),pCDT.getCDTStatus(),lColumn.getCDTColumnListener());
					} else
						lRow.addCell(pCDT.getContext(),"",lColumn.getValueType(),lColumn.getCellType(),lColumn.getName(),pCDT.getCDTStatus(),lColumn.getCDTColumnListener());


				}
			}
			pCDT.append(lRow);
			if (saveInDataBase)
				pCDT.execute();
			else
				pCDT.commit();
		}



		pCDT.moveToFirst();

		Log.i("fillFromJsonArray"," Count :"+ lSizeJsArray);
	}
	/**
	 * Parse a JsonArray and put the values into a client data table
	 * @param pJsArray
	 * @param pCDT
	 * @throws JSONException
	 */
	public static void syncJsonArrayInCdt(JSONArray pJsArray,ClientDataTable pCDT) throws JSONException{
		parseJsonArrayIntoCDT(pJsArray, pCDT, false);
	}

	public static void syncJSONObjectInCdt(JSONObject pJsonObj,ClientDataTable pCDT) throws JSONException{
		parseJsonArrayIntoCDT(new JSONArray().put(pJsonObj),pCDT,false);
	}
	public static void syncJSONObjectInCdtWithExec(JSONObject pJsonObj,ClientDataTable pCDT) throws JSONException{
		parseJsonArrayIntoCDT(new JSONArray().put(pJsonObj),pCDT,true);
	}

	public static void syncJsonArrayInCdtWithExec(JSONArray pJsArray,ClientDataTable pCDT) throws JSONException{
		parseJsonArrayIntoCDT(pJsArray,pCDT,true);
	}
	/**
	 * Parse a JsonArray and put the values into a Table in dataBase
	 * @author wajdihh
	 * @param pJsArray
	 * @throws JSONException
	 */
	public static void parseJsonArrayIntoDBTable(JSONArray pJsArray,String pTableName,SQLiteDatabase pDataBase) throws JSONException{

		int lSizeJsArray = pJsArray.length();

		if(lSizeJsArray!=0){
			ArrayList<String> lMappingColumnsName=new ArrayList<String>();

			JSONObject lFirstJsObject=pJsArray.getJSONObject(0);
			ArrayList<String> lJsArrayColumnsNames = getKeysNames(lFirstJsObject);

			ArrayList<String> lDataBaseTableColumns=PuUtils.getTableColumnsNames(pDataBase, pTableName);

			for(String lColumnTableName:lDataBaseTableColumns){

				for(String lColumnJsName:lJsArrayColumnsNames){
					if(lColumnTableName.equals(lColumnJsName)){
						lMappingColumnsName.add(lColumnTableName);
						break;
					}
				}
			}

			for (int k = 0; k < lSizeJsArray && lMappingColumnsName.size()!=0; k++) {

				JSONObject lJsObject=pJsArray.getJSONObject(k);

				ContentValues values=new ContentValues();
				for (String lColumnName:lMappingColumnsName)
					values.put(lColumnName, optStringValue(lJsObject,lColumnName));

				pDataBase.insert(pTableName, null, values);
			}

			Log.i("fillDataBaseTableFromJsonArray"," Count :"+ lSizeJsArray);
		}else
			Log.i("fillDataBaseTableFromJsonArray","JsonArray est vide");
	}

	public static String optStringValue(JSONObject response,String key) throws JSONException {
		String result=null;
		if(((response.has(key) && !response.isNull(key))))
			result=response.getString(key);

		return result;
	}
}
