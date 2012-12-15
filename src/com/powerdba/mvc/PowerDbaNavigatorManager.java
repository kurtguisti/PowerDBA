package com.powerdba.mvc;

import com.powerdba.chart.ChartGroupHolder;
import com.powerdba.jdbc.*;
import com.powerdba.ActionHolder;
import com.powerdba.LookupDisplayGroup;
import com.powerdba.MenuGenerator;
import com.powerdba.OracleBaseObject;
import com.powerdba.OracleDatabaseConnection;
import com.powerdba.PowerDbaAction;
import com.powerdba.ProcessDAO;
import com.powerdba.Query;
import com.powerdba.QueryHolder;
import com.powerdba.SqlAddress;
import com.powerdba.WaitDAO;
import com.powerdba.chart.ChartGenerator;
import com.powerdba.gui.PageGenerator;
import com.powerdba.gui.PageSet;
import com.powerdba.gui.Window;
import com.powerdba.mvc.PowerDbaActions;
import com.powerdba.mvc.jsp.JspEnvironment;
import com.powerdba.mvc.jsp.SelectEntry;
import com.powerdba.util.DateTranslator;
import com.powerdba.util.StringUtility;
import com.powerdba.util.Tracer;
import com.powerdba.chart.ChartHolder;
import com.powerdba.jdbc.ConnectionConfigurationHolder;


import java.sql.DriverManager;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.lang.StringUtils;

public class PowerDbaNavigatorManager {

    public static final String DIV = "<table cellspacing=0 cellpadding=0 width=100%><tr><td><img src='images/clear.gif' height=10></td></tr><tr><td bgcolor=#e5e5e5><img src=images/clear.gif height=2></td></tr><tr><td><img src='images/clear.gif' height=10></td></tr></table>";
    private static int INITIAL_PAGE = PowerDbaActions.DB_SESSIONS;
    private static String OBJECT_NAME = "PowerDbaManager";
    
    private PowerDbaNavigatorView myView;
    private OracleDatabaseConnection database;
    private boolean connected = false;
    private int lastAction = 0;
    private String lastDatabase;
    private int action=0;
    private String actionString;
    private PowerDbaAction actionObj;
    private JspEnvironment env;
    private String allInstances;

    public PowerDbaNavigatorManager(JspEnvironment env) {
      this.env = env;
      this.myView = new PowerDbaNavigatorView(env);
    }

