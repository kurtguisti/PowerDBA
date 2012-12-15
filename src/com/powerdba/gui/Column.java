/*
 * Created by IntelliJ IDEA.
 * User: kguisti
 * Date: Nov 2, 2002
 * Time: 7:52:19 PM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.powerdba.gui;

public class Column {

    public static final int RIGHT=1;
    public static final int CENTER=2;
    public static final int LEFT=3;

    public Column() {}

    public Column(String className, String columnType, String columnName, String columnHeading,
                  int columnIndex, long columnLength, long columnPrecision, 
                  int columnScale, int isNullable, boolean isHidden, Link link) {
      this.className = className;
      this.columnType = columnType;
      this.columnName = columnName;
      this.columnHeading = columnHeading;
      this.columnIndex = columnIndex;
      this.columnLength = columnLength;
      this.columnPrecision = columnPrecision;
      this.columnScale = columnScale;
      this.isNullable = isNullable;
      this.isHidden = isHidden;
      this.link = link;
      
    }

    private String className;
    private String columnType;
    private String columnName;
    private String columnHeading;
    private int columnIndex;
    private long columnLength;
    private long columnPrecision;
    private int columnScale;
    private int isNullable;
    private boolean isHidden;
    private Link link;
    private int justification = LEFT;
    private long maxLength = 0;
    private boolean preformat = false;
    
    public String getClassName() { return this.className; }
    public String getColumnType() { return this.columnType; }    
    public String getColumnName() { return this.columnName; }
    public int getColumnIndex() { return this.columnIndex; }
    public long getColumnLength() { return this.columnLength; }
    public long getColumnPrecision() { return this.columnPrecision; }
    public int getColumnScale() { return this.columnScale; }
    public int isNullable() { return this.isNullable; }
    public boolean isHidden() { return this.isHidden; }
    public Link getLink() { return this.link; }

    public void setClassName(String value) { this.className = value; }
    public void setColumnType(String value) {this.columnType = value; }
    public void setColumnName(String value) {this.columnName = value; }
    public void setColumnIndex(int value) {this.columnIndex = value; }
    public void setColumnLength(long value) {this.columnLength = value; }
    public void setColumnPrecision(long value) {this.columnPrecision = value; }
    public void setColumnScale(int value) {this.columnScale = value; }
    public void setIsNullable(int value) {this.isNullable = value; }
    public void setIsHidden(boolean value) {this.isHidden = value; }
    public void setLink(Link value) {this.link = value; }
    


  public boolean isPreformat() {
			return preformat;
		}

		public void setPreformat(boolean preformat) {
			this.preformat = preformat;
		}

	public void setColumnHeading(String columnHeading)
  {
    this.columnHeading = columnHeading;
  }


  public String getColumnHeading()
  {
    return columnHeading;
  }


  public void setJustification(int justification)
  {
    this.justification = justification;
  }


  public int getJustification()
  {
    return justification;
  }

	/**
	 * @return Returns the maxLength.
	 */
	public long getMaxLength() {
		return maxLength;
	}

	/**
	 * @param maxLength The maxLength to set.
	 */
	public void setMaxLength(long maxLength) {
		this.maxLength = maxLength;
	}
  
}