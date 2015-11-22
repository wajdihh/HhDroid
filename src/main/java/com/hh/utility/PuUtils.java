
/*
 * Copyright 2013 Wajdi Hh "wajdihh@gmail.com".
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
package com.hh.utility;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import com.hh.droid.R;

import java.io.*;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PuUtils {


	/**To display alert dialog 
	 * @author wajdihh
	 * Display dialog
	 * @param pContext
	 * @param pTitle
	 * @param pMessage
	 */
	public static void showMessage(Context pContext,String pTitle, String pMessage) {

		if(Looper.getMainLooper()==Looper.myLooper()){
			AlertDialog.Builder dialog = new AlertDialog.Builder(pContext);
			dialog.setTitle(pTitle);
			dialog.setMessage(pMessage);
			dialog.setNeutralButton("Ok", null);
			dialog.create().show();
		}else
			Log.e("ShowMessageLog", pTitle+" : "+pMessage);
	}

	/**
	 * to validate Hex format
	 * @author wajdihh
	 * * @param pHex
	 * * @return true is validate
	 */
	public static boolean isValidHex(String pHex){

		Pattern lPattern = Pattern.compile("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$");
		Matcher lMatcher = lPattern.matcher(pHex);
		return lMatcher.matches();


	}

	public static boolean isValidEmail(String email) {
		Pattern pattern = Patterns.EMAIL_ADDRESS;
		return pattern.matcher(email).matches();
	}
	/**
	 * to query columns names of a Table from database
	 * @author wajdihh
	 * @param pDatabase
	 * @param pTableName
	 * @return list of columns
	 */
	public static ArrayList<String> getTableColumnsNames(SQLiteDatabase pDatabase,String pTableName){
		ArrayList<String> lListOfColumns=new ArrayList<String>();
		Cursor lCursor = pDatabase.rawQuery("PRAGMA table_info(" + pTableName + ")", null);
		while(lCursor.moveToNext())
			lListOfColumns.add(lCursor.getString(1));
		return lListOfColumns;
	}

	public static void printKeyHash(Activity pActivity){
		// Add code to print out the key hash
		try {
			PackageInfo info = pActivity.getPackageManager().getPackageInfo(pActivity.getPackageName(),PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
			}
		} catch (PackageManager.NameNotFoundException e) {
			Log.d("KeyHash:", e.toString());
		} catch (NoSuchAlgorithmException e) {
			Log.d("KeyHash:", e.toString());
		}
	}


	/**
	 * Méthode pour ouvrir une application etxterne
	 * @param pContext
	 */
	public static void openApp(Context pContext,String pPackageName){
		Intent launchIntent = pContext.getPackageManager().getLaunchIntentForPackage(pPackageName);
		pContext.startActivity(launchIntent);
	}

	/**
	 * Méthode qui permet de lancer la gallerie photos
	 * @param pContext
	 */
	public static void openGallery(Context pContext)
	{
		Intent galleryIntent = new Intent(Intent.ACTION_VIEW, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		pContext.startActivity(galleryIntent);
	}

	/**
	 * Méthode permet d'ouvir une URL dans un navigateur
	 * @param pContext
	 * @param pUrl
	 */
	public static void openUrl(Context pContext,String pUrl){
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(pUrl));
		pContext.startActivity(browserIntent);
	}


	/**
	 * Permet d' inviter l utilisateur à installer une application si elle n existe pas
	 */
	public static void showAlertInstallAppPlay(final Context pContext, final String packageName,String appName) {

		String msg=pContext.getString(R.string.msg_install_app_part1)+" "+appName+" "+pContext.getString(R.string.msg_install_app_part2);
		android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(pContext)
				.setPositiveButton(R.string.btn_install, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						openAppInGooglePlay(pContext,packageName);
					}
				})
				.setNegativeButton(R.string.btn_otherTime,null)
				.setTitle(R.string.lab_info)
				.setMessage(msg)
				.create();
		alertDialog.show();
	}


	/**
	 * Pemert d ouvrir google play sur l'application selectionnée
	 * @param pContext
	 * @param pPackageName
	 */
	public static void openAppInGooglePlay(Context pContext,String pPackageName){
		try {
			pContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + pPackageName)));
		} catch (android.content.ActivityNotFoundException anfe) {
			pContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + pPackageName)));
		}
	}

	/**
	 * Méthode pour afficher un progressBar de chargement avec un délais définit
	 * @param pContext
	 * @param delay
	 */
	public static void runProgressWaiter(Context pContext,int delay){

		final ProgressDialog progress=new ProgressDialog(pContext);
		progress.setMessage(pContext.getString(R.string.msg_waiting_loading));
		progress.show();
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {

				progress.hide();
			}
		}, delay);
	}


	/**
	 * permet d'exporter la base de données dans la carte SD
	 * @param pContext
	 * @param pDatabaseName
	 */

	public static void saveDBInSdcard(Context pContext,String pDatabaseName){
		try {
			File sd = Environment.getExternalStorageDirectory();
			File data = Environment.getDataDirectory();

			if (sd.canWrite()) {
				String currentDBPath = "//data//"+ pContext.getPackageName() +"//databases//"+pDatabaseName;
				String backupDBPath = pDatabaseName;
				File currentDB = new File(data, currentDBPath);
				File backupDB = new File(sd, backupDBPath);

				FileChannel src = new FileInputStream(currentDB).getChannel();
				FileChannel dst = new FileOutputStream(backupDB).getChannel();
				dst.transferFrom(src, 0, src.size());
				src.close();
				dst.close();
				Toast.makeText(pContext, backupDB.toString(), Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			Toast.makeText(pContext, e.toString(), Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * permet de recuperer la dernier chaine du un chemin
	 *   EXP sdcard/loca/s0/Documentation temporaire/S31/  => Donnera "S31"
	 * @param path
	 * @return
	 */
	public static String getLastOfPath(String path){
		String[] names=path.split("/");
		return names[names.length-1];
	}

	public static boolean containsIgnoreCase(String src, String what) {
		final int length = what.length();
		if (length == 0)
			return true; // Empty string is contained

		final char firstLo = Character.toLowerCase(what.charAt(0));
		final char firstUp = Character.toUpperCase(what.charAt(0));

		for (int i = src.length() - length; i >= 0; i--) {
			// Quick check before calling the more expensive regionMatches() method:
			final char ch = src.charAt(i);
			if (ch != firstLo && ch != firstUp)
				continue;

			if (src.regionMatches(true, i, what, 0, length))
				return true;
		}

		return false;
	}

	public static String getDeviceId(Context pContext){
		return Settings.Secure.getString(pContext.getContentResolver(), Settings.Secure.ANDROID_ID);
	}

	public static void setBadge(Context context, int count) {
		String launcherClassName = getLauncherClassName(context);
		if (launcherClassName == null) {
			return;
		}
		Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
		intent.putExtra("badge_count", count);
		intent.putExtra("badge_count_package_name", context.getPackageName());
		intent.putExtra("badge_count_class_name", launcherClassName);
		context.sendBroadcast(intent);
	}

	public static String getLauncherClassName(Context context) {

		PackageManager pm = context.getPackageManager();

		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);

		List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
		for (ResolveInfo resolveInfo : resolveInfos) {
			String pkgName = resolveInfo.activityInfo.applicationInfo.packageName;
			if (pkgName.equalsIgnoreCase(context.getPackageName())) {
				String className = resolveInfo.activityInfo.name;
				return className;
			}
		}
		return null;
	}

	private static String convertToHex(byte[] data) {
		StringBuilder buf = new StringBuilder();
		for (byte b : data) {
			int halfbyte = (b >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
				halfbyte = b & 0x0F;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	}

	public static String SHA1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		md.update(text.getBytes("iso-8859-1"), 0, text.length());
		byte[] sha1hash = md.digest();
		return convertToHex(sha1hash);
	}

	public static String getDeviceName() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model.startsWith(manufacturer)) {
			return capitalize(model);
		} else {
			return capitalize(manufacturer) + " " + model;
		}
	}


	private static String capitalize(String s) {
		if (s == null || s.length() == 0) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		} else {
			return Character.toUpperCase(first) + s.substring(1);
		}
	}

	public  static void shareOnFacebook(Context pContext,String urlToShare){

		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		// intent.putExtra(Intent.EXTRA_SUBJECT, "Foo bar"); // NB: has no effect!
		intent.putExtra(Intent.EXTRA_TEXT, urlToShare);

		// See if official Facebook app is found
		boolean facebookAppFound = false;
		List<ResolveInfo> matches = pContext.getPackageManager().queryIntentActivities(intent, 0);
		for (ResolveInfo info : matches) {
			if (info.activityInfo.packageName.toLowerCase().startsWith("com.facebook.katana")) {
				intent.setPackage(info.activityInfo.packageName);
				facebookAppFound = true;
				break;
			}
		}

		// As fallback, launch sharer.php in a browser
		if (!facebookAppFound) {
			String sharerUrl = "https://www.facebook.com/sharer/sharer.php?u=" + urlToShare;
			intent = new Intent(Intent.ACTION_VIEW, Uri.parse(sharerUrl));
		}

		pContext.startActivity(intent);
	}
	public  static void shareOnTwitter(Context pContext,String urlToShare){

		Intent tweetIntent = new Intent(Intent.ACTION_SEND);
		tweetIntent.putExtra(Intent.EXTRA_TEXT, urlToShare);
		tweetIntent.setType("text/plain");

		PackageManager packManager = pContext.getPackageManager();
		List<ResolveInfo> resolvedInfoList = packManager.queryIntentActivities(tweetIntent,  PackageManager.MATCH_DEFAULT_ONLY);

		boolean resolved = false;
		for(ResolveInfo resolveInfo: resolvedInfoList){
			if(resolveInfo.activityInfo.packageName.startsWith("com.twitter.android")){
				tweetIntent.setClassName(
						resolveInfo.activityInfo.packageName,
						resolveInfo.activityInfo.name );
				resolved = true;
				break;
			}
		}
		if(resolved){
			pContext.startActivity(tweetIntent);
		}else{
			Intent i = new Intent();
			i.putExtra(Intent.EXTRA_TEXT, urlToShare);
			i.setAction(Intent.ACTION_VIEW);
			i.setData(Uri.parse("https://twitter.com/intent/tweet?text=message&via=profileName"));
			pContext.startActivity(i);
		}
	}

}
