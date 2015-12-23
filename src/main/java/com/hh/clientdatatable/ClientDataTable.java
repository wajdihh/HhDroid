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
import com.hh.listeners.*;
import com.hh.utility.PuException;
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
	private TCell _mCellHowValueChanged;
	private boolean _mIsExecInDateBase;
	private boolean mIsCdtSorted;
	private Map<String,ClientDataTable> _mNestedJsonArrays;
	private List<String> _mNestedJsonArraysParentKeys;

	private Map<String,ClientDataTable> _mNestedJSONObject;
	private List<String> _mNestedJSONObjectParentKeys;

	private CDTStatusUtils mCDTStatusUtils;

	private OnNotifyDataSetChangedListener _mOnNotifyDataSetChangedListener;

	{
		_mListOfRows = new ArrayList<>();
		_mListOfColumns = new ArrayList<>();
		_mNestedJsonArrays= new HashMap<>();
		_mNestedJsonArraysParentKeys=new ArrayList<>();
		_mNestedJSONObject=new HashMap<>();
		_mNestedJSONObjectParentKeys=new ArrayList<>();
		_mPosition = -1;
		_mTempIteration=-1;
		_mCursorSize = -1;
		mIsCdtSorted=false;
		mCDTStatusUtils=new CDTStatusUtils();
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

	public boolean isConnectedToDB(){
		if(_mSqliteDataBase!=null && !_mTableName.isEmpty() && _mIsExecInDateBase)
			return true;
		return false;
	}

	public Cursor getCursor(){
		return _mCursor;
	}
	public TRow getCurrentRow(){
		return _mListOfRows.get(_mPosition);
	}


	public void append(){
		append(new TRow(_mContext,_mCDTStatus,getListOfColumns()));
	}

	public void append(TRow row){

		mCDTStatusUtils.notifyOnBeforeInsert();

		if(_mCDTStatus==CDTStatus.DEFAULT){
			_mCDTStatus=CDTStatus.INSERT;
			addRow(row);
			_mPosition=_mListOfRows.size()-1;
		}
	}

	public void delete(){

		mCDTStatusUtils.notifyOnBeforeDelete();

		if(_mCDTStatus==CDTStatus.DEFAULT){

			_mCDTStatus=CDTStatus.DELETE;
			if(_mListOfDeletedRows==null)
				_mListOfDeletedRows=new ArrayList<>();
		}

		_mOldRow=new TRow(cloneListOfCells(getCurrentRow().getCells()));
	}


	public void edit(){

		mCDTStatusUtils.notifyOnBeforeEdit();

		if(_mCDTStatus==CDTStatus.DEFAULT){

			_mCDTStatus=CDTStatus.UPDATE;

			getCurrentRow().memorizeValues();

			_mOldRow=new TRow(cloneListOfCells(getCurrentRow().getCells()));
		}
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
		if(_mCDTStatus==CDTStatus.DELETE || _mCDTStatus==CDTStatus.UPDATE)
			if(getRowsCount()==0){
				Log.e("CDT vide","Client data table est vide");
				return;
			}

		// if we have set CDTListener and the data is not valid return
		for (OnCDTStateListener listener:mCDTStatusUtils.mListOfStateListener) {
			if (!listener.onBeforeValidate()) {
				if (pCallback != null) pCallback.onError("");
				return;
			}
		}

		if(pIsExecInDateBase && isConnectedToDB())
			if(_mSqliteDataBase.isOpen())
				commitIntoDataBase(false);
			else
				PuUtils.showMessage(_mContext, "Erreur Data base", "La base de donnees est close");

		commitIntoCDT(pIsUseCDTListener,pIsExecInDateBase);
		_mCDTStatus=CDTStatus.DEFAULT;

		if(_mCellHowValueChanged!=null)
			_mCellHowValueChanged.setValueChanged(false);

		mCDTStatusUtils.notifyOnAfterValidate(pIsExecInDateBase);

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

		mCDTStatusUtils.notifyOnBeforeRevert();

		switch (_mCDTStatus){
			case INSERT:
				_mListOfRows.remove(_mPosition);
				_mCDTStatus=CDTStatus.DEFAULT;
				break;

			case UPDATE:
				getCurrentRow().revertOldValues();
				_mCDTStatus=CDTStatus.DEFAULT;
				break;

			case DELETE:
				if(_mOldRow!=null) {
					_mListOfRows.add(_mOldRow);
					_mOldRow.getCells().clear();
					_mOldRow = null;
				}
				_mCDTStatus=CDTStatus.DEFAULT;
				break;
		}

		mCDTStatusUtils.notifyOnAfterRevert();
		clearListOfDeletedRows();
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

		_mCDTStatus=pCDTStatus;
		_mIsExecInDateBase=true;

		if(_mCDTStatus==CDTStatus.UPDATE &&(_mListOfDeletedRows==null|| _mListOfDeletedRows.isEmpty()))
			if(getRowsCount()==0){
				PuUtils.showMessage(_mContext, "CDT vide", "Client data table est vide");
				return;
			}

		if(isConnectedToDB() && _mSqliteDataBase.isOpen() && (_mCDTStatus==CDTStatus.INSERT || _mCDTStatus==CDTStatus.UPDATE)){
			int size = _mListOfRows.size();
			for (int i = 0; i < size; i++){

				moveToPosition(i);
				commitIntoDataBase(true);

				if(isUseCDTListener)
					if(_mCDTStatus==CDTStatus.INSERT)
						mCDTStatusUtils.notifyOnAfterInsert(true);

					else if(_mCDTStatus==CDTStatus.UPDATE)
						mCDTStatusUtils.notifyOnAfterEdit(_mOldRow,getCurrentRow(),true);
			}


			if (_mListOfDeletedRows!=null && !_mListOfDeletedRows.isEmpty()) {

				size = _mListOfDeletedRows.size();
				for (int i = 0; i < size; i++){
					if( isUseCDTListener)
						mCDTStatusUtils.notifyOnAfterDelete(_mListOfDeletedRows.get(i), true);

					deleteRowDataBase(_mListOfDeletedRows.get(i));
				}

				clearListOfDeletedRows();
			}

			_mCDTStatus=CDTStatus.DEFAULT;

			if (_mCellHowValueChanged!=null) _mCellHowValueChanged.setValueChanged(false);

		}else
			PuUtils.showMessage(_mContext, "Erreur Data base", "La base de donn�es est ferm�");
	}


	private void commitIntoCDT(boolean pIsUseCDTListener,boolean pIsExecuteAction){

		switch (_mCDTStatus) {
			case INSERT:
				// Pas besoin car on l a deja dans l Append()
				//_mListOfRows.add(new TRow(_mContext, _mCDTStatus, getColumnsCount()));

				if(pIsUseCDTListener) mCDTStatusUtils.notifyOnAfterInsert(pIsExecuteAction);
				break;
			case DELETE:
				if(pIsUseCDTListener) mCDTStatusUtils.notifyOnAfterDelete(getCurrentRow(), pIsExecuteAction);
				_mListOfDeletedRows.add(getCurrentRow());
				_mListOfRows.remove(_mPosition);
				if (_mPosition == getRowsCount()){
					_mPosition--;
				}

				_mTempIteration=-1;
				break;

			case UPDATE:

				if(pIsUseCDTListener && isValuesChanged()) mCDTStatusUtils.notifyOnAfterEdit(_mOldRow, getCurrentRow(), pIsExecuteAction);
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

		switch (_mCDTStatus) {
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
				PuUtils.showMessage(_mContext, "No commit", "Acun changement n'est applique car le mode est DEFAULT");
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
					PuUtils.showMessage(_mContext, "Erreur delete", "Valeur vide pour la clé de suppression :"+_mWhereClauseColumns[i]);
			}

			_mSqliteDataBase.delete(_mTableName, _mWhereClause.toString(), lArgs);
		}else
			PuUtils.showMessage(_mContext, "Erreur delete", "Clause where non definit");
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
		if(_mTempIteration==-1){
			_mTempIteration=_mPosition;
			_mPosition=-1;
		}

		return moveToPosition(_mPosition + 1);
	}
	/**
	 * Move to next row
	 */
	public boolean moveToNext() {
		return moveToPosition(_mPosition + 1);
	}

	/**
	 * Move to the previous row.
	 */
	public boolean moveToPrevious() {
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

		final int count = getRowsCount();
		if (pIndex >= count) {
			_mPosition = count;

			if(_mTempIteration!=-1){
				_mPosition=_mTempIteration;
				_mTempIteration=-1;
			}
			return false;
		}
		if (pIndex < 0) {
			_mPosition = -1;
			return false;
		}

		if (pIndex == _mPosition) {
			return true;
		}

		_mPosition = pIndex;
		return true;
	}

	/**
	 * Sort the client data table with priority of columns passed in parametres
	 *
	 * @param pListOfSortedColumnsNames
	 * @param TRowSortOrder
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

		// TODO a optimiser a faire getCurrentRow.getCells(posCell)
		if(getRowsCount()==0) {
			PuUtils.showMessage(_mContext, "CDT vide", "Client data table est vide");
			return new TCell();
		}

		TCell lResult;
		if(isConnectedToDB())
			lResult=new TCell(_mContext,ValueType.TEXT, _mCDTStatus,pCellName);
		else
			lResult=new TCell();

		int lColumnSize = getColumnsCount();
		boolean lIsCellFound=false;
		for (int i = 0; i < lColumnSize && getRowsCount() != 0; i++) {
			if (_mListOfColumns.get(i).getName().equalsIgnoreCase(pCellName)) {
				lResult = getCell(_mPosition, i);
				lResult.setCDTStatus(_mCDTStatus);
				lResult.setValueType(_mListOfColumns.get(i).getValueType());
				lResult.setOnCDTColumnListener(_mListOfColumns.get(i).getCDTColumnListener());
				lIsCellFound=true;
				break;
			}

		}
		if(!lIsCellFound)
			PuUtils.showMessage(_mContext, "Wrong cell Name","There no cellName called :"+pCellName);

		return lResult;
	}

	public TCell getPrimaryKeyCell() {

		if(getRowsCount()==0) {
			PuUtils.showMessage(_mContext, "CDT vide", "Client data table est vide");
			return new TCell();
		}

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

		_mCursor= _mSqliteDataBase.rawQuery(pSQLCommand, null);

		try {
			_mCursor.moveToFirst();
			fillFromCursor(_mCursor);
		} finally {

		}
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
								lRow.addCell(_mContext,pCursor.getString(j),lColumn.getValueType(),lColumn.getCellType(),lColumn.getName(),_mCDTStatus,lColumn.getCDTColumnListener());
								lIsColumnFound=true;
								break;
							}
						}
						if(!lIsColumnFound) {
							assert lColumn != null;
							lRow.addCell(_mContext,_mRes.getString(R.string.cst_notInCursor),lColumn.getValueType(),lColumn.getCellType(),lColumn.getName(),_mCDTStatus,lColumn.getCDTColumnListener());
						}
					}
				} else {
					for (int i = 0; i < lNbrColumnsCursor; i++) {
						String lColumnName = lCursorColumnsNames[i];
						if (lColumnName != null && !lColumnName.equals(""))
							lRow.addCell(_mContext,pCursor.getString(i), ValueType.TEXT, TCell.CellType.NONE,lColumnName,_mCDTStatus,null);
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
	}

	/**
	 * Clear ClientDataTable Content And table
	 */
	public void clearAndExecute() {
		_mCursorSize = -1;
		_mListOfRows.clear();

		_mIsExecInDateBase=true;
		if(isConnectedToDB()){
			_mSqliteDataBase.delete(_mTableName,null,null);
			_mIsExecInDateBase=false;
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

	public TColumn addColumn(String pName, ValueType pType,TColumn.ColumnType pColumnType,OnCDTColumnListener pListener) {

		TColumn column=new TColumn(pName, pType,pColumnType,pListener);
		_mListOfColumns.add(column);
		return column;
	}


	public TColumn addColumn(String pName, ValueType pType,OnCDTColumnListener pListener) {

		TColumn column=new TColumn(pName, pType, TCell.CellType.NONE,pListener);
		_mListOfColumns.add(column);
		return column;
	}
	public TColumn addColumn(String pName, ValueType pType,TCell.CellType pCellType) {

		TColumn column=new TColumn(pName, pType,pCellType,null);
		_mListOfColumns.add(column);
		return column;
	}
	public TColumn addColumn(String pName, ValueType pType,TCell.CellType pCellType,OnCDTColumnListener pListener) {

		TColumn column=new TColumn(pName, pType,pCellType,pListener);
		_mListOfColumns.add(column);
		return column;
	}
	/**
	 * allows to add a Row
	 *
	 * @param pColumn
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
				lRow.addCell(_mContext,values[i], lCol.getValueType(),lCol.getCellType(),lCol.getName(),_mCDTStatus,lCol.getCDTColumnListener());
			else
				lRow.addCell(_mContext,values[i], lCol.getValueType(),lCol.getCellType(),lCol.getName(),_mCDTStatus,lCol.getCDTColumnListener());
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
			list.add(row.cellByName(pFieldName).asString());
		}
		return list;
	}
	/**
	 * Return if value existe
	 * @param pFieldName
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
	 * @throws PuException
	 * @throws IndexOutOfBoundsException
	 */
	public void setCell(int pRowIndex, int pColumnIndex, TCell pCell)
			throws PuException, IndexOutOfBoundsException {

		TRow row = _mListOfRows.get(pRowIndex);

		if (!row.getCell(pColumnIndex).getValueType()
				.equals(pCell.getValueType())) {
			throw new PuException(_mContext,
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

		boolean isChanged=false;
		for (TColumn column:_mListOfColumns){
			for (TRow row :_mListOfRows){
				TCell cell=row.cellByName(column.getName());
				if(cell.isValueChanged()){
					isChanged=true;
					_mCellHowValueChanged=cell;
					break;
				}
			}
			if(isChanged)
				break;
		}
		return isChanged;
	}
	/**
	 * Returns a new data table, with the same data and metadata as this one.
	 * Any change to the returned table should not change this table and vice
	 * versa. This is a deep clone.
	 *
	 * @return The cloned data table.
	 */
	public ClientDataTable clone() {

		ClientDataTable lResult = new ClientDataTable(_mContext);

		for (TColumn iTColumn : _mListOfColumns) {
			lResult.addColumn(iTColumn);
		}

		for (TRow iTRow : _mListOfRows) {
			lResult.addRow(iTRow);
		}

		return lResult;
	}



	public void addJSONArray(String key,String parentKey,ClientDataTable arrays){

		_mNestedJsonArrays.put(key, arrays);
		_mNestedJsonArraysParentKeys.add(parentKey);
	}

	public void addNestedJSONObject(String key,String parentKey,ClientDataTable jsonObjectCDT){
		_mNestedJSONObject.put(key, jsonObjectCDT);
		_mNestedJSONObjectParentKeys.add(parentKey);
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
					map.put("MAIN",mainJsonObject);
					fillJsonField(row,column,map.get("MAIN"), jsonObjectGeneratedMode);
				}

			}
		}

		// Json Arrays
		if(!_mNestedJsonArrays.isEmpty()){

			int i=0;
			for (Map.Entry<String,ClientDataTable> entry : _mNestedJsonArrays.entrySet())
			{

				if(entry.getValue()==null)
					continue;
				String parentKey=_mNestedJsonArraysParentKeys.get(i);
				JSONArray subArrays=entry.getValue().toJSONArray();
				JSONObject jsonParent=map.get(parentKey);

				jsonParent.put(entry.getKey(),subArrays);

				i++;
			}
		}

		// JSon Object
		if(!_mNestedJSONObject.isEmpty()){

			int i=0;
			for (Map.Entry<String,ClientDataTable> entry : _mNestedJSONObject.entrySet())
			{

				if(entry.getValue()==null)
					continue;
				String parentKey=_mNestedJSONObjectParentKeys.get(i);
				JSONObject subJSONObject=entry.getValue().toJSONObject();
				JSONObject jsonParent=map.get(parentKey);

				jsonParent.put(entry.getKey(),subJSONObject);

				i++;
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
			jsonObject.put(column.getName(),cell.asInteger());
		else if(cell.getValueType()==ValueType.DOUBLE)
			jsonObject.put(column.getName(),cell.asDouble());
		else if(cell.getValueType()==ValueType.DATETIME)
			jsonObject.put(column.getName(),hasFormatedDate?cell.asDateString():cell.asDateTime());
		else
			jsonObject.put(column.getName(), cell.asValue());
	}
	/**
	 *
	 * {@linkplain displayContent}
	 * to display client data table content in LogCat
	 * @param pColumnsToDisplay : List of columns to display
	 * <strong>if we put * we will display all columns content</strong>
	 */
	public void displayContent(String ...pColumnsToDisplay) {

		if(pColumnsToDisplay!=null && pColumnsToDisplay.length!=0){

			StringBuffer lColumns = new StringBuffer();
			int lSize = _mListOfRows.size();
			if(pColumnsToDisplay.length==1 && pColumnsToDisplay[0].equals("*")){

				for (int i = 0; i < _mListOfColumns.size(); i++)
					lColumns.append(_mListOfColumns.get(i).getName() + " | ");
				// Display columns names
				Log.d(TAG, "COLUMNS:  " + lColumns.toString() + "\n");

				// Display rows
				for (int i = 0; i < lSize; i++)
					Log.v(TAG, "ROW N�" + (i + 1) + ":  " + _mListOfRows.get(i).getContent());

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
			//TODO chaines dans les resources
		}else{
			PuUtils.showMessage(_mContext, "Erreur DisplayContent", "il faut que la liste != null ou elle contien au moins 1 element");
		}
	}
	public void setOnNotifyDataSetChangedListener(OnNotifyDataSetChangedListener pListener){
		_mOnNotifyDataSetChangedListener=pListener;
	}


	public void setOnCDTStateListener(OnCDTStateListener pListener){
		mCDTStatusUtils.mListOfStateListener.add(pListener);
	}
	public void removeCDTStateListener(OnCDTStateListener pListener){
		mCDTStatusUtils.mListOfStateListener.remove(pListener);
	}
}
