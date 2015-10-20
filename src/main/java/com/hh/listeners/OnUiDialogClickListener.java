package com.hh.listeners;

import android.app.Dialog;
import android.app.DialogFragment;
import android.view.View;
import android.view.View.OnClickListener;

public class OnUiDialogClickListener implements OnClickListener{

	private Dialog _mDialog;
	private DialogFragment _mDialogFrg;
	public void setDialog(Dialog pDialog){
		_mDialog=pDialog;
	}
	public void setDialogFragment(DialogFragment pDialogFragment){
        _mDialogFrg=pDialogFragment;
	}
	@Override
	public void onClick(View v) {
		if(_mDialog!=null)
			_mDialog.dismiss();

        if(_mDialogFrg!=null)
            _mDialogFrg.dismiss();
	}

}
