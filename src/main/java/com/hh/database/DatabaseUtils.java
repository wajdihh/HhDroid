package com.hh.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by WajdiHh on 21/12/2015.
 * Email : wajdihh@gmail.com
 */
public class DatabaseUtils {

    /**
     * To get last primaryKey table ID
     * @param tableName
     * @return
     */
    public static int getLastPrimaryKeyValue(SQLiteDatabase pDB,String tableName){

        try {
            Cursor c=pDB.rawQuery("select seq from sqlite_sequence where name='"+tableName+"'",null);
            c.moveToFirst();
            if(c.getCount()==0)
                return 0;
            return c.getInt(c.getColumnIndex("seq"));
        }catch (Exception e){

        }
        return 0;
    }
}