    // process method returns the view.
    public PowerDbaNavigatorView process() throws Exception {

      Tracer.log("Starting the PowerDBAManager process() method.  Incoming formAction is " + env.getParameter("formaction"), Tracer.DEBUG, this); 
      PageSet pageSet; 
      StringBuffer sb = new StringBuffer();
      Query q;

      try {
      
        // Get the specified action from the environment...
        this.actionString = env.getParameter("formaction");
        if ( actionString.equals("0") || actionString.equals("1") ) {
        	action = INITIAL_PAGE; 
          actionString = "INITIAL_PAGE";
        }
        
        if ((Integer) env.getSessionAttribute("lastaction") != null) {
            this.lastAction = ((Integer) env.getSessionAttribute("lastaction")).intValue();
        }
        
        // This will override the isRac setting in the actions.xml file.
        if ( env.getParameter("allinstances").equals("") ) {
        	this.allInstances = "N";
        } else {
          this.allInstances =  env.getParameter("allinstances");
        }    
        
        this.lastDatabase = (String) env.getSessionAttribute("lastdatabase");        
        myView.setRefreshInterval(env.getInt("refreshinterval"));
        
        // If the incoming request has a database descriptor on board, instantiate a database object based on this name.
        if ( env.getParameter("database").equals("") || 
        		 env.getParameter("database").equals("-1") ) { 
        } else {
          setDatabase(env.getParameter("database")); 
        }
        
        setDatabaseList();
        
        Tracer.log("actionString: " + this.actionString,Tracer.DEBUG,"PowerDBAManager");
        
        // Re-assign the action based on some conditions
        if ( this.database == null ) {
          // No database descriptor name found in the incoming request
          this.action = PowerDbaActions.NEW;
          this.actionString = new Integer(PowerDbaActions.NEW).toString();
        } else if ( !this.database.getName().equals(this.lastDatabase) && this.lastAction == PowerDbaActions.DB_SESS_ZOOM ) {
          // Session Zoom action and database has changed.  Assume that we never want to switch db's on this screen.
          // TODO: Data drive this, so that for certain screens, a database switch reroutes the action.
          Tracer.log("Rerouting request to DB_SESSIONS action", Tracer.DEBUG, this);
          this.action = PowerDbaActions.DB_SESSIONS;
          this.actionString = new Integer(PowerDbaActions.DB_SESSIONS).toString();
        }
        Tracer.log("actionString3: " + this.actionString,Tracer.DEBUG,"PowerDBAManager");

        if ( StringUtils.isNumeric(this.actionString) ) {
          this.action = new Integer(actionString).intValue();
        	this.actionString = PowerDbaActions.getPageCode(new Integer(actionString).intValue());
          Tracer.log("actionString4: " + this.actionString,Tracer.DEBUG,"PowerDBAManager");
        } 
        

        
        // If we are connected, set the time from the database and the lookup lists for the page.
        if ( connected ) {
            setDatabaseTime();
            setLookupLists();
        }
        
        dumpEnvironmentToLog();
        
        if ( this.actionString == null ) {
        	throw new SQLException("The actionString for action " + this.action + " was not found in PowerDbaPages hash Lookup.");
        }
        
        if ( ActionHolder.getList().contains(this.actionString) && connected ) {

	        	actionObj = ActionHolder.getAction(actionString);
	        	
	        	Tracer.log("Processing action " + actionObj.getName() + " from xml defined action.", Tracer.DEBUG, this);

	        	// Do we override the isRAC that is set in the action xml.
	        	boolean masterIsRac = 
	          	this.allInstances.equals("Y")?true:actionObj.isRac();
	        	
	          // Run each of the queries associated with the action.
	        	ArrayList pages = new ArrayList();
	        	for ( int i=0; i<actionObj.getQueries().size(); i++ ) {
	        		pages.addAll( this.getQueryResults( (String)actionObj.getQueries().get(i), 
	        				                                masterIsRac));     		
	        	}

	          myView.setWindow(new Window(actionObj.getTitle(), 
	                                      actionObj.getMenu1(),
	                                      actionObj.getMenu2(), 
	                                      pages,
	                                      masterIsRac));        	
        }
        
        myView.setCurrAction(this.action);
        myView.setCurrActionString(this.actionString);

        switch (action) {
        
          case PowerDbaActions.CHARTS_SUMMARY:          
            myView.setTitle("System Overview");
            myView.setMenus(MenuGenerator.CHARTS_MENU);
            setChartGroup("SUMMARY"); 

            q = QueryHolder.getQuery(database, env, "sessions");
            q.setDatabase(database);
            PageGenerator pg = new PageGenerator(q.getPageSet().getPage(1));
            pg.setColor1("white");
            pg.setColor2("#eeeeee");
            pg.setHeadingBackgoundColor("white");
            pg.setDatabase(database.getName());
            myView.setHtml(myView.getHtml() + "<center>" + pg.getHtmlNew() + "</center>");            
            ChartHolder.cleanCharts(1);
            break;
            
          case PowerDbaActions.CHARTS_SESSIONS:          
            myView.setTitle("Session Charts (Current Activity)");
            myView.setMenus(MenuGenerator.CHARTS_MENU);         
            setChartGroup("SESSION");            
            break;
            
          case PowerDbaActions.CHARTS_CLUSTER:          
            myView.setTitle("Clusterwide Charts");
            myView.setMenus(MenuGenerator.CHARTS_MENU);         
            setChartGroup("CLUSTER_WIDE");            
            break;
            
          case PowerDbaActions.CHARTS_CPUIO:          
            myView.setTitle("CPU and IO Charts");
            myView.setMenus(MenuGenerator.CHARTS_MENU);         
            setChartGroup("CLUSTER_CPUIO");  
            q = QueryHolder.getQuery(database, env, "carfaxcontrolmservices");
            q.setDatabase(database);
            pg = new PageGenerator(q.getPageSet().getPage(1));
            pg.setColor1("white");
            pg.setColor2("#eeeeee");
            pg.setHeadingBackgoundColor("white");
            pg.setDatabase(database.getName());
            myView.setHtml(myView.getHtml() + "<left>" + pg.getHtmlNew() + "</left>"); 
            q = QueryHolder.getQuery(database, env, "carfaxcontrolmservicesdetail");
            q.setDatabase(database);
            pg = new PageGenerator(q.getPageSet().getPage(1));
            pg.setColor1("white");
            pg.setColor2("#eeeeee");
            pg.setHeadingBackgoundColor("white");
            pg.setDatabase(database.getName());
            myView.setHtml(myView.getHtml() + "<left>" + pg.getHtmlNew() + "</left>"); 
            ChartHolder.cleanCharts(1);
            break;
            
          case PowerDbaActions.CHARTS_CPU:          
            myView.setTitle("CPU Chart");
            myView.setMenus(MenuGenerator.CHARTS_MENU);         
            setChartGroup("CLUSTER_CPU");  
            q = QueryHolder.getQuery(database, env, "carfaxcontrolmservices");
            q.setDatabase(database);
            pg = new PageGenerator(q.getPageSet().getPage(1));
            pg.setColor1("white");
            pg.setColor2("#eeeeee");
            pg.setHeadingBackgoundColor("white");
            pg.setDatabase(database.getName());
            myView.setHtml(myView.getHtml() + "<left>" + pg.getHtmlNew() + "</left>"); 
            q = QueryHolder.getQuery(database, env, "carfaxcontrolmservicesdetail");
            q.setDatabase(database);
            pg = new PageGenerator(q.getPageSet().getPage(1));
            pg.setColor1("white");
            pg.setColor2("#eeeeee");
            pg.setHeadingBackgoundColor("white");
            pg.setDatabase(database.getName());
            myView.setHtml(myView.getHtml() + "<left>" + pg.getHtmlNew() + "</left>"); 
            ChartHolder.cleanCharts(1);
            break;
            
          case PowerDbaActions.CHARTS_METRICS:          
            myView.setTitle("Metrics Charts");
            myView.setMenus(MenuGenerator.CHARTS_MENU);         
            setChartGroup("METRICS");            
            break;
            
          case PowerDbaActions.CHARTS_SHREDDER_THROUGHPUT:          
            myView.setTitle("Shredding Throughput");
            myView.setMenus(MenuGenerator.AQ_MENU_DEST);         
            setChartGroup("SHREDDER_THROUGHPUT");            
            break;
            
          case PowerDbaActions.CHARTS_MESSAGE_RATE:          
            myView.setTitle("Message Creation Rates");
            myView.setMenus(MenuGenerator.AQ_MENU_DEST);         
            setChartGroup("MESSAGE_GENERATION_RATE");            
            break;

            
          case PowerDbaActions.CHARTS_CONTENTION:          
            myView.setTitle("Contention Charts");
            myView.setMenus(MenuGenerator.CHARTS_MENU);         
            setChartGroup("CONTENTION");             
            break;
            
          case PowerDbaActions.CHARTS_IO:          
            myView.setTitle("IO Charts (Recent Historical Data)");
            myView.setMenus(MenuGenerator.CHARTS_MENU);         
            setChartGroup("IO");            
            break;
            
          case PowerDbaActions.CHARTS_ACTIVITY:
            myView.setTitle("Activity Charts (Recent Historical Data)");
            myView.setMenus(MenuGenerator.CHARTS_MENU);         
            setChartGroup("ACTIVITY");            
            break;
            
          case PowerDbaActions.DB_SESS_ZOOM:
                  
            long sid = env.getLong("sid");
	          String subAction = env.getParameter("subaction");
	          
	          if ( subAction.equals("troff") || subAction.equals("tron") ) {
	              
	            if ( !env.getParameter("dbausername").equals("") && !env.getParameter("dbapassword").equals("") ) {
	              setNonPooledDatabase(env.getParameter("dbausername"), env.getParameter("dbapassword"));
	            } else {
	              String msg = "Username and Password must both be specified";
	              myView.setMessage(msg);
	              throw new SQLException(msg);
	            }
	             
	            if ( subAction.equals("troff") ) {
	                ProcessDAO.stopTrace(database, sid);
	                myView.setMessage("Tracing for " + sid + " stopped.");
	            } else if ( subAction.equals("tron") ) {
	                ProcessDAO.startTrace(database, sid);
	                myView.setMessage("Tracing for " + sid + " started.");
	            } 	
	          }
        
            myView.setTitle("Session Details");
            myView.setMenus(MenuGenerator.INSTANCE_MENU, MenuGenerator.SESSIONS_MENU);
            myView.setMenu3(MenuGenerator.SESSION_DETAIL_MENU);
            
            SqlAddress sa = null;
            String sql = null;
            
            if ( validateSession(sid) ) {   
            
	            myView.setSid(sid);
	            
	            sa = ProcessDAO.getSqlAddress(sid, database);                        
	            sql = ProcessDAO.getSqlTextFromLC(sa, database);
	            
	            myView.setSql(sql);
	            myView.setHash(sa.getHashValue());
	            
	            q = QueryHolder.getQuery(database, env, "session_detail");
	            if ( q != null ) {
	              q.setBindValue("sid", Long.toString(sid));
	              q.setDatabase(database);
	              sb.append(q.getHtml(ProcessDAO.DETAIL,6));
	            }
	            
	            sb.append(DIV);
	            
	            sb.append(ProcessDAO.buildCurrentSql(sql, sid, database, "Current SQL",sa.getHashValue(), 5));
	            sb.append(ProcessDAO.buildSqlStats(sa, database));
	            
	            sb.append(DIV);
	            
	            sb.append("<table cellspacing=0 cellpadding=0>");
	            sb.append("<tr>");
	            sb.append("<td valign=top>");
	            q = QueryHolder.getQuery(database, env, "waithistory");
	            if ( q != null ) {
	              q.setBindValue("sid", Long.toString(sid));
	              q.setDatabase(database);
	              sb.append(q.getHtml(ProcessDAO.TABLE));
	            }
	            sb.append("</td>");   
	            sb.append("<td><img src=images/clear.gif width=10 height=1></td>");
	            sb.append("<td bgcolor=#e5e5e5><img src=images/clear.gif width=2 height=1></td>");
	            sb.append("<td><img src=images/clear.gif width=10 height=1></td>");
	            sb.append("<td valign=top>");
	            q = QueryHolder.getQuery(database, env, "timemodel");
	            if ( q != null ) {
	              q.setBindValue("sid", Long.toString(sid));
	              q.setDatabase(database);
	              sb.append(q.getHtml(ProcessDAO.TABLE));
	            }
	            sb.append("</td>");
	            sb.append("</tr>");
	
	            sb.append("</table>");
	            
	            sb.append(DIV);        
	            q = QueryHolder.getQuery(database, env, "longops_sid");
	            if ( q != null ) {
	              q.setBindValue("sid", Long.toString(sid));
	              q.setDatabase(database);
	              sb.append(q.getHtml(ProcessDAO.TABLE));
	            }
	            sb.append(DIV);
	
	            sb.append("<table cellspacing=0 cellpadding=0>");
	            sb.append("<tr>");
	
	            sb.append("<td valign=top>");   
	            sb.append("  <table cellspacing=0 cellpadding=0>");
	            if ( this.database.getVersion().getVersion1() > 9 ) {
		            sb.append("  <tr><td>");
		            sb.append(   ProcessDAO.buildSessionWaitClassSummary(sid, database));
		            sb.append("  </td></tr>");
		            sb.append("  <tr><td bgcolor=white><img src=images/clear.gif height=10></td></tr>");
		            sb.append("  <tr><td bgcolor=#e5e5e5><img src=images/clear.gif height=2></td></tr>");
		            sb.append("  <tr><td bgcolor=white><img src=images/clear.gif height=10></td></tr>");
	            }
	            sb.append("  <tr><td>");
	            sb.append(   ProcessDAO.buildSessionWaitSummary(sid, database));
	            sb.append("  </td></tr>");
	            sb.append("  </table>");
	            sb.append("</td>");
	            // Vertical line
	            sb.append("<td><img src=images/clear.gif width=1 height=1></td>");
	            sb.append("<td bgcolor=#e5e5e5><img src=images/clear.gif width=2 height=1></td>");
	            sb.append("<td><img src=images/clear.gif width=10 height=1></td>");
	            //
	            sb.append("<td valign=top>");
	            sb.append("  <table cellspacing=0 cellpadding=0><tr><td>");
	            sb.append(   ProcessDAO.buildSessionIoSummary(sid,database));
	            sb.append("  </td></tr>");
	            sb.append("  <tr><td bgcolor=white><img src=images/clear.gif height=10></td></tr>");
	            sb.append("  <tr><td bgcolor=#e5e5e5><img src=images/clear.gif height=2></td></tr>");
	            sb.append("  <tr><td bgcolor=white><img src=images/clear.gif height=10></td></tr>");
	            sb.append("  <tr><td>");
	            q = QueryHolder.getQuery(database, env, "trx_sid");
	            if ( q != null ) {
	              q.setBindValue("sid", Long.toString(sid));
	              q.setDatabase(database);
	              sb.append(q.getHtml(ProcessDAO.TABLE));
	            }
	            sb.append("  </td></tr>");
	            sb.append("  <tr><td bgcolor=white><img src=images/clear.gif height=10></td></tr>");
	            sb.append("  <tr><td bgcolor=#e5e5e5><img src=images/clear.gif height=2></td></tr>");
	            sb.append("  <tr><td bgcolor=white><img src=images/clear.gif height=10></td></tr>");
	            sb.append("  <tr><td>");
	            q = QueryHolder.getQuery(database, env, "sessionsorts");
	            if ( q != null ) {
	              q.setBindValue("sid", Long.toString(sid));
	              q.setDatabase(database);
	              sb.append(q.getHtml(ProcessDAO.TABLE));
	            }
	            sb.append("  </td></tr>");
	            sb.append("</table>");
	            sb.append("</td>");
	            sb.append("</tr>");
	            
	            sb.append("</table>");
	            
	            //sb.append(DIV);
	            //sb.append(ProcessDAO.buildSessionPx(sid, database));
	            
	            sb.append(DIV);
	                       
	            myView.setHtml(sb.toString());
            }
                        
            break;
            
      case PowerDbaActions.DB_SESSION_STATS:
              
        sid = env.getLong("sid");
        
        myView.setTitle("Statistics");
        myView.setMenus(MenuGenerator.INSTANCE_MENU, MenuGenerator.SESSIONS_MENU);
        myView.setMenu3(MenuGenerator.SESSION_DETAIL_MENU);
        
        validateSession(sid);            
        myView.setSid(sid);
        
        sa = ProcessDAO.getSqlAddress(sid, database);                        
        sql = ProcessDAO.getSqlTextFromLC(sa, database);
        
        myView.setSql(sql);
        myView.setHash(sa.getHashValue());

        q = QueryHolder.getQuery(database, env, "session_detail");
        if ( q != null ) {
          q.setBindValue("sid", Long.toString(sid));
          q.setDatabase(database);
          sb.append(q.getHtml(ProcessDAO.DETAIL,6));
        }
        sb.append(DIV);
        sb.append(ProcessDAO.buildSessionStats(sid, database));
        
        myView.setHtml(sb.toString());
                    
        break;
            
          case PowerDbaActions.LC_HEAVY_SQL: 
            myView.setPageTitle("Heavy Sql");
            myView.setMenu1(MenuGenerator.INSTANCE_MENU);
            myView.setMenu2(MenuGenerator.LIBRARY_CACHE_MENU);
            myView.setHtml(ProcessDAO.getHeavySql(database, 20));

            break;
            
          case PowerDbaActions.DB_ROLES_ASSIGNED: 
            String username = env.getParameter("username");
            myView.setPageTitle("Roles Assigned to User " + username);
            myView.setMenu1(MenuGenerator.DATABASE_MENU);
            myView.setPage(ProcessDAO.getRoles(database, username).getPage(1));

            break;
            
          case PowerDbaActions.DB_OBJECT_SUMMARY: 
            username = env.getParameter("username");
            myView.setPageTitle("Objects for User " + username);
            myView.setMenu1(MenuGenerator.DATABASE_MENU);
            myView.setPage(ProcessDAO.getObjectSummary(database, username).getPage(1));

            break;
            
          case PowerDbaActions.DB_OBJECT_LIST: 
            String schema = env.getParameter("schema");
            String oType = env.getParameter("type");
            myView.setPageTitle(oType + " objects in the " + schema + " Schema.");
            myView.setMenu1(MenuGenerator.DATABASE_MENU);
            if ( oType.equals("REFRESH GROUP") ) {
              myView.setPage(ProcessDAO.getRefreshGroupList(database, schema).getPage(1));
            } else {
              myView.setPage(ProcessDAO.getObjectList(database, oType, schema).getPage(1));
            }

            break;
            
          case PowerDbaActions.DB_OBJECT_DETAIL: 

            schema = env.getParameter("owner");
            oType = env.getParameter("type");
            String obj = env.getParameter("objname");
            myView.setPageTitle("Object Definition for " + oType + " " + schema + "." + obj);
            myView.setMenu1(MenuGenerator.DATABASE_MENU);
            
            String html = null;
            if ( oType.equals("PACKAGE") || oType.equals("PACKAGE BODY") || oType.equals("PROCEDURE") || 
                 oType.equals("FUNCTION") || oType.equals("TRIGGER") || oType.equals("JAVA SOURCE") ||
                 oType.equals("TYPE") || oType.equals("TYPE BODY") ) {
              html = ProcessDAO.getSource(schema, obj, oType, database);
            }
            else if ( oType.equals("TABLE") )              html = getTableHtml(schema, obj, database);
            else if ( oType.equals("INDEX") )              html = getIndexHtml(schema, obj, database);
            else if ( oType.equals("VIEW") )               html = ProcessDAO.getView(schema, obj, database);
            else if ( oType.equals("SEQUENCE") )           html = ProcessDAO.getSequence(schema, obj, database);
            else if ( oType.equals("MATERIALIZED VIEW") )  html = ProcessDAO.getMView(schema, obj, database);
            else if ( oType.equals("DATABASE LINK") )      html = ProcessDAO.getDBLink(schema, obj, database);
            else if ( oType.equals("QUEUE") )              html = ProcessDAO.getAQ(schema, obj, database);
            else if ( oType.equals("EVALUATION CONTEXT") ) html = ProcessDAO.getEvaluationContext(schema, obj, database);
            else if ( oType.equals("LOB") )                html = ProcessDAO.getLob(schema, obj, database);
            else if ( oType.equals("RULE") )               html = ProcessDAO.getRule(schema, obj, database);
            else if ( oType.equals("DATAPUMP TABLE") )     html = ProcessDAO.getDatapumpTable(schema, obj, database);
            else if ( oType.equals("SYNONYM") )            html = ProcessDAO.getSynonym(schema, obj, database);
            else if ( oType.equals("REFRESH GROUP") ) {
	            obj = env.getParameter("objname");
	            String refgroup = env.getParameter("refgroup"); 
	            myView.setPageTitle("Object Definition for " + oType + " " + schema + "." + obj);
              StringBuffer refChild = new StringBuffer();
              refChild.append(ProcessDAO.getRefreshGroup(schema, refgroup, database));
              refChild.append("<BR>");
	            refChild.append(ProcessDAO.getRefreshGroupChildren(schema, refgroup, database));
              html = refChild.toString();
            }
            myView.setHtml(html);
            break;
            
          case PowerDbaActions.DB_ROLE_ROLES: 
            String role = env.getParameter("role");
            myView.setPageTitle("Roles Assigned to Role " + role);
            myView.setMenu1(MenuGenerator.DATABASE_MENU);
            myView.setPage(ProcessDAO.getRoleRoles(database, role).getPage(1));
            break;
            
          case PowerDbaActions.DB_ROLE_OBJECTS: 
            role = env.getParameter("role");
            myView.setPageTitle("Object Privs Assigned to Role " + role);
            myView.setMenu1(MenuGenerator.DATABASE_MENU);
            myView.setPage(ProcessDAO.getRoleObjects(database, role).getPage(1));
            break;
            
          case PowerDbaActions.DB_SYSTEM_PRIVS: 
            role = env.getParameter("role");
            myView.setPageTitle("System Privs Assigned to Role " + role);
            myView.setMenu1(MenuGenerator.DATABASE_MENU);
            myView.setPage(ProcessDAO.getRoleSystemPrivs(database, role).getPage(1));
            break;
            
          case PowerDbaActions.DB_SYS_PRIVS: 
            String grantee = env.getParameter("grantee");
            myView.setPageTitle("System Privs Assigned to " + grantee);
            myView.setMenu1(MenuGenerator.DATABASE_MENU);
            myView.setPage(ProcessDAO.getSysPrivs(database, grantee).getPage(1));
            break;
            
          case PowerDbaActions.DB_DIRECT_GRANTS: 
            username = env.getParameter("username");
            myView.setPageTitle("Direct Grants to " + username);
            myView.setMenu1(MenuGenerator.DATABASE_MENU);
            myView.setPage(ProcessDAO.getDirectGrants(database, username).getPage(1));
            break;
            
          case PowerDbaActions.LC_BAD_SQL: 
            myView.setTitle("Bad Sql");
            myView.setHtml(ProcessDAO.getBadSql(database, 20));
            myView.setMenu1(MenuGenerator.INSTANCE_MENU);
            myView.setMenu2(MenuGenerator.LIBRARY_CACHE_MENU);
            break;
            
          case PowerDbaActions.LC_SUMMARY:  
            myView.setTitle("Library Cache Summary");
            myView.setMenus(MenuGenerator.INSTANCE_MENU, MenuGenerator.LIBRARY_CACHE_MENU);
          
            q = QueryHolder.getQuery(database, env, "librarycache");
            pg = new PageGenerator(q.getPageSet().getFirstPage());
            sb.append(pg.getHtmlNew());  
            
            sb.append("<BR>");
            
            String whereClause = "name like '%plsql%' or name like '%shared%' or name like '%cursor%' or name like '%optimizer%'";
            sb.append(ProcessDAO.getInstanceParameters(database, whereClause));
            myView.setHtml(sb.toString());
            break;

          case PowerDbaActions.DB_LATCH_CHILDREN:  
            String latchNum = env.getParameter("latchnum");
            myView.setTitle("Latch Children for latch " + latchNum);
            myView.setHtml(ProcessDAO.getChildLatches(database, latchNum));
            myView.setMenu1(MenuGenerator.INSTANCE_MENU);
            myView.setMenu2(MenuGenerator.LATCH_MENU);
            break;
            
          case PowerDbaActions.LC_PARSING_SUMMARY:
            myView.setTitle("Parsing Parameter Utilization");
            myView.setMenu1(MenuGenerator.INSTANCE_MENU);
            myView.setMenu2(MenuGenerator.LIBRARY_CACHE_MENU);
            myView.setHtml(ProcessDAO.getParsingSummary(database));
            break;
                       
          case PowerDbaActions.DB_OBJECT:  
            String objectName = env.getParameter("oracleobjectname");
            OracleBaseObject obo = ProcessDAO.getOracleObject(objectName, database);
            
            html = new String();
            if ( obo.getType().equals("TABLE") ) {
              html = getTableHtml(obo.getOwner(), obo.getName(), database);
            }
            else if ( obo.getType().equals("INDEX") ) {
              html = getIndexHtml(obo.getOwner(), obo.getName(), database);
            }
            else if ( obo.getType().equals("VIEW") ) {
              html = ProcessDAO.getView(obo.getOwner(), obo.getName(), database);
            }
            
            myView.setTitle(StringUtility.initCap("Object Information for " + obo.getType()) +  " " + obo.getOwner() + "." + obo.getName());
            myView.setHtml(html);
            myView.setMenu1(MenuGenerator.DATABASE_MENU);
            break;
            
          case PowerDbaActions.DB_INDEX_PERFVIEW:  
            String owner = env.getParameter("owner").toUpperCase();
            String name = env.getParameter("name").toUpperCase();
            html = getIndexHtml(owner, name, database);              
            myView.setTitle(StringUtility.initCap("Tuning Information for Index " +  " " + owner + "." + name));
            myView.setHtml(html);
            myView.setMenu1(MenuGenerator.DATABASE_MENU);
            break;
            
          case PowerDbaActions.DB_TABLE_PERFVIEW:  
            owner = env.getParameter("owner").toUpperCase();
            name = env.getParameter("name").toUpperCase();
            html = getTableHtml(owner, name, database);              
            
            myView.setTitle(StringUtility.initCap("Tuning Information for Table " +  " " + owner + "." + name));
            myView.setHtml(html);
            myView.setMenu1(MenuGenerator.DATABASE_MENU);
            //myView.setMenu2(MenuGenerator.LATCH_MENU);
            break;
                        
          case PowerDbaActions.DB_LOG_HISTORY:               
            myView.setTitle("Redo Log History"); 
            myView.setMenus(MenuGenerator.INSTANCE_MENU, MenuGenerator.REDO_MENU);
            myView.setDateList(ProcessDAO.getDateList(database));
            String date = env.getParameter("rdate");
            if ( date.equals("") ) date = DateTranslator.getStringDate(database.getDateTime(), 
                                                                       DateTranslator.HISTORY_DATEONLY);
            myView.setCurrentDate(date);
            if ( !date.equals("") ) {
              pageSet = ProcessDAO.getLogHistory(database, date);
              env.setSessionAttribute("pageset", pageSet);
              myView.setPage(pageSet.getPage(1));
            }
            break;
            
          case PowerDbaActions.DB_LOG_HISTORY_SECOND:
            myView.setTitle("Redo Log History");
            String minute = env.getParameter("time");
            pageSet = ProcessDAO.getLogHistorySeconds(database, minute);
            env.setSessionAttribute("pageset", pageSet);
            myView.setPage(pageSet.getPage(1));
            myView.setMenu1(MenuGenerator.INSTANCE_MENU);
            myView.setMenu2(MenuGenerator.REDO_MENU);
            break;


	          
          case PowerDbaActions.DB_INSTANCE_MEMORY_SUMMARY:
            myView.setTitle("Instance Memory Summary");
            myView.setHtml(ProcessDAO.getInstanceMemorySummary(database));
            myView.setMenu1(MenuGenerator.INSTANCE_MENU);
            myView.setMenu2(MenuGenerator.MEMORY_MENU);
            break;
              
          case PowerDbaActions.DB_SQL_CHILDREN:             
            myView.setMenus(MenuGenerator.INSTANCE_MENU, MenuGenerator.SESSIONS_MENU);
            myView.setMenu3(MenuGenerator.SESSION_DETAIL_MENU);
            myView.setTitle("Open Cursor Detail");
            String address = env.getParameter("address");
            String hash = env.getParameter("hash");
            sid = env.getLong("sid");
            myView.setAddress(address);
            myView.setHash(hash);
            myView.setSid(sid);
            String textHtml = ProcessDAO.buildCurrentSql(
              ProcessDAO.getSqlTextFromLC(new SqlAddress("0",hash),database),sid,database,"SQL",hash,10);
            String statsHtml =  ProcessDAO.buildSqlStats(new SqlAddress(address, hash), database);
            myView.setHtml(textHtml + statsHtml);
            break;
             
          case PowerDbaActions.DB_INSTANCE_MEMORY_DETAIL:               
            myView.setHtml(ProcessDAO.getInstanceMemoryDetails(database));
            myView.setMenu1(MenuGenerator.INSTANCE_MENU);
            myView.setMenu2(MenuGenerator.MEMORY_MENU);
            break;


            
          case PowerDbaActions.DB_STREAMS:
            myView.setTitle("Streams Error Summary");
            myView.setHtml(ProcessDAO.getStreamsHtml(database));
            myView.setMenus(MenuGenerator.MESSAGING_MENU, MenuGenerator.STREAMS_MENU);
            break;
            
          //case PowerDbaActions.DB_STREAMS_ERROR_QUEUE:
          //  myView.setMenus(MenuGenerator.MESSAGING_MENU, MenuGenerator.STREAMS_MENU);
          //  myView.setTitle("Apply Errors");
          //  myView.setHtml(ProcessDAO.getApplyErrors(database));
          //  break;
            

                
          case PowerDbaActions.LC_OPEN_CURSORS:
            sid = env.getLong("sid");
            myView.setSid(sid);
            myView.setHtml(ProcessDAO.buildAllSql(sid, database));
            myView.setTitle("Open Cursors");
            myView.setMenus(MenuGenerator.INSTANCE_MENU, MenuGenerator.SESSIONS_MENU);
            myView.setMenu3(MenuGenerator.SESSION_DETAIL_MENU);
            break;
            
          case PowerDbaActions.DB_SESSION_CONNECT_INFO:
            sid = env.getLong("sid");
            myView.setSid(sid);
            myView.setHtml(ProcessDAO.getSessionConnectInfo(sid, database));
            myView.setTitle("Connect Info");
            myView.setMenus(MenuGenerator.INSTANCE_MENU, MenuGenerator.SESSIONS_MENU);
            myView.setMenu3(MenuGenerator.SESSION_DETAIL_MENU);
            break;
            
          case PowerDbaActions.DB_SESSION_ACCESS:
            sid = env.getLong("sid");
            myView.setSid(sid);
            myView.setHtml(ProcessDAO.getSessionAccess(sid, database));
            myView.setTitle("Objects Accessed");
            myView.setMenus(MenuGenerator.INSTANCE_MENU, MenuGenerator.SESSIONS_MENU);
            myView.setMenu3(MenuGenerator.SESSION_DETAIL_MENU);
            break;
              
          case PowerDbaActions.DB_SESSION_LOCKS_HELD:
            myView.setTitle("Locks Held");
            myView.setMenus(MenuGenerator.INSTANCE_MENU, MenuGenerator.SESSIONS_MENU);
            myView.setMenu3(MenuGenerator.SESSION_DETAIL_MENU);
            sid = env.getLong("sid");
            myView.setSid(sid);
            myView.setHtml(ProcessDAO.getSessionLocksHeld(sid, database));
            break;
            
          case PowerDbaActions.DB_SESSION_LOCKS_REQUESTED:
            myView.setTitle("Locks Requested");
            myView.setMenus(MenuGenerator.INSTANCE_MENU, MenuGenerator.SESSIONS_MENU);
            myView.setMenu3(MenuGenerator.SESSION_DETAIL_MENU);
            sid = env.getLong("sid");
            myView.setSid(sid);
            myView.setHtml(ProcessDAO.getSessionLocksRequested(sid, database));
            break;
            
          case PowerDbaActions.DB_TS:
            Tracer.log("Process DB_TS", Tracer.DEBUG, this);
            String ts = env.getParameter("ts");
            myView.setHtml(ProcessDAO.getTablespaceHtml(ts,database));
            myView.setTitle("Map of Tablespace " + ts);
            myView.setMenu1(MenuGenerator.DATABASE_MENU);
            break;
              
          case PowerDbaActions.DB_FILE:
            long fileId = env.getLong("fileid");
            Tracer.log("Process DB_FILE " + fileId, Tracer.DEBUG, this);
            myView.setHtml(ProcessDAO.getFileHtml(fileId,database));
            myView.setTitle("File Details for File Id: " + fileId);
            myView.setMenu1(MenuGenerator.DATABASE_MENU);
            break;
            
          case PowerDbaActions.DB_SEGMENT:
            String segmentName = env.getParameter("seg");
            Tracer.log("Process DB_SEGMENT " + segmentName, Tracer.DEBUG, this);
            myView.setHtml(ProcessDAO.getSegment(segmentName, database));
            myView.setTitle("Segment Details for: " + segmentName);
            myView.setMenu1(MenuGenerator.DATABASE_MENU);
            break;
              
          case PowerDbaActions.EXPLAIN_PLAN:
            myView.setMenu1(MenuGenerator.INSTANCE_MENU);
            myView.setSid(env.getLong("sid"));
            
            if ( !env.getParameter("explainusername").equals("") && !env.getParameter("explainpassword").equals("") ) {
              setNonPooledDatabase(env.getParameter("explainusername"), env.getParameter("explainpassword"));
            }
            
            String sqlToExplain = null; 
            long hashValue = 0;
            if ( env.getParameter("sqltoexplain").equals("") || env.getParameter("sqltoexplain").equals("null") ) {
              hashValue = env.getLong("hash");
              myView.setHash(Long.toString(hashValue));
              sqlToExplain = ProcessDAO.getExplainOutput(hashValue, database);
              myView.setTitle("Explain Plan for Hash: " + hashValue);
            } else {
              sqlToExplain = env.getParameter("sqltoexplain");
              myView.setTitle("Explain Plan for SQL");
            }
            
            myView.setSql(sqlToExplain);
            Tracer.log(sqlToExplain, Tracer.DEBUG, this);
            myView.setHtml(ProcessDAO.buildExplainOutput(sqlToExplain, database));
            if ( !env.getParameter("explainusername").equals("") && !env.getParameter("explainpassword").equals("") ) {
              try {
                this.database.getConn().close();
              } catch ( SQLException se ) {}
            }   
            break;
            
          case PowerDbaActions.STREAMS_MANAGE_TRX_EXEC:
              myView.setMenus(MenuGenerator.MESSAGING_MENU, MenuGenerator.STREAMS_MENU);
              
              if ( !env.getParameter("streamsusername").equals("") && !env.getParameter("streamspassword").equals("") ) {
                setNonPooledDatabase(env.getParameter("streamsusername"), env.getParameter("streamspassword"));
              } else {
                String msg = "Username and Password must both be specified";
                myView.setMessage(msg);
                throw new SQLException(msg);
              }
              
              subAction = env.getParameter("subaction");
              String trx = env.getParameter("streamstrx");
              
              if ( subAction.equals("exec") ) {
                  ProcessDAO.execStreamsTrx(database, trx);
                  myView.setMessage("Transaction " + trx + " successfully reexecuted.");
              } else if ( subAction.equals("override") ) {
                  ProcessDAO.execStreamsTrxOverride(database, trx);
                  myView.setMessage("Transaction " + trx + " successfully reexecuted with conflict resolution overridden.");
              } else if ( subAction.equals("del") ) {
                  ProcessDAO.deleteStreamsTrx(database, trx);
                  myView.setMessage("Transaction " + trx + " successfully removed from the error queue.");
              } else if ( subAction.equals("overridedelete") ) {
                  ProcessDAO.execStreamsTrxOverride(database, trx);
                  ProcessDAO.deleteStreamsTrx(database, trx);
                  myView.setMessage("Transaction " + trx + " successfully executed and removed from the error queue.");
              }

              if ( !env.getParameter("streamsusername").equals("") && !env.getParameter("streamspassword").equals("") ) {
                try {
                  this.database.getConn().close();
                } catch ( SQLException se ) {}
              }   
              break;
              
          case PowerDbaActions.DB_SQL_PLAN_STATISTICS:
            Tracer.log("Process EXPLAIN_PLAN ", Tracer.DEBUG, this);
            hashValue = env.getLong("hash");
            sid = env.getLong("sid");
            myView.setHtml(ProcessDAO.getSqlPlanStatistics(hashValue, sid, database));
            myView.setTitle("Runtime Plan Stats for Hash: " + hashValue);
            myView.setMenu1(MenuGenerator.INSTANCE_MENU);
            break;
            
          case PowerDbaActions.DB_STREAMS_ERROR_DETAIL:
            Tracer.log("Process DB_STREAMS_ERROR_DETAIL", Tracer.DEBUG, this);
            String tableName = env.getParameter("table");
            myView.setHtml(ProcessDAO.getStreamsTableErrorHtml(tableName,database));
            myView.setTitle("Streams Errors for Table: " + tableName);
            myView.setMenu1(MenuGenerator.MESSAGING_MENU);
            myView.setMenu2(MenuGenerator.STREAMS_MENU);
            break;
            
          case PowerDbaActions.DB_STREAMS_RULE_SETS:
            Tracer.log("Process DB_STREAMS_RULE_SETS", Tracer.DEBUG, this);
            myView.setHtml(ProcessDAO.getStreamsRuleSets(database));
            myView.setTitle("Streams Rules");
            myView.setMenu1(MenuGenerator.MESSAGING_MENU);
            myView.setMenu2(MenuGenerator.STREAMS_MENU);

            break;
            
          case PowerDbaActions.DB_STREAMS_RULE_SETS_RN:
            myView.setTitle("Ruleset: " + env.getParameter("rid"));
            myView.setMenus(MenuGenerator.DATABASE_MENU, MenuGenerator.STREAMS_MENU);
            myView.setHtml(ProcessDAO.getStreamsRuleSetsId(env.getParameter("rid"),database));
            break;
            
          case PowerDbaActions.DB_STREAMS_TRX:
            myView.setTitle("Streams Transaction: " + env.getParameter("streamstrx"));
            myView.setMenus(MenuGenerator.DATABASE_MENU, MenuGenerator.STREAMS_MENU);
            
            subAction = env.getParameter("subaction");
            trx = env.getParameter("streamstrx");
            myView.setTrx(trx);
            
            if ( !subAction.equals("") ) {
            
	            if ( !env.getParameter("streamsusername").equals("") && !env.getParameter("streamspassword").equals("") ) {
	              setNonPooledDatabase(env.getParameter("streamsusername"), env.getParameter("streamspassword"));
	            } else {
	              String msg = "Username and Password must both be specified";
	              myView.setMessage(msg);
	              myView.setHtml(ProcessDAO.getLCRTransaction(trx, database));
	              throw new SQLException(msg);
	            }
	            	           
	            if ( subAction.equals("exec") ) {
                ProcessDAO.execStreamsTrx(database, trx);
                myView.setMessage("Transaction " + trx + " successfully reexecuted.");
	            } else if ( subAction.equals("over") ) {
                ProcessDAO.execStreamsTrxOverride(database, trx);
                myView.setMessage("Transaction " + trx + " successfully reexecuted with conflict resolution overridden.");
	            } else if ( subAction.equals("del") ) {
                ProcessDAO.deleteStreamsTrx(database, trx);
                myView.setMessage("Transaction " + trx + " successfully removed from the error queue.");
	            } else if ( subAction.equals("overridedelete") ) {
                ProcessDAO.execStreamsTrxOverride(database, trx);
                ProcessDAO.deleteStreamsTrx(database, trx);
                myView.setMessage("Transaction " + trx + " successfully executed and removed from the error queue.");
	            }
	            
              myView.setHtml(ProcessDAO.getLCRTransaction(trx, database));
	
	            if ( !env.getParameter("streamsusername").equals("") && !env.getParameter("streamspassword").equals("") ) {
	              try {
	                this.database.getConn().close();
	              } catch ( SQLException se ) {}
	            }   
	            
            } else {
                myView.setHtml(ProcessDAO.getLCRTransaction(trx, database));
            }
            
            

            break;
            
          case PowerDbaActions.DB_STREAMS_DML_HANDLERS:
            myView.setTitle("DML Handlers");
            myView.setMenus(MenuGenerator.MESSAGING_MENU, MenuGenerator.STREAMS_MENU);
            myView.setHtml(ProcessDAO.getDmlHandlersHtml(database));
            break;
            
          case PowerDbaActions.DB_SOURCE:
            myView.setTitle("Source for : " + env.getParameter("dbobj"));
            myView.setMenus(MenuGenerator.MESSAGING_MENU, MenuGenerator.STREAMS_MENU);
            myView.setHtml(ProcessDAO.getSourceHtml(env.getParameter("dbobj"),database));
            break;        
            
          case PowerDbaActions.DB_INSTANCE:
            myView.setHtml(ProcessDAO.getInstanceInfo(database));
            myView.setTitle("Instance Overview");
            myView.setMenu1(MenuGenerator.INSTANCE_MENU);
            myView.setMenu2(MenuGenerator.INSTANCE_SUMMARY_MENU);
            break;
            
          case PowerDbaActions.DB_PARAMETERS:
            myView.setTitle("All Instance Parameters");
            myView.setMenus(MenuGenerator.INSTANCE_MENU, MenuGenerator.INSTANCE_SUMMARY_MENU);
            myView.setHtml(ProcessDAO.getInstanceParameters(database));
            break;

            
          case PowerDbaActions.DB_REDO_LOG:
            myView.setTitle("Redo Log Files");
            myView.setMenus(MenuGenerator.INSTANCE_MENU, MenuGenerator.REDO_MENU);
            myView.setHtml(ProcessDAO.getRedoLog(database));
            break;
              
          case PowerDbaActions.DB_LOG_SUMMARY:
            myView.setTitle("Redo Log Summary");
            myView.setMenus(MenuGenerator.INSTANCE_MENU, MenuGenerator.REDO_MENU);
            sb = new StringBuffer();
            sb.append(ProcessDAO.getLogSummary(database));
            sb.append(DIV);
            sb.append(ProcessDAO.getCurrentLogFiles(database));
            sb.append(DIV);
            sb.append(ProcessDAO.getInstanceParameters(database, "name like '%log%' and name not like '%login%' "));
            myView.setHtml(sb.toString());       
            break;
              
          case PowerDbaActions.DB_STREAMS_CAPTURE:
            myView.setTitle("Capture/Propagation Configuration");
            myView.setMenus(MenuGenerator.MESSAGING_MENU, MenuGenerator.STREAMS_MENU);
            sb = new StringBuffer();
            sb.append(ProcessDAO.getCapture(database));
            sb.append(DIV);
            sb.append(DIV);
            sb.append(DIV);
            sb.append(ProcessDAO.getPropagation(database));
            myView.setHtml(sb.toString());

            break;
              
          case PowerDbaActions.DB_STREAMS_SUMMARY:
            myView.setTitle("Minimum Log Required by Capture");
            myView.setMenus(MenuGenerator.DATABASE_MENU, MenuGenerator.STREAMS_MENU);
            myView.setHtml(ProcessDAO.getStreamsSummary(database));
            break;
              
          case PowerDbaActions.DB_STREAMS_APPLY:
            myView.setTitle("Streams Apply Process Info");
            myView.setMenus(MenuGenerator.DATABASE_MENU, MenuGenerator.STREAMS_MENU);
            myView.setHtml(ProcessDAO.getApply(database));
            break;
              
          case PowerDbaActions.DB_STREAMS_PROPAGATION:
            myView.setTitle("Queue Propagation Info");
            myView.setMenus(MenuGenerator.MESSAGING_MENU, MenuGenerator.STREAMS_MENU);
            myView.setHtml(ProcessDAO.getPropagation(database));
            break;
              
          case PowerDbaActions.DB_STREAMS_QUEUES:
            myView.setTitle("Streams Queues");
            myView.setMenus(MenuGenerator.MESSAGING_MENU, MenuGenerator.STREAMS_MENU);
            myView.setHtml(ProcessDAO.getQueues(database));
            break;

            
          case PowerDbaActions.DB_LONGOPS_DETAIL:
            myView.setTitle("Long Operations running for sid : " + env.getLong("sid"));
            myView.setMenu1(MenuGenerator.INSTANCE_MENU);
            myView.setHtml(ProcessDAO.getLongOpsDetail(database, env.getLong("sid")));
            break;
            
          case PowerDbaActions.DB_TABLESPACES:
            myView.setHtml(ProcessDAO.getTablespaces(database));
            myView.setTitle("Tablespaces");
            myView.setMenus(MenuGenerator.DATABASE_MENU, MenuGenerator.TABLESPACE_MENU);
            break;  
              
          case PowerDbaActions.DB_JOBS:
            myView.setTitle("Jobs");
            myView.setMenu1(MenuGenerator.DATABASE_MENU);
            html = ProcessDAO.getJobs(database);
            html = html + DIV + ProcessDAO.getInstanceParameters(database, "name like '%job%'");
            myView.setHtml(html);

            break;
            
          case PowerDbaActions.DB_JOBS_WL:
	          myView.setTitle("Jobs");
	          myView.setMenus(MenuGenerator.INSTANCE_MENU,MenuGenerator.SESSIONS_MENU);
	          html = ProcessDAO.getJobs(database);
	          html = html + DIV + ProcessDAO.getInstanceParameters(database, "name like '%job%'");
	          myView.setHtml(html);
	
	          break; 
	          

              
          case PowerDbaActions.DB_JOBS_DETAIL:
            myView.setHtml(ProcessDAO.getJobDetail(database,env.getLong("jid")));
            myView.setTitle("Job Detail");
            myView.setMenu1(MenuGenerator.DATABASE_MENU);
            
            subAction = env.getParameter("subaction");
            int jobid = env.getInt("key");
            
            if ( !subAction.equals("") ) {
                
	            if ( !env.getParameter("jobusername").equals("") && !env.getParameter("jobpassword").equals("") ) {
	              setNonPooledDatabase(env.getParameter("jobusername"), env.getParameter("jobpassword"));
	            } else {
	              String msg = "Username and Password must both be specified";
	              myView.setMessage(msg);
	              throw new SQLException(msg);
	            }
	            
	           
	            if ( subAction.equals("break") ) {
	                ProcessDAO.breakJob(database, jobid);
	                myView.setMessage("job " + jobid + " broken.");
	            } else if ( subAction.equals("unbreak") ) {
	                ProcessDAO.unbreakJob(database, jobid);
	                myView.setMessage("Job " + jobid + " unbroken.");
	            } 
	
	            if ( !env.getParameter("jobusername").equals("") && !env.getParameter("jobpassword").equals("") ) {
	                myView.setHtml(ProcessDAO.getJobDetail(database, jobid));
	              try {
	                  this.database.getConn().close();
	              } catch ( SQLException se ) {}
	            }   
	            
            } else {              
                myView.setHtml(ProcessDAO.getJobDetail(database, jobid));
            }
            
            break; 
            
          case PowerDbaActions.DB_CAPTURE_DETAIL:
              
              myView.setMenus(MenuGenerator.MESSAGING_MENU, MenuGenerator.STREAMS_MENU);
              
              subAction = env.getParameter("subaction");
              String capture = env.getParameter("key");
              
              if ( !subAction.equals("") ) {
                  
  	            if ( !env.getParameter("streamsusername").equals("") && !env.getParameter("streamspassword").equals("") ) {
  	              setNonPooledDatabase(env.getParameter("streamsusername"), env.getParameter("streamspassword"));
  	            } else {
  	              String msg = "Username and Password must both be specified";
  	              myView.setMessage(msg);
  	              throw new SQLException(msg);
  	            }
  	             
  	            if ( subAction.equals("stop") ) {
  	                ProcessDAO.stopCapture(database, capture);
  	                myView.setMessage("Capture Process " + capture + " successfully stopped.");
  	            } else if ( subAction.equals("start") ) {
  	                ProcessDAO.startCapture(database, capture);
  	                myView.setMessage("Capture Process " + capture + " started.");
  	            } 
  	
  	            if ( !env.getParameter("streamsusername").equals("") && !env.getParameter("streamspassword").equals("") ) {
  	                myView.setHtml(ProcessDAO.getCaptureDetail(database,capture));
  	              try {
  	                  this.database.getConn().close();
  	              } catch ( SQLException se ) {}
  	            }   
  	            
              } else {              
                myView.setHtml(ProcessDAO.getCaptureDetail(database,capture));
              }

              break;                
              
          case PowerDbaActions.DB_DATABASE:
            Tracer.log("Process DB_DATABASE for " + database, Tracer.DEBUG, this);
            myView.setHtml(ProcessDAO.getDatabase(database));
            myView.setTitle("Database Summary");
            myView.setMenu1(MenuGenerator.DATABASE_MENU);
            break; 
              
          case PowerDbaActions.DB_SQL_TEXT:
            sid = env.getLong("sid");
            myView.setSid(sid);
            
            hash = env.getParameter("hash");
            if ( hash.equals("") ) {
              sa = ProcessDAO.getSqlAddress(sid, database);
            } else {
              sa = new SqlAddress("0", hash);
            }
            sql = ProcessDAO.getSqlTextFromLC(sa, database);
            
            myView.setTitle("SQL Text");

            myView.setHtml(ProcessDAO.buildCurrentSql(sql, sid, database, "Sql", sa.getHashValue(), 40));
            myView.setMenus(MenuGenerator.INSTANCE_MENU, MenuGenerator.SESSIONS_MENU);
            myView.setMenu3(MenuGenerator.SESSION_DETAIL_MENU);
            break;
              
          case PowerDbaActions.DB_WAIT_DETAIL:       
            myView.setTitle("Wait Event Detail");
            myView.setMenus(MenuGenerator.INSTANCE_MENU, MenuGenerator.SESSIONS_MENU);
            myView.setSid(env.getLong("sid"));
            
            myView.setHtml(WaitDAO.getWaitDetail(database, 
                                                 env.getParameter("event"),
                                                 env.getParameter("p1"),
                                                 env.getParameter("p2"),
                                                 env.getParameter("p3"),
                                                 env.getParameter("sid")));

            break;
                      
          // Generic Page Navigation Actions              
          case PowerDbaActions.NEXT_PAGE:  
            pageSet = (PageSet) env.getSessionAttribute("pageset");
            myView.setPage(pageSet.getNextPage()); 
            break;    
            
          case PowerDbaActions.PREVIOUS_PAGE:
            pageSet = (PageSet) env.getSessionAttribute("pageset");
            myView.setPage(pageSet.getPreviousPage());
            break;
            
          case PowerDbaActions.FIRST_PAGE:    
            pageSet = (PageSet) env.getSessionAttribute("pageset");
            myView.setPage(pageSet.getFirstPage());
            break;
            
          case PowerDbaActions.LAST_PAGE: 
            pageSet = (PageSet) env.getSessionAttribute("pageset");
            myView.setPage(pageSet.getLastPage());
            break;
            
          case PowerDbaActions.NEW:
            break;
       }           
       
       //if ( database != null && !database.getConn().isClosed() ) {
       //  this.thisSessionStatsEnd = ProcessDAO.getPowerDBASessionStats(database);
       //  setStatsOnView();
       //}
        
       // Record where we were just at in the session, so on the next action we know where we were.
       env.setSessionAttribute("lastaction", new Integer(this.action));
       env.setSessionAttribute("lastdatabase", this.database==null?"":this.database.getName());
       
       myView.build();

      } catch ( SQLException e ) {
      
        Tracer.log(e, e.getMessage(), Tracer.ERROR, this);

        try {
          if ( this.database != null ) this.database.getConn().rollback();
        } catch ( Exception e2 ) {}
        
        throw new WsnException("SQLException thrown in PowerDbaManager", e.getMessage(),e);

      } catch (Exception e ) {

        Tracer.log(e, e.getMessage(), Tracer.ERROR, this);      
        try {
          if ( this.database != null ) this.database.getConn().rollback();
        } catch ( Exception e3 ) {}
        
        
        throw new WsnException("Exception thrown PowerDbaManager", e.getMessage(),e);
        
      } finally {       
        if ( database != null && database.getConn() != null ) {  
	        try {
	          this.database.getConn().close();
  	      } catch ( Exception e ) {}
        }
      }
      
      Tracer.log("Returning the view object.", Tracer.DEBUG, this);
      
      return myView;

    }
    
