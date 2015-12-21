

package com.hh.clientdatatable;

import android.content.Context;

import com.hh.clientdatatable.ClientDataTable.CDTStatus;
import com.hh.droid.HhDroid;
import com.hh.listeners.OnCDTColumnListener;
import com.hh.utility.PuDate;
import com.hh.utility.PuUtils;

import java.util.Date;


public class TCell implements Cloneable {

    public String getName() {
        return _mName;
    }

    public enum ValueType {BOOLEAN, INTEGER, DOUBLE, TEXT, DATETIME,BASE64}

    public enum CellType {NONE,CURRENCY}

    private String _mValue="";
    private String _mOldValue;
    private String _mName;
    private ValueType _mValueType;
    private CellType _mCellType;
    private CDTStatus _mCDTStatus;
    private OnCDTColumnListener _mOnCDTColumnListener;
    private Context _mContext;
    private boolean _mIsValueChanged;


    public TCell(Context pContext, Object pValue, ValueType pValueType,CellType pCellType,String pCellName, CDTStatus pCDTStatus, OnCDTColumnListener pOnCDTColumnListener) {

        initTcell(pContext,pValueType,pCDTStatus,pCellName);
        _mCellType=pCellType;
        _mOnCDTColumnListener = pOnCDTColumnListener;

        if (pValueType == ValueType.DATETIME) {
            long lLongDate = PuDate.getTimeFromStringDate((String) pValue);
            if(lLongDate==-1)
                _mValue=(String) pValue;
            else
                _mValue = String.valueOf(lLongDate);
        } else
            _mValue = String.valueOf(pValue);
    }

    public TCell(Context pContext, ValueType pValueType, CDTStatus pCDTStatus,CellType pCellType,String pCellName, OnCDTColumnListener pOnCDTColumnListener) {
        initTcell(pContext,pValueType,pCDTStatus,pCellName);
        _mCellType=pCellType;
        _mOnCDTColumnListener = pOnCDTColumnListener;
    }

    public TCell(Context pContext, ValueType pValueType, CDTStatus pCDTStatus,String pCellName) {
        initTcell(pContext,pValueType,pCDTStatus,pCellName);
    }

    public TCell() {
    }

    private void initTcell(Context pContext, ValueType pValueType, CDTStatus pCDTStatus,String pCellName) {
        _mCDTStatus = pCDTStatus;
        _mContext = pContext;
        _mName=pCellName;
        _mValueType=pValueType;
    }

    public void insertIntoDB(Object pValue) {

        if (_mCDTStatus == CDTStatus.INSERT) {

            if((_mValue!=null && pValue!=null) &&!_mValue.equals(pValue.toString()))
                onValueChanged();

            _mValue = String.valueOf(pValue);
        } else
            PuUtils.showMessage(_mContext, "Erreur d'insertion", "Le mode insertion est désactivée" +
                    " il faut le reactiver en utilisant append() ou insert()");
    }

    public void updateFromDB(Object pValue) {

        if (_mCDTStatus == CDTStatus.UPDATE) {

            if((_mValue!=null && pValue!=null) &&!_mValue.equals(pValue.toString()))
                onValueChanged();

            _mValue = String.valueOf(pValue);

        } else
            PuUtils.showMessage(_mContext, "Erreur de modification", "Le mode update est désactivée" +
                    " il faut le reactiver en utilisant edit()");
    }

    public void setValue(String pValue) {
        if (_mOnCDTColumnListener != null)
            pValue = _mOnCDTColumnListener.onSetValue(pValue);

        if(_mValue==null || ((_mValue!=null && pValue!=null) &&!_mValue.equals(pValue.toString())))
            onValueChanged();

        _mValue = pValue;
    }

    public void setValue(boolean pValue) {
        if (_mOnCDTColumnListener != null)
            pValue = _mOnCDTColumnListener.onSetValue(pValue);

        if(_mValue==null || !_mValue.equals(pValue))
            onValueChanged();

        _mValue = String.valueOf(pValue);

    }

    public void setValue(int pValue) {
        if (_mOnCDTColumnListener != null)
            pValue = _mOnCDTColumnListener.onSetValue(pValue);

        if(_mValue==null || !_mValue.equals(pValue))
            onValueChanged();
        _mValue = String.valueOf(pValue);
    }

    public void setValue(double pValue) {
        if (_mOnCDTColumnListener != null)
            pValue = _mOnCDTColumnListener.onSetValue(pValue);

        if(_mValue==null || !_mValue.equals(pValue))
            onValueChanged();

        _mValue = String.valueOf(pValue);
    }

    public void setValue(long pValueTimeInMillies) {
        if (_mOnCDTColumnListener != null)
            pValueTimeInMillies = _mOnCDTColumnListener.onSetValue(pValueTimeInMillies);

        if(_mValue==null || !_mValue.equals(pValueTimeInMillies))
            onValueChanged();
        _mValue = String.valueOf(pValueTimeInMillies);
    }

