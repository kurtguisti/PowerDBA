package com.powerdba;

import java.util.StringTokenizer;

public class OracleVersion 
{
  public OracleVersion(){}
  
  public OracleVersion(int v1, int v2, int v3, int v4, int v5) {
    this.version1 = v1;
    this.version2 = v2;
    this.version3 = v3;
    this.version4 = v4;
    this.version5 = v5;
  }
  
  public OracleVersion(String version) {
    StringTokenizer st = new StringTokenizer(version, ".");
    for ( int i=1; st.hasMoreTokens(); i++ ) {
    
      switch (i) {
      
        case 1:
          version1 = Integer.parseInt(st.nextToken());
          break;
        case 2:
          version2 = Integer.parseInt(st.nextToken());
          break;
        case 3:
          version3 = Integer.parseInt(st.nextToken());
          break;
        case 4:
          version4 = Integer.parseInt(st.nextToken());
          break;
        case 5:
          version5 = Integer.parseInt(st.nextToken());
          break;
    
      }
    }
  }
  
  private int version1 = 0;
  private int version2 = 0;
  private int version3 = 0;
  private int version4 = 0;
  private int version5 = 0;
  private String compatibility = null;
  
  public String toString() {
  
    return version1 + "." + version2 + "." + version3 + "." + version4 + "." + version5;
    
  }
  
  public void setCompatability(String comp)
  {
    this.compatibility = comp;
  }


  public void setVersion1(int version1)
  {
    this.version1 = version1;
  }
  

  public String getCompatibility()
  {
    return this.compatibility;
  }
  
  public int getVersion1()
  {
    return version1;
  }


  public void setVersion2(int version2)
  {
    this.version2 = version2;
  }


  public int getVersion2()
  {
    return version2;
  }


  public void setVersion3(int version3)
  {
    this.version3 = version3;
  }


  public int getVersion3()
  {
    return version3;
  }


  public void setVersion4(int version4)
  {
    this.version4 = version4;
  }


  public int getVersion4()
  {
    return version4;
  }


  public void setVersion5(int version5)
  {
    this.version5 = version5;
  }


  public int getVersion5()
  {
    return version5;
  }
}