    ////////// Private Methods /////////////////
    
    private ArrayList getQueryResults(String queryName, boolean isRac) throws SQLException, Exception {
    	  Tracer.log("Running Query " + queryName, Tracer.DEBUG, this);
        Query q = QueryHolder.getQuery(database, env, queryName);
        q.setRacEnabled(isRac);
        return q.getPageSections();
    }

    private void setDatabase(String db) throws Exception {

      try {
//        if ( this.database != null ) {
//          try {
//            this.database.getConn().close();
//          } catch ( SQLException e ) {}
//        }

        
        this.database = ConnectionManager.getDatabase(db);
        this.connected = true;
        myView.setConnected(connected);
        myView.setDatabase(database);
        
      } catch ( SQLException e ) {
          myView.setConnected(false);
          throw e;
      } 
    }
    
    private void setNonPooledDatabase(String unm, String pwd) throws Exception {
    
      // This creates a new "non-pooled" connection.  It assumes that a pooled connection
      // was already created and set in the attribute database.  This is so we can query
      // the various sys level tables to populate metadata about the new connection.  It also
      // uses the same URL as that defined in the pooled database definition.
        
      try {

        if ( this.database != null ) {
          try {
            this.database.getConn().close();
          } catch ( SQLException e ) {}
        }

        Tracer.log("Creating a new non-pooled Connection to " + this.database.getName(), Tracer.DEBUG, this);
        try {
          DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
          this.database.setConn(DriverManager.getConnection(database.getConn().getMetaData().getURL(), unm, pwd));
        } catch ( SQLException e ) {
          throw e;
        }
        this.database.getConn().setAutoCommit(false);
        this.database.setPooledConnection(false);
        this.connected = true;
        myView.setConnected(connected);
        myView.setDatabase(this.database);

      } catch ( SQLException e ) {
      
        myView.setConnected(connected);
        String msg = "Error creating connection to connect descriptor " + database.getName() + " for user " + unm + ".";
        Tracer.log(e, msg, Tracer.ERROR, this);
        throw new SQLException(msg + "<BR><b>Root Cause:</b>  " + e.getMessage() + "\n");

      } catch ( Exception e ) {
      
        myView.setConnected(false);
        String msg = "Error creating connection to connect descriptor " + database.getName() + ".";
        Tracer.log(e, msg, Tracer.ERROR, this);
        throw new SQLException(msg + "<BR><b>Root Cause:</b>  " + e.getMessage() + "\n");

      } 

    }

    private void setDatabaseTime() throws Exception {
      if ( database == null ) {
        myView.setDatabaseTime(" ");
      } else {
        myView.setDatabaseTime(DateTranslator.getStringDateTime(database.getDateTime()));
      }
    }

    private void setDatabaseList() throws Exception {

      try {
          
        ArrayList dbList = ConnectionConfigurationHolder.getPoolList();       
        ArrayList dbSelectList = new ArrayList();

        for ( int i=0; i<dbList.size(); i++ ) {
          DbConfig cfg = (DbConfig) dbList.get(i);
          String poolName = cfg.getPoolName();
          
          // Only add instances to the list if they are available or the state is UNKNOWN
          if ( cfg.getLastStatus() == DbState.UP || cfg.getLastStatus() == DbState.UNKNOWN ) {
	          dbSelectList.add(new SelectEntry(poolName, "+ " + StringUtility.initCap(poolName)));
          }
	        
        }
        Collections.sort(dbSelectList, SelectEntry.CASE_INSENSITIVE_DISPLAY_ORDER); 
        myView.setDbList(dbSelectList);
        

      } catch (Exception e) {
        Tracer.log(e, "Could not get List of Databases from ConnectionManager.", Tracer.ERROR, OBJECT_NAME);
        throw e;
      }
    
    }
    
