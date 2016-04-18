package com.hh.ui.widget;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.hh.droid.R;
import com.hh.features.PfKeyboard;
import com.hh.ui.UiUtility;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by benhadjahameda on 04/02/2015.
 */
public class UiEditTextDate extends LinearLayout {

    private final Context _myContext;
    private EditText _mEdDay;
    private EditText _mEdMonth;
    private EditText _mEdYear;
    private LinearLayout _mParent;

    public UiEditTextDate(Context context, AttributeSet attrs) {
        super(context, attrs);
        _myContext=context;
        LayoutInflater lInflater = LayoutInflater.from(_myContext);
        lInflater.inflate(R.layout.ui_edittextdate, this);

        _mEdDay= (EditText) findViewById(R.id.uiEditTextDate_EdDateDay);
        _mParent= (LinearLayout) findViewById(R.id.uiEditTextDateParent);

        _mEdDay.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {

                if(editable.length()==1 && Integer.parseInt(editable.toString())>3){
                    editable.insert(0,"0");
                    _mEdMonth.setFocusableInTouchMode(true);
                    _mEdMonth.requestFocus();
                }
                if(editable.length()==2) {
                    _mEdMonth.setFocusableInTouchMode(true);
                    _mEdMonth.requestFocus();
                }
            }
        });
        _mEdMonth= (EditText) findViewById(R.id.uiEditTextDate_EdDateMonth);
        _mEdMonth.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}
            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length()==1 && Integer.parseInt(editable.toString())>1){
                    editable.insert(0,"0");
                    _mEdYear.setFocusableInTouchMode(true);
                    _mEdYear.requestFocus();
                }
                if(editable.length()==2) {
                    _mEdYear.setFocusableInTouchMode(true);
                    _mEdYear.requestFocus();
                }
            }
        });
        _mEdMonth.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL && _mEdMonth.getText().length() == 0) {
                    _mEdDay.setFocusableInTouchMode(true);
                    _mEdDay.requestFocus();
                }
                return false;
            }
        });
        _mEdYear= (EditText) findViewById(R.id.uiEditTextDate_EdDateYear);
        _mEdYear.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}
            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length()==4) {
                    UiUtility.clearAllChildrensFocus(_mParent);
                    PfKeyboard.hide(_myContext,_mEdYear);
                }
            }
        });

        _mEdYear.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL && _mEdYear.getText().length() == 0) {
                    _mEdMonth.setFocusableInTouchMode(true);
                    _mEdMonth.requestFocus();
                }
                return false;
            }
        });

    }

    public EditText getEdDay(){
        return _mEdDay;
    }
    public int getDay(){
        String lContent=_mEdDay.getText().toString();
        if(lContent.equals(""))
            return 0;

        int lValue=Integer.parseInt(lContent);
        if(lValue<0 || lValue>31){
            _mEdDay.setError(_myContext.getString(R.string.error_day));
            _mEdDay.requestFocus();
            return -1;
        }
        return lValue;
    }

    public void setDate(Date date){

        Calendar c = Calendar.getInstance();
        c.setTime(date);
        _mEdDay.setText("" + c.get(Calendar.DAY_OF_MONTH));
        _mEdMonth.setText(""+(c.get(Calendar.MONTH)+1));
        _mEdYear.setText(""+c.get(Calendar.YEAR));
    }
    public int getMonth(){
        String lContent=_mEdMonth.getText().toString();
        if(lContent.equals(""))
            return 0;

        int lValue=Integer.parseInt(lContent);
        if(lValue<0 || lValue>12){
            _mEdMonth.setError(_myContext.getString(R.string.error_month));
            _mEdMonth.requestFocus();
            return -1;
        }
        return lValue;
    }
    public int getYear(){
        String lContent=_mEdYear.getText().toString();
        if(lContent.equals(""))
            return 0;

        int lValue=Integer.parseInt(lContent);
        if(lValue<1950 || lValue>2050){
            _mEdYear.setError(_myContext.getString(R.string.error_year));
            _mEdYear.requestFocus();
            return -1;
        }
        return lValue;
    }
}
