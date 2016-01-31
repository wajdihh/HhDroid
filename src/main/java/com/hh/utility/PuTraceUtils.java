package com.hh.utility;

import android.util.Log;

import java.util.LinkedList;

public final class PuTraceUtils {

    private static long startTime;


    public static void startCounter(String tag){
        startTime = System.currentTimeMillis();
        System.out.println("#"+tag+"# {I'am starting }");
    }

    public static void stopCounter(String tag){
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println("#"+tag+"# {Time is } :"+elapsedTime);
    }
}