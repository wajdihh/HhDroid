package com.hh.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Scanner;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.hh.droid.R;
import com.hh.utility.PuException;

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
		super(context, context.getResources().getString(R.string.DataBaseName), null, DAOManager.mDBVersion);
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

		} catch (IOException e) {
			e.printStackTrace();
		} catch (PuException e1) {
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
		} catch (PuException e1) {
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

	private ArrayList<String> getDBScriptFile(String pFolderName) throws PuException, IOException{

		ArrayList<String> lListScriptFiles=null;
		AssetManager lAssManager=_mRes.getAssets();

		String[] lTabFiles = lAssManager.list(pFolderName);
		int lTabFileSize=lTabFiles.length;

		if(lTabFileSize==0){
			throw new PuException(_mContext, "Erreur", "Vous d�vez avoir un dossier :"+pFolderName+" sous assets dans lequel au moins un script base de donn�es .db");
		}
		lListScriptFiles=new ArrayList<String>();
		for (int i = 0; i < lTabFileSize; i++) 
			if(lTabFiles[i].contains(".db"))
				lListScriptFiles.add(lTabFiles[i]);
		
		if(lListScriptFiles.isEmpty())
			throw new PuException(_mContext, "Acun Script base de donn�es", "Acun script base de donn�es pr�sent dans le dossier : "+pFolderName);

		return lListScriptFiles;
	}
}
