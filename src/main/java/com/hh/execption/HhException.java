package com.hh.execption;

import android.content.Context;


public class HhException extends Exception{



	private static final long serialVersionUID = 1L;

	public HhException(){};
	public HhException(String pMessage){
		super(pMessage);
	}
	public HhException(Context pContext, int pMessageRes){
		super(pContext.getResources().getString(pMessageRes));
	}

	public HhException(String pTitre, String pMessage){
		super(pTitre+" : "+pMessage);
	}

	public static String getExceptionMessage(Exception e){

		if(e.getMessage()!=null)
			return e.getMessage();
		return e.toString();
	}

}
