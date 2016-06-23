package com.hh.application;

import android.app.Application;
import com.hh.utility.PuDate;

public abstract class HhDroidApplication extends Application{

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
		// Pour les excetioon non gérer de l 'application
		defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(_unCaughtExceptionHandler);
	}


	public abstract  String getCurrencySymbole();
	public abstract  PuDate getConfiguredDate();
	public abstract  int getDatabaseVersion();

	protected void onHandleUncaughtException(Throwable ex){

	}

}
