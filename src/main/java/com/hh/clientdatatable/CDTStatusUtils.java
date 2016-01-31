package com.hh.clientdatatable;

import com.hh.listeners.OnCDTStatusObserver;

import java.util.ArrayList;

/**
 * Created by WajdiHh on 23/12/2015.
 * Email : wajdihh@gmail.com
 */
public class CDTStatusUtils {


    public ArrayList<OnCDTStatusObserver> mListOfStateListener;


    public CDTStatusUtils() {
        mListOfStateListener=new ArrayList<>();
    }

    public void notifyOnBeforeDelete(){
        for (OnCDTStatusObserver listener:mListOfStateListener)
            listener.onBeforeDelete();
    }

    public void notifyOnBeforeEdit(){
        for (OnCDTStatusObserver listener:mListOfStateListener)
            listener.onBeforeEdit();
    }

    public void notifyOnBeforeInsert(){
        for (OnCDTStatusObserver listener:mListOfStateListener)
            listener.onBeforeInsert();
    }

    public void notifyOnBeforeRevert(){
        for (OnCDTStatusObserver listener:mListOfStateListener)
            listener.onBeforeRevert();
    }

    public void notifyOnBeforeValidate(){
        //MUST be implemeted in CODE CDT
    }

    public void notifyOnAfterDelete(TRow deletedRow,boolean isExecuteMode){
        for (OnCDTStatusObserver listener:mListOfStateListener)
            listener.onAfterDelete(deletedRow,isExecuteMode);
    }

    public void notifyOnAfterEdit(TRow oldRow,TRow newRow,boolean isExecuteMode){
        for (OnCDTStatusObserver listener:mListOfStateListener)
            listener.onAfterEdit(oldRow,newRow,isExecuteMode);
    }

    public void notifyOnAfterInsert(boolean isExecuteMode){
        for (OnCDTStatusObserver listener:mListOfStateListener)
            listener.onAfterInsert(isExecuteMode);
    }

    public void notifyOnAfterRevert(){
        for (OnCDTStatusObserver listener:mListOfStateListener)
            listener.onAfterRevert();
    }

    public void notifyOnAfterValidate(boolean isExecuteMode){
        for (OnCDTStatusObserver listener:mListOfStateListener)
            listener.onAfterValidate(isExecuteMode);
    }
}
