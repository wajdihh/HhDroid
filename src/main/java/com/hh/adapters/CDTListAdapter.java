package com.hh.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnFocusChangeListener;
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.hh.clientdatatable.ClientDataTable;
import com.hh.clientdatatable.ClientDataTable.CDTStatus;
import com.hh.clientdatatable.TCell;
import com.hh.droid.R;
import com.hh.execption.WrongTypeException;
import com.hh.features.PfKeyboard;
import com.hh.listeners.OnNotifyDataSetChangedListener;
import com.hh.ui.UiUtility;
import com.hh.ui.widget.UiBooleanRadioGroup;
import com.hh.ui.widget.UiPicassoImageView;

import java.util.ArrayList;

public class CDTListAdapter extends BaseAdapter implements OnNotifyDataSetChangedListener {

    public static final int FLAG_DEFAULT = 0x00;
    public static final int FLAG_AUTO_REQUERY = 0x01;


    protected ClientDataTable mClientDataTable;
    protected Context mContext;
    protected Resources mRes;
    private int mLayoutRow;
    private LayoutInflater mInflater;
    private ViewHolder _mHolder;
    private View _mConvertView;
    private ArrayList<String> _mListOfTags;
    private boolean _mIsEnableAutoNotifyDataSetChanged;
    private boolean _mAutoRequery;
    private boolean _mIsEnableOnClickWidget;
    private boolean _mFirstBuild;
    private int mBase64OptionSize=2;
    public void setBase64OptionSize(int optionSize){
        mBase64OptionSize=optionSize;
    }

    public CDTListAdapter(Context pContext, int pLayoutRow, ClientDataTable pCDT) {

        mClientDataTable = pCDT;
        mContext = pContext;
        mRes = pContext.getResources();
        mLayoutRow = pLayoutRow;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mClientDataTable.setOnNotifyDataSetChangedListener(this);

        View convertView = mInflater.inflate(pLayoutRow, null);
        ViewHolder.clearAllTags();
        _mListOfTags = new ArrayList<String>(ViewHolder.getAllLayoutTags(convertView));
        _mIsEnableAutoNotifyDataSetChanged = false;
        _mAutoRequery = false;
        _mIsEnableOnClickWidget = true;
        _mFirstBuild = true;
    }

    public void setEnableOnClickWidget(boolean pIsEnabled) {
        _mIsEnableOnClickWidget = pIsEnabled;
    }

    public ClientDataTable getClientDataTable() {
        return mClientDataTable;
    }

    public void setClientDataTable(ClientDataTable mClientDataTable) {
        this.mClientDataTable = mClientDataTable;
        mClientDataTable.setOnNotifyDataSetChangedListener(this);
    }

    public void setEnableAutoNotifyDataSetChanged(boolean pIsEnabled, int pFLAG) {
        _mIsEnableAutoNotifyDataSetChanged = pIsEnabled;
        if (pFLAG == FLAG_AUTO_REQUERY)
            _mAutoRequery = true;
    }

    @Override
    protected void finalize() throws Throwable {
        mContext = null;
        super.finalize();
    }

    @Override
    public int getCount() {
        return mClientDataTable.getRowsCount();
    }

