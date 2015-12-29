package com.hh.clientdatatable;

import com.hh.clientdatatable.TCell.ValueType;
import com.hh.listeners.OnCDTColumnListener;

public class TColumn {



    public enum ColumnType {PrimaryKey,ToIgnoreInDB,JsonParent,JsonField};


    private String _mName;
    private ValueType _mType;
    private TCell.CellType _mCellType;
    private OnCDTColumnListener _mListener;
    private ColumnType _mColumnType;
    private String _mJsonParent;
    private boolean mIsIgnoreAsJsonField;

    /**
     * @param _mName : name of column
     * @param pType  : Type of column : boolean TEXT etc...
     */
    public TColumn(String pName, ValueType pType) {

        _mName = pName;
        _mType = pType;
    }

    public TColumn(String pName, ValueType pType,ColumnType pColumnType) {

        _mName = pName;
        _mType = pType;
        _mColumnType=pColumnType;
    }

    public TColumn(String pName, ValueType pType, TCell.CellType pCellType, OnCDTColumnListener pListener) {

        _mName = pName;
        _mType = pType;
        _mListener = pListener;
        _mCellType = pCellType;
    }
    public TColumn(String pName, ValueType pType,ColumnType pColumnType, OnCDTColumnListener pListener) {

        _mName = pName;
        _mType = pType;
        _mListener = pListener;
        _mColumnType=pColumnType;
    }

    public OnCDTColumnListener getCDTColumnListener() {
        return _mListener;
    }

    public String getName() {
        return _mName;
    }

    public ColumnType getColumnType() {
        return _mColumnType;
    }

    public void setName(String _mName) {
        this._mName = _mName;
    }

    public ValueType getValueType() {
        return _mType;
    }

    public void setValueType(ValueType _mType) {
        this._mType = _mType;
    }

    public TCell.CellType getCellType() {
        return _mCellType;
    }

    public void setCellType(TCell.CellType _mType) {
        this._mCellType = _mType;
    }

    public String getJsonParent() {
        return _mJsonParent;
    }

    public void setJsonParent(String pRoot) {
        this._mJsonParent = pRoot;
    }
    public void setJsonParentAsMain() {
        this._mJsonParent = "MAIN";
    }

    public boolean isIgnoredAsJsonField() {
        return mIsIgnoreAsJsonField;
    }

    public void toIgnoreInJSONParent() {
        this.mIsIgnoreAsJsonField = true;
    }
}
