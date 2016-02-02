package com.hh.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.hh.clientdatatable.ClientDataTable;
import com.hh.clientdatatable.ClientDataTable.CDTStatus;
import com.hh.clientdatatable.TCell;
import com.hh.droid.R;
import com.hh.execption.WrongTypeException;
import com.hh.features.PfKeyboard;
import com.hh.listeners.MyCallback;
import com.hh.ui.UiUtility;
import com.hh.ui.widget.UiPicassoImageView;
import com.hh.utility.PuUtils;

import java.util.ArrayList;


/**
 * This class is used to display data from a client data table to  layout widgets
 * @author WajdiHh
 * @see wajdihh@gmail.com
 *
 *
 */
public class CDTLayoutAdapter {


	public static final int FLAG_AUTO_REQUERY = 0x01;
	/**
	 * mClientDataTable : the client data table used in this adapter to display data
	 */
	protected ClientDataTable mClientDataTable;
	protected Context mContext;
	protected Resources mRes;
	private ViewGroup mLayout;
	private ViewHolder _mHolder;
	private ArrayList<String> _mListOfTags;
	private int mBase64OptionSize=2;
	public void setBase64OptionSize(int optionSize){
		mBase64OptionSize=optionSize;
	}

	/**<hr>
	 * Use this constructor to map data from the client data table to the layout parent widgets,
	 * <blockquote>For example : LinearLayout with 5 editText , WE MUST define tag for each editText
	 * ,<b>THE TAG MUST BE THE COLUMN TO DISPLAY in the client data table,<b> then we pass the parent view
	 * in parameters after the findViewByID etc.. and automatically w'll have the Data filled in different 
	 * EditText  <blockquote>
	 * <hr>
	 * @param pContext
	 * @param pLayout : the layout parent , it's can be a linearLayout , relativeLatout etc...
	 * @param pCDT : the client data table
	 */
	public CDTLayoutAdapter(Context pContext,ViewGroup pLayout,ClientDataTable pCDT){

		mClientDataTable=pCDT;
		mContext=pContext;
		mRes=pContext.getResources();
		mLayout=pLayout;
		ViewHolder.clearAllTags();
		_mListOfTags=new ArrayList<String>(ViewHolder.getAllLayoutTags(pLayout));
		mappingData();
	}

	/**<br>
	 * Use this constructor if we need to define the client data table with the setter
	 * @param pContext
	 * @param pLayout: the layout parent , it's can be a linearLayout , relativeLatout etc...
	 */
	public CDTLayoutAdapter(Context pContext,ViewGroup pLayout){

		mContext=pContext;
		mRes=pContext.getResources();
		mLayout=pLayout;
		ViewHolder.clearAllTags();
		_mListOfTags=new ArrayList<String>(ViewHolder.getAllLayoutTags(pLayout));
		mappingData();
	}
	/**
	 * Return the client data table used in this adapter
	 * @return : CDT
	 */
	public ClientDataTable getClientDataTable() {
		return mClientDataTable;
	}

	public ViewGroup getParentLayout(){
		return mLayout;
	}
	/**
	 * Set the client data table to use for mapping data in layout parent
	 * @param mClientDataTable
	 */
	public void setClientDataTable(ClientDataTable mClientDataTable) {
		this.mClientDataTable = mClientDataTable;
		mappingData();
	}

