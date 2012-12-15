
package com.powerdba;

import com.powerdba.chart.ChartGenerator;
import com.powerdba.chart.ChartDAO;
import com.powerdba.chart.OracleMetric;
import com.powerdba.chart.TimeSeriesDataSet;
import com.powerdba.gui.Link;
import com.powerdba.gui.LinkVar;
import com.powerdba.gui.Page;
import com.powerdba.gui.PageGenerator;
import com.powerdba.gui.PageLoader;
import com.powerdba.gui.PageSet;
import com.powerdba.jdbc.ConnectionManager;
import com.powerdba.mvc.PowerDbaActions;
import com.powerdba.mvc.jsp.SelectEntry;
import com.powerdba.util.StringUtility;
import com.powerdba.util.Tracer;
import com.powerdba.util.XmlHandle;

import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

public class PerfStatDAO {

  private static final String OBJECT_NAME = "PerfStatDAO";
  public static final String DATA = "data";
  public static final String CHART = "chart";
  
  static public String getIntervalSQL(long begin, long end, OracleDatabaseConnection database) throws Exception {
      
      StringBuffer sb = new StringBuffer();
      
      Query q = QueryHolder.getQuery(database, "intervalsql");
      q.setBindValue("start_time", new Timestamp(begin));
      q.setBindValue("end_time", new Timestamp(end));
      q.setDatabase(database);
      q.setRacEnabled(true);  // Allows the query to attempt to return multiple pages one for each instance in cluster  
      
      Tracer.log("Processing inteval query: \n" + q.toString(), Tracer.DEBUG, OBJECT_NAME);
      
      ArrayList pageSections = q.getPageSections();
      
      Tracer.log("Processing " + pageSections.size() + " page sections.", Tracer.DEBUG, OBJECT_NAME);
      // Process multiple page sections as setup in the manager for certain pages.  Must have set multiple to true.
      // and setup a pages arraylist.
	    for ( int i=0; i<pageSections.size(); i++ ) {
        Page page = (Page) pageSections.get(i);
        if ( i == 1 ) {
          sb.append("<font size=-1 color=#888888><b>&nbsp;Other instances in the RAC Cluster:</b></font>");
          sb.append("<BR><BR>");
        }
        sb.append(buildOnePageSection(page));
        sb.append("<br>");
	    }

      return sb.toString();
    }
  
  private static String buildOnePageSection(Page page) throws Exception {
      
        String html = new String();
      
        Tracer.log("Building Page Section", Tracer.DEBUG, OBJECT_NAME); 

        try {
          
	        if ( page != null ) {
	      
	          PageGenerator pg = new PageGenerator(page);
	          pg.setColor1("#e5e5e5");
	          pg.setColor2("#f5f5f5");
	          pg.setHeadingBackgoundColor("#73969c");
	          pg.setCellBorderColor("#cccccc");
	          pg.setForm("powerdba");
	          pg.setJsp("powerdba.jsp");
	          pg.setDatabase(page.getDatabaseLink());
            html = pg.getHtmlNew();
	        } 
	        
	        return html;
	      
        } catch ( Exception e ) {
            throw e;
        }
  }

  static public Hashtable getPerfStatStats() throws JDOMException, Exception {
  
    Hashtable statsHash = new Hashtable();

    try {
    
      PerfStatDAO p = new PerfStatDAO();
      InputStream stream = p.getClass().getResourceAsStream("perfstat.xml");

      XmlHandle xmlHandle = new XmlHandle();
      xmlHandle.setInput(stream);
      Document doc = xmlHandle.getDocument();
      Element statistics = doc.getRootElement().getChild("statistics");  

      if ( statistics == null ) throw new JDOMException("Missing outer statistics block in the perfstat.xml file");

      List statisticsList = statistics.getChildren("statistic");

      for ( int i=0; i < statisticsList.size(); i++ ) {
            
        Element element = (Element) statisticsList.get(i);
    
        String id = element.getChild("id").getTextTrim();
        String name = element.getChild("name").getTextTrim();
        String query =  element.getChild("query").getTextTrim();
        String orderBy = element.getChild("orderby").getTextTrim();
        String groupBy = element.getChild("groupby").getTextTrim();
        
        Tracer.log("Creating stat [" + name + "]", Tracer.DEBUG, OBJECT_NAME);

        statsHash.put(new Integer(id), new PerfStatStat(Integer.parseInt(id), name, query, "", orderBy, groupBy));
      }
      
    } catch ( Exception e ) {
      Tracer.log(e, "Error building perfstat data list from xml", Tracer.ERROR, OBJECT_NAME);
      throw e;
    }

    Tracer.log("perfstat hash created...", Tracer.DEBUG, OBJECT_NAME);
    return statsHash;
    
  }
    
  public static String getHtml(long beginId, long endId, PerfStatStat pss, 
                               OracleDatabaseConnection database, String[] selectedValues) throws Exception {
    
    PerfStatSnapshot snap = getSnapshot(database, beginId, endId);
    return getHtmlBySnapId(pss, snap, database, selectedValues);
           
  }
  
