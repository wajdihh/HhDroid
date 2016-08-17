package com.hh.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.hh.droid.R;

/**
 * Created by WajdiHh on 11/08/2016
 * Email : wajdihh@gmail.com
 */
public class UiBooleanRadioGroup extends LinearLayout {

    private Context mContext;
    private TextView mTVLabel;
    public UiBooleanRadioGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext=context;
        initView();
        setAttrs(context, attrs);
    }

    private void initView() {
        LayoutInflater lInflater = LayoutInflater.from(mContext);
        lInflater.inflate(R.layout.ui_bool_rg, this);
        mTVLabel= (TextView) findViewById(R.id.tvLabel);
    }

    public void setText(String text){
        mTVLabel.setText(text);
    }

    public void setText(int textRes){
        mTVLabel.setText(textRes);
    }

    private void setAttrs(Context pContext,AttributeSet pAttributes) {
        TypedArray a = pContext.obtainStyledAttributes(pAttributes, R.styleable.UiBooleanRadioGroupAttrs);
        final int N = a.getIndexCount();
        for (int i = 0; i < N; ++i)
        {
            int attr = a.getIndex(i);
            if (attr == R.styleable.UiBooleanRadioGroupAttrs_text) {
                String text = a.getString(R.styleable.UiBooleanRadioGroupAttrs_text);
                setText(text);
            }
        }
        a.recycle();
    }
}
