
package com.powerdba.gui;

import java.util.ArrayList;
import java.util.Hashtable;

public class PageSet {


    public PageSet() {}

    public PageSet(int rowsPerPage, int keyColumn) {
      this.rowsPerPage = rowsPerPage;
      this.keyColumn = keyColumn;
     }

    private String title;
    private int rowsPerPage;                     // Number of rows per page
    private int keyColumn;                       // Column defined as the primary key for the pageset
    private ArrayList pages = new ArrayList();   // List of pages
    private ArrayList columns = new ArrayList(); // List of columns.  Defines the sructure of the 
                                                 // rows withing this pageset.  Row structures
                                                 // must be the same within a pageset (implies page as well)
    private Hashtable columnsHash = new Hashtable();
    private int currentPageNumber = 1;
    private String sql;
    private ArrayList charts;
    private int dataType;

    public int getRowsPerPage() { return this.rowsPerPage; }
    public int getKeyColumn() { return this.keyColumn; }    
    public ArrayList getPages() { return this.pages; }
    public ArrayList getColumns() { return this.columns; }
    public int getCurrentPageNumber() { return this.currentPageNumber; }
    public int getPageCount() { return this.pages.size(); }
    public String getTitle() { return this.title; }
    
    
    public Page getPage(int pageNumber) {
      int pageNo = 0;

      if ( pageNumber == 0 || pageNumber > this.pages.size() ) {
        pageNo = 1;
      } else {
        pageNo = pageNumber;
      }

      Page page = new Page();
      if ( this.pages.size() > 0 ) {
        page = (Page) this.pages.get(pageNo - 1);
        page.setPageNumber(pageNo);
        page.setTitle(this.title);
        page.setTotalPages(this.pages.size()); 
      }
      return page;
    }

    public Page getCurrentPage() {
      Page page = (Page) this.pages.get(this.getCurrentPageNumber()-1);
      page.setPageNumber(this.getCurrentPageNumber());
      page.setTotalPages(this.pages.size());
      return page; 
    }

    public Page getNextPage() {
      if ( this.currentPageNumber == pages.size() ) {
        return getFirstPage();
      } else {
        this.currentPageNumber++;
        return getCurrentPage();
      }
    }
    public Page getPreviousPage() {
      if ( this.currentPageNumber == 1 ) {
        return getLastPage();
      } else {  
        this.currentPageNumber--;
        return getCurrentPage(); 
      }
    }
    public Page getFirstPage() {
      this.currentPageNumber = 1;
      return getCurrentPage(); 
    }
    public Page getLastPage() {
      this.currentPageNumber = this.pages.size();
      return getCurrentPage(); 
    }

    public void setRowsPerPage(int value) { this.rowsPerPage = value; }
    public void setKeyColumn(int value) {this.keyColumn = value; }
    public void setPages(ArrayList value) {this.pages = value; }
    public void setColumns(ArrayList value) {this.columns = value; }
    public void setCurrentPageNumber(int value) {this.currentPageNumber = value; }
    public void setTitle(String value) {this.title = value; }


  public void set_title(String title)
  {
    this.title = title;
  }


  public String get_title()
  {
    return title;
  }


  public void setSql(String sql)
  {
    this.sql = sql;
  }


  public String getSql()
  {
    return sql;
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
    
}