    @Override
    public Object getItem(int position) {
        if (mClientDataTable.moveToPosition(position))
            return mClientDataTable;
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void clear() {
        mClientDataTable.clear();
        notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View lWidget = null;
        int lNbrTags = 0;
        if (convertView == null) {

            if (_mFirstBuild) {
                if (parent instanceof ListView)
                    ((ListView) parent).setCacheColorHint(Color.TRANSPARENT);
                _mFirstBuild = false;
            }

            convertView = mInflater.inflate(mLayoutRow, parent, false);
            _mConvertView = convertView;
            _mHolder = new ViewHolder();

            lNbrTags = _mListOfTags.size();

            for (int i = 0; i < lNbrTags; i++) {

                lWidget = convertView.findViewWithTag(_mListOfTags.get(i));
                if (lWidget != null) {

                    int lColumnIndex = mClientDataTable.indexOfColumn(_mListOfTags.get(i));

                    if (lColumnIndex != -1)
                        _mHolder.mSparseArrayHolderViews.put(lColumnIndex, lWidget);
                    else
                        _mHolder.mSparseArrayHolderViewsNotInCDT.put(i, lWidget);

                    convertView.setBackgroundResource(R.drawable.selector_row_light);

                    if (_mIsEnableOnClickWidget) {

                        if (!(lWidget instanceof TextView))
                            lWidget.setOnClickListener(new onClickWidgetListener(convertView));

                        if (lWidget instanceof Button)
                            lWidget.setOnClickListener(new onClickWidgetListener(convertView));
                    }
                    convertView.setOnClickListener(new onClickRowListener(convertView));
                    convertView.setOnCreateContextMenuListener(new onCreateRowContextMenuListener(convertView));

                    if(lWidget instanceof UiBooleanRadioGroup)
                        ((UiBooleanRadioGroup) lWidget).setOnSelectedUiBooleanRGValue(new MyOnSelectedUiBooleanRGValue(convertView));
                    else if(lWidget instanceof RadioGroup)
                        ((RadioGroup) lWidget).setOnCheckedChangeListener(new MyOnCheckedChangeListener(convertView));
                    else if (lWidget instanceof CheckBox) {
                        CheckBox lCheckBox = (CheckBox) lWidget;
                        lCheckBox.setOnCheckedChangeListener(new onCheckedRowChangeListener(convertView, _mListOfTags.get(i)));
                    }
                    else if (lWidget instanceof TextView) {
                        TextView lTextView = (TextView) lWidget;
                        lTextView.setOnFocusChangeListener(new onFocusedRowChangeListener(convertView, _mListOfTags.get(i)));
                    }
                }
            }
            convertView.setTag(R.id.TAG_HOLDER, _mHolder);
        } else {
            _mHolder = (ViewHolder) convertView.getTag(R.id.TAG_HOLDER);
        }
        convertView.setTag(R.id.TAG_POSITION, position);
        if(!mClientDataTable.isEmpty())
            bindData(convertView, lWidget, position);

        return convertView;
    }

    private void bindData(View convertView, View lWidget, int position) {

        if (getItem(position) != null) {
            int lListHolderSize = _mHolder.mSparseArrayHolderViews.size();
            for (int i = 0; i < lListHolderSize; i++) {

                int lColumnIndex = _mHolder.mSparseArrayHolderViews.keyAt(i);
                lWidget = _mHolder.mSparseArrayHolderViews.get(lColumnIndex);

                if (lWidget != null) {

                    final TCell data = mClientDataTable.getCell(position, lColumnIndex);
                    if (lWidget instanceof Checkable) {

                        ((Checkable) lWidget).setChecked(data.asBoolean());
                    } else if (lWidget instanceof TextView) {

                        ((TextView) lWidget).setText(data.asString());
                    }else if (lWidget instanceof UiPicassoImageView) {
                        if(data.getValueType() == TCell.ValueType.BASE64){
                            try {
                                throw new WrongTypeException(mContext, R.string.exception_canotUserBase64);
                            } catch (WrongTypeException e) {
                                e.printStackTrace();
                            }
                        }else {
                            UiPicassoImageView picassoImageView = (UiPicassoImageView) lWidget;
                            picassoImageView.setData(data.asString());
                        }
                    } else if (lWidget instanceof ImageView) {
                        ImageView im= (ImageView) lWidget;
                        if (data.getValueType() == TCell.ValueType.INTEGER && !data.asString().isEmpty()) {
                            im.setImageResource(data.asInteger());
                        } else if (data.getValueType() == TCell.ValueType.BASE64) {
                            byte[] decodedString = Base64.decode(data.asString(), Base64.NO_WRAP);
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inSampleSize = mBase64OptionSize;
                            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            im.setImageBitmap(decodedByte);
                        } else {
                            if (!data.asString().equals(""))
                                setViewImage((ImageView) lWidget, data.asString());
                            else
                                im.setImageDrawable(null);
                        }
                    }
                    onIteratedRow(convertView, lWidget, position);
                }
            }
            int lListHolderSizeNotInCDT = _mHolder.mSparseArrayHolderViewsNotInCDT.size();
            for (int i = 0; i < lListHolderSizeNotInCDT; i++) {
                int lColumnIndex = _mHolder.mSparseArrayHolderViewsNotInCDT.keyAt(i);
                lWidget = _mHolder.mSparseArrayHolderViewsNotInCDT.get(lColumnIndex);

                if (lWidget != null) {
                    onIteratedRow(convertView, lWidget, position);
                }
            }
        }
    }

    public void setViewImage(ImageView v, String value) {
        try {
            v.setImageResource(Integer.parseInt(value));
        } catch (NumberFormatException nfe) {
            v.setImageURI(Uri.parse(value));
        }
    }

    public void validateEditTextChanges() {
        UiUtility.clearAllChildrensFocus((ViewGroup) _mConvertView);
        mClientDataTable.edit();
        PfKeyboard.hide(mContext, _mConvertView);
    }

    /**
     * override this method when we need to access to CDT content
     * for each row when iterate all the data
     *
     * @param row      :row (convertView)
     * @param widget   : Button , TextView etc...
     * @param position : position of row
     */
    protected void onIteratedRow(View row, View widget, int position) {
    }

    ;

    /**
     * override this method to capture the click on selected row
     *
     * @param row
     * @param position : position of selected row
     */
    protected void onClickRow(View row, int position) {
    }

    ;

    /**
     * override this method to capture the click on selected widget (Button, ImageView ...) inside a row
     *
     * @param tagWidget : the tag of the selected widget
     * @param position  : the position of row of selected widget
     */

    protected void onClickWidget(View row,String tagWidget, int position) {
    }


    /**
     * override this method to capture the Long click on selected Row,
     * and we can create context Menu inside this method
     *
     * @param row      : selected row
     * @param menu     : for create a context menu
     * @param position : position of selected row
     */
    protected void onLongClickRow(View row, Menu menu, int position) {
    }

    protected  void onClickUiBoolRGWidget(View parentView,View clickedView,String widgetTag,boolean isChecked, int position){};

    protected  void onCheckRadioButtonWidget(View parentView,View clickedView,String widgetTag,int radioButtonID, int position){};
    ;

    /**
     * Onclick Listener for each row
     *
     * @author WajdiHh
     */
    class onClickRowListener implements OnClickListener {

        private View _mRow;

        public onClickRowListener(View pRow) {
            _mRow = pRow;
        }

        @Override
        public void onClick(View v) {
            int lCurrentPos = (Integer) _mRow.getTag(R.id.TAG_POSITION);
            getItem(lCurrentPos);
            onClickRow(_mRow, lCurrentPos);
        }
    }

    /**
     * Onclick Listener for each widget on the row
     *
     * @author WajdiHh
     */
    class onClickWidgetListener implements OnClickListener {

        private View _mRow;
        public onClickWidgetListener(View pRow) {
            _mRow = pRow;
        }

        @Override
        public void onClick(View v) {
            int lCurrentPos = (Integer) _mRow.getTag(R.id.TAG_POSITION);
            getItem(lCurrentPos);
            String lTag = "";
            if (v.getTag() != null)
                lTag = v.getTag().toString();

            onClickWidget(_mRow,lTag, lCurrentPos);
        }
    }

    /**
     * onCheckedRowChangeListener to change status of Chekbox for each row
     *
     * @author WajdiHh
     */
    class onCheckedRowChangeListener implements OnCheckedChangeListener {
        private View _mRow;
        private String _mColumnName;

        public onCheckedRowChangeListener(View pRow, String pColumnName) {
            _mRow = pRow;
            _mColumnName = pColumnName;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int lCurrentPos = (Integer) _mRow.getTag(R.id.TAG_POSITION);
            getItem(lCurrentPos);

            if(!mClientDataTable.isEmpty()) {
                if( mClientDataTable.getCDTStatus()==CDTStatus.DEFAULT){
                    Log.i(this.getClass().getName(), "ClientDataTable is in default Mode, we can't change filed values");
                    return;
                }
                mClientDataTable.cellByName(_mColumnName).setValue(isChecked);
            }
        }
    }

    /**
     * onCheckedRowChangeListener to change status of Checkbox for each row
     *
     * @author WajdiHh
     */
    class onFocusedRowChangeListener implements OnFocusChangeListener {
        private View _mRow;
        private String _mColumnName;

        public onFocusedRowChangeListener(View pRow, String pColumnName) {
            _mRow = pRow;
            _mColumnName = pColumnName;
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            int lCurrentPos = (Integer) _mRow.getTag(R.id.TAG_POSITION);
            TextView lTextView = (TextView) v;
            getItem(lCurrentPos);

            if(!mClientDataTable.isEmpty()) {
                if( mClientDataTable.getCDTStatus()==CDTStatus.DEFAULT){
                    Log.i(this.getClass().getName(), "ClientDataTable is in default Mode, we can't change filed values");
                    return;
                }
                mClientDataTable.cellByName(_mColumnName).setValue(lTextView.getText().toString());
            }
        }
    }

    /**
     * OnClickListener in case of radioButton Boolean
     */
    class MyOnSelectedUiBooleanRGValue implements UiBooleanRadioGroup.OnSelectedUiBooleanRGValue {

        private View _mRow;
        public MyOnSelectedUiBooleanRGValue(View pRow) {
            _mRow = pRow;
        }

        @Override
        public void onSelectedValue(View view,String tag,boolean isChecked) {

            int lCurrentPos = (Integer) _mRow.getTag(R.id.TAG_POSITION);
            getItem(lCurrentPos);
            onClickUiBoolRGWidget(_mRow,view,tag, isChecked,lCurrentPos);

            if(!mClientDataTable.isEmpty()) {
                if( mClientDataTable.getCDTStatus()==CDTStatus.DEFAULT){
                    Log.i(this.getClass().getName(), "ClientDataTable is in default Mode, we can't change filed values");
                    return;
                }
                mClientDataTable.cellByName(tag).setValue(isChecked);
            }
        }
    }

    /**
     * OnClickListener in case of radioButton Boolean
     */
    class MyOnCheckedChangeListener implements RadioGroup.OnCheckedChangeListener {

        private View _mRow;
        public MyOnCheckedChangeListener(View pRow) {
            _mRow = pRow;
        }

        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i) {

            int lCurrentPos = (Integer) _mRow.getTag(R.id.TAG_POSITION);
            getItem(lCurrentPos);
            String lTag = "";
            if (radioGroup.getTag() != null)
                lTag = radioGroup.getTag().toString();

            onCheckRadioButtonWidget(_mRow,radioGroup,lTag,i,lCurrentPos);
        }
    }
    /**
     * Create a contextMenu for eachRow or get the longClick event
     *
     * @author WajdiHh
     */
    class onCreateRowContextMenuListener implements OnCreateContextMenuListener {
        private View _mRow;

        public onCreateRowContextMenuListener(View pRow) {
            _mRow = pRow;
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
            int lCurrentPos = (Integer) _mRow.getTag(R.id.TAG_POSITION);
            getItem(lCurrentPos);
            onLongClickRow(_mRow, menu, lCurrentPos);
        }
    }

    public void onCancel(){}
    @Override
    public void notifyValueChanged() {

        if (_mAutoRequery)
            mClientDataTable.requery();

        if (_mIsEnableAutoNotifyDataSetChanged)
            notifyDataSetChanged();

    }
}