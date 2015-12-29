package com.hh.utility;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


/**
 * Date format
 * YYYY-MM-DD
 * YYYY-MM-DD HH:MM
 * YYYY-MM-DD HH:MM:SS
 * YYYY-MM-DD HH:MM:SS.SSS
 * HH:MM
 * HH:MM:SS
 * HH:MM:SS.SSS
 * @author wajdihh
 *
 */

public class PuDate{

	/**
	 * Format : dd/MM/yyyy
	 */
	public static final String FORMAT_DATE_FR = "dd/MM/yyyy";
	/**
	 * Format : MM/dd/yyyy
	 */
	public static final String FORMAT_DATE_EN = "MM/dd/yyyy";
	/**
	 * Format : dd/MM/yyyy HH:mm:ss
	 */
	public static final String FORMAT_DATETIME_FR = "dd/MM/yyyy HH:mm:ss";
	/**
	 * Format : MM/dd/yyyy HH:mm:ss
	 */
	public static final String FORMAT_DATETIME_EN = "MM/dd/yyyy HH:mm:ss";

    /**
     * Format : dd/MM/yyyy HH:mm
     */
    public static final String FORMAT_DATETIME_HOUR_FR = "dd/MM/yyyy HH:mm";
    /**
     * Format : MM/dd/yyyy HH:mm
     */
    public static final String FORMAT_DATETIME_HOUR_EN = "MM/dd/yyyy HH:mm";

    public static final String FORMAT_TIME = "HH:mm";

	private String _mDateStringFormat;
	private static SimpleDateFormat _mDateFormater;

	{
		_mDateStringFormat="yyyy-MM-dd";
	}

	public int month;
	public int day;
	public int year;
	public int hour;
	public int minute;
	public int second;
    public Date date;

	public PuDate(String dateFormat){
		_mDateStringFormat=dateFormat;
		_mDateFormater=new SimpleDateFormat(dateFormat,getLocal());
	}

	public PuDate(int month, int day, int year,String dateFormat) {
        this(dateFormat);
		this.month = month;
		this.day = day;
		this.year = year;
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH,month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        date = cal.getTime();
	}

	public PuDate(int month, int day, int year, int hour, int minute,int second,String dateFormat) {
		this(dateFormat);
		this.month = month;
		this.day = day;
		this.year = year;
		this.hour = hour;
		this.minute = minute;
		this.second = second;
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH,month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);
        date = cal.getTime();
	}
    public PuDate(Date pDate,String dateFormat){
        this(dateFormat);
        date=pDate;
        Calendar cal = Calendar.getInstance();
        cal.setTime(pDate);
        this.month = cal.get(Calendar.MONTH);
        this.day = cal.get(Calendar.DAY_OF_MONTH);
        this.year = cal.get(Calendar.YEAR);
        this.hour = cal.get(Calendar.HOUR_OF_DAY);
        this.minute = cal.get(Calendar.MINUTE);
        this.second = cal.get(Calendar.SECOND);
    }
	public Locale getLocal(){
		return Locale.getDefault();
	}

	public String getDateStringFormat() {
		return _mDateStringFormat;
	}

	public void setDateStringFormat(String _mDateStringFormat) {
		this._mDateStringFormat = _mDateStringFormat;
		_mDateFormater.applyPattern(_mDateStringFormat);
	}

	public SimpleDateFormat getDateFormater() {
		return _mDateFormater;
	}

	public void setDateFormater(SimpleDateFormat _mDateFormater) {
		this._mDateFormater = _mDateFormater;
	}
	
	/**
	 * Static
	 */
	
	public static long getTimeFromStringDate(String pDateString){
		long lTime=-1;
		if(pDateString==null || pDateString.isEmpty())
			return -1;
		try {
			lTime= _mDateFormater.parse(pDateString).getTime();
		} catch (ParseException e) {
			Log.e("pUDate.getTimeFromStringDate", "Parse date incorrect");
			lTime=-1;
		}
		return lTime;
	}

    @Override
    public String toString() {
        return _mDateFormater.format(date);
    }

    public String defaultValue() {
        return _mDateFormater.format(date);
    }
    public String value(String pDateFormat) {
        setDateStringFormat(pDateFormat);
        return _mDateFormater.format(date);
    }
	public String getFormattingDate(Date date,String pDateFormat) {
		return  new SimpleDateFormat(pDateFormat, getLocal()).format(date);
	}
	public Date getDate(String date, String pDateFormat) throws ParseException {
		return  new SimpleDateFormat(pDateFormat, getLocal()).parse(date);
	}
    public static String getStringFromDate(long pDateTime){
		return _mDateFormater.format(pDateTime);
	}
}
