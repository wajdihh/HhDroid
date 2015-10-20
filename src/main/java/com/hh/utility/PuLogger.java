package com.hh.utility;

import android.content.Context;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Wajdi Hh on 23/07/2015.
 * Consultant Mobile de www.dauphineconsulting.com
 * wajdihh@gmail.com
 */
public class PuLogger {

    public static void appendLogFile(Context pContext,String pathFile,String tag,Throwable myEx)
    {
        File logFile = new File(pathFile+"/log.file");
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        try
        {
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));

            String content="############################################################################\n\n" +
                    ""+tag+" IN "+new SimpleDateFormat("dd/MM/yyyy  HH:mm").format(new Date().getTime());

            content+="\n\n"+Log.getStackTraceString(myEx);

            buf.append(content);
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {e.printStackTrace();
        }
    }
}
