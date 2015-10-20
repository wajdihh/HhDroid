package com.hh.parsing;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.hh.clientdatatable.ClientDataTable;
import com.hh.clientdatatable.TCell;
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

		int lSizeJsArray = pJsArray.length();
		 if(lSizeJsArray==0)
			 return;

		pCDT.getListOfRows().clear();
		pCDT.getListOfRows().ensureCapacity(lSizeJsArray);

		JSONObject lFirstJsObject=pJsArray.getJSONObject(0);
		ArrayList<String> lJsArrayColumnsNames = getKeysNames(lFirstJsObject);

		int lNbrColumnsCDT = pCDT.getColumnsCount();
		int lNbrColumnsJsArray= lJsArrayColumnsNames.size();

		if (lNbrColumnsCDT == 0) {
			for (int i = 0; i < lNbrColumnsJsArray; i++) {
				pCDT.addColumn(new TColumn(lJsArrayColumnsNames.get(i),ValueType.TEXT));
			}
		}
		if (lSizeJsArray > 0) {

			for (int k = 0; k < lSizeJsArray; k++) {

				JSONObject lJsObject=pJsArray.getJSONObject(k);
				TRow lRow = new TRow();

				if (lNbrColumnsCDT > 0) {
					for (int i = 0; i < lNbrColumnsCDT; i++) {
						boolean lIsColumnFound=false;
						TColumn lColumn = pCDT.getListOfColumns().get(i);
						for (int j = 0; j < lNbrColumnsJsArray; j++) {
							if (lColumn != null && lColumn.getName().equals(lJsArrayColumnsNames.get(j))) {
								lRow.addCell(pCDT.getContext(),lJsObject.getString(lColumn.getName()),lColumn.getValueType(),lColumn.getCellType(),lColumn.getName(),pCDT.getCDTStatus(),lColumn.getCDTColumnListener());
								lIsColumnFound=true;
								break;
							}
						}
						if(!lIsColumnFound) {
							assert lColumn != null;
							lRow.addCell(pCDT.getContext(),"",lColumn.getValueType(),lColumn.getCellType(),lColumn.getName(),pCDT.getCDTStatus(),lColumn.getCDTColumnListener());
						}
					}
				} else {
					for (String lColumnName : lJsArrayColumnsNames) {
						if (lColumnName != null && !lColumnName.equals(""))
							lRow.addCell(pCDT.getContext(),lJsObject.getString(lColumnName), ValueType.TEXT, TCell.CellType.NONE,lColumnName,pCDT.getCDTStatus(),null);
					}
				}
				pCDT.append(lRow);
				if (saveInDataBase)
					pCDT.execute();
				else
					pCDT.commit();
			}
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

		parseJsonArrayIntoCDT(pJsArray,pCDT,false);
	}

	public static void syncJsonArrayInCdtWithExec(JSONArray pJsArray,ClientDataTable pCDT) throws JSONException{
		parseJsonArrayIntoCDT(pJsArray,pCDT,true);
	}
	/**
	 * Parse a JsonArray and put the values into a Table in dataBase
	 * @author wajdihh
	 * @param pJsArray
	 * @param pCDT
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

			for (int k = 0; k < lSizeJsArray; k++) {

				JSONObject lJsObject=pJsArray.getJSONObject(k);

				ContentValues values=new ContentValues();
				for (String lColumnName:lMappingColumnsName) 
					values.put(lColumnName, lJsObject.getString(lColumnName));

				pDataBase.insert(pTableName, null, values);
			}

			Log.i("fillDataBaseTableFromJsonArray"," Count :"+ lSizeJsArray);
		}else
			Log.i("fillDataBaseTableFromJsonArray","JsonArray est vide");
	}
}
