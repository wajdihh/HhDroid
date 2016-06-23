package com.hh.database;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.hh.droid.HhDroid;
import com.hh.droid.R;
import com.hh.execption.HhException;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Class allow to create or upgrade Sqlite data base from .db script in assets folder 
 * @author WajdiHh
 *
 */
public class DBSqliteOpenHelper extends SQLiteOpenHelper{

	private static final String mAssetsDBPathForCreate="DBCreate";
	private static final String mAssetsDBPathForUpdate="DBUpdate";	

	private Resources _mRes;
	private Context _mContext;

	public DBSqliteOpenHelper(Context context) {		
		super(context, context.getResources().getString(R.string.DataBaseName), null, HhDroid.getInstance(context).mDBVersion);
		_mContext=context;
		_mRes=context.getResources();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		ArrayList<String> lListOfSqlite;
		try {
			lListOfSqlite = getDBScriptFile(mAssetsDBPathForCreate);

			int lListSize=lListOfSqlite.size();
			StringWriter lWriter=new StringWriter();

			AssetManager lAssManager=_mRes.getAssets();

			for (int i = 0; i < lListSize; i++) {
				String lFileName=lListOfSqlite.get(i);

				InputStream lInputSream=lAssManager.open(mAssetsDBPathForCreate+"/"+lFileName);
				BufferedReader buffer=new BufferedReader(new InputStreamReader(lInputSream));

				String line="";
				while ( null!=(line=buffer.readLine())){
					if(!line.contains("-") && !line.contains("*") && !line.contains("/"))
						lWriter.write(line);
				}
				lWriter.close();
			}
			excuteCMD(lWriter, db);

			// Invoquer le onUpgrade avec 1 comme version ancienne de la base et 'lDBVersion' comme
			// version actuelle pour excuter les script de mise à jour
			onUpgrade(db, 1, HhDroid.getInstance(_mContext).mDBVersion);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (HhException e1) {
			e1.printStackTrace();
			_mContext.deleteDatabase(_mRes.getString(R.string.DataBaseName));
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
		try {
			ArrayList<String> lListOfSqlite=getDBScriptFile(mAssetsDBPathForUpdate);

			int lListSize=lListOfSqlite.size();
			StringWriter lWriter=new StringWriter();

			AssetManager lAssManager=_mRes.getAssets();

			for (int i = 0; i < lListSize; i++) {
				String lFileName=lListOfSqlite.get(i);

				Scanner lScanner = new Scanner(lFileName).useDelimiter("[^0-9]+");
				int lNumOfScriptFile = lScanner.nextInt();

				if(oldVersion< lNumOfScriptFile && lNumOfScriptFile <= newVersion){

					InputStream lInputSream=lAssManager.open(mAssetsDBPathForUpdate+"/"+lFileName);
					BufferedReader buffer=new BufferedReader(new InputStreamReader(lInputSream));

					String line="";
					while ( null!=(line=buffer.readLine()))
						lWriter.write(line);

					lWriter.close();
				}			
			}
			excuteCMD(lWriter, db); 			

		} catch (IOException e) {
			e.printStackTrace();
		} catch (HhException e1) {
			e1.printStackTrace();
		}
	}

	private void excuteCMD(StringWriter pWriter,SQLiteDatabase db){

		String[] lSqlCMD=pWriter.toString().split(";");
		for (int i = 0; i < lSqlCMD.length; i++) {
			String lCMD = lSqlCMD[i];
			if (! lCMD.trim().equals(""))
				db.execSQL(lCMD);  
		}
	}

	private ArrayList<String> getDBScriptFile(String pFolderName) throws HhException, IOException{

		ArrayList<String> lListScriptFiles=null;
		AssetManager lAssManager=_mRes.getAssets();

		String[] lTabFiles = lAssManager.list(pFolderName);
		int lTabFileSize=lTabFiles.length;

		if(lTabFileSize==0){
			throw new HhException(_mContext.getString(R.string.exception_assetPart1)+" "+pFolderName+" "+_mContext.getString(R.string.exception_assetPart2));
		}
		lListScriptFiles= new ArrayList<>();
		for (int i = 0; i < lTabFileSize; i++) 
			if(lTabFiles[i].contains(".db"))
				lListScriptFiles.add(lTabFiles[i]);
		
		if(lListScriptFiles.isEmpty())
			throw new HhException(_mContext.getString(R.string.exception_noScriptFound)+" "+pFolderName);

		return lListScriptFiles;
	}
}
