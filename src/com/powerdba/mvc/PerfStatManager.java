package com.powerdba.mvc;

import com.powerdba.OracleDatabaseConnection;
import com.powerdba.OracleVersion;
import com.powerdba.PerfStatDAO;
import com.powerdba.PerfStatSnapshot;
import com.powerdba.PerfStatStat;
import com.powerdba.ProcessDAO;
import com.powerdba.chart.ChartDAO;
import com.powerdba.chart.ChartHolder;
import com.powerdba.chart.ChartGenerator;
import com.powerdba.chart.Chart;
import com.powerdba.chart.TimeSeriesDataSet;
import com.powerdba.jdbc.ConnectionManager;
import com.powerdba.jdbc.DbConfig;
import com.powerdba.jdbc.DbConfigDAO;
import com.powerdba.mvc.PowerDbaActions;
import com.powerdba.mvc.WsnException;
import com.powerdba.mvc.jsp.JspEnvironment;
import com.powerdba.mvc.jsp.SelectEntry;
import com.powerdba.util.DateTranslator;
import com.powerdba.util.PropertyHolder;
import com.powerdba.util.Tracer;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class PerfStatManager {
    
    private static final long HOUR = 3600*1000;
    private static final long MINUTE = 3600*1000/60;

    private PerfStatView myView;
    private String key = " ";
    private OracleDatabaseConnection database;
    private Hashtable perfStatStatsHash;
    private int stat;
    private Date dateTime;
    private Date dateTime2;
    private String offSetHours;
    private int beginSnapId;
    private int endSnapId;
    private int action;
    private JspEnvironment env;

    //Constructors
    public PerfStatManager(JspEnvironment env) {
        // build the view
        myView = new PerfStatView(env);
        this.env = env;
    }

    // Main Process
    public PerfStatView process() throws Exception {
      // load incoming variables for page navigation

      try {
        // Get the specified action from the environment...
        action = env.getInt("formaction");
        myView.setCurrAction(action);
        myView.setRefreshInterval(env.getInt("refreshinterval"));
        setDatabaseList();
        if ( env.getParameter("database").equals("") || env.getParameter("database").equals("-1") ) { 
        } else {
          setDatabase(env.getParameter("database")); 
        } 

        stat = env.getInt("stat");
        if ( stat == 0 ) stat = 504;
        myView.setStat(Integer.toString(stat));
        
        if ( env.getParameter("datetime").equals("") ) {
          dateTime = new Date(database.getDateTime().getTime() - (HOUR*1));
        } else {                    
          dateTime = DateTranslator.getDateDateTime(env.getParameter("datetime") + " " + 
                                                    env.getParameter("datetimehr") + ":" + 
                                                    env.getParameter("datetimemin"), DateTranslator.HISTORY_DATETIME);
        }
        
        if ( env.getParameter("datetime2").equals("") ) {
          dateTime2 = new Date(System.currentTimeMillis() + (HOUR*1));
        } else {                    
          dateTime2 = DateTranslator.getDateDateTime(env.getParameter("datetime2") + " " + 
                                                     env.getParameter("datetime2hr") + ":" + 
                                                     env.getParameter("datetime2min"), DateTranslator.HISTORY_DATETIME);
        }
        
        myView.setDateTime(this.dateTime);
        myView.setDateTime2(this.dateTime2);
        myView.setPageType(env.getParameter("pagetype"));
        
        this.perfStatStatsHash = PerfStatDAO.getPerfStatStats();
        myView.setStatList(getStatsSelectList());
        
        setDatabaseTime();
        setSnapLists();
          
        if ( stat == 0) { action = PowerDbaActions.NEW;}
        
        Tracer.log("Processing " + PowerDbaActions.getPageCode(action) + ".", Tracer.DEBUG, this); 

        switch (action) {
            
          case PowerDbaActions.PERFSTAT:
            
            myView.setTitle("Aggregated Historical Data");
            
            Hashtable selectedValuesHash = (Hashtable) env.getSessionAttribute("selectedcategorieshash");
            String[] selectedValues = new String[0];
            if ( selectedValuesHash != null ) {
              selectedValues = (String[]) selectedValuesHash.get(new Integer(env.getInt("stat")));
              //selectedValuesHash.remove(new Integer(env.getInt("stat")));
              env.setSessionAttribute("selectedcategorieshash", selectedValuesHash);
            }
                           
            long beginSnapId = env.getLong("beginsnapid");
            long endSnapId = env.getLong("endsnapid");
                            
            PerfStatSnapshot snap = PerfStatDAO.getSnapshot(database, dateTime, dateTime2);
            beginSnapId = snap.getBeginId();
            endSnapId = snap.getEndId();
            
            myView.setBeginSnapId(beginSnapId);
            myView.setEndSnapId(endSnapId);
          
            PerfStatStat pss = (PerfStatStat) this.perfStatStatsHash.get(new Integer(this.stat));
            
            if ( pss == null ) {
              myView.setHtml("");
            } else {
              myView.setHtml(PerfStatDAO.getHtml(beginSnapId, endSnapId, pss, database, selectedValues));
            }
  
            break;
            
          case PowerDbaActions.PERFSTAT_DETAIL:
                          
            buildDetailCommon();                                                       
            break;
            
          case PowerDbaActions.OWR_CHART:
              
            StringBuffer sb = new StringBuffer();
            String detailHtml = "";
              
            long detailBegin = env.getLong("begindetail");
            long detailEnd = 0;
            
            if ( detailBegin > 0 ) {
              detailEnd = env.getLong("enddetail");
              detailHtml = PerfStatDAO.getIntervalSQL(detailBegin, detailEnd, database);
            }
              
            Timestamp begin = PerfStatDAO.getEarliestOwrSnap(database);
            Timestamp end = database.getDateTime();  
            
            PerfStatSnapshot owrSnap = new PerfStatSnapshot(begin, end);

            // Instantiate a chart definition from the ChartHolder.
            Chart chart = ChartHolder.getChart(env.getParameter("chartid"));
            
            // Get the list of categories from the chart definition, builds a list with full metric definitions
            //  from the database based on the ids that were gotten from the Chart Holder.
  	        ArrayList metrics = ChartDAO.getOWRTimeSeriesMetrics(database, chart.getMetrics());
  	
  	        // Build the timeseries collection by passing the list of categories to perfstat.
  	        TimeSeriesDataSet dataset = null;
  	        
  	        if ( chart.isClusterEnabled() ) {
  	          dataset = PerfStatDAO.getOWRTSDatasetRAC(metrics, owrSnap, database);
  	        } else {
  	          dataset = PerfStatDAO.getOWRTSDataset(metrics, owrSnap, database);
  	        }
  	        
  	        // Override the title and/or unit of measure desc if it was defined in chart.xml.  
  	        // Otherwise use the generic title for the stat.    
  	        
  	        if ( !chart.getDescription().equals("") ) dataset.setDescription(chart.getDescription());
  	        if ( !chart.getUom().equals("") ) dataset.setUom(chart.getUom());
  	        
  	        sb.append(ChartGenerator.getChartHtml(dataset, env.getSessionId(), "TS", chart.getName(), 1000, 600, null, owrSnap, this.database));
  	        
  	        if ( detailBegin > 0 ) {
	  	        sb.append("<br><table><tr><th><font size=-1>What was Running on " + this.database.getName() + " between:</th><th><font size=-1>" + new Timestamp(detailBegin).toString() + " and " + 
	  	                  new Timestamp(detailEnd).toString() + "</font></th></tr></table>");
  	        }
  	        sb.append(detailHtml);
  	        
  	        myView.setHtml(sb.toString());
            
            break;
            
          case PowerDbaActions.PERFSTAT_DETAIL2:
            
            // Get the list of categoryn variables from the request and stuff it into the session
            // to simulate the action of selecting the values from the summary screen.
            boolean valueFound = true;
            int stat = env.getInt("stat");
            String category;
            ArrayList categories = new ArrayList();
            Hashtable stats = new Hashtable();
            for ( int i=1; valueFound; i++ ) {
              category = this.env.getParameter("category" + i);
              if ( category.equals("") ) {
                valueFound = false;
              } else {
                categories.add(category);
              }
            }
            
            String[] cats = new String[categories.size()];
            for ( int i=0; i<categories.size(); i++ ) {
              cats[i] = (String) categories.get(i);
            }
            
            stats.put(new Integer(stat), cats);
            env.setSessionAttribute("selectedcategorieshash", stats);

            // Call the generic method to build the view.                
            buildDetailCommon();                                                   
            break;   
            
          case PowerDbaActions.NEW:
            Tracer.log("Processing NEW Action", Tracer.DEBUG, this);
            myView.setTitle("Detailed Historical Data Analysis");
            if ( !PerfStatDAO.isPerfstatInstalled(database) ) {
              myView.setMessage("ERROR: Stats Pack tables not found.  If you would like access to historical data, <br>" +
                                "please install statspack according to the documentation found at http://tahiti.oracle.com");
            }
            break;                
        }

        myView.build();

      } catch ( SQLException e ) {
          Tracer.log(e, "Error in the PowerDBA PerfStatManager process() method " + e.getMessage(), Tracer.ERROR, this);
          throw new WsnException("PowerDBA PerfStat Manager ",e.toString());
      }  catch ( Exception e ) {
          Tracer.log(e, "Error in the PowerDBA PerfStatManager process() method " + e.getMessage(), Tracer.ERROR, this);
          throw new WsnException("PowerDBA PerfStat Manager ",e.toString());
      } finally {       
          if ( database != null && database.getConn() != null ) {  
    	        try {
    	          this.database.getConn().close();
      	      } catch ( Exception e ) {}
            }
          }
      return myView;
    }
    
    private OracleDatabaseConnection setDatabase(String db) throws Exception {

      try {
      
        if ( this.database != null ) {
          try {
            this.database.getConn().close();
          } catch ( SQLException e ) {}
        }
        
        OracleDatabaseConnection database = new OracleDatabaseConnection();
        database.setName(db);

        Tracer.log("Acquiring a new Connection to " + database.getName(), Tracer.DEBUG, this);

        database.setConn(ConnectionManager.getConnection(db));
        database.getConn().setAutoCommit(false);
        Hashtable metadata = ProcessDAO.getDatabaseMetadata(database);
        database.setVersion((OracleVersion)metadata.get("instance_version"));
        database.setCompatible((OracleVersion)metadata.get("compatible"));
        database.setStringVersion((String)metadata.get("version_string"));
        database.setInstance((String)metadata.get("instance_name"));
        database.setDatabase((String)metadata.get("global_name"));
        database.setDateTime((Timestamp)metadata.get("sysdate"));
        database.setOtherRacInstances(ProcessDAO.getOtherInstances(database));  // db access
        database.setOtherRacDescriptors(ProcessDAO.getOtherDescriptors(database.getOtherRacInstances()));
        myView.setIsConnected(true);
        this.database = database;
        myView.setDatabase(database);

      } catch ( SQLException e ) {
      
        myView.setIsConnected(false);      
        String msg = "Error creating connection to connect descriptor " + db + ".";
        Tracer.log(e, msg, Tracer.ERROR, this);
        throw new Exception(msg + "<BR><b>Root Cause:</b>  " + e.getMessage() + "\n");

      } 
      
      return database;
    }
    
    private void setDatabaseTime() throws Exception {
      if ( database == null ) {
        myView.setDatabaseTime(new Timestamp(System.currentTimeMillis()));
      } else {
        myView.setDatabaseTime(database.getDateTime());
      }
    }
    
    private ArrayList getStatsSelectList() throws Exception {
  
      ArrayList statsSelectList = new ArrayList();     
      Enumeration statsEnum = this.perfStatStatsHash.keys();
      
      while ( statsEnum.hasMoreElements() ) {
        Integer id = (Integer) statsEnum.nextElement();
        statsSelectList.add(new SelectEntry(id.toString(), ((PerfStatStat) this.perfStatStatsHash.get(id)).getName()));
      }  
      Collections.sort(statsSelectList,SelectEntry.CASE_INSENSITIVE_DISPLAY_ORDER);
      return statsSelectList;
    }
    
    private void setDatabaseList() throws Exception {

      try {
        ArrayList dbList = new ArrayList();
        Vector       v;
        String db = null;
        DbConfigDAO reader = new DbConfigDAO();
  
        v = reader.readFile(PropertyHolder.getProperty("connectionPoolPropertiesFile"));
        Tracer.log("Read " + v.size() + " Server Configs.", Tracer.METHOD, "ProcessManager");
  
        for (int i = 0; i < v.size(); i++) {
          db = ((DbConfig) v.elementAt(i)).getPoolName();
          dbList.add(new SelectEntry(db, db));
        }
        Collections.sort(dbList,SelectEntry.CASE_INSENSITIVE_DISPLAY_ORDER);  
        myView.setDbList(dbList);
        Tracer.log("Set the dblist size of " + dbList.size(), Tracer.DEBUG, "ProcessManager");
  
      } catch (Exception e) {
        Tracer.log(e, "Could not get List of Databases from properties file.", Tracer.ERROR, "ProcessManager");
        throw e;
      }
    
    }
    
    private void setSnapLists() throws Exception {
      myView.setBeginSnapList(PerfStatDAO.getSnapList(this.database));
      myView.setEndSnapList(PerfStatDAO.getSnapList(this.database));
    }
    
    private void buildDetailCommon() throws SQLException, Exception {
        
        Tracer.log("In buildDetailCommon()", Tracer.DEBUG, "PerfstatDAO");
    
        Hashtable selectedValuesHash = (Hashtable) env.getSessionAttribute("selectedcategorieshash");
        String[] storedSelectedValues = new String[0];
        
        if ( selectedValuesHash != null ) {
          storedSelectedValues = (String[]) selectedValuesHash.get(new Integer(env.getInt("stat")));
        } else {
          selectedValuesHash = new Hashtable();
        }

        String[] selectedValuesFromRequest = env.getReqParameterValues("recordkey");
        String[] valuesToUse = null;
        
        if ( selectedValuesFromRequest != null && selectedValuesFromRequest.length > 0 ) {
          valuesToUse = selectedValuesFromRequest;
        } else {
          valuesToUse = storedSelectedValues;
        }
   
        if ( valuesToUse == null || valuesToUse.length == 0 ) throw new Exception("No Selections were made.");
        
        // Get the beginning and ending snap ids
        long beginSnapId = env.getLong("beginsnapid");
        long endSnapId   = env.getLong("endsnapid");
        String pageType  = env.getParameter("pagetype");

        if ( pageType.equals("") ) pageType = PerfStatDAO.CHART;

        PerfStatSnapshot snap = PerfStatDAO.getSnapshot(database, dateTime, dateTime2);
                                        
        if ( pageType.equals(PerfStatDAO.DATA) ) {
          myView.setTitle("History Data Table");
        } else {
          myView.setTitle("History Chart");
        }

        myView.setHtml(PerfStatDAO.getDetailHtml(env.getInt("stat"),
                                                 snap,
                                                 valuesToUse,
                                                 database,
                                                 pageType));
                                                 
        // Put the current stats selected values into the session.                
        selectedValuesHash.put(new Integer(env.getInt("stat")), valuesToUse);
        env.setSessionAttribute("selectedcategorieshash", selectedValuesHash);
    }

}

