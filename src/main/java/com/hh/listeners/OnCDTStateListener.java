package com.hh.listeners;

import com.hh.clientdatatable.TRow;

/**
 * Created by PWBA06861 on 26/08/2015.
 */
public class OnCDTStateListener {

    public void onBeforeDelete() {
    }
    public void onAfterDelete(TRow deletedRow) {
    }

    public void onBeforeEdit() {
    }
    public void onAfterEdit(TRow oldRow,TRow newRow) {
    }

    public void onBeforeInsert() {
    }
    public void onAfterInsert() {
    }

    public void onBeforeRevert(){
    }

    public void onAfterRevert(){
    }

    public  boolean onBeforeValidate(){
        return true;
    }
    public  void onAfterValidate(){}

}
