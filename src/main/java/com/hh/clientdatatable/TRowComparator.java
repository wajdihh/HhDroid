package com.hh.clientdatatable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

final class TRowComparator implements Comparator<TRow>{

	private static TRowComparator INSTANCE;

	private TRowComparator() {}
	
	static TRowComparator getInstance(HashMap<String, ClientDataTable.SortType> pListOfSortedField) {
		if (INSTANCE == null)
			INSTANCE = new TRowComparator();
		
		INSTANCE._myListOfSortedField = pListOfSortedField;
		INSTANCE._myListOfSortedFieldName = new ArrayList<String>(pListOfSortedField.keySet());
		
		return INSTANCE;
	}


	private HashMap<String, ClientDataTable.SortType> _myListOfSortedField;
	private ArrayList<String> _myListOfSortedFieldName;

	/**
	 * Fonction prédéfinie pr comparer deux objets 
	 * retour : 0 : egaux , 1 objet1 > objet2 et -1 objet1 <objet2
	 */
	@Override
	public int compare(TRow pRow1, TRow pRow2) {
		int lCompare = 0;
		// Si les valeurs de deux champs sont égaux , on passe au critère  suivant de compraison depuis la liste
		for (String lFieldName : _myListOfSortedFieldName) {
			// Mettre à jour les Champs de la ligne 1 et 2
			TCell lFieldRow1 = pRow1.cellByName(lFieldName);
            TCell lFieldRow2 = pRow2.cellByName(lFieldName);
            switch (lFieldRow1.getValueType()) {
			case INTEGER: // Dans le cas ou le champs est de nature 'entier'
				lCompare = lFieldRow1.asInteger() - lFieldRow2.asInteger();
				break;
			case DOUBLE: // Dans le cas ou le champs est de nature 'double'
				lCompare = Double.compare(lFieldRow1.asDouble(), lFieldRow2.asDouble());
				break;
			case DATETIME: // Dans le cas ou le champs est de nature 'Date'
				Date lDate1 = new Date(lFieldRow1.asDateTime());
				Date lDate2 = new Date(lFieldRow2.asDateTime());

				lCompare = lDate1.compareTo(lDate2);
				break;
			default : //  Dans le cas : String et Other
				String lFieldRow2Value = lFieldRow2.asString();
				if (lFieldRow2Value != null)
					lCompare = lFieldRow1.asString().compareTo(lFieldRow2Value);
				break;
			}
			if (lCompare != 0)
				return _myListOfSortedField.get(lFieldName) == ClientDataTable.SortType.DESC ? -lCompare : lCompare;
		}	
		return lCompare;
	}

}