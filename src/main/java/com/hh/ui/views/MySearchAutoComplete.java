package com.hh.ui.views;

import android.content.Context;
import android.support.v7.widget.SearchView;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

/**
 * Created by Wajdi Hh on 14/08/2015.
 * wajdihh@gmail.com
 */



public class MySearchAutoComplete  extends SearchView.SearchAutoComplete{

    private OnSearchAutoCompleteBackClickListener _mOnSearchAutoCompleteBackClickListener;
    public MySearchAutoComplete(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // special case for the back key, we do not even try to send it
            // to the drop down list but instead, consume it immediately
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                KeyEvent.DispatcherState state = getKeyDispatcherState();
                if (state != null) {
                    state.startTracking(event, this);
                }
                return true;
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                KeyEvent.DispatcherState state = getKeyDispatcherState();
                if (state != null) {
                    state.handleUpEvent(event);
                }
                if (event.isTracking() && !event.isCanceled()) {
                    if(_mOnSearchAutoCompleteBackClickListener!=null) _mOnSearchAutoCompleteBackClickListener.onBackClick(this);
                    return true;
                }
            }
        }
        return true;

    }

    public void setOnSearchAutoCompleteBackClickListener(OnSearchAutoCompleteBackClickListener pOnSearchAutoCompleteBackClickListener){
        _mOnSearchAutoCompleteBackClickListener=pOnSearchAutoCompleteBackClickListener;
    }
    public interface OnSearchAutoCompleteBackClickListener{
        public void onBackClick(View v);
    }
}
