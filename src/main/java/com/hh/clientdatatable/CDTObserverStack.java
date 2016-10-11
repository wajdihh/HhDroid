package com.hh.clientdatatable;

import com.hh.listeners.OnCDTStatusObserver;

import java.util.ArrayList;

/**
 * Created by WajdiHh on 23/12/2015.
 * Email : wajdihh@gmail.com
 */
public class CDTObserverStack extends ArrayList<OnCDTStatusObserver> {


    public void notifyOnBeforeDelete(){
        for (OnCDTStatusObserver listener:this)
            listener.onBeforeDelete();
    }

    public void notifyOnBeforeEdit(){
        for (OnCDTStatusObserver listener:this)
            listener.onBeforeEdit();
    }

    public void notifyOnBeforeInsert(){
        for (OnCDTStatusObserver listener:this)
            listener.onBeforeInsert();
    }

    public void notifyOnBeforeRevert(){
        for (OnCDTStatusObserver listener:this)
            listener.onBeforeRevert();
    }

    public void notifyOnBeforeValidate(){
        //MUST be implemeted in CODE CDT
    }

    public void notifyOnAfterDelete(TRow deletedRow,boolean isExecuteMode){
        for (OnCDTStatusObserver listener:this)
            listener.onAfterDelete(deletedRow,isExecuteMode);
    }

    public void notifyOnAfterEdit(TRow oldRow,TRow newRow,boolean isExecuteMode){
        for (OnCDTStatusObserver listener:this)
            listener.onAfterEdit(oldRow, newRow, isExecuteMode);
    }

    public void notifyOnAfterInsert(boolean isExecuteMode){
        for (OnCDTStatusObserver listener:this)
            listener.onAfterInsert(isExecuteMode);
    }

    public void notifyOnAfterRevert(){
        for (OnCDTStatusObserver listener:this)
            listener.onAfterRevert();
    }

    public void notifyOnAfterValidate(boolean isExecuteMode){
        for (OnCDTStatusObserver listener:this)
            listener.onAfterValidate(isExecuteMode);
    }
}