  private static String getHtmlBySnapId(PerfStatStat pss, 
                                        PerfStatSnapshot snap, 
                                        OracleDatabaseConnection database,
                                        String[] selectedValues) throws Exception {

    PreparedStatement pstmt = null;
    ResultSet rset          = null;
    
    String SQL = pss.getQuery() + "\n" + "  and a.snap_id = ? and b.snap_id = ? " + " " + 
                 pss.getGroupBy() + " " + pss.getOrderBy();
    
    if ( snap.getEndId() == snap.getBeginId() ) 
      return "<table><tr><td>Only one snapshot exists in this time frame</td></tr></table>";
    
    if ( database.getVersion().getVersion1() > 9 )  SQL = translateToOWR(SQL);
    
    Tracer.log("Perfstat Query: \n " + SQL, Tracer.DEBUG, "");

    try {
    
      pstmt = database.getConn().prepareStatement(SQL, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      pstmt.setLong(1, snap.getBeginId());
      pstmt.setLong(2, snap.getEndId());
      
      try {
        rset  = pstmt.executeQuery();
      } catch ( SQLException e ) {
        throw new SQLException("Perfstat Tables Not Found in this database.  cause= " + e.getMessage());
      }
      
      Hashtable links = new Hashtable();     

      PageSet ps = PageLoader.buildPageSet(rset, 1, 500, 1, links, new ArrayList(), new ArrayList(), pss.getName(), 1000);
                                           
      PageGenerator pgen = new PageGenerator(ps.getPage(1));
      pgen.setColor1("#e5e5e5");
      pgen.setColor2("#f5f5f5");
      pgen.setHeadingBackgoundColor("#73969c");
      pgen.setCellBorderColor("#cccccc");
      pgen.setForm("perfstat");
      pgen.setJsp("perfstat.jsp");
      pgen.setSelectedKeys(selectedValues);
      
      StringBuffer html = new StringBuffer();
      
      html.append(snap.getHtmlDescription());

      html.append(pgen.getHtml());

      return html.toString();
      
    } catch ( SQLException e ) {
      Tracer.log("Error in building html for history", Tracer.ERROR, OBJECT_NAME);
      throw e;
    } finally {
      try {
        if ( pstmt != null ) pstmt.close();
      } catch ( Exception e ) {}
    }

  }   
  
  public static ArrayList getTopNCategories(PerfStatStat pss, PerfStatSnapshot snap, OracleDatabaseConnection database, int n) throws SQLException {

    PreparedStatement pstmt = null;
    ResultSet rset          = null;
    ArrayList list = new ArrayList();
    
    String SQL = pss.getQuery() + "\n" + " and a.snap_id = ? and b.snap_id = ? " + " " + pss.getGroupBy() + " " + pss.getOrderBy();
    if ( database.getVersion().getVersion1() > 9 )  SQL = translateToOWR(SQL);
    
    if ( snap.getBeginId() == snap.getEndId() ) throw new SQLException("Only one snapshot exists in this time frame");
    
    Tracer.log("Perfstat Query: \n " + SQL, Tracer.DEBUG, "");

    try {
    
      pstmt = database.getConn().prepareStatement(SQL, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      pstmt.setLong(1, snap.getBeginId());
      pstmt.setLong(2, snap.getEndId());

      rset  = pstmt.executeQuery();
      for ( int i=1; i<=n && rset.next(); i++ ) {
        list.add(rset.getString(1));
      }
      
      return list;
      
    } catch ( SQLException e ) {
      Tracer.log("Error in building top n list of categories for this time frame.", Tracer.ERROR, OBJECT_NAME);
      throw e;
    } finally {
      try {
        if ( pstmt != null ) pstmt.close();
      } catch ( Exception e ) {}
    }
  }
  
  public static PerfStatSnapshot getSnapshot(OracleDatabaseConnection database, Date startTime, Date endTime) throws SQLException {
  
    Tracer.log("=====Building Snapshot for startTime = " + new Timestamp(startTime.getTime()).toString(), Tracer.DEBUG, OBJECT_NAME);
    Tracer.log("=====Building Snapshot for endTime = " + new Timestamp(endTime.getTime()).toString(), Tracer.DEBUG, OBJECT_NAME);
  
    PreparedStatement pstmt = null;
    ResultSet rset          = null;
    String SQL = null;
    long startSnapId = 0;
    long endSnapId = 0;
           
    PerfStatSnapshot snap = null;  
      
    SQL  = "select snap_id                              \n" +
           "  from stats$snapshot                       \n" +
           "  where snap_id = (select max(snap_id)      \n" +
           "                     from stats$snapshot    \n" +
           "                      where snap_time < ?) \n";
    
    if ( database.getVersion().getVersion1() > 9 )  SQL = translateToOWR(SQL);

    try {
    
      pstmt = database.getConn().prepareStatement(SQL, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      pstmt.setTimestamp(1, new Timestamp(endTime.getTime()));
      rset  = pstmt.executeQuery();    
      if ( rset.next() ) {
        endSnapId = rset.getLong(1);             
      } else {
        throw new SQLException("No Snapshots where found less than the upper time of " + endTime.toString());
      }
      
    } catch ( SQLException e ) {
      Tracer.log("Error in getting ending snap id", Tracer.ERROR, OBJECT_NAME);
      throw e;
    } finally {
      try {
        if ( pstmt != null ) pstmt.close();
      } catch ( Exception e ) {}
    }      
    
    SQL  = "select snap_id                              \n" +
           "  from stats$snapshot                       \n" +
           "  where snap_id = (select min(snap_id)      \n" +
           "                     from stats$snapshot    \n" +
           "                      where snap_time > ?) \n";
    
    if ( database.getVersion().getVersion1() > 9 )  SQL = translateToOWR(SQL);

    try {
    
      pstmt = database.getConn().prepareStatement(SQL, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      pstmt.setTimestamp(1, new Timestamp(startTime.getTime()));
      rset  = pstmt.executeQuery();    
      if ( rset.next() ) {
        startSnapId = rset.getLong(1);             
      } else {
        throw new SQLException("No Snapshots where found greater than the lower time of " + startTime.toString());
      }
      
    } catch ( SQLException e ) {
      Tracer.log("Error in getting ending snap ids from date query", Tracer.ERROR, OBJECT_NAME);
      throw e;
    } finally {
      try {
        if ( pstmt != null ) pstmt.close();
      } catch ( Exception e ) {}
    } 
    
    return PerfStatDAO.getSnapshot(database, startSnapId, endSnapId);
 
  }
  
  public static PerfStatSnapshot getSnapshot(OracleDatabaseConnection database, long beginId, long endId) throws SQLException {
  
      Tracer.log("Getting snapshot object for beginId: " + beginId + " and endId: " + endId, Tracer.DEBUG, OBJECT_NAME);
  
      PreparedStatement pstmt = null;
      ResultSet rset          = null;
      String SQL = null;
      
      SQL = "select snap_time, snap_id, snap_level from stats$snapshot where snap_id = ?";
      
      if ( database.getVersion().getVersion1() > 9 )  SQL = translateToOWR(SQL);         
      PerfStatSnapshot snap = null;               

      try {
      
        pstmt = database.getConn().prepareStatement(SQL, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        pstmt.setLong(1, beginId);
        rset  = pstmt.executeQuery();
        
        if ( rset.next() ) {
          snap = new PerfStatSnapshot(rset.getTimestamp(1), rset.getLong(2), rset.getShort(3));                    
        } else {
          throw new SQLException("No snapshot rows found trying to get the snapshot record for id " + beginId);
        }
        
        if ( pstmt != null ) pstmt.close();
                                                     
        pstmt = database.getConn().prepareStatement(SQL, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        pstmt.setLong(1, endId);
        rset  = pstmt.executeQuery();
        
        if ( rset.next() ) {          
          snap.setEndTime(rset.getTimestamp(1));
          snap.setEndId(rset.getLong(2));
          snap.setEndLevel(rset.getShort(3));
        } else {
          throw new SQLException("No snapshots found trying to get the snapshot record for id " + endId);
        }
        
      } catch ( SQLException e ) {
        Tracer.log("Error in building perfstat snapshot object", Tracer.ERROR, OBJECT_NAME);
        throw e;
      } finally {
        try {
          if ( pstmt != null ) pstmt.close();
        } catch ( Exception e ) {}
      }
      
      Tracer.log("Returning snap: " + snap.toString(), Tracer.DEBUG, "");
      
      return snap;
    
  }
  
  static public String getDatabaseTime(OracleDatabaseConnection database) throws SQLException {

      StringBuffer sb = new StringBuffer();

      PreparedStatement pstmt = null;
      ResultSet rset          = null;
      String SQL = "select to_char(sysdate,'dd-mon-yyyy hh:miAM') from dual";

      try {
          pstmt = database.getConn().prepareStatement(SQL, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
          rset  = pstmt.executeQuery();
          rset.next();
          sb.append(rset.getString(1));
      } catch ( SQLException e ) {
          Tracer.log("Error in getting database date/time", Tracer.ERROR, OBJECT_NAME);
          throw e;
      } finally {
        try {
            if ( pstmt != null ) pstmt.close();
        } catch ( Exception e ) {}
      }
      return sb.toString();
  }
  
  static public ArrayList getSnapList(OracleDatabaseConnection database) throws SQLException {

    ArrayList list = new ArrayList();

    PreparedStatement pstmt = null;
    ResultSet rset          = null;
    String SQL = "select snap_id, to_char(snap_time,'dd-mon-yyyy hh:miam'), snap_time from stats$snapshot " +
                 "  where snap_time > sysdate - 3 " +
                 " union all " +
                 "select 0, '<b>Instance Startup</b>', startup_time from v$instance " +
                 "  order by 3";
    
    if ( database.getVersion().getVersion1() > 9 )  SQL = translateToOWR(SQL);
    
    Tracer.log(SQL, Tracer.DEBUG, OBJECT_NAME);

    try {
        pstmt = database.getConn().prepareStatement(SQL, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        rset  = pstmt.executeQuery();
        while (rset.next()) {
          list.add(new SelectEntry(rset.getString(1), rset.getString(2)));
        }
    } catch ( SQLException e ) {
        Tracer.log("Error in getting snap lists", Tracer.ERROR, OBJECT_NAME);
        throw e;
    } finally {
        try {
            if ( pstmt != null ) pstmt.close();
        } catch ( Exception e ) {}
    }
    return list;
  }
  
  static public boolean isPerfstatInstalled(OracleDatabaseConnection database) throws SQLException {
  
    boolean rval = true;

    if ( database.getVersion().getVersion1() < 10 ) {
    
	    PreparedStatement pstmt = null;
	    ResultSet rset          = null;
	    String SQL = "select count(*) from all_tables where table_name = 'STATS$SNAPSHOT'";
	
	    try {
	        pstmt = database.getConn().prepareStatement(SQL, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
	        rset  = pstmt.executeQuery();
	        rset.next();
	        if ( rset.getInt(1) == 0 ) {
	          rval = false;
	        }
	    } catch ( SQLException e ) {
	        Tracer.log("Error checking for perfstat tables.", Tracer.ERROR, OBJECT_NAME);
	        throw e;
	    } finally {
	        try {
	            if ( pstmt != null ) pstmt.close();
	        } catch ( Exception e ) {}
	    }
    }
    
    return rval;
  }
  
  // Builds and returns the html for either the chart of the data table html based on the snapshot, the stat number, and page type
  static public String getDetailHtml(int stat, 
                                     PerfStatSnapshot snap,
                                     String[] keys, 
                                     OracleDatabaseConnection database, 
                                     String pageType)
    throws SQLException, Exception {
    
    String rval = null;

    try {
      if ( pageType.equals(PerfStatDAO.DATA) ) {
        rval = ChartGenerator.getDataHtml(getPerfStatTSDataset(stat, snap, keys, database), snap);
      } else {
        rval = ChartGenerator.getChartHtml(getPerfStatTSDataset(stat, snap, keys, database), 
                                           ChartDAO.buildChartURL(PowerDbaActions.PERFSTAT_DETAIL2, database.getName(), stat, keys),
                                           snap);      
      }
    }
    catch (Exception e){
      Tracer.log("Error building the page to display detail info about wait event group", Tracer.ERROR, OBJECT_NAME);
      throw e;
    }
    
    return rval;
  }

  static public TimeSeriesDataSet getPerfStatTSDataset(int stat, 
                                                       PerfStatSnapshot snap, 
                                                       ArrayList keys, 
                                                       OracleDatabaseConnection database) throws Exception {
    
    // Translate the ArrayList to an array and call getPerfStatTSDataset
    String[] categories = new String[keys.size()];  
    for ( int i=0; i<keys.size(); i++ ) {
      categories[i] = (String) keys.get(i);
    }  
    return getPerfStatTSDataset(stat, snap, categories, database);
  }
  
  static public Timestamp getEarliestOwrSnap(OracleDatabaseConnection database) throws Exception {
      
      PreparedStatement pstmt = null;
      ResultSet rset = null;
      String SQL = null;
      Timestamp rval = null;
	        
      try {
      
			  SQL = "select min(end_time) from v$sysmetric_history " +
			        "  where end_time > sysdate - (2/24) and group_id in (1,2)";
			  
			  pstmt = database.getConn().prepareStatement(SQL, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			  rset  = pstmt.executeQuery();
			  rset.next();
			  rval = rset.getTimestamp(1);
          
      } catch ( Exception e ) {
          Tracer.log(e, "Error getting the minimum snapshot time from the owr tables", Tracer.ERROR, OBJECT_NAME);
          throw e;
      } finally {
          try {
            if ( pstmt != null ) pstmt.close();
          } catch ( Exception e ) {}
      }
      
	    return rval;
      
  }
  
  static public TimeSeriesDataSet getOWRTSDatasetRAC(ArrayList metrics, PerfStatSnapshot owrSnap, OracleDatabaseConnection database) throws Exception {
      
      TimeSeriesCollection tsCollection = new TimeSeriesCollection();
      TimeSeries timeSeries = null;
      PreparedStatement pstmt = null;
      ResultSet rset = null;
      String SQL = null;
  
      OracleMetric metric = (OracleMetric) metrics.get(0);
      
      ArrayList databases = database.getAllDescriptors();
      //databases.add(database.getName());
      OracleDatabaseConnection chartDb = null;
      
      for ( int i=0; i<databases.size(); i++ )  {
      	
        String descriptor = (String) databases.get(i);
        
	      try {
	      	
          timeSeries = new TimeSeries(descriptor, Second.class);
          
          chartDb = ConnectionManager.getDatabase(descriptor);
	      
				  SQL = "select end_time, value     " + 
				        "  from v$sysmetric_history " +
				        "  where metric_name = ?    " +
				        "    and group_id = ?       " +
				        "    and end_time > ?       " +
				        "    and group_id in (1,2)  " ;
				  
				  pstmt = chartDb.getConn().prepareStatement(SQL, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
					pstmt.setString(1, metric.getMetricName());
					pstmt.setInt(2, metric.getGroupId());
					pstmt.setTimestamp(3, owrSnap.getBeginTime());
				  rset  = pstmt.executeQuery();
				
				  for ( int j=0; rset.next(); j++ ) { 
				    timeSeries.add(new Second(new Date(rset.getTimestamp(1).getTime())), rset.getLong(2));
				  }
				  
		      tsCollection.addSeries(timeSeries);
	          
	      } catch ( Exception e ) {
	          Tracer.log(e, "Error building RAC time series for " + metric.getGroupName() + ":" + metric.getMetricName(), Tracer.ERROR, OBJECT_NAME);
	          //throw e;
	      } finally {          
	          try {
	            if ( pstmt != null ) pstmt.close();
	          } catch ( Exception e ) {}
	          
            if ( chartDb != null && chartDb.getConn() != null ) {  
	    	        try {
	    	          chartDb.getConn().close();
	      	      } catch ( Exception e ) {}
              }   
	      }
	           
      }
      
	    return new TimeSeriesDataSet(tsCollection); 
	    
  }

  static public TimeSeriesDataSet getOWRTSDataset(ArrayList metrics, PerfStatSnapshot owrSnap, OracleDatabaseConnection database) throws Exception {
      
      TimeSeriesCollection tsCollection = new TimeSeriesCollection();
      TimeSeries timeSeries = null;
      PreparedStatement pstmt = null;
      ResultSet rset = null;
      String SQL = null;
 
	    for ( int i=0; i<metrics.size(); i++ ) {	        

	      OracleMetric metric = (OracleMetric) metrics.get(i);
	      timeSeries = new TimeSeries(metric.getMetricName(), Second.class);
	        
	      try {
	      
				  SQL = "select end_time, value     " + 
				        "  from v$sysmetric_history " +
				        "  where metric_name = ?    " +
				        "    and group_id = ?       " +
				        "    and end_time > ?       " +
				        "    and group_id in (1,2)  " ;
				  
				  pstmt = database.getConn().prepareStatement(SQL, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
					pstmt.setString(1, metric.getMetricName());
					pstmt.setInt(2, metric.getGroupId());
					pstmt.setTimestamp(3, owrSnap.getBeginTime());
				  rset  = pstmt.executeQuery();
				
				  for ( int j=0; rset.next(); j++ ) { 
				    timeSeries.add(new Second(new Date(rset.getTimestamp(1).getTime())), rset.getLong(2));
				  }
	          
	      } catch ( Exception e ) {
	          Tracer.log(e, "Error building time series for " + metric.getGroupName() + ":" + metric.getMetricName(), Tracer.ERROR, OBJECT_NAME);
	          throw e;
	      } finally {
	          try {
	            if ( pstmt != null ) pstmt.close();
	          } catch ( Exception e ) {}
	      }
	      
	      tsCollection.addSeries(timeSeries);
	      
	    }
	    
	    return new TimeSeriesDataSet(tsCollection); 
  }
  
  static public TimeSeriesDataSet getPerfStatTSDataset(int stat, 
                                                       PerfStatSnapshot snap, 
                                                       String[] keys, 
                                                       OracleDatabaseConnection database) throws Exception {

    PreparedStatement pstmt = null;
    ResultSet rset = null;
    String title = null;
    String uom = null;
    TimeSeriesCollection dataset = null;
    TimeSeries timeSeries = null;
    String SQL;
    float value = 0f;
    float heldValue = 0f;
    long heldTime = 0;
    long interval = 0;
    //Tracer.log("Snap Interval (seconds) = " + snap.getDurationSeconds() + " stat = " + stat, Tracer.DEBUG, OBJECT_NAME);

    try {

      switch (stat) {
      
        case 1001:
        
          title = "System IO";
          uom = "IOs/Sec";
          
          SQL =  "select snap_time, sum(t1.phyrds + t1.phywrts) " +
                 "  from stats$filestatxs t1, " +
                 "       stats$snapshot snap " +
                 "  where t1.snap_id = snap.snap_id " +
                 "    and t1.snap_id between ? and ? " +
                 "    group by snap.snap_time " +
                 "    order by snap.snap_time";
          
          if ( database.getVersion().getVersion1() > 9 )  SQL = translateToOWR(SQL);
        
          dataset = new TimeSeriesCollection();              
          timeSeries = new TimeSeries(keys[0], Second.class);
                              
          try {
        
            pstmt = database.getConn().prepareStatement(SQL, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            pstmt.setLong(1, snap.getBeginId());
            pstmt.setLong(2, snap.getEndId());
            rset  = pstmt.executeQuery();
            heldValue = 0;
            heldTime = 0;
            for ( int j=0; rset.next(); j++ ) {
              if ( j>0 ) {
                interval = (rset.getTimestamp(1).getTime() - heldTime)/1000;
                value = ((rset.getFloat(2) - heldValue) / interval);
                if ( value >= 0 ) {
                  timeSeries.add(new Second(new Date(rset.getTimestamp(1).getTime())), value);
                }
              }
              heldValue = rset.getFloat(2);
              heldTime = rset.getTimestamp(1).getTime();

            }
            
          } catch ( Exception e ) {
            Tracer.log(e, "Error building time series for " + keys[0], Tracer.ERROR, OBJECT_NAME);
            throw e;
          } finally {
            try {
              if ( pstmt != null ) pstmt.close();
            } catch ( Exception e ) {}
          }
          
          dataset.addSeries(timeSeries);
 
          break;
          
      
        case 504:
        
          title = "Oracle System Wait Events";
          uom = "Time Waited/Sec";
          
          SQL =  "select snap_time, a.time_waited_micro/1000000 " +
                 "  from stats$system_event a, " +
                 "       stats$snapshot snap " +
                 "  where a.snap_id = snap.snap_id " +
                 "    and a.event = ? " +
                 "    and a.snap_id between ? and ? " +
                 "    order by snap.snap_time";
          
          if ( database.getVersion().getVersion1() > 9 )  SQL = translateToOWR(SQL);
        
          dataset = new TimeSeriesCollection();
        
          for ( int i=0; i<keys.length; i++) {
        
            timeSeries = new TimeSeries(keys[i], Second.class); 
                                
            try {
          
              pstmt = database.getConn().prepareStatement(SQL, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
              pstmt.setString(1, keys[i].trim());
              pstmt.setLong(2, snap.getBeginId());
              pstmt.setLong(3, snap.getEndId());
              rset  = pstmt.executeQuery();
              heldValue = 0;
              for ( int j=0; rset.next(); j++ ) {
                if ( j>0 ) {
                  interval = (rset.getTimestamp(1).getTime() - heldTime)/1000;
                  value = ((rset.getFloat(2) - heldValue) / interval);
                  if ( value >= 0 ) {
                    timeSeries.add(new Second(new Date(rset.getTimestamp(1).getTime())), value);
                  }
                }
                heldValue = rset.getFloat(2);
                heldTime = rset.getTimestamp(1).getTime();
              }
              
            } catch ( Exception e ) {
              Tracer.log(e, "Error building time series for " + keys[i], Tracer.ERROR, OBJECT_NAME);
              throw e;
            } finally {
              try {
                if ( pstmt != null ) pstmt.close();
              } catch ( Exception e ) {}
            }
            
            dataset.addSeries(timeSeries);

          }
 
          break;
          
        case 1002:
        
          title = "IO by drive";
          uom = "IOs/Sec";
        
          dataset = new TimeSeriesCollection();
        
          for ( int i=0; i<keys.length; i++) {
        
            timeSeries = new TimeSeries(keys[i], Second.class); 
          
            SQL =  "select snap.snap_time, " +
                   "       sum(t1.phyrds + t1.phywrts) " +
                   "  from stats$filestatxs t1, " +
                   "       stats$snapshot snap " +
                   "  where t1.snap_id = snap.snap_id " +
                   "    and substr(t1.filename, 1, 4) = ? " +
                   "    and t1.snap_id between ? and ? " +
                   "  group by snap.snap_time" +   
                   "  order by snap.snap_time";
            
            if ( database.getVersion().getVersion1() > 9 )  SQL = translateToOWR(SQL);
                         
            try {
          
              pstmt = database.getConn().prepareStatement(SQL, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
              pstmt.setString(1, keys[i].trim());
              pstmt.setLong(2, snap.getBeginId());
              pstmt.setLong(3, snap.getEndId());
              rset  = pstmt.executeQuery();
              heldValue = 0;
              for ( int j=0; rset.next(); j++ ) {
                if ( j>0 ) {
                  interval = (rset.getTimestamp(1).getTime() - heldTime)/1000;
                  value = ((rset.getFloat(2) - heldValue) / interval);
                  if ( value >= 0 ) {
                    timeSeries.add(new Second(new Date(rset.getTimestamp(1).getTime())), value);
                  }
                }
                heldValue = rset.getFloat(2);
                heldTime = rset.getTimestamp(1).getTime();
              }
              
            } catch ( Exception e ) {
              Tracer.log(e, "Error building time series for " + keys[i], Tracer.ERROR, OBJECT_NAME);
              throw e;
            } finally {
              try {
                if ( pstmt != null ) pstmt.close();
              } catch ( Exception e ) {}
            }
            
            dataset.addSeries(timeSeries);

          }
       
          break;

          
        case 501:
        
          title = "Data File IO";
          uom = "IOs/Sec";
        
          dataset = new TimeSeriesCollection();
        
          for ( int i=0; i<keys.length; i++) {
        
            timeSeries = new TimeSeries(keys[i], Second.class); 
          
            SQL =  "select snap.snap_time, " +
                   "       t1.phyrds + t1.phywrts " +
                   "  from stats$filestatxs t1, " +
                   "       stats$snapshot snap " +
                   "  where t1.snap_id = snap.snap_id " +
                   "    and t1.filename = ? " +
                   "    and t1.snap_id between ? and ? " +
                   "    order by snap.snap_time";
            
            if ( database.getVersion().getVersion1() > 9 )  SQL = translateToOWR(SQL);
                         
            try {
          
              pstmt = database.getConn().prepareStatement(SQL, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
              pstmt.setString(1, keys[i].trim());
              pstmt.setLong(2, snap.getBeginId());
              pstmt.setLong(3, snap.getEndId());
              rset  = pstmt.executeQuery();
              heldValue = 0;
              for ( int j=0; rset.next(); j++ ) {
                if ( j>0 ) {
                  interval = (rset.getTimestamp(1).getTime() - heldTime)/1000;
                  value = ((rset.getFloat(2) - heldValue) / interval);
                  if ( value >= 0 ) {
                    timeSeries.add(new Second(new Date(rset.getTimestamp(1).getTime())), value);
                  }
                }
                heldValue = rset.getFloat(2);
                heldTime = rset.getTimestamp(1).getTime();
              }
              
            } catch ( Exception e ) {
              Tracer.log(e, "Error building time series for " + keys[i], Tracer.ERROR, OBJECT_NAME);
              throw e;
            } finally {
              try {
                if ( pstmt != null ) pstmt.close();
              } catch ( Exception e ) {}
            }
            
            dataset.addSeries(timeSeries);

          }
       
          break;
        
        case 509:
        
          title = "Tablespace IO";
          uom = "IOs/Sec";
          
          SQL =  "select snap.snap_time, " +
                 "       sum(t1.phyrds + t1.phywrts) " +
                 "  from stats$filestatxs t1, " +
                 "       stats$snapshot snap " +
                 "  where t1.snap_id = snap.snap_id " +
                 "    and t1.tsname = ? " +
                 "    and t1.snap_id between ? and ? " +
                 "  group by t1.tsname, snap.snap_id, t1.snap_id, snap.snap_time " +
                 "  order by snap.snap_time";
          
          if ( database.getVersion().getVersion1() > 9 )  SQL = translateToOWR(SQL);
        
          dataset = new TimeSeriesCollection();
        
          for ( int i=0; i<keys.length; i++) {
        
            timeSeries = new TimeSeries(keys[i], Second.class); 
                         
            try {
          
              pstmt = database.getConn().prepareStatement(SQL, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
              pstmt.setString(1, keys[i].trim());
              pstmt.setLong(2, snap.getBeginId());
              pstmt.setLong(3, snap.getEndId());
              rset  = pstmt.executeQuery();
              heldValue = 0;
              for ( int j=0; rset.next(); j++ ) {
                if ( j>0 ) {
                  interval = (rset.getTimestamp(1).getTime() - heldTime)/1000;
                  value = ((rset.getFloat(2) - heldValue) / interval);
                  if ( value >= 0 ) {
                    timeSeries.add(new Second(new Date(rset.getTimestamp(1).getTime())), value);
                  }
                }
                heldValue = rset.getFloat(2);
                heldTime = rset.getTimestamp(1).getTime();
              }
              
            } catch ( Exception e ) {
              Tracer.log(e, "Error building time series for " + keys[i], Tracer.ERROR, OBJECT_NAME);
              throw e;
            } finally {
              try {
                if ( pstmt != null ) pstmt.close();
              } catch ( Exception e ) {}
            }
            
            dataset.addSeries(timeSeries);

          }
        
          break;
          
        case 508:
        
          title = "Oracle Statistics";
          uom = "Number/Sec";
          
          SQL =  "select snap.snap_time, " +
                 "       value " +
                 "  from stats$sysstat a, " +
                 "       stats$snapshot snap " +
                 "  where a.snap_id = snap.snap_id " +
                 "    and a.name = ? " +
                 "    and a.snap_id between ? and ? " +
                 "  order by snap.snap_time";
          
          if ( database.getVersion().getVersion1() > 9 )  SQL = translateToOWR(SQL);
        
          dataset = new TimeSeriesCollection();
        
          for ( int i=0; i<keys.length; i++) {
        
            timeSeries = new TimeSeries(keys[i], Second.class); 
                                  
            try {
          
              pstmt = database.getConn().prepareStatement(SQL, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
              pstmt.setString(1, keys[i].trim());
              pstmt.setLong(2, snap.getBeginId());
              pstmt.setLong(3, snap.getEndId());
              rset  = pstmt.executeQuery();
              heldValue = 0;
              for ( int j=0; rset.next(); j++ ) {
                if ( j>0 ) {
                  interval = (rset.getTimestamp(1).getTime() - heldTime)/1000;
                  value = ((rset.getFloat(2) - heldValue) / interval);
                  if ( value >= 0 ) {
                    timeSeries.add(new Second(new Date(rset.getTimestamp(1).getTime())), value);
                  }
                }
                heldValue = rset.getFloat(2);
                heldTime = rset.getTimestamp(1).getTime();
              }
              
            } catch ( Exception e ) {
              Tracer.log(e, "Error building time series for " + keys[i], Tracer.ERROR, OBJECT_NAME);
              throw e;
            } finally {
              try {
                if ( pstmt != null ) pstmt.close();
              } catch ( Exception e ) {}
            }
            
            dataset.addSeries(timeSeries);

          }
        
          break;
          
        case 503:
        
          title = "Oracle Buffer Waits";
          uom = "Wait Count";
          
          SQL =  "select snap.snap_time, " +
                 "       wait_count " +
                 "  from stats$waitstat t1, " +
                 "       stats$snapshot snap " +
                 "  where t1.snap_id = snap.snap_id " +
                 "    and t1.class = ? " +
                 "    and t1.snap_id between ? and ? " +
                 "  order by snap.snap_time";
        
          if ( database.getVersion().getVersion1() > 9 )  SQL = translateToOWR(SQL);
        
          dataset = new TimeSeriesCollection();
        
          for ( int i=0; i<keys.length; i++) {
        
            timeSeries = new TimeSeries(keys[i], Second.class); 
            
            try {
          
              pstmt = database.getConn().prepareStatement(SQL, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
              pstmt.setString(1, keys[i].trim());
              pstmt.setLong(2, snap.getBeginId());
              pstmt.setLong(3, snap.getEndId());
              rset  = pstmt.executeQuery();
              heldValue = 0;
              for ( int j=0; rset.next(); j++ ) {
                if ( j>0 ) {
                  interval = (rset.getTimestamp(1).getTime() - heldTime)/1000;
                  //Tracer.log("========================= Interval = " + interval, Tracer.DEBUG, OBJECT_NAME);
                  value = ((rset.getFloat(2) - heldValue) / interval);
                  if ( value >= 0 ) {
                    timeSeries.add(new Second(new Date(rset.getTimestamp(1).getTime())), value);
                  }
                }
                heldValue = rset.getFloat(2);
                heldTime = rset.getTimestamp(1).getTime();
              }
              
            } catch ( SQLException e ) {
              Tracer.log(e, "Error building time series for " + keys[i], Tracer.ERROR, OBJECT_NAME);
              throw e;
            } finally {
              try {
                if ( pstmt != null ) pstmt.close();
              } catch ( Exception e ) {}
            }
            
            dataset.addSeries(timeSeries);

          }
        
          break;
          
      }
          
    } catch ( Exception e ) {
        Tracer.log(e, "Error building Time Series Dataset", Tracer.ERROR, OBJECT_NAME);
        throw e;
    } finally {
      try {
          if ( pstmt != null ) pstmt.close();
      } catch ( Exception e ) {}
    } 
    
    Tracer.log("Returning Time Series Data Set " + title, Tracer.DEBUG, OBJECT_NAME);
    
    return new TimeSeriesDataSet(title, uom, dataset);
    
  }
  
  public static long translateStartSnap(OracleDatabaseConnection database, long snapId, int hoursBack) throws SQLException {
    long rval = 0;
    
    if ( snapId == 0 ) {

      Tracer.log("Getting the snap id back from " + hoursBack + " hours ago.", Tracer.DEBUG, OBJECT_NAME);

      PreparedStatement pstmt = null;
      ResultSet rset = null;
      String SQL = "select min(snap_id) \n" +
                   "  from stats$snapshot \n" +
                   "  where snap_time > (select greatest( min(snap_time), vi.startup_time ) \n" +
                  "                        from stats$snapshot s, \n" + 
                  "                             v$instance vi \n" +
                  "                        where s.snap_time > sysdate - (?/24) \n" +
                  "                        group by vi.startup_time)";
    
      try {    
        pstmt = database.getConn().prepareStatement(SQL, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        pstmt.setInt(1, hoursBack);
        rset  = pstmt.executeQuery();
        if ( rset.next() ) {
          rval = rset.getLong(1);
        } else {
          rval = 0;
        }
      } catch ( SQLException e ) {
        String msg="Error getting default start snap id.  " + e.getMessage();
        Tracer.log(e, msg, Tracer.ERROR, OBJECT_NAME);
        throw new SQLException(msg);
      } finally {
        try {
          if ( pstmt != null ) pstmt.close();
        } catch ( Exception e ) {}
      }     

    } else {
    
      rval = snapId;

    }
    
    Tracer.log("Returning snapId of " + rval, Tracer.DEBUG, OBJECT_NAME);
    return rval;
    
  }
  
  public static long translateEndSnap(OracleDatabaseConnection database, long snapId) throws SQLException {
    long rval;
    
    if ( snapId == 0 ) {
    
      PreparedStatement pstmt = null;
      ResultSet rset = null;
      
      String SQL = "select max(snap_id) from stats$snapshot";
    
      try {
    
        pstmt = database.getConn().prepareStatement(SQL, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        rset  = pstmt.executeQuery();
        rset.next();
        rval = rset.getLong(1);
      } catch ( SQLException e ) {
        Tracer.log(e, "Error getting default end snap id", Tracer.ERROR, OBJECT_NAME);
        throw e;
      } finally {
        try {
          if ( pstmt != null ) pstmt.close();
        } catch ( Exception e ) {}
      }     
    } else {
      rval = snapId;
    }
    return rval;
    
  }   
  
  private static String translateToOWR(String queryIn) {
      
      String SQL = queryIn;
      SQL = StringUtility.replace(SQL, "stats$", "dba_hist_");
      SQL = StringUtility.replace(SQL, "snap_time","end_interval_time");
      SQL = StringUtility.replace(SQL, "stats$", "dba_hist_");
      SQL = StringUtility.replace(SQL, "snap_time","end_interval_time");
      SQL = StringUtility.replace(SQL, "a.event","a.event_name");
      SQL = StringUtility.replace(SQL, "b.event","b.event_name");
      SQL = StringUtility.replace(SQL, "a.name","a.stat_name");
      SQL = StringUtility.replace(SQL, "b.name","b.stat_name");
      SQL = StringUtility.replace(SQL, "a.statistic#","a.stat_id");
      SQL = StringUtility.replace(SQL, "b.statistic#","b.stat_id");
      
      return SQL;
  }


}