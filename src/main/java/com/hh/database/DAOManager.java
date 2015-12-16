package com.hh.database;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.IOException;

public class DAOManager {

	public static int mDBVersion=1;

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

		try {
			Cursor c=mDataBase.rawQuery("select seq from sqlite_sequence where name='"+tableName+"'",null);
			c.moveToFirst();
			if(c.getCount()==0)
				return 0;
			return c.getInt(c.getColumnIndex("seq"));
		}catch (Exception e){

		}
		return 0;
	}
}
