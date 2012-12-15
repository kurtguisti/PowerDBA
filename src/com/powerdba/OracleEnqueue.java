package com.powerdba;

public class OracleEnqueue {

  public OracleEnqueue() {}
  
  public OracleEnqueue(String code, String name, String desc) {
    this.code = code;
    this.name = name;
    this.desc = desc;
  }
  
  private String code = " ";
  private String name = " ";
  private String desc = " ";
  
  public String toHtml() {
  
    StringBuffer sb = new StringBuffer();
    
    sb.append("<table>");
    sb.append("<tr>");
    sb.append("<th><font size=-1>Code:</font></th><td><font size=-1>"+this.code+"</font><td>");
    sb.append("</tr>");
    sb.append("<tr>");
    sb.append("<th><font size=-1>Name:</font></th><td><font size=-1>"+this.name+"</font><td>");
    sb.append("</tr>");
    sb.append("<tr>");
    sb.append("<th><font size=-1>Description:</font></th><td><font size=-1>"+this.desc+"</font><td>");
    sb.append("</tr>");
    sb.append("</table>");
    
    return sb.toString();
  }

  public void setCode(String code)
  {
    this.code = code;
  }


  public String getCode()
  {
    return code;
  }


  public void setName(String name)
  {
    this.name = name;
  }


  public String getName()
  {
    return name;
  }


  public void setDesc(String desc)
  {
    this.desc = desc;
  }


  public String getDesc()
  {
    return desc;
  }
}