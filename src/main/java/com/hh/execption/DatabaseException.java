package com.hh.execption;

import android.content.Context;

/**
 * Created by WajdiHh on 30/12/2015.
 * Email : wajdihh@gmail.com
 */
public class DatabaseException extends  HhException {

    public DatabaseException(Context pContext,int message) {
        super(pContext,message);
    }
}
