

package com.hh.clientdatatable;

import android.content.Context;
import com.hh.clientdatatable.ClientDataTable.CDTStatus;
import com.hh.clientdatatable.TCell.ValueType;
import com.hh.listeners.OnCDTColumnListener;

import java.util.ArrayList;

public class TRow {

	private ArrayList<TCell> _mListOfcells;

	/**
	 * Init block
	 */
	{
		_mListOfcells=new ArrayList<TCell>();
	}

	/**
	 * Create an empty row list.
	 */
	public TRow() {

	}

	public TRow(ArrayList<TCell> pCells){
		_mListOfcells=pCells;
	}

	public TRow(Context pContext,CDTStatus pStatus){

	}
	public TRow(Context pContext,CDTStatus pStatus,ArrayList<TColumn> pListofColumn){
				
		for (TColumn column:pListofColumn)
			_mListOfcells.add(new TCell(pContext,column.getValueType(),pStatus,column.getCellType(),column.getName(),column.getCDTColumnListener()));
	}
	/**
	 * Adds a single cell to the end of the row.
	 *
	 * @param cell The cell's value.
	 */
	public void addCell(TCell cell) {
		_mListOfcells.add(cell);
	}
	
	public void addCell(Context pContext, Object value,ValueType pType,TCell.CellType pCellType,String pCellName,CDTStatus pCDTStatus,OnCDTColumnListener pColumnListener) {
		_mListOfcells.add(new TCell(pContext,value, pType,pCellType,pCellName,pCDTStatus,pColumnListener));
	}

	public void addAll(ArrayList<TCell> pCells){
		_mListOfcells=pCells;
	}

	/**
	 * Returns the list of all cell values.
	 *
	 * @return The list of all cell values. The returned list is
	 *     immutable.
	 */
	public ArrayList<TCell> getCells() {
		return _mListOfcells;
	}


	/**
	 * Returns a single cell by its index.
	 *
	 * @param index The index of the cell to get.
	 *
	 * @return A single cell by it's index.
	 */
	public TCell getCell(int index) {
		return _mListOfcells.get(index);
	}

    public TCell cellByName(String pCellName) {
        for (TCell cell:_mListOfcells) {
            if (cell.getName().equalsIgnoreCase(pCellName)) {
                return cell;
            }
        }
        return null;
    }

	TCell setCell(int index, TCell cell) throws IndexOutOfBoundsException {
		return _mListOfcells.set(index, cell);
	}

	/**
	 * Get content of row
	 * @return content of cells separated by '|'
	 */
	public String getContent(){
		
		StringBuffer lContent=new StringBuffer();
		
		int lSizeCells = getCells().size();
		for (int j = 0; j < lSizeCells; j++) {
			lContent.append(getCells().get(j).asString() + " | ");
		}
		return lContent.toString();
	}

	public void memorizeValues(){
		for (TCell cell:_mListOfcells)
			cell.memorizeOldValue();
	}

	public void revertOldValues(){
		for (TCell cell:_mListOfcells)
			cell.revertOldValue();
	}

}
