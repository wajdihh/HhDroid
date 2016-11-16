package com.hh.utility;

import com.hh.execption.HhException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

	public enum LANG{FR,EN}
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
	 * Format : Samedi dd/MM/yyyy HH:mm
	 */
	public static final String FORMAT_DATETIME_DAY_HOUR_FR = "EEEE dd/MM/yyyy HH:mm";


	public static final String FORMAT_DATETIME_DAY_HOUR_EN = "EEEE MM/dd/yyyy HH:mm";

	/**
	 * Format : Samedi dd/MM/yyyy
	 */
	public static final String FORMAT_DATE_DAY_HOUR_FR = "EEEE dd/MM/yyyy";

	public static final String FORMAT_DATE_DAY_HOUR_EN = "EEEE MM/dd/yyyy";
	/**
	 * Format : MM/dd/yyyy HH:mm
	 */
	public static final String FORMAT_DATETIME_HOUR_EN = "MM/dd/yyyy HH:mm";

	public static final String FORMAT_TIME = "HH:mm";


	private SimpleDateFormat mSimpleDateFormat;
	private String[] formats;

	private static final String[] formatsFr = {FORMAT_DATE_FR,FORMAT_DATETIME_FR,FORMAT_DATETIME_DAY_HOUR_FR,FORMAT_DATE_DAY_HOUR_FR ,FORMAT_DATETIME_HOUR_FR};
	private static final String[] formatsEn = {FORMAT_DATE_EN,FORMAT_DATETIME_EN,FORMAT_DATETIME_DAY_HOUR_EN,FORMAT_DATE_DAY_HOUR_EN ,FORMAT_DATETIME_HOUR_EN};

	public PuDate(String pSelectedPattern,LANG pLang){
		mSimpleDateFormat=new SimpleDateFormat(pSelectedPattern,getLocal());
		if(pLang==LANG.FR)
			formats=formatsFr;
		else formats=formatsEn;
	}

	public  Locale getLocal(){
		return Locale.getDefault();
	}

	/**
	 * Static
	 */

	public long parse(String pDateString) throws HhException {
		long value=-1;
		if (pDateString != null) {

			//Parse date by setted Patern
			try {
				value=mSimpleDateFormat.parse(pDateString).getTime();
			} catch (ParseException e) {

				// IF patern is different to default pattern
				int index=0;
				for (String pattern : formats) {
					index++;
					try {
						value = new SimpleDateFormat(pattern).parse(pDateString).getTime();
						break;
					}catch (ParseException e1){
						if(index==formats.length)
							throw new HhException("Unable To find your used formatter pattern");
					}
				}
			}
		}
		return value;
	}

	public String format(long date,String pDateFormat) {
		return  new SimpleDateFormat(pDateFormat, getLocal()).format(date);
	}
	public String format(long pDateTime){
		return mSimpleDateFormat.format(pDateTime);
	}

}
