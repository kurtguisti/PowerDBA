package com.powerdba.chart;
import com.powerdba.OracleDatabaseConnection;
import com.powerdba.mvc.PowerDbaActions;
import com.powerdba.util.Tracer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import org.jfree.data.DefaultPieDataset;
import org.jfree.data.time.Minute;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

public class ChartDAO {

  private static final String OBJECT_NAME = "ChartDAO";

  public ChartDAO(){}
  
  // This method allows us to get a pie dataset by simply passing the db name and the chartname.
  // It opens a new connection and then closes it.
  public static DefaultPieDataset getPieDataSet(String dbName, String chartName) throws SQLException {
      
    OracleDatabaseConnection db = null;
    Chart chart = null;
      
	  try {
	    db = new OracleDatabaseConnection(dbName);
	    chart = ChartHolder.getChart(chartName);
	    return getPieDataSet(db, chart);
	  } catch ( Exception e ) {
	    throw new SQLException(e.getMessage());
	  } finally {
      try {
        db.getConn().close();  // Close (release back to the pool) the db connection opened on behalf of this method.
      } catch (SQLException e1) {}
	  }
  }
  
  public static DefaultPieDataset getPieDataSet(OracleDatabaseConnection database, Chart chart) throws SQLException {
  
    DefaultPieDataset pieDataset = new DefaultPieDataset();

    PreparedStatement pstmt = null;
    ResultSet rset = null;
    
    int topN = chart.getTopN();
    if ( topN == 0 ) topN = 999999;
    
    try {
      pstmt = database.getConn().prepareStatement(chart.getQuery());
      rset  = pstmt.executeQuery();
      for ( int i=1; rset.next() && i<=topN; i++ ) {
        pieDataset.setValue(rset.getString(1), new Integer(rset.getInt(2)));
      }
    } catch ( SQLException e ) {
      Tracer.log("Error in building pie dataset from query: \n" + chart.getQuery(), Tracer.ERROR, OBJECT_NAME);
      throw e;
    } finally {
      try {
        if ( pstmt != null ) pstmt.close();
      } catch ( Exception e ) {}
    }
    
    Tracer.log("Returning pie dataset", Tracer.DEBUG, OBJECT_NAME);
    
    return pieDataset;
    
  }
  
  // This method allows us to get a tsdataset by simply passing the db name and the chartname.
  // It opens a new connection and then closes it.
  public static TimeSeriesCollection getTsDataSet(String dbName, String chartName) throws SQLException {
      
    OracleDatabaseConnection db = null;
    Chart chart = null;
      
	  try {
	    db = new OracleDatabaseConnection(dbName);
	    chart = ChartHolder.getChart(chartName);
	    return getTsDataSet(db, chart);
	  } catch ( Exception e ) {
	    throw new SQLException(e.getMessage());
	  } finally {
	     try {
	       db.getConn().close();  // Close (release back to the pool) the db connection opened on behalf of this method.
	     } catch (SQLException e1) {}
	  }
  }
  
  public static TimeSeriesCollection getTsDataSet(OracleDatabaseConnection database, Chart chart) throws SQLException {
  
    TimeSeriesCollection dataset = new TimeSeriesCollection();
    TimeSeries series = null;
        
    if ( chart.getCategoryQuery() == null ) 
        throw new SQLException("You must define a category query for chart " + chart.getName());

    ArrayList categories = getTimeSeriesCategories(database, chart.getCategoryQuery());
    
    for ( int i=0; i<categories.size(); i++ ) {
      String category = (String) categories.get(i);
    
      if ( chart.getTimeInterval().indexOf("Minute") != 0 ) {
        series = new TimeSeries(category, Minute.class);      
      } else if ( chart.getTimeInterval().indexOf("Second") != 0 ) {
        series = new TimeSeries(category, Second.class);      
      }
      
      PreparedStatement pstmt = null;
      ResultSet rset = null;  
      
      try {
      
        pstmt = database.getConn().prepareStatement(chart.getQuery());
        rset  = pstmt.executeQuery();
        while ( rset.next() ) {
          if ( chart.getTimeInterval().indexOf("Minute") != -1 ) {
            series.add(new Minute(new Date(rset.getTimestamp(1).getTime())), rset.getLong(2));      
          } else if ( chart.getTimeInterval().indexOf("Second") != -1 ) {
            series.add(new Second(new Date(rset.getTimestamp(1).getTime())), rset.getLong(2));      
          }
        } 
        
        dataset.addSeries(series);
        
      } catch ( SQLException e ) {
        Tracer.log("Error accessing database to get time series data", Tracer.ERROR, OBJECT_NAME);
        throw e;
      } finally {
        try {
          if ( pstmt != null ) pstmt.close();
        } catch ( Exception e ) {}
      }
      
    }
    
    return dataset;
    
  }
  
  public static ArrayList getTimeSeriesCategories(OracleDatabaseConnection database, String query) throws SQLException {
  
    ArrayList categories = new ArrayList();
    PreparedStatement pstmt = null;
    ResultSet rset = null;  
    
    try {
    
      pstmt = database.getConn().prepareStatement(query);
      rset  = pstmt.executeQuery();
      while ( rset.next() ) {
        categories.add(rset.getString(1));
        Tracer.log("Adding Time Series Catogory from category query: " + rset.getString(1), Tracer.DEBUG, OBJECT_NAME);
      }
      
    } catch ( SQLException e ) {
      Tracer.log("Error accessing database to get time series categories", Tracer.ERROR, OBJECT_NAME);
      throw e;
    } finally {
      try {
        if ( pstmt != null ) pstmt.close();
      } catch ( Exception e ) {}
    }     
    
    return categories;
           
  }
  
  public static ArrayList getOWRTimeSeriesMetrics(OracleDatabaseConnection database, ArrayList metricList) throws SQLException {
      
    ArrayList categories = new ArrayList();
    PreparedStatement pstmt = null;
    ResultSet rset = null;
    String SQL = null;
    
    Tracer.log("metricList = " + metricList + " size= " + metricList.size(), Tracer.DEBUG, OBJECT_NAME);
        
    SQL = "select metric_name, group_name, metric_id, group_id " +
          "  from sys.v_$metricname " +
          "  where metric_id = ? and group_id in  (1,2)";  
  
	  for ( int i=0; i<metricList.size(); i++ ) {
	
	    try {
	      
        pstmt = database.getConn().prepareStatement(SQL);
	      pstmt.setInt(1, Integer.parseInt((String)metricList.get(i)));
	      rset  = pstmt.executeQuery();
	      while ( rset.next() ) {
	        categories.add(new OracleMetric(rset.getString(1),rset.getString(2), rset.getInt(3), rset.getInt(4)));
	        Tracer.log("Adding Time Series Metric to list: " + rset.getString(1), Tracer.DEBUG, OBJECT_NAME);
	      }
	  
		  } catch ( SQLException e ) {
		    Tracer.log(e, "Error accessing database to get OWR time series metric names", Tracer.ERROR, OBJECT_NAME);
		    throw e;
		  } finally {
		    try {
		      if ( pstmt != null ) pstmt.close();
		    } catch ( Exception e ) {}
		  }
		    
	  }
    
    return categories;
           
  }
  
  public static String buildChartURL(int action, String database, int stat, String[] categories) {
  
    StringBuffer sb = new StringBuffer();

    sb.append("perfstat.jsp?formaction="+PowerDbaActions.PERFSTAT_DETAIL2+"&database="+database+"&stat="+stat);
    
    for ( int i=0; i<categories.length; i++ ) {
      sb.append("&category" + (i+1) + "=" + categories[i]);
    }
    
    return sb.toString();  
  }
  
  public static String buildChartURL(int action, String database, int stat, TimeSeriesDataSet dataset) {
  
    StringBuffer sb = new StringBuffer();

    sb.append("perfstat.jsp?formaction="+PowerDbaActions.PERFSTAT_DETAIL2+"&database="+database+"&stat="+stat);
    
    for ( int i=0; i<dataset.getSeriesCount(); i++ ) {
      sb.append("&category" + (i+1) + "=" + dataset.getSeries(i).getName());
    }
    
    return sb.toString(); 
  }
  
  public static String buildChartURL(String action, String database) throws Exception {
  
    String rval = null;
    try {
      if ( action != null ) {
        rval = "powerdba.jsp?formaction="+PowerDbaActions.getPageId(action)+"&database="+database;
      }
    } catch ( Exception e ) {
      throw e;
    }
    return rval;
  }
  
  public static String buildChartURL(String action, String chart, String database) throws Exception {
      
    String rval = null;
    try {
      if ( action != null ) {
        rval = "chartZoom.jsp?formaction="+PowerDbaActions.getPageId(action)+"&database="+database+"&chartid="+chart;
      }
    } catch ( Exception e ) {
      throw e;
    }
    return rval;
  }
  
  
  
  
}