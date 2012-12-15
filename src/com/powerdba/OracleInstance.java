package com.powerdba;

public class OracleInstance {

  public OracleInstance() {}
  
  public OracleInstance(String name, OracleVersion version) {
    this.name = name;
    this.version = version;
  }
  
  private String name;
  private OracleVersion version;
  
  public String toHtml() {
  
    StringBuffer sb = new StringBuffer();
    
    sb.append("<table>");
    sb.append("<tr>");
    sb.append("<th><font size=-1>Database Name:</font></th><td><font size=-1>"+this.name+"</font><td>");
    sb.append("</tr>");
    sb.append("<tr>");
    sb.append("<th><font size=-1>Version:</font></th><td><font size=-1>"+this.version+"</font><td>");
    sb.append("</tr>");
    sb.append("</table>");
    
    return sb.toString();
  }


  public void setName(String name)
  {
    this.name = name;
  }


  public String getName()
  {
    return name;
  }


  public void setVersion(OracleVersion version)
  {
    this.version = version;
  }


  public OracleVersion getVersion()
  {
    return version;
  }


}