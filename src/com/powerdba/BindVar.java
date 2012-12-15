
package com.powerdba;

public class BindVar {
    public BindVar() {}
    
    public BindVar(String varName) {
      this.dataType = "java.lang.String";
      this.varName = varName;
    }
    
    public BindVar(String varName, String dataType, String defaultValue) {
        this.dataType = dataType;
        this.varName = varName;
        this.defaultValue = defaultValue;
      }

    private String varName = "";
    private String dataType = "";
    private Object value;
    private Object defaultValue;


  public void setVarName(String varName)
  {
    this.varName = varName;
  }


  public String getVarName()
  {
    return varName;
  }


  public void setValue(Object value)
  {
    this.value = value;
  }


  public Object getValue()
  {
    return value;
  }


  public void setDataType(String dataType)
  {
    this.dataType = dataType;
  }


  public String getDataType()
  {
    return dataType;
  }
  

    
    /**
     * @return Returns the defaultValue.
     */
    public Object getDefaultValue() {
        return defaultValue;
    }
    /**
     * @param defaultValue The defaultValue to set.
     */
    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }
}