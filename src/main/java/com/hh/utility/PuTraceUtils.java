package com.hh.utility;

import android.util.Log;

import java.util.LinkedList;

public final class PuTraceUtils {

    private static long startTime;


    public static void startCounter(){
        startTime = System.currentTimeMillis();
        System.out.println("## {I'am starting }");
    }

    public static void stopCounter(){
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println("## {Time is } :"+elapsedTime);
    }
}