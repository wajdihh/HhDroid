package com.hh.utility;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.widget.ImageView;
import android.widget.Toast;
import com.hh.droid.R;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;

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
     * Permet de donner la largeur et la hauteur selon une valueur à ne pas dépasser
     * @param bitmap
     * @param boundBoxInDp
     * @return
     */
    public static Bitmap scaleImage(Bitmap bitmap, int boundBoxInDp)
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
        return  Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
    }

    public static int getImageOrientationOnGallery(Context pContext,Uri imageUri){
        String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media.ORIENTATION};
        Cursor cursor = pContext.getContentResolver().query(imageUri, columns, null, null, null);

        if (cursor == null)
            return 0;


        cursor.moveToFirst();

        int orientationColumnIndex = cursor.getColumnIndex(columns[1]);

        return cursor.getInt(orientationColumnIndex);
    }

    public static File rotateImageFile(File file){

        Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());

        Matrix matrix = new Matrix();
        matrix.postRotate(getImageOrientation(file.getAbsolutePath()));
        bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
        FileOutputStream fOut;
        try {
            fOut = new FileOutputStream(file.getAbsolutePath());
            bmp.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
            fOut.flush();
            fOut.close();

        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return file;
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
                case ExifInterface.ORIENTATION_TRANSVERSE:
                    rotate = -90;
                    break;
                case ExifInterface.ORIENTATION_NORMAL:
                    rotate = 0;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rotate;
    }

    public static Bitmap rotateImage(Bitmap source, int angleOrientation) {
        Bitmap retVal;

        Matrix matrix = new Matrix();
        matrix.postRotate(angleOrientation);
        retVal = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);

        return retVal;
    }

    public static void scanFileInGallery(final Context pContext,File f, final boolean isDelete){

        MediaScannerConnection.scanFile(pContext,
                new String[]{f.getAbsolutePath()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        if (isDelete) {
                            if (uri != null) {
                                pContext.getContentResolver().delete(uri, null, null);
                            }
                        }
                    }
                });
    }

    public static void saveBitmapToSdCard(Bitmap bitmap,String destPath,String destName) {

        File myDir = new File(destPath);
        myDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        File file = new File (myDir, destName);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveBitmapToJPG(Bitmap bitmap, File photo) throws IOException {
        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, 0, 0, null);
        OutputStream stream = new FileOutputStream(photo);
        newBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        stream.close();
    }

    public static String getImageNameFromURI(Context pContext,Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = pContext.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public static Bitmap resizeImage(File origin, double percentage) {

        // we'll start with the original picture already open to a file
        Bitmap b = BitmapFactory.decodeFile(origin.getAbsolutePath());
        return Bitmap.createScaledBitmap(b,(int)(b.getWidth()*percentage), (int)(b.getHeight()*percentage), true);
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
