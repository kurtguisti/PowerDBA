/*
 * Created by IntelliJ IDEA.
 * User: kguisti
 * Date: Nov 2, 2002
 * Time: 7:52:19 PM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.powerdba.gui;

import java.util.ArrayList;
import java.util.Hashtable;

public class Page {

  public Page() {}

  public Page(ArrayList rows, int sortColumnIndex, 
              ArrayList columns, Hashtable columnsHash, int keyPosition) {
    this.rows = rows;
    this.sortColumnIndex = sortColumnIndex;
    this.columnsHash = columnsHash;
    this.columns = columns;
    this.keyPosition = keyPosition;
  }

  private ArrayList rows = new ArrayList();  // A page contains rows.  A row
  private int sortColumnIndex;               // The current sort column for the page.  Each page
                                             // may be sorted independently of the pageset.
  private ArrayList columns = new ArrayList();
  private Hashtable columnsHash = new Hashtable();
  private int pageNumber = 1;
  private int totalPages;
  private String title;
  private long genTime;
  private boolean pageSetLimitExceeded = false;
  private long pageSetLimit;
  private int keyPosition;
  private String sql;
  private ArrayList charts = new ArrayList();
  private int dataType;
  private String databaseLink;
  private String errorMessage;
  
  public ArrayList getRows() { return this.rows; }
  public int getSortColumnIndex() { return this.sortColumnIndex; }    
  public int getRowCount() { return this.rows.size(); }
  public ArrayList getColumns() { return this.columns; }
  public int getPageNumber() { return this.pageNumber; }
  public int getTotalPages() { return this.totalPages; }
  public String getTitle() { return this.title; }
  
  public void setRows(ArrayList value) { this.rows = value; }
  public void setSortColumnIndex(int value) {this.sortColumnIndex = value; }
  public void setColumns(ArrayList value) {this.columns = value; }
  public void setPageNumber(int value) {this.pageNumber = value; }
  public void setTotalPages(int value) {this.totalPages = value; }
  public void setTitle(String value) {this.title = value; }

  public long getGenTime() {
		return genTime;
	}

	public void setGenTime(long genTime) {
		this.genTime = genTime;
	}

	public void setPageSetLimitExceeded(boolean pageSetLimitExceeded)
  {
    this.pageSetLimitExceeded = pageSetLimitExceeded;
  }


  public boolean isPageSetLimitExceeded()
  {
    return pageSetLimitExceeded;
  }


  public void setPageSetLimit(long pageSetLimit)
  {
    this.pageSetLimit = pageSetLimit;
  }


  public long getPageSetLimit()
  {
    return pageSetLimit;
  }


  public void setKeyPosition(int keyPosition)
  {
    this.keyPosition = keyPosition;
  }


  public int getKeyPosition()
  {
    return keyPosition;
  }


  public void setSql(String sql)
  {
    this.sql = sql;
  }


  public String getSql(){
    return this.sql;
  }


  public void setColumnsHash(Hashtable columnsHash)
  {
    this.columnsHash = columnsHash;
  }


  public Hashtable getColumnsHash()
  {
    return columnsHash;
  }


  public void setCharts(ArrayList charts)
  {
    this.charts = charts;
  }


  public ArrayList getCharts()
  {
    return charts;
  }


  public void setDataType(int dataType)
  {
    this.dataType = dataType;
  }


  public int getDataType()
  {
    return dataType;
  }
  
  

/**
 * @return Returns the databaseLink.
 */
public String getDatabaseLink() {
    return databaseLink;
}
/**
 * @param databaseLink The databaseLink to set.
 */
public void setDatabaseLink(String databaseLink) {
    this.databaseLink = databaseLink;
}

/**
 * @return Returns the errorMessage.
 */
public String getErrorMessage() {
    return errorMessage;
}
/**
 * @param errorMessage The errorMessage to set.
 */
public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
}
}