package com.hh.execption;

import android.content.Context;

/**
 * Created by WajdiHh on 30/12/2015.
 * Email : wajdihh@gmail.com
 */
public class WrongTypeException extends  HhException {

    public WrongTypeException(String message) {
        super(message);
    }
    public WrongTypeException(Context pContext,int message) {
        super(pContext,message);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}

