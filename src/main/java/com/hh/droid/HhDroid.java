
package com.hh.droid;

import android.content.Context;
import com.hh.application.HhDroidApplication;
import com.hh.utility.PuDate;


public class HhDroid {

	protected static HhDroid instance;
    public String mCurrencySymbol="";
	public PuDate mPuDate;
	public int mDBVersion;

	public HhDroid(Context pContext){

		HhDroidApplication app= (HhDroidApplication) pContext.getApplicationContext();
		mCurrencySymbol=app.getCurrencySymbole();
		mPuDate=app.getConfiguredDate();
		mDBVersion=app.getDatabaseVersion();
	}
	public static HhDroid getInstance(Context pContext) {
		if (instance == null) {
			instance = new HhDroid(pContext);
		}

		return instance;
	}

}
