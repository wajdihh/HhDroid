package com.hh.listeners;

import android.view.View;
import android.widget.RadioGroup;

/**
 * Created by Wajdi Hh on 13/08/2016.
 * wajdihh@gmail.com
 */
public interface OnRecycleCheckedRadioButtonGroupChangeListener {

    public void onCheckedChanged(View parentView,RadioGroup radioButtonGroup,String widgetTag,int radioButtonID, int position) ;
}
