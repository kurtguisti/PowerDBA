package com.powerdba;

import com.powerdba.util.StringUtility;

public class OracleBaseObject {

  public OracleBaseObject() {}
  
  public OracleBaseObject(String type, String owner, String name) {
    this.type = type;
    this.owner = owner;
    this.name = name;
  }
  
  private String type = "NA";
  private String owner;
  private String name;
  private long rowCount = -1;


  public void setType(String type)
  {
    this.type = type;
  }


  public String getType()
  {
    return type;
  }


  public void setOwner(String owner)
  {
    this.owner = owner;
  }


  public String getOwner()
  {
    return owner;
  }


  public void setName(String name)
  {
    this.name = name;
  }


  public String getName()
  {
    return name;
  }
  
  public String getFullName() 
  {
    String returnVal = "";
    String numRows = "";
    if ( this.getRowCount() > -1 ) {
      numRows = "[" + this.getRowCount() + "]";
    }
    
    if ( this.getName() != null ) {
      returnVal = this.getName() + " " + numRows;
      //returnVal = this.getOwner() + "." + this.getName() + " " + numRows;
    }
    return returnVal;
  }
  
  public String getFullNameWithType()
  {
    return this.getFullName() + " : " + this.getType();
  }
  
/**
 * @return Returns the rowCount.
 */
public long getRowCount() {
    return rowCount;
}
/**
 * @param rowCount The rowCount to set.
 */
public void setRowCount(long rowCount) {
    this.rowCount = rowCount;
}
}