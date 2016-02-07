package com.hh.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.hh.clientdatatable.ClientDataTable;
import com.hh.clientdatatable.TCell;
import com.hh.droid.R;
import com.hh.execption.WrongTypeException;
import com.hh.listeners.OnRecycleCheckedChangeListener;
import com.hh.listeners.OnRecycleClickListener;
import com.hh.listeners.OnRecycleFocusedChangeListener;
import com.hh.listeners.OnRecycleWidgetClickListener;
import com.hh.ui.widget.UiPicassoImageView;

/**
 * Created by Wajdi Hh on 13/08/2015.
 * wajdihh@gmail.com
 */
public class CDTRecycleAdapter extends RecyclerView.Adapter<RecycleViewHolder> {

    protected Context mContext;
    protected Resources mRes;
    protected ClientDataTable mClientDataTable;
    private boolean _mIsEnableOnClickWidget;
    private int _mLayoutRes;
    private int mBase64OptionSize=2;
    public void setBase64OptionSize(int optionSize){
        mBase64OptionSize=optionSize;
    }

    public CDTRecycleAdapter(Context pContext, int pLayoutRes, ClientDataTable pCDT){
        mContext=pContext;
        mRes=pContext.getResources();
        mClientDataTable = pCDT;
        _mLayoutRes = pLayoutRes;

        setHasStableIds(true);


    }
    @Override
    protected void finalize() throws Throwable {
        mContext = null;
        super.finalize();
    }

    @Override
    public RecycleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(_mLayoutRes, null);
        return new RecycleViewHolder(mContext,v,mClientDataTable,_mIsEnableOnClickWidget);
    }


    public void setEnableOnClickWidget(boolean pIsEnabled) {
        _mIsEnableOnClickWidget = pIsEnabled;
    }

    @Override
    public void onBindViewHolder(RecycleViewHolder holder, int position) {

        mClientDataTable.moveToPosition(position);

        int lListHolderSize = holder.mSparseArrayHolderViews.size();

        for (int i = 0; i < lListHolderSize; i++) {

            int lColumnIndex = holder.mSparseArrayHolderViews.keyAt(i);
            View lWidget = holder.mSparseArrayHolderViews.get(lColumnIndex);

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
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length,options);
                        im.setImageBitmap(decodedByte);
                    } else {
                        if (!data.asString().equals(""))
                            setViewImage((ImageView) lWidget, data.asString());
                        else
                            im.setImageDrawable(null);
                    }
                } else
                    throw new IllegalStateException(lWidget.getClass().getName() + " is not a " +
                            " view that can be bounds by this SimpleAdapter");

                onIteratedRow(holder.mRowView,lWidget, lWidget.getTag().toString());
            }
        }
        int lListHolderSizeNotInCDT = holder.mSparseArrayHolderViewsNotInCDT.size();

        for (int i = 0; i < lListHolderSizeNotInCDT; i++) {
            int lColumnIndex = holder.mSparseArrayHolderViewsNotInCDT.keyAt(i);
            View lWidget = holder.mSparseArrayHolderViewsNotInCDT.get(lColumnIndex);

            if (lWidget != null) {
                onIteratedRow(holder.mRowView, lWidget, lWidget.getTag().toString());
            }
        }

        // ClickListener
        holder.setClickListener(new OnRecycleClickListener() {
            @Override
            public void onClick(View v, int position) {
                onClickRow(v,position);
            }

            @Override
            public void onLongClick(View v, int position) {
                onLongClickRow(v, position);
            }
        });

        holder.setOnRecycleWidgetClickListener(new OnRecycleWidgetClickListener() {

            @Override
            public void onClick(View parentView,View clickedView, String tag, int position) {
                onClickWidget(parentView,clickedView, tag, position);
            }
        });

        holder.setOnRecycleCheckedChangeListener(new OnRecycleCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked, int position) {

                String columnName=buttonView.getTag().toString();
                mClientDataTable.moveToPosition(position);
                mClientDataTable.cellByName(columnName).setValue(isChecked);
            }
        });

        holder.setOnRecycleFocusedChangeListener(new OnRecycleFocusedChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus, int position) {

                mClientDataTable.moveToPosition(position);

                TextView lTextView = (TextView) v;
                String columnName = v.getTag().toString();

                mClientDataTable.cellByName(columnName).setValue(lTextView.getText().toString());
            }
        });
    }

    public void setViewImage(ImageView v, String value) {

        try {
            v.setImageResource(Integer.parseInt(value));
        } catch (NumberFormatException nfe) {
            v.setImageURI(Uri.parse(value));
        }
    }
    @Override
    public int getItemCount() {
        return  mClientDataTable.getRowsCount();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    public void clear() {
        mClientDataTable.clear();
        notifyDataRecycleChanged();
    }

    public void notifyDataRecycleChanged(){
        mClientDataTable.requery();
        notifyDataSetChanged();
    }

    /**
     * override this method when we need to access to CDT content
     * for each row when iterate all the data
     *
     * @param widget   : Button , TextView etc...
     * @param position : position of row
     */
    protected void onIteratedRow(View row, View widget,String widgetTag) {
    }

    /**
     * override this method to capture the click on selected row
     *
     * @param row
     * @param position : position of selected row
     */
    protected void onClickRow(View row, int position) {
        mClientDataTable.moveToPosition(position);
    }

    ;

    /**
     * override this method to capture the click on selected widget (Button, ImageView ...) inside a row
     *
     * @param tagWidget : the tag of the selected widget
     * @param position  : the position of row of selected widget
     */

    protected void onClickWidget(View parentView,View clickedView,String tagWidget, int position) {
        mClientDataTable.moveToPosition(position);
    }


    /**
     * override this method to capture the Long click on selected Row,
     * and we can create context Menu inside this method
     *
     * @param row      : selected row
     * @param position : position of selected row
     */
    protected void onLongClickRow(View row, int position) {
        mClientDataTable.moveToPosition(position);
    }





}
