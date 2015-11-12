
package com.hh.droid;

import android.content.Context;
import com.hh.application.HhDroidApplication;
import com.hh.utility.PuDate;

import java.text.SimpleDateFormat;
import java.util.Locale;


public class HhDroid {

	protected static HhDroid instance;
    public String mCurrencySymbol="";
	public PuDate mPuDate;

	public HhDroid(Context pContext){

		HhDroidApplication app= (HhDroidApplication) pContext.getApplicationContext();
		mCurrencySymbol=app.getCurrencySymbole();
		mPuDate=app.getConfiguredDate();
	}
	public static HhDroid getInstance(Context pContext) {
		if (instance == null) {
			instance = new HhDroid(pContext);
		}

		return instance;
	}

}
