package com.powerdba;

import java.util.ArrayList;

public class Link {
    public Link() {}
    
    public Link(int action, String columnName) {
      this.action = action;
      this.columnName = columnName;
    }

    private String fileName = "";
    private int action = 0;
    private String columnName = "";
    private ArrayList variables = new ArrayList();
    private String text = "";
    private String alt = "";
    private String type = "NewPage";
    private String jsFunction = "NA";
    private String getAltFrom = "";


    /**
     * @return Returns the fileName.
     */
    public String getFileName() {
        return fileName;
    }
    /**
     * @param fileName The fileName to set.
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
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


  public void setAction(int action)
  {
    this.action = action;
  }


  public int getAction()
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

}
