/*
 * Created by IntelliJ IDEA.
 * User: kguisti
 * Date: Nov 2, 2002
 * Time: 7:52:19 PM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.powerdba;

public class LinkVar {
    public LinkVar() {}
    
    public LinkVar(int index, String varName) {
      this.index = index;
      this.varName = varName;
    }
    
    public LinkVar(String columnName, String varName) {
      this.columnName = columnName;
      this.varName = varName;
    }

    private int index = 0;
    private String varName = "";
    private int offset = 0;
    private String columnName = "";


  public void setIndex(int index)
  {
    this.index = index;
  }


  public int getIndex()
  {
    return index;
  }


  public void setVarName(String varName)
  {
    this.varName = varName;
  }


  public String getVarName()
  {
    return varName;
  }


  public void setOffset(int offset)
  {
    this.offset = offset;
  }


  public int getOffset()
  {
    return offset;
  }


  public void setColumnName(String columnName)
  {
    this.columnName = columnName;
  }


  public String getColumnName()
  {
    return columnName;
  }



}
