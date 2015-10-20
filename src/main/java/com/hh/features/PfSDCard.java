package com.hh.features;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class PfSDCard {

	public static Drawable getImageFromSdCard(String pPath,String pImageName) {
		Drawable d = null;
		try {
			Bitmap bitmap = BitmapFactory.decodeFile(pPath + "/" + pImageName);
			d = new BitmapDrawable(bitmap);

		} catch (IllegalArgumentException e) {}
		return d;
	}

	public static void downloadImageToSDCard(String pImageURL, String pPath,String pFileName) {
		try {
			URL myImageURL = new URL(pImageURL);

			HttpURLConnection connection = (HttpURLConnection) myImageURL.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();

			Bitmap myBitmap = BitmapFactory.decodeStream(input);

			OutputStream fOut = null;
			File lParentFolder = new File(pPath);
			if (!lParentFolder.exists()){ 
				lParentFolder.mkdirs();
				return;
			}
			File file = new File(lParentFolder, pFileName);
			fOut = new FileOutputStream(file);
			myBitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);
			fOut.flush();
			fOut.close();

		} catch (IOException e) {
		}

	}
}
