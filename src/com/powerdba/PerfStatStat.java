package com.powerdba;

public class PerfStatStat {

  public PerfStatStat(){}
  
  public PerfStatStat(int id, String name, String query, String tableName, String orderBy, String groupBy) {
    this.id = id;
    this.name = name;
    this.query = query;
    this.tableName = tableName;
    this.orderBy = orderBy;
    this.groupBy = groupBy;
  }
  
  private int id;
  private String name;
  private String query;
  private String tableName;
  private String orderBy = "";
  private String groupBy = "";


  public void setId(int id)
  {
    this.id = id;
  }


  public int getId()
  {
    return id;
  }


  public void setName(String name)
  {
    this.name = name;
  }


  public String getName()
  {
    return name;
  }


  public void setQuery(String query)
  {
    this.query = query;
  }


  public String getQuery()
  {
    return query;
  }


  public void setTableName(String tableName)
  {
    this.tableName = tableName;
  }


  public String getTableName()
  {
    return tableName;
  }


  public void setOrderBy(String orderBy)
  {
    this.orderBy = orderBy;
  }


  public String getOrderBy()
  {
    return orderBy;
  }


  public void setGroupBy(String groupBy)
  {
    this.groupBy = groupBy;
  }


  public String getGroupBy()
  {
    return groupBy;
  }
  
}