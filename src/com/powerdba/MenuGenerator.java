package com.powerdba;

import com.powerdba.mvc.PowerDbaActions;
import com.powerdba.mvc.jsp.HtmlComponent;

import java.sql.*;
import java.util.Hashtable;

public class MenuGenerator {

    private static final String OBJECT_NAME = "MenuGenerator";
    
    //private static final String SMDIV = "<td bgcolor=white> &middot; </td>";
    private static final String SMDIV = "<td bgcolor=white><font color=silver size=-2> | </font></td>";
    private static final String SMDIVWH = "<td bgcolor=white> </td>";
    private static final String INDENT = "<td bgcolor=white><img src='images/clear.gif' width=2 height=1></td>";
    private static final String INDENTBIG = "<td bgcolor=white><img src='images/clear.gif' width=20 height=1></td>";
    private static final String ENDLINE = "<td bgcolor=silver><img src='images/clear.gif' width=1 height=5></td>";
    
    private static Hashtable defaultActions;

    public static final int TOP_MENU              = 0;
    public static final int INSTANCE_MENU         = 1000;    
    public static final int SESSIONS_MENU         = 1100;
    public static final int REDO_MENU             = 1200;
    public static final int INSTANCE_SUMMARY_MENU = 1300;
    public static final int SESSION_DETAIL_MENU   = 1400;
    public static final int LIBRARY_CACHE_MENU    = 1500;
    public static final int LATCH_MENU            = 1600;  
    public static final int MEMORY_MENU           = 1700;
    public static final int PQ_DETAIL_MENU        = 100;
    public static final int SYSTEMWAIT_MENU       = 1800;
    public static final int SYSSTAT_MENU          = 1900;
    public static final int DATABASE_MENU         = 2000;
    public static final int SCHEMA_MENU           = 2200;
    public static final int TABLESPACE_MENU       = 2300;
    public static final int ROLLBACK_MENU         = 2400;
    public static final int MVIEW_MENU            = 2500;
    public static final int RMAN_MENU             = 2600; 
    public static final int CHARTS_MENU           = 4000;
    public static final int HISTORY_MENU          = 5000; 
    public static final int MESSAGING_MENU        = 6000;
    public static final int STREAMS_MENU          = 6100;
    public static final int AQ_MENU_SRC           = 6200;
    public static final int AQ_MENU_DEST          = 6300;
    public static final int NO_MENU               = 999999;
    
    public Hashtable menuConversionHash = new Hashtable();

    public MenuGenerator() {}
    
    static public int getMenuIdFromString(String menuString) {
    	int menuId = 0;
    	if ( menuString == null ) {
    		menuId = 0;
    	} else if ( menuString.equals("TOP_MENU") ) {
    		menuId = 0;
      } else if ( menuString.equals("INSTANCE_MENU") ) {
      	menuId = 1000;

      } else if ( menuString.equals("SESSIONS_MENU") ) {
      	menuId = 1100;
      } else if ( menuString.equals("REDO_MENU") ) {
      	menuId = 1200;
      } else if ( menuString.equals("INSTANCE_SUMMARY_MENU") ) {
      	menuId = 1300;
      } else if ( menuString.equals("SESSION_DETAIL_MENU") ) {
      	menuId = 1400;
      } else if ( menuString.equals("LIBRARY_CACHE_MENU") ) {
      	menuId = 1500;
      } else if ( menuString.equals("LATCH_MENU") ) {
      	menuId = 1600;
      } else if ( menuString.equals("MEMORY_MENU") ) {
      	menuId = 1700;
      } else if ( menuString.equals("PQ_DETAIL_MENU") ) {
      	menuId = 100;
      } else if ( menuString.equals("SYSTEMWAIT_MENU") ) {
      	menuId = 1800;
      } else if ( menuString.equals("SYSSTAT_MENU") ) {
      	menuId = 1900;
      } else if ( menuString.equals("DATABASE_MENU") ) {
      	menuId = 2000;
      } else if ( menuString.equals("MESSAGING_MENU") ) {
      	menuId = 6000;
      } else if ( menuString.equals("STREAMS_MENU") ) {
      	menuId = 6100;
      } else if ( menuString.equals("AQ_MENU_SRC") ) {
      	menuId = 6200;
      } else if ( menuString.equals("AQ_MENU_DEST") ) {
      	menuId = 6300;
      } else if ( menuString.equals("SCHEMA_MENU") ) {
      	menuId = 2200;
      } else if ( menuString.equals("TABLESPACE_MENU") ) {
      	menuId = 2300;
      } else if ( menuString.equals("ROLLBACK_MENU") ) {
      	menuId = 2500;
      } else if ( menuString.equals("RMAN_MENU") ) {
      	menuId = 2600;
      } else if ( menuString.equals("MVIEW_MENU") ) {
      	menuId = 4000;
      }  else if ( menuString.equals("HISTORY_MENU") ) {
      	menuId = 5000;
      }  else if ( menuString.equals("NO_MENU") ) {
      	menuId = 999999;
      }  else if ( menuString.equals("ROLLBACK_MENU") ) {
      	menuId = 1000;
      } 
    	return menuId;
    }
    
    static public String getMenuStringFromId(int menuId) {
    	String menuString = "";
    	if ( menuId == 0 ) {
    		menuString = "TOP_MENU";
      } else if ( menuId == 1000 ) {
      	menuString = "INSTANCE_MENU";
      } else if ( menuId == 6000 ) {
      	menuString = "MESSAGING_MENU";
      } else if ( menuId == 1100)  {
      	menuString = "SESSIONS_MENU";
      } else if ( menuId == 1200)  {
      	menuString = "REDO_MENU";
      } else if ( menuId == 1300)  {
      	menuString = "INSTANCE_SUMMARY_MENU";
      } else if ( menuId == 1400) {
      	menuString = "SESSION_DETAIL_MENU";
      } else if ( menuId == 1500)  {
      	menuString = "LIBRARY_CACHE_MENU";
      } else if ( menuId == 1600)  {
      	menuString = "LATCH_MENU";
      } else if ( menuId == 1700)  {
      	menuString = "MEMORY_MENU";
      } else if ( menuId == 100)  {
      	menuString = "PQ_DETAIL_MENU";
      } else if ( menuId == 1800)  {
      	menuString = "SYSTEMWAIT_MENU";
      } else if ( menuId == 1900)  {
      	menuString = "SYSSTAT_MENU";
      } else if ( menuId == 2000)  {
      	menuString = "DATABASE_MENU";
      } else if ( menuId == 6100)  {
      	menuString = "STREAMS_MENU";
      } else if ( menuId == 2200)  {
      	menuString = "SCHEMA_MENU";
      } else if ( menuId == 2300)  {
      	menuString = "TABLESPACE_MENU";
      } else if ( menuId == 2500)  {
      	menuString = "ROLLBACK_MENU";
      } else if ( menuId == 2600)  {
      	menuString = "RMAN_MENU";
      } else if ( menuId == 6200)  {
      	menuString = "AQ_MENU_SRC";
      } else if ( menuId == 6300)  {
      	menuString = "AQ_MENU_DEST";
      } else if ( menuId == 4000)  {
      	menuString = "MVIEW_MENU";
      }  else if ( menuId == 5000)  {
      	menuString = "HISTORY_MENU";
      }  else if ( menuId == 999999) {
      	menuString = "NO_MENU";
      }  else if ( menuId == 1000) {
      	menuString = "ROLLBACK_MENU";
      }
    	return menuString;
    }
    
    static public String getMenu(int currentAction, OracleDatabaseConnection database, int menu) throws SQLException {
      return getMenu(currentAction, database, 0, menu);
    }
    
    static public String getMenu(int currentAction, OracleDatabaseConnection database, long sid, int menu) throws SQLException {

      defaultActions = new Hashtable();    
      defaultActions.put(new Integer(DATABASE_MENU), new Integer(PowerDbaActions.DB_DATABASE));
      defaultActions.put(new Integer(MESSAGING_MENU), new Integer(PowerDbaActions.DB_AQ_QUEUE_ROUTING));
      defaultActions.put(new Integer(INSTANCE_MENU), new Integer(PowerDbaActions.DB_SESSIONS));
      defaultActions.put(new Integer(SESSIONS_MENU), new Integer(PowerDbaActions.DB_SESSIONS));
      defaultActions.put(new Integer(REDO_MENU), new Integer(PowerDbaActions.DB_LOG_SUMMARY));
      defaultActions.put(new Integer(INSTANCE_SUMMARY_MENU), new Integer(PowerDbaActions.DB_INSTANCE));
      defaultActions.put(new Integer(SESSION_DETAIL_MENU), new Integer(PowerDbaActions.DB_SESS_ZOOM));
      defaultActions.put(new Integer(LIBRARY_CACHE_MENU), new Integer(PowerDbaActions.LC_SUMMARY));
      defaultActions.put(new Integer(LATCH_MENU), new Integer(PowerDbaActions.DB_LATCH_PARENT));
      defaultActions.put(new Integer(PQ_DETAIL_MENU), new Integer(PowerDbaActions.DB_PQSLAVES));
      defaultActions.put(new Integer(SCHEMA_MENU), new Integer(PowerDbaActions.DB_SCHEMAS));
      defaultActions.put(new Integer(RMAN_MENU), new Integer(PowerDbaActions.DB_RECOVERY_SIZE));
    
      String html = new String();
       
      switch (menu) {
        case MenuGenerator.INSTANCE_MENU:          
          html = getInstanceMenu(currentAction, database);
          break;
        case MenuGenerator.SESSIONS_MENU:
          html = getSessionsMenu(currentAction, database);
          break;
        case MenuGenerator.DATABASE_MENU:
          html = getDatabaseMenu(currentAction, database);
          break;
        case MenuGenerator.MESSAGING_MENU:
          html = getMessagingMenu(currentAction, database);
          break;
        case MenuGenerator.STREAMS_MENU:
          html = getStreamsMenu(currentAction, database);
          break;
        case MenuGenerator.AQ_MENU_SRC:
          html = getAQMenuSrc(currentAction, database);
          break;
        case MenuGenerator.AQ_MENU_DEST:
          html = getAQMenuDest(currentAction, database);
          break;
        case MenuGenerator.REDO_MENU:
          html = getRedoMenu(currentAction, database);
          break;
        case MenuGenerator.TOP_MENU:
          html = getTopMenu(currentAction, database);
          break;
        case MenuGenerator.INSTANCE_SUMMARY_MENU:
          html = getInstanceSummaryMenu(currentAction, database);
          break;
        case MenuGenerator.SESSION_DETAIL_MENU:
          html = getSessionDetailMenu(currentAction, database, sid);
          break;
        case MenuGenerator.PQ_DETAIL_MENU:
            html = getPQDetailMenu(currentAction, database, sid);
            break;
        case MenuGenerator.LIBRARY_CACHE_MENU:
          html = getLibraryCacheMenu(currentAction, database);
          break;
        case MenuGenerator.LATCH_MENU:
          html = getLatchMenu(currentAction, database);
          break;
        case MenuGenerator.SYSSTAT_MENU:
          html = getSysstatMenu(currentAction, database);
          break;
        case MenuGenerator.CHARTS_MENU:
          html = getChartsMenu(currentAction, database);
          break;
        case MenuGenerator.MEMORY_MENU:
          html = getMemoryMenu(currentAction, database);
          break;
        case MenuGenerator.TABLESPACE_MENU:
            html = getTablespaceMenu(currentAction, database);
            break;
        case MenuGenerator.MVIEW_MENU:
            html = getMviewMenu(currentAction, database);
            break;
        case MenuGenerator.SCHEMA_MENU:
            html = getSchemaMenu(currentAction, database);
            break;
        case MenuGenerator.RMAN_MENU:
          html = getRmanMenu(currentAction, database);
          break;
        default:
          html = "";          
      }     
      
      return html;
       
    }
    
