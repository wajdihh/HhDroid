package com.hh.listeners;

import java.util.Date;

/**
 * Created by Wajdi Hh on 13/01/2015.
 */
public abstract class OnCDTColumnObserver {


    public String onGetValue(String pValue){return pValue;}
    public String onGetValueInt(int pValue){return ""+pValue;}
    public String onGetValueDouble(double pValue){return ""+pValue;}
    public String onGetValueBool(boolean pValue){return ""+pValue;}
    public String onGetValueDate(long pValue){return ""+pValue;}

    public Date onGetValue(Date pValue){return pValue;}
    public boolean onGetValue(boolean pValue){return pValue;}
    public int onGetValue(int pValue){return pValue;}
    public long onGetValue(long pValue){return pValue;}
    public double onGetValue(double pValue){return pValue;}
    public float onGetValue(float pValue){return pValue;}

    public String onSetValue(String pValue){return   pValue;}
    public boolean onSetValue(boolean pValue){return  pValue;}
    public int onSetValue(int pValue){return pValue;}
    public long onSetValue(long pValue){return pValue;}
    public double onSetValue(double pValue){return pValue;}
    public float onSetValue(float pValue){return pValue;}
}
