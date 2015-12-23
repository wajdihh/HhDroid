package com.hh.clientdatatable;

import com.hh.listeners.OnCDTStateListener;

import java.util.ArrayList;

/**
 * Created by WajdiHh on 23/12/2015.
 * Email : wajdihh@gmail.com
 */
public class CDTStatusUtils {


    public ArrayList<OnCDTStateListener> mListOfStateListener;


    public CDTStatusUtils() {
        mListOfStateListener=new ArrayList<>();
    }

    public void notifyOnBeforeDelete(){
        for (OnCDTStateListener listener:mListOfStateListener)
            listener.onBeforeDelete();
    }

    public void notifyOnBeforeEdit(){
        for (OnCDTStateListener listener:mListOfStateListener)
            listener.onBeforeEdit();
    }

    public void notifyOnBeforeInsert(){
        for (OnCDTStateListener listener:mListOfStateListener)
            listener.onBeforeInsert();
    }

    public void notifyOnBeforeRevert(){
        for (OnCDTStateListener listener:mListOfStateListener)
            listener.onBeforeRevert();
    }

    public void notifyOnBeforeValidate(){
        //MUST be implemeted in CODE CDT
    }

    public void notifyOnAfterDelete(TRow deletedRow,boolean isExecuteMode){
        for (OnCDTStateListener listener:mListOfStateListener)
            listener.onAfterDelete(deletedRow,isExecuteMode);
    }

    public void notifyOnAfterEdit(TRow oldRow,TRow newRow,boolean isExecuteMode){
        for (OnCDTStateListener listener:mListOfStateListener)
            listener.onAfterEdit(oldRow,newRow,isExecuteMode);
    }

    public void notifyOnAfterInsert(boolean isExecuteMode){
        for (OnCDTStateListener listener:mListOfStateListener)
            listener.onAfterInsert(isExecuteMode);
    }

    public void notifyOnAfterRevert(){
        for (OnCDTStateListener listener:mListOfStateListener)
            listener.onAfterRevert();
    }

    public void notifyOnAfterValidate(boolean isExecuteMode){
        for (OnCDTStateListener listener:mListOfStateListener)
            listener.onAfterValidate(isExecuteMode);
    }
}