    static public String getTopMenu(int currentAction, OracleDatabaseConnection database)  throws SQLException {
      StringBuffer sb = new StringBuffer();
      
      // Create the Top Menu
      sb.append("<div class='iframeHeaderRow2'>");
      sb.append(bigMenuOption2("Activity", PowerDbaActions.DB_SESSIONS, currentAction, database, INSTANCE_MENU));
      sb.append(bigMenuOption2("Messaging", PowerDbaActions.DB_MESSAGE_LATENCY, currentAction, database, MESSAGING_MENU));
      sb.append(bigMenuOption2("Database", PowerDbaActions.DB_SCHEMAS, currentAction, database, DATABASE_MENU));
      sb.append(bigMenuOption2("Dash*Board",  PowerDbaActions.CHARTS_CLUSTER, currentAction, database, CHARTS_MENU));
      //sb.append(bigMenuOption2("History",  PowerDbaActions.NEW, currentAction, database, HISTORY_MENU));
      sb.append("</div>");
       
      return sb.toString();
    }

 
    static public String getMessagingMenu(int currentAction, OracleDatabaseConnection database)  {
      StringBuffer sb = new StringBuffer();
      
      // Create the Secondary Menu for Instance...
      sb.append("<div class='iframeHeaderRow'>");
      sb.append(bigMenuOption("All Latency", PowerDbaActions.DB_MESSAGE_LATENCY, currentAction, database, PowerDbaActions.DB_MESSAGE_LATENCY));
      sb.append(bigMenuOption("AQ Source", PowerDbaActions.DB_AQ_QUEUE_ROUTING, currentAction, database, AQ_MENU_SRC));   
      sb.append(bigMenuOption("AQ Destination", PowerDbaActions.DB_AQ_DEST_MESSAGES, currentAction, database, AQ_MENU_DEST));   
      sb.append(bigMenuOption("Streams", PowerDbaActions.DB_STREAMS_CAPTURE, currentAction, database, STREAMS_MENU));
      
      sb.append("</div>");
        
      return sb.toString();
    }
    
    static public String getInstanceMenu(int currentAction, OracleDatabaseConnection database)  {
      StringBuffer sb = new StringBuffer();
      
      // Create the Secondary Menu for Instance...
      sb.append("<div class='iframeHeaderRow'>");
      sb.append(bigMenuOption("Current Workload", PowerDbaActions.DB_SESSIONS, currentAction, database, SESSIONS_MENU));   
      sb.append(bigMenuOption("System Stats", PowerDbaActions.DB_SYSTEM_EVENTS, currentAction, database, SYSSTAT_MENU));
      sb.append(bigMenuOption("Redo Log", PowerDbaActions.DB_LOG_SUMMARY, currentAction, database, REDO_MENU));
      sb.append(bigMenuOption("Library Cache", PowerDbaActions.LC_SUMMARY, currentAction, database, LIBRARY_CACHE_MENU));
      sb.append(bigMenuOption("Memory", PowerDbaActions.DB_INSTANCE_MEMORY_SUMMARY, currentAction, database, MEMORY_MENU));
      sb.append(bigMenuOption("Latches", PowerDbaActions.DB_LATCH_PARENT, currentAction, database, LATCH_MENU));
      sb.append(bigMenuOption("Init Params", PowerDbaActions.DB_INSTANCE, currentAction, database, INSTANCE_SUMMARY_MENU));


      sb.append("</div>");
        
      return sb.toString();
    }
    

    static public String getDatabaseMenu(int currentAction, OracleDatabaseConnection database)  {
      StringBuffer sb = new StringBuffer();
      
      // Create the Secondary Menu for Databaseaa...
      sb.append("<div class='iframeHeaderRow'>");
      sb.append(bigMenuOption("Schemas", PowerDbaActions.DB_SCHEMAS, currentAction, database, SCHEMA_MENU));
      sb.append(bigMenuOption("Job Queue", PowerDbaActions.DB_JOBS, currentAction, database, PowerDbaActions.DB_JOBS));
      sb.append(bigMenuOption("Mviews", PowerDbaActions.DB_MVIEWS, currentAction, database, MVIEW_MENU));
      sb.append(bigMenuOption("Storage", PowerDbaActions.DB_TABLESPACES, currentAction, database, PowerDbaActions.DB_TABLESPACES));
      sb.append(bigMenuOption("RMAN", PowerDbaActions.DB_RECOVERY_SIZE, currentAction, database, RMAN_MENU));
      sb.append(bigMenuOption("Summary", PowerDbaActions.DB_DATABASE, currentAction, database, PowerDbaActions.DB_DATABASE));

      sb.append("</div>");

      return sb.toString();
    }
    
    static public String getChartsMenu(int currentAction, OracleDatabaseConnection database)  {
      StringBuffer sb = new StringBuffer();
      
      // Create the Secondary Menu for Charts...
      sb.append("<div class='iframeHeaderRow'>");
      sb.append(bigMenuOption("CPU", PowerDbaActions.CHARTS_CPU, currentAction, database, SYSTEMWAIT_MENU));
      sb.append(bigMenuOption("Summary", PowerDbaActions.CHARTS_SUMMARY, currentAction, database, SESSIONS_MENU));
      sb.append(bigMenuOption("Clusterwide", PowerDbaActions.CHARTS_CLUSTER, currentAction, database, SYSTEMWAIT_MENU));
      sb.append(bigMenuOption("CPU & IO", PowerDbaActions.CHARTS_CPUIO, currentAction, database, SYSTEMWAIT_MENU));


      
      //sb.append(bigMenuOption("Pie Charts", PowerDbaActions.CHARTS_SESSIONS, currentAction, database, SYSTEMWAIT_MENU));
      //if ( database.hasMetrics() ) 
      //  sb.append(bigMenuOption("Metrics", PowerDbaActions.CHARTS_METRICS, currentAction, database, SYSTEMWAIT_MENU));
      
      if ( database.getVersion().getVersion1() < 10 ) {
	      sb.append(bigMenuOption("Contention", PowerDbaActions.CHARTS_CONTENTION, currentAction, database, SYSSTAT_MENU));
	      sb.append(bigMenuOption("IO", PowerDbaActions.CHARTS_IO, currentAction, database, REDO_MENU));
	      sb.append(bigMenuOption("Activity", PowerDbaActions.CHARTS_ACTIVITY, currentAction, database, SESSIONS_MENU));
      }
      sb.append("</div>");
        
      return sb.toString();
    }

    static public String getStreamsMenu(int currentAction, OracleDatabaseConnection database)  {
      StringBuffer sb = new StringBuffer();
      
      // Create the Secondary Menu for Streams...
      sb.append("<table cellpadding = 0 cellspacing=0>");
      //sb.append("<tr><td bgcolor=silver colspan=20><img src='images/clear.gif' height=1></td></tr>");
      sb.append(INDENT);
      sb.append(buildLittleOption("Capture", PowerDbaActions.DB_STREAMS_CAPTURE, currentAction, database));
      sb.append(SMDIV); 
      sb.append(buildLittleOption("Apply", PowerDbaActions.DB_STREAMS_APPLY, currentAction, database));
      sb.append(SMDIV); 
      sb.append(buildLittleOption("Rules", PowerDbaActions.DB_STREAMS_RULE_SETS, currentAction, database));
      sb.append(SMDIV); 
      sb.append(buildLittleOption("DML Handlers", PowerDbaActions.DB_STREAMS_DML_HANDLERS, currentAction, database));
      sb.append(SMDIV); 
      sb.append(buildLittleOption("LCR Shredders", PowerDbaActions.DB_STREAMS_SHREDDERS, currentAction, database));
      sb.append(SMDIV); 
      sb.append(buildLittleOption("Buffered Queues", PowerDbaActions.DB_STREAMS_QUEUES, currentAction, database));
      sb.append(SMDIV); 
      sb.append(buildLittleOption("Apply Errors", PowerDbaActions.DB_STREAMS_ERROR_QUEUE, currentAction, database));
      sb.append(SMDIV); 
      sb.append(buildLittleOption("Streams Trx", PowerDbaActions.DB_STREAMS_TRANS, currentAction, database));
      sb.append(SMDIV);
      sb.append(buildLittleOption("BG Procs", PowerDbaActions.DB_STREAMS_BG, currentAction, database));
      sb.append(SMDIV);
      sb.append(buildLittleOption("Minimum Log", PowerDbaActions.DB_STREAMS_SUMMARY, currentAction, database));
      sb.append(SMDIV); 
      //sb.append(buildLittleOption("Reg Logs", PowerDbaActions.DB_REGISTERED_LOGS, currentAction, database));
      //sb.append(ENDLINE);
      sb.append(buildLittleOption("Queue States", PowerDbaActions.DB_QUEUE_STATES, currentAction, database));
      sb.append(ENDLINE);
      sb.append("<tr><td bgcolor=silver colspan=20><img src='images/clear.gif' height=1></td></tr>");
      sb.append("</tr></table>");

      sb.append("<br>");
        
      return sb.toString();
    }
    

    
    static public String getTablespaceMenu(int currentAction, OracleDatabaseConnection database)  {
        StringBuffer sb = new StringBuffer();
        
        // Create the Secondary Menu for Streams...
        sb.append("<table cellpadding=0 cellspacing=0><tr>");
        sb.append(INDENT);
        sb.append(buildLittleOption("Tablespaces", PowerDbaActions.DB_TABLESPACES, currentAction, database));
        sb.append(SMDIV); 
        sb.append(buildLittleOption("Freespace", PowerDbaActions.DB_FREESPACE, currentAction, database));
        sb.append(SMDIV);
        sb.append(buildLittleOption("Freespace Problems", PowerDbaActions.DB_FREESPACE_PROBLEMS, currentAction, database));
        sb.append(SMDIV);
        sb.append(buildLittleOption("Directories", PowerDbaActions.DB_DIRECTORIES, currentAction, database));
        sb.append(ENDLINE); 
        sb.append("<tr><td bgcolor=silver colspan=20><img src='images/clear.gif' height=1></td></tr>");
        sb.append("</tr></table>");

        sb.append("<br>");
          
        return sb.toString();
    }
    
    static public String getSchemaMenu(int currentAction, OracleDatabaseConnection database)  {
        StringBuffer sb = new StringBuffer();
        
        // Create the Secondary Menu for Streams...
        sb.append("<table cellpadding=0 cellspacing=0><tr>");
        sb.append(INDENT);
        sb.append(buildLittleOption("All Schemas", PowerDbaActions.DB_SCHEMAS, currentAction, database));
        sb.append(SMDIV); 
        sb.append(buildLittleOption("Schemas w/Objects", PowerDbaActions.DB_SCHEMAS_W_OBJECTS, currentAction, database));
        sb.append(ENDLINE); 
        sb.append("<tr><td bgcolor=silver colspan=20><img src='images/clear.gif' height=1></td></tr>");
        sb.append("</tr></table>");

        sb.append("<br>");
          
        return sb.toString();
    }
    
