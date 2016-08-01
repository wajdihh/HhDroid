

package com.hh.clientdatatable;

import android.content.Context;
import android.util.Log;
import com.hh.clientdatatable.ClientDataTable.CDTStatus;
import com.hh.droid.HhDroid;
import com.hh.listeners.OnCDTColumnObserver;
import com.hh.utility.PuDate;
import com.hh.utility.PuUtils;

import java.text.DecimalFormat;
import java.text.ParseException;
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
    private OnCDTColumnObserver _mOnCDTColumnObserver;
    private Context _mContext;
    private boolean _mIsValueChanged;


    public TCell(Context pContext, Object pValue, ValueType pValueType,CellType pCellType,String pCellName, CDTStatus pCDTStatus, OnCDTColumnObserver pOnCDTColumnObserver) {

        initTcell(pContext,pValueType,pCDTStatus,pCellName);
        _mCellType=pCellType;
        _mOnCDTColumnObserver = pOnCDTColumnObserver;

        if (pValueType == ValueType.DATETIME) {
            try {
                _mValue = String.valueOf(PuDate.parseDate((String) pValue));
            } catch (ParseException e) {
                _mValue=(String) pValue;
            }
        } else
            _mValue =(pValue==null)?"":String.valueOf(pValue);
    }

    public TCell(Context pContext, ValueType pValueType, CDTStatus pCDTStatus,CellType pCellType,String pCellName, OnCDTColumnObserver pOnCDTColumnObserver) {
        initTcell(pContext,pValueType,pCDTStatus,pCellName);
        _mCellType=pCellType;
        _mOnCDTColumnObserver = pOnCDTColumnObserver;
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
            PuUtils.showMessage(_mContext, "Erreur d'insertion", "Le mode insertion est d�sactiv�e" +
                    " il faut le reactiver en utilisant append() ou insert()");
    }

    public void updateFromDB(Object pValue) {

        if (_mCDTStatus == CDTStatus.UPDATE) {

            if((_mValue!=null && pValue!=null) &&!_mValue.equals(pValue.toString()))
                onValueChanged();

            _mValue = String.valueOf(pValue);

        } else
            PuUtils.showMessage(_mContext, "Erreur de modification", "Le mode update est d�sactiv�e" +
                    " il faut le reactiver en utilisant edit()");
    }

    public void setName(String name){
        _mName=name;
    }

    public void setValue(String pValue) {
        if (_mOnCDTColumnObserver != null)
            pValue = _mOnCDTColumnObserver.onSetValue(pValue);

        if(_mValue==null || ((_mValue!=null && pValue!=null) &&!_mValue.equals(pValue.toString())))
            onValueChanged();

        _mValue = pValue;
    }

    public void setValue(boolean pValue) {
        if (_mOnCDTColumnObserver != null)
            pValue = _mOnCDTColumnObserver.onSetValue(pValue);

        if(_mValue==null || !_mValue.equals(pValue))
            onValueChanged();

        _mValue = String.valueOf(pValue);

    }

    public void setValue(int pValue) {
        if (_mOnCDTColumnObserver != null)
            pValue = _mOnCDTColumnObserver.onSetValue(pValue);

        if(_mValue==null || !_mValue.equals(pValue))
            onValueChanged();
        _mValue = String.valueOf(pValue);
    }

    public void setValue(double pValue) {
        if (_mOnCDTColumnObserver != null)
            pValue = _mOnCDTColumnObserver.onSetValue(pValue);

        if(_mValue==null || !_mValue.equals(pValue))
            onValueChanged();

        _mValue = String.valueOf(pValue);
    }

    public void setValue(long pValueTimeInMillies) {
        if (_mOnCDTColumnObserver != null)
            pValueTimeInMillies = _mOnCDTColumnObserver.onSetValue(pValueTimeInMillies);

        if(_mValue==null || !_mValue.equals(pValueTimeInMillies))
            onValueChanged();
        _mValue = String.valueOf(pValueTimeInMillies);
    }

    public boolean asBoolean() {

        boolean lResult = false;
        if(_mValue==null)
            return lResult;
        try {
            if (_mValue != null)
                lResult = Boolean.parseBoolean(_mValue);

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (_mOnCDTColumnObserver != null)
            lResult= _mOnCDTColumnObserver.onGetValue(lResult);
        return lResult;
    }

    public String asString() {

        String lResult = "";
        if(_mValue==null)
            return lResult;

        if (_mOnCDTColumnObserver != null){

            if(!_mValue.isEmpty()) {
                switch (_mValueType) {
                    case INTEGER:
                        lResult = _mOnCDTColumnObserver.onGetValueInt(Integer.parseInt(_mValue));
                        break;
                    case BOOLEAN:
                        lResult = _mOnCDTColumnObserver.onGetValueBool(Boolean.parseBoolean(_mValue));
                        break;
                    case DOUBLE:
                        if(_mValue.contains(","))
                            _mValue=_mValue.replace(",",".");
                        lResult = _mOnCDTColumnObserver.onGetValueDouble(Double.parseDouble(_mValue));
                        DecimalFormat format = new DecimalFormat();
                        format.setDecimalSeparatorAlwaysShown(false);
                        if(_mValue.contains(","))
                            _mValue=_mValue.replace(",",".");
                        lResult=format.format(Double.parseDouble(lResult));
                        break;
                    case DATETIME:
                        lResult = _mOnCDTColumnObserver.onGetValueDate(Long.parseLong(_mValue));
                        break;
                    case TEXT:
                        lResult = _mOnCDTColumnObserver.onGetValue(_mValue);
                        break;
                }
            }else
                lResult = _mOnCDTColumnObserver.onGetValue(_mValue);

            if(_mValueType!=ValueType.TEXT)
                lResult = _mOnCDTColumnObserver.onGetValue(lResult);

            // If we not define the listener
        }else{
            if(!_mValue.isEmpty()){
                if(_mValueType==ValueType.DATETIME){
                    long dateLong=Long.parseLong(_mValue);
                    //3600000 its 01/01/1970 equivalent to NULL
                    if(dateLong==-3600000)
                        lResult="";
                    else
                        lResult = PuDate.getStringFromDate(dateLong);
                }else if(_mValueType==ValueType.DOUBLE){
                    DecimalFormat format = new DecimalFormat();
                    format.setDecimalSeparatorAlwaysShown(false);
                    if(_mValue.contains(","))
                        _mValue=_mValue.replace(",",".");
                    lResult=format.format(Double.parseDouble(_mValue));
                }else
                    lResult=_mValue;
            }else
                lResult=_mValue;
        }

        if(_mCellType==CellType.CURRENCY)
            return lResult+" "+ HhDroid.getInstance(_mContext).mCurrencySymbol;

        return lResult;
    }

    /**
     * Renvoi la valeur par d�faut
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
        if(_mValue==null)
            return lResult;
        try {
            if (_mValue != null && !_mValue.isEmpty())
                lResult = Integer.parseInt(_mValue);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("As ", "CANNOT Parse the CELL :" + _mName + " WITH VALUE :" + _mValue);
        }
        if (_mOnCDTColumnObserver != null)
            lResult= _mOnCDTColumnObserver.onGetValue(lResult);
        return lResult;
    }

    public float asFloat() {

        float lResult = -1;
        if(_mValue==null)
            return lResult;
        try {
            if (_mValue != null && !_mValue.isEmpty())
                lResult = Float.parseFloat(_mValue);

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (_mOnCDTColumnObserver != null)
            lResult= _mOnCDTColumnObserver.onGetValue(lResult);
        return lResult;
    }

    public double asDouble() {

        double lResult = -1;
        if(_mValue==null)
            return lResult;
        try {
            if (_mValue != null && !_mValue.isEmpty()){
                if(_mValue.contains(","))
                    _mValue=_mValue.replace(",",".");
                lResult = Double.parseDouble(_mValue);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        if (_mOnCDTColumnObserver != null)
            lResult= _mOnCDTColumnObserver.onGetValue(lResult);
        return lResult;
    }

    public Date asDate() {

        Date lResult = new Date();
        if(_mValue==null)
            return lResult;
        try {
            if (_mValue != null && !_mValue.isEmpty())
                lResult = new Date(Long.parseLong(_mValue));

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (_mOnCDTColumnObserver != null)
            lResult= _mOnCDTColumnObserver.onGetValue(lResult);
        return lResult;
    }

    public long asDateTime() {

        long lResult = -1;
        if(_mValue==null)
            return lResult;
        try {
            if (_mValue != null && !_mValue.isEmpty())
                lResult = Long.parseLong(_mValue);

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (_mOnCDTColumnObserver != null)
            lResult= _mOnCDTColumnObserver.onGetValue(lResult);
        return lResult;
    }

    public String asDateString() {

        String lResult = "01/01/1970";
        if(_mValue==null)
            return lResult;

        boolean mIsDateInLongFormat=false;
        try {
            /**
             * If date is in String format exp 12/12/2015 and it's valid (no exception)
             */
            PuDate.parseDate(_mValue);

            // return the date as it is
            lResult=_mValue;
        } catch (Exception e) {
            // if the date is not in String format so it sur than it is in long format like 1254725121212
            mIsDateInLongFormat=true;
        }

        if(mIsDateInLongFormat)
            lResult = PuDate.getStringFromDate(Long.parseLong(_mValue));

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

    public void setOnCDTColumnListener(OnCDTColumnObserver pCDTColumnListener) {

        _mOnCDTColumnObserver = pCDTColumnListener;
    }

    private void onValueChanged() {
        _mIsValueChanged=true;
        Log.i("onValueChanged","Cell Name ("+_mName+") : Value ("+_mValue+")");
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