    public void setValueDateString(String pValueDate) {

        if(_mValue==null || (_mValue!=null && pValueDate!=null) &&!_mValue.equals(pValueDate))
            onValueChanged();
        _mValue = String.valueOf(PuDate.getTimeFromStringDate(pValueDate));
    }

    public boolean asBoolean() {

        boolean lResult = false;
        try {
            if (_mValue != null)
                lResult = Boolean.parseBoolean(_mValue);

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (_mOnCDTColumnListener != null)
            lResult= _mOnCDTColumnListener.onGetValue(lResult);
        return lResult;
    }

    public String asString() {

        String lResult = "";
        try {
            if (_mValue != null && !_mValue.isEmpty()){
                if(_mValueType==ValueType.DATETIME){
                    long dateLong=Long.parseLong(_mValue);
                    lResult = PuDate.getStringFromDate(dateLong);

                    if (_mOnCDTColumnListener != null)
                        lResult= _mOnCDTColumnListener.onGetValueDate(new Date(dateLong));

                } else
                    lResult = _mValue;
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        if (_mOnCDTColumnListener != null)
            lResult= _mOnCDTColumnListener.onGetValue(_mValue);

        if (_mOnCDTColumnListener != null && _mValueType==ValueType.INTEGER)
            lResult= _mOnCDTColumnListener.onGetValueInt(Integer.parseInt(_mValue));

        if (_mOnCDTColumnListener != null && _mValueType==ValueType.BOOLEAN)
            lResult= _mOnCDTColumnListener.onGetValueBool(Boolean.parseBoolean(_mValue));

        if (_mOnCDTColumnListener != null && _mValueType==ValueType.DOUBLE)
            lResult= _mOnCDTColumnListener.onGetValueDouble(Double.parseDouble(_mValue));

        if(_mCellType==CellType.CURRENCY)
            return lResult+" "+ HhDroid.getInstance(_mContext).mCurrencySymbol;


        return lResult;
    }

    /**
     * Renvoi la valeur par défaut
     * @return
     */
    public String asValue() {
        return _mValue;
    }

    public void memorizeOldValue(){
        _mOldValue=_mValue;
    }

    public void revertOldValue(){
        _mValue=_mOldValue;
    }
    public String getOldValue(){
        return _mOldValue;
    }
    public int asInteger() {

        int lResult = -1;

        try {
            if (_mValue != null && !_mValue.isEmpty())
                lResult = Integer.parseInt(_mValue);

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (_mOnCDTColumnListener != null)
            lResult= _mOnCDTColumnListener.onGetValue(lResult);
        return lResult;
    }

    public float asFloat() {

        float lResult = -1;

        try {
            if (_mValue != null)
                lResult = Float.parseFloat(_mValue);

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (_mOnCDTColumnListener != null)
            lResult= _mOnCDTColumnListener.onGetValue(lResult);
        return lResult;
    }

    public double asDouble() {

        double lResult = -1;

        try {
            if (_mValue != null)
                lResult = Double.parseDouble(_mValue);

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (_mOnCDTColumnListener != null)
            lResult= _mOnCDTColumnListener.onGetValue(lResult);
        return lResult;
    }

    public Date asDate() {

        Date lResult = null;

        try {
            if (_mValue != null && !_mValue.isEmpty())
                lResult = new Date(Long.parseLong(_mValue));

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (_mOnCDTColumnListener != null)
            lResult= _mOnCDTColumnListener.onGetValue(lResult);
        return lResult;
    }

    public long asDateTime() {

        long lResult = -1;

        try {
            if (_mValue != null && !_mValue.isEmpty())
                lResult = Long.parseLong(_mValue);

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (_mOnCDTColumnListener != null)
            lResult= _mOnCDTColumnListener.onGetValue(lResult);
        return lResult;
    }

    public String asDateString() {

        String lResult = "error";
        try {
            if (_mValue != null)
                lResult = PuDate.getStringFromDate(Long.parseLong(_mValue));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lResult;
    }

    public boolean isEmpty(){
        return  (_mValue==null || _mValue.equals(""));
    }
    public ValueType getValueType() {

        return _mValueType;
    }
    public void setValueType(ValueType pValueType) {

        _mValueType=pValueType;
    }
    public void setCDTStatus(CDTStatus pCDTStatus) {

        _mCDTStatus = pCDTStatus;
    }

    public void setOnCDTColumnListener(OnCDTColumnListener pCDTColumnListener) {

        _mOnCDTColumnListener = pCDTColumnListener;
    }

    private void onValueChanged() {
        _mIsValueChanged=true;
    }



    public void setValueChanged(boolean pIsValueChanged){
        _mIsValueChanged=pIsValueChanged;
    }
    public boolean isValueChanged(){
        return _mIsValueChanged;
    }
    @Override
    protected TCell clone() throws CloneNotSupportedException {
        TCell cell = null;
        try {
            cell = (TCell) super.clone();
        } catch(CloneNotSupportedException cnse) {
            cnse.printStackTrace(System.err);
        }
        return cell;
    }
}
