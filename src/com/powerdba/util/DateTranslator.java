/*
 * Created by IntelliJ IDEA.
 * User: kguisti
 * Date: Nov 2, 2001
 * Time: 11:50:09 AM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.powerdba.util;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class DateTranslator {

    public static final String STANDARD_DATE_FORMAT     = "dd-MM-yyyy";
    public static final String STANDARD_DATETIME_FORMAT = "dd-MM-yyyy hh:mm:ss";
    public static final String BASIC_DATETIME_FORMAT = "HH:mm:ss dd-MMM-yyyy EE"; 
    public static final String BASIC_DATETIME_FORMAT2 = "EE dd-MMM-yyyy HH:mm";
    public static final String ORACLE_DATETIME_FORMAT = "dd-MM-yyyy hh:mma";
    public static final String HISTORY_DATETIME = "dd-MMM-yyyy HH:mm";
    public static final String HISTORY_DATEONLY = "dd-MMM-yyyy";
    public static final String HISTORY_HRMIN_ONLY = "HH:mm";
    public static final String CHART_MINUTE = "MMdd HH:mm";
    public static final String CHART_SECOND = "MMdd HH:mm:ss";
    public static final String CHART_MINUTE_NODATE = "HH:mm";
    public static final String SECONDS = "ss";
    public static final String MINUTES = "mm";
    public static final String HOURS   = "HH";
    public static final String AMPM    = "a";

    static public Date getCurrentTime () {
        return new Date(System.currentTimeMillis());
    }
    
    static public String getSeconds(Date date) {
      SimpleDateFormat sdf = new SimpleDateFormat(SECONDS);
      return sdf.format(date);      
    }
    
    static public String getMinutes(Date date) {
      SimpleDateFormat sdf = new SimpleDateFormat(MINUTES);
      return sdf.format(date);      
    }
    
    static public String getHours(Date date) {
      SimpleDateFormat sdf = new SimpleDateFormat(HOURS);
      return sdf.format(date);      
    }
    
    static public String getAmPm(Date date) {
      SimpleDateFormat sdf = new SimpleDateFormat(AMPM);
      return sdf.format(date);      
    }
    
    static public long getDateTime(Date date) {

        // This method returns the number of milliseconds since the epoch up until the date & time
        // of the date passed in
        return date.getTime();
    }

    static public long getDate(Date date) {

        Tracer.log("Translating java.util.Date " + date.toString() + " to a long representing midnight of this date",
                    Tracer.METHOD,"static");

        // This method returns the number of milliseconds since the epoch up until midnight of the
        // date passed in.
        SimpleDateFormat sdf = new SimpleDateFormat(STANDARD_DATE_FORMAT);
        SimpleDateFormat sdf2 = new SimpleDateFormat(STANDARD_DATETIME_FORMAT);

        String midnightString = sdf.format(date) + " 00:00:00 am";

        ParsePosition pos = new ParsePosition(0);
        Date midnightDate = sdf2.parse(midnightString, pos);

        return midnightDate.getTime();
    }

    static public String getDow(long date) {

        SimpleDateFormat sdf = new SimpleDateFormat("EE");
        return sdf.format(new Date(date));
    }

    static public String getStringDate(long date) {

        SimpleDateFormat sdf = new SimpleDateFormat(STANDARD_DATE_FORMAT);
        if ( date == -1 ) {
            return "";
        } else {
            return sdf.format(new Date(date));
        }
    }
    
    static public String getStringDate(Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    static public String getStringDate(Date date) {
        return DateTranslator.getStringDate(DateTranslator.getDate(date));
    }

    static public String getStringTime(long date) {
        return getStringHrs(date) + ":" + getStringMins(date) + " " + getStringAm(date);
    }

    static public String getStringDateTime(long date) {
        SimpleDateFormat sdf = new SimpleDateFormat(BASIC_DATETIME_FORMAT);
        return sdf.format(new Date(date));
    }
    
    static public String getStringTime(Date date) {
    	SimpleDateFormat sdf = new SimpleDateFormat(HISTORY_HRMIN_ONLY);
      return sdf.format(date);
    }

    static public String getStringDateTime(Date date) {
        return DateTranslator.getStringDateTime(DateTranslator.getDateTime(date));
    }

    static public long getTime(Date date) {

        // This method returns the number of milliseconds since midnight...
        return getDateTime(date) - getDate(date);
    }

    static public long getLongDate(String dateString) {


        SimpleDateFormat sdf = new SimpleDateFormat(STANDARD_DATETIME_FORMAT);
        Tracer.log("Translating String Date " + dateString + " to a long value.", Tracer.METHOD, "DateTranslator");
        return sdf.parse(dateString + " 00:00:00 am", new ParsePosition(0)).getTime();
    }

    static public long getLongDateTime(String dateTimeString, String dateFormat) {

        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        Tracer.log("Converting string date: " + dateTimeString, Tracer.METHOD, "RulesetDAO");

        if ( dateTimeString.equals("") ) {
            return 0;
        } else {
            return sdf.parse(dateTimeString, new ParsePosition(0)).getTime();
        }
    }
    
    static public Date getDateDateTime(String dateTimeString, String dateFormat) {
      SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
      return sdf.parse(dateTimeString, new ParsePosition(0));
    }

    static public long getLongTime(String timeString) {

        String x = "01/01/2000 00:00:00 am";
        String y = "01/01/2000 " + timeString;

        SimpleDateFormat sdf = new SimpleDateFormat(STANDARD_DATETIME_FORMAT);

        return sdf.parse(y, new ParsePosition(0)).getTime() -
               sdf.parse(x, new ParsePosition(0)).getTime();

    }

    static public String getStringHrs(long time) {
    
        Tracer.log("incoming value: " + time, Tracer.DEBUG, "HtmlComponent.getTimeHtml");

        
        SimpleDateFormat sdf = new SimpleDateFormat("hh");
        
        Date d = new Date(time);
        
        Tracer.log("Returning " + sdf.format(d), Tracer.DEBUG, "getStringHrs");

        return sdf.format(d);

    }

    static public String getStringMins(long time) {

        SimpleDateFormat sdf = new SimpleDateFormat("MM");
        
        Date d = new Date(time);
        
        Tracer.log("Returning " + sdf.format(d), Tracer.DEBUG, "getStringMins");

        return sdf.format(d);

    }

    static public String getStringAm(long time) {

        // This method returns the number of milliseconds since the epoch up until midnight of the
        // date passed in.


        SimpleDateFormat sdf = new SimpleDateFormat("a");
        
        Date d = new Date(time);
        
        Tracer.log("Returning " + sdf.format(d), Tracer.DEBUG, "getStringAm");

        return sdf.format(d);

    }

    static public String getFormattedCurrentTime() {
        return DateTranslator.getStringDateTime(System.currentTimeMillis());
    }

    public static void main(String[] args) {

        String d = "03/15/2002";

        System.out.println(DateTranslator.getLongDate(d));

        System.exit(0);
    }


    //static public String getStringMins(Long time) {}

    //static public String getStringAm(Long time) {}






}
