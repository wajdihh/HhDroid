package com.hh.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.hh.droid.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Transformation;

import java.io.File;
import java.util.concurrent.Executors;

/**
 * Created by wajdihh on 01/01/2016.
 */
public class UiPicassoImageView extends ImageView {

    private Picasso mPicasso;
    private RequestCreator mPicassoRequestCreator;
    private String mUrl;
    public UiPicassoImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mPicasso = new Picasso.Builder(context).executor(Executors.newSingleThreadExecutor()).build();

        setAttrs(context, attrs);
    }

    public void setData(String uri){
        // SI on a pas encore chargé l image par picosso

        mUrl=uri;
        if (uri.contains("http"))
            mPicassoRequestCreator= mPicasso.load(uri);
        else
            mPicassoRequestCreator= mPicasso.load(new File(uri));

        mPicassoRequestCreator.fit().centerCrop().noFade().into(this);
    }

    public RequestCreator getPicassoRequestCreator(){
        return mPicassoRequestCreator;
    }

    public void transform(Transformation transformation){

        mPicasso.cancelRequest(this);
        if (mUrl.contains("http"))
            mPicassoRequestCreator= mPicasso.load(mUrl);
        else
            mPicassoRequestCreator= mPicasso.load(new File(mUrl));

        mPicassoRequestCreator.transform(transformation).into(this);
    }

    public void resize(int w,int h){

        mPicasso.cancelRequest(this);
        if (mUrl.contains("http"))
            mPicassoRequestCreator= mPicasso.load(mUrl);
        else
            mPicassoRequestCreator= mPicasso.load(new File(mUrl));

        mPicassoRequestCreator.resize(w,h).centerCrop().into(this);;

    }

    public void placeHolder(int imageRes){
        mPicassoRequestCreator.placeholder(imageRes).into(this);
    }

    public void errorImage(int imageRes){
        mPicassoRequestCreator.error(imageRes).into(this);
    }

    public void placeHolder(Drawable image){
        mPicassoRequestCreator.placeholder(image).into(this);
    }

    public void errorImage(Drawable image){
        mPicassoRequestCreator.error(image).into(this);
    }
    public void setImagesResources(int placeHolderImRes,int errorImageRes){
        mPicassoRequestCreator.placeholder(placeHolderImRes).error(errorImageRes).into(this);;
    }

    private void setAttrs(Context pContext,AttributeSet pAttributes) {
        TypedArray a = pContext.obtainStyledAttributes(pAttributes, R.styleable.UiPicassoImageViewAttrs);
        final int N = a.getIndexCount();
        for (int i = 0; i < N; ++i)
        {
            int attr = a.getIndex(i);
            if (attr == R.styleable.UiPicassoImageViewAttrs_placeHolderImage) {
                Drawable drawable = a.getDrawable(R.styleable.UiPicassoImageViewAttrs_placeHolderImage);
                if (drawable != null)
                    placeHolder(drawable);

            } else if (attr == R.styleable.UiPicassoImageViewAttrs_errorImage) {
                Drawable drawable = a.getDrawable(R.styleable.UiPicassoImageViewAttrs_errorImage);
                if (drawable != null)
                    errorImage(drawable);

            }
        }
        a.recycle();
    }
}
