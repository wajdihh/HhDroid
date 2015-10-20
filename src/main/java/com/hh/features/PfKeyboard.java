package com.hh.features;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class PfKeyboard {

	public static void show(Context pContext,View pView){
		InputMethodManager lInputManager = (InputMethodManager) pContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		lInputManager.showSoftInput(pView, 0);
	}

	public static void hide(Context pContext,View pView){
		InputMethodManager lInputManager = (InputMethodManager) pContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		lInputManager.hideSoftInputFromWindow(pView.getWindowToken(), 0);
	}

	
}
