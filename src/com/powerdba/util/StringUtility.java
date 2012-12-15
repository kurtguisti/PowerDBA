/*
 * Created by IntelliJ IDEA.
 * User: kguisti
 * Date: Nov 13, 2001
 * Time: 10:06:16 AM
 * To change template for new class use 
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.powerdba.util;

import java.util.Enumeration;
import java.util.StringTokenizer;

public final class StringUtility {

    public static int IP4 = 4;
    public static int IP6 = 6;
    
    public static String initCap(String str) {
      if ( str == null ) {
        return " ";
      } else {
        return str.substring(0,1).toUpperCase() + str.substring(1).toLowerCase();
      }
    }

    public static String removeString(String str, String removeStr) {

        Enumeration e = new StringTokenizer(str,removeStr);
        StringBuffer sb = new StringBuffer();

        while ( e.hasMoreElements() ) {
            sb.append((String) e.nextElement());
        }

        return sb.toString();
    }
    
    public static String encodeXml(String str) {
    	String rval = null;
    	rval = StringUtility.replace(str, "<", "&LT;");
    	rval = StringUtility.replace(rval, ">", "&GT;");
    	return rval;
    }

    public static String replace(String str, String pattern, String replace) {
        int s = 0;
        int e = 0;
        StringBuffer result = new StringBuffer();

        if(pattern == null || pattern.equals("")) return str;

        while ((e = str.indexOf(pattern, s)) >= 0) {
            result.append(str.substring(s, e));
            result.append(replace);
            s = e + pattern.length();
        }
        result.append(str.substring(s));

        return result.toString();
    }

    public static String pad(String str, int toLength) {

        if ( str.length() >= toLength ) return str;

        StringBuffer sb = new StringBuffer();

        for ( int i=0; i<(toLength-str.length()); i++ ) {
            sb.append(" ");
        }
        return str + sb.toString();
    }

    public static String replicate(String str, int count) {

        StringBuffer sb = new StringBuffer();

        for ( int i=0; i<count; i++ ) {
            sb.append(str);
        }

        return sb.toString();
    }


}
