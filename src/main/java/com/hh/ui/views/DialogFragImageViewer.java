package com.hh.ui.views;

import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.hh.droid.R;
import com.squareup.picasso.Picasso;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by benhadjahameda on 11/03/2015.
 */
public class DialogFragImageViewer extends Fragment {

    private static String _mURL;
    private static Drawable _mDrawable;
    private ImageView _mImageView;
    private PhotoViewAttacher mAttacher;

    public static DialogFragImageViewer newInstance(String pUrl){
        DialogFragImageViewer f=new DialogFragImageViewer();
        Bundle b=new Bundle();
        b.putString("url",pUrl);
        f.setArguments(b);
        return f;
    }
    public static DialogFragImageViewer newInstance(Drawable pDrawable){
        DialogFragImageViewer f=new DialogFragImageViewer();
        _mDrawable=pDrawable;
        return f;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _mURL=getArguments()!=null?getArguments().getString("url"):null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.dialogfrg_imageviewer, container, false);

        _mImageView= (ImageView) rootView.findViewById(R.id.uiDIalogImViewer_im);
        mAttacher = new PhotoViewAttacher(_mImageView);
        if(_mURL!=null)
            picassoLoadImage(_mImageView,_mURL);
        else
            _mImageView.setImageDrawable(_mDrawable);

        return rootView;
    }

    public ImageView getImageView(){
        return _mImageView;
    }
    private void picassoLoadImage(ImageView pImageView,String url){
        Picasso.with(getActivity())
                .load(url)
                .placeholder(R.drawable.default_picture)
                .error(R.drawable.default_picture)
                .into(pImageView);
    }

}