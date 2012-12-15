
package com.powerdba.gui;

import com.powerdba.chart.TimeSeriesDataSet;
import com.powerdba.util.Tracer;
import com.powerdba.gui.Link;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;

import oracle.sql.CLOB;

import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

public class PageLoader {

  private static final String OBJECT_NAME = "PageLoader";
  
  static public PageSet buildPageSet(TimeSeriesDataSet ds) throws Exception {
                                                              
    // Loads a JfreeChart TimeSeriesCollection Into a PageSet Structure
    
    TimeSeriesCollection dataset = ds.getTimeSeries();
                                     
    String timeColumn = "Time";
    DecimalFormat format = new DecimalFormat("###,###,###.00000");
    PageSet ps = new PageSet();
    ps.setTitle(ds.getDescription());
  
    Column column;
    ArrayList columns = new ArrayList();
    Hashtable columnsHash = new Hashtable();
    // Create the first column for the time description.
    column = new Column("java.lang.String","",timeColumn.toLowerCase(),timeColumn.toLowerCase(),1,0,0,0,0,false,null);
    column.setJustification(Column.CENTER);
    columns.add(column);
    
    for ( int i=2; i<=dataset.getSeriesCount()+1; i++ ) {    
      TimeSeries t = dataset.getSeries(i-2);    
      column  = new Column("java.lang.String","",t.getName().toLowerCase(),t.getName().toLowerCase(),i,0,0,0,0,false,null);
      column.setJustification(Column.RIGHT);
      columns.add(column);
      columnsHash.put(column.getColumnName(), column);      
    }      
    
    ps.setColumns(columns);
    ps.setColumnsHash(columnsHash);
    
    // Build the pageset's rows         
    Hashtable row;
    ArrayList rows = new ArrayList();
    Page page = new Page();
    ArrayList pages = new ArrayList();
    
    // Drive from the first time series in the dataset since all series have equal # of rows.
    Collection periods = dataset.getSeries(0).getTimePeriods();
    Iterator i = periods.iterator();
   
    while ( i.hasNext() ) {
      RegularTimePeriod period = (RegularTimePeriod) i.next();
      row = new Hashtable();  // Row Objects are the column data elements.

      // Build the row
      
      // Put the time value in the first column
      row.put(timeColumn.toLowerCase(), period.toString());
      
      // Loop through each dataset and the value corresponding to that time value for each time series.
      for ( int j=0; j<dataset.getSeriesCount(); j++ ) {
        TimeSeries currentSeries = dataset.getSeries(j); 
        row.put(currentSeries.getName().toLowerCase(), format.format(currentSeries.getValue(period).doubleValue()));
      }

      // Add the row to the current list of rows and increment the row counter.
      rows.add(row);

    }
    
    // Instantiate a new Page object with the rows just loaded
    page = new Page(rows, 1, columns, columnsHash, 0);
    page.setPageNumber(1);
    page.setTitle(ps.getTitle());
    
    // Add the page to the list of pages and then the list of pages to the PageSet.
    pages.add(page);
    ps.setPages(pages);

    return ps; 
  }
  
  static public PageSet buildPageSet(ResultSet rset, 
                                     int keyIndex, 
                                     int rowsPerPage, 
                                     int sortIndex) throws SQLException {
                                     
    return buildPageSet(rset, keyIndex, rowsPerPage, sortIndex, new Hashtable(), new ArrayList(), new ArrayList(),"", -1, null, 0);
  }
  
  static public PageSet buildPageSet(ResultSet rset, 
                                     int keyIndex, 
                                     int rowsPerPage, 
                                     int sortIndex,
                                     Hashtable links,
                                     ArrayList hiddens,
                                     ArrayList summary,
                                     String title,
                                     long rowLimit) throws SQLException {
    return buildPageSet(rset, keyIndex, rowsPerPage, sortIndex, links, hiddens, summary, title, rowLimit, null, 0 );
  }
                                     
  static public PageSet buildPageSet(ResultSet rset, 
                                     int keyIndex, 
                                     int rowsPerPage, 
                                     int sortIndex,
                                     Hashtable links,
                                     ArrayList hiddens,
                                     ArrayList summary,
                                     String title,
                                     long rowLimit,
                                     String sql,
                                     int dataType) throws SQLException {

    PageSet ps = new PageSet(rowsPerPage, keyIndex);
    ps.setTitle(title);
    ps.setSql(sql);
        
    ResultSetMetaData rmd = rset.getMetaData();
    Column c;
    ArrayList columns = new ArrayList();
    boolean isHidden = false;
    Link link = null;
        
    for ( int i=1; i <= rmd.getColumnCount(); i++ ) {
    
      if ( hiddens.contains(new Integer(i)) ) {
        isHidden = true;
      } else {
        isHidden = false;
      }
      
      link = (Link) links.get(new Integer(i));
      
      c = new Column(rmd.getColumnClassName(i), rmd.getColumnTypeName(i),
                     getColumnHeading(rmd.getColumnName(i)), 
                     getColumnHeading(rmd.getColumnName(i)),i, rmd.getColumnDisplaySize(i),
                     rmd.getPrecision(i), rmd.getScale(i), rmd.isNullable(i), 
                     isHidden, link);
      columns.add(c);
    }        

    ps.setColumns(columns);         // Defines the structure of the pageset.
         
    ArrayList row = new ArrayList();
    ArrayList rows = new ArrayList();
    int rowCounter = 0;
    int totalRowCounter = 0;

    ArrayList pages = new ArrayList();

    while ( rset.next() && ( totalRowCounter < rowLimit && rowLimit > 0 ) ) {

      row = new ArrayList();  // Row Objects are the column data elements.

      // Build the row
      for ( int i=1; i<=rmd.getColumnCount(); i++ ) {
        row.add(rset.getObject(i));   // Add the generic objects to the row
      }

      // Add the row to the current list of rows and increment the row counter.
      rows.add(row);
      rowCounter++;
      totalRowCounter++;

      // If we hit rows per page do some stuff...
      if ( rowCounter == rowsPerPage ) { 
        // Create a new page object with the rows in memory and add it to the set
        if ( rows.size() > 0 ) {
          Page page = new Page(rows, sortIndex, columns, new Hashtable(), keyIndex);
          page.setTitle(ps.getTitle());
          page.setSql(ps.getSql());
          pages.add(page);
        }
        // Clear the rows Array and reset the counter
        rows = new ArrayList();
        rowCounter = 0;
      }     
    }

    // Create a new page object with the rows in memory and add it to the set
    if ( rows.size() > 0 ) {
      Page page = new Page(rows, sortIndex, columns, new Hashtable(), keyIndex);
      page.setTitle(ps.getTitle());
      page.setSql(ps.getSql());
      pages.add(page);
    }
    
    if ( totalRowCounter == rowLimit ) {
      for ( int i=0; i<pages.size(); i++ ) {
        Page pg = (Page) pages.get(i);
        pg.setPageSetLimitExceeded(true);
        pg.setPageSetLimit(rowLimit);
      }
    }

    ps.setPages(pages);

    return ps;

  }
  
  static public PageSet buildPageSetNew(ResultSet rset, 
                                        int keyIndex, 
                                        int rowsPerPage, 
                                        int sortIndex) throws SQLException {
                                     
    return buildPageSetNew(rset, keyIndex, rowsPerPage, sortIndex, new Hashtable(), new ArrayList(), new ArrayList(), new ArrayList(),"", -1, new ArrayList(), 0);
  }
  
  static public PageSet buildPageSetNew(ResultSet rset, 
                                        int keyIndex, 
                                        int rowsPerPage, 
                                        int sortIndex,
                                        Hashtable links,
                                        ArrayList hiddens,
                                        ArrayList preformats,
                                        ArrayList summary,
                                        String title,
                                        long rowLimit) throws SQLException {
    return buildPageSetNew(rset, keyIndex, rowsPerPage, sortIndex, links, hiddens, preformats, summary, title, rowLimit, new ArrayList(), 0 );
  }
  
  static public PageSet buildPageSetNew(ResultSet rset, 
                                        int keyIndex, 
                                        int rowsPerPage, 
                                        int sortIndex,
                                        Hashtable links,
                                        ArrayList hiddens,
                                        ArrayList preformats,
                                        ArrayList summary,
                                        String title,
                                        long rowLimit,
                                        ArrayList charts,
                                        int dataType) throws SQLException {
      
    Tracer.log("Building a new format page set object from a jdbc ResultSet", Tracer.DEBUG, OBJECT_NAME);

    PageSet ps = new PageSet(rowsPerPage, keyIndex);
    ps.setTitle(title);
    ps.setSql("");
        
    ResultSetMetaData rmd = rset.getMetaData();
    Column c;
    ArrayList columns = new ArrayList();
    Hashtable columnsHash = new Hashtable();
    boolean isHidden = false;
    boolean isPreformat = false;
    Link link = null;
    
    // Build the column definitions List and put it in the pageset object
    
    if ( rmd.getColumnCount() == 0 ) throw new SQLException("Columns in query is 0");
        
    for ( int i=1; i <= rmd.getColumnCount(); i++ ) {
    
      String columnName = rmd.getColumnName(i);
      
      if ( hiddens.contains(columnName.toLowerCase()) ) {
      	isHidden = true;    
      } else {
      	isHidden = false;
      }
      if ( preformats.contains(columnName.toLowerCase()) ) {
      	isPreformat = true;
      } else {
      	isPreformat = false;
      }
      
      if ( links == null ) throw new SQLException("links array is null");
      
      link = (Link) links.get(columnName.toLowerCase());
      
      int precision = 0;
      try {
      	precision = rmd.getPrecision(i);
      } catch (NumberFormatException nfe) {
      	precision = 2000;
      }
      
      c = new Column(rmd.getColumnClassName(i), rmd.getColumnTypeName(i), rmd.getColumnName(i).toLowerCase(), 
                     getColumnHeading(rmd.getColumnName(i)), i, rmd.getColumnDisplaySize(i),
                     precision, rmd.getScale(i), rmd.isNullable(i), isHidden, link);
      c.setPreformat(isPreformat);
                     
      Tracer.log("=================" + c.getColumnName() + " type " + c.getColumnType() + " scale " + c.getColumnScale() + " precision " + precision + " display size " + c.getColumnLength() , Tracer.DEBUG, OBJECT_NAME);
      Tracer.log("=================" + (c.isHidden()?"Hidden":"Not Hidden") , Tracer.DEBUG, OBJECT_NAME);                    

      if ( c.getColumnType().equals("NUMBER") ) c.setJustification(Column.RIGHT);
                     
      columns.add(c);
      columnsHash.put(c.getColumnName(), c);
      
    }      
    
    ps.setColumns(columns);
    ps.setColumnsHash(columnsHash);
    ps.setCharts(charts);
    ps.setDataType(dataType);
    
    // Build the pageset's rows         
    Hashtable row;
    ArrayList rows = new ArrayList();
    int rowCounter = 0;
    int totalRowCounter = 0;

    ArrayList pages = new ArrayList();

    while ( rset.next() && ( totalRowCounter < rowLimit && rowLimit > 0 ) ) {

      row = new Hashtable();  // Row Objects are the column data elements.

      // Build the row
      int maxColSize = 0;
      for ( int i=1; i<=rmd.getColumnCount(); i++ ) {
      	
      	String columnName = rmd.getColumnName(i).toLowerCase();  
      	Object columnValue = null;
      	
        Tracer.log("Column " + i + " Class Name: " + rmd.getColumnClassName(i), Tracer.DEBUG, "PageLoader");
        //Tracer.log("Column " + i + " Width: " + rmd.getPrecision(i), Tracer.DEBUG, "PageLoader");

        if ( rmd.getColumnClassName(i).equals("java.lang.String") ) 
          columnValue = rset.getString(i)==null?"":rset.getString(i);
        
        if ( rmd.getColumnClassName(i).equals("byte[]") ) 
          columnValue = rset.getBytes(i).toString();
        
        if ( rmd.getColumnClassName(i).equals("java.math.BigDecimal") ) 
          columnValue = new BigDecimal(rset.getDouble(i));

        if ( rmd.getColumnClassName(i).equals("java.sql.Timestamp") ) {
          if ( rset.getTimestamp(i) == null ) {
            columnValue = new Timestamp(0);
          } else {
            columnValue = rset.getTimestamp(i);
          }
        }

        if ( rmd.getColumnClassName(i).equals("oracle.sql.TIMESTAMPTZ") ) {
            if ( rset.getTimestamp(i) == null ) {
              columnValue = new Timestamp(0);
            } else {
              columnValue = rset.getTimestamp(i);
            }
        }
        
        if ( rmd.getColumnClassName(i).equals("oracle.sql.CLOB") ) {
            columnValue = rset.getClob(i);
        }
        
        // TODO: Get the size of the current value and check to see if it's bigger than the max.
        // If it is then set the max to be the current value.  This will allow me to 
        // format the ascii output columns dynamically using the Format class.
        
        int currentSize = columnValue.toString().length();
        if ( currentSize > ((Column)columnsHash.get(columnName)).getMaxLength() ) {
        	((Column)columnsHash.get(columnName)).setMaxLength(currentSize);
        }

        row.put(columnName, columnValue);
        
      }

      // Add the row to the current list of rows and increment the row counter.
      rows.add(row);
      rowCounter++;
      totalRowCounter++;

      // If we hit rows per page, then create a page object and set certain attributes from the pageSet...
      if ( rowCounter == rowsPerPage ) { 
        // Create a new page object with the rows in memory and add it to the set
        if ( rows.size() > 0 ) {
          Page page = new Page(rows, sortIndex, columns, columnsHash, keyIndex);
          page.setTitle(ps.getTitle());
          page.setSql(ps.getSql());
          page.setCharts(ps.getCharts());
          page.setDataType(ps.getDataType());
          pages.add(page);
        }
        // Clear the rows Array and reset the counter
        rows = new ArrayList();
        rowCounter = 0;
      }     
    }

    // Create a new page object with the rows in memory and add it to the set
    if ( rows.size() > 0 ) {
      Page page = new Page(rows, sortIndex, columns, columnsHash, keyIndex);
      page.setTitle(ps.getTitle());
      page.setSql(ps.getSql());
      page.setCharts(ps.getCharts());
      page.setDataType(ps.getDataType());
      pages.add(page);
      Tracer.log("Created a page with " + rows.size() + " rows.", Tracer.DEBUG, OBJECT_NAME);     
    }
    
    if ( totalRowCounter == rowLimit ) {
      for ( int i=0; i<pages.size(); i++ ) {
        Page pg = (Page) pages.get(i);
        pg.setPageSetLimitExceeded(true);
        pg.setPageSetLimit(rowLimit);
      }
    }

    ps.setPages(pages);
    
    Tracer.log("Returning a new format page set with " + ps.getPageCount() + " Pages.", Tracer.DEBUG, OBJECT_NAME);

    return ps;

  }
  

  
  static public String getColumnHeading(String cname) {
  
    String rval = "";
    String initCapWord;
    String firstPos;
    String word;
  
    StringTokenizer s = new StringTokenizer(cname, "_");
    int tokenCount = s.countTokens();
    for ( int i=0; i <= s.countTokens(); i++ ) {
      word = s.nextToken();
      firstPos = word.substring(0,1);
      firstPos = firstPos.toUpperCase();    
      word = firstPos + word.substring(1).toLowerCase();
      rval = rval + word + " ";
    }
    
    return rval;

  }
  


}