    private void setLookupLists() throws Exception {

      try {
            
        if ( this.action == PowerDbaActions.DB_SESSIONS || 
             this.action == PowerDbaActions.DB_SESSIONS2 ||
             this.action == PowerDbaActions.DB_POWERDBA_SESSIONS 
            ) {        
          myView.setLookupGroup(QueryHolder.getLookupGroup(database, "sessions"));
        } else if ( this.action == PowerDbaActions.DB_LOCKS ) {
          myView.setLookupGroup(QueryHolder.getLookupGroup(database, "locks"));
        } else if ( this.action == PowerDbaActions.DB_SCHEMAS ) {
            myView.setLookupGroup(QueryHolder.getLookupGroup(database, "schemas"));
        } else if ( this.action == PowerDbaActions.PGA_DETAILS ) {
          myView.setLookupGroup(QueryHolder.getLookupGroup(database, "pga_detail"));
        } else if ( this.action == PowerDbaActions.DB_QUEUE_STATES ) {
          myView.setLookupGroup(QueryHolder.getLookupGroup(database, "queues"));
        } else if ( this.action == PowerDbaActions.DB_AQ_DEQ_ERRORS ) {
          myView.setLookupGroup(QueryHolder.getLookupGroup(database, "deq_message_types"));
        } else if ( this.action == PowerDbaActions.DB_AQ_ENQ_ERRORS ) {
          myView.setLookupGroup(QueryHolder.getLookupGroup(database, "enq_message_types"));
        } else if ( this.action == PowerDbaActions.DB_STREAMS_ERROR_QUEUE ) {
          myView.setLookupGroup(QueryHolder.getLookupGroup(database, "applyprocs"));
        } else {
          myView.setLookupGroup(new LookupDisplayGroup());
        }

      } catch (Exception e) {
        Tracer.log(e, "Error setting the lookup group for action " + 
        		(String) PowerDbaActions.getReversePages().get(this.action) + ".", Tracer.ERROR, OBJECT_NAME);
        throw e;
      }
    
    }
    
    private String getTableHtml(String owner, String name, OracleDatabaseConnection database) throws SQLException {
    
      StringBuffer html = new StringBuffer();
      
      try {
        html.append(ProcessDAO.getTableStructure(owner, name, database));
        html.append(ProcessDAO.DIV);
        html.append(ProcessDAO.getTableStatistics(owner, name, database));
        html.append(ProcessDAO.DIV);
        html.append(ProcessDAO.getIndexListForTable(owner, name, database));
        html.append(ProcessDAO.DIV);
        html.append(ProcessDAO.getTableSegment(owner, name, "TABLE", database));
      }
      catch (SQLException e) {
        throw e;
      }
      
      return html.toString();
      
    }
    
    private String getIndexHtml(String owner, String name, OracleDatabaseConnection database) throws SQLException {
    
      StringBuffer html = new StringBuffer();
      
      try {
        html.append(ProcessDAO.getIndexStructure(owner, name, database));
        html.append(ProcessDAO.DIV);
        html.append(ProcessDAO.getIndexStatistics(owner, name, database));
        html.append(ProcessDAO.DIV);
        html.append(ProcessDAO.getIndexListForIndex(owner, name, database));
      }
      catch (SQLException e) {
        throw e;
      }
      
      return html.toString();
      
    }
    
