/*
 * Created by IntelliJ IDEA.
 * User: kguisti
 * Date: Nov 13, 2001
 * Time: 10:06:16 AM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.powerdba.util;



public final class NumberUtility {

    static public String getKbytes(long in) {

        return String.valueOf(in/1000) + "K";

    }

    static public String getKbytes(String in) {

        return ( new Long(in).longValue()/1000 ) + "K";
    }

}
