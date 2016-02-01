package com.hh.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import com.hh.clientdatatable.ClientDataTable;
import com.hh.listeners.OnRecycleCheckedChangeListener;
import com.hh.listeners.OnRecycleClickListener;
import com.hh.listeners.OnRecycleFocusedChangeListener;
import com.hh.listeners.OnRecycleWidgetClickListener;

import java.util.HashSet;

public  class RecycleViewHolder extends RecyclerView.ViewHolder {

	 HashSet<String> mListOfTags;
	//init block
	 {
		mListOfTags=new HashSet<String>();
	}

	private OnRecycleClickListener _mClickListener;
	private OnRecycleWidgetClickListener _mOnRecycleWidgetClickListener;
	private OnRecycleCheckedChangeListener _mOnRecycleCheckedChangeListener;
	private OnRecycleFocusedChangeListener _mOnRecycleFocusedChangeListener;

	SparseArray<View> mSparseArrayHolderViews;
	SparseArray<View> mSparseArrayHolderViewsNotInCDT;
	View mRowView;

	public RecycleViewHolder(Context pContext, final View itemView,ClientDataTable pClientDataTable,final boolean pIsEnableOnClickWidget) {
		super(itemView);

		mSparseArrayHolderViews=new SparseArray<View>();
		mSparseArrayHolderViewsNotInCDT=new SparseArray<View>();


		mListOfTags=getAllLayoutTags(itemView);
		mRowView=itemView;
		int index=0;
		for (final String tag:mListOfTags){

			View lWidget = itemView.findViewWithTag(tag);
			if (lWidget != null) {

				int lColumnIndex = pClientDataTable.indexOfColumn(tag);

				if (lColumnIndex != -1)
					mSparseArrayHolderViews.put(lColumnIndex, lWidget);
				else
					mSparseArrayHolderViewsNotInCDT.put(index, lWidget);


				if (lWidget instanceof CheckBox) {
					CheckBox lCheckBox = (CheckBox) lWidget;
					lCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
							if(_mOnRecycleCheckedChangeListener!=null) _mOnRecycleCheckedChangeListener.onCheckedChanged(compoundButton, b, getPosition());
						}
					});
				}
				if (lWidget instanceof TextView) {
					TextView lTextView = (TextView) lWidget;
					lTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
						@Override
						public void onFocusChange(View view, boolean b) {
							if(_mOnRecycleFocusedChangeListener!=null) _mOnRecycleFocusedChangeListener.onFocusChange(view,b,getPosition());
						}
					});
				}


				if(pIsEnableOnClickWidget)
					lWidget.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							if (!(view instanceof TextView))
								if(_mOnRecycleWidgetClickListener!=null) _mOnRecycleWidgetClickListener.onClick(itemView,view, tag,getPosition());

							if (view instanceof Button)
								if(_mOnRecycleWidgetClickListener!=null) _mOnRecycleWidgetClickListener.onClick(itemView,view, tag,getPosition());
						}
					});

			}
			index++;
		}

		if(!pIsEnableOnClickWidget){
			itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if(_mClickListener!=null) _mClickListener.onClick(view, getPosition());
				}
			});
			itemView.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {
					if(_mClickListener!=null) _mClickListener.onLongClick(view, getPosition());
					return true;
				}
			});
		}
	}



	private HashSet<String> getAllLayoutTags(View v){

		ViewGroup vg = (ViewGroup) v;
		int lSize=vg.getChildCount();
		for (int i = 0; i < lSize; ++i) {
			View child = vg.getChildAt(i);
			if(child.getTag()!=null)
				mListOfTags.add(child.getTag().toString());

			if(child instanceof ViewGroup)
				mListOfTags.addAll(getAllLayoutTags(child));
		}
		return mListOfTags;
	}


	/* Setter for listener. */
	public void setClickListener(OnRecycleClickListener clickListener) {
		this._mClickListener = clickListener;
	}
	public void setOnRecycleWidgetClickListener(OnRecycleWidgetClickListener clickListener) {
		this._mOnRecycleWidgetClickListener = clickListener;
	}
	public void setOnRecycleFocusedChangeListener(OnRecycleFocusedChangeListener pOnRecycleFocusedChangeListener) {
		_mOnRecycleFocusedChangeListener = pOnRecycleFocusedChangeListener;
	}
	public void setOnRecycleCheckedChangeListener(OnRecycleCheckedChangeListener pOnRecycleCheckedChangeListener) {
		_mOnRecycleCheckedChangeListener = pOnRecycleCheckedChangeListener;
	}
}
