package com.hh.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.hh.clientdatatable.ClientDataTable;
import com.hh.droid.R;
import com.hh.listeners.*;

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
	private OnRecycleCheckedRadioButtonGroupChangeListener mOnRecycleCheckedRadioButtonGroupChangeListener;
	private OnRecycleTextWatcher _mOnRecycleTextWatcher;

	SparseArray<View> mSparseArrayHolderViews;
	SparseArray<View> mSparseArrayHolderViewsNotInCDT;
	View mRowView;

	public RecycleViewHolder(Context pContext, final View itemView, ClientDataTable pClientDataTable, final boolean pIsEnableOnClickWidget) {
		super(itemView);

		mSparseArrayHolderViews=new SparseArray<View>();
		mSparseArrayHolderViewsNotInCDT=new SparseArray<View>();


		mListOfTags=getAllLayoutTags(itemView);
		mRowView=itemView;
		int index=0;
		for (final String tag:mListOfTags){

			final View lWidget = itemView.findViewWithTag(tag);
			if (lWidget != null) {

				lWidget.setTag(R.id.TAG_IS_WIDGET_IN_CDT,false);
				onCreateRowInViewHolder(mRowView,lWidget);
				int lColumnIndex = pClientDataTable.indexOfColumn(tag);

				if (lColumnIndex != -1) {
					mSparseArrayHolderViews.put(lColumnIndex, lWidget);
					lWidget.setTag(R.id.TAG_IS_WIDGET_IN_CDT,true);
				}else
					mSparseArrayHolderViewsNotInCDT.put(index, lWidget);

				if(lWidget instanceof RadioGroup)
					((RadioGroup) lWidget).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(RadioGroup radioGroup, int i) {
							String tag="";
							if(lWidget.getTag()!=null)
								tag=lWidget.getTag().toString();
							if(mOnRecycleCheckedRadioButtonGroupChangeListener!=null) mOnRecycleCheckedRadioButtonGroupChangeListener.onCheckedChanged(mRowView,radioGroup,tag, i, getAdapterPosition());
						}
					});

				else if (lWidget instanceof CheckBox) {
					CheckBox lCheckBox = (CheckBox) lWidget;
					lCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
							if(_mOnRecycleCheckedChangeListener!=null) _mOnRecycleCheckedChangeListener.onCheckedChanged(
									(Boolean) lWidget.getTag(R.id.TAG_IS_WIDGET_IN_CDT),compoundButton, b, getAdapterPosition());
						}
					});
				}
				else if (lWidget instanceof EditText) {
					final EditText lEditText = (EditText) lWidget;
					lEditText.addTextChangedListener(new TextWatcher() {
						@Override
						public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

						}

						@Override
						public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

						}

						@Override
						public void afterTextChanged(Editable editable) {
							if (_mOnRecycleTextWatcher != null)
								_mOnRecycleTextWatcher.afterTextChanged((Boolean) lWidget.getTag(R.id.TAG_IS_WIDGET_IN_CDT)
										,lEditText, editable.toString(), getAdapterPosition());
						}
					});
				}


				if(pIsEnableOnClickWidget)
					lWidget.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							if(_mOnRecycleWidgetClickListener==null)
								return;

							if (view instanceof Button)
								_mOnRecycleWidgetClickListener.onClick(itemView, view, tag, getAdapterPosition());
							else if (view instanceof EditText)
								_mOnRecycleWidgetClickListener.onClick(itemView, view, tag, getAdapterPosition());
							else{
								if (!(view instanceof TextView))
									_mOnRecycleWidgetClickListener.onClick(itemView,view, tag,getAdapterPosition());
							}
						}
					});

			}
			index++;
		}

		if(!pIsEnableOnClickWidget){
			itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if(_mClickListener!=null) _mClickListener.onClick(view, getAdapterPosition());
				}
			});
			itemView.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {
					if(_mClickListener!=null) _mClickListener.onLongClick(view, getAdapterPosition());
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
	public void setOnRecycleTextWatcher(OnRecycleTextWatcher pOnRecycleTextWatcher) {
		_mOnRecycleTextWatcher = pOnRecycleTextWatcher;
	}
	public void setOnRecycleCheckedChangeListener(OnRecycleCheckedChangeListener pOnRecycleCheckedChangeListener) {
		_mOnRecycleCheckedChangeListener = pOnRecycleCheckedChangeListener;
	}
	public void setOnRecycleCheckedRadioButtonGroupChangeListener(OnRecycleCheckedRadioButtonGroupChangeListener pOnRecycleCheckedRadioButtonGroupChangeListener) {
		mOnRecycleCheckedRadioButtonGroupChangeListener = pOnRecycleCheckedRadioButtonGroupChangeListener;
	}

	protected  void onCreateRowInViewHolder(View row,View widget){

	}
}
