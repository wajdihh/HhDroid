package com.hh.utility;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;
import com.hh.droid.R;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by WajdiHh on 14/10/2015.
 * Email : wajdihh@gmail.com
 */
public class PuFile {

    public enum ViewerDataType{Video,Audio,Doc,Image}

    private static final char UNIX_SEPARATOR = '/';
    private static final char WINDOWS_SEPARATOR = '\\';

    public static void download(String url,String path) throws IOException, URISyntaxException {

        int count=0;
        // C'est pour l encodage de l URL mettre l url dans une URI et puis l inverse
        URL myUrl = new URL(url);
        URI uri = new URI(myUrl.getProtocol(), myUrl.getUserInfo(), myUrl.getHost(), myUrl.getPort(), myUrl.getPath(), myUrl.getQuery(), myUrl.getRef());
        myUrl = uri.toURL();
        URLConnection connection = myUrl.openConnection();
        connection.setConnectTimeout(3000);
        connection.connect();
        // input stream to read file - with 8k buffer
        InputStream input = new BufferedInputStream(myUrl.openStream(), 8192);

        // Output stream to write file
        OutputStream output = new FileOutputStream(path);

        byte data[] = new byte[1024];

        while ((count = input.read(data)) != -1) {
            output.write(data, 0, count);
        }
        // flushing output
        output.flush();
        // closing streams
        output.close();
        input.close();
    }

    /**
     * Permet de supprimer le contenu d'un dossier
     * @param folder
     * @throws IOException
     */
    public static void deleteDirectory(File folder)
            throws IOException {
        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteDirectory(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }


    /**
     * permet d'avoir l extension d'un fichier
     * @param filename
     * @return
     */
    public static String getExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int index = indexOfExtension(filename);
        if (index == -1) {
            return "";
        } else {
            return filename.substring(index + 1);
        }
    }

    private static int indexOfExtension(String filename) {
        if (filename == null) {
            return -1;
        }
        int extensionPos = filename.lastIndexOf('.');
        int lastSeparator = indexOfLastSeparator(filename);
        return lastSeparator > extensionPos ? -1 : extensionPos;
    }

    private static int indexOfLastSeparator(String filename) {
        if (filename == null) {
            return -1;
        }
        int lastUnixPos = filename.lastIndexOf(UNIX_SEPARATOR);
        int lastWindowsPos = filename.lastIndexOf(WINDOWS_SEPARATOR);
        return Math.max(lastUnixPos, lastWindowsPos);
    }

    /**
     * Permet d'ouvrir un fichier PDF via intent
     * @param context
     * @param path
     */
    public static void openFile(Context context,String path,ViewerDataType dataType){

        File file = new File(path);
        Intent intent = new Intent(Intent.ACTION_VIEW);

        if(dataType==ViewerDataType.Doc)
            intent.setDataAndType(Uri.fromFile(file), "application/pdf");
        else if(dataType==ViewerDataType.Audio)
            intent.setDataAndType(Uri.fromFile(file), "audio/*");
        else if(dataType==ViewerDataType.Video)
            intent.setDataAndType(Uri.fromFile(file), "video/*");
        else if(dataType==ViewerDataType.Image)
            intent.setDataAndType(Uri.fromFile(file), "image/*");


        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        context.startActivity(intent);
    }

    public static void shareFile(Context pContext,Uri bmpUri) {

        if (bmpUri != null) {
            // Construct a ShareIntent with link to image
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
            shareIntent.setType("image/*");
            // Launch sharing dialog for image
            pContext.startActivity(Intent.createChooser(shareIntent, "Share File"));
        } else {
            Toast.makeText(pContext, pContext.getString(R.string.error_sharing), Toast.LENGTH_SHORT).show();
        }
    }

    public static String getPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
