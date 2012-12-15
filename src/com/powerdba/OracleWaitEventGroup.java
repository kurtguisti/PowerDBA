package com.powerdba;

public class OracleWaitEventGroup {
    public OracleWaitEventGroup() {}
    
    public OracleWaitEventGroup(String name, String query, String explanation,
                                short p1Count, short p2Count, short p3Count) {
      this.name = name;
      this.query = query;
      this.explanation = explanation;
      this.p1Count = p1Count;
      this.p2Count = p2Count;
      this.p3Count = p3Count;
    }

    private String name;
    private String query;
    private String explanation;
    private short p1Count;
    private short p2Count;
    private short p3Count;


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


  public void setExplanation(String explanation)
  {
    this.explanation = explanation;
  }


  public String getExplanation()
  {
    return explanation;
  }


  public void setP1Count(short p1Count)
  {
    this.p1Count = p1Count;
  }


  public short getP1Count()
  {
    return p1Count;
  }


  public void setP2Count(short p2Count)
  {
    this.p2Count = p2Count;
  }


  public short getP2Count()
  {
    return p2Count;
  }


  public void setP3Count(short p3Count)
  {
    this.p3Count = p3Count;
  }


  public short getP3Count()
  {
    return p3Count;
  }
    

    
}