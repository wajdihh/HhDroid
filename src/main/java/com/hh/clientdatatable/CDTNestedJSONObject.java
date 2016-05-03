package com.hh.clientdatatable;

/**
 * Created by WajdiHh on 22/04/2016.
 * Email : wajdihh@gmail.com
 */
public class CDTNestedJSONObject {

    private String parentKey;
    private String key;
    private ClientDataTable clientDataTable;
    private ClientDataTable.JSONObjectGeneratedMode generatedMode;


    public CDTNestedJSONObject(String parentKey, String key, ClientDataTable clientDataTable, ClientDataTable.JSONObjectGeneratedMode generatedMode) {
        this.parentKey = parentKey;
        this.key = key;
        this.clientDataTable = clientDataTable;
        this.generatedMode = generatedMode;
    }

    public String getParentKey() {
        return parentKey;
    }

    public void setParentKey(String parentKey) {
        this.parentKey = parentKey;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public ClientDataTable getClientDataTable() {
        return clientDataTable;
    }

    public void setClientDataTable(ClientDataTable clientDataTable) {
        this.clientDataTable = clientDataTable;
    }

    public ClientDataTable.JSONObjectGeneratedMode getGeneratedMode() {
        return generatedMode;
    }

    public void setGeneratedMode(ClientDataTable.JSONObjectGeneratedMode generatedMode) {
        this.generatedMode = generatedMode;
    }
}