    static public String getRmanMenu(int currentAction, OracleDatabaseConnection database)  {
      StringBuffer sb = new StringBuffer();
      
      // Create the Secondary Menu for Streams...
      sb.append("<table cellpadding=0 cellspacing=0><tr>");
      sb.append(INDENT);
      sb.append(buildLittleOption("Recovery Area", PowerDbaActions.DB_RECOVERY_SIZE, currentAction, database));
      sb.append(ENDLINE); 
      sb.append("<tr><td bgcolor=silver colspan=20><img src='images/clear.gif' height=1></td></tr>");
      sb.append("</tr></table>");

      sb.append("<br>");
        
      return sb.toString();
  }
    
    static public String getMviewMenu(int currentAction, OracleDatabaseConnection database)  {
        StringBuffer sb = new StringBuffer();
        
        // Create the Secondary Menu for Streams...
        sb.append("<table cellpadding=0 cellspacing=0><tr>");
        sb.append(INDENT);
        sb.append(buildLittleOption("Mviews", PowerDbaActions.DB_MVIEWS, currentAction, database));
        sb.append(SMDIV); 
        sb.append(buildLittleOption("Mview Logs", PowerDbaActions.DB_MVIEW_LOGS, currentAction, database));
        sb.append(SMDIV); 
        sb.append(buildLittleOption("Mview Dependencies", PowerDbaActions.DB_MVIEW_LOG_DEP, currentAction, database));
        sb.append(ENDLINE);
        sb.append("<tr><td bgcolor=silver colspan=20><img src='images/clear.gif' height=1></td></tr>");
        sb.append("</tr></table>");

        sb.append("<br>");
          
        return sb.toString();
    }


    
    static public String getLibraryCacheMenu(int currentAction, OracleDatabaseConnection database)  {
      StringBuffer sb = new StringBuffer();
        
      // Create the Secondary Menu for Sessions...
      sb.append("<table cellpadding = 0 cellspacing=0>");
      //sb.append("<tr><td bgcolor=silver colspan=20><img src='images/clear.gif' height=1></td></tr>");
      sb.append(INDENT);
      sb.append(buildLittleOption("Library Cache Summary", PowerDbaActions.LC_SUMMARY, currentAction, database));
      sb.append(SMDIV);
      sb.append(buildLittleOption("Heavy Sql", PowerDbaActions.LC_HEAVY_SQL, currentAction, database));
      sb.append(SMDIV);
      sb.append(buildLittleOption("Bad Sql", PowerDbaActions.LC_BAD_SQL, currentAction, database));
      sb.append(SMDIV);
      sb.append(buildLittleOption("Long Running Sql", PowerDbaActions.LC_ELAPSED_SQL, currentAction, database));
      sb.append(SMDIV);
      sb.append(buildLittleOption("Most Executed Sql", PowerDbaActions.LC_MOSTEXEC_SQL, currentAction, database));
      sb.append(SMDIV);
      sb.append(buildLittleOption("Parsing", PowerDbaActions.LC_PARSING_SUMMARY, currentAction, database));

      sb.append(ENDLINE);
      sb.append("<tr><td bgcolor=silver colspan=20><img src='images/clear.gif' height=1></td></tr>");
      sb.append("</tr></table>");


      return sb.toString();
    }
    

    static public String getSessionsMenu(int currentAction, OracleDatabaseConnection database)  {
      StringBuffer sb = new StringBuffer();
             
      // Create the Secondary Menu for Sessions...
      sb.append("<table cellpadding = 0 cellspacing=0>");
      //sb.append("<tr><td bgcolor=silver colspan=20><img src='images/clear.gif' height=2></td></tr>");
      sb.append(INDENT);
      sb.append(buildLittleOption("Sessions", PowerDbaActions.DB_SESSIONS, currentAction, database));
      sb.append(SMDIV); 
      //sb.append(buildLittleOption("User Sessions - SQL", PowerDbaPages.DB_SESSIONS2, currentAction, database));
      //sb.append(SMDIV); 
      sb.append(buildLittleOption("Session Longops", PowerDbaActions.DB_LONGOPS, currentAction, database));
      sb.append(SMDIV); 
      sb.append(buildLittleOption("Blockers", PowerDbaActions.DB_BLOCKERS, currentAction, database));
      sb.append(SMDIV); 
      sb.append(buildLittleOption("Locks", PowerDbaActions.DB_LOCKS, currentAction, database));
      sb.append(SMDIV);
      sb.append(buildLittleOption("Bgd Sessions", PowerDbaActions.DB_BG_SESSIONS, currentAction, database));
      sb.append(SMDIV);
      sb.append(buildLittleOption("Transactions", PowerDbaActions.DB_TRANSACTIONS, currentAction, database));
      sb.append(SMDIV); 
      sb.append(buildLittleOption("All Jobs", PowerDbaActions.DB_SCHJOBS_WL, currentAction, database));
      sb.append(SMDIV);
      sb.append(buildLittleOption("Job Queue", PowerDbaActions.DB_JOBS_WL, currentAction, database));
      sb.append(SMDIV);
      sb.append(buildLittleOption("OPQ", PowerDbaActions.DB_PQSLAVES, currentAction, database));
      sb.append(SMDIV);
      sb.append(buildLittleOption("Services", PowerDbaActions.DB_GLOBAL_SERVICES, currentAction, database));
      sb.append(ENDLINE);

      sb.append("<tr><td bgcolor=silver colspan=20><img src='images/clear.gif' height=1></td></tr>");
      sb.append("</tr></table>");

      return sb.toString();
    }
    
