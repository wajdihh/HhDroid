package com.hh.clientdatatable;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.hh.clientdatatable.TCell.ValueType;
import com.hh.database.DatabaseUtils;
import com.hh.droid.R;
import com.hh.execption.HhException;
import com.hh.listeners.MyCallback;
import com.hh.listeners.OnCDTColumnObserver;
import com.hh.listeners.OnCDTStatusObserver;
import com.hh.listeners.OnNotifyDataSetChangedListener;
import com.hh.utility.PuUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 *
 * @author WajdiHh
 *
 */
public class ClientDataTable {

	private final String TAG = "Client DataTable Content";
	private final String TAG_Error="HhDroidError";

	public enum SortType{/** Assendent*/ ASC,	/** Dessendent */	DESC}
	public enum CDTStatus {DEFAULT,UPDATE, INSERT,DELETE};
	public enum JSONObjectGeneratedMode {DEFAULT,NoEmptyField,FormatedDate};

	// List of Rows
	private ArrayList<TRow> _mListOfRows;
	private ArrayList<TRow> _mListOfDeletedRows;
	private TRow _mOldRow;
	// List Of columns
	private ArrayList<TColumn> _mListOfColumns;
	private Context _mContext;
	private int _mPosition;
	private int _mTempIteration;
	private int _mCursorSize;
	private Resources _mRes;
	private SQLiteDatabase _mSqliteDataBase;
	private String _mTableName;
	private String _mCDTName="";
	private StringBuffer _mWhereClause;
	private String[] _mWhereClauseColumns;
	private Cursor _mCursor;
	private CDTStatus _mCDTStatus=CDTStatus.DEFAULT;
	private ArrayList<TRow> _mListTempSortOfRows;
	private boolean _mIsExecInDateBase;
	private boolean mIsCdtSorted;
	private ArrayList<CDTNestedJSONObject> mNestedJSONObjects;
	private ArrayList<CDTNestedJSONArray> mNestedJSONArrays;
	private CDTObserverStack mCdtObserverStack;

	private OnNotifyDataSetChangedListener _mOnNotifyDataSetChangedListener;

	{
		_mListOfRows = new ArrayList<>();
		_mListOfColumns = new ArrayList<>();
		mNestedJSONObjects=new ArrayList<>();
		mNestedJSONArrays=new ArrayList<>();
		_mPosition = -1;
		_mTempIteration=-1;
		_mCursorSize = -1;
		mIsCdtSorted=false;
		mCdtObserverStack =new CDTObserverStack();
	}

	public ClientDataTable(Context pContext) {
		_mContext = pContext;
		_mRes = pContext.getResources();
	}

	public ClientDataTable(Context pContext,SQLiteDatabase pSqliteDataBase,
						   String pTableName,String[] pWhereClauseColumns) {
		_mContext = pContext;
		_mRes = pContext.getResources();
		_mSqliteDataBase=pSqliteDataBase;
		_mTableName=pTableName;
		_mWhereClauseColumns=pWhereClauseColumns;

		if(pSqliteDataBase==null)
			PuUtils.showMessage(_mContext, "Errueur Base de donn�e", "La base de donn�es est NULL");
		if(_mTableName==null && _mTableName.equals(""))
			PuUtils.showMessage(_mContext, "Errerur de table", "La table est non mentionn�e");

		if(pWhereClauseColumns!=null && pWhereClauseColumns.length!=0){

			_mWhereClause=new StringBuffer();
			_mWhereClause.append(pWhereClauseColumns[0]+"= ?");

			for (int i = 1; i < pWhereClauseColumns.length; i++)
				_mWhereClause.append("AND "+pWhereClauseColumns[i]+"= ?");
		}
	}

