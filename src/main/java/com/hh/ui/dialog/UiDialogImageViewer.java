package com.hh.ui.dialog;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.hh.droid.R;
import com.hh.listeners.OnUiDialogClickListener;
import com.hh.ui.views.DialogFragImageViewer;
import com.hh.utility.PuImage;

/**
 * Created by benhadjahameda on 10/03/2015.
 */
public class UiDialogImageViewer extends DialogFragment {

    private static String[] _mUrls;
    private static Drawable _mDrawable;

    private int _mIndexPager=0;
    private ViewPager mPager;
    private TextView _mTvNbr;
    private RelativeLayout _mNbrContainer;
    private LinearLayout _mBtnContainer;
    private PagerAdapter mPagerAdapter;
    private  Button _mBtnValidate;
    private static OnUiDialogClickListener _mOnClickListener;

    public static UiDialogImageViewer newInstance(String[] pUrls) {
        _mUrls=pUrls;
        _mDrawable=null;
        _mOnClickListener=null;
        return new UiDialogImageViewer();
    }
    public static UiDialogImageViewer newInstance(Drawable pDrawable) {
        _mDrawable=pDrawable;
        _mUrls=null;
        _mOnClickListener=null;
        return new UiDialogImageViewer();
    }

    public static UiDialogImageViewer newInstance(Drawable pDrawable,OnUiDialogClickListener pOnClickValidateListener) {
        _mDrawable=pDrawable;
        _mUrls=null;
        _mOnClickListener=pOnClickValidateListener;
        return new UiDialogImageViewer();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.ui_dialog_imageviwer, container, false);
        initView(v);
        return v;
    }

    private void initView(View v) {

        mPager = (ViewPager) v.findViewById(R.id.pager);
        _mNbrContainer= (RelativeLayout) v.findViewById(R.id.uiDIalogImViewer_LlNbr);
        _mBtnContainer= (LinearLayout) v.findViewById(R.id.uiDIalogImViewer_LlBtn);
        _mBtnValidate= (Button) v.findViewById(R.id.uiDIalogImViewer_btnValidate);
        _mBtnContainer.setVisibility(View.GONE);
        _mNbrContainer.setVisibility(View.VISIBLE);
        if(_mOnClickListener!=null) {
            _mOnClickListener.setDialogFragment(this);
            _mNbrContainer.setVisibility(View.GONE);
            _mBtnContainer.setVisibility(View.VISIBLE);
            _mBtnValidate.setOnClickListener(_mOnClickListener);
        }
        Button lBtnCancel=(Button) v.findViewById(R.id.uiDIalogImViewer_btnCancel);
        lBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UiDialogImageViewer.this.dismiss();
            }
        });

        mPagerAdapter = new ScreenSlidePagerAdapter(getChildFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
            }

            @Override
            public void onPageSelected(int i) {
                if (_mTvNbr != null && _mUrls != null) {
                    _mIndexPager=i;
                    _mTvNbr.setText((i + 1) + "/" + _mUrls.length);
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });
        mPager.setOffscreenPageLimit(10);
        _mTvNbr=(TextView) v.findViewById(R.id.uiDIalogImViewer_Nbr);
        if(_mUrls!=null)
        _mTvNbr.setText("1/"+_mUrls.length);
        if(_mDrawable!=null)
            _mTvNbr.setText("1/1");

        Button lBtnShare=(Button) v.findViewById(R.id.uiDIalogImViewer_btnShare);
        lBtnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragImageViewer lRoot = (DialogFragImageViewer) mPagerAdapter.instantiateItem(mPager, _mIndexPager);
                ImageView ivImage = lRoot.getImageView();
                PuImage.shareImage(getActivity(),ivImage);
            }
        });
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            if(_mUrls!=null)
                return  DialogFragImageViewer.newInstance(_mUrls[position]);

            return  DialogFragImageViewer.newInstance(_mDrawable);
        }


        @Override
        public int getCount() {
            return (_mUrls!=null)?_mUrls.length:1;
        }
    }

}