	/**
	 * this local method is used to create the widgets and listeners
	 */
	private void mappingData(){

		View lWidget=null;
		_mHolder=new ViewHolder();

		int lNbrTags=_mListOfTags.size();

		for (int i = 0; i < lNbrTags; i++) {

			lWidget=mLayout.findViewWithTag(_mListOfTags.get(i));

			if (lWidget!=null){
				if(mClientDataTable.indexOfColumn(_mListOfTags.get(i))!=-1)
					_mHolder.mListHoldersViews.add(lWidget);
				else
					_mHolder.mListHoldersViewsNotInCDT.add(lWidget);

				if(lWidget instanceof ImageView)
					lWidget.setBackgroundResource(R.drawable.selector_row);

				lWidget.setOnClickListener(new MyOnClickListener());
				if (lWidget instanceof CheckBox){
					CheckBox lCheckBox = (CheckBox) lWidget;
					lCheckBox.setOnCheckedChangeListener(new MyCheckedChangeListener());
				}
				if (lWidget instanceof TextView){
					final TextView lTextView = (TextView) lWidget;
					lTextView.setOnFocusChangeListener(new MyFocusChangeListener());
				}

				onCreateWidget(lWidget);
			}

		}
		bindData();
	}
	/**
	 * this local method is used to map data between the client data table and the
	 *  Different widgets on the layout parent
	 */
	private void bindData(){

		if(mClientDataTable.getRowsCount()==0)
			return;


		int lListHolderSize=_mHolder.mListHoldersViews.size();

		for(int i=0;i<lListHolderSize;i++) {

			View lWidget = _mHolder.mListHoldersViews.get(i);

			if(lWidget!=null){
				String tag=lWidget.getTag().toString();
				final TCell data = mClientDataTable.cellByName(tag);

				if (lWidget instanceof Checkable) {
					((Checkable) lWidget).setChecked(data.asBoolean());
				} else if (lWidget instanceof TextView) {
					((TextView) lWidget).setText(data.asString());
				} else if (lWidget instanceof UiPicassoImageView) {
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

				onIteratedWidget(lWidget, tag);
			}
		}

		int lListHolderSizeNotInCDT = _mHolder.mListHoldersViewsNotInCDT.size();
		for (int i = 0; i < lListHolderSizeNotInCDT; i++) {
			View lWidget= _mHolder.mListHoldersViewsNotInCDT.get(i);
			if (lWidget != null) {
				String tag=lWidget.getTag().toString();
				onIteratedWidget(lWidget, tag);
			}
		}
	}
	/**
	 * Local methode used to set image on the imageView widgets
	 * @param v
	 * @param value
	 */
	private void setViewImage(ImageView v, String value) {
		try {
			v.setImageResource(Integer.parseInt(value));
		} catch (NumberFormatException nfe) {
			v.setImageURI(Uri.parse(value));
		}
	}

	/**
	 * Override this method to capture click event on different widgets,
	 * it's Similar the onClick method on Android SDK , but in this case <b>We must define Tag
	 * on the widgets that's we need to click on</b> and test on different Tag to capture the click.
	 * <hr>
	 * @param widgetTag : the tag defined in widget
	 */
	protected  void onClickWidget(String widgetTag){};
	/**
	 * Override this method to define listeners or events when creating the different widgets
	 * <b>This method is invoked JUSTE one time on creating constructor</b>
	 * @param widget : the created view
	 */
	protected  void onCreateWidget(View widget){};
	/**
	 * Override this method to handle view and data in the client data table
	 * when iterating the parent view, <b>we invoke this method every time to call notifyDataSetChanged</b>
	 * @param widget :  View
	 */
	protected  void onIteratedWidget(View widget,String tag){};

	public void clear(){
		mClientDataTable.clear();
		notifyDataSetChanged();
	}

	/**
	 * <hr>{@linkplain notifyDataSetChanged}
	 * use this method to refresh Data
	 */
	public void notifyDataSetChanged(){
		if(mClientDataTable.isConnectedToDB())
			mClientDataTable.requery();

		bindData();

		UiUtility.clearAllChildrensFocus((ViewGroup) mLayout);
	}


	/**
	 * You must call this method, when w'll use the adapter in Edit Mode
	 */
	public void startEdit(){
		mClientDataTable.edit();
		mappingData();
	}

	public void startEditObserve(){
		mClientDataTable.editObserve();
		mappingData();
	}

	/**
	 * You must call this method, when w'll use the adapter in Insert Mode
	 */
	public void startInsert(){
		mClientDataTable.insert();
		mappingData();
	}

	public void startInsertObserve(){
		mClientDataTable.insertObserve();
		mappingData();
	}
	public void startAppend(){
		mClientDataTable.append();
		mappingData();
	}
	public void startAppendObserve(){
		mClientDataTable.appendObserve();
		mappingData();
	}
	public void startLoad(){
		mappingData();
	}
	public void startDelete(){
		mClientDataTable.delete();
		mappingData();
	}
	public void startDeleteObserve(){
		mClientDataTable.deleteObserve();
		mappingData();
	}

	public void executeChanges( ){
		validateChanges();
		mClientDataTable.execute();
	}

	public void executeChangesObserve( ){
		validateChanges();
		mClientDataTable.executeObserve();
	}

	public void commitChanges(){

		validateChanges();
		mClientDataTable.commit();
	}

	public void commitChangesObserve(){

		validateChanges();
		mClientDataTable.commitObserve();
	}

	public void executeChanges(MyCallback pMyCallback){
		validateChanges();
		mClientDataTable.execute(pMyCallback);
	}

	public void executeChangesObserve(MyCallback pMyCallback ){
		validateChanges();
		mClientDataTable.executeObserve(pMyCallback);
	}

	public void commitChanges(MyCallback pMyCallback){

		validateChanges();
		mClientDataTable.commit(pMyCallback);
	}

	public void commitChangesObserve(MyCallback pMyCallback){

		validateChanges();
		mClientDataTable.commitObserve(pMyCallback);
	}
	private void validateChanges(){

		UiUtility.clearAllChildrensFocus((ViewGroup)mLayout);
		PfKeyboard.hide(mContext, mLayout);

		if(mClientDataTable.getCDTStatus()==CDTStatus.DEFAULT){
			PuUtils.showMessage(mContext, "Error CDT", "CDT is in default STATUS, you can't validate in default");
			return;
		}

	}

	class MyFocusChangeListener implements View.OnFocusChangeListener{

		@Override
		public void onFocusChange(View v, boolean hasFocus) {

			TextView lTextView=(TextView) v;
			String lColumnName=v.getTag().toString();
			mClientDataTable.cellByName(lColumnName).setValue(lTextView.getText().toString());
		}
	}


	class MyCheckedChangeListener implements OnCheckedChangeListener{

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

			String lColumnName=buttonView.getTag().toString();
			mClientDataTable.cellByName(lColumnName).setValue(isChecked);

		}
	}

	class MyOnClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {

			String lTag="";
			if(v.getTag()!=null)
				lTag=v.getTag().toString();
			onClickWidget(lTag);
		}
	}

}
