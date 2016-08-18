package com.hh.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.hh.droid.R;

/**
 * Created by WajdiHh on 11/08/2016
 * Email : wajdihh@gmail.com
 */
public class UiBooleanRadioGroup extends LinearLayout {

    private Context mContext;
    private TextView mTVLabel;
    private RadioGroup mRadioGroup;
    private RadioButton mRadioButtonYes;
    private RadioButton mRadioButtonNo;
    private String mTag="";
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
        mRadioGroup= (RadioGroup) findViewById(R.id.radioGroup);
        mRadioButtonYes= (RadioButton) findViewById(R.id.UiBooleanIDYes);
        mRadioButtonNo= (RadioButton) findViewById(R.id.UiBooleanIDNo);
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (mOnSelectedUiBooleanRGValue != null)
                    mOnSelectedUiBooleanRGValue.onSelectedValue(mRadioGroup,mTag, (i == R.id.UiBooleanIDYes));
            }
        });
    }

    public void setText(String text){
        mTVLabel.setText(text);
    }
    public void setPositiveRadioText(String text){
        mRadioButtonYes.setText(text);
    }
    public void setNegativeRadioText(String text){
        mRadioButtonNo.setText(text);
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
            }else if (attr == R.styleable.UiBooleanRadioGroupAttrs_positiveText) {
                String text = a.getString(R.styleable.UiBooleanRadioGroupAttrs_positiveText);
                setPositiveRadioText(text);
            }else if (attr == R.styleable.UiBooleanRadioGroupAttrs_negativeText) {
                String text = a.getString(R.styleable.UiBooleanRadioGroupAttrs_negativeText);
                setNegativeRadioText(text);

            } else if (attr == R.styleable.UiBooleanRadioGroupAttrs_android_tag) {
                mTag = a.getString(R.styleable.UiBooleanRadioGroupAttrs_android_tag);
            }
        }
        a.recycle();
    }

    public interface  OnSelectedUiBooleanRGValue{
        public void onSelectedValue(View clickedView,String tag,boolean isChecked);
    }
    private OnSelectedUiBooleanRGValue mOnSelectedUiBooleanRGValue;
    public void setOnSelectedUiBooleanRGValue(OnSelectedUiBooleanRGValue pOnSelectedUiBooleanRGValue){
        mOnSelectedUiBooleanRGValue=pOnSelectedUiBooleanRGValue;
    }
}