    static public String getSessionDetailMenu(int currentAction, OracleDatabaseConnection database, long sid)  {
      StringBuffer sb = new StringBuffer();
        
      // Create the Secondary Menu for Sessions...
      sb.append("<table cellpadding = 0 cellspacing=0>");
      sb.append("<tr>");
      sb.append(INDENTBIG);
      sb.append(buildSessionOption("Session Details", PowerDbaActions.DB_SESS_ZOOM, currentAction, database, sid));
      sb.append(SMDIVWH);
      sb.append(buildSessionOption("Open Cursors", PowerDbaActions.LC_OPEN_CURSORS, currentAction, database, sid));
      sb.append(SMDIVWH);
      sb.append(buildSessionOption("Statistics", PowerDbaActions.DB_SESSION_STATS, currentAction, database, sid));
      sb.append(SMDIVWH);
      sb.append(buildSessionOption("Locks Held", PowerDbaActions.DB_SESSION_LOCKS_HELD, currentAction, database, sid));
      sb.append(SMDIVWH);
      sb.append(buildSessionOption("Locks Requested", PowerDbaActions.DB_SESSION_LOCKS_REQUESTED, currentAction, database, sid));
      sb.append(SMDIVWH);
      sb.append(buildSessionOption("Connect Info", PowerDbaActions.DB_SESSION_CONNECT_INFO, currentAction, database, sid));
      sb.append(SMDIVWH);
      sb.append(buildSessionOption("Objects", PowerDbaActions.DB_SESSION_ACCESS, currentAction, database, sid));
      sb.append("</tr></table>");

      return sb.toString();
    }
    
    static public String getAQMenuSrc(int currentAction, OracleDatabaseConnection database)  {
      StringBuffer sb = new StringBuffer();
      
      sb.append("<table cellpadding = 0 cellspacing=0>");
      //sb.append("<tr><td bgcolor=silver colspan=20><img src='images/clear.gif' height=1></td></tr>");
      sb.append(INDENT);
      sb.append(buildLittleOption("Source Messages", PowerDbaActions.DB_AQ_QUEUE_ROUTING, currentAction, database));
      sb.append(SMDIV); 
      sb.append(buildLittleOption("Available Queues", PowerDbaActions.DB_AQ_QUEUE_MASTER, currentAction, database));
      sb.append(SMDIV); 
      sb.append(buildLittleOption("Queue Propagations", PowerDbaActions.DB_AQ_PROPAGATIONS, currentAction, database));
      sb.append(SMDIV); 
      sb.append(buildLittleOption("Enqueue Errors", PowerDbaActions.DB_AQ_ENQ_ERRORS, currentAction, database));
      sb.append(SMDIV);
      sb.append(buildLittleOption("Queue Tables", PowerDbaActions.DB_AQ_QUEUE_TABLES, currentAction, database));
      sb.append(ENDLINE); 
      sb.append("<tr><td bgcolor=silver colspan=20><img src='images/clear.gif' height=1></td></tr>");
      sb.append("</tr></table>");

      sb.append("<br>");



      return sb.toString();
    }
    
    static public String getAQMenuDest(int currentAction, OracleDatabaseConnection database)  {
      StringBuffer sb = new StringBuffer();
      
      sb.append("<table cellpadding = 0 cellspacing=0>");
      //sb.append("<tr><td bgcolor=silver colspan=20><img src='images/clear.gif' height=1></td></tr>");
      sb.append(INDENT);
      sb.append(buildLittleOption("Destination Messages", PowerDbaActions.DB_AQ_DEST_MESSAGES, currentAction, database));
      sb.append(SMDIV);
      sb.append(buildLittleOption("Shredders", PowerDbaActions.DB_AQ_MSG_HANDLERS, currentAction, database));
      sb.append(SMDIV); 
      sb.append(buildLittleOption("Shredder Stats", PowerDbaActions.DB_AQ_MSG_HANDLER_STATS, currentAction, database));
      sb.append(SMDIV); 
      sb.append(buildLittleOption("Errors", PowerDbaActions.DB_AQ_DEQ_ERRORS, currentAction, database));
      sb.append(SMDIV);
      sb.append(buildLittleOption("Error Summary", PowerDbaActions.DB_AQ_HANDLER_ERRSUMM, currentAction, database));
      sb.append(SMDIV);
      sb.append(buildLittleOption("Jobs", PowerDbaActions.DB_AQ_SHREDDERS, currentAction, database));
      sb.append(SMDIV);
      sb.append(buildLittleOption("Throughput", PowerDbaActions.CHARTS_SHREDDER_THROUGHPUT, currentAction, database));
      sb.append(ENDLINE); 
      sb.append("<tr><td bgcolor=silver colspan=20><img src='images/clear.gif' height=1></td></tr>");
      sb.append("</tr></table>");
      sb.append("<br>");
      return sb.toString();
    }
    
      
    static public String getPQDetailMenu(int currentAction, OracleDatabaseConnection database, long sid)  {
        StringBuffer sb = new StringBuffer();
          
        // Create the Secondary Menu for Sessions...
        sb.append("<table cellpadding = 0 cellspacing=0>");
        sb.append("<tr>");
        sb.append(INDENTBIG);
        sb.append(buildSessionOption("PQ Slaves", PowerDbaActions.DB_PQSLAVES, currentAction, database, sid));
        sb.append(SMDIVWH);
        sb.append(buildSessionOption("PQ Stats", PowerDbaActions.DB_PQSTATS, currentAction, database, sid));
        sb.append(SMDIVWH);
        sb.append(buildSessionOption("PQ Sessions", PowerDbaActions.DB_PQSESSION, currentAction, database, sid));
        sb.append(SMDIVWH);
        sb.append(buildSessionOption("PQ Queue Stats", PowerDbaActions.DB_PQTQ, currentAction, database, sid));
        sb.append("</tr></table>");

        return sb.toString();
      }    
    
