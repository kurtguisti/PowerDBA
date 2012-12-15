package com.powerdba;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Hashtable;

public class OracleStatistic {

  public OracleStatistic() {
    loadStatClassValues();
  }
  
  public OracleStatistic(String name, long value, int sclass, String uom, float multiplier, String desc) {
    loadStatClassValues();
    this.name = name;
    this.value = value;
    this.sclass = sclass;
    this.uom = uom;
    this.multiplier = multiplier;
    this.desc = desc;
    if ( value != 0 && multiplier != 1 ) { 
      this.translate();
    }
  }
  
  private String name;
  private long value;
  private String adjustedValue;
  private int sclass;
  private String uom;
  private float multiplier;
  private String desc;
  
  private Hashtable statClasses = new Hashtable();
  
  public void translate() {
    float f = Float.parseFloat(Long.toString(this.value));
    f = f / this.multiplier;
    NumberFormat format = NumberFormat.getInstance();
    if ( value != 0 && multiplier != 1 ) {
      this.adjustedValue = ((DecimalFormat) format).format(f) + ((this.uom != null)?"&nbsp;" + this.uom:"");
    } else {
      this.adjustedValue = Long.toString(value) + " " + ((uom != null)?uom:"");
    }
  }

  public void setName(String name)
  {
    this.name = name;
  }


  public String getName()
  {
    return name;
  }


  public void setValue(long value)
  {
    this.value = value;
    this.translate();
  }


  public long getValue()
  {
    return value;
  }
  
  public String toString() {
    return this.getName() + ": " + this.getAdjustedValue();
  }
  
  public String toHtml() {
    return "<b>" + this.getName() + ":</b>&nbsp;" + this.getAdjustedValue();
  }  
  
  private void loadStatClassValues() {
  
    //1, User
    //2, Redo
    //4, Enqueue 
    //8, Cache 
    //16, OS 
    //32, Real Application Clusters 
    //64, SQL 
    //128, Debug
  
    this.statClasses.put(new Integer(1), "User");
    this.statClasses.put(new Integer(2), "Redo");
    this.statClasses.put(new Integer(4), "Enqueue");
    this.statClasses.put(new Integer(8), "Cache");
    this.statClasses.put(new Integer(16), "OS");
    this.statClasses.put(new Integer(32), "RAC");
    this.statClasses.put(new Integer(40), "RAC Cache");
    this.statClasses.put(new Integer(64), "SQL");
    this.statClasses.put(new Integer(72), "SQL Cache");
    this.statClasses.put(new Integer(128), "Debug");   

  }


  public void setStatClasses(Hashtable statClasses)
  {
    this.statClasses = statClasses;
  }


  public Hashtable getStatClasses()
  {
    return statClasses;
  }
  
  public String getStatClassDescription(String classId) {
    String rval = null;
    rval =  (String) this.statClasses.get(new Integer(classId));
    if ( rval == null ) rval = "Class " + classId;
    return rval;
  }



  public void setSclass(int sclass)
  {
    this.sclass = sclass;
  }


  public int getSclass()
  {
    return sclass;
  }


  public void setUom(String uom)
  {
    this.uom = uom;
  }


  public String getUom()
  {
    return uom;
  }


  public void setMultiplier(float multiplier)
  {
    this.multiplier = multiplier;
  }


  public float getMultiplier()
  {
    return multiplier;
  }


  public void setDesc(String desc)
  {
    this.desc = desc;
  }


  public String getDesc()
  {
    return desc;
  }


  public void setAdjustedValue(String adjustedValue)
  {
    this.adjustedValue = adjustedValue;
  }


  public String getAdjustedValue()
  {
    return adjustedValue;
  }
  
}