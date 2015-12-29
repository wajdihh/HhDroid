package com.hh.adapters;

import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashSet;

public class ViewHolder {

		SparseArray<View> mSparseArrayHolderViews;
		SparseArray<View> mSparseArrayHolderViewsNotInCDT;
		ArrayList<View> mListHoldersViews;
		ArrayList<View> mListHoldersViewsNotInCDT;
		static HashSet<String> mListOfTags;
		//init block
		static {
			mListOfTags=new HashSet<String>();
		}
		
		public ViewHolder() {
			mSparseArrayHolderViews= new SparseArray<>();
			mSparseArrayHolderViewsNotInCDT= new SparseArray<>();
			mListHoldersViews= new ArrayList<>();
			mListHoldersViewsNotInCDT= new ArrayList<>();
		}
		public static void clearAllTags(){
            mListOfTags.clear();
        }
		public static HashSet<String> getAllLayoutTags(View v){

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
	
}