    private boolean validateSession(long sid) throws SQLException {
      if ( !ProcessDAO.sessionExists(sid, database) ) {
        myView.setHtml("<b><br><br><font size=-1>Session " + sid + " has logged off this instance.</font></b><br><br>");
        return false;
      } else {
        return true;
      }
    }
    
    private void setChartGroup(String groupName) throws SQLException, Exception {
      if ( database.getVersion().getVersion1() > 9 ) {
        // Use the OWR
        myView.setHtml(ChartGenerator.getChartGroupHtml(ChartGroupHolder.getChartGroup(groupName + "_OWR"), env, database));
      } else {
        // Uses Perfstat
        myView.setHtml(ChartGenerator.getChartGroupHtml(ChartGroupHolder.getChartGroup(groupName), env, database));
      }
    }
    
    private void dumpEnvironmentToLog() {
	    Tracer.log("PowerDBAManager Environment: ", Tracer.DEBUG, this);
	    Tracer.log("  formaction        : " + env.getParameter("formaction") + " - " + env.getParameter("formaction"), Tracer.INFO, this);
	    Tracer.log("  action            : " + action + " - " + PowerDbaActions.getPageCode(action), Tracer.INFO, this);
	    Tracer.log("  connection        : " + env.getParameter("database"), Tracer.DEBUG, this);
	    Tracer.log("  sid               : " + env.getParameter("sid"), Tracer.DEBUG, this);
	    Tracer.log("  address           : " + env.getParameter("address"), Tracer.DEBUG, this);
	    Tracer.log("  hash              : " + env.getParameter("hash"), Tracer.DEBUG, this);
	    Tracer.log("  statistic         : " + env.getParameter("statistic"), Tracer.DEBUG, this);
	    Tracer.log("  refreshinterval   : " + env.getInt("refreshinterval"), Tracer.DEBUG, this);
	    Tracer.log("  sessionstatus     : " + env.getParameter("sessionstatus"), Tracer.DEBUG, this);
	    Tracer.log("  oracleuser        : " + env.getParameter("oracleuser"), Tracer.DEBUG, this);
	    Tracer.log("  sessiontype       : " + env.getParameter("sessiontype"), Tracer.DEBUG, this);
	    Tracer.log("  lastaction        : " + this.lastAction + " - " + PowerDbaActions.getPageCode(lastAction), Tracer.INFO,  this);
	    Tracer.log("  lastdatabase      : " + this.lastDatabase, Tracer.INFO, this);
	    Tracer.log("  size of Conn Hash : " + ConnectionManager.dumpDbPoolsHash(), Tracer.INFO, this);
    }

}