	public Context getContext(){
		return _mContext;
	}
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		_mContext = null;
		if(_mCursor!=null){
			_mCursor.close();
			_mCursor=null;
		}
	}

	public void setName(String pName){
		_mCDTName=pName;
	}
	public String getName(){
		return _mCDTName;
	}
	public CDTStatus getCDTStatus(){
		return _mCDTStatus;
	}
	public void setCDTStatus(CDTStatus pCdtStatus){
		_mCDTStatus=pCdtStatus;
	}

	public boolean isConnectedToDB(){
		if(_mSqliteDataBase!=null && !_mTableName.isEmpty() && _mIsExecInDateBase)
			return true;
		return false;
	}

	public Cursor getCursor(){
		return _mCursor;
	}
	public TRow getCurrentRow(){

		if(_mListOfRows.isEmpty())
			throw new AssertionError("The ClientDataTable is EMPTY !!");

		return _mListOfRows.get(_mPosition);
	}


	public void append(){
		append(new TRow(_mContext, getCDTStatus(), getListOfColumns()));
	}
	public void appendObserve(){
		appendObserve(new TRow(_mContext, getCDTStatus(), getListOfColumns()));
	}
	public void insert(){

		if(_mListOfRows.isEmpty())
			throw new AssertionError("Cannot insert because CDT is empty!!");

		if(_mPosition>=_mListOfRows.size())
			throw new AssertionError("Cannot insert because CDT Position is outbound the list rows size");

		if (getCDTStatus() == CDTStatus.DEFAULT){
			setCDTStatus(CDTStatus.INSERT);
		}
	}

	public void insertObserve(){
		mCdtObserverStack.notifyOnBeforeInsert();

		if(_mListOfRows.isEmpty())
			throw new AssertionError("Cannot insert because CDT is empty!!");

		if(_mPosition>=_mListOfRows.size())
			throw new AssertionError("Cannot insert because CDT Position is outbound the list rows size");

		if (getCDTStatus() == CDTStatus.DEFAULT){
			setCDTStatus(CDTStatus.INSERT);
		}
	}
	public void append(TRow row){
		append(row, false);
	}

	public void appendObserve(TRow row){
		append(row,true);
	}

	private void append(TRow row,boolean pIsObserve){

		if(pIsObserve)
			mCdtObserverStack.notifyOnBeforeInsert();

		if(getCDTStatus()==CDTStatus.DEFAULT || getCDTStatus()==CDTStatus.INSERT){
			setCDTStatus(CDTStatus.INSERT);
			addRow(row);
			_mPosition=_mListOfRows.size()-1;
		}else
			throw new AssertionError("Cannot append a new line because CDT is in mode :"+getCDTStatus().name()+" You must commit your change");
	}

	public void delete(){

		if(_mListOfRows.isEmpty())
			throw new AssertionError("Cannot Delete because CDT is empty!!");

		if(getCDTStatus()==CDTStatus.DEFAULT || getCDTStatus()==CDTStatus.DELETE){

			setCDTStatus(CDTStatus.DELETE);
			if(_mListOfDeletedRows==null)
				_mListOfDeletedRows=new ArrayList<>();
		}else
			throw new AssertionError("Cannot delete the selected row, because CDT is in mode :"+getCDTStatus().name()+"  You must commit your change first, to pass in mode DELETE");

		_mOldRow=new TRow(cloneListOfCells(getCurrentRow().getCells()));
	}

	public void deleteObserve(){

		if(_mListOfRows.isEmpty())
			throw new AssertionError("Cannot Delete because CDT is empty!!");

		mCdtObserverStack.notifyOnBeforeDelete();

		if(getCDTStatus()==CDTStatus.DEFAULT || getCDTStatus()==CDTStatus.DELETE){
			setCDTStatus(CDTStatus.DELETE);
			if(_mListOfDeletedRows==null)
				_mListOfDeletedRows=new ArrayList<>();
		}else
			throw new AssertionError("Cannot delete the selected row, because CDT is in mode :"+getCDTStatus().name()+"  You must commit your change first, to pass in mode DELETE");

		_mOldRow=new TRow(cloneListOfCells(getCurrentRow().getCells()));
	}

	public void edit(){

		if(_mListOfRows.isEmpty())
			throw new AssertionError("Cannot EDIT because CDT is empty!!");

		if(getCDTStatus()==CDTStatus.DEFAULT || getCDTStatus()==CDTStatus.UPDATE){

			setCDTStatus(CDTStatus.UPDATE);

			getCurrentRow().memorizeValues();

			_mOldRow=new TRow(cloneListOfCells(getCurrentRow().getCells()));
		}else
			throw new AssertionError("Cannot edit the selected row, because CDT is in mode :"+getCDTStatus().name()+"  You must commit your change first to pass in mode DEFAULT");
	}

	public void editObserve(){

		if(_mListOfRows.isEmpty())
			throw new AssertionError("Cannot EDIT because CDT is empty!!");

		mCdtObserverStack.notifyOnBeforeEdit();

		if(getCDTStatus()==CDTStatus.DEFAULT || getCDTStatus()==CDTStatus.UPDATE){

			setCDTStatus(CDTStatus.UPDATE);

			getCurrentRow().memorizeValues();

			_mOldRow=new TRow(cloneListOfCells(getCurrentRow().getCells()));
		}else
			throw new IllegalStateException("Cannot edit the selected row, because CDT is in mode :"+_mCDTStatus.name()+"   You must commit your change first to pass in mode DEFAULT");
	}
	/**
	 * permet de faire un clone dela liste des celles ( prb des references)
	 * @param list
	 * @return
	 */

	private ArrayList<TCell> cloneListOfCells(List<TCell> list) {
		ArrayList<TCell> clone = new ArrayList<TCell>(list.size());
		for(TCell item: list)
			try {
				clone.add(item.clone());
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
		return clone;
	}

	private void clearListOfDeletedRows(){
		if(_mListOfDeletedRows!=null)
			_mListOfDeletedRows.clear();

		_mListOfDeletedRows = null;
	}

	private void validate(boolean pIsUseCDTListener,boolean pIsExecInDateBase,MyCallback pCallback){


		_mIsExecInDateBase=pIsExecInDateBase;
		if(getCDTStatus()==CDTStatus.DELETE || getCDTStatus()==CDTStatus.UPDATE)
			if(getRowsCount()==0)
				throw new AssertionError("Cannot EDIT because CDT is empty!!");


		// if we have set CDTListener and the data is not valid return
		for (OnCDTStatusObserver listener: mCdtObserverStack) {
			if (!listener.onBeforeValidate()) {
				if (pCallback != null) pCallback.onError("");
				return;
			}
		}

		if(pIsExecInDateBase && isConnectedToDB())
			if(_mSqliteDataBase.isOpen())
				commitIntoDataBase(false);
			else
				throw new AssertionError("The Database is closed !");

		commitIntoCDT(pIsUseCDTListener,pIsExecInDateBase);
		setCDTStatus(CDTStatus.DEFAULT);

		resetValuesChanged();

		mCdtObserverStack.notifyOnAfterValidate(pIsExecInDateBase);

		if(pCallback!=null ) pCallback.onSuccess("");

		if(pIsExecInDateBase)
			clearListOfDeletedRows();


		if (_mListOfDeletedRows!=null && !_mListOfDeletedRows.isEmpty())
			System.out.println("#######################       LIST OLD TAILLE EST :" + _mListOfDeletedRows.size());

		if(_mOnNotifyDataSetChangedListener!=null)
			_mOnNotifyDataSetChangedListener.notifyValueChanged();
	}
	/**
	 * Apply changes on Date base (if connected) and refresh the layout with the new values
	 */
	public void commit(){
		validate(false,false,null);
	}

	public void commitObserve(){
		validate(true,false,null);
	}

	public void execute(){
		validate(false,true,null);
	}

	public void executeObserve() {
		validate(true,true,null);
	}
	public void commit(MyCallback pCallback) {
		validate(false,false,pCallback);
	}

	public void commitObserve(MyCallback pCallback) {
		validate(true,false,pCallback);
	}

	public void execute(MyCallback pCallback){
		validate(false, true, pCallback);
	}

	public void executeObserve(MyCallback pCallback){
		validate(true, true, pCallback);
	}

	public void revert(){

		mCdtObserverStack.notifyOnBeforeRevert();

		switch (getCDTStatus()){
			case INSERT:
				_mListOfRows.remove(_mPosition);
				setCDTStatus(CDTStatus.DEFAULT);
				break;

			case UPDATE:
				getCurrentRow().revertOldValues();
				setCDTStatus(CDTStatus.DEFAULT);
				break;

			case DELETE:
				if(_mOldRow!=null) {
					_mListOfRows.add(_mOldRow);
					_mOldRow.getCells().clear();
					_mOldRow = null;
				}
				setCDTStatus(CDTStatus.DEFAULT);
				break;
		}

		mCdtObserverStack.notifyOnAfterRevert();
		clearListOfDeletedRows();
	}

	/**
	 * Cancel action : edit, update or Delete, and set the CDT to Default without touching data
	 */
	public void cancel(){
		setCDTStatus(CDTStatus.DEFAULT);
	}

	/**
	 * Apply changes on Date base (if connected) , used when we set the _mIsTemporaryIgnoreDatabase to true
	 */
	public void executeAll(CDTStatus pCDTStatus){

		saveCDTChangesIntoDatabase(false, pCDTStatus);
	}

	public void executeObserveAll(CDTStatus pCDTStatus){

		saveCDTChangesIntoDatabase(true, pCDTStatus);
	}

	private void saveCDTChangesIntoDatabase(boolean isUseCDTListener,CDTStatus pCDTStatus){

		setCDTStatus(pCDTStatus);
		_mIsExecInDateBase=true;


		if(getCDTStatus()==CDTStatus.UPDATE &&(_mListOfDeletedRows==null|| _mListOfDeletedRows.isEmpty()))
			if(getRowsCount()==0)
				throw new AssertionError("Cannot EDIT because CDT is empty!!");



		if(isConnectedToDB() && _mSqliteDataBase.isOpen() && (getCDTStatus()==CDTStatus.INSERT || getCDTStatus()==CDTStatus.UPDATE)){
			int size = _mListOfRows.size();
			int savedPosition=getPosition();
			for (int i = 0; i < size; i++){

				moveToPosition(i);
				commitIntoDataBase(true);

				if(isUseCDTListener)
					if(getCDTStatus()==CDTStatus.INSERT)
						mCdtObserverStack.notifyOnAfterInsert(true);

					else if(getCDTStatus()==CDTStatus.UPDATE)
						mCdtObserverStack.notifyOnAfterEdit(_mOldRow,getCurrentRow(),true);
			}

			// Back to the saved position
			if(savedPosition!=-1)
				moveToPosition(savedPosition);
			if (_mListOfDeletedRows!=null && !_mListOfDeletedRows.isEmpty()) {

				size = _mListOfDeletedRows.size();
				for (int i = 0; i < size; i++){
					if( isUseCDTListener)
						mCdtObserverStack.notifyOnAfterDelete(_mListOfDeletedRows.get(i), true);

					deleteRowDataBase(_mListOfDeletedRows.get(i));
				}

				clearListOfDeletedRows();
			}

			setCDTStatus(CDTStatus.DEFAULT);

			resetValuesChanged();

		}else
			throw new AssertionError("The Database is closed !");

	}


	private void commitIntoCDT(boolean pIsUseCDTListener,boolean pIsExecuteAction){

		switch (getCDTStatus()) {
			case INSERT:
				// Pas besoin car on l a deja dans l Append()
				//_mListOfRows.add(new TRow(_mContext, _mCDTStatus, getColumnsCount()));

				if(pIsUseCDTListener) mCdtObserverStack.notifyOnAfterInsert(pIsExecuteAction);
				break;
			case DELETE:
				_mListOfDeletedRows.add(getCurrentRow());
				_mListOfRows.remove(_mPosition);
				if(pIsUseCDTListener) mCdtObserverStack.notifyOnAfterDelete(_mListOfDeletedRows.get(_mListOfDeletedRows.size()-1), pIsExecuteAction);
				if (_mPosition == getRowsCount()){
					_mPosition--;
				}

				_mTempIteration=-1;
				break;

			case UPDATE:

				if(pIsUseCDTListener && isValuesChanged()) mCdtObserverStack.notifyOnAfterEdit(_mOldRow, getCurrentRow(), pIsExecuteAction);
				if(_mOldRow!=null) {
					_mOldRow.getCells().clear();
					_mOldRow = null;
				}
				break;
			default:
				break;
		}
	}
	private void commitIntoDataBase(boolean pInsertIfNotUpdated){

		switch (getCDTStatus()) {
			case INSERT:
				insertInDB();
				break;
			case UPDATE:
				updateInDB(pInsertIfNotUpdated);
				break;
			case DELETE:
				deleteFromDB();
				break;
			case DEFAULT:
				Log.e("commitIntoDataBase","No commit, because the CDT is in Default mode");
				break;
			default:
				break;
		}
	}

	private void insertInDB(){

		ContentValues lValues=new ContentValues();

		int lSize=_mListOfColumns.size();

		for (int i = 0; i <lSize; i++) {

			TColumn column=_mListOfColumns.get(i);
			String lColumnName=column.getName();

			TCell lCell=cellByName(lColumnName);
			String lColumnValue=lCell.asValue();

			if(column.getColumnType()== TColumn.ColumnType.JsonField ||column.getColumnType()== TColumn.ColumnType.JsonParent
					||column.getColumnType()== TColumn.ColumnType.ToIgnoreInDB
					|| column.getColumnType()== TColumn.ColumnType.PrimaryKey
					|| lColumnValue.equals(_mRes.getString(R.string.cst_notInCursor)))
				continue;


			lValues.put(lColumnName, lColumnValue);
		}
		if(lValues.size()==0){
			PuUtils.showMessage(_mContext, "Erreur insertion", "Acune valeur definie pour inserer");
			return;
		}

		_mSqliteDataBase.insertOrThrow(_mTableName, null, lValues);

		//Updated primary Key
		TCell tCellPrimaryKey=getPrimaryKeyCell();
		if (tCellPrimaryKey != null)
			tCellPrimaryKey.setValue(DatabaseUtils.getLastPrimaryKeyValue(_mSqliteDataBase, _mTableName));
	}

	private void updateInDB(boolean pInsertIfNotUpdated){

		if(_mWhereClause!=null){

			ContentValues lValues=new ContentValues();
			String[] lArgs=new String[_mWhereClauseColumns.length];
			int lSize=_mListOfColumns.size();

			for (int i = 0; i <lSize; i++) {


				TColumn column=_mListOfColumns.get(i);
				String lColumnName=column.getName();
				TCell lCell=cellByName(lColumnName);
				String lColumnValue=lCell.asValue();

				if(column.getColumnType()== TColumn.ColumnType.JsonField ||column.getColumnType()== TColumn.ColumnType.JsonParent
						||column.getColumnType()== TColumn.ColumnType.ToIgnoreInDB
						|| column.getColumnType()== TColumn.ColumnType.PrimaryKey
						|| lColumnValue.equals(_mRes.getString(R.string.cst_notInCursor)))
					continue;

				lValues.put(lColumnName, lColumnValue);
			}


			for (int i = 0; i < _mWhereClauseColumns.length; i++) {

				if(indexOfColumn(_mWhereClauseColumns[i])==-1){
					PuUtils.showMessage(_mContext, "Erreur Column", "Column "+_mWhereClauseColumns[i]+" de clause Where  " +
							"n'est pas une column de client data table :"+_mCDTName);
					return;
				}else if(lValues.containsKey(_mWhereClauseColumns[i])){
					PuUtils.showMessage(_mContext, "Erreur Column", "impossible de definir la colonne "+
							_mWhereClauseColumns[i]+" de clause Where comme colonne a modifier dans le CDT:"+_mCDTName);
					return;
				}
				TCell lCell=cellByName(_mWhereClauseColumns[i]);
				lArgs[i]=lCell.asString();

			}


			if(lValues.size()!=0){
				int resultUpdate=_mSqliteDataBase.update(_mTableName, lValues, _mWhereClause.toString(),lArgs);
				// If updated fails so row is not existe, so we must to add It
				if(pInsertIfNotUpdated && resultUpdate==0){
					_mSqliteDataBase.insertOrThrow(_mTableName,null,lValues);
					//Updated primary Key
					TCell tCellPrimaryKey=getPrimaryKeyCell();
					if (tCellPrimaryKey != null)
						tCellPrimaryKey.setValue(DatabaseUtils.getLastPrimaryKeyValue(_mSqliteDataBase, _mTableName));
				}

			}


		}else
			PuUtils.showMessage(_mContext, "Erreur Update", "Clause where non definit");
	}

	private void deleteFromDB(){
		deleteRowDataBase(getCurrentRow());
	}

	private void deleteRowDataBase(TRow pTRow){

		if(_mWhereClause!=null){

			String[] lArgs=new String[_mWhereClauseColumns.length];

			for (int i = 0; i < _mWhereClauseColumns.length; i++) {
				TCell lCell=pTRow.cellByName(_mWhereClauseColumns[i]);

				lArgs[i]=lCell.asValue();
				if(lCell.asValue().equals(""))
					Log.e("ERROR Delete from database","Cannot delete the element, because the constraint value of "+_mWhereClauseColumns[i]+" is empty ! or element is not exist in database");
			}

			_mSqliteDataBase.delete(_mTableName, _mWhereClause.toString(), lArgs);
		}else
			throw new AssertionError(_mRes.getString(R.string.assertError_clauseWereNotFoundWhenDeletingPart1)+_mTableName+" "+_mRes.getString(R.string.assertError_clauseWereNotFoundWhenDeletingPart2));
	}
	/**
	 * Test if the C data table is empty
	 *
	 * @return false/true
	 */
	public boolean isEmpty() {
		if (_mListOfRows.size() > 0)
			return false;
		return true;
	}

	/**
	 * Cette methode permet d intaliser le cdt pour l itération
	 * par exemple dans une interation avec une boucle si on met un break, donc notre CDT ne va pas continuer jusqu'au bout
	 * du coup on aura pas une intilisation de tempIteration dans la fonction moveToPosition
	 */
	public void initForIterate(){
		_mTempIteration=-1;
	}
	/**
	 * iterate the CDT, must user moveToFisrt Before the wihle LOOP
	 *
	 * NE JAMAIS METTRE UN BREAK DAANS Iterate, si c'est le cas, ajouter initForIterate()
	 */
	public boolean iterate() {
		// Init
		if(_mTempIteration==-1){
			_mTempIteration=_mPosition;
			_mPosition=-1;
		}

		// recover old position value
		if(_mPosition + 1>=getRowsCount()){
			if(_mTempIteration!=-1){
				_mPosition=_mTempIteration;
				_mTempIteration=-1;
			}
			return false;
		}

		return moveToPosition(_mPosition + 1);
	}
	/**
	 * Move to next row
	 */
	public boolean moveToNext() {
		if(_mPosition + 1>=getRowsCount())
			return false;

		return moveToPosition(_mPosition + 1);
	}

	/**
	 * Move to the previous row.
	 */
	public boolean moveToPrevious() {
		if(_mPosition -1<=0)
			return false;
		return moveToPosition(_mPosition -1);
	}


	/**
	 * for positioning the CDT to the specific row, if the id found
	 */

	public boolean findRowByID(String idColumnName,int idValue){

		int i=0;
		for (TRow row:_mListOfRows){
			if(row.cellByName(idColumnName).asInteger()==idValue){
				moveToPosition(i);
				return true;
			}
			i++;
		}
		return false;
	}
	/**
	 * Move to row position at pIndex
	 *
	 * @param pIndex
	 * @return true if moved
	 */
	public boolean moveToPosition(int pIndex) {

		int count = getRowsCount();
		if (pIndex >= count || pIndex <0 )
			throw new IndexOutOfBoundsException("ClientDataTable size =" + count + " and selected index =" + pIndex);

		_mPosition = pIndex;
		return true;
	}

	/**
	 * Sort the client data table with priority of columns passed in parametres
	 *
	 *            : The order of Sort ASSENDING or Decedding
	 */
	public void sort(LinkedHashMap<String, SortType> pListOfSortedFieldName) {

		if (_mListTempSortOfRows == null)
			_mListTempSortOfRows = new ArrayList<TRow>(_mListOfRows);

		if (!pListOfSortedFieldName.isEmpty())
			Collections.sort(_mListOfRows, TRowComparator.getInstance(pListOfSortedFieldName));

		mIsCdtSorted=true;

	}

	/**
	 * Permet d'annuler le trie sur tous les champs de CDS
	 *  @author Wajdi Hh : 01/11/2013
	 */
	public void cancelAllSort(){
		_mListOfRows.clear();
		_mListOfRows.addAll(_mListTempSortOfRows);

		mIsCdtSorted=false;

	}

	/**
	 * Check if column passed on param contains value
	 *
	 * @param pColumnName
	 * @param pValue
	 * @return true if column contains / false else
	 */
	public boolean isColumnContains(String pColumnName, String pValue) {

		boolean lIsContains = false;
		int lColumnIndex = indexOfColumn(pColumnName);
		if (lColumnIndex != -1) {
			ArrayList<TCell> lColumnCells = getColumnCells(lColumnIndex);
			int lCellsSize = lColumnCells.size();
			for (int i = 0; i < lCellsSize; i++) {
				if (lColumnCells.get(i).asString().equalsIgnoreCase(pValue)) {
					lIsContains = true;
					break;
				}
			}
		}
		return lIsContains;
	}

	public boolean isColumnContains(String pColumnName, boolean pValue) {

		boolean lIsContains = false;
		int lColumnIndex = indexOfColumn(pColumnName);
		if (lColumnIndex != -1) {
			ArrayList<TCell> lColumnCells = getColumnCells(lColumnIndex);
			int lCellsSize = lColumnCells.size();
			for (int i = 0; i < lCellsSize; i++) {
				if (lColumnCells.get(i).asBoolean()==pValue) {
					lIsContains = true;
					break;
				}
			}
		}
		return lIsContains;
	}

	/**
	 * return the index of the column Name if existe , else return -1
	 *
	 * @param pColumnName
	 * @return index of columnName
	 */
	public int indexOfColumn(String pColumnName) {

		int lIndex = -1;
		int lColumnNumber = getColumnsCount();
		for (int i = 0; i < lColumnNumber; i++) {
			if (_mListOfColumns.get(i).getName().equalsIgnoreCase(pColumnName)) {
				lIndex = i;
				break;
			}
		}
		return lIndex;
	}

	/**
	 * get cell by Name
	 *
	 * @param pCellName
	 * @return the cell
	 */
	public TCell cellByName(String pCellName) {

		if(getRowsCount()==0)
			throw new AssertionError("The ClientDataTable is EMPTY ");

		TCell lResult;
		if(isConnectedToDB())
			lResult=new TCell(_mContext,ValueType.TEXT, getCDTStatus(),pCellName);
		else
			lResult=new TCell();

		int lColumnSize = getColumnsCount();
		boolean lIsCellFound=false;
		for (int i = 0; i < lColumnSize && getRowsCount() != 0; i++) {
			if (_mListOfColumns.get(i).getName().equalsIgnoreCase(pCellName)) {
				lResult = getCell(_mPosition, i);
				lResult.setCDTStatus(getCDTStatus());
				lResult.setValueType(_mListOfColumns.get(i).getValueType());
				lResult.setOnCDTColumnListener(_mListOfColumns.get(i).getCDTColumnListener());
				lIsCellFound=true;
				break;
			}

		}
		if(!lIsCellFound)
			throw new AssertionError("Wrong cell Name :! There no cellName called :"+pCellName);

		return lResult;
	}

	public TCell getPrimaryKeyCell() {

		if(getRowsCount()==0)
			throw new AssertionError("Cannot EDIT because CDT is empty!!");

		TCell lResult=null;
		int lColumnSize = getColumnsCount();
		boolean lIsCellFound=false;
		for (int i = 0; i < lColumnSize && getRowsCount() != 0; i++) {
			if (_mListOfColumns.get(i).getColumnType()== TColumn.ColumnType.PrimaryKey) {
				lResult = getCell(_mPosition, i);
				lResult.setValueType(_mListOfColumns.get(i).getValueType());
				lResult.setName(_mListOfColumns.get(i).getName());
				lResult.setOnCDTColumnListener(_mListOfColumns.get(i).getCDTColumnListener());
				lIsCellFound=true;
				break;
			}

		}
		if(!lIsCellFound)
			PuUtils.showMessage(_mContext, "NO PRIMARY KEY NAME","No column with primary key name has found");

		return lResult;
	}
	/**
	 * Get the SUM of the cells Column IF THE TYPE is a NUMBER,else the asFloat
	 * will return 0 and the Sum will be 0
	 *
	 * @param pColumnName
	 * @return Sum of CELLS
	 */
	public float getSumOfColumnValues(String pColumnName) {

		float lResult = 0;
		int lColumnIndex = indexOfColumn(pColumnName);
		if (lColumnIndex != -1) {
			ArrayList<TCell> lColumnCells = getColumnCells(lColumnIndex);
			int lCellsSize = lColumnCells.size();

			for (int i = 0; i < lCellsSize; i++) {
				float lCellValue = lColumnCells.get(i).asFloat();
				if (lCellValue != -1)
					lResult += lCellValue;

			}
		}
		return lResult;
	}

	public void requery(){

		if(isConnectedToDB() && _mCursor!=null && !mIsCdtSorted){
			_mCursor.requery();
			fillFromCursor(_mCursor);
		}else{
			ArrayList<TRow> temp=new ArrayList<>();
			temp.addAll(_mListOfRows);
			_mListOfRows.clear();
			_mListOfRows.addAll(temp);
			temp.clear();
			temp=null;
		}

	}

	public void fillFromTable(String pSQLCommand){

		if(_mSqliteDataBase==null)
			throw new AssertionError("This clientDataTable has not configured database, try to use getDatabase in the constructor of the CDT");

		_mCursor= _mSqliteDataBase.rawQuery(pSQLCommand, null);
		_mCursor.moveToFirst();
		fillFromCursor(_mCursor);
		Log.i("fillFromTable", " Count :" + _mCursor.getCount());


	}

	/**
	 * Fill the client Data table from an Android Cursor Database
	 *
	 * @param pCursor
	 */
	public void fillFromCursor(Cursor pCursor) {

		_mCursor=pCursor;
		_mCursorSize = pCursor.getCount();
		_mListOfRows.clear();
		_mListOfRows.ensureCapacity(_mCursorSize);

		String[] lCursorColumnsNames = pCursor.getColumnNames();
		int lNbrColumnsCDT = getColumnsCount();

		int lNbrColumnsCursor = lCursorColumnsNames.length;

		if (lNbrColumnsCDT == 0) {
			for (int i = 0; i < lNbrColumnsCursor; i++) {
				_mListOfColumns.add(new TColumn(lCursorColumnsNames[i],ValueType.TEXT));
			}
		}
		if (_mCursorSize > 0) {

			pCursor.moveToFirst();
			_mPosition = 0;
			while (!pCursor.isAfterLast()) {
				TRow lRow = new TRow();

				if (lNbrColumnsCDT > 0) {
					for (int i = 0; i < lNbrColumnsCDT; i++) {
						boolean lIsColumnFound=false;
						TColumn lColumn = _mListOfColumns.get(i);
						for (int j = 0; j < lNbrColumnsCursor; j++) {
							if (lColumn != null && lColumn.getName().equals(lCursorColumnsNames[j])) {
								lRow.addCell(_mContext,pCursor.getString(j),lColumn.getValueType(),lColumn.getCellType(),lColumn.getName(),getCDTStatus(),lColumn.getCDTColumnListener());
								lIsColumnFound=true;
								break;
							}
						}
						if(!lIsColumnFound) {
							assert lColumn != null;
							lRow.addCell(_mContext,_mRes.getString(R.string.cst_notInCursor),lColumn.getValueType(),lColumn.getCellType(),lColumn.getName(),getCDTStatus(),lColumn.getCDTColumnListener());
						}
					}
				} else {
					for (int i = 0; i < lNbrColumnsCursor; i++) {
						String lColumnName = lCursorColumnsNames[i];
						if (lColumnName != null && !lColumnName.equals(""))
							lRow.addCell(_mContext,pCursor.getString(i), ValueType.TEXT, TCell.CellType.NONE,lColumnName,getCDTStatus(),null);
					}
				}
				_mListOfRows.add(lRow);
				pCursor.moveToNext();
			}
		}
		Log.i("fillFromCursor", " Count :" + _mCursorSize);
	}

	/**
	 * Clear ClientDataTable Content
	 */
	public void clear() {
		_mCursorSize = -1;
		_mListOfRows.clear();
		setCDTStatus(CDTStatus.DEFAULT);
	}

	/**
	 * Clear ClientDataTable Content And table
	 */

	public void clearAttachedDatabaseTable() {
		_mCursorSize = -1;
		_mListOfRows.clear();
		setCDTStatus(CDTStatus.DEFAULT);
		_mIsExecInDateBase=true;
		if(isConnectedToDB()){
			_mSqliteDataBase.delete(_mTableName,null,null);
			_mIsExecInDateBase=false;
		}
	}

	/**
	 * Clear ClientDataTable Content And table
	 */

	public void clearAndExecute() {
		setCDTStatus(CDTStatus.DEFAULT);
		_mCursorSize = -1;
		while (iterate()){
			delete();
			execute();
		}
	}

	public void clearAndExecuteObserve() {
		setCDTStatus(CDTStatus.DEFAULT);
		_mCursorSize = -1;
		while (iterate()){
			delete();
			executeObserve();
		}
	}
	/**
	 * Returns whether the Client Data table is pointing to the last row.
	 *
	 * @return
	 */
	public boolean isLast() {
		return (_mPosition == getRowsCount() - 1);
	}

	/**
	 * Returns whether the Client Data table is pointing to the first row.
	 *
	 * @return
	 */
	public boolean isFirst() {
		return (_mPosition == 0);
	}

	/**
	 * get the current position of the ClientDataTable
	 *
	 * @return
	 */
	public int getPosition() {
		return _mPosition;
	}

	/**
	 * Move the CDT to the first Row
	 */
	public boolean moveToFirst() {
		return moveToPosition(0);
	}

	/**
	 * Move the CDT to the last Row
	 */
	public boolean moveToLast() {
		return moveToPosition(getRowsCount()-1);
	}


	/**
	 * Check if the CDT is AfterLast row
	 *
	 * @return
	 */
	public boolean isAfterLast() {
		if (getRowsCount() == 0) {
			return true;
		}
		return _mPosition == getRowsCount();
	}

	/**
	 * allows to add a column
	 *
	 * @param pColumn
	 */
	public void addColumn(TColumn pColumn) {

		_mListOfColumns.add(pColumn);
	}

	/**
	 * Add Columns with name and Type
	 *
	 * @param pName
	 * @param pType
	 */
	public TColumn addColumn(String pName, ValueType pType) {
		TColumn column=new TColumn(pName, pType);
		_mListOfColumns.add(column);
		return column;
	}

	public TColumn addColumn(String pName,TColumn.ColumnType pColumnType) {
		TColumn column=new TColumn(pName, ValueType.TEXT,pColumnType);
		_mListOfColumns.add(column);
		return column;
	}

	public TColumn addColumn(String pName, ValueType pType,TColumn.ColumnType pColumnType) {

		TColumn column=new TColumn(pName, pType,pColumnType);
		_mListOfColumns.add(column);
		return column;
	}

	public TColumn addColumn(String pName, ValueType pType,TColumn.ColumnType pColumnType,OnCDTColumnObserver pListener) {

		TColumn column=new TColumn(pName, pType,pColumnType,pListener);
		_mListOfColumns.add(column);
		return column;
	}


	public TColumn addColumn(String pName, ValueType pType,OnCDTColumnObserver pListener) {

		TColumn column=new TColumn(pName, pType, TCell.CellType.NONE,pListener);
		_mListOfColumns.add(column);
		return column;
	}
	public TColumn addColumn(String pName, ValueType pType,TCell.CellType pCellType) {

		TColumn column=new TColumn(pName, pType,pCellType,null);
		_mListOfColumns.add(column);
		return column;
	}
	public TColumn addColumn(String pName, ValueType pType,TCell.CellType pCellType,OnCDTColumnObserver pListener) {

		TColumn column=new TColumn(pName, pType,pCellType,pListener);
		_mListOfColumns.add(column);
		return column;
	}
	/**
	 * allows to add a Row
	 *
	 */
	public void addRow(TRow pRow) {

		_mListOfRows.add(pRow);
	}

	/**
	 * Add row from values with differents type : boolean, String etc...
	 *
	 * @param values
	 */
	public void addRowFromValues(Object... values) {

		Iterator<TColumn> columnIt = _mListOfColumns.listIterator();
		TRow lRow = new TRow();

		for (int i = 0; i < values.length && columnIt.hasNext(); i++) {

			TColumn lCol = columnIt.next();
			if(_mSqliteDataBase==null)
				lRow.addCell(_mContext,values[i], lCol.getValueType(),lCol.getCellType(),lCol.getName(),getCDTStatus(),lCol.getCDTColumnListener());
			else
				lRow.addCell(_mContext,values[i], lCol.getValueType(),lCol.getCellType(),lCol.getName(),getCDTStatus(),lCol.getCDTColumnListener());
		}

		addRow(lRow);
	}

	/**
	 * Return the number of Columns
	 *
	 * @return
	 */
	public int getColumnsCount() {
		return _mListOfColumns.size();
	}

	/**
	 * Return the number of rows
	 *
	 * @return
	 */
	public int getRowsCount() {

		return _mListOfRows.size();
	}
	/**
	 * Return the number of rows
	 *
	 * @return
	 */
	public int size() {

		return _mListOfRows.size();
	}
	/**
	 * Return the liste of columns
	 * @return
	 */
	public ArrayList<TColumn> getListOfColumns(){
		return _mListOfColumns;
	}
	/**
	 * Return the liste of rows
	 * @return
	 */
	public ArrayList<TRow> getListOfRows(){
		return _mListOfRows;
	}

	/**
	 * Retun an array of Column content
	 * @param pFieldName
	 * @return
	 */
	public ArrayList<String> values(String pFieldName){
		ArrayList<String> list=new ArrayList<>();
		for (TRow row:_mListOfRows){
			list.add(row.cellByName(pFieldName).asValue());
		}
		return list;
	}
	/**
	 * Return if value existe
	 * @param value
	 * @return
	 */
	public boolean isValueExist(String fieldName,String value){

		for (TRow row:_mListOfRows){
			if(row.cellByName(fieldName).asString().equals(value))
				return true;
		}
		return false;
	}
	/**
	 * Retrun List of cells of column X
	 *
	 * @param pColumnIndex
	 * @return List of Cells
	 */
	public ArrayList<TCell> getColumnCells(int pColumnIndex) {

		ArrayList<TCell> lListOfCells = new ArrayList<TCell>(getRowsCount());

		for (TRow row : _mListOfRows) {
			lListOfCells.add(row.getCell(pColumnIndex));
		}

		return lListOfCells;
	}

	/**
	 * Return a Cell from the table
	 *
	 * @param pRowIndex
	 * @param pColumnIndex
	 * @return
	 */
	public TCell getCell(int pRowIndex, int pColumnIndex) {
		return _mListOfRows.get(pRowIndex).getCell(pColumnIndex);
	}

	/**
	 * Set Cell on the table
	 *
	 * @param pRowIndex
	 * @param pColumnIndex
	 * @param pCell
	 * @throws HhException
	 * @throws IndexOutOfBoundsException
	 */
	public void setCell(int pRowIndex, int pColumnIndex, TCell pCell)
			throws HhException, IndexOutOfBoundsException {

		TRow row = _mListOfRows.get(pRowIndex);

		if (!row.getCell(pColumnIndex).getValueType()
				.equals(pCell.getValueType())) {
			throw new HhException(
					"New cell value type does not match expected value type."
							+ " Expected type: "
							+ row.getCell(pColumnIndex).getValueType()
							+ " but was: " + pCell.getValueType());
		}
		row.setCell(pColumnIndex, pCell);
	}

	public void addCell(int pRowIndex, int pColumnIndex, TCell pCell) {
		_mListOfRows.get(pRowIndex).setCell(pColumnIndex, pCell);
	}


	public boolean isValuesChanged(){

		if(_mListOfDeletedRows!=null && !_mListOfDeletedRows.isEmpty())
			return true;

		for (TColumn column:_mListOfColumns){
			for (TRow row :_mListOfRows){
				TCell cell=row.cellByName(column.getName());
				if(cell.isValueChanged()){
					return  true;
				}
			}
		}
		return false;
	}

	private void resetValuesChanged(){

		for (TColumn column:_mListOfColumns){
			for (TRow row :_mListOfRows){
				TCell cell=row.cellByName(column.getName());
				cell.setValueChanged(false);
			}
		}
	}

	/**
	 * Returns a new data table, with the same data and metadata as this one.
	 * Any change to the returned table should not change this table and vice
	 * versa. This is a deep clone.
	 *
	 * @return The cloned data table.
	 */
	public ClientDataTable clone() {

		ClientDataTable lResult = null;
		if(_mTableName!=null && _mSqliteDataBase!=null && _mWhereClauseColumns !=null)
			lResult =new ClientDataTable(_mContext,_mSqliteDataBase,_mTableName,_mWhereClauseColumns);
		else
			lResult =new ClientDataTable(_mContext);


		if(mCdtObserverStack!=null  && !mCdtObserverStack.isEmpty())
			for (OnCDTStatusObserver o:mCdtObserverStack)
				lResult.setOnCDTStatusObserver(o);

		for (TColumn iTColumn : _mListOfColumns) {
			lResult.addColumn(iTColumn);
		}

		for (TRow iTRow : _mListOfRows) {
			lResult.addRow(iTRow);
		}

		return lResult;
	}



	public void addJSONArray(String key,String parentKey,ClientDataTable arrays){
		mNestedJSONArrays.add(new CDTNestedJSONArray(parentKey, key, arrays));
	}

	public void addNestedJSONObject(String key,String parentKey,ClientDataTable jsonObjectCDT,JSONObjectGeneratedMode pMode){
		mNestedJSONObjects.add(new CDTNestedJSONObject(parentKey, key, jsonObjectCDT,pMode));
	}

	public JSONArray toJSONArray(JSONObjectGeneratedMode... jsonObjectGeneratedMode) throws JSONException {
		return createJsonArray(jsonObjectGeneratedMode);
	}

	public JSONObject toJSONObject(JSONObjectGeneratedMode... jsonObjectGeneratedMode) throws JSONException {
		return toJSONObject(getCurrentRow(), jsonObjectGeneratedMode);
	}


	public JSONObject toJSONObject(TRow row,JSONObjectGeneratedMode... jsonObjectGeneratedMode) throws JSONException {
		return createJson(row, jsonObjectGeneratedMode);
	}

	private JSONArray createJsonArray(JSONObjectGeneratedMode... jsonObjectGeneratedMode) throws JSONException {

		JSONArray arrays=new JSONArray();
		for (TRow row:_mListOfRows){
			arrays.put(toJSONObject(row, jsonObjectGeneratedMode));
		}

		return arrays;
	}

	private JSONObject createJson(TRow row,JSONObjectGeneratedMode... jsonObjectGeneratedMode) throws JSONException {

		JSONObject mainJsonObject=new JSONObject();
		//JSONObject currentParent = null;
		Map<String,JSONObject> map=new HashMap<>();
		for (TColumn column:_mListOfColumns){

			if(column.getColumnType()== TColumn.ColumnType.JsonParent) {

				if(column.getJsonParent()==null ||column.getJsonParent().isEmpty()){
					mainJsonObject.put(column.getName(),new JSONObject());
					map.put(column.getName(),mainJsonObject.optJSONObject(column.getName()));
				}else{
					// if the parent has a parent
					if(column.getJsonParent()!=null && !column.getJsonParent().isEmpty()){

						if(column.getJsonParent().equals("MAIN")){
							mainJsonObject.put(column.getName(),new JSONObject());
							map.put(column.getName(),mainJsonObject.optJSONObject(column.getName()));
						}else{
							JSONObject parent=map.get(column.getJsonParent());
							parent.put(column.getName(),new JSONObject());
							map.put(column.getName(),parent.optJSONObject(column.getName()));
						}
					}
				}
			}else{
				if(column.getJsonParent()!=null){
					// If the json parent is "MAIN"
					if(column.getJsonParent().equals("MAIN")){
						map.put(column.getName(),mainJsonObject);
						fillJsonField(row,column,map.get(column.getJsonParent()),jsonObjectGeneratedMode);
					}else
						fillJsonField(row,column,map.get(column.getJsonParent()),jsonObjectGeneratedMode);
				}else{
					if(column.isIgnoredAsJsonField())
						continue;

					map.put("MAIN",mainJsonObject);
					fillJsonField(row,column,map.get("MAIN"), jsonObjectGeneratedMode);
				}

			}
		}

		// Json Arrays
		if(!mNestedJSONArrays.isEmpty()){


			for (CDTNestedJSONArray entry : mNestedJSONArrays)
			{

				if(entry.getClientDataTable()==null)
					throw new RuntimeException(entry.getKey()+" has a null value, check your clientDataTable instantiation");


				String parentKey=entry.getParentKey();
				JSONArray subArrays=entry.getClientDataTable().toJSONArray();
				JSONObject jsonParent=map.get(parentKey);

				jsonParent.put(entry.getKey(),subArrays);
			}
		}

		// JSon Object
		if(!mNestedJSONObjects.isEmpty()){

			for (CDTNestedJSONObject entry :mNestedJSONObjects)
			{

				if(entry.getClientDataTable()==null){
					throw new RuntimeException(entry.getKey()+" has a null value, check your clientDataTable instantiation");
				}

				String parentKey=entry.getParentKey();
				JSONObjectGeneratedMode mode=entry.getGeneratedMode();
				if(entry.getClientDataTable().isEmpty())
					HhException.raiseErrorException("Cannot EDIT because CDT is empty!! >> "+entry.getKey());
				else {
					JSONObject subJSONObject = entry.getClientDataTable().toJSONObject(mode);
					JSONObject jsonParent = map.get(parentKey);

					jsonParent.put(entry.getKey(), subJSONObject);
				}
			}
		}

		return mainJsonObject;
	}

	private void fillJsonField(TRow row,TColumn column,JSONObject jsonObject,JSONObjectGeneratedMode... jsonObjectGeneratedMode) throws JSONException {

		TCell cell = row.cellByName(column.getName());
		boolean hasNoEmptyFieldMode=false;
		boolean hasFormatedDate=false;

		for (JSONObjectGeneratedMode mode:jsonObjectGeneratedMode){
			if(mode==JSONObjectGeneratedMode.NoEmptyField)
				hasNoEmptyFieldMode=true;

			if(mode==JSONObjectGeneratedMode.FormatedDate)
				hasFormatedDate=true;
		}

		if(hasNoEmptyFieldMode && cell.isEmpty())
			return;

		if(cell.getValueType()==ValueType.BOOLEAN)
			jsonObject.put(column.getName(),cell.asBoolean());
		else if(cell.getValueType()==ValueType.INTEGER)
			//TODO when change the mecansime to Field , we will have @defaultValue annatotation so we must remove this test
			if(cell.asInteger()==-1)
				jsonObject.put(column.getName(),null);
			else
				jsonObject.put(column.getName(),cell.asInteger());

		else if(cell.getValueType()==ValueType.DOUBLE)
			if(cell.asDouble()==-1)
				jsonObject.put(column.getName(),null);
			else
				jsonObject.put(column.getName(),cell.asDouble());
		else if(cell.getValueType()==ValueType.DATETIME)
			jsonObject.put(column.getName(),hasFormatedDate?cell.asDateString():cell.asDateTime());
		else
			jsonObject.put(column.getName(), cell.asValue());
	}
	/**
	 *
	 * to display client data table content in LogCat
	 * @param pColumnsToDisplay : List of columns to display
	 * <strong>if we put * we will display all columns content</strong>
	 */
	public void displayContent(String ...pColumnsToDisplay) {

		StringBuffer lColumns = new StringBuffer();
		int lSize = _mListOfRows.size();
		if(pColumnsToDisplay.length==0){

			for (int i = 0; i < _mListOfColumns.size(); i++)
				lColumns.append(_mListOfColumns.get(i).getName() + " | ");
			// Display columns names
			Log.d(TAG, "COLUMNS:  " + lColumns.toString() + "\n");

			// Display rows
			for (int i = 0; i < lSize; i++)
				Log.v(TAG, "ROW NO" + (i + 1) + ":  " + _mListOfRows.get(i).getContent());

		}else{
			for (int i = 0; i < pColumnsToDisplay.length; i++)
				lColumns.append(pColumnsToDisplay[i] + " | ");

			// Display columns names
			Log.d(TAG, "COLUMNS:  " + lColumns.toString() + "\n");

			for (int i = 0; i < lSize; i++) {

				StringBuffer lRowContent=new StringBuffer();

				for (int j = 0; j < pColumnsToDisplay.length; j++) {
					String lCellContent=_mListOfRows.get(i).getCell(indexOfColumn(pColumnsToDisplay[j])).asString();
					lRowContent.append(lCellContent+" | ");
				}

				Log.v(TAG, "ROW No" + (i + 1) + ":  " + lRowContent);
			}
		}
	}
	public void setOnNotifyDataSetChangedListener(OnNotifyDataSetChangedListener pListener){
		_mOnNotifyDataSetChangedListener=pListener;
	}


	public void setOnCDTStatusObserver(OnCDTStatusObserver pListener){
		mCdtObserverStack.add(pListener);
	}
	public void removeCDTStatusObserver(OnCDTStatusObserver pListener){
		mCdtObserverStack.remove(pListener);
	}
}