    static public String getInstanceSummaryMenu(int currentAction, OracleDatabaseConnection database)  {
      StringBuffer sb = new StringBuffer();
        
      // Create the Secondary Menu for Instance...
      sb.append("<table cellpadding = 0 cellspacing=0>");
      //sb.append("<tr><td bgcolor=silver colspan=20><img src='images/clear.gif' height=1></td></tr>");
      sb.append(INDENT);
      sb.append(buildLittleOption("Instance Overview", PowerDbaActions.DB_INSTANCE, currentAction, database));
      sb.append(SMDIV);
      sb.append(buildLittleOption("All Parameters", PowerDbaActions.DB_PARAMETERS, currentAction, database));
      sb.append(SMDIV);
      sb.append(buildLittleOption("Hidden Parameters", PowerDbaActions.DB_HIDDEN_PARMS, currentAction, database));
      sb.append(ENDLINE);
      sb.append("<tr><td bgcolor=silver colspan=20><img src='images/clear.gif' height=1></td></tr>");
      sb.append("</tr></table>");


      return sb.toString();
    }
    
    static public String getRedoMenu(int currentAction, OracleDatabaseConnection database)  {
      StringBuffer sb = new StringBuffer();
        
      // Create the Secondary Menu for Sessions...
      sb.append("<table cellpadding = 0 cellspacing=0>");
      //sb.append("<tr><td bgcolor=silver colspan=20><img src='images/clear.gif' height=1></td></tr>");
      sb.append(INDENT);
      sb.append(buildLittleOption("Log Summary", PowerDbaActions.DB_LOG_SUMMARY, currentAction, database));
      sb.append(SMDIV);
      sb.append(buildLittleOption("Log Files", PowerDbaActions.DB_REDO_LOG, currentAction, database));
      sb.append(SMDIV);
      sb.append(buildLittleOption("Log History", PowerDbaActions.DB_LOG_HISTORY, currentAction, database));
      sb.append(ENDLINE);
      sb.append("<tr><td bgcolor=silver colspan=20><img src='images/clear.gif' height=1></td></tr>");
      sb.append("</tr></table>");


      return sb.toString();
    }

    static public String getLatchMenu(int currentAction, OracleDatabaseConnection database)  {
      StringBuffer sb = new StringBuffer();
        
      // Create the Secondary Menu for Sessions...
      sb.append("<table cellpadding = 0 cellspacing=0>");
      //sb.append("<tr><td bgcolor=silver colspan=20><img src='images/clear.gif' height=1></td></tr>");
      sb.append(buildLittleOption("Parent Latches", PowerDbaActions.DB_LATCH_PARENT, currentAction, database));
      sb.append(ENDLINE);
      sb.append("<tr><td bgcolor=silver colspan=20><img src='images/clear.gif' height=1></td></tr>");
      sb.append("</tr></table>");
      return sb.toString();
    }
    
    static public String getMemoryMenu(int currentAction, OracleDatabaseConnection database)  {
      StringBuffer sb = new StringBuffer();
        
      // Create the Secondary Menu for Buffer Cache...
      sb.append("<table cellpadding = 0 cellspacing=0>");
      //sb.append("<tr><td bgcolor=silver colspan=20><img src='images/clear.gif' height=1></td></tr>");
      sb.append(buildLittleOption("Memory Summary", PowerDbaActions.DB_INSTANCE_MEMORY_SUMMARY, currentAction, database));
      sb.append(SMDIV);
      sb.append(buildLittleOption("Memory Detail", PowerDbaActions.DB_INSTANCE_MEMORY_DETAIL, currentAction, database));
      sb.append(SMDIV);
      sb.append(buildLittleOption("Buffer Cache By File", PowerDbaActions.BUFFER_CACHE, currentAction, database));
      sb.append(SMDIV);
      sb.append(buildLittleOption("Buffer Cache By Segment (SLOW)", PowerDbaActions.BUFFER_CACHE_SEG, currentAction, database));
      sb.append(SMDIV);
      sb.append(buildLittleOption("PGA Summary", PowerDbaActions.DB_MEMORY, currentAction, database));
      sb.append(SMDIV);
      sb.append(buildLittleOption("PGA Detail", PowerDbaActions.PGA_DETAILS, currentAction, database));
      sb.append(SMDIV);
      sb.append(buildLittleOption("WebLogic", PowerDbaActions.DB_CARFAX_WEBLOGIC_SUMMARY, currentAction, database));
      sb.append(ENDLINE);
      sb.append("<tr><td bgcolor=silver colspan=20><img src='images/clear.gif' height=1></td></tr>");
      sb.append("</tr></table>");
      return sb.toString();
    }
    
    static public String getPGAMenu(int currentAction, OracleDatabaseConnection database)  {
      StringBuffer sb = new StringBuffer();
        
      // Create the Secondary Menu for Buffer Cache...
      sb.append("<table cellpadding = 0 cellspacing=0>");
      //sb.append("<tr><td bgcolor=silver colspan=20><img src='images/clear.gif' height=1></td></tr>");
      sb.append(buildLittleOption("PGA Summary", PowerDbaActions.DB_MEMORY, currentAction, database));
      sb.append(SMDIV);
      sb.append(buildLittleOption("PGA by Session", PowerDbaActions.PGA_DETAILS, currentAction, database));
      sb.append(ENDLINE);
      sb.append("<tr><td bgcolor=silver colspan=20><img src='images/clear.gif' height=1></td></tr>");
      sb.append("</tr></table>");
      return sb.toString();
    }
    
