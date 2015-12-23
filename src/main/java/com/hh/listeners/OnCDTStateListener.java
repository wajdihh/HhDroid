package com.hh.listeners;

import com.hh.clientdatatable.TRow;

/**
 * Created by PWBA06861 on 26/08/2015.
 */
public class OnCDTStateListener {

    public void onBeforeDelete() {
    }
    public void onAfterDelete(TRow deletedRow,boolean isInExecuteMode) {
    }

    public void onBeforeEdit() {
    }
    public void onAfterEdit(TRow oldRow,TRow newRow,boolean isInExecuteMode) {
    }

    public void onBeforeInsert() {
    }
    public void onAfterInsert(boolean isInExecuteMode) {
    }

    public void onBeforeRevert(){
    }

    public void onAfterRevert(){
    }

    public  boolean onBeforeValidate(){
        return true;
    }
    public  void onAfterValidate(boolean isInExecuteMode){}

}
