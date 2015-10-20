package com.hh.application;

import android.app.Application;

import com.hh.droid.HhDroid;
import com.hh.utility.PuDate;

public abstract class HhDroidApplication extends Application{

	protected PuDate mDate;
	private Thread.UncaughtExceptionHandler defaultUEH;
	private Thread.UncaughtExceptionHandler _unCaughtExceptionHandler =
			new Thread.UncaughtExceptionHandler() {
				@Override
				public void uncaughtException(Thread thread, Throwable ex) {

					onHandleUncaughtException(ex);
					// re-throw critical exception further to the os (important)
					defaultUEH.uncaughtException(thread, ex);
				}
			};

	@Override
	public void onCreate() {
		super.onCreate();
		mDate=new PuDate();
		initParams();

		// Pour les excetioon non gérer de l 'application
		defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(_unCaughtExceptionHandler);
	}

	protected void initParams(){
		HhDroid.getInstance().mDateFormat=mDate.getDateFormater();
		HhDroid.getInstance().mLocalDate=mDate.getLocal();
		HhDroid.getInstance().mCurrencySymbol=getCurrencySymbole();
	}

	protected abstract  String getCurrencySymbole();

	protected void onHandleUncaughtException(Throwable ex){

	}

}
