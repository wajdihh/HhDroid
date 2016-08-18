
/*
 * Copyright 2013 Wajdi Hh "wajdihh@gmail.com" .
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hh.ui;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import com.hh.features.PfKeyboard;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * Class containing static methods utilities
 * 
 * @author WajdiHh : Last modification : 23/05/2013
 *
 */
public class UiUtility {

	
	/**<br>
	 * Clear focus from all children's parent view
	 * 
	 * @param pParentView : The parent view which contains the focused children's
	 */
	public static void clearAllChildrensFocus(ViewGroup pParentView){

		pParentView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
		pParentView.setFocusableInTouchMode(true);
		pParentView.requestFocus();

	}
	/**<br>
	 * Convert Dp unit to pixel unit 
	 * 
	 * @author WajdiHh
	 * @param pContext :the context the view is running in
	 * @param pDpValue : the value on DP unit
	 * @return : the convert pixel value
	 */
	public static int getPxFromDp(Context pContext,float pDpValue){ 
		float lscale = pContext.getResources().getDisplayMetrics().density;
		return (int) (pDpValue * lscale + 0.5f);
	}

	/**<br>
	 * convert color from integer value to hex value
	 *
	 *@author WajdiHh
	 * @param pColor : Integer value
	 * @return String  : Hex value
	 */
	public static String colorConvertIntegerToHex(int pColor){
		return String.format("#%06X", 0xFFFFFF & pColor);
	}
	
	/**<br>
	 * convert color from hex value to integr value
	 * 
	 * @author WajdiHh
	 * @param pColor : hex value
	 * @return int : integer value
	 */
	public static int colorConvertHexToInteger(String pColor){
		return Color.parseColor(pColor);
	}
	
	/**<br>
	 * Create Rectangular Shape with specific color
	 * 
	 * @author WajdiHh
	 * @param bottom :  margin value
	 * @param top :  margin value
	 * @param right :  margin value
	 * @param left:  margin value
	 * @param color : the color of the shape
	 * @return ShapeDrawable: Rectangular Shape
	 */
	public static ShapeDrawable createShapRec(float bottom,float top,float right,float left, int color){

		ShapeDrawable footerBackground = new ShapeDrawable();

		float[] radii ={top,left,top,right,bottom,left,bottom,right};
		footerBackground.setShape(new RoundRectShape(radii, null, null));
		footerBackground.getPaint().setColor(color);

		return footerBackground;
	}
	
	/**<br>
	 * get the current rotation screen (portrait, landscape …)
	 * 
	 * @author WajdiHh
	 * @param context : the context the view is running in
	 * @return int : the constant screen rotation : 
	 * 
	 * <b>(Surface.ROTATION_0, Surface.ROTATION_90,Surface.ROTATION_180,Surface.ROTATION_270)</b>
	 */
	public static int getScreenRotation(Context context){
		
		WindowManager mWindowManager =  (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
	    Display  mDisplay = mWindowManager.getDefaultDisplay();
	 
	    return mDisplay.getRotation();
	}

    public int getPxFromDb(Context pContext,int pDimenResID){
        DisplayMetrics metrics = pContext.getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pContext.getResources().getDimension(pDimenResID), metrics);
    }

    public static Bitmap scaleImage(Context context, Uri photoUri,int pImageMaxDimension) throws IOException {

        InputStream is = context.getContentResolver().openInputStream(photoUri);
        if(is==null) return null;
        BitmapFactory.Options dbo = new BitmapFactory.Options();
        dbo.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, dbo);
        is.close();

        int rotatedWidth, rotatedHeight;
        int orientation = getOrientation(context, photoUri);

        if (orientation == 90 || orientation == 270) {
            rotatedWidth = dbo.outHeight;
            rotatedHeight = dbo.outWidth;
        } else {
            rotatedWidth = dbo.outWidth;
            rotatedHeight = dbo.outHeight;
        }

        Bitmap srcBitmap;
        is = context.getContentResolver().openInputStream(photoUri);
        if (rotatedWidth > pImageMaxDimension || rotatedHeight > pImageMaxDimension) {
            float widthRatio = ((float) rotatedWidth) / ((float) pImageMaxDimension);
            float heightRatio = ((float) rotatedHeight) / ((float) pImageMaxDimension);
            float maxRatio = Math.max(widthRatio, heightRatio);

            // Create the bitmap from file
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = (int) maxRatio;
            srcBitmap = BitmapFactory.decodeStream(is, null, options);
        } else {
            srcBitmap = BitmapFactory.decodeStream(is);
        }
        is.close();

        /*
         * if the orientation is not 0 (or -1, which means we don't know), we
         * have to do a rotation.
         */
        if (orientation > 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);

            srcBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.getWidth(), srcBitmap.getHeight(), matrix, true);
        }
        String type = context.getContentResolver().getType(photoUri);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (type.equals("image/png")) {
            srcBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        } else if (type.equals("image/jpg") || type.equals("image/jpeg")) {
            srcBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        }
        byte[] bMapArray = baos.toByteArray();
        baos.close();
        return BitmapFactory.decodeByteArray(bMapArray, 0, bMapArray.length);
    }

    public static int getOrientation(Context context, Uri photoUri) {
        /* it's on the external media. */
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);

        if (cursor.getCount() != 1) {
            return -1;
        }

        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    /**
     * Clear focus, when EditText is set with imeoption=done
     * @param pTextView
     */
    public static void clearFocusWhenKeyboardActionIsDone(final Context pContext,final ViewGroup parentView,final TextView pTextView){
        pTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    clearAllChildrensFocus(parentView);
                    PfKeyboard.hide(pContext,pTextView);
                }
                return false;
            }
        });

    }
}