package com.hh.ui.widget;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.PopupMenu;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.hh.droid.R;

/**
 * Created by benhadjahameda on 04/02/2015.
 */
public class UiImageProgress extends LinearLayout implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    private int _mWidth;
    private int _mHeight;

    public interface OnUiPickImageProgressListener{
        public void onPickFromCamera(UiImageProgress uiImageProgress);
        public void onPickFromGallery(UiImageProgress uiImageProgress);
    }

    private  Context _mContext;
    private Button _mBtnPicture;
    private Button _mBtnRemovePicture;
    private ImageView _mImMainPicture;
    private TextView _mTvTitle;
    public OnUiPickImageProgressListener _mOnUiPickImageProgressListener;

    public UiImageProgress(Context context, AttributeSet attrs) {
        super(context, attrs);

        _mContext=context;
        LayoutInflater lInflater = LayoutInflater.from(context);
        lInflater.inflate(R.layout.ui_imageprogress, this);

        _mImMainPicture= (ImageView) findViewById(R.id.UiImP_ImMainPic);

        _mBtnPicture= (Button) findViewById(R.id.UiImP_BtnPict);
        _mBtnPicture.setOnClickListener(this);

        _mBtnRemovePicture= (Button) findViewById(R.id.UiImP_BtnRemovePic);
        _mBtnRemovePicture.setOnClickListener(this);

        _mTvTitle= (TextView) findViewById(R.id.UiImP_Title);

        setAttrs(context, attrs);
    }

    public void setOnUiPickImageProgressListener(OnUiPickImageProgressListener pListener){
        _mOnUiPickImageProgressListener=pListener;
    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void setMainImage(Bitmap pImage){
        Log.i("","UiImage Size WIDTH is :" + pImage.getWidth());
        Log.i("","UiImage Size is HEIGHT  :"+pImage.getHeight());
        _mImMainPicture.setImageBitmap(pImage);
        _mBtnPicture.setVisibility(GONE);
        _mBtnRemovePicture.setVisibility(VISIBLE);
    }

    public Drawable getImage(){
        return _mImMainPicture.getDrawable();
    }
    public void setMainImageErro(){
        removeMainPicture(false);
        Toast.makeText(_mContext,_mContext.getString(R.string.errorPickPicture),Toast.LENGTH_LONG).show();
    }
    public void showMenu(){
        PopupMenu popup = new PopupMenu(_mContext, _mBtnPicture);
        MenuInflater inflate = popup.getMenuInflater();
        inflate.inflate(R.menu.image_progress, popup.getMenu());
        popup.setOnMenuItemClickListener(this);
        popup.show();

    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.UiImP_BtnPict) {
            showMenu();

        } else if (i == R.id.UiImP_BtnRemovePic) {
            removeMainPicture(true);
        }
    }



    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        int i = menuItem.getItemId();
        if (i == R.id.uiImageProgress_takePicture) {
            if(_mOnUiPickImageProgressListener!=null) _mOnUiPickImageProgressListener.onPickFromCamera(this);
            return true;
        } else if (i == R.id.uiImageProgress_Gallery) {
            if(_mOnUiPickImageProgressListener!=null) _mOnUiPickImageProgressListener.onPickFromGallery(this);
            return true;
        } else {
            return false;
        }
    }

    private void setAttrs(Context pContext,AttributeSet pAttributes) {
        TypedArray a = pContext.obtainStyledAttributes(pAttributes, R.styleable.UiImageProgressAttrs);
        final int N = a.getIndexCount();
        for (int i = 0; i < N; ++i)
        {
            int attr = a.getIndex(i);
            if (attr == R.styleable.UiImageProgressAttrs_Imagetitle) {
                String lText = a.getString(attr);
                _mTvTitle.setText(lText);

            }
        }
        a.recycle();
    }

    private void removeMainPicture(boolean pWithConfirm){

        if(pWithConfirm) {
            new AlertDialog.Builder(_mContext)
                    .setTitle(R.string.title_delete_image)
                    .setMessage(R.string.msg_delete_image)
                    .setPositiveButton(R.string.lab_delete, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            _mImMainPicture.setImageBitmap(null);
                            _mImMainPicture.setImageDrawable(null);
                            _mBtnPicture.setVisibility(VISIBLE);
                            _mBtnRemovePicture.setVisibility(GONE);
                        }
                    })
                    .setNegativeButton(R.string.lab_no,null)
                    .show();
        }else{
            _mImMainPicture.setImageBitmap(null);
            _mImMainPicture.setImageDrawable(null);
            _mBtnPicture.setVisibility(VISIBLE);
            _mBtnRemovePicture.setVisibility(GONE);
        }
    }
}
