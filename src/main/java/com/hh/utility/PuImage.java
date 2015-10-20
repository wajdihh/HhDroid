package com.hh.utility;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.widget.ImageView;
import android.widget.Toast;
import com.hh.droid.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by benhadjahameda on 13/03/2015.
 */
public class PuImage {

    public static void shareImage(Context pContext,ImageView image) {

        // Get access to the URI for the bitmap
        Uri bmpUri = getLocalBitmapUri(image);
        shareImage(pContext,bmpUri);
    }

    public static void shareImage(Context pContext,Uri bmpUri) {

        if (bmpUri != null) {
            // Construct a ShareIntent with link to image
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
            shareIntent.setType("image/*");
            // Launch sharing dialog for image
            pContext.startActivity(Intent.createChooser(shareIntent, "Share Image"));
        } else {
            Toast.makeText(pContext,pContext.getString(R.string.error_sharing),Toast.LENGTH_SHORT).show();
        }
    }

    public static void shareMultiImage(Context pContext,ArrayList<Uri> bmpUris) {

        if (bmpUris != null && bmpUris.size()!=0) {
            // Construct a ShareIntent with link to image
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.setType("image/*");

            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, bmpUris);
            // Launch sharing dialog for image
            pContext.startActivity(Intent.createChooser(shareIntent, "Share Image"));
        } else {
            Toast.makeText(pContext,pContext.getString(R.string.error_sharing),Toast.LENGTH_SHORT).show();
        }
    }
    // Returns the URI path to the Bitmap displayed in specified ImageView
    private static Uri getLocalBitmapUri(ImageView imageView) {
        // Extract Bitmap from ImageView drawable
        Drawable drawable = imageView.getDrawable();
        Bitmap bmp = null;
        if (drawable instanceof BitmapDrawable){
            bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } else {
            return null;
        }
        // Store image to default external storage directory
        Uri bmpUri = null;
        try {
            File file =  new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), "share_image_" + System.currentTimeMillis() + ".png");
            file.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

    /**
     * Permet de donner la largeur et la hauteur selon une valueur � ne pas d�passer
     * @param bitmap
     * @param boundBoxInDp
     * @return
     */
    public static MyRectangle scaleImage(Bitmap bitmap, int boundBoxInDp)
    {

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();


        float xScale = ((float) boundBoxInDp) / width;
        float yScale = ((float) boundBoxInDp) / height;
        float scale = (xScale <= yScale) ? xScale : yScale;

        // Create a matrix for the scaling and add the scaling data
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        // Create a new bitmap and convert it to a format understood by the ImageView
        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        width = scaledBitmap.getWidth();
        height = scaledBitmap.getHeight();

        return new MyRectangle(width,height);
    }

    /**
     * get the origine orientation of the image
     * @param imagePath
     * @return
     */

    public static int getImageOrientation(String imagePath) {
        int rotate = 0;
        try {
            File imageFile = new File(imagePath);
            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rotate;
    }

    public static class MyRectangle {

        private int height;
        private int width;

        public MyRectangle(int width,int height) {
            this.width=width;
            this.height=height;
        }

        public int getHeight(){
            return height;
        }

        public int getWidth(){
            return width;
        }
    }
}
