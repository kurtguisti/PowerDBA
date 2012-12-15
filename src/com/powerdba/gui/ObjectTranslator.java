
package com.powerdba.gui;

import com.powerdba.mvc.jsp.HtmlComponent;
import com.powerdba.util.*;

import java.io.BufferedReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Date;
import java.text.DecimalFormat;
import oracle.sql.*;


public class ObjectTranslator {

  private static final String OBJECT_NAME = "ObjectTranslator";
  
  static public String getString(String className, long precision, Object value) throws SQLException {
    Column c = new Column();
    c.setClassName(className);
    c.setColumnLength(precision);
    return getString(c, value);
  }

  static public String getString(Column column, Object value) throws SQLException {
  
    Tracer.log("=============== Translating object " + column.getClassName(), Tracer.ERROR, OBJECT_NAME);
  
    String rval = null;
    
    // Translate it into a String
    if ( value != null ) {
        
      //Tracer.log("column name: " + column.toString(), Tracer.DEBUG, "");
      //Tracer.log("width:" + column.getColumnLength(), Tracer.DEBUG, "");

      if ( column.getColumnName()!= null && column.getColumnName().toLowerCase().equals("command") ) {
         rval = HtmlComponent.getTextArea("sql", value.toString(),130,4);          
      }
      else if ( column.getClassName().equals("java.lang.String") ) {
        if ( column.getColumnLength() > 200000000 ) {  // This is how a Long column is indicated via JDBC
          rval = HtmlComponent.getTextArea("sql",value.toString(),130,4);          
        } else {
          rval = (String) value;
        }
      }
      else if ( column.getClassName().equals("java.math.BigDecimal") ) {
        rval = value.toString();
      }
      else if ( column.getClassName().equals("java.sql.Timestamp") || column.getClassName().equals("oracle.sql.TIMESTAMP") ) {
        rval = ((Date) value).toString();
      }
      else if ( column.getClassName().equals("java.sql.CLOB") ) {
      	System.out.println("Got a CLOB.........");
      	oracle.sql.CLOB clob = (oracle.sql.CLOB) value;
        try {
        	rval = CLOBToString(clob);
					//rval = HtmlComponent.getTextArea("sql",CLOBToString(clob),130,4);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
      }
      else if ( column.getClassName().equals("oracle.sql.CLOB") ) {
      	Tracer.log("Got a CLOB.........",Tracer.DEBUG,"ObjectTranslator");
      	oracle.sql.CLOB clob = (oracle.sql.CLOB) value;
        try {
        	rval = CLOBToString(clob);
					//rval = HtmlComponent.getTextArea("sql",CLOBToString(clob),130,4);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
      }
      
    }

    return rval;

  }
  
	private static String CLOBToString(CLOB cl) throws Exception {

		StringBuffer strOut = new StringBuffer();
		String line;

		BufferedReader br = new BufferedReader(cl.getCharacterStream());
		while ((line=br.readLine())!=null) {
		  strOut.append(line);
		}
		return strOut.toString();

	}
  
  static public String getStringNew(Column column, Hashtable row) throws SQLException {
  
    // Get the object out of the table cell
    Object value = (Object) row.get(column.getColumnName().toLowerCase());
    
//Tracer.log("**** The column name is " + column.getColumnName(), Tracer.DEBUG, OBJECT_NAME);
//Tracer.log("**** The column datatype is " + column.getClassName(), Tracer.DEBUG, OBJECT_NAME);
//Tracer.log("**** The column length is " + column.getColumnLength(), Tracer.DEBUG, OBJECT_NAME);
//Tracer.log("********************************************", Tracer.DEBUG, OBJECT_NAME);
    
    String rval = null;
    
    // Translate the object into a String
    if ( value != null ) {
  
	    if ( column.getClassName().equals("java.lang.String") ) {
        if ( column.getColumnLength() > 200000000 || column.getColumnLength() == 0 ) {  // This is how a Long column is indicated via JDBC
          rval = HtmlComponent.getTextArea("sql",value.toString(),130,4);          
        } else {
          rval = (String) value;
        }
	    }
      else if ( column.getClassName().equals("java.math.BigDecimal") ) {
      	//DecimalFormat form = new DecimalFormat("###,###,###.00");
        //rval = form.format(((java.math.BigDecimal) value));
      	rval = value.toString();
      }
      else if ( column.getClassName().equals("java.sql.Timestamp") ) {
        rval = ((Date) value).toString();
      }
      else if ( column.getClassName().equals("java.sql.Timestamp") || 
      		      column.getClassName().equals("oracle.sql.TIMESTAMP") ) {
	      rval = ((Date) value).toString();
	    }
	    else if ( column.getClassName().equals("java.sql.CLOB") ) {
      	oracle.sql.CLOB clob = (oracle.sql.CLOB) value;
      	try {
					rval = CLOBToString(clob);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	      //rval = HtmlComponent.getTextArea("sql",value.toString(),130,4);
	    }
	    else if ( column.getClassName().equals("oracle.sql.CLOB") ) {
      	Tracer.log("Got a CLOB.........",Tracer.DEBUG,"ObjectTranslator");
      	oracle.sql.CLOB clob = (oracle.sql.CLOB) value;
        try {
        	rval = CLOBToString(clob);
					//rval = HtmlComponent.getTextArea("sql",CLOBToString(clob),130,6);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
      }

      
    }
    
//Tracer.log("Got Value " + rval + " for column " + column.getColumnName() + " class name " + column.getClassName(), Tracer.DEBUG, OBJECT_NAME);


    return rval;

  }

}