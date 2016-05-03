package com.hh.clientdatatable;

/**
 * Created by WajdiHh on 22/04/2016.
 * Email : wajdihh@gmail.com
 */
public class CDTNestedJSONArray {

    private String parentKey;
    private String key;
    private ClientDataTable clientDataTable;


    public CDTNestedJSONArray(String parentKey, String key, ClientDataTable clientDataTable) {
        this.parentKey = parentKey;
        this.key = key;
        this.clientDataTable = clientDataTable;
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
}
