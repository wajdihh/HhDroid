package com.hh.adapters;

import android.content.Context;

import com.hh.clientdatatable.ClientDataTable;

/**
 * Created by benhadjahameda on 08/01/2015.
 *
 * Default recycleView Adapter, To use when we don't  need to override methods
 */

public class CDTDefaultRecycleAdapter extends  CDTRecycleAdapter{
    public CDTDefaultRecycleAdapter(Context pContext, int pLayoutRow, ClientDataTable pCDT) {
        super(pContext, pLayoutRow, pCDT);
    }
}
