/*
 * Created by IntelliJ IDEA.
 * User: kguisti
 * Date: Nov 8, 2001
 * Time: 10:32:13 AM
 * To change template for new class use 
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.powerdba.mvc.jsp;
import java.util.Comparator;

public class SelectEntry implements Comparable {

    private String value;
    private String display;
    private short order;

    public SelectEntry( String value, String display) {

        this.value = value;
        this.display = display;
    }

    public SelectEntry( String value, String display, short order) {

        this.value = value;
        this.display = display;
        this.order = order;
    }

    public String getValue() {
        return value;
    }

    public String getDisplay() {
        return display;
    }

    public short getOrder() {
        return order;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public void setOrder(short order) {
        this.order = order;
    }

    public int compareTo(Object obj) {
      SelectEntry tmp = (SelectEntry) obj;

      if ( this.order < tmp.getOrder() ) {
        return -1;
      } else if ( this.order > tmp.getOrder() ) {
        return 1;
      }

      return 0;
    }

    public boolean equals(Object obj) {
      if ( obj instanceof SelectEntry ) {
          SelectEntry se = (SelectEntry) obj;
          return toString().equals(se.toString());
      }
      return false;
    }

    public String toString() {
        return " \nSelectEntry: \n Value " + value + " Display " + display + " Order " + order;
    }
    
    // Comparator to sort on the display key in ascending order.  Not case sensitive.
    static public final Comparator CASE_INSENSITIVE_DISPLAY_ORDER = new Comparator() {
      public int compare(Object o1, Object o2) {
          SelectEntry r1 = (SelectEntry) o1;
          SelectEntry r2 = (SelectEntry) o2;
          return -r2.getDisplay().toLowerCase().compareTo(r1.getDisplay().toLowerCase());
      }
    };



}
