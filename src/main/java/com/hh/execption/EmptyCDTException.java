package com.hh.execption;

import android.content.Context;
import com.hh.droid.R;

/**
 * Created by WajdiHh on 30/12/2015.
 * Email : wajdihh@gmail.com
 */
public class EmptyCDTException extends  HhException {

    public EmptyCDTException(Context pContext) {
        super(pContext.getString(R.string.exception_emptyCDT));
    }
}
