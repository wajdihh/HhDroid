
package com.hh.droid;

import java.text.SimpleDateFormat;
import java.util.Locale;


public class HhDroid {

	public Locale mLocalDate;
	public SimpleDateFormat mDateFormat;
    public String mCurrencySymbol="";


	protected static HhDroid instance;

	public static HhDroid getInstance() {
		if (instance == null) {
			instance = new HhDroid();
		}

		return instance;
	}
}
