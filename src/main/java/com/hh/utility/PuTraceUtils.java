package com.hh.utility;

import android.util.Log;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;

public final class PuTraceUtils {

    private static HashMap<String,Long> listTimes;
    static {
        listTimes=new HashMap<>();
    }

    public static void startCounter(String tag){
        long startTime = System.currentTimeMillis();
        listTimes.put(tag,startTime);
        System.out.println("#"+tag+"# {Start counting... }");
    }

    public static void stopCounter(String tag){
        long stopTime = System.currentTimeMillis();
        if (listTimes.containsKey(tag)) {
            long startTime=listTimes.get(tag);
            long elapsedTime = stopTime - startTime;
            NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
            System.out.println("#"+tag+"# {End counting :"+numberFormat.format(elapsedTime)+" ms }");
            listTimes.remove(tag);
        }else
            Log.e(tag,"NO KEY IN HASHMAP To calculate Time");

    }
}