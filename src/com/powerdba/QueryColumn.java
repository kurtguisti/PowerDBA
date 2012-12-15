package com.powerdba;

public class QueryColumn {

  public QueryColumn (){}
  
  public QueryColumn(String name, String alias) {
    this.name = name;
    this.alias=alias;
  }
  
  private String name;
  private String alias;


  public void setName(String name)
  {
    this.name = name;
  }


  public String getName()
  {
    return name;
  }


  public void setAlias(String alias)
  {
    this.alias = alias;
  }


  public String getAlias()
  {
    return alias;
  }



}