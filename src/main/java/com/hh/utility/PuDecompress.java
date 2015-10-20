package com.hh.utility;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
/**
 * Created by Wajdi Hh on 20/08/2015.
 * wajdihh@gmail.com
 */
public class PuDecompress {

    private String _zipFile;
    private String _location;

    public PuDecompress(String zipFile, String location) {
        _zipFile = zipFile;
        _location = location;

        _dirChecker("");
    }

    public void unzip() throws IOException{

        FileInputStream fin = new FileInputStream(_zipFile);
        ZipInputStream zin = new ZipInputStream(fin);
        ZipEntry ze = null;
        while ((ze = zin.getNextEntry()) != null) {
            Log.v("Decompress", "Unzipping " + ze.getName());

            if(ze.isDirectory()) {
                _dirChecker(ze.getName());
            } else {
                FileOutputStream fout = new FileOutputStream(_location + ze.getName());
                for (int c = zin.read(); c != -1; c = zin.read()) {
                    fout.write(c);
                }

                zin.closeEntry();
                fout.close();
            }

        }
        zin.close();


    }

    private void _dirChecker(String dir) {
        File f = new File(_location + dir);

        Log.v("_dirChecker", "_dirChecker " + dir);
        if(!f.isDirectory()) {
            f.mkdirs();
            Log.v("isDirectory", "isDirectory " + dir);
        }
    }
}