    static public String getSysstatMenu(int currentAction, OracleDatabaseConnection database)  {
      StringBuffer sb = new StringBuffer();
        
      // Create the Secondary Menu for Sessions...
      sb.append("<table cellpadding = 0 cellspacing=0>");
      //sb.append("<tr><td bgcolor=silver colspan=20><img src='images/clear.gif' height=1></td></tr>");

      sb.append(buildLittleOption("System Waits", PowerDbaActions.DB_SYSTEM_EVENTS, currentAction, database));
      sb.append(SMDIV);
      sb.append(buildLittleOption("System Statistics", PowerDbaActions.DB_SYSSTAT, currentAction, database));
      sb.append(SMDIV);
      sb.append(buildLittleOption("System Ratios", PowerDbaActions.DB_SYSSTAT, currentAction, database));
      sb.append(ENDLINE);
      sb.append("<tr><td bgcolor=silver colspan=20><img src='images/clear.gif' height=1></td></tr>");
      sb.append("</tr></table>");
      return sb.toString();
    }
    
    static private String bigMenuOption(String text, int action, int currentAction, OracleDatabaseConnection database, int menuId ) {
    
      String rval = null;
    
      if ( action == currentAction || getPrimaryMenuOption(currentAction) == menuId ) {
      
        //rval = "  <div class='iframeCell'>" + text + "</div>";   
        rval = "  <div class='iframeCell'><a href='powerdba.jsp?formaction=" + action + 
               "&database="+database.getName()+"' onClick=''>" + text + "</a></div>";

      } else {
        
        rval = "  <div class='iframeHeaderCell'><a href='powerdba.jsp?formaction=" + action + 
               "&database="+database.getName()+"' onClick=''>" + text + "</a></div>";
      }
      return rval;
      
    }
    
    static private String bigMenuOption2(String text, int action, int currentAction, OracleDatabaseConnection database, int menuId) {
    
      String rval = null;
      
      if ( action == currentAction  ||  getTopMenuOption(currentAction) == menuId ) {
      
        if ( text.equals("History") ) {
          rval = "  <div class='iframeCell2'><a href='perfstat.jsp?formaction=" + action + 
                 "&database="+database.getName()+"' onClick=''>" + text + "</a></div>";          
        } else {
        
          rval = "  <div class='iframeCell2'><a href='powerdba.jsp?formaction=" + action + 
                 "&database="+database.getName()+"' onClick=''>" + text + "</a></div>";
        } 

      } else {
      
        if ( text.equals("History") ) {
          rval = "  <div class='iframeHeaderCell2'><a href='perfstat.jsp?formaction=" + action + 
                 "&database="+database.getName()+"' onClick=''>" + text + "</a></div>";          
        } else {
        
          rval = "  <div class='iframeHeaderCell2'><a href='powerdba.jsp?formaction=" + action + 
                 "&database="+database.getName()+"' onClick=''>" + text + "</a></div>";
        }
      }
      return rval;
      
    }
    
    static private String buildLittleOption(String text, int action, int currentAction, OracleDatabaseConnection database) {
    
      String rval = null;
      
      if ( action == currentAction  ) {      
          rval = "<td  bgcolor=white valign='center'>&nbsp;&nbsp;<b><u>" + HtmlComponent.getExtraSmallButton(text,"powerdba.jsp?formaction="
                  + action + "&database=" + database.getName()+"&allinstances=N", null) + "</u></b>&nbsp;&nbsp;</td>\n"; } 
      else {      
        rval = "<td  bgcolor=white valign='center'>&nbsp;&nbsp;" + HtmlComponent.getExtraSmallButton(text,"powerdba.jsp?formaction="
               + action + "&database=" + database.getName()+"&allinstances=N", null) + "&nbsp;&nbsp;</td>\n"; 
      }
      return rval;      
    }
    
    static private String buildSessionOption(String text, int action, int currentAction, OracleDatabaseConnection database, long sid) {
    
      String rval = null;
      
      if ( action == currentAction  ) {
      
        rval = "<td bgcolor=white valign='center'>&nbsp;&nbsp;<b><u>" + HtmlComponent.getExtraSmallButton(text,"powerdba.jsp?formaction="
                + action + "&database=" + database.getName() + "&sid=" + sid+"&allinstances=N", null, "#aaaaaa") + "</u></b>&nbsp;&nbsp;</td>\n";      

      } else {
      
        rval = "<td bgcolor=white valign='center'>&nbsp;&nbsp;" + HtmlComponent.getExtraSmallButton(text,"powerdba.jsp?formaction="
               + action + "&database=" + database.getName() + "&sid=" + sid+"&allinstances=N", null, "#aaaaaa") + "&nbsp;&nbsp;</td>\n"; 
      }
      return rval;
      
    }
    
    static private String getButton(String text, int action, int currentAction, OracleDatabaseConnection database) {

      String rval = null;
      String jsp = null;
      
      if ( text.equals("History") ) {
        jsp = "perfstat.jsp";
      } else {
        jsp = "powerdba.jsp";
      }
      
      if ( action != currentAction ) {

        rval = "<td>" + HtmlComponent.getSmallButton(text, jsp + "?formaction="
                  + action + "&database="+database.getName()+"&allinstances=N", null, 85, 15) + "</td>\n";
      } else 
      {
        rval = "<td>" + HtmlComponent.getSmallButtonReadOnly(text, 85, 15) + "</td>\n";
      }
      
      return rval;

    }
    
    static private int getPrimaryMenuOption(int currentAction) {
      return Math.round(currentAction/100) * 100;
    }
    
    static private int getTopMenuOption(int currentAction) {
      return Math.round(currentAction/1000) * 1000;
    }
    
    static private int getSecondaryMenuOption(int currentAction) {
      return currentAction;
    }
    


}
