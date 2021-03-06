package com.hh.database;

import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;

import java.io.IOException;
import java.util.Calendar;

public class DAOManager {

	private DBSqliteOpenHelper _mSqliteHelper;
	protected Context mContext;
	protected Resources mRes;
	protected SQLiteDatabase mDataBase;
	
	public DAOManager(Context pContext){
		
		mContext=pContext;
		mRes=pContext.getResources();
		_mSqliteHelper=new DBSqliteOpenHelper(pContext);
		mDataBase=_mSqliteHelper.getWritableDatabase();
	}

	public SQLiteDatabase getDataBase(){
		return mDataBase;
	}
	@Override
	protected void finalize() throws Throwable {
	
		if ((mDataBase != null) && (mDataBase.isOpen()))
			mDataBase.close();
		if (_mSqliteHelper!=null)
			_mSqliteHelper.close();	
		mContext = null;
		super.finalize();
	}

	protected void close() throws IOException {
		_mSqliteHelper.close();
		mDataBase.close();
	}

	/**
	 * To get last primaryKey table ID
	 * @param tableName
	 * @return
	 */
	public int getLastPrimaryKeyValue(String tableName){
		return DatabaseUtils.getLastPrimaryKeyValue(mDataBase,tableName);
	}

	public int getTempPrimaryKeyValue(){
		return Calendar.getInstance().get(Calendar.MILLISECOND)*Calendar.getInstance().get(Calendar.SECOND);
	}
	public String getString(int resID){
		return mRes.getString(resID);
	}
}
