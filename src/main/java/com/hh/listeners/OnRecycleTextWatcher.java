package com.hh.listeners;

import android.widget.EditText;

/**
 * Created by Wajdi Hh on 13/08/2015.
 * wajdihh@gmail.com
 */
public interface OnRecycleTextWatcher {

    public void afterTextChanged(boolean pIsWidgetInCDT,EditText v, String newText, int position) ;
}
