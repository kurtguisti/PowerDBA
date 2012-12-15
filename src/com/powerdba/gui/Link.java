package com.powerdba.gui;

import com.powerdba.OracleDatabaseConnection;
import java.util.ArrayList;
import java.util.Hashtable;

public class Link {
  public Link() {}
  
  public Link(String action, String columnName, String description) {
    this.action = action;
    this.columnName = columnName;
    this.description = description;
  }

  private String fileName = "";
  private String description = "";
  private String action = "";
  private String columnName = "";
  private ArrayList variables = new ArrayList();
  private String text = "";
  private String alt = "";
  private String type = "NewPage";
  private String jsFunction;
  private String getAltFrom;


	/**
	 * @return Returns the getAltFrom.
	 */
	public String getGetAltFrom() {
	    return getAltFrom;
	}
	/**
	 * @param getAltFrom The getAltFrom to set.
	 */
	public void setGetAltFrom(String getAltFrom) {
	    this.getAltFrom = getAltFrom;
	}
  public void setFileName(int command, String database) { 
    this.fileName = "powerdba.jsp?formaction=" + command + "&database="+database;
  }
  
  public void setFileName(int command, OracleDatabaseConnection database) { 
    this.fileName = "powerdba.jsp?formaction=" + command + "&database="+database.getName();
  }
  
  public void setFileName(String url) {
    this.fileName = url;
  }
  
  public String getFileName() { return this.fileName; }


  public void setAction(String action)
  {
    this.action = action;
  }


  public String getAction()
  {
    return action;
  }


  public void setColumnName(String columnName)
  {
    this.columnName = columnName;
  }


  public String getColumnName()
  {
    return columnName;
  }


  public void setVariables(ArrayList variables)
  {
    this.variables = variables;
  }


  public ArrayList getVariables()
  {
    return variables;
  }


  public void setText(String text)
  {
    this.text = text;
  }


  public String getText()
  {
    return text;
  }


  public void setAlt(String alt)
  {
    this.alt = alt;
  }


  public String getAlt()
  {
    return alt;
  }


  public void setType(String type)
  {
    this.type = type;
  }


  public String getType()
  {
    return type;
  }


  public void setJsFunction(String jsFunction)
  {
    this.jsFunction = jsFunction;
  }


  public String getJsFunction()
  {
    return jsFunction;
  }
  
  public String toString() {
  
    StringBuffer sb = new StringBuffer();
    
    sb.append("Link: \n");
    sb.append("  action: " + this.action + "\n");    
    sb.append("  On Column: " + this.columnName + "\n");
    sb.append("  file name: " + this.fileName + "\n");
    sb.append("  Link Variables: \n");
    for ( int i=0; i<variables.size(); i++ ) {
      LinkVar var = (LinkVar) variables.get(i);
      sb.append("    columnName: " + var.getColumnName() + "\n");
      sb.append("    index: " + var.getIndex() + "\n");
      sb.append("    varName: " + var.getVarName() + "\n");
    }
    
    return sb.toString();
    
  }


  public void setDescription(String description)
  {
    this.description = description;
  }


  public String getDescription()
  {
    return description;
  }

}
