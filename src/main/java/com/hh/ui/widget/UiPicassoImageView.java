package com.hh.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
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

    public UiPicassoImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mPicasso = new Picasso.Builder(context).executor(Executors.newSingleThreadExecutor()).build();

        setAttrs(context, attrs);
    }

    public void setData(String uri){
        // SI on a pas encore charg� l image par picosso
        if (uri.contains("http"))
            mPicassoRequestCreator= mPicasso.load(uri);
        else
            mPicassoRequestCreator= mPicasso.load(new File(uri));

        mPicassoRequestCreator.fit().centerCrop().into(this);;
    }

    public RequestCreator getPicassoRequestCreator(){
        return mPicassoRequestCreator;
    }

    public void transform(Transformation transformation){
         mPicassoRequestCreator.transform(transformation).into(this);
    }

    public void resize(int w,int h){
        mPicassoRequestCreator.resize(w,h).centerCrop().into(this);;
    }

    public void placeHolder(int imageRes){
        mPicassoRequestCreator.placeholder(imageRes).into(this);
    }

    public void errorImage(int imageRes){
        mPicassoRequestCreator.error(imageRes).into(this);
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
                int resID=a.getResourceId(R.styleable.UiPicassoImageViewAttrs_placeHolderImage,R.drawable.ic_action_delete);
                placeHolder(resID);

            } else if (attr == R.styleable.UiPicassoImageViewAttrs_errorImage) {
                int resID=a.getResourceId(R.styleable.UiPicassoImageViewAttrs_errorImage, R.drawable.ic_action_delete);
                errorImage(resID);

            }
        }
        a.recycle();
    }
}
