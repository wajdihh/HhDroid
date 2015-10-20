package com.hh.utility;

import android.content.Context;


public class PuException extends Exception{


	private static final long serialVersionUID = 1L;

	private int code;
	private String message;



	@Override
	public String getMessage() {
		return message;
	}

	public PuException(String pMessage){
		super(pMessage);
		this.message=pMessage;
	}
	public PuException(Context pContext,String pMessage){
		super(pMessage);

		PuUtils.showMessage(pContext, "Exception", pMessage);
	}
	public PuException(Context pContext,String pTitre,String pMessage){
		super(pMessage);

		PuUtils.showMessage(pContext,pTitre, pMessage);
	}

	public static String getExceptionMessage(Exception e){

		if(e.getMessage()!=null)
			return e.getMessage();
		return e.toString();
	}

	@Override
	public void printStackTrace() {
		super.printStackTrace();
	}
}
