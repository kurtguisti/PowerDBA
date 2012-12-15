
package com.powerdba;

import com.powerdba.gui.ObjectTranslator;
import com.powerdba.gui.PageGenerator;
import com.powerdba.gui.PageLoader;
import com.powerdba.gui.PageSet;
import com.powerdba.gui.Link;
import com.powerdba.gui.LinkVar;
import com.powerdba.jdbc.ConnectionConfigurationHolder;
import com.powerdba.jdbc.DbConfig;
import com.powerdba.mvc.jsp.SelectEntry;
import com.powerdba.util.StringUtility;
import com.powerdba.util.Tracer;
import com.powerdba.util.DateTranslator;
import com.powerdba.mvc.jsp.HtmlComponent;
import com.powerdba.mvc.WsnException;
import com.powerdba.mvc.PowerDbaActions;

import java.sql.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.StringTokenizer;

public class ProcessDAO {

    private static final String OBJECT_NAME = "ProcessDAO"; 
    public static final int TABLE = 1;
    public static final int DETAIL = 2;
    public static final String DIV = "<hr size=1 noshade width=100%>";

    public ProcessDAO() {}
    
    static public String getGlobalName(Connection conn) throws SQLException {

        String rval = null;
        PreparedStatement pstmt = null;
        ResultSet rset          = null;

        String SQL = "select global_name from global_name";
        try {
            pstmt = conn.prepareStatement(SQL);
            rset  = pstmt.executeQuery();
            if ( rset.next() ) rval = rset.getString(1);
        } catch ( SQLException e ) {
            Tracer.log(e, "Error in getting global name.", Tracer.ERROR, OBJECT_NAME);
            throw e;
        } finally {
            try {
                if ( pstmt != null ) pstmt.close();
            } catch ( Exception e ) {}
        }

        return rval;
    }

    static public String getTablespaces(OracleDatabaseConnection database) throws SQLException {

        StringBuffer sb = new StringBuffer();

        PreparedStatement pstmt = null;

        String SQL = "select t.tablespace_name \"Tablespace Name\", " +
                     "       t.block_size \"Block Size\", " +
                     "       t.initial_extent \"Initial\", " +
                     "       t.next_extent \"Next\", " +
                     "       t.min_extents \"Min Extents\", " +
                     "       t.max_extents \"Max Extents\", " +
                     "       t.pct_increase \"Pct Increase\", " +
                     "       t.min_extlen \"Min Ext Len\", " +
                     "       t.status \"Status\", " +
                     "       t.contents \"Contents\", " +
                     "       t.logging \"Logging\", " +
                     "       t.force_logging \"Force Logging\", " +
                     "       t.extent_management \"Extent Mgmt\", " +
                     "       t.allocation_type \"Allocation Type\", " +
                     "       t.plugged_in \"Plugged In\", " +
                     "       t.segment_space_management \"Segment Mgmt\", " +
                     "       to_char(sum(d.bytes),'9,999,999,999') \"Size\" " +
                     "  from dba_tablespaces t, " +
                     "       dba_data_files d " +
                     "  where t.tablespace_name = d.tablespace_name " + 
                     "  group by t.tablespace_name, t.block_size, t.initial_extent, t.next_extent, " +
                     "           t.min_extents, t.max_extents, t.pct_increase, t.min_extlen, t.status, " +
                     "           t.contents, t.logging, t.force_logging, t.extent_management, t.allocation_type, " +
                     "           t.plugged_in, t.segment_space_management" +
                     "  order by t.tablespace_name";

        try {
            Hashtable links = new Hashtable();

            // 1st link
            Link link = new Link();
            link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_FILES + "&database=" + database.getName());
            link.setText("Zoom");
            link.setAlt("Show Files of this Tablespace.");
            ArrayList vars = new ArrayList();
            LinkVar lv = new LinkVar();
            lv.setIndex(1);
            lv.setVarName("ts");
            vars.add(lv);
            link.setVariables(vars);
            links.put(new Integer(1), link);
            sb.append(buildSessionsHtml(SQL, links, database));

        } catch ( SQLException e ) {
            Tracer.log("Error in loading tablespaces", Tracer.ERROR, OBJECT_NAME);
            throw e;
        } finally {
            try {
                if ( pstmt != null ) pstmt.close();
            } catch ( Exception e ) {}
        }
        return sb.toString();
    }

    static public String getTablespaceHtml(String ts, OracleDatabaseConnection database) throws SQLException {

        StringBuffer sb = new StringBuffer();

        PreparedStatement pstmt = null;
        ResultSet rset          = null;

        String SQL = "select segment_name,file_id,block_id,block_id+blocks-1 end_id ,blocks ,bytes from dba_extents " +
                     "  where tablespace_name = upper(?) " +
                     " union " +
                     "select '* * * Free * * *',file_id,block_id,block_id+blocks-1 end_id, blocks ,bytes from dba_free_space " +
                     "  where tablespace_name = upper(?) " +
                     " order by 2,3";

        try {
            Hashtable links = new Hashtable();

            // 1st link
            Link link = new Link();
            link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_SEGMENT + "&database=" + database.getName());
            link.setText("Segment");
            link.setAlt("Show Segment.");
            ArrayList vars = new ArrayList();
            LinkVar lv = new LinkVar();
            lv.setIndex(1);                                 // 1st pos in query
            lv.setVarName("seg");
            vars.add(lv);
            link.setVariables(vars);

            links.put(new Integer(1), link);

            // 2nd link
            link = new Link();
            link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_FILE + "&database=" + database.getName());
            link.setText("File");
            link.setAlt("Show File Details.");
            vars = new ArrayList();
            lv = new LinkVar();
            lv.setIndex(2);         // 2nd position in query
            lv.setVarName("fileid");
            vars.add(lv);
            link.setVariables(vars);

            links.put(new Integer(2), link);
            
            sb.append(buildTsHtml(SQL, ts, links, database));

        } catch ( SQLException e ) {
            Tracer.log("Error getting tablespace map", Tracer.ERROR, OBJECT_NAME);
            throw e;
        } finally {
            try {
                if ( pstmt != null ) pstmt.close();
            } catch ( Exception e ) {}
        }
        return sb.toString();
    }

    static public String getFileHtml(long fileId, OracleDatabaseConnection database) throws SQLException {

        StringBuffer sb = new StringBuffer();

        PreparedStatement pstmt = null;
        ResultSet rset          = null;

        String SQL = "select * from dba_data_files where file_id = ? order by file_name";

        try {
          ArrayList links = new ArrayList();
          sb.append(buildFileHtml(SQL, fileId, database, links));
        } catch ( SQLException e ) {
          Tracer.log("Error getting oracle file info", Tracer.ERROR, OBJECT_NAME);
          throw e;
        } finally {
          try {
            if ( pstmt != null ) pstmt.close();
          } catch ( Exception e ) {}
        }
        return sb.toString();
    }

    static public String getStreamsTableErrorHtml(String tableName, OracleDatabaseConnection database) throws SQLException {

        StringBuffer sb         = new StringBuffer();
        PreparedStatement pstmt = null;
        ResultSet rset          = null;

        String SQL = "select lcr_webvarchar2 \"Errored Transactions\" " +
                     "  from str_view_txn_errors " +
                     "  where table_name = ? order by source_commit_scn";

        try {
            pstmt = database.getConn().prepareStatement(SQL);
            pstmt.setString(1, tableName);
            rset  = pstmt.executeQuery();
            sb.append(buildTabularDisplay(rset, new Hashtable(), null));
        } catch ( SQLException e ) {
            Tracer.log("Error in loading Streams errors", Tracer.ERROR, OBJECT_NAME);
            throw e;
        } finally {
            try {
                if ( pstmt != null ) pstmt.close();
            } catch ( Exception e ) {}
        }
        return sb.toString();
    }

    static public String getStreamsRuleSets(OracleDatabaseConnection database) throws SQLException {
      return getStreamsRuleSetsId("%", database);
    }

    static public String getStreamsRuleSetsId(String rname, OracleDatabaseConnection database) throws SQLException {

        StringBuffer sb         = new StringBuffer();
        PreparedStatement pstmt = null;
        ResultSet rset          = null;

        String SQL = "select decode(c.capture_name,null, " +
                     "              decode(a.apply_name,null, " +
                     "                     decode(p.propagation_name,null,'**NA**','Propagation'),'Apply'),'Capture') \"Type\", " +
                     " decode(c.capture_name,null,  " +
                     "        decode(a.apply_name,null, " +
                     "               decode(p.propagation_name,null,'**NA**',propagation_name),apply_name),capture_name) \"Process Name\", " +
                     " nvl(r.rule_set_name,'**NA**') \"Rule Set Name\", " +
                     " rl.rule_owner || '.' || rl.rule_name \"Rule Name\", " +
                     " substr(to_char(rl.rule_condition),1,500) \"Rule Condition\", " +
                     " 'Edit' \" \", " +
                     " tf.tfunc \"Transform Function\" " +
                     "  from dba_rule_set_rules r,  " +
                     "       dba_rules rl,          " +
                     "       dba_capture c,         " +
                     "       dba_apply a,           " +
                     "       dba_propagation p,     " +
                     "  (SELECT r.RULE_NAME, ac.NVN_VALUE.ACCESSVARCHAR2() tfunc FROM DBA_RULES r, " +
                     "        TABLE(R.RULE_ACTION_CONTEXT.ACTX_LIST) ac WHERE ac.NVN_NAME = 'STREAMS$_TRANSFORM_FUNCTION' AND " +
                     "        r.RULE_OWNER = 'STRM_ADMIN' ) tf " +
                     "  where r.rule_name (+) = rl.rule_name        " +
                     "    and tf.rule_name (+) = rl.rule_name       " +
                     "    and r.rule_set_name = c.rule_set_name (+) " +
                     "    and r.rule_set_name = a.rule_set_name (+) " +
                     "    and r.rule_set_name = p.rule_set_name (+) " +
                     "    and r.rule_set_name like ?                " +
                     "  order by decode(\"Type\",'**NA**','ZZZZZ',\"Type\"), r.rule_set_name, r.rule_name ";

        try {
            pstmt = database.getConn().prepareStatement(SQL);
            pstmt.setString(1, rname);
            rset  = pstmt.executeQuery();

            Hashtable links = new Hashtable();

            // 1st link
            Link link = new Link();
            link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_SOURCE + "&database=" + database.getName());
            link.setText("Source");
            link.setAlt("Show Source.");
            ArrayList vars = new ArrayList();
            LinkVar lv = new LinkVar();
            lv.setIndex(7);                                 // pos in query
            lv.setVarName("dbobj");
            vars.add(lv);
            link.setVariables(vars);
            links.put(new Integer(7), link);

            // 2st link
            link = new Link();
            link.setFileName("editor.jsp?formaction=" + PowerDbaActions.EDIT_RULE + "&database=" + database.getName());
            link.setText("Edit This Rule");
            link.setAlt("Edit this Rule.");
            link.setType("Popup");
            link.setJsFunction("editRule");
            
            vars = new ArrayList();
            lv = new LinkVar();
            lv.setIndex(4);                                 // pos in query
            lv.setVarName("ruleName");
            vars.add(lv);
            link.setVariables(vars);
            links.put(new Integer(6), link);

            sb.append(buildTabularDisplay(rset, links, null));
        } catch ( SQLException e ) {
            Tracer.log("Error in loading Streams errors", Tracer.ERROR, OBJECT_NAME);
            throw e;
        } finally {
            try {
                if ( pstmt != null ) pstmt.close();
            } catch ( Exception e ) {}
        }
        return sb.toString();
    }
    
    static public String getLCRTransaction(String localTrx, OracleDatabaseConnection database) throws SQLException {

	    StringBuffer sb         = new StringBuffer();
	    PreparedStatement pstmt = null;
	    ResultSet rset          = null;
	    
	    for ( int i=1; i<=200; i++) {
	
	        String SQL = "select strm_admin.lcr_print.print_lcr_html(DBMS_APPLY_ADM.GET_ERROR_MESSAGE(?,?)) \"Lcr Transaction\" from dual";
	
	        try {
	            pstmt = database.getConn().prepareStatement(SQL);
	            pstmt.setInt(1, i);
	            pstmt.setString(2, localTrx);
	            rset  = pstmt.executeQuery();
	            Tracer.log(SQL,Tracer.DEBUG,OBJECT_NAME);
	            sb.append(buildTabularDisplay(rset, new Hashtable(), "LCR (entry " + i + ")"));
	        } catch ( SQLException e ) {
	            if ( e.getErrorCode() != 23605 ) { // Ignore an ORA-23605 error....
	              throw new SQLException("Problem running the stored procedure strm_admin.str_tools.print_lcr_varchar2.\n" + SQL + "\n" + e.getMessage());
	            }
	        } finally {
	            try {
	                if ( pstmt != null ) pstmt.close();
	            } catch ( Exception e ) {}
	        }
	    
	    }
	    return sb.toString();
    }
    
    static public String getInstanceParameters(OracleDatabaseConnection database) throws SQLException {
        return getInstanceParameters(database, "name like '%'");
    }

    static public String getInstanceParameters(OracleDatabaseConnection database, String whereClause) throws SQLException {

        StringBuffer sb         = new StringBuffer();
        PreparedStatement pstmt = null;
        ResultSet rset          = null;

        String SQL = "select name , value , decode(isdefault,'FALSE','TRUE','FALSE') \"Specified in Parameter File\", ISSES_MODIFIABLE \"Sess Modifiable\", " +
                     " ISSYS_MODIFIABLE \"Sys Modifiable\", ISMODIFIED \"Modified Since Startup\", " +
                     " ISADJUSTED \"Adjusted by Oracle\", description from v$parameter where " + whereClause +
                     "    order by isdefault, name";
        try {
            pstmt = database.getConn().prepareStatement(SQL);
            rset  = pstmt.executeQuery();
            
            sb.append(buildTabularDisplay(rset, new Hashtable(), "Instance Parameters"));
        } catch ( SQLException e ) {
            Tracer.log("Error in loading Instance Parameters", Tracer.ERROR, OBJECT_NAME);
            throw e;
        } finally {
            try {
                if ( pstmt != null ) pstmt.close();
            } catch ( Exception e ) {}
        }

        return sb.toString();
    }
    
    static public String getInitOra(OracleDatabaseConnection database) throws SQLException {

        StringBuffer sb         = new StringBuffer();
        PreparedStatement pstmt = null;
        ResultSet rset          = null;

        String SQL = "select name \"Name\", value \"Value\", ISSES_MODIFIABLE \"Sess Modifiable\", " +
                     " ISSYS_MODIFIABLE \"Sys Modifiable\", ISMODIFIED \"Modified Since Startup\", " +
                     " ISADJUSTED \"Adjusted by Oracle\", description \"Description\" from v$parameter " +
                     " where isdefault = 'FALSE' order by isdefault, name";
        try {
            pstmt = database.getConn().prepareStatement(SQL);
            rset  = pstmt.executeQuery();
            
            sb.append(buildTabularDisplay(rset, new Hashtable(), "Modified Parameters"));
        } catch ( SQLException e ) {
            Tracer.log("Error in loading Instance Parameters", Tracer.ERROR, OBJECT_NAME);
            throw e;
        } finally {
            try {
                if ( pstmt != null ) pstmt.close();
            } catch ( Exception e ) {}
        }

        return sb.toString();
    }
    
    static public PageSet getLogHistory(OracleDatabaseConnection database, String date) throws SQLException {

        PreparedStatement pstmt = null;
        ResultSet rset          = null;

        String SQL = "select to_char(hist.first_time,'mm/dd/yy hh24:mi') \"Minute\" , " +
                     "  lpad(' ',count(*)+1,'*') \"Histogram\", " +
                     "  count(*) \"How<br>Many\", " +
                     "  to_char(nvl(sum(l.bytes),count(*)*max(s.bytes))/1024,'999,999,999') \"KBytes\"  " +
                     "  from v$loghist hist, " +
                     "        v$log l, (select distinct bytes from v$log) s " +
                     "   where hist.thread# = l.thread# (+) " +
                     "     and hist.sequence# = l.sequence# (+) " +
                     "     and trunc(hist.first_time) = trunc(to_date('"+date+"','dd-mon-yyyy')) " +
                     "  group by to_char(hist.first_time,'mm/dd/yy hh24:mi') " +
                     "  order by to_char(hist.first_time,'mm/dd/yy hh24:mi')";

        try {
        
          pstmt = database.getConn().prepareStatement(SQL);
          rset  = pstmt.executeQuery();
            
          Hashtable links = new Hashtable();     
          Link link = new Link();
          link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_LOG_HISTORY_SECOND + "&database=" + database.getName());
          link.setText("By Second");
          link.setAlt("Show Second Level Detail.");
          link.getVariables().add(new LinkVar(0, "time"));
          links.put(new Integer(1), link);

          String title = "Redo Log Written";
          return PageLoader.buildPageSet(rset, 0, 500, 1, links, new ArrayList(), new ArrayList(), title, 5000);
          
        } catch ( SQLException e ) {
          Tracer.log("Error in building html for log history", Tracer.ERROR, OBJECT_NAME);
          throw e;
        } finally {
          try {
            if ( pstmt != null ) pstmt.close();
          } catch ( Exception e ) {}
        }

    }
    
    static public int getInstanceCount(OracleDatabaseConnection database) throws SQLException {
      
      StringBuffer sb         = new StringBuffer();
      PreparedStatement pstmt = null;
      ResultSet rset          = null;

      String SQL = "select count(*) from gv$instance";

      try {
      
        pstmt = database.getConn().prepareStatement(SQL);
        rset  = pstmt.executeQuery();

        rset.next();
        return rset.getInt(1);

      } catch ( SQLException e ) {
          Tracer.log("Error getting number of instances.", Tracer.ERROR, OBJECT_NAME);
          throw e;
      } finally {
          try {
              if ( pstmt != null ) pstmt.close();
          } catch ( Exception e ) {}
      }
    }
    
    static public ArrayList getOtherInstances(OracleDatabaseConnection database) throws SQLException {
        
        ArrayList list = new ArrayList();
        PreparedStatement pstmt = null;
        ResultSet rset          = null;

        String SQL = "select instance_name from gv$instance where instance_name not in (select instance_name from v$instance)";

        try {
        
          pstmt = database.getConn().prepareStatement(SQL);
          rset  = pstmt.executeQuery();

          while ( rset.next() ) {
              list.add(rset.getString(1));
          }
          return list;

        } catch ( SQLException e ) {
            Tracer.log("Error in building list of other instances", Tracer.ERROR, OBJECT_NAME);
            throw e;
        } finally {
            try {
                if ( pstmt != null ) pstmt.close();
            } catch ( Exception e ) {}
        }
    }
    
    static public ArrayList getOtherDescriptors(ArrayList instanceList) throws SQLException {
        
			// Create and return a list of jdbc descriptors pertaining to the other databases in the cluster with the passed in 
			// database.
    	
    	
			
			ArrayList list = new ArrayList();		
			try {		
			  Hashtable hash = ConnectionConfigurationHolder.getInstanceNameHash();
			  for ( int i=0; i<instanceList.size(); i++ ) {
			    String descriptor = (String) hash.get((String)instanceList.get(i));
			    if ( descriptor != null ) {
			      list.add(descriptor);
			    }
			  }	  
			  Tracer.log("Returning a list of other descriptors size "+ list.size(),Tracer.DEBUG,"ProcessDAO");
			  return list;
			
			} catch ( Exception e ) {
			    Tracer.log("Error in building list of other descriptors", Tracer.ERROR, OBJECT_NAME);
			    throw new SQLException("Error in building list of other descriptors");
			}		    
		}

    static public PageSet getLogHistorySeconds(OracleDatabaseConnection database, String minute) throws SQLException {

        PreparedStatement pstmt = null;
        ResultSet rset          = null;

        String SQL = "select to_char(hist.first_time,'mm/dd/yy hh24:mi:ss') \"Second\" , " +
                     "  lpad(' ',count(*)+1,'*') \"Histogram\", " +
                     "  count(*) \"How<br>Many\", " +
                     "  to_char(nvl(sum(l.bytes),count(*)*max(s.bytes))/1024,'999,999,999') \"KBytes\"  " +
                     "  from v$loghist hist, " +
                     "        v$log l, (select distinct bytes from v$log) s " +
                     "   where hist.thread# = l.thread# (+) " +
                     "     and hist.sequence# = l.sequence# (+) " +
                     "     and to_char(hist.first_time,'mm/dd/yy hh24:mi') = ? " +
                     "  group by to_char(hist.first_time,'mm/dd/yy hh24:mi:ss')";

        try {
        
          pstmt = database.getConn().prepareStatement(SQL);
          pstmt.setString(1, minute);
          rset  = pstmt.executeQuery();
            
          Hashtable links = new Hashtable();     

          String title = "Redo Log Generation (by second) for minute: " + minute;
          return PageLoader.buildPageSet(rset, 0, 500, 1, links, new ArrayList(), 
                                         new ArrayList(), title, 500);
        } catch ( SQLException e ) {
            Tracer.log("Error in building html for log history", Tracer.ERROR, OBJECT_NAME);
            throw e;
        } finally {
            try {
                if ( pstmt != null ) pstmt.close();
            } catch ( Exception e ) {}
        }

    }
    
    static public String getLogSummary(OracleDatabaseConnection database) throws SQLException {

        StringBuffer sb         = new StringBuffer();
        PreparedStatement pstmt = null;
        ResultSet rset          = null;

        String SQL = "select " +
                     "       log_cnt \"Total Number of Log Files\", " +
                     "       unarchived_log_files \"Unarchived Log Files\", " +
                     "       to_char(avg_size,'999,999,999,999') \"Average Log File Size\", " +
                     "       log_mode \"Log Mode\", " +
                     "       scn \"Current SCN\" " +
                     "from " +
                     "  (select count(*) unarchived_log_files " +
                     "     from v$log where archived = 'NO' and status != 'CURRENT'), " +
                     "  (select count(*) log_cnt from v$log), "  +
                     "  (select log_mode from v$database), " +
                     "  (select avg(bytes) avg_size from v$log), " +
                     "  (select to_char(dbms_flashback.get_system_change_number,'999,999,999,999,999') scn from dual)";

        try {
        
          pstmt = database.getConn().prepareStatement(SQL);
          rset  = pstmt.executeQuery();
            
          Hashtable links = new Hashtable();     

          String title = "Logging Summary";
          return buildDetailDisplay(rset, new Hashtable(), title, 1);
        } catch ( SQLException e ) {
            Tracer.log("Error in building html for logging summary", Tracer.ERROR, OBJECT_NAME);
            throw e;
        } finally {
            try {
                if ( pstmt != null ) pstmt.close();
            } catch ( Exception e ) {}
        }

    }
    
    static public String getCurrentLogFiles(OracleDatabaseConnection database) throws SQLException {

      PreparedStatement pstmt = null;
      ResultSet rset          = null;

      String SQL = "select t1.group#, t1.thread#, t1.sequence#, t1.archived, t1.status, " +
                   "       to_char(t1.first_change#,'999,999,999,999,999') \"First Change\", " +
                   "       to_char(t1.first_time,'mm/dd/yyyy hh24:mi:ss') first_time, t2.member " +
                   "  from v$log t1, v$logfile t2 where t1.group#= t2.group# and t1.status = 'CURRENT'";

      try {
      
        pstmt = database.getConn().prepareStatement(SQL);
        rset  = pstmt.executeQuery();;     

        String title = "Current File(s)";
        return buildTabularDisplay(rset, new Hashtable(), title);
      } catch ( SQLException e ) {
          Tracer.log("Error in building html for current log", Tracer.ERROR, OBJECT_NAME);
          throw e;
      } finally {
          try {
              if ( pstmt != null ) pstmt.close();
          } catch ( Exception e ) {}
      }

    }
    
    static public OracleBaseObject getOracleObject(String name, OracleDatabaseConnection database) throws SQLException {

      OracleBaseObject obo = null;
      PreparedStatement pstmt = null;
      ResultSet rset          = null;
      
      Tracer.log("Looking Up " + name, Tracer.DEBUG, "ProcessDAO");

      String SQL = "select object_type, owner, object_name from dba_objects where object_name = ? " +
                   "  and object_type != 'SYNONYM' and object_type not like '%PARTITION%'";
      try {
        pstmt = database.getConn().prepareStatement(SQL);
        pstmt.setString(1,name);
        rset  = pstmt.executeQuery();
        
        if ( rset.next() ) {
          obo = new OracleBaseObject(rset.getString(1), rset.getString(2), rset.getString(3));
        }
        
        if (obo == null || obo.getType() == null ) {
          obo = new OracleBaseObject("NA","NA",name);
        } else if ( obo.getType().equals("TABLE") ) {
	        SQL = "select num_rows from dba_tables where owner = ? and table_name = ?";
	        pstmt = database.getConn().prepareStatement(SQL);
	        pstmt.setString(1,obo.getOwner());
	        pstmt.setString(2,obo.getName());
	        rset  = pstmt.executeQuery();
	        if ( rset.next()) {
	            obo.setRowCount(rset.getLong(1));
	        }
        } else if ( obo.getType().equals("INDEX") ) {
	        SQL = "select distinct_keys from dba_indexes where owner = ? and index_name = ?";
	        pstmt = database.getConn().prepareStatement(SQL);
	        pstmt.setString(1,obo.getOwner());
	        pstmt.setString(2,obo.getName());
	        rset  = pstmt.executeQuery();
	        if ( rset.next()) {
	            obo.setRowCount(rset.getLong(1));
	        }
        } 
      } catch ( SQLException e ) {
        Tracer.log("Error in loading database data for base object " + name, Tracer.ERROR, OBJECT_NAME);
        throw e;
      } finally {
        try {
          if ( pstmt != null ) pstmt.close();
        } catch ( Exception e ) {}
      }
      
      return obo;
      
    }
    
    static public String getTableStructure(String owner, String name, OracleDatabaseConnection database) throws SQLException {

      PreparedStatement pstmt = null;
      ResultSet rset          = null;

      String SQL = "select column_name, nullable, data_type, data_length, data_precision " +
                   "  from dba_tab_columns " +
                   "  where owner = ? " +
                   "    and table_name = ? " +
                   "  order by column_id ";

      try {
      
        pstmt = database.getConn().prepareStatement(SQL);
        pstmt.setString(1, owner);
        pstmt.setString(2, name);
        rset  = pstmt.executeQuery();
        String title = "Table Structure";
        
        return buildTabularDisplay(rset, new Hashtable(), title, true, 60, false);
      
      } catch ( SQLException e ) {
        Tracer.log(e, "Error in building html for table definition", Tracer.ERROR, OBJECT_NAME);
        throw e;
      } finally {
        try {
            if ( pstmt != null ) pstmt.close();
        } catch ( Exception e ) {}
      }

    }
 
    static public String getTableSegment(String owner, String name, String type, OracleDatabaseConnection database) throws SQLException {

        PreparedStatement pstmt = null;
        ResultSet rset          = null;

        String SQL = "select * from dba_segments " +
                     "  where owner = ? " +
                     "    and segment_name = ? and segment_type=?";

        try {
        
          pstmt = database.getConn().prepareStatement(SQL);
          pstmt.setString(1, owner);
          pstmt.setString(2, name);
          pstmt.setString(3, type);
          rset  = pstmt.executeQuery();   
    
          String title = "Segment Information";
          return buildDetailDisplay(rset,title,4);
        
        } catch ( SQLException e ) {
          Tracer.log(e, "Error in building html for segment definition", Tracer.ERROR, OBJECT_NAME);
          throw e;
        } finally {
          try {
              if ( pstmt != null ) pstmt.close();
          } catch ( Exception e ) {}
        }

      }
    
    static public String getPartitions(String owner, String name, String type, OracleDatabaseConnection database) throws SQLException {

      PreparedStatement pstmt = null;
      ResultSet rset          = null;

      String SQL = "select partition_name, blocks from dba_segments " +
                   "  where owner = ? " +
                   "    and segment_name = ?";

      try {
      
        pstmt = database.getConn().prepareStatement(SQL);
        pstmt.setString(1, owner);
        pstmt.setString(2, name);
        rset  = pstmt.executeQuery();   
  
        String title = "Partition Information";
        return buildTabularDisplay(rset,title);
      
      } catch ( SQLException e ) {
        Tracer.log(e, "Error in building html for segment partitions", Tracer.ERROR, OBJECT_NAME);
        throw e;
      } finally {
        try {
            if ( pstmt != null ) pstmt.close();
        } catch ( Exception e ) {}
      }

    }
    
    static public String getIndexStructure(String owner, String name, OracleDatabaseConnection database) throws SQLException {

      PreparedStatement pstmt = null;
      ResultSet rset          = null;

      String SQL = "select column_position || '. ' || column_name \"Column\" " +
                   "  from dba_ind_columns " +
                   "  where index_owner = ? " +
                   "    and index_name = ? " +
                   "  order by column_position ";

      try {
      
        pstmt = database.getConn().prepareStatement(SQL);
        pstmt.setString(1, owner);
        pstmt.setString(2, name);
        rset  = pstmt.executeQuery();
          
        Hashtable links = new Hashtable();     
  
        String title = "Index Columns";
        return buildTabularDisplay(rset, links, title, true, 25, false);
      
      } catch ( SQLException e ) {
        Tracer.log(e, "Error in building html for index definition", Tracer.ERROR, OBJECT_NAME);
        throw e;
      } finally {
        try {
            if ( pstmt != null ) pstmt.close();
        } catch ( Exception e ) {}
      }

    }
    
    static public String getTableStatistics(String owner, String name, OracleDatabaseConnection database) throws SQLException {

      PreparedStatement pstmt = null;
      ResultSet rset          = null;

      String SQL = "select * from dba_tables where owner = ? and table_name = ?";

      try {
      
        pstmt = database.getConn().prepareStatement(SQL);
        pstmt.setString(1, owner);
        pstmt.setString(2, name);
        rset  = pstmt.executeQuery();
          
        String title = "Table Statistics";
        return buildDetailDisplay(rset, new Hashtable(), title, 4);
      
      } catch ( SQLException e ) {
        Tracer.log(e, "Error in building html for table stats", Tracer.ERROR, OBJECT_NAME);
        throw e;
      } finally {
        try {
            if ( pstmt != null ) pstmt.close();
        } catch ( Exception e ) {}
      }

    }
    
    static public String getSequence(String owner, String name, OracleDatabaseConnection database) throws SQLException {

      PreparedStatement pstmt = null;
      ResultSet rset          = null;

      String SQL = "select * from dba_sequences where sequence_owner = ? and sequence_name = ?";

      try {
      
        pstmt = database.getConn().prepareStatement(SQL);
        pstmt.setString(1, owner);
        pstmt.setString(2, name);
        rset  = pstmt.executeQuery(); 
  
        String title = "Sequence Definition";
        return buildDetailDisplay(rset, new Hashtable(), title, 1);
      
      } catch ( SQLException e ) {
        Tracer.log(e, "Error in building html for sequence definition", Tracer.ERROR, OBJECT_NAME);
        throw e;
      } finally {
        try {
            if ( pstmt != null ) pstmt.close();
        } catch ( Exception e ) {}
      }

    }
    
    static public String getMView(String owner, String name, OracleDatabaseConnection database) throws SQLException {

      PreparedStatement pstmt = null;
      ResultSet rset          = null;

      String SQL = "select * from dba_mviews where owner = ? and mview_name = ?";

      try {
      
        pstmt = database.getConn().prepareStatement(SQL);
        pstmt.setString(1, owner);
        pstmt.setString(2, name);
        rset  = pstmt.executeQuery();
          
        Hashtable links = new Hashtable();     
  
        String title = "Materialized View";
        return buildDetailDisplay(rset, new Hashtable(), title, 1);
      
      } catch ( SQLException e ) {
        Tracer.log(e, "Error in building html for mview definition", Tracer.ERROR, OBJECT_NAME);
        throw e;
      } finally {
        try {
            if ( pstmt != null ) pstmt.close();
        } catch ( Exception e ) {}
      }
    }
    
    static public String getDBLink(String owner, String name, OracleDatabaseConnection database) throws SQLException {

        PreparedStatement pstmt = null;
        ResultSet rset          = null;

        String SQL = "select owner, db_link, username, host, created, " +
                     "'create database link ' || owner || '.' || db_link || decode(username,null,'',' connect to ' || username || ' identified by password') || ' using ''' || host || ''';' command from dba_db_links where owner = ? and db_link = ?";

        try {
        
          pstmt = database.getConn().prepareStatement(SQL);
          pstmt.setString(1, owner);
          pstmt.setString(2, name);
          rset  = pstmt.executeQuery();  
    
          String title = "Database Link";
          return buildDetailDisplay(rset, new Hashtable(), title, 1);
        
        } catch ( SQLException e ) {
          Tracer.log(e, "Error in building html for db link definition", Tracer.ERROR, OBJECT_NAME);
          throw e;
        } finally {
          try {
              if ( pstmt != null ) pstmt.close();
          } catch ( Exception e ) {}
        }
      }
    
    static public String getAQ(String owner, String name, OracleDatabaseConnection database) throws SQLException {

        PreparedStatement pstmt = null;
        ResultSet rset          = null;

        String SQL = "select * from dba_queues where owner = ? and name = ?";

        try {
        
          pstmt = database.getConn().prepareStatement(SQL);
          pstmt.setString(1, owner);
          pstmt.setString(2, name);
          rset  = pstmt.executeQuery();    
    
          String title = "Advanced Queue";
          return buildDetailDisplay(rset, new Hashtable(), title, 1);
        
        } catch ( SQLException e ) {
          Tracer.log(e, "Error in building html for AQ definition", Tracer.ERROR, OBJECT_NAME);
          throw e;
        } finally {
          try {
              if ( pstmt != null ) pstmt.close();
          } catch ( Exception e ) {}
        }
      }
    
    static public String getEvaluationContext(String owner, String name, OracleDatabaseConnection database) throws SQLException {

        PreparedStatement pstmt = null;
        ResultSet rset          = null;

        String SQL = "select * from dba_evaluation_contexts where evaluation_context_owner = ? and evaluation_context_name = ?";

        try {
        
          pstmt = database.getConn().prepareStatement(SQL);
          pstmt.setString(1, owner);
          pstmt.setString(2, name);
          rset  = pstmt.executeQuery();    
    
          String title = "Evaluation Context";
          return buildDetailDisplay(rset, new Hashtable(), title, 1);
        
        } catch ( SQLException e ) {
          Tracer.log(e, "Error in building html for evaluation context definition", Tracer.ERROR, OBJECT_NAME);
          throw e;
        } finally {
          try {
              if ( pstmt != null ) pstmt.close();
          } catch ( Exception e ) {}
        }
      }
    
    static public String getLob(String owner, String name, OracleDatabaseConnection database) throws SQLException {

        PreparedStatement pstmt = null;
        ResultSet rset          = null;

        String SQL = "select * from dba_lobs where owner = ? and segment_name = ?";

        try {
        
          pstmt = database.getConn().prepareStatement(SQL);
          pstmt.setString(1, owner);
          pstmt.setString(2, name);
          rset  = pstmt.executeQuery();
            
          Hashtable links = new Hashtable();     
    
          String title = "Lob";
          return buildDetailDisplay(rset, new Hashtable(), title, 1);
        
        } catch ( SQLException e ) {
          Tracer.log(e, "Error in building html for lob definition", Tracer.ERROR, OBJECT_NAME);
          throw e;
        } finally {
          try {
              if ( pstmt != null ) pstmt.close();
          } catch ( Exception e ) {}
        }
      }
    
    static public String getRule(String owner, String name, OracleDatabaseConnection database) throws SQLException {

        PreparedStatement pstmt = null;
        ResultSet rset          = null;

        String SQL = "select rule_owner,rule_name,rule_condition,rule_evaluation_context_owner,rule_evaluation_context_name,rule_comment from dba_rules where rule_owner = ? and rule_name = ?";

        try {
        
          pstmt = database.getConn().prepareStatement(SQL);
          pstmt.setString(1, owner);
          pstmt.setString(2, name);
          rset  = pstmt.executeQuery();   
    
          String title = "Rule";
          return buildDetailDisplay(rset, new Hashtable(), title, 1);
        
        } catch ( SQLException e ) {
          Tracer.log(e, "Error in building html for rule definition", Tracer.ERROR, OBJECT_NAME);
          throw e;
        } finally {
          try {
              if ( pstmt != null ) pstmt.close();
          } catch ( Exception e ) {}
        }
      }
    
    static public String getDatapumpTable(String owner, String name, OracleDatabaseConnection database) throws SQLException {

        PreparedStatement pstmt = null;
        ResultSet rset          = null;

        String SQL = "select * from " + owner + "." + name + " order by process_order";

        try {
        
          pstmt = database.getConn().prepareStatement(SQL);
          rset  = pstmt.executeQuery();    
    
          String title = "Datapump Job " + name;
          return buildTabularDisplay(rset, title);
        
        } catch ( SQLException e ) {
          if ( e.getErrorCode() == 942 ) {
            return "<br><b>&nbsp;Datapump table " + owner + "." + name + " was not found.  The job probably has finished.</b>";
          } else {
            Tracer.log(e, "Error in building html for datapump job definition", Tracer.ERROR, OBJECT_NAME);
            throw e;
          }
        } finally {
          try {
              if ( pstmt != null ) pstmt.close();
          } catch ( Exception e ) {}
        }
      }
    
    static public String getSynonym(String owner, String name, OracleDatabaseConnection database) throws SQLException {

        PreparedStatement pstmt = null;
        ResultSet rset          = null;

        String SQL = "select * from dba_synonyms where owner=? and synonym_name = ?";

        try {
        
          pstmt = database.getConn().prepareStatement(SQL);
          pstmt.setString(1, owner);
          pstmt.setString(2, name);
          rset  = pstmt.executeQuery();    
    
          String title = "Synonym " + name;
          return buildDetailDisplay(rset, title, 1);
        
        } catch ( SQLException e ) {
            Tracer.log(e, "Error in building html for synonym definition", Tracer.ERROR, OBJECT_NAME);
            throw e;
        } finally {
          try {
              if ( pstmt != null ) pstmt.close();
          } catch ( Exception e ) {}
        }
      }
    
    
    static public String getRefreshGroupChildren(String owner, String name, OracleDatabaseConnection database) throws SQLException {

        PreparedStatement pstmt = null;
        ResultSet rset          = null;

        String SQL = "select owner, name, 'MATERIALIZED VIEW', type, implicit_destroy, " +
                     "       push_deferred_rpc, refresh_after_errors, " +
                     "       rollback_seg, job, next_date, interval, broken, " +
                     "       purge_option, parallelism, heap_size from dba_refresh_children where owner = ? and refgroup = ?";

        try {
        
          pstmt = database.getConn().prepareStatement(SQL);
          pstmt.setString(1, owner);
          pstmt.setString(2, name);
          rset  = pstmt.executeQuery();
            
          Hashtable links = new Hashtable();
          
          Link link = new Link();
          link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_OBJECT_DETAIL + "&database=" + database.getName());
          link.setText("show object");
          link.setAlt("Show Object Detail.");
          link.getVariables().add(new LinkVar(1, "owner"));
          link.getVariables().add(new LinkVar(2, "objname"));
          link.getVariables().add(new LinkVar(3, "type"));
          
          links.put(new Integer(2), link);
    
          String title = "Refresh Group Children";
          return buildTabularDisplay(rset, links, title);
        
        } catch ( SQLException e ) {
          Tracer.log(e, "Error in building html for refresh group definition", Tracer.ERROR, OBJECT_NAME);
          throw e;
        } finally {
          try {
              if ( pstmt != null ) pstmt.close();
          } catch ( Exception e ) {}
        }

      }
    
    static public String getRefreshGroup(String owner, String name, OracleDatabaseConnection database) throws SQLException {

        PreparedStatement pstmt = null;
        ResultSet rset          = null;

        String SQL = "select * from dba_rgroup where owner = ? and refgroup = ?";

        try {
        
          pstmt = database.getConn().prepareStatement(SQL);
          pstmt.setString(1, owner);
          pstmt.setString(2, name);
          rset  = pstmt.executeQuery();
    
          String title = "Refresh Group";
          return buildDetailDisplay(rset, title);
        
        } catch ( SQLException e ) {
          Tracer.log(e, "Error in building html for refresh group detail", Tracer.ERROR, OBJECT_NAME);
          throw e;
        } finally {
          try {
              if ( pstmt != null ) pstmt.close();
          } catch ( Exception e ) {}
        }

      }
    
    static public String getView(String owner, String name, OracleDatabaseConnection database) throws SQLException {

      PreparedStatement pstmt = null;
      ResultSet rset          = null;

      String SQL = "select * from dba_views where owner = ? and view_name = ?";

      try {
      
        pstmt = database.getConn().prepareStatement(SQL);
        pstmt.setString(1, owner);
        pstmt.setString(2, name);
        rset  = pstmt.executeQuery();
          
        Hashtable links = new Hashtable();     
  
        String title = "View Definition";
        return buildDetailDisplay(rset, new Hashtable(), title, 1);
      
      } catch ( SQLException e ) {
        Tracer.log(e, "Error in building html for view definition", Tracer.ERROR, OBJECT_NAME);
        throw e;
      } finally {
        try {
            if ( pstmt != null ) pstmt.close();
        } catch ( Exception e ) {}
      }

    }
    
    static public String getIndexStatistics(String owner, String name, OracleDatabaseConnection database) throws SQLException {

      PreparedStatement pstmt = null;
      ResultSet rset          = null;

      String SQL = "select * from dba_indexes where owner = ? and index_name = ?";

      try {
      
        pstmt = database.getConn().prepareStatement(SQL);
        pstmt.setString(1, owner);
        pstmt.setString(2, name);
        rset  = pstmt.executeQuery();

        Hashtable links = new Hashtable();         
        Link link = new Link();
        link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_TABLE_PERFVIEW + "&database=" + database.getName());
        link.setType("Table Performance View");
        link.setAlt("Show Details of this Table.");
        link.getVariables().add(new LinkVar(4, "owner"));
        link.getVariables().add(new LinkVar(5, "name"));
        links.put(new Integer(5), link);
  
        String title = "Index Definition";
        return buildDetailDisplay(rset,links, title, 4);
      
      } catch ( SQLException e ) {
        Tracer.log(e, "Error in building html for index def", Tracer.ERROR, OBJECT_NAME);
        throw e;
      } finally {
        try {
            if ( pstmt != null ) pstmt.close();
        } catch ( Exception e ) {}
      }

    }
    
    static public String getIndexListForTable(String owner, String name, OracleDatabaseConnection database) throws SQLException {

      PreparedStatement pstmt = null;
      ResultSet rset          = null;

      String SQL = "select owner, index_name, uniqueness, index_type from dba_indexes where " +
                   "  table_owner = ? and table_name = ? order by decode(uniqueness,'UNIQUE',0,1), index_name";

      try {
      
        pstmt = database.getConn().prepareStatement(SQL);
        pstmt.setString(1, owner);
        pstmt.setString(2, name);
        rset  = pstmt.executeQuery();
          
        Hashtable links = new Hashtable();         
        Link link = new Link();
        link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_INDEX_PERFVIEW + "&database=" + database.getName());
        link.setType("Index Performance View");
        link.setAlt("Show Details of this Index.");
        link.getVariables().add(new LinkVar(1, "owner"));
        link.getVariables().add(new LinkVar(2, "name"));
        links.put(new Integer(2), link);
  
        String title = "Indexes";
        return buildTabularDisplay(rset, links, title);
      
      } catch ( SQLException e ) {
        Tracer.log(e, "Error in building html for table index list", Tracer.ERROR, OBJECT_NAME);
        throw e;
      } finally {
        try {
            if ( pstmt != null ) pstmt.close();
        } catch ( Exception e ) {}
      }

    }
    
    static public String getIndexListForIndex(String owner, String name, OracleDatabaseConnection database) throws SQLException {

      PreparedStatement pstmt = null;
      ResultSet rset          = null;

      String SQL = "select owner, index_name, uniqueness, index_type from dba_indexes where " +
                   "  (table_owner, table_name) = (select table_owner, table_name from dba_indexes " + 
                   "                                where owner = ? and index_name = ?) " +
                   "  and index_name != ? " +
                   "  order by decode(uniqueness,'UNIQUE',0,1), index_name";

      try {
      
        pstmt = database.getConn().prepareStatement(SQL);
        pstmt.setString(1, owner);
        pstmt.setString(2, name);
        pstmt.setString(3, name);
        rset  = pstmt.executeQuery();

        Hashtable links = new Hashtable();         
        Link link = new Link();
        link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_INDEX_PERFVIEW + "&database=" + database.getName());
        link.setType("Index Performance View");
        link.setAlt("Show Details of this Index.");
        link.getVariables().add(new LinkVar(1, "owner"));
        link.getVariables().add(new LinkVar(2, "name"));
        links.put(new Integer(2), link);        
  
        String title = "Other Indexes on Table";
        return buildTabularDisplay(rset, links, title);
      
      } catch ( SQLException e ) {
        Tracer.log(e, "Error in building html for table index list", Tracer.ERROR, OBJECT_NAME);
        throw e;
      } finally {
        try {
            if ( pstmt != null ) pstmt.close();
        } catch ( Exception e ) {}
      }

    }
    

    static public String getSegment(String segmentName, OracleDatabaseConnection database) throws SQLException {

      StringBuffer sb         = new StringBuffer();
      PreparedStatement pstmt = null;
      ResultSet rset          = null;

      String SQL = "select * from dba_segments where segment_name = ?";
      try {
        pstmt = database.getConn().prepareStatement(SQL);
        pstmt.setString(1,segmentName);
        rset  = pstmt.executeQuery();
        
        Hashtable links = new Hashtable();

        Link link = new Link();
        link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_SESS_ZOOM + "&database=" + database.getName());
        link.setText("Zoom");
        link.setAlt("Show Details of this session.");
        link.getVariables().add(new LinkVar(0, "sid"));
        links.put(new Integer(1), link);       
        
        sb.append(buildTabularDisplay(rset, new Hashtable(), "Segment Data"));
      } catch ( SQLException e ) {
        Tracer.log("Error in loading database data for segment " + segmentName, Tracer.ERROR, OBJECT_NAME);
        throw e;
      } finally {
        try {
          if ( pstmt != null ) pstmt.close();
        } catch ( Exception e ) {}
      }
      
      return sb.toString();
    }
    
    static public PageSet getRoles(OracleDatabaseConnection database, String username) throws SQLException {

      StringBuffer sb         = new StringBuffer();
      PreparedStatement pstmt = null;
      ResultSet rset          = null;

      String SQL = "select grantee, granted_role, admin_option, default_role, " +
                   " 'Roles Granted' \" \", 'Objects Granted' \" \", 'System Privs' \" \" " +
                   "  from dba_role_privs where grantee = ?";
      try {
        pstmt = database.getConn().prepareStatement(SQL);
        pstmt.setString(1, username);
        rset  = pstmt.executeQuery();
        
        Hashtable links = new Hashtable();

        Link link = new Link();
        link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_ROLE_ROLES + "&database=" + database.getName());
        link.setText("Zoom");
        link.setAlt("Show Roles for this Role.");
        link.getVariables().add(new LinkVar(1, "role"));
        links.put(new Integer(5), link);
        
        link = new Link();
        link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_ROLE_OBJECTS + "&database=" + database.getName());
        link.setText("Zoom");
        link.setAlt("Show Object Grants for this Role.");
        link.getVariables().add(new LinkVar(1, "role"));
        links.put(new Integer(6), link);
        
        link = new Link();
        link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_SYSTEM_PRIVS + "&database=" + database.getName());
        link.setText("Zoom");
        link.setAlt("Show System Privs for this Role.");
        link.getVariables().add(new LinkVar(1, "role"));
        links.put(new Integer(7), link);
           
        String title = "Granted Roles";
        return PageLoader.buildPageSet(rset, 0, 500, 1, links, new ArrayList(), 
                                       new ArrayList(), title, 500);
      } catch ( SQLException e ) {
          Tracer.log("Error in loading role List for user " + username, Tracer.ERROR, OBJECT_NAME);
          throw e;
      } finally {
          try {
              if ( pstmt != null ) pstmt.close();
          } catch ( Exception e ) {}
      }
    }
    
    static public PageSet getRoleRoles(OracleDatabaseConnection database, String role) throws SQLException {

      StringBuffer sb         = new StringBuffer();
      PreparedStatement pstmt = null;
      ResultSet rset          = null;

      String SQL = "select role, granted_role, admin_option, 'Roles Granted' \" \", 'Objects Granted' \" \"  from role_role_privs where role = ?";
      try {
        pstmt = database.getConn().prepareStatement(SQL);
        pstmt.setString(1, role);
        rset  = pstmt.executeQuery();
        
        Hashtable links = new Hashtable();
        
        Link link = new Link();
        link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_ROLE_ROLES + "&database=" + database.getName());
        link.setText("Zoom");
        link.setAlt("Show Roles for this Role.");
        link.getVariables().add(new LinkVar(1, "role"));
        links.put(new Integer(4), link);
        
        link = new Link();
        link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_ROLE_OBJECTS + "&database=" + database.getName());
        link.setText("Zoom");
        link.setAlt("Show Object Grants for this Role.");
        link.getVariables().add(new LinkVar(1, "role"));
        links.put(new Integer(5), link);
           
        String title = "Role Roles";
        return PageLoader.buildPageSet(rset, 0, 500, 1, links, new ArrayList(), 
                                       new ArrayList(), title, 500);
      } catch ( SQLException e ) {
          Tracer.log("Error in loading role roles for role " + role, Tracer.ERROR, OBJECT_NAME);
          throw e;
      } finally {
          try {
              if ( pstmt != null ) pstmt.close();
          } catch ( Exception e ) {}
      }
    }
    
    static public PageSet getDirectGrants(OracleDatabaseConnection database, String role) throws SQLException {

      StringBuffer sb         = new StringBuffer();
      PreparedStatement pstmt = null;
      ResultSet rset          = null;

      String SQL = "select * from dba_tab_privs where grantee = ?";
      try {
        pstmt = database.getConn().prepareStatement(SQL);
        pstmt.setString(1, role);
        rset  = pstmt.executeQuery();
        
        Hashtable links = new Hashtable();
           
        String title = "Direct Grants";
        return PageLoader.buildPageSet(rset, 0, 500, 1, links, new ArrayList(), 
                                       new ArrayList(), title, 500);
      } catch ( SQLException e ) {
          Tracer.log("Error in direct grants", Tracer.ERROR, OBJECT_NAME);
          throw e;
      } finally {
          try {
              if ( pstmt != null ) pstmt.close();
          } catch ( Exception e ) {}
      }
    }
    
    static public PageSet getSysPrivs(OracleDatabaseConnection database, String grantee) throws SQLException {

      StringBuffer sb         = new StringBuffer();
      PreparedStatement pstmt = null;
      ResultSet rset          = null;

      String SQL = "select * from dba_sys_privs where grantee = ?";
      try {
        pstmt = database.getConn().prepareStatement(SQL);
        pstmt.setString(1, grantee);
        rset  = pstmt.executeQuery();
        
        Hashtable links = new Hashtable();
           
        String title = "System Privs";
        return PageLoader.buildPageSet(rset, 0, 500, 1, links, new ArrayList(), 
                                       new ArrayList(), title, 500);
      } catch ( SQLException e ) {
          Tracer.log("Error in getting sys privs", Tracer.ERROR, OBJECT_NAME);
          throw e;
      } finally {
          try {
              if ( pstmt != null ) pstmt.close();
          } catch ( Exception e ) {}
      }
    }
    
    static public PageSet getObjectSummary(OracleDatabaseConnection database, String username) throws SQLException {

      StringBuffer sb         = new StringBuffer();
      PreparedStatement pstmt = null;
      ResultSet rset          = null;

      String SQL = "select owner, object_type, status, count(*) \"Count\" from dba_objects where owner = upper(?) " +
                   "   group by owner, object_type, status union all select owner, 'REFRESH GROUP', 'NA', count(*) from dba_rgroup where owner = upper(?) group by owner order by 2";
                   //"  union all select owner, 'DATAPUMP TABLE', 'NA', count(*) from dba_tables where owner = ? and table_name in (select job_name from dba_datapump_jobs) group by owner order by 2";
      
      try {
        pstmt = database.getConn().prepareStatement(SQL);
        pstmt.setString(1, username);
        pstmt.setString(2, username);
        //pstmt.setString(3, username);
        rset  = pstmt.executeQuery();
        
        Hashtable links = new Hashtable();
        
        Link link = new Link();
        link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_OBJECT_LIST + "&database=" + database.getName());
        link.setText("show objects");
        link.setAlt("Show Objects for this Type.");
        link.getVariables().add(new LinkVar(0, "schema"));
        link.getVariables().add(new LinkVar(1, "type"));
        links.put(new Integer(2), link);
        
        ArrayList hiddens = new ArrayList();
        hiddens.add(new Integer(1));
           
        String title = "User Object Summary";
        return PageLoader.buildPageSet(rset, 0, 500, 1, links, hiddens, 
                                       new ArrayList(), title, 500);
      } catch ( SQLException e ) {
          Tracer.log("Error in object summary", Tracer.ERROR, OBJECT_NAME);
          throw e;
      } finally {
          try {
              if ( pstmt != null ) pstmt.close();
          } catch ( Exception e ) {}
      }
    }
    
    static public PageSet getObjectList(OracleDatabaseConnection database, String objectType, String schema) throws SQLException {

      StringBuffer sb         = new StringBuffer();
      PreparedStatement pstmt = null;
      ResultSet rset          = null;
      
      String SQL = null;
      
      if ( objectType.equals("DATAPUMP TABLE") ) {
         SQL = "select owner, 'DATAPUMP TABLE', 'NA', table_name " +
                     "  from dba_tables where owner = ? " +
                     "  and table_name in (select job_name from dba_datapump_jobs)" + 
                     "  and ? = 'DATAPUMP TABLE'";
      } else {

	       SQL = "select owner, " +
	                   "       object_type, " +
	                   "       object_name, status " +
	                   "  from dba_objects " +
	                   "  where owner = upper(?) " +
	                   "    and object_type = ? " +
	                   "  order by object_type, object_name";
      }
      
      try {
        pstmt = database.getConn().prepareStatement(SQL);
        pstmt.setString(1, schema);
        pstmt.setString(2, objectType);
        rset  = pstmt.executeQuery();
        
        Hashtable links = new Hashtable();
        
        Link link = new Link();
        link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_OBJECT_DETAIL + "&database=" + database.getName());
        link.setText("show object");
        link.setAlt("Show Object Detail.");
        link.getVariables().add(new LinkVar(0, "owner"));
        link.getVariables().add(new LinkVar(1, "type"));
        link.getVariables().add(new LinkVar(2, "objname"));
        links.put(new Integer(3), link);
        
        ArrayList hiddens = new ArrayList();
        hiddens.add(new Integer(1));
           
        String title = "User Object Summary";
        return PageLoader.buildPageSet(rset, 0, 500, 1, links, new ArrayList(), 
                                       new ArrayList(), title, 500);
      } catch ( SQLException e ) {
          Tracer.log("Error in object summary", Tracer.ERROR, OBJECT_NAME);
          throw e;
      } finally {
          try {
              if ( pstmt != null ) pstmt.close();
          } catch ( Exception e ) {}
      }
    }
    
    static public PageSet getRefreshGroupList(OracleDatabaseConnection database, String schema) throws SQLException {

        StringBuffer sb         = new StringBuffer();
        PreparedStatement pstmt = null;
        ResultSet rset          = null;

        String SQL = "select owner, " +
                     "       'REFRESH GROUP', " +
                     "       name, refgroup " +
                     "  from dba_rgroup " +
                     "  where owner = upper(?) ";
        try {
          pstmt = database.getConn().prepareStatement(SQL);
          pstmt.setString(1, schema);
          rset  = pstmt.executeQuery();
          
          Hashtable links = new Hashtable();
          
          Link link = new Link();
          link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_OBJECT_DETAIL + "&database=" + database.getName());
          link.setText("show object");
          link.setAlt("Show Object Detail.");
          link.getVariables().add(new LinkVar(0, "owner"));
          link.getVariables().add(new LinkVar(1, "type"));
          link.getVariables().add(new LinkVar(2, "objname"));
          link.getVariables().add(new LinkVar(3, "refgroup"));
          links.put(new Integer(3), link);
          
          ArrayList hiddens = new ArrayList();
          hiddens.add(new Integer(1));
          hiddens.add(new Integer(4));
             
          String title = "User Object Summary";
          
          return PageLoader.buildPageSet(rset, 0, 500, 1, links, new ArrayList(), new ArrayList(), title, 500);
          
        } catch ( SQLException e ) {
            Tracer.log("Error in object summary", Tracer.ERROR, OBJECT_NAME);
            throw e;
        } finally {
            try {
                if ( pstmt != null ) pstmt.close();
            } catch ( Exception e ) {}
        }
      }
    
    static public PageSet getRoleObjects(OracleDatabaseConnection database, String role) throws SQLException {

      StringBuffer sb         = new StringBuffer();
      PreparedStatement pstmt = null;
      ResultSet rset          = null;

      String SQL = "select * from dba_tab_privs where grantee = ?";
      try {
        pstmt = database.getConn().prepareStatement(SQL);
        pstmt.setString(1, role);
        rset  = pstmt.executeQuery();
        Hashtable links = new Hashtable();
        String title = "Role Objects";
        return PageLoader.buildPageSet(rset, 0, 500, 1, links, new ArrayList(),new ArrayList(), title, 500);
      } catch ( SQLException e ) {
          Tracer.log("Error in loading role objects for role " + role, Tracer.ERROR, OBJECT_NAME);
          throw e;
      } finally {
          try {
              if ( pstmt != null ) pstmt.close();
          } catch ( Exception e ) {}
      }
    }
    
    static public PageSet getRoleSystemPrivs(OracleDatabaseConnection database, String role) throws SQLException {

      PreparedStatement pstmt = null;
      ResultSet rset          = null;

      String SQL = "select *  from role_sys_privs where role = ?";
      try {
        pstmt = database.getConn().prepareStatement(SQL);
        pstmt.setString(1, role);
        rset  = pstmt.executeQuery();
        
        Hashtable links = new Hashtable();
           
        String title = "Role System Privs";
        return PageLoader.buildPageSet(rset, 0, 500, 1, links, new ArrayList(), 
                                       new ArrayList(), title, 500);
      } catch ( SQLException e ) {
          Tracer.log("Error in loading role system privs for role " + role, Tracer.ERROR, OBJECT_NAME);
          throw e;
      } finally {
          try {
              if ( pstmt != null ) pstmt.close();
          } catch ( Exception e ) {}
      }
    }
    
    static public String getLatches(OracleDatabaseConnection database) throws SQLException {

      StringBuffer sb         = new StringBuffer();
      PreparedStatement pstmt = null;
      ResultSet rset          = null;

      String SQL = "select name, latch#,level#,wait_time,gets,misses,to_char(round(100*misses/decode(gets,0,.0000001,gets),4),'999.999') \"% Miss\", sleeps,immediate_gets,immediate_misses, " +
                   "       waiters_woken, waits_holding_latch, spin_gets, sleep1, sleep2 s2, " +
                   "       sleep3 s3, sleep4 s4, sleep5 s5, sleep6 s6, sleep7 s7, sleep8 s8, sleep9 s9, sleep10 s10, sleep11 s11 " +
                   "  from v$latch order by latch#";
                   //order by misses/decode(gets,0,.0000001,gets) desc";
      try {
        pstmt = database.getConn().prepareStatement(SQL);
        rset  = pstmt.executeQuery();
        
        Hashtable links = new Hashtable();

        Link link = new Link();
        link.setFileName(PowerDbaActions.DB_LATCH_CHILDREN, database);
        link.setText("Child Latch");
        link.setAlt("Latch Children.");
        link.getVariables().add(new LinkVar(2, "latchnum"));
        links.put(new Integer(1), link);
        
        String title = "Latches";
        return buildTabularDisplay(rset, links, "Latches");
        
      } catch ( SQLException e ) {
          Tracer.log("Error in loading Latch List", Tracer.ERROR, OBJECT_NAME);
          throw e;
      } finally {
          try {
              if ( pstmt != null ) pstmt.close();
          } catch ( Exception e ) {}
      }
    }
    
    static public String getParsingSummary(OracleDatabaseConnection database) throws SQLException {

      StringBuffer sb         = new StringBuffer();
      PreparedStatement pstmt = null;
      ResultSet rset          = null;

      String SQL = "select 'session_cached_cursors'  parameter, \n" +
                   "       lpad(value, 5)  value,  \n" +
                   "        decode(value, 0, '  n/a', to_char(100 * used / value, '990') || '%')  usage  \n" +
                   "   from ( select  \n" +
                   "         max(s.value)  used  \n" +
                   "         from v$statname  n,  \n" +
                   "              v$sesstat  s  \n" +
                   "        where n.name = 'session cursor cache count'  \n" +
                   "          and s.statistic# = n.statistic#),  \n" +
                   "       ( select  \n" +
                   "           value  \n" +
                   "         from  \n" +
                   "           v$parameter  \n" +
                   "         where  \n" +
                   "           name = 'session_cached_cursors'  \n" +
                   "       )  \n" +
                   "   union all  \n" +
                   "   select  \n" +
                   "     'open_cursors',  \n" +
                   "     lpad(value, 5),  \n" +
                   "     to_char(100 * used / value,  '990') || '%'  \n" +
                   "   from  \n" +
                   "     ( select  \n" +
                   "         max(sum(s.value))  used  \n" +
                   "       from  \n" +
                   "         v$statname  n,  \n" +
                   "         v$sesstat  s  \n" +
                   "       where  \n" +
                   "         n.name in ('opened cursors current', 'session cursor cache count') and  \n" +
                   "         s.statistic# = n.statistic#  \n" +
                   "       group by  \n" +
                   "         s.sid  \n" +
                   "     ),  \n" +
                   "     ( select  \n" +
                   "         value  \n" +
                   "       from  \n" +
                   "         v$parameter  \n" +
                   "       where  \n" +
                   "         name = 'open_cursors'  \n" +
                   "     ) ";
      try {
      
        pstmt = database.getConn().prepareStatement(SQL);
        rset  = pstmt.executeQuery();
        
        String title = "Parsing Parms";
        return buildTabularDisplay(rset, new Hashtable(), title, true, 50, false);
        
      } catch ( SQLException e ) {
          Tracer.log(e, "Error in Parsing Summary", Tracer.ERROR, OBJECT_NAME);
          throw e;
      } finally {
          try {
              if ( pstmt != null ) pstmt.close();
          } catch ( Exception e ) {}
      }
    }


    static public String getChildLatches(OracleDatabaseConnection database, String latchNum) throws SQLException {

      StringBuffer sb         = new StringBuffer();
      PreparedStatement pstmt = null;
      ResultSet rset          = null;
      boolean summarize       = false;
      
      String SQL = "";
      String title = null;
      
      try {
        pstmt = database.getConn().prepareStatement("select count(*) from v$latch_children where latch#=?");
        pstmt.setInt(1, Integer.parseInt(latchNum));
        rset  = pstmt.executeQuery();
        rset.next();
        if ( rset.getLong(1) > 100 ) {
          summarize = true;
        }
      } catch ( SQLException e ) {
          Tracer.log("Error counting child latches", Tracer.ERROR, OBJECT_NAME);
          throw e;
      } finally {
          try {
              if ( pstmt != null ) pstmt.close();
          } catch ( Exception e ) {}
      }
      
      if ( summarize ) {
      
        SQL = "select name, latch#, count(*) count, sum(wait_time) wait_time,sum(gets) gets,sum(misses) misses,to_char(round(sum(misses)/decode(sum(gets),0,.0000001,sum(gets)),4),'999.999') \"% Miss\", " +
               "       sum(sleeps) sleeps, sum(immediate_gets) immediate_gets, sum(immediate_misses) immediate_misses, " +
               "       sum(waiters_woken) waiters_woken, sum(waits_holding_latch) waiters_holding_latch, sum(spin_gets) spin_gets, sum(sleep1) sleep1, sum(sleep2) s2, " +
               "       sum(sleep3) s3, sum(sleep4) s4, sum(sleep5) s5, sum(sleep6) s6, sum(sleep7) s7, sum(sleep8) s8, sum(sleep9) s9, sum(sleep10) s10, sum(sleep11) s11 " +
               "  from v$latch_children " +
               "  where latch# = ? " +
               "  group by name, latch# " +
               "  order by sum(waits_holding_latch) desc --misses/decode(gets,0,.0000001,gets) desc";
               
        title = "Latch Children - Summarized";
        
      } else {         

        SQL = "select name, latch#,child#,level#,wait_time,gets,misses,to_char(round(misses/decode(gets,0,.0000001,gets),4),'999.999') \"% Miss\", " +
             "       sleeps,immediate_gets,immediate_misses, " +
             "       waiters_woken, waits_holding_latch, spin_gets, sleep1, sleep2 s2, " +
             "       sleep3 s3, sleep4 s4, sleep5 s5, sleep6 s6, sleep7 s7, sleep8 s8, sleep9 s9, sleep10 s10, sleep11 s11 " +
             "  from v$latch_children " +
             "  where latch# = ? " +
             "  order by waits_holding_latch desc --misses/decode(gets,0,.0000001,gets) desc";
             
        title = "Latch Children - All";
        
      }

      try {
        pstmt = database.getConn().prepareStatement(SQL);
        pstmt.setInt(1, Integer.parseInt(latchNum));
        rset  = pstmt.executeQuery();
      

        return buildTabularDisplay(rset, new Hashtable(), title);
      } catch ( SQLException e ) {
          Tracer.log("Error in loading Latch Children List", Tracer.ERROR, OBJECT_NAME);
          throw e;
      } finally {
          try {
              if ( pstmt != null ) pstmt.close();
          } catch ( Exception e ) {}
      }
    }

    static public String getRedoLog(OracleDatabaseConnection database) throws SQLException {

      StringBuffer sb         = new StringBuffer();
      PreparedStatement pstmt = null;
      ResultSet rset          = null;

      String SQL = "select l.*, lf.type, lf.member from v$log l, v$logfile lf " +
                   "  where l.group# = lf.group# order by sequence#";
      try {
        pstmt = database.getConn().prepareStatement(SQL);
        rset  = pstmt.executeQuery();
        
        sb.append(buildTabularDisplay(rset, new Hashtable(), "Redo Log Files"));
      } catch ( SQLException e ) {
        Tracer.log("Error in loading Redo Log", Tracer.ERROR, OBJECT_NAME);
        throw e;
      } finally {
        try {
            if ( pstmt != null ) pstmt.close();
        } catch ( Exception e ) {}
      }

      return sb.toString();
    }
    
    
    static public OracleBaseObject getTable(OracleDatabaseConnection database, String tableName, String owner) throws SQLException {

        StringBuffer sb         = new StringBuffer();
        PreparedStatement pstmt = null;
        ResultSet rset          = null;

        String SQL = "select table_name, owner, num_rows from dba_tables";
        OracleBaseObject obo;
        try {
            pstmt = database.getConn().prepareStatement(SQL);
            rset  = pstmt.executeQuery(); 
            
            obo = new OracleBaseObject("table", rset.getString(2), rset.getString(1));
            obo.setRowCount(rset.getLong(3));
            return obo;
            
        } catch ( SQLException e ) {
            Tracer.log("Error in loading system event data", Tracer.ERROR, OBJECT_NAME);
            throw e;
        } finally {
            try {
                if ( pstmt != null ) pstmt.close();
            } catch ( Exception e ) {}
        }
        
    }

    static public String getSystemEvent(OracleDatabaseConnection database) throws SQLException {

        StringBuffer sb         = new StringBuffer();
        PreparedStatement pstmt = null;
        ResultSet rset          = null;

        String SQL = "select " +
                     "       event \"Event\",                                  " +
                     "       round(time_waited/100,0) \"Time Waited (secs)\",  " +
                     "       round(average_wait/100,0) \"Average Wait (secs)\", " +
                     "       total_waits \"Total Waits\",                      " +
                     "       total_timeouts \"Total Timeouts\"                " +
                     "  from v$system_event                                    " +
                     "  where event not like 'PX%'   " +
                     "    and event not like 'Queue Monitor%' " +
                     "    and event not in ('smon timer', " +
                     "               'pmon timer', " +
                     "               'rdbms ipc message', " +
                     "               'Null event', " +
                     "               'parallel query dequeue', " +
                     "               'pipe get', " +
                     "               'client message', " +
                     "               'SQL*Net message to client', " +
                     "               'SQL*Net message from client', " +
                     "               'SQL*Net more data from client', " +
                     "               'SQL*Net more data to client', " +
                     "               'SQL*Net message from dblink', " +
                     "               'rdbms ipc reply', " +
                     "               'dispatcher timer', " +
                     "               'virtual circuit status', " +
                     "               'lock manager wait for remote message', " +
                     "               'PX Idle Wait', " +
                     "               'PX Deq: Execution Msg', " +
                     "               'PX Deq: Table Q Normal', " +
                     "               'wakeup time manager', " +
                     "                'slave wait', " +
                     "               'i/o slave wait', " +
                     "               'jobq slave wait', " +
                     "               'null event', " +
                     "               'gcs remote message', " +
                     "               'gcs for action', " +
                     "               'ges remote message', " +
                     "               'queue messages', " +
                     "               'ARCH wait on SENDREQ') " +
                     "  order by time_waited desc";
        try {
            pstmt = database.getConn().prepareStatement(SQL);
            rset  = pstmt.executeQuery(); 
            
            Hashtable links = new Hashtable();
            
            Link link = new Link();
            link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_WAIT_DETAIL + "&database=" + database.getName());
            link.setType("Wait Event Detail");
            link.setAlt("Show Details of this Wait Event.");
            link.getVariables().add(new LinkVar(1, "event"));
            links.put(new Integer(1), link);   
            
            sb.append(buildTabularDisplay(rset, links, "System Wait Events"));            
        } catch ( SQLException e ) {
            Tracer.log("Error in loading system event data", Tracer.ERROR, OBJECT_NAME);
            throw e;
        } finally {
            try {
                if ( pstmt != null ) pstmt.close();
            } catch ( Exception e ) {}
        }
        
        return sb.toString();
    }

    static public String getCapture(OracleDatabaseConnection database) throws SQLException, Exception {

        StringBuffer sb         = new StringBuffer();
        PreparedStatement pstmt = null;
        ResultSet rset          = null;
        
        String SQL2 = "SELECT capture_name, " +
                      "       c.inst_id \"Instance\", " +
                      "       SUBSTR(s.PROGRAM,INSTR(S.PROGRAM,'(')+1,4) \"Process Name\", " + 
                      "       c.SID \"Sid\", " +
                      "       c.STATE \"State\", " +
                      "       s.EVENT \"Wait\", " +
                      "       c.state_changed_time, " +
                      "       c.TOTAL_MESSAGES_CAPTURED \"Messages Captured\", " +
                      "       c.TOTAL_MESSAGES_ENQUEUED \"Messages Enqueued\" " +
                      "  FROM GV$STREAMS_CAPTURE c, " +
                      "       GV$SESSION s " +
                      "  WHERE c.inst_id     = s.inst_id " +
                      "    and c.SID         = s.SID " + 
                      "    and c.SERIAL#     = s.SERIAL# ";
        
        String tbls = "select table_owner, count(*) \"Count\" from dba_capture_prepared_tables group by table_owner";
        String schs = "select schema_name from dba_capture_prepared_schemas";
       
        String queueTables = "select owner, queue_table, owner_instance, primary_instance, secondary_instance " +
                              "  from dba_queue_tables where owner = 'STRM_ADMIN'";


        try {
        
            Query q = QueryHolder.getQuery(database, "capture");
            
            PageSet pageSet = q.getPageSet();
            PageGenerator pg = new PageGenerator(pageSet.getPage(1));
            pg.setDatabase(q.getDatabase().getName());
            try {
                sb.append(pg.getHtmlNew());
            } catch (Exception e) {
                Tracer.log(e, "Error Getting Html from Pageset", Tracer.DEBUG, OBJECT_NAME);
                throw e;
            }
            
            sb.append("<hr>");

            pstmt = database.getConn().prepareStatement(SQL2);
            rset  = pstmt.executeQuery();
            
            // 1st link
            Hashtable links = new Hashtable();
            Link link = new Link();
            link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_SESS_ZOOM + "&database=" + database.getName());
            link.setText("Sid");
            link.setAlt("Show Session.");
            ArrayList vars = new ArrayList();

            LinkVar lv = new LinkVar();
            lv.setIndex(4);                                 // 1st pos in query
            lv.setVarName("sid");
            vars.add(lv);

            link.setVariables(vars);
            links.put(new Integer(3), link);
            sb.append(buildTabularDisplay(rset, links, "Capture Processes"));       
            pstmt.close();

            pstmt = database.getConn().prepareStatement(tbls);
            rset  = pstmt.executeQuery();
            sb.append("<hr>");
            sb.append(buildTabularDisplay(rset, new Hashtable(), "Instantiated Tables"));
            pstmt.close();
            
            pstmt = database.getConn().prepareStatement(schs);
            rset  = pstmt.executeQuery();
            sb.append("<hr>");
            sb.append(buildTabularDisplay(rset, new Hashtable(), "Instantiated Schemas"));
            pstmt.close();
            
            sb.append("<hr>");
            
            pstmt = database.getConn().prepareStatement(queueTables);
            rset  = pstmt.executeQuery();
            sb.append(buildTabularDisplay(rset, new Hashtable(), "Queue Tables"));
            pstmt.close();


        } catch ( SQLException e ) {
            Tracer.log("Error in loading capture data.", Tracer.ERROR, OBJECT_NAME);
            throw e;
        } finally {
            try {
                if ( pstmt != null ) pstmt.close();
            } catch ( Exception e ) {}
        }

        return sb.toString();
    }

    static public String getStreamsSummary(OracleDatabaseConnection database) throws SQLException {

        StringBuffer sb         = new StringBuffer();
        PreparedStatement pstmt = null;
        ResultSet rset          = null;
        
        String SQL =   
          " select l.name, to_char(l.first_time, 'mm/dd/yyyy hh24:mi:ss') first_time " +
          "   from DBA_REGISTERED_ARCHIVED_LOG l, (select min(required_checkpoint_scn) lscn from dba_capture) c " +
          "     where c.lscn between l.first_scn and l.next_scn order by l.thread#" ;
        
//        "select name MIN_KEEP_LOG from v$archived_log a, " + 
//        " (select min(ckpt_scn) ckpt_scn from " + 
//        " (select ckpt_scn from (select distinct ckpt_scn from system.logmnr_restart_ckpt$ " + 
//        " where ckpt_scn <= (select min(applied_scn) from dba_capture) " + 
//        " order by ckpt_scn desc) " + 
//        " where rownum < 3)) b " + 
//        " where b.ckpt_scn between a.first_change# and a.next_change#";
        
        

//        String SQL = "select capt.status \"Capture Status\", " + 
//                    "        app.status \"Apply Status\", " +
//                     "       prop.status \"Propagation Status\", " + 
//                     "       errors.cnt \"Apply Errors\" " +
//                     " from " +
//                     "(select status from dba_capture) capt, " +
//                     "(select status from dba_apply) app, " +
//                     "(select DECODE(SCHEDULE_DISABLED,'Y', 'DISABLED','N', 'ENABLED') status from dba_queue_schedules) prop, " +
//                     "(select count(*) cnt from dba_apply_error) errors";                     
        
        try {
        
            pstmt = database.getConn().prepareStatement(SQL);
            rset  = pstmt.executeQuery();

            Hashtable links = new Hashtable();
//            Link link = new Link();
//            link.setFileName("powerdba.jsp?formaction=" + PowerDbaPages.DB_STREAMS + "&database=" + database.getName());
//            link.setText("Error Count");
//            link.setAlt("Show Apply Errors.");
//            ArrayList vars = new ArrayList();
//            link.setVariables(vars);
//            links.put(new Integer(4), link);
//
//            link = new Link();
//            link.setFileName("powerdba.jsp?formaction=" + PowerDbaPages.DB_STREAMS_CAPTURE + "&database=" + database.getName());
//            link.setText("Capture Process");
//            link.setAlt("Show Capture Process Details.");
//            vars = new ArrayList();
//            link.setVariables(vars);
//            links.put(new Integer(1), link);
//
//            link = new Link();
//            link.setFileName("powerdba.jsp?formaction=" + PowerDbaPages.DB_STREAMS_APPLY + "&database=" + database.getName());
//            link.setText("Apply Process");
//            link.setAlt("Show Apply Process Details.");
//            vars = new ArrayList();
//            link.setVariables(vars);
//            links.put(new Integer(2), link);
//
//            link = new Link();
//            link.setFileName("powerdba.jsp?formaction=" + PowerDbaPages.DB_STREAMS_PROPAGATION + "&database=" + database.getName());
//            link.setText("Propagation Process");
//            link.setAlt("Show Propagation Process Details.");
//            vars = new ArrayList();
//            link.setVariables(vars);
//            links.put(new Integer(3), link);
            
            return buildTabularDisplay(rset, links,  null);

        } catch ( SQLException e ) {
            Tracer.log("Error in loading streams log data " +SQL, Tracer.ERROR, OBJECT_NAME);
            throw e;
        } finally {
            try {
                if ( pstmt != null ) pstmt.close();
            } catch ( Exception e ) {}
        }
    }


    static public String getQueues(OracleDatabaseConnection database) throws SQLException {

        StringBuffer sb         = new StringBuffer();
        PreparedStatement pstmt = null;
        ResultSet rset          = null;

        String SQL = "select * from v$buffered_queues";

        try {

            pstmt = database.getConn().prepareStatement(SQL);
            rset  = pstmt.executeQuery();

            sb.append(buildTabularDisplay(rset, new Hashtable(), "Buffered Queues"));

        } catch ( SQLException e ) {
            Tracer.log("Error in loading capture data " +SQL, Tracer.ERROR, OBJECT_NAME);
            throw e;
        } finally {
            try {
                if ( pstmt != null ) pstmt.close();
            } catch ( Exception e ) {}
        }

        return sb.toString();
    }
    
//    static public String getApplyErrors(OracleDatabaseConnection database) throws SQLException {
//
//        StringBuffer sb         = new StringBuffer();
//        PreparedStatement pstmt = null;
//        ResultSet rset          = null;
//
//        String SQL = "select * from dba_apply_error order by source_commit_scn";
//
//        try {
//
//          pstmt = database.getConn().prepareStatement(SQL);
//          rset  = pstmt.executeQuery();
//          
//          Hashtable links = new Hashtable();
//            
//          Link link = new Link();
//          link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_STREAMS_TRX + "&database=" + database.getName());
//          link.setText("LCR");
//          link.setAlt("Show LCR.");
//          link.getVariables().add(new LinkVar(4, "streamstrx"));
//          links.put(new Integer(4), link); 
//
//          sb.append(buildTabularDisplay(rset, links, "Apply Errors"));
//
//        } catch ( SQLException e ) {
//            Tracer.log("Error in loading apply errors html " +SQL, Tracer.ERROR, OBJECT_NAME);
//            throw e;
//        } finally {
//            try {
//                if ( pstmt != null ) pstmt.close();
//            } catch ( Exception e ) {}
//        }
//
//        return sb.toString();
//    }

    static public String getApply(OracleDatabaseConnection database) throws SQLException {

        StringBuffer sb         = new StringBuffer();
        PreparedStatement pstmt = null;
        ResultSet rset          = null;

        String SQL = "select apply_name, queue_owner, queue_name, rule_set_name, apply_user, status, apply_tag from dba_apply";

        String reader = "SELECT r.apply_name, SUBSTR(s.PROGRAM,INSTR(S.PROGRAM,'(')+1,4) PROCESS_NAME, r.inst_id, r.sid, " +
                      " r.STATE, r.TOTAL_MESSAGES_DEQUEUED, " +
                      " DECODE(ap.APPLY_CAPTURED,'YES','Captured LCRS','NO','User-enqueued messages','UNKNOWN') MESSAGE_TYPE, " +
                      " vs.value \"PGA Memory\" " +
                      "  FROM GV$STREAMS_APPLY_READER r, GV$SESSION s, DBA_APPLY ap, GV$sesstat vs " + 
                      "  WHERE r.SID = s.SID " +
                      "    AND r.inst_id = s.inst_id " +
                      "    AND r.SERIAL# = s.SERIAL# " +
                      "    AND s.sid = vs.sid " +
                      "    AND s.inst_id = vs.inst_id " +
                      "    AND vs.statistic#=20 " +
                      "    AND r.APPLY_NAME = ap.APPLY_NAME ";

        String coord = "SELECT apply_name, SUBSTR(s.PROGRAM,INSTR(S.PROGRAM,'(')+1,4) PROCESS_NAME,c.inst_id, c.SID,c.STATE, " +
                       " c.TOTAL_RECEIVED, c.TOTAL_APPLIED, c.TOTAL_ERRORS \"Cumulative Errors\", vs.value \"PGA Memory\"   " +
                       "    FROM GV$STREAMS_APPLY_COORDINATOR c, GV$SESSION s, Gv$sesstat vs " +
                       "    WHERE c.SID         = s.SID " +
                       "      AND c.inst_id     = s.inst_id " +
                       "      AND vs.sid        = s.sid " +
                       "      and vs.inst_id    = s.inst_id " +
                       "      and vs.statistic# = 20 " +
                       "      and c.SERIAL#     = s.SERIAL#";

        String srvr =  "select apply_name ,a.inst_id, a.sid \"SID\", state \"State\", " +
                       "   total_assigned \"Total Assigned\", total_admin \"Total Admin\", " +
                       "   total_messages_applied \"Ttl Msg Applied\", elapsed_dequeue_time \"DQ Time\", " +
                       "   elapsed_apply_time \"Apply Time\", s.value \"PGA Memory\" from gv$streams_apply_server a, gv$sesstat s " +
                       "   where a.sid = s.sid and a.inst_id = s.inst_id and s.statistic#=20";

        String objs = "SELECT SOURCE_DATABASE, SOURCE_OBJECT_OWNER \"Schema\", count(*) \"Count\" FROM DBA_APPLY_INSTANTIATED_OBJECTS group by source_database, source_object_owner";

        String latency = "SELECT apply_name, " +
                         " to_char(round((DEQUEUE_TIME-DEQUEUED_MESSAGE_CREATE_TIME)*86400,2),'9,999.99') || ' Seconds' LATENCY, " +
                         " TO_CHAR(DEQUEUED_MESSAGE_CREATE_TIME,'HH24:MI:SS MM/DD/YY') CREATION, " +
                         " TO_CHAR(DEQUEUE_TIME,'HH24:MI:SS MM/DD/YY') LAST_DEQUEUE, DEQUEUED_MESSAGE_NUMBER  " +
                         "  FROM GV$STREAMS_APPLY_READER order by apply_name";

        String rules = "select streams_name \"Streams Name\", " +
                       "       rule_owner||'.'||rule_name \"Rule\", " +
                       "       table_owner||'.'||table_name \"Table\", " +
                       "       rule_type \"Rule Type\", " +
                       "       subsetting_operation \"Subsetting Operation\", " +
                       "       include_tagged_lcr \"Inc Tagged LCR\", " +
                       "       source_database \"Source DB\" " +
                       " FROM dba_streams_table_rules " +
                       " where streams_type = 'APPLY' " +
                       " order by table_owner, table_name";


        try {
            pstmt = database.getConn().prepareStatement(SQL);
            rset  = pstmt.executeQuery();

            Hashtable links = new Hashtable();

            // 1st link
            Link link = new Link();
            link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_STREAMS_RULE_SETS_RN + "&database=" + database.getName());
            link.setText("Ruleset Name");
            link.setAlt("Show Rule Set.");
            ArrayList vars = new ArrayList();
            LinkVar lv = new LinkVar();
            lv.setIndex(4);                                 // 1st pos in query
            lv.setVarName("rid");
            vars.add(lv);
            link.setVariables(vars);
            links.put(new Integer(4), link);
            
            sb.append(buildTabularDisplay(rset, links, "Apply Process Config"));
            if ( pstmt != null ) pstmt.close();
            sb.append("<hr>");

            pstmt = database.getConn().prepareStatement(reader);
            rset  = pstmt.executeQuery();

            links = new Hashtable();

            // 1st link
            link = new Link();
            link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_SESS_ZOOM + "&database=" + database.getName());
            link.setText("sid");
            link.setAlt("Show sid.");
            vars = new ArrayList();

            lv = new LinkVar();
            lv.setIndex(4);                                 // 1st pos in query
            lv.setVarName("sid");
            vars.add(lv);

            link.setVariables(vars);
            links.put(new Integer(4), link);
            sb.append(buildTabularDisplay(rset, links, "Reader Progress"));
            if ( pstmt != null ) pstmt.close();            

            sb.append("<hr>");

            pstmt = database.getConn().prepareStatement(coord);
            rset  = pstmt.executeQuery();

            links = new Hashtable();

            // 1st link
            link = new Link();
            link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_SESS_ZOOM + "&database=" + database.getName());
            link.setText("sid");
            link.setAlt("Show sid.");
            vars = new ArrayList();
            lv = new LinkVar();
            lv.setIndex(4);                                 // 1st pos in query
            lv.setVarName("sid");
            vars.add(lv);
            link.setVariables(vars);
            links.put(new Integer(4), link);

            link = new Link();
            link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_STREAMS + "&database=" + database.getName());
            link.setText("Error Count");
            link.setAlt("Show Apply Errors.");
            vars = new ArrayList();
            link.setVariables(vars);
            links.put(new Integer(8), link);

            sb.append(buildTabularDisplay(rset, links, "Coordinator"));
            if ( pstmt != null ) pstmt.close();
            sb.append("<hr>");
            
            links = new Hashtable();

            // 1st link
            link = new Link();
            link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_SESS_ZOOM + "&database=" + database.getName());
            link.setText("sid");
            link.setAlt("Show sid.");
            vars = new ArrayList();

            lv = new LinkVar();
            lv.setIndex(3);                                 // 1st pos in query
            lv.setVarName("sid");
            vars.add(lv);

            link.setVariables(vars);
            links.put(new Integer(3), link);
            pstmt = database.getConn().prepareStatement(srvr);
            rset  = pstmt.executeQuery();
            sb.append(buildTabularDisplay(rset, links, "Server Progress"));
            if ( pstmt != null ) pstmt.close();
            sb.append("<hr>");

            pstmt = database.getConn().prepareStatement(latency);
            rset  = pstmt.executeQuery();
            sb.append(buildTabularDisplay(rset, new Hashtable(), "Apply Process Latency"));
            if ( pstmt != null ) pstmt.close();
            sb.append("<hr>");
            
            // 1st link
            links = new Hashtable();
            link = new Link();
            link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_INSTANTIATED_TABLES + "&database=" + database.getName());
            
            link.setText("Instantiated Tables");
            link.setAlt("Show Tables.");
            vars = new ArrayList();
            lv = new LinkVar();
            lv.setIndex(1);                                 // 1st pos in query
            lv.setVarName("sourcedb");
            vars.add(lv);
            lv = new LinkVar();
            lv.setIndex(2);                                 // 2nd pos in query
            lv.setVarName("sourceschema");
            vars.add(lv);
            
            link.setVariables(vars);
            links.put(new Integer(3), link);

            pstmt = database.getConn().prepareStatement(objs);
            rset  = pstmt.executeQuery();
            sb.append(buildTabularDisplay(rset, links, "Instantiated Objects"));
             if ( pstmt != null ) pstmt.close();       

            pstmt = database.getConn().prepareStatement(rules);
            rset  = pstmt.executeQuery();
            sb.append("<hr>");
            sb.append(buildTabularDisplay(rset, new Hashtable(), "Apply Rules"));
            if ( pstmt != null ) pstmt.close(); 

        } catch ( SQLException e ) {
            Tracer.log("Error in loading apply data", Tracer.ERROR, OBJECT_NAME);
            throw e;
        } finally {
            try {
                if ( pstmt != null ) pstmt.close();
            } catch ( Exception e ) {}
        }
    
        return sb.toString();
    }

    static public String getPropagation(OracleDatabaseConnection database) throws SQLException {

        StringBuffer sb         = new StringBuffer();
        PreparedStatement pstmt = null;
        ResultSet rset          = null;

        String SQL = "select propagation_name, " +
        		         "source_queue_owner || '.' || source_queue_name \"Source Queue\", " +
        		         "destination_queue_owner || '.' || destination_queue_name \"Destination Queue\", " +
        		         "destination_dblink \"Destination DB\", " +
        		         "rule_set_owner \"Owner\", " +
                     "rule_set_name \"Rule Set Name\" from dba_propagation";

        String SQL2 = "SELECT s.schema || '.' || qname \"Queue\", " +
                      "sub.address \"Destination\", " +
                      "s.NEXT_TIME \"Next Time\", " + 
                      "s.LATENCY \"Latency\", " +
                      "s.schedule_disabled \"Disabled\", " +
                      "PROCESS_NAME \"Job\", s.session_id \"sid\" , " +
                      " s.total_time, s.total_number, s.total_bytes, last_error_msg \"Error\", " +
                      " last_error_time \"Error Time\", failures " +
                      "  FROM DBA_QUEUE_SCHEDULES s, " +
                      "       DBA_PROPAGATION p, " +
                      "       DBA_QUEUE_SUBSCRIBERS sub" +
                      "  WHERE s.DESTINATION = p.DESTINATION_DBLINK (+) " +
                      "    AND s.SCHEMA      = p.SOURCE_QUEUE_OWNER (+) " +
                      "    AND s.QNAME       = p.SOURCE_QUEUE_NAME (+) " +
                      "    AND s.qname       = sub.queue_name " +
                      "    AND sub.consumer_name is null";
        
        
/*        select q.queue_table "Source Queue Table", 
        qs.qname "Source Queue",  
        '  ---->  ' "Direction", 
        qs.destination "Destination DB", 
        s.address "Destination Queue" 
   from dba_queue_schedules qs, 
        dba_queue_subscribers s, 
        dba_queues q
   where qs.qname = s.queue_name
     and q.name = qs.qname
     and s.consumer_name is null
*/

        try {
            pstmt = database.getConn().prepareStatement(SQL);
            rset  = pstmt.executeQuery();

            Hashtable links = new Hashtable();
            
            // 1st link
            Link link = new Link();
            link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_STREAMS_RULE_SETS_RN + "&database=" + database.getName());
            link.setText("Ruleset Name");
            link.setAlt("Show Rule Set.");
            ArrayList vars = new ArrayList();
            LinkVar lv = new LinkVar();
            lv.setIndex(6);                                 // 1st pos in query
            lv.setVarName("rid");
            vars.add(lv);
            link.setVariables(vars);
            links.put(new Integer(6), link);
            
            sb.append(buildTabularDisplay(rset, links, "Propagation Config"));    
            pstmt.close();

            sb.append("<hr>");

            pstmt = database.getConn().prepareStatement(SQL2);
            rset  = pstmt.executeQuery();
            sb.append(buildTabularDisplay(rset, new Hashtable(), "Propagation Processes"));            
            pstmt.close();

           
        } catch ( SQLException e ) {
            Tracer.log("Error in loading propagation data", Tracer.ERROR, OBJECT_NAME);
            throw e;
        } finally {
            try {
                if ( pstmt != null ) pstmt.close();
            } catch ( Exception e ) {}
        }

        return sb.toString();
    }

    static public String getInstanceInfo(OracleDatabaseConnection database) throws SQLException {

        StringBuffer sb         = new StringBuffer();
        PreparedStatement pstmt = null;
        ResultSet rset          = null;
        String SQL = "select * from v$instance";

        try {
          pstmt = database.getConn().prepareStatement(SQL);
          rset  = pstmt.executeQuery();
          sb.append(buildDetailDisplay(rset, new Hashtable(), "Instance Status", 3));
          sb.append(DIV);
          sb.append(ProcessDAO.getInitOra(database));
        } catch ( SQLException e ) {
          Tracer.log("Error in loading Instance Info", Tracer.ERROR, OBJECT_NAME);
          throw e;
        } finally {
          try {
              if ( pstmt != null ) pstmt.close();
          } catch ( Exception e ) {}
        }

        return sb.toString();
    }


    static public String getStreamsHtml(OracleDatabaseConnection database) throws SQLException {

        StringBuffer sb = new StringBuffer();

        PreparedStatement pstmt = null;
        ResultSet rset          = null;

        String SQL = "select table_name \"Interface Table Name\", " +
                     "       error_message \"Oracle Message\", " +
                     "       count(*) \"Error Count\", " +
                     "       max(max_scn) \"Latest SCN\" " +
                     "  from " +
                     " (select txn_id ,error_message, table_name, max(source_commit_scn) max_scn " +
                     "     from str_view_txn_errors "  +
                     "     group by txn_id, error_message, table_name) t " +
                     "  group by table_name, error_message " +
                     "  order by table_name, error_message";

        try {
            Hashtable links = new Hashtable();

            // 1st link
            Link link = new Link();
            link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_STREAMS_ERROR_DETAIL + "&database=" + database.getName());
            link.setText("Table");
            link.setAlt("Show Errors.");
            ArrayList vars = new ArrayList();

            LinkVar lv = new LinkVar();
            lv.setIndex(1);                                 // 1st pos in query
            lv.setVarName("table");
            vars.add(lv);

            link.setVariables(vars);
            links.put(new Integer(1), link);

            // 2nd link
            link = new Link();
            link.setFileName("http://otn.oracle.com/pls/db92/db92.drilldown?remark=&book=&preference=");
            link.setText("Error");
            link.setAlt("Research Error on tahiti.oracle.com.");
            vars = new ArrayList();
            lv = new LinkVar();
            lv.setIndex(2);             // get value from 2nd pos in query
            lv.setVarName("word");
            lv.setOffset(10);           // Put only the 1st 10 characters into query parms
            vars.add(lv);
            link.setVariables(vars);
            links.put(new Integer(2), link);

            pstmt = database.getConn().prepareStatement(SQL);
            rset  = pstmt.executeQuery();


            // Create the body of the report...
            sb.append(buildTabularDisplay(rset, links, null));



        } catch ( SQLException e ) {
            Tracer.log("Error in loading Partition in load", Tracer.ERROR, OBJECT_NAME);
            throw e;
        } finally {
            try {
                if ( pstmt != null ) pstmt.close();
            } catch ( Exception e ) {}
        }
        return sb.toString();
    }

    static public String getSourceHtml(String dbObject, OracleDatabaseConnection database) throws SQLException {

        StringBuffer sb = new StringBuffer();

        PreparedStatement pstmt = null;
        ResultSet rset          = null;

        String SQL = "select replace(text,' ','&nbsp;') \"Source Code\" from dba_source " +
                     "  where decode(instr(?,'.'), 0 ,upper(name),upper(owner||'.'||name)) = upper(?) order by line";

        try {

            pstmt = database.getConn().prepareStatement(SQL);
            pstmt.setString(1, dbObject);
            pstmt.setString(2, dbObject);
            rset  = pstmt.executeQuery();
        
            // Create the body of the report...
            sb.append(buildTabularDisplay(rset, new Hashtable(), null, false));

        } catch ( SQLException e ) {
            Tracer.log("Error in loading Partition in load", Tracer.ERROR, OBJECT_NAME);
            throw e;
        } finally {
            try {
                if ( pstmt != null ) pstmt.close();
            } catch ( Exception e ) {}
        }
        return sb.toString();
    }
    
    static public String getSource(String schema, String name, String oType, OracleDatabaseConnection database) throws SQLException {

        StringBuffer sb = new StringBuffer();
        StringBuffer sql = new StringBuffer();

        PreparedStatement pstmt = null;
        ResultSet rset          = null;

        String SQL = "select replace(text,' ','&nbsp;') \"Source Code\" from dba_source " +
                     "  where name = ? and type = ? and owner = ? order by line";

        try {

            pstmt = database.getConn().prepareStatement(SQL);
            pstmt.setString(1, name);
            pstmt.setString(2, oType);
            pstmt.setString(3, schema);
            rset  = pstmt.executeQuery();
            
            while ( rset.next() ) sql.append(rset.getString(1));
            
            sb.append("<table width=150 bgcolor='#ffffff' cellspacing=0 cellpadding=0>");
            sb.append("<tr>");
            sb.append("<th align=left bgcolor='#73969c'><font size=-1>&nbsp;" + oType + " Source&nbsp;</font><th>");
            sb.append("</tr>");
            sb.append("</table>");

            if ( sql.length() > 0 ) {
              sb.append("<table cellspacing=0 bgcolor='#ffffff' width=80%><tr>");
              sb.append("<tr>");
              sb.append("<td bgcolor='#e5e5e5'>");
              sb.append("<table title='Explained SQL'><tr><td><font size=-1>");
              sb.append(HtmlComponent.getTextArea("sql",sql.toString(),130,40) + "</font></td></tr></table>");
              sb.append("</td>");
            }
            sb.append("</tr></table>");            

        } catch ( SQLException e ) {
            Tracer.log("Error in get package source", Tracer.ERROR, OBJECT_NAME);
            throw e;
        } finally {
            try {
                if ( pstmt != null ) pstmt.close();
            } catch ( Exception e ) {}
        }
        return sb.toString();
    }


    static public String getGenericDetail(OracleDatabaseConnection database, String sql, 
                                          Hashtable links, String title, long key, int columns) throws SQLException {

        StringBuffer sb = new StringBuffer();
        PreparedStatement pstmt = null;
        ResultSet rset          = null;

        try {

            pstmt = database.getConn().prepareStatement(sql);
            pstmt.setLong(1,key);
            rset  = pstmt.executeQuery();
            sb.append(buildDetailDisplay(rset, links, title, columns));

        } catch ( SQLException e ) {
            Tracer.log("Error in loading " + title, Tracer.ERROR, OBJECT_NAME);
            throw e;
        } finally {
            try {
                if ( pstmt != null ) pstmt.close();
            } catch ( Exception e ) {}
        }
        return sb.toString();
    }
    
    static public String getGenericDetail(OracleDatabaseConnection database, String sql, 
                                          Hashtable links, String title, String key, int columns) throws SQLException {

			StringBuffer sb = new StringBuffer();
			PreparedStatement pstmt = null;
			ResultSet rset          = null;
			
		  try {
			
			  pstmt = database.getConn().prepareStatement(sql);
			  pstmt.setString(1,key);
			  rset  = pstmt.executeQuery();
			  sb.append(buildDetailDisplay(rset, links, title, columns));
			
		  } catch ( SQLException e ) {
			  Tracer.log("Error in loading " + title, Tracer.ERROR, OBJECT_NAME);
			  throw e;
		  } finally {
			  try {
		    	if ( pstmt != null ) pstmt.close();
		  	} catch ( Exception e ) {}
		  }
			return sb.toString();
		}
    


    static public String getGenericTabular(OracleDatabaseConnection database, String sql, 
                                           Hashtable links, String title, long key, boolean border) throws SQLException {

      StringBuffer sb = new StringBuffer();
      PreparedStatement pstmt = null;
      ResultSet rset          = null;

      try {

        pstmt = database.getConn().prepareStatement(sql);
        pstmt.setLong(1,key);
        rset  = pstmt.executeQuery();
        sb.append(buildTabularDisplay(rset, links, title, border));

      } catch ( SQLException e ) {
        Tracer.log("Error in loading " + title, Tracer.ERROR, OBJECT_NAME);
        throw e;
      } finally {
        try {
          if ( pstmt != null ) pstmt.close();
        } catch ( Exception e ) {}
      }
      return sb.toString();
    }

//    static public String getLocks(OracleDatabaseConnection database) throws SQLException 
//    {
//      String sql = "SELECT ''Background'', l.sid, l.type, decode(l.lmode, 0, ''NONE'', 1, ''NULL'', 2, ''ROW SHARE'', 3, " +
//                   " ''ROW EXCLUSIVE'', 4, ''SHARE'', 5, ''SHARE ROW EXCLUSIVE'', 6, ''EXCLUSIVE'', ''?'') \"Mode\", " +
//                   "  decode( l.request, 0, ''NONE'', 1, ''NULL'', 2, ''ROW SHARE'', 3, ''ROW EXCLUSIVE'', 4, " +
//                   "  ''SHARE'', 5, ''SHARE ROW EXCLUSIVE'', 6, ''EXCLUSIVE'', ''?'') \"Request\", l.id1, l.id2, l.block " +
//                   "  from v$lock l order by l.sid, l.type";
//      return getGenericTabular(database, 
//    }


    static public String getJobDetail(OracleDatabaseConnection database, long jobId) throws SQLException {

      Hashtable links = new Hashtable();

      String sql = "select job \"Job Number\", " +
                   "       what \"Command\", " +
                   "       decode(l.sid, NULL, 'Idle', 'Running') \"Status\", " +
                   "       decode(l.sid,0,null,l.sid) \"Sid\" , " +  
                   "       decode(l.sid,null,'In ' || to_char(round((j.next_date-sysdate)*24*60,1)) || ' Minutes',to_char(round((sysdate - j.this_date)*24*60,1)) || ' Minutes Ago') \"Relative Start\", " +
                   "       interval \"Interval\", " +
                   "       next_date \"Next Run Date/Time\", " +
                   "       last_date \"Last Date Run\", " +
                   "       this_date \"This Date Run\", " +
                   "       broken \"Is It Broken\", " +
                   "       failures \"Failures\", " +
                   "       log_user \"Login User\", " +
                   "       priv_user \"Privileged User\", " +
                   "       schema_user \"Schema User\", " +
                   "       instance " +
                   "  from dba_jobs j, " +
                   "       (select id2 job_id, sid from v$lock where type = 'JQ') l " +
                   "  where l.job_id(+) = j.job " +
                   "    and j.job = ?"; 

      // 1st link
      Link link = new Link();
      link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_SESS_ZOOM + "&database=" + database.getName());
      link.setText("Zoom");
      link.setAlt("Show Details of this session.");
      ArrayList vars = new ArrayList();
      LinkVar lv = new LinkVar();
      lv.setIndex(4);
      lv.setVarName("sid");
      vars.add(lv);
      link.setVariables(vars);
      links.put(new Integer(4), link);
                   
      return getGenericDetail(database, sql, links, "Job Detail", jobId, 2);

    }
    
    static public String getCaptureDetail(OracleDatabaseConnection database, String captureName) throws SQLException {
        Hashtable links = new Hashtable();
        String sql = "select * from dba_capture where capture_name = ?";                      
        return getGenericDetail(database, sql, links, "Capture Detail", captureName, 4);
    }

    static public String getLongOpsDetail(OracleDatabaseConnection database, long sid) throws SQLException {
      String sql = "select * from v$session_longops where sid = ? and time_remaining>0";
      return getGenericDetail(database, sql, new Hashtable(), "Long Job Detail", sid, 2);
    }

    static public String getJobs(OracleDatabaseConnection database) throws SQLException {

        StringBuffer sb = new StringBuffer();
        PreparedStatement pstmt = null;
        ResultSet rset          = null;

        String SQL = "SELECT j.job \"Job Id\" ," + 
                     "  substr(j.what, 1, 60) \"Command\" , " +
                     "  decode(l.sid, null, ' ', 'Running') \"Status\", " +
                     "  l.instance_name \"Sid Inst\", " +
                     "  l.sid \"Sid\" , " +
                     "  to_char(j.next_date, 'MM/DD/YY HH24:MI:SS') \"Next Date\" , " +
                     "  to_char(j.last_date, 'MM/DD/YY HH24:MI:SS') \"Last Date\" , " +
                     "  decode(l.sid, null, 'In ' || to_char(round((j.next_date-sysdate)*24*60,1)) || ' Minutes', to_char(round((sysdate - j.this_date)*24*60,1)) || ' Minutes Ago') \"Relative Start\", " +
                     "  substr(interval, 1, 30) \"Interval\", " +
                     "  j.log_user \"Login User\" , " +
                     "  j.broken \"Broken\", " +
                     "  j.failures \"Failures\", " +
                     "  j.instance " + 
                     "    FROM (select i.instance_name,lo.id2 job_id, lo.sid from gv$lock lo, gv$instance i where lo.inst_id = i.inst_id and lo.type = 'JQ') l, " +
                     "         dba_jobs j " +
                     "    WHERE l.job_id(+) = j.job" +
                     "    ORDER BY j.next_date";

        try {

            pstmt = database.getConn().prepareStatement(SQL);
            rset  = pstmt.executeQuery();

            Hashtable links = new Hashtable();

            // 1st link
            Link link = new Link();
            link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_SESS_ZOOM + "&database=" + database.getName());
            link.setText("Zoom");
            link.setAlt("Show Details of this session.");
            ArrayList vars = new ArrayList();
            LinkVar lv = new LinkVar();
            lv.setIndex(5);
            lv.setVarName("sid");
            vars.add(lv);
            link.setVariables(vars);
            links.put(new Integer(5), link);

            // 2nd link
            link = new Link();
            link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_JOBS_DETAIL + "&database=" + database.getName());
            link.setText("Zoom");
            link.setAlt("Show Details of this Job.");
            vars = new ArrayList();
            lv = new LinkVar();
            lv.setIndex(1);
            lv.setVarName("key");
            vars.add(lv);
            link.setVariables(vars);
            links.put(new Integer(1), link);
            
            // 2nd link
            link = new Link();
            link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_OBJECT_SUMMARY + "&database=" + database.getName());
            link.setText("Zoom");
            link.setAlt("Show Objects.");
            vars = new ArrayList();
            lv = new LinkVar();
            lv.setIndex(10);
            lv.setVarName("username");
            vars.add(lv);
            link.setVariables(vars);
            links.put(new Integer(10), link);
        
            // Create the body of the report...
            sb.append(buildTabularDisplay(rset, links, "Job Queue", true, 90, false));

        } catch ( SQLException e ) {
            Tracer.log("Error in getting jobs from db", Tracer.ERROR, OBJECT_NAME);
            throw e;
        } finally {
            try {
                if ( pstmt != null ) pstmt.close();
            } catch ( Exception e ) {}
        }
        return sb.toString();
    }

    static public String getDatabase(OracleDatabaseConnection database) throws SQLException {

        StringBuffer sb = new StringBuffer();

        PreparedStatement pstmt = null;
        ResultSet rset          = null;
        String where            = "";

        String SQL = "select * from v$database";
        String SQL2 = "select * from v$option order by parameter";

        try {

            pstmt = database.getConn().prepareStatement(SQL);
            rset  = pstmt.executeQuery();

            sb.append(buildDetailDisplay(rset, new Hashtable(), "Database Summary", 4));

            sb.append("<hr>");

            pstmt = database.getConn().prepareStatement(SQL2);
            rset  = pstmt.executeQuery();

            sb.append(buildTabularDisplay(rset, new Hashtable(), "Installed Options", true, 40, false));

        } catch ( SQLException e ) {
            Tracer.log("Error in loading database info in load", Tracer.ERROR, OBJECT_NAME);
            throw e;
        } finally {
            try {
                if ( pstmt != null ) pstmt.close();
            } catch ( Exception e ) {}
        }
        return sb.toString();
    }
    
    static public Hashtable getDatabaseMetadata(OracleDatabaseConnection database) throws SQLException {
        
        // Get all metadata in one call to the database for performance.
        Hashtable metaData = new Hashtable();
        PreparedStatement pstmt = null;
        ResultSet rset          = null;
        
        String SQL = "select gn.global_name, " +
                     "       inst.version, " +
                     "       vs.value, " +
                     "       inst.instance_name, " +
                     "       compatible.value, " +
                     "       sysdate  " +
                     "  from v$instance inst, " +
        		         "       (select value from v$parameter where lower(name) = 'compatible') compatible," +
        		         "       global_name gn, " +
        		         "       (select banner value from v$version) vs";

        try {
            pstmt = database.getConn().prepareStatement(SQL);
            rset  = pstmt.executeQuery();
            rset.next();
            metaData.put("global_name",rset.getString(1));
            metaData.put("instance_version",new OracleVersion(rset.getString(2)));
            metaData.put("version_string", rset.getString(3));
            metaData.put("instance_name", rset.getString(4));
            metaData.put("compatible",new OracleVersion(rset.getString(5)));
            metaData.put("compatibility", rset.getString(5));
            metaData.put("sysdate", rset.getTimestamp(6));           
            return metaData;           
        } catch ( SQLException e ) {
            Tracer.log("Error in getting database version", Tracer.ERROR, OBJECT_NAME);
            throw e;
        } finally {
            try {
                if ( pstmt != null ) pstmt.close();
            } catch ( Exception e ) {}
        }
    }
    
    static public String getDmlHandlersHtml(OracleDatabaseConnection database) throws SQLException {

        StringBuffer sb = new StringBuffer();

        PreparedStatement pstmt = null;
        ResultSet rset          = null;

        String SQL = "select apply_name, object_owner || '.' || object_name \"Object Name\", " + 
                     " operation_name \"Operation\", replace(user_procedure,chr(34),'') \"Stored Procedure\", "  + 
                     "  error_handler \"Error Handler\"  " + 
                     "  from dba_apply_dml_handlers where user_procedure is not null";

        try {

            pstmt = database.getConn().prepareStatement(SQL);
            rset  = pstmt.executeQuery();

            Hashtable links = new Hashtable();

            // 1st link
            Link link = new Link();
            link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_SOURCE + "&database=" + database.getName());
            link.setText("Source");
            link.setAlt("Show Source.");
            ArrayList vars = new ArrayList();
            LinkVar lv = new LinkVar();
            lv.setIndex(3);                // get value for this variable from this pos in query
            lv.setVarName("dbobj");
            vars.add(lv);
            link.setVariables(vars);
            links.put(new Integer(3), link);         // Put the link on this position
            
            // Create the body of the report...
            sb.append(buildTabularDisplay(rset, links, null));

        } catch ( SQLException e ) {
            Tracer.log("Error in loading Partition in load", Tracer.ERROR, OBJECT_NAME);
            throw e;
        } finally {
            try {
                if ( pstmt != null ) pstmt.close();
            } catch ( Exception e ) {}
        }
        return sb.toString();
    }



    static public String buildSessionDetailConstant(long sid, OracleDatabaseConnection database) throws SQLException {

        SqlAddress sa = getSqlAddress(sid, database);

        StringBuffer sb = new StringBuffer();

        PreparedStatement pstmt = null;
        ResultSet rset          = null;

        try {

            String sql = null;

            sql = "select " +
                  " t2.username \"Oracle Username\", " +
                  " t2.program \"Client Program\", " +
                  " t2.sid \"Oracle SID\", " +
                  " t2.serial# \"Serial#\", " +
                  " t3.pid \"Oracle PID\", " +
                  " t3.spid \"Server OS PID\", " +
                  " t2.osuser \"OS User\", " +
                  " decode(t2.command, 0, ' ', 2,'Ins',3,'Sel',6,'Upd',45,'Rbk',7,'Del',47,'Pls',62,'Alz',vs.command) \"Cmd\", " +
                  " t2.resource_consumer_group \"Resource Group\", " +
                  " t2.process \"Client Program OS PID\", " +
                  " t2.status \"Status\", " +
                  " to_char(t2.logon_time,'Day mm/dd @ hh:mi:ssam') \"Logon Time\", " +              
                  " t3.program \"Server Program\", " +
                  " to_char(pga_used_mem,'999,999,999') \"PGA Used\", " +
                  " to_char(pga_alloc_mem,'999,999,999') \"PGA Allocated\", " +
                  " to_char(t3.pga_freeable_mem,'999,999,999') \"PGA Freeable\", " +
                  " to_char(t3.pga_max_mem,'999,999,999') \"PGA Max\", " + 
                  " latchwait \"Latch Wait\", " +
                  " t3.latchspin \"Latch Spin\" " +
                  "  from v$session t2, " +
                  "       v$process t3  " +
                  "  where t2.paddr = t3.addr " +
                  "    and t1.sid = ?";
                  
            pstmt = database.getConn().prepareStatement(sql);
            pstmt.setLong(1,sid);
            rset  = pstmt.executeQuery();

            Hashtable links = new Hashtable();

            sb.append(buildDetailDisplay(rset, links, "Session Data", 4));

        } catch ( SQLException e ) {
            Tracer.log(e,"Error in getting session io summary for sid " + sid, Tracer.ERROR, OBJECT_NAME);
            throw e;
        } finally {
            if ( pstmt != null )  pstmt.close();
        }

        return sb.toString();
    }
    
    static public String getSqlTextFromLC(SqlAddress sa, OracleDatabaseConnection database) throws SQLException {

      StringBuffer sqlbuf = new StringBuffer();
      PreparedStatement pstmt = null;
      ResultSet rset          = null;

      try {
      
        String sql = "select sql_text                " +
                     "  from v$sqltext_with_newlines " +
                     "  where hash_value = ?         " +
                     "  order by address, piece      ";
                     
        pstmt = database.getConn().prepareStatement(sql);
        pstmt.setString(1,sa.getHashValue());
        rset  = pstmt.executeQuery();
        while ( rset.next() ) {
          sqlbuf.append(rset.getString(1));
        }
        
        return sqlbuf.toString();
        
      } catch ( SQLException e ) {
        Tracer.log(e,"Error in getting SQL Text", Tracer.ERROR, OBJECT_NAME);
        throw e;
      } finally {
        if ( pstmt != null ) pstmt.close();
      }
      
    }

    static public String buildCurrentSql(String sql, long sid, OracleDatabaseConnection database, String heading,
                                         String hash, int windowHeight) throws SQLException {
          
        StringBuffer sb = new StringBuffer();
        
        sb.append("<table width=120 bgcolor='#ffffff' cellspacing=0 cellpadding=0>");
        sb.append("<tr>");
        sb.append("<th align=left bgcolor='#73969c'><font size=-1>&nbsp;" + heading + "&nbsp;</font><th>");
        if ( !hash.equals("0") ) {
          sb.append("&nbsp;");
          sb.append(HtmlComponent.getImageButtonHref("explain", "images/explain_it.jpg", 
                                                     "powerdba.jsp?formaction="+PowerDbaActions.EXPLAIN_PLAN+
                                                     "&database=" + database.getName()+"&sid="+sid+"&hash="+hash, ""));
        }
        sb.append("</tr>");
        sb.append("</table>");
        if ( sql.length() > 0 ) {
          sb.append("<table cellspacing=0 cellpadding=0 bgcolor='#ffffff'><tr>");
          sb.append("<tr>");
          sb.append("<td bgcolor='#e5e5e5'>");  
          sb.append("<table title='Explained SQL'><tr><td><font size=-1>");
          sb.append(HtmlComponent.getTextArea("sql", sql.toString(), 120, windowHeight) + "</font></td></tr></table>");
          sb.append("</td>");
        }
        sb.append("</tr>");
        sb.append("</table>");

        return sb.toString();
    }
    
    static public String buildSourceDisplay(String sql, long sid, OracleDatabaseConnection database, String heading,
                                         String hash) throws SQLException {
          
        StringBuffer sb = new StringBuffer();
        
        sb.append("<table width=150 bgcolor='#ffffff' cellspacing=0 cellpadding=0>");
        sb.append("<tr>");
        sb.append("<th align=left bgcolor='#73969c'><font size=-1>&nbsp;" + heading + "&nbsp;</font><th>");
        if ( !hash.equals("0") ) {
          sb.append(HtmlComponent.getExtraSmallButton("explain","powerdba.jsp?formaction="+PowerDbaActions.EXPLAIN_PLAN+
                                                      "&database=" + database.getName()+"&sid="+sid+"&hash="+hash,
                                                      ""));
        }
        sb.append("</tr>");
        sb.append("</table>");
        if ( sql.length() > 0 ) {
          sb.append("<table cellspacing=0 bgcolor='#ffffff' width=80%><tr>");
          sb.append("<tr>");

          sb.append("<td bgcolor='#e5e5e5'>");
  
          sb.append("<table title='Explained SQL'><tr><td><font size=-1>");
          sb.append(HtmlComponent.getTextArea("sql",sql.toString(),130,10) + "</font></td></tr></table>");
  
  
          //sb.append("<div><pre class=textBlock cols=100 width=100 WRAP>" + parseNoWeb(sql.toString()) + "</pre></div>");
          sb.append("</td>");
        }
        sb.append("</tr></table>");

        return sb.toString();
    }

    static public String getExplainOutput(long hashValue, OracleDatabaseConnection database) throws SQLException, WsnException {

        StringBuffer sb = new StringBuffer();
        StringBuffer sqlbuf = new StringBuffer();

        PreparedStatement pstmt = null;
        ResultSet rset          = null;

        try {
            String sql = "select sql_text from v$sqltext_with_newlines where hash_value = ? order by piece";
            pstmt = database.getConn().prepareStatement(sql);
            pstmt.setLong(1, hashValue);
            rset  = pstmt.executeQuery();
            while (rset.next()) {
              sqlbuf.append(rset.getString(1));
            }
        } catch ( SQLException e ) {
            String msg = "<br>Error generating Explain Plan output for " + hashValue;
            Tracer.log(e, msg, Tracer.ERROR, OBJECT_NAME);
            throw e;
        } finally {
            if ( pstmt != null ) {
                pstmt.close();
            }
        }

        return sqlbuf.toString();
    }
    
    static public String getExplainOutputFromSqlId(String sqlId, OracleDatabaseConnection database) throws SQLException, WsnException {

      StringBuffer sb = new StringBuffer();
      StringBuffer sqlbuf = new StringBuffer();

      PreparedStatement pstmt = null;
      ResultSet rset          = null;

      try {
          String sql = "select sql_text from v$sqltext_with_newlines where sql_id = ? order by piece";
          pstmt = database.getConn().prepareStatement(sql);
          pstmt.setString(1, sqlId);
          rset  = pstmt.executeQuery();
          while (rset.next()) {
            sqlbuf.append(rset.getString(1));
          }
      } catch ( SQLException e ) {
          String msg = "<br>Error generating Explain Plan output for " + sqlId;
          Tracer.log(e, msg, Tracer.ERROR, OBJECT_NAME);
          throw e;
      } finally {
          if ( pstmt != null ) {
              pstmt.close();
          }
      }

      return sqlbuf.toString();
  }
    
    static public ArrayList getStatList(OracleDatabaseConnection database) throws SQLException, WsnException {

	    ArrayList stats = new ArrayList();
	
	    PreparedStatement pstmt = null;
	    ResultSet rset          = null;
	
	    try {
	        String sql = "select statistic#, name  from v$statname " +
	                     " where (name like '%parse%' " +
	                     " or name like '%sort%' " +
	                     " or name like '%gets%' " +
	                     " or name like '%memory%' " +
	                     " or name like '%calls%' " +
	                     " or name like '%commits%' " +
	                     " or name like '%cursor%' " +
	                     " or name like '%pga%' " + 
	                     " or name = 'session logical reads') " + 
	                     " order by name";
	        
	        pstmt = database.getConn().prepareStatement(sql);
	        rset  = pstmt.executeQuery();
	        while ( rset.next() ) {
	          stats.add(new SelectEntry(rset.getString(1), rset.getString(2)));
	        }
	        
	        return stats;
	    } catch ( SQLException e ) {
	        Tracer.log(e,"Error getting statistics list from the database", Tracer.ERROR, OBJECT_NAME);
	        throw e;
	    } finally {
	        if ( pstmt != null ) {
	            pstmt.close();
	        }
	    }
    }
    
    static public String buildExplainOutputFromSqlId(String sqlId, String childNumber, OracleDatabaseConnection database) throws SQLException {

      StringBuffer sb         = new StringBuffer();
      PreparedStatement pstmt = null;
      ResultSet rset          = null;
      String oracleId = "0";

      try {
      	
      	String sqlToDisplay = null;
      	
        try {             
          pstmt = database.getConn().prepareStatement("select sql_text from v$sql where sql_id = ?");
          pstmt.setString(1,sqlId);
          rset  = pstmt.executeQuery();
          if (rset.next()) sqlToDisplay = rset.getString(1);
        } catch ( SQLException se ) {
          throw new SQLException("Error getting the sql text for the sql_id " + se.getMessage());  
        } finally {
          if ( pstmt != null ) pstmt.close();
        }  
	      
	      sb.append("<table width=150 bgcolor='#ffffff' cellspacing=0 cellpadding=0>");
	      sb.append("<tr>");
	      sb.append("<th align=left bgcolor='#73969c'><font size=-1>&nbsp;Query&nbsp;</font><th>");
	      sb.append("</tr>");
	      sb.append("</table>");
	      sb.append("<table title='Explained SQL' bgcolor=#f5f5f5><tr><td><font size=-1>");
	      sb.append(HtmlComponent.getTextArea("sqltoexplain",sqlToDisplay,100,20) + "</font></td></tr></table>");
	      sb.append(DIV);
      
        try {             
          pstmt = database.getConn().prepareStatement("select userenv('SESSIONID') from dual");
          rset  = pstmt.executeQuery();
          if (rset.next()) oracleId = rset.getString(1);
        } catch ( SQLException se ) {
          throw new SQLException("Error getting the session sid " + se.getMessage());  
        } finally {
          if ( pstmt != null ) pstmt.close();
        }   
        
        // Clean out the plan table
        PreparedStatement ps = null;
        try {
          ps = database.getConn().prepareStatement("delete from plan_table where statement_id = ?");
          ps.setString(1,oracleId);
          ps.execute();
          ps.close();
          database.getConn().commit();
        } catch ( SQLException se ) {
          throw new SQLException("Error deleting from plan_table " + se.getMessage());
        } finally {
          if ( ps != null ) ps.close();
        } 
        
        String msg = "";

                     
        // Get the output
        String sql = "select replace(plan_table_output,' ','&nbsp;') " +
                     "  from table(dbms_xplan.display_cursor(?,?,'all +PEEKED_BINDS +ALLSTATS +ALIAS'))";
        try {             
          pstmt = database.getConn().prepareStatement(sql);
          pstmt.setString(1, sqlId);
          pstmt.setString(2, childNumber);
          rset  = pstmt.executeQuery();
        } catch ( SQLException se ) {
          throw new SQLException("Error selecting from plan_table");  
        } 
        
        // Generate some HTML to print the results
        
        if ( msg.length() > 0 ) {
          sb.append("<table><tr><td><font size=-1>" + msg + "</font></td></tr></table>");
          sb.append(DIV);              
        }
        
        String line = null;
        String color1 = "#f5f5f5";
        String color2 = "#e5e5e5";
        String color = null;
        int rownum = 1;
        
        sb.append("<table width=150 bgcolor='#ffffff' cellspacing=0 cellpadding=0>");
        sb.append("<tr>");
        sb.append("<th align=left bgcolor='#73969c'><font size=-1>&nbsp;Explain Plan&nbsp;</font><th>");
        sb.append("</tr>");
        sb.append("</table>");
                    
        sb.append("<table title='Explain Plan'>");
        while ( rset.next() ) {
          if ( rownum%2 == 0 ) {
            color = color1;
          } else {
            color = color2;
          }
          line = rset.getString(1);
          if ( !line.substring(0,4).equals("----") ) {
            sb.append("<tr>");
            if ( line.indexOf("|") != 0 ) {
              sb.append("<td colspan=9 bgcolor='" + color + "'><font size='-2'>");
              sb.append(line);
              sb.append("</td>");
            } else {
              // Parse the line if it has a '|' character in it.
	            StringTokenizer st = new StringTokenizer(line, "|");
	            int colnum = 1;
	            String printValue = new String();
	            while ( st.hasMoreTokens() ) {

	              if ( colnum == 2 && rownum > 2 ) {
	                  
	                printValue = StringUtility.replace(st.nextToken(),"*"," ");
	                sb.append("<td bgcolor='"+color+"'><font size='-2'>" + printValue + "</font></td>");

	              } else if ( colnum == 3 && rownum > 2 ) {
	                  
	                String objectName = StringUtility.replace(st.nextToken(), "&nbsp;", "").trim();
	                sb.append("<td bgcolor='" + color + "'><font size='-2'>");
	                if ( objectName.equals("Name") ) {
		                sb.append("Object Information");	   
		                
	                } else if ( objectName != null && !objectName.trim().equals("") ) {
	                    
	                  //Tracer.log("************looking up name: " + objectName + " in database " + database, Tracer.DEBUG,"");
	                    
	                  OracleBaseObject obo = ProcessDAO.getOracleObject(objectName, database);
	                  sb.append("&nbsp;" + (obo.getFullNameWithType().equals(" ")?objectName:obo.getFullNameWithType())/* + "</a>"*/);
	                  sb.append("&nbsp;");
	                  sb.append("<a href='powerdba.jsp");
	                  sb.append("?database=" + database.getName() + "");
	                  sb.append("&formaction=" + PowerDbaActions.DB_OBJECT);
	                  sb.append("&oracleobjectname=" + objectName + "");
	                  sb.append("'>");
	                  sb.append("<img src='images/zoom.gif' border=0 vspace=0 width=10 height=10 alt='" +
                                     "Details" + "'></a>");

	                }
	                sb.append("</font></td>");
	              } else {
	  	            printValue = st.nextToken().trim();
	                sb.append("<td bgcolor='"+color+"'><font size='-2'>" + printValue + "</font></td>");
	              }
	              
	              colnum++;
	            }
            }
            sb.append("</tr>\n");
            rownum++;
          }
        }
        sb.append("</table>");
        
        //sb.append(buildCurrentSql(inSql, 0, database, conn, "Formatted Sql","0"));

    
        // Clean out the plan table
        try {
          ps = database.getConn().prepareStatement("delete from plan_table where statement_id = ?");
          ps.setString(1,oracleId);
          ps.execute();
          ps.close();
          database.getConn().commit();
        } catch ( SQLException se ) {
          throw new SQLException("Error deleting from plan_table " + se.getMessage());
        } finally {
          if ( ps != null ) ps.close();
        }

      } catch ( SQLException e ) {
        String msg = e.getMessage();
        Tracer.log(e,msg, Tracer.ERROR, OBJECT_NAME);
        throw new SQLException(msg);
      } finally {
        if ( pstmt != null ) pstmt.close();
      }

      if ( sb == null ) {
        sb.append("Unable to Explain Query...");
      }

      return sb.toString();
    }

    static public String buildExplainOutput(String inSql, OracleDatabaseConnection database) throws SQLException {

      StringBuffer sb         = new StringBuffer();
      PreparedStatement pstmt = null;
      ResultSet rset          = null;
      String oracleId = "0";

      try {
	      
	      sb.append("<table width=150 bgcolor='#ffffff' cellspacing=0 cellpadding=0>");
	      sb.append("<tr>");
	      sb.append("<th align=left bgcolor='#73969c'><font size=-1>&nbsp;Query&nbsp;</font><th>");
	      sb.append("</tr>");
	      sb.append("</table>");
	      sb.append("<table title='Explained SQL' bgcolor=#f5f5f5><tr><td><font size=-1>");
	      sb.append(HtmlComponent.getTextArea("sqltoexplain",inSql,100,20) + "</font></td></tr></table>");
	      sb.append(DIV);
      
        String sql = "select userenv('SESSIONID') from dual";
        try {             
          pstmt = database.getConn().prepareStatement(sql);
          rset  = pstmt.executeQuery();
          if (rset.next()) oracleId = rset.getString(1);
        } catch ( SQLException se ) {
          throw new SQLException("Error getting the session sid " + se.getMessage());  
        } finally {
          if ( pstmt != null ) pstmt.close();
        }   
        
        // Clean out the plan table
        PreparedStatement ps = null;
        try {
          ps = database.getConn().prepareStatement("delete from plan_table where statement_id = ?");
          ps.setString(1,oracleId);
          ps.execute();
          ps.close();
          database.getConn().commit();
        } catch ( SQLException se ) {
          throw new SQLException("Error deleting from plan_table " + se.getMessage());
        } finally {
          if ( ps != null ) ps.close();
        } 
        
        String msg = "";

        // Run explain plan
        try {
          ps = database.getConn().prepareStatement("explain plan set statement_id = '" + oracleId + "' for " + inSql);
          ps.execute();
          database.getConn().commit();
          ps.close();
        } catch ( SQLException se ) {
          msg = "There was a problem explaining this sql.<br>" + se.getMessage();
        } finally {
          if ( ps != null ) ps.close();
        }
                     
        // Get the output
        sql = "select replace(plan_table_output,' ','&nbsp;') " +
              "  from table(dbms_xplan.display(?,?))";
        try {             
          pstmt = database.getConn().prepareStatement(sql);
          pstmt.setString(1, "PLAN_TABLE");
          pstmt.setString(2, oracleId);
          rset  = pstmt.executeQuery();
        } catch ( SQLException se ) {
          throw new SQLException("Error selecting from plan_table");  
        } 
        
        // Generate some HTML to print the results
        
        if ( msg.length() > 0 ) {
          sb.append("<table><tr><td><font size=-1>" + msg + "</font></td></tr></table>");
          sb.append(DIV);              
        }
        
        String line = null;
        String color1 = "#f5f5f5";
        String color2 = "#e5e5e5";
        String color = null;
        int rownum = 1;
        
        sb.append("<table width=150 bgcolor='#ffffff' cellspacing=0 cellpadding=0>");
        sb.append("<tr>");
        sb.append("<th align=left bgcolor='#73969c'><font size=-1>&nbsp;Explain Plan&nbsp;</font><th>");
        sb.append("</tr>");
        sb.append("</table>");
                    
        sb.append("<table title='Explain Plan'>");
        while ( rset.next() ) {
          if ( rownum%2 == 0 ) {
            color = color1;
          } else {
            color = color2;
          }
          line = rset.getString(1);
          if ( !line.substring(0,4).equals("----") ) {
            sb.append("<tr>");
            if ( line.indexOf("|") != 0 ) {
              sb.append("<td colspan=9 bgcolor='" + color + "'><font size='-2'>");
              sb.append(line);
              sb.append("</td>");
            } else {
              // Parse the line if it has a '|' character in it.
	            StringTokenizer st = new StringTokenizer(line, "|");
	            int colnum = 1;
	            String printValue = new String();
	            while ( st.hasMoreTokens() ) {

	              if ( colnum == 2 && rownum > 2 ) {
	                  
	                printValue = StringUtility.replace(st.nextToken(),"*"," ");
	                sb.append("<td bgcolor='"+color+"'><font size='-2'>" + printValue + "</font></td>");

	              } else if ( colnum == 3 && rownum > 2 ) {
	                  
	                String objectName = StringUtility.replace(st.nextToken(), "&nbsp;", "").trim();
	                sb.append("<td bgcolor='" + color + "'><font size='-2'>");
	                if ( objectName.equals("Name") ) {
		                sb.append("Object Information");	   
		                
	                } else if ( objectName != null && !objectName.trim().equals("") ) {
	                    
	                  //Tracer.log("************looking up name: " + objectName + " in database " + database, Tracer.DEBUG,"");
	                    
	                  OracleBaseObject obo = ProcessDAO.getOracleObject(objectName, database);
	                  sb.append("&nbsp;" + (obo.getFullNameWithType().equals(" ")?objectName:obo.getFullNameWithType())/* + "</a>"*/);
	                  sb.append("&nbsp;");
	                  sb.append("<a href='powerdba.jsp");
	                  sb.append("?database=" + database.getName() + "");
	                  sb.append("&formaction=" + PowerDbaActions.DB_OBJECT);
	                  sb.append("&oracleobjectname=" + objectName + "");
	                  sb.append("'>");
	                  sb.append("<img src='images/zoom.gif' border=0 vspace=0 width=10 height=10 alt='" +
                                     "Details" + "'></a>");

	                }
	                sb.append("</font></td>");
	              } else {
	  	            printValue = st.nextToken().trim();
	                sb.append("<td bgcolor='"+color+"'><font size='-2'>" + printValue + "</font></td>");
	              }
	              
	              colnum++;
	            }
            }
            sb.append("</tr>\n");
            rownum++;
          }
        }
        sb.append("</table>");
        
        //sb.append(buildCurrentSql(inSql, 0, database, conn, "Formatted Sql","0"));

    
        // Clean out the plan table
        try {
          ps = database.getConn().prepareStatement("delete from plan_table where statement_id = ?");
          ps.setString(1,oracleId);
          ps.execute();
          ps.close();
          database.getConn().commit();
        } catch ( SQLException se ) {
          throw new SQLException("Error deleting from plan_table " + se.getMessage());
        } finally {
          if ( ps != null ) ps.close();
        }

      } catch ( SQLException e ) {
        String msg = e.getMessage();
        Tracer.log(e,msg, Tracer.ERROR, OBJECT_NAME);
        throw new SQLException(msg);
      } finally {
        if ( pstmt != null ) pstmt.close();
      }

      if ( sb == null ) {
        sb.append("Unable to Explain Query...");
      }

      return sb.toString();
    }

    static public String buildAllSql(long sid, OracleDatabaseConnection database) throws SQLException {

        StringBuffer sb = new StringBuffer();
        PreparedStatement pstmt = null;
        ResultSet rset          = null;

        try {
          String sql = "select " +
                       "       oc.sql_id, " +
                       "       oc.address \"Address\", " +
                       "       oc.hash_value \"Hash Value\", " +
                       "       oc.sql_text \"SQL\", " +
                       "       to_char(sum(s.executions),'999,999,999,999') execs, " +
                       "       to_char(sum(s.buffer_gets),'999,999,999,999') gets, " +
                       "       to_char(sum(s.parse_calls),'999,999,999,999') parses, " +
                       "       to_char(round(decode(s.executions,0,0,max(s.buffer_gets/decode(s.executions,0,.0000001,s.executions))),0),'999,999,999,999') \"Gets/Exec\", " +
                       "       'View SQL' \" \", " +
                       "       'Explain' \" \", " +
                       "       decode(s.hash_value, null, '', 'SQL Area Stats') \" \", " +
                       "       decode(sps.hash_value, null, '', 'Runtime Plan') \" \" " +
                       "  from v$open_cursor oc,          " +
                       "       v$sql_plan_statistics sps, " +
                       "       v$sql s " +
                       "  where oc.hash_value = sps.hash_value (+) " +
                       "    and oc.hash_value = s.hash_value (+) " +
                       "    and oc.address    = s.address (+) " +
                       "    and oc.sid = ? " +
                       "    and s.buffer_gets is not null " +
                       "  group by s.executions, s.buffer_gets, oc.user_name, oc.address, oc.hash_value, " +
                       "           oc.sql_text, s.hash_value, sps.hash_value, oc.sql_id " +
                       //"  order by nvl(decode(s.executions,0,0,max(s.buffer_gets/decode(s.executions,0,.0000001,s.executions))),0) desc, oc.sql_text";
                       "  order by gets desc, oc.sql_text";
          pstmt = database.getConn().prepareStatement(sql);
          pstmt.setLong(1,sid);
          rset  = pstmt.executeQuery();

          Hashtable links = new Hashtable();

          // 1st link
          Link link = new Link();
          link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_SQL_TEXT + "&database=" + database.getName() + 
                           "&sid=" + sid);
          link.setText("Source");
          link.setAlt("Show Source.");
          link.getVariables().add(new LinkVar(3, "hash"));
          links.put(new Integer(9), link); 

          link = new Link();
          link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.EXPLAIN_PLAN_FROM_MEMORY + "&database=" + database.getName() + 
                           "&sid=" + sid);
          link.setText("Explain");
          link.setAlt("Show Explain Plan.");
          link.getVariables().add(new LinkVar(1, "sqlid"));
          links.put(new Integer(10), link);               
          
          link = new Link();
          link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_SQL_CHILDREN + "&database=" + database.getName() +
                           "&sid=" + sid);
          link.setText("SQL Area");
          link.setAlt("Show SQL Area Stats.");
          link.getVariables().add(new LinkVar(2, "address"));
          link.getVariables().add(new LinkVar(3, "hash"));
          links.put(new Integer(11), link);            

          link = new Link();
          link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_SQL_PLAN_STATISTICS + "&database=" + database.getName());
          link.setText("SQL Plan");
          link.setAlt("Show Runtime Plan Stats.");
          link.getVariables().add(new LinkVar(2, "hash"));
          links.put(new Integer(11), link);     

          sb.append(buildTabularDisplay(rset, links, "Open Cursors"));
            
        } catch ( SQLException e ) {
          Tracer.log(e,"Error in getting Open Cursors for sid " + sid, Tracer.ERROR, OBJECT_NAME);
          throw e;
        } finally {
          if ( pstmt != null ) {
            pstmt.close();
          }
        }

        return sb.toString();
    }
    
    static public String getBadSql(OracleDatabaseConnection database, int howMany) throws SQLException {

      StringBuffer sb = new StringBuffer();
      PreparedStatement pstmt = null;
      ResultSet rset          = null;

      try {
      
        String sql = "select u.username \"User\", " +
                     "       sa.address \"Address\", " +
                     "       sa.hash_value \"Hash\", " +
                     "       substr(sa.sql_text,1,80) \"SQL\", " +
                     "       to_char(sum(s.rows_processed),'999,999,999,999') \"Rows Processed\", " +
                     "       to_char(round(decode(sum(s.rows_processed),0,0,max(s.buffer_gets/decode(s.rows_processed,0,.0000001,s.rows_processed))),0),'999,999,999,999') \"Buffer Gets<br>Per Row\", " +
                     "       to_char(sum(s.executions),'999,999,999,999') \"Execs\", " +
                     "       'SQL' \" \", " +
                     "       'Explain' \" \", " +
                     "       decode(s.hash_value, null, '', 'SQL Area') \" \", " +
                     "       decode(sps.hash_value, null, '', 'Runtime Plan') \" \" " +
                     "  from v$sqlarea sa,          " +
                     "       v$sql_plan_statistics sps, " +
                     "       v$sql s, " +
                     "       dba_users u " +
                     "  where sa.hash_value = sps.hash_value (+) " +
                     "    and sa.hash_value = s.hash_value (+) " +
                     "    and sa.address    = s.address (+) " +
                     "    and sa.parsing_user_id = u.user_id " +
                     "    and s.buffer_gets is not null " +
                     "    and nvl(decode(s.executions,0,0,s.buffer_gets/decode(s.executions,0,.0000001,s.executions)),0) > 20 " +
                     //"    and rownum < 21 " +
                     "  group by s.executions, s.buffer_gets, u.username, sa.address, sa.hash_value, " +
                     "           substr(sa.sql_text,1,80), s.hash_value, sps.hash_value, s.rows_processed " +
                     "  order by decode(sum(s.rows_processed),0,0,sum(s.buffer_gets)/decode(sum(s.rows_processed),0,.0000001,sum(s.rows_processed))) desc";

        pstmt = database.getConn().prepareStatement(sql);
        rset  = pstmt.executeQuery();

        Hashtable links = new Hashtable();

        Link link = new Link();
        link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_SQL_TEXT + "&database=" + database.getName());
        link.setText("Source");
        link.setAlt("Show Source.");
        link.getVariables().add(new LinkVar(3, "hash"));
        links.put(new Integer(8), link); 

        link = new Link();
        link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.EXPLAIN_PLAN + "&database=" + database.getName());
        link.setText("Explain");
        link.setAlt("Show Explain Plan.");
        link.getVariables().add(new LinkVar(3, "hash"));
        links.put(new Integer(9), link);               
        
        link = new Link();
        link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_SQL_CHILDREN + "&database=" + database.getName());
        link.setText("SQL Area");
        link.setAlt("Show SQL Area Stats.");
        link.getVariables().add(new LinkVar(2, "address"));
        link.getVariables().add(new LinkVar(3, "hash"));
        links.put(new Integer(10), link);            

        link = new Link();
        link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_SQL_PLAN_STATISTICS + "&database=" + database.getName());
        link.setText("SQL Plan");
        link.setAlt("Show Runtime Plan Stats.");
        link.getVariables().add(new LinkVar(3, "hash"));
        links.put(new Integer(11), link);     
        
        sb.append(buildTabularDisplay(rset, links, "Bad SQL", true, 100, false));
          
      } catch ( SQLException e ) {
        Tracer.log(e,"Error in getting Sqlarea bad queries", Tracer.ERROR, OBJECT_NAME);
        throw e;
      } finally {
        if ( pstmt != null ) pstmt.close();
      }

      return sb.toString();
    }
    
    static public String getMostExecutedSql(OracleDatabaseConnection database, int howMany) throws SQLException {

      StringBuffer sb = new StringBuffer();
      PreparedStatement pstmt = null;
      ResultSet rset          = null;

      try {
      
        String sql = "select u.username \"User\", " +
                     "       sa.address \"Address\", " +
                     "       sa.hash_value \"Hash\", " +
                     "       substr(sa.sql_text,1,80) \"SQL\", " +
                     "       to_char(round(decode(s.executions,0,0,max(s.buffer_gets/decode(s.executions,0,.0000001,s.executions))),0),'999,999,999,999') \"Avg Buffer<br>Gets\", " +
                     "       to_char(sum(s.executions),'999,999,999,999') \"Execs\", " +
                     "       to_char(sum(s.parse_calls),'999,999,999,999') \"Parses\", " +
                     "       'SQL' \" \", " +
                     "       'Explain' \" \", " +
                     "       decode(s.hash_value, null, '', 'SQL Area') \" \", " +
                     "       decode(sps.hash_value, null, '', 'Runtime Plan') \" \" " +
                     "  from v$sqlarea sa,          " +
                     "       v$sql_plan_statistics sps, " +
                     "       v$sql s, " +
                     "       dba_users u " +
                     "  where sa.hash_value = sps.hash_value (+) " +
                     "    and sa.hash_value = s.hash_value (+) " +
                     "    and sa.address    = s.address (+) " +
                     "    and sa.parsing_user_id = u.user_id " +
                     "    and s.buffer_gets is not null " +
                     "    and s.executions > 1000 and u.username not in ('SYS','SYSTEM')" +
                     "  group by s.executions, s.buffer_gets, u.username, sa.address, sa.hash_value, " +
                     "           substr(sa.sql_text,1,80), s.hash_value, sps.hash_value " +
                     "  order by sum(s.executions) desc";

        pstmt = database.getConn().prepareStatement(sql);
        rset  = pstmt.executeQuery();

        Hashtable links = new Hashtable();

        Link link = new Link();
        link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_SQL_TEXT + "&database=" + database.getName());
        link.setText("Source");
        link.setAlt("Show Source.");
        link.getVariables().add(new LinkVar(3, "hash"));
        links.put(new Integer(8), link); 

        link = new Link();
        link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.EXPLAIN_PLAN + "&database=" + database.getName());
        link.setText("Explain");
        link.setAlt("Show Explain Plan.");
        link.getVariables().add(new LinkVar(3, "hash"));
        links.put(new Integer(9), link);               
        
        link = new Link();
        link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_SQL_CHILDREN + "&database=" + database.getName());
        link.setText("SQL Area");
        link.setAlt("Show SQL Area Stats.");
        link.getVariables().add(new LinkVar(2, "address"));
        link.getVariables().add(new LinkVar(3, "hash"));
        links.put(new Integer(10), link);            

        link = new Link();
        link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_SQL_PLAN_STATISTICS + "&database=" + database.getName());
        link.setText("SQL Plan");
        link.setAlt("Show Runtime Plan Stats.");
        link.getVariables().add(new LinkVar(3, "hash"));
        links.put(new Integer(11), link);     
        
        sb.append(buildTabularDisplay(rset, links, "Most Exec'ed SQL", true, 100, false));
          
      } catch ( SQLException e ) {
        Tracer.log(e,"Error in getting Sqlarea heavy queries", Tracer.ERROR, OBJECT_NAME);
        throw e;
      } finally {
        if ( pstmt != null ) pstmt.close();
      }

      return sb.toString();
    }
    
    static public String getHeavySql(OracleDatabaseConnection database, int howMany) throws SQLException {

      StringBuffer sb = new StringBuffer();
      PreparedStatement pstmt = null;
      ResultSet rset          = null;

      try {
      
        String sql = "select u.username \"User\", " +
                     "       sa.address \"Address\", " +
                     "       sa.hash_value \"Hash\", " +
                     "       substr(sa.sql_text,1,80) \"SQL\", " +
                     "       to_char(round(decode(s.executions,0,0,max(s.buffer_gets/decode(s.executions,0,.0000001,s.executions))),0),'999,999,999,999') \"Avg Buffer<br>Gets\", " +
                     "       to_char(sum(s.executions),'999,999,999,999') \"Execs\", " +
                     "       to_char(sum(s.parse_calls),'999,999,999,999') \"Parses\", " +
                     "       'SQL' \" \", " +
                     "       'Explain' \" \", " +
                     "       decode(s.hash_value, null, '', 'SQL Area') \" \", " +
                     "       decode(sps.hash_value, null, '', 'Runtime Plan') \" \" " +
                     "  from v$sqlarea sa,          " +
                     "       v$sql_plan_statistics sps, " +
                     "       v$sql s, " +
                     "       dba_users u " +
                     "  where sa.hash_value = sps.hash_value (+) " +
                     "    and sa.hash_value = s.hash_value (+) " +
                     "    and sa.address    = s.address (+) " +
                     "    and sa.parsing_user_id = u.user_id " +
                     "    and s.buffer_gets is not null " +
                     "    and nvl(decode(s.executions,0,0,s.buffer_gets/decode(s.executions,0,.0000001,s.executions)),0) > 20 " +
                     //"    and rownum < 21 " +
                     "  group by s.executions, s.buffer_gets, u.username, sa.address, sa.hash_value, " +
                     "           substr(sa.sql_text,1,80), s.hash_value, sps.hash_value " +
                     "  order by decode(sum(s.executions),0,0,sum(s.buffer_gets)/decode(sum(s.executions),0,.0000001,sum(s.executions))) desc";

        pstmt = database.getConn().prepareStatement(sql);
        rset  = pstmt.executeQuery();

        Hashtable links = new Hashtable();

        Link link = new Link();
        link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_SQL_TEXT + "&database=" + database.getName());
        link.setText("Source");
        link.setAlt("Show Source.");
        link.getVariables().add(new LinkVar(3, "hash"));
        links.put(new Integer(8), link); 

        link = new Link();
        link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.EXPLAIN_PLAN + "&database=" + database.getName());
        link.setText("Explain");
        link.setAlt("Show Explain Plan.");
        link.getVariables().add(new LinkVar(3, "hash"));
        links.put(new Integer(9), link);               
        
        link = new Link();
        link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_SQL_CHILDREN + "&database=" + database.getName());
        link.setText("SQL Area");
        link.setAlt("Show SQL Area Stats.");
        link.getVariables().add(new LinkVar(2, "address"));
        link.getVariables().add(new LinkVar(3, "hash"));
        links.put(new Integer(10), link);            

        link = new Link();
        link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_SQL_PLAN_STATISTICS + "&database=" + database.getName());
        link.setText("SQL Plan");
        link.setAlt("Show Runtime Plan Stats.");
        link.getVariables().add(new LinkVar(3, "hash"));
        links.put(new Integer(11), link);     
        
        sb.append(buildTabularDisplay(rset, links, "Heaviest SQL", true, 100, false));
          
      } catch ( SQLException e ) {
        Tracer.log(e,"Error in getting Sqlarea heavy queries", Tracer.ERROR, OBJECT_NAME);
        throw e;
      } finally {
        if ( pstmt != null ) pstmt.close();
      }

      return sb.toString();
    }

    static public String buildSessionWaitSummary(long sid, OracleDatabaseConnection database) throws SQLException {

      StringBuffer sb = new StringBuffer();
      PreparedStatement pstmt = null;
      ResultSet rset          = null;

      try {
      
        String sql = "select event \"Event\",  total_waits \"Waits\", " +
                     "  round(time_waited/100,0) \"Time Waited (secs)\", " +
                     "  round(average_wait/100,0) \"Avg Wait (secs)\", round(max_wait/100,0) \"Max Wait (secs)\", wait_class " +
                     "  from v$session_event where sid = ? order by time_waited desc";
                     
        pstmt = database.getConn().prepareStatement(sql);
        pstmt.setLong(1,sid);
        rset  = pstmt.executeQuery();
        
        Hashtable links = new Hashtable();
        
        Link link = new Link();
        link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_WAIT_DETAIL + "&database=" + database.getName());
        link.setType("Wait Event Detail");
        link.setAlt("Show Details of this Wait Event.");
        link.getVariables().add(new LinkVar(1, "event"));
        links.put(new Integer(1), link);   

        sb.append(buildTabularDisplay(rset, links, "Session Wait Events"));

      } catch ( SQLException e ) {
        Tracer.log(e,"Error in getting session wait summary for sid " + sid, Tracer.ERROR, OBJECT_NAME);
        throw e;
      } finally {
        if ( pstmt != null ) {
          pstmt.close();
        }
      }

      return sb.toString();
    }
    
    static public String buildSessionWaitClassSummary(long sid, OracleDatabaseConnection database) throws SQLException {

        StringBuffer sb = new StringBuffer();
        PreparedStatement pstmt = null;
        ResultSet rset          = null;

        try {
        
          String sql = "select wait_class , total_waits \"Waits\", " +
                       "  round(time_waited/100,0) \"Time Waited (secs)\" " +
                       "  from v$session_wait_class where sid = ? order by time_waited desc";
                       
          pstmt = database.getConn().prepareStatement(sql);
          pstmt.setLong(1,sid);
          rset  = pstmt.executeQuery();

          sb.append(buildTabularDisplay(rset, "Session Wait Classes"));

        } catch ( SQLException e ) {
          Tracer.log(e,"Error in getting session wait class summary for sid " + sid, Tracer.ERROR, OBJECT_NAME);
          throw e;
        } finally {
          if ( pstmt != null ) {
            pstmt.close();
          }
        }

        return sb.toString();
    }

    static public String buildSessionIoSummary(long sid, OracleDatabaseConnection database) throws SQLException {

        StringBuffer sb = new StringBuffer();

        PreparedStatement pstmt = null;
        ResultSet rset          = null;

        try {
            String sql = "select to_char(block_gets,'9,999,999,999') block_gets, " +
            		         "       to_char(consistent_gets,'9,999,999,999') consistent_gets, " +
                         "       to_char(physical_reads,'9,999,999,999') physical_reads, " +
                         "       to_char(block_changes,'9,999,999,999') block_changes, " + 
                         "       to_char(consistent_changes,'9,999,999,999') consistent_changes, " +
                         "       to_char(round((1-((physical_reads)/decode(block_gets+consistent_gets,0,.0000001,block_gets+consistent_gets)))*100,2),'999.99') || '%' \"Cache Hit Ratio\" " +
                         "  from v$sess_io where sid = ?";
            pstmt = database.getConn().prepareStatement(sql);
            pstmt.setLong(1,sid);
            rset  = pstmt.executeQuery();

            sb.append(buildTabularDisplay(rset, new Hashtable(),"Session IO Summary"));

        } catch ( SQLException e ) {
            Tracer.log(e,"Error in getting session io summary for sid " + sid, Tracer.ERROR, OBJECT_NAME);
            throw e;
        } finally {
            if ( pstmt != null ) {
                pstmt.close();
            }
        }

        return sb.toString();
    }
    
    static public ArrayList getDateList(OracleDatabaseConnection database) throws SQLException {

      ArrayList list = new ArrayList();

      PreparedStatement pstmt = null;
      ResultSet rset          = null;

      try {
        for ( int i=-20; i<=0; i++ ) {
      
          String sql = "select to_char(trunc(sysdate)+(" + i + "),'dd-mon-yyyy') from dual";
          pstmt = database.getConn().prepareStatement(sql);
          rset  = pstmt.executeQuery();
          if ( rset.next() )
            list.add(new SelectEntry(rset.getString(1),rset.getString(1)));
        }

      } catch ( SQLException e ) {
        Tracer.log(e,"Error in getting list of recent dates", Tracer.ERROR, OBJECT_NAME);
        throw e;
      } finally {
        if ( pstmt != null ) {
            pstmt.close();
        }
      }

      return list;
    }
    
    
    static private boolean checkForSession(long sid, OracleDatabaseConnection database) throws SQLException {
    
      boolean rval = true;      
      PreparedStatement pstmt = null;
      ResultSet rset          = null;

      try {
          String sql = "select 1 from v$session where sid = ?";
          pstmt = database.getConn().prepareStatement(sql);
          pstmt.setLong(1, sid);
          rset  = pstmt.executeQuery();
          if ( !rset.next() ) rval = false;
      } catch ( SQLException e ) {
          Tracer.log(e,"Error checking for existance of session for " + sid, Tracer.ERROR, OBJECT_NAME);
          throw e;
      } finally {
          if ( pstmt != null ) pstmt.close();
      }
      
      return rval;
      
    }
    
    static private ArrayList getStatClasses(OracleDatabaseConnection database) throws SQLException {
    
      ArrayList rval = new ArrayList();
      
      PreparedStatement pstmt = null;
      ResultSet rset          = null;

      try {
        String sql = "select distinct class from v$statname";
        pstmt = database.getConn().prepareStatement(sql);
        rset  = pstmt.executeQuery();
        for ( int i=0; rset.next(); i++ ) {
          rval.add(new Integer(rset.getInt(1)));
        }
      } catch ( SQLException e ) {
        Tracer.log(e,"Error building list of stats classes", Tracer.ERROR, OBJECT_NAME);
        throw e;
      } finally {
        if ( pstmt != null ) pstmt.close();
      }
      
      return rval;
      
    }

    static public String buildSessionStats(long sid, OracleDatabaseConnection database) throws SQLException {
    
      StringBuffer sb = new StringBuffer();

      PreparedStatement pstmt = null;
      ResultSet rset          = null;

      try {
      
        String sql = "select class, name, value " +
                     "  from v$sesstat ss, " +
                     "       v$statname sn " +
                     "  where sn.statistic#=ss.statistic# " +
                     "    and sid = ? " +
                     "    and class > 0 " +
                     "    and (value > 0 or value < -100000) " +
                     "    order by class, name";
                     
        pstmt = database.getConn().prepareStatement(sql);
        pstmt.setLong(1,sid);
        rset  = pstmt.executeQuery();

        sb.append(buildTabularDisplay(rset, new Hashtable(),"Statistics", true, 40, true));

      } catch ( SQLException e ) {
        Tracer.log(e,"Error in getting session io summary for sid " + sid, Tracer.ERROR, OBJECT_NAME);
        throw e;
      } finally {
        if ( pstmt != null ) pstmt.close();
      }

      return sb.toString();
    }
    
    static public String getSessionLocksRequested(long sid, OracleDatabaseConnection database) throws SQLException {

      StringBuffer sb = new StringBuffer();

      PreparedStatement pstmt = null;
      ResultSet rset          = null;

      try {
      
        String sql = "select lock_type, mode_requested, lock_id1, lock_id2 " +
                     "  from dba_lock_internal where session_id = ? and mode_requested != 'Null' " +
                     "    and mode_requested != 'None' ";
                     
        pstmt = database.getConn().prepareStatement(sql);
        pstmt.setLong(1, sid);
        rset  = pstmt.executeQuery();
        
        sb.append(buildTabularDisplay(rset, new Hashtable(),"Locks Requested"));

      } catch ( SQLException e ) {
        Tracer.log(e,"Error in getting locks held sid " + sid, Tracer.ERROR, OBJECT_NAME);
        throw e;
      } finally {
        if ( pstmt != null )  pstmt.close();
      }

      return sb.toString();
    }
    
    static public String buildSessionTrx(long sid, OracleDatabaseConnection database) throws SQLException {

      StringBuffer sb = new StringBuffer();

      PreparedStatement pstmt = null;
      ResultSet rset          = null;

      try {
        String sql = "select start_time, log_io, phy_io, ubafil, ubablk, ubarec " +
                     "  from v$transaction where ses_addr = (select saddr from v$session where sid = ?)";
        pstmt = database.getConn().prepareStatement(sql);
        pstmt.setLong(1, sid);
        rset  = pstmt.executeQuery();

        sb.append(buildDetailDisplay(rset, new Hashtable(), "Current Transaction(s)", 6));

      } catch ( SQLException e ) {
        Tracer.log(e,"Error in getting trx info for sid " + sid, Tracer.ERROR, OBJECT_NAME);
        throw e;
      } finally {
        if ( pstmt != null ) {
            pstmt.close();
        }
      }

      return sb.toString();
    }
    
    static public String buildSessionPx(long sid, OracleDatabaseConnection database) throws SQLException {

        StringBuffer sb = new StringBuffer();

        PreparedStatement pstmt = null;
        ResultSet rset          = null;

        try {
          String sql = "select * from v$px_sesstat where sid = ?";
          pstmt = database.getConn().prepareStatement(sql);
          pstmt.setLong(1, sid);
          rset  = pstmt.executeQuery();

          sb.append(buildTabularDisplay(rset, "Parallel Query"));

        } catch ( SQLException e ) {
          Tracer.log(e,"Error in getting px info for sid " + sid, Tracer.ERROR, OBJECT_NAME);
          throw e;
        } finally {
          if ( pstmt != null ) {
              pstmt.close();
          }
        }

        return sb.toString();
      }
    
    static public String buildSessionLongops(long sid, OracleDatabaseConnection database) throws SQLException {

      StringBuffer sb = new StringBuffer();

      PreparedStatement pstmt = null;
      ResultSet rset          = null;

      try {
      
        String sql = "select sid \"Sid\", username \"User\", " +
                     //" to_char(start_time,'dd/mm/yyyy hh24:mi:ss') \"Started\",  " +
                     " to_char(round(elapsed_seconds/60,2),'999,999.99') \"Minutes Elapsed\", " +
                     " to_char(round(time_remaining/60,2),'999,999.99') \"Minutes Remaining\", " +
                     //" round( ( ( sysdate+(time_remaining/60/60/24) ) - start_time ) * 24 * 60, 2) \"Total Minutes\", " +
                     " to_char(sysdate + (time_remaining/60/60/24),'mm/dd/yyyy hh24:mi:ss') \"Finish Time\", " +
                     " to_char(round(sofar/totalwork*100,2),'999.99')||'%' \"Percent Complete\", message \"Message\" " +
                     "   from v$session_longops t1 " +
                     "   where time_remaining > 0 and sid = ? " +
                     "   order by start_time";
                     
        pstmt = database.getConn().prepareStatement(sql);
        pstmt.setLong(1, sid);
        rset  = pstmt.executeQuery();        
        
        sb.append(buildTabularDisplay(rset, new Hashtable(), "Longops"));

      } catch ( SQLException e ) {
        Tracer.log(e,"Error in getting longops info for sid " + sid, Tracer.ERROR, OBJECT_NAME);
        throw e;
      } finally {
        if ( pstmt != null )  pstmt.close();
      }

      return sb.toString();
    }
    
    static public Hashtable getPowerDBASessionStats(OracleDatabaseConnection database) throws SQLException {

      Hashtable hash = new Hashtable();
      PreparedStatement pstmt = null;
      ResultSet rset          = null;

      try {
      
        String sql = "select name, value from v$mystat m, v$statname s " +
                     "  where m.statistic# = s.statistic# " +
                     "    and name in ('consistent gets','CPU used by this session', " +
                     "                 'parse count (hard)', 'parse count (total)') " +
                     "  order by name";
                     
        hash.put("Elapsed Time", Long.toString(System.currentTimeMillis()));
        pstmt = database.getConn().prepareStatement(sql);
        rset  = pstmt.executeQuery();
        
        while ( rset.next() ) {
          hash.put(rset.getString(1), rset.getString(2));
        }
        
        return hash;

      } catch ( SQLException e ) {
        Tracer.log(e,"Error in getting this sessions stats", Tracer.ERROR, OBJECT_NAME);
        throw e;
      } finally {
        if ( pstmt != null ) pstmt.close();
      }
    }
    
    static public String getSessionLocksHeld(long sid, OracleDatabaseConnection database) throws SQLException {

        StringBuffer sb = new StringBuffer();

        PreparedStatement pstmt = null;
        ResultSet rset          = null;

        try {
            String sql = "select lock_type, mode_held, lock_id1, lock_id2 " +
                         "  from dba_lock_internal where session_id = ? and mode_held != 'Null' " +
                         "    and mode_held != 'None' ";
            pstmt = database.getConn().prepareStatement(sql);
            pstmt.setLong(1, sid);
            rset  = pstmt.executeQuery();
            
            sb.append(buildTabularDisplay(rset, new Hashtable(),"Locks Held (non-Null)"));

        } catch ( SQLException e ) {
            Tracer.log(e,"Error in getting locks held sid " + sid, Tracer.ERROR, OBJECT_NAME);
            throw e;
        } finally {
            if ( pstmt != null ) pstmt.close();
        }

        return sb.toString();
    }
    
    static public String getSessionConnectInfo(long sid, OracleDatabaseConnection database) throws SQLException {

        StringBuffer sb = new StringBuffer();

        PreparedStatement pstmt = null;
        ResultSet rset          = null;

        try {
            String sql = "select osuser, authentication_type, network_service_banner from v$session_connect_info where sid = ?";
            pstmt = database.getConn().prepareStatement(sql);
            pstmt.setLong(1, sid);
            rset  = pstmt.executeQuery();
            
            sb.append(buildTabularDisplay(rset, new Hashtable(),"Connect Info"));

        } catch ( SQLException e ) {
            Tracer.log(e,"Error in getting connect info for sid " + sid, Tracer.ERROR, OBJECT_NAME);
            throw e;
        } finally {
            if ( pstmt != null ) pstmt.close();
        }

        return sb.toString();
    }
    
    static public String getSessionAccess(long sid, OracleDatabaseConnection database) throws SQLException {

        StringBuffer sb = new StringBuffer();

        PreparedStatement pstmt = null;
        ResultSet rset          = null;

        try {
            String sql = "select type, owner, object from v$access where sid = ? order by " +
                         " decode(type,'TABLE','0','VIEW','1','PACKAGE','2', type), owner, object";
            pstmt = database.getConn().prepareStatement(sql);
            pstmt.setLong(1, sid);
            rset  = pstmt.executeQuery();
            
            Hashtable links = new Hashtable();
  
            Link link = new Link();
            link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_OBJECT_DETAIL + "&database=" + database.getName());
            link.setText("Zoom");
            link.setAlt("Show Details of this object.");
            link.getVariables().add(new LinkVar(1, "type"));
            link.getVariables().add(new LinkVar(2, "owner"));
            link.getVariables().add(new LinkVar(3, "objname"));
            links.put(new Integer(3), link); 
            
            sb.append(buildTabularDisplay(rset, links, "Objects Accessed"));

        } catch ( SQLException e ) {
            Tracer.log(e,"Error in getting object access info for sid " + sid, Tracer.ERROR, OBJECT_NAME);
            throw e;
        } finally {
            if ( pstmt != null ) pstmt.close();
        }

        return sb.toString();
    }
    
    static public String getInstanceMemorySummary(OracleDatabaseConnection database) throws SQLException {

      StringBuffer sb = new StringBuffer();
      PreparedStatement pstmt = null;
      ResultSet rset          = null;

      try {
        String sql = "select pool, to_char(sum(bytes),'999,999,999,999,999,999,999') bytes from v$sgastat group by pool";
        pstmt = database.getConn().prepareStatement(sql);
        rset  = pstmt.executeQuery();
        sb.append(buildTabularDisplay(rset, new Hashtable(),"Instance Memory Summary"));
      } catch ( SQLException e ) {
        Tracer.log(e,"Error getting Instance Memory Summary", Tracer.ERROR, OBJECT_NAME);
        throw e;
      } finally {
        if ( pstmt != null ) pstmt.close();
      }

      return sb.toString();
    }
    
    static public boolean sessionExists(long sid, OracleDatabaseConnection database) throws SQLException {

      boolean exists = true;
      PreparedStatement pstmt = null;
      ResultSet rset          = null;

      try {
        String sql = "select count(*) from v$session where sid = ?";
        pstmt = database.getConn().prepareStatement(sql);
        pstmt.setLong(1, sid);
        rset  = pstmt.executeQuery();
        rset.next();
        if ( rset.getLong(1) == 0 ) exists = false;
        return exists;
      } catch ( SQLException e ) {
        Tracer.log(e,"Error occurred checking for the existance of session id " + sid, Tracer.ERROR, OBJECT_NAME);
        throw e;
      } finally {
        if ( pstmt != null ) pstmt.close();
      }
    }
    
    static public String getInstanceMemoryDetails(OracleDatabaseConnection database) throws SQLException {

        StringBuffer sb = new StringBuffer();
        PreparedStatement pstmt = null;
        ResultSet rset          = null;

        try {
            String sql = "select pool, name, to_char(bytes,'999,999,999,999,999,999,999') bytes from v$sgastat";
            pstmt = database.getConn().prepareStatement(sql);
            rset  = pstmt.executeQuery();
            sb.append(buildTabularDisplay(rset, new Hashtable(),"Instance Memory Detail"));
        } catch ( SQLException e ) {
            Tracer.log(e,"Error getting Instance Memory Detail", Tracer.ERROR, OBJECT_NAME);
            throw e;
        } finally {
            if ( pstmt != null ) pstmt.close();
        }

        return sb.toString();
    }
    
    static public String getSqlPlanStatistics(long hash, long sid, OracleDatabaseConnection database) throws SQLException {

        StringBuffer sb = new StringBuffer();
        PreparedStatement pstmt = null;
        ResultSet rset          = null;
        String sql = null;
        
        try {
        
            SqlAddress sa = new SqlAddress("0",Long.toString(hash));                        
            String displaySql = ProcessDAO.getSqlTextFromLC(sa, database);
            sb.append(ProcessDAO.buildCurrentSql(displaySql, sid, database, "Sql",Long.toString(hash), 10));
            
            sb.append(DIV);
        
            sql = "select sps.child_number, " +
                  "  sps.executions, sps.starts, " +
                  "  sps.last_output_rows, " + 
                  "  sps.last_cr_buffer_gets, " +
                  "  round(sps.last_elapsed_time/1000000,2) last_elapsed_time, " +
                  "  lpad(sp.operation, depth + length(sp.operation), '..') || ' ' || sp.options \"Operation\" , " +
                  "  object_name " +
                  "  from v$sql_plan_statistics sps, " +
                  "       v$sql_plan sp " +
                  "  where sps.hash_value = sp.hash_value " +
                  "    and sps.address = sp.address " +
                  "    and sps.hash_value = ? " +
                  "    and sps.child_number = sp.child_number " +
                  "    and sps.operation_id = sp.id " +
                  "  order by sps.address, sps.child_number, sps.operation_id";
                  
            pstmt = database.getConn().prepareStatement(sql);
            pstmt.setLong(1, hash);
            rset  = pstmt.executeQuery();
            
            Hashtable links = new Hashtable();
  
            Link link = new Link();
            link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.DB_OBJECT + "&database=" + database.getName());
            link.setText("Zoom");
            link.setAlt("Show Details of this object.");
            link.getVariables().add(new LinkVar(8, "oracleobjectname"));
            links.put(new Integer(8), link);            
            
            sb.append(buildTabularDisplay(rset, links, "Runtime Plan"));
        } catch ( SQLException e ) {
            Tracer.log(e,"Error getting " + sql, Tracer.ERROR, OBJECT_NAME);
            throw e;
        } finally {
            if ( pstmt != null ) pstmt.close();
        }

        return sb.toString();
    }


    static public String buildSqlStats(SqlAddress sa, OracleDatabaseConnection database) throws SQLException {

      StringBuffer sb = new StringBuffer();

      PreparedStatement pstmt = null;
      ResultSet rset          = null;

      try {
        String sql = "select " +
                     "   'Explain' \" \", " +
                     "   sql_id, " +
                     "   child_number \"Child#\", " +
                     "   executions \"Execs\", " +
                     "   fetches, " +
                     "   rows_processed, " +
                     "   round(decode(executions,0,0,rows_processed/decode(executions,0,.0000001,executions)),2) \"Rows/Exec\", " +
                     "   to_char(buffer_gets,'999,999,999,999') buffer_gets, " +
                     "   to_char(round(decode(executions,0,0,buffer_gets/decode(executions,0,.0000001,executions)),2),'999,999,999.99') \"Gets/Exec\", " +
                     "   sorts, " +
                     "   round(decode(executions,0,0,sorts/decode(executions,0,.0000001,executions)),2) \"Sorts/Exec\",  " +
                     //"   first_load_time, " +
                     "   last_load_time, " +
                     "   round(cpu_time/1000000,2) \"CPU Secs\", " +
                     "   round(elapsed_time/1000000,2) \"Elapsed Secs\", " + 
                     "   round((elapsed_time/1000000)/decode(executions,0,.0000001,executions),5) \"Elapsed/Exec\", " + 
                     "   round((cpu_time/1000000)/decode(executions,0,.0000001,executions),5) \"CPU/Exec\", " + 
                     "   optimizer_cost, " +
                     "   parse_calls, " +
                     "   child_latch " +
                     "  from v$sql " +
                     "  where address = ? " +
                     "    and hash_value = ? " +
                     "  order by buffer_gets desc";
                     //"  order by round(decode(executions,0,0,buffer_gets/decode(executions,0,.0000001,executions)),2) desc";                     
        pstmt = database.getConn().prepareStatement(sql);
        pstmt.setString(1,sa.getAddress());
        pstmt.setString(2,sa.getHashValue());
        rset  = pstmt.executeQuery();
        
        Hashtable links = new Hashtable();
        
        Link link = new Link();
        link.setFileName("powerdba.jsp?formaction=" + PowerDbaActions.EXPLAIN_PLAN_FROM_MEMORY + "&database=" + database.getName());
        link.setText("Explain");
        link.setAlt("Explain this cursor.");
        link.getVariables().add(new LinkVar(2, "sqlid"));
        link.getVariables().add(new LinkVar(3, "childnumber"));
        links.put(new Integer(1), link); 

        sb.append(buildTabularDisplay(rset, links, "SQL Area Stats"));

      } catch ( SQLException e ) {
          Tracer.log(e,"Error in getting SQL stats", Tracer.ERROR, OBJECT_NAME);
          throw e;
      } finally {
          if ( pstmt != null ) pstmt.close();
      }

      return sb.toString();
    }

    static public SqlAddress getSqlAddress(long sid, OracleDatabaseConnection database) throws SQLException {

      PreparedStatement pstmt = null;
      ResultSet rset          = null;
      SqlAddress sa           = null;
      
      try {
      
        String sql = "select sql_address, sql_hash_value from v$session where sid=?";
        pstmt = database.getConn().prepareStatement(sql);
        pstmt.setLong(1,sid);
        rset  = pstmt.executeQuery();

        if ( rset.next() ) {
          sa = new SqlAddress();
          sa.setAddress(rset.getString(1));
          sa.setHashValue(rset.getString(2));
        }
        
        if ( pstmt != null ) pstmt.close();
        
        // Check for available sql via the primary hash value
        sql = "select count(*) from v$sqlarea where hash_value = " + sa.getHashValue();
        pstmt = database.getConn().prepareStatement(sql);
        rset  = pstmt.executeQuery();
        if ( !rset.next() ) {
          sa = new SqlAddress("0","0");
        }
        
        if ( sa.getHashValue().equals("0") ) {
          sa = ProcessDAO.getSqlAddress2(sid, database);
        }
        
      } catch ( SQLException e ) {
        Tracer.log(e,"Error in getting SQL address for sid " + sid, Tracer.ERROR, OBJECT_NAME);
        throw e;
      } finally {
        if ( pstmt != null ) pstmt.close();
      }

      return sa;
    }
    
    static private SqlAddress getSqlAddress2(long sid, OracleDatabaseConnection database) throws SQLException {

      PreparedStatement pstmt = null;
      ResultSet rset          = null;
      SqlAddress sa           = null;
      
      try {
      
        String sql = "select prev_sql_addr, prev_hash_value from v$session where sid=?";
        pstmt = database.getConn().prepareStatement(sql);
        pstmt.setLong(1,sid);
        rset  = pstmt.executeQuery();

        if ( rset.next() ) {
          sa = new SqlAddress();
          sa.setAddress(rset.getString(1));
          sa.setHashValue(rset.getString(2));
        }

      } catch ( SQLException e ) {
          Tracer.log(e,"Error in getting SQL address2 for sid " + sid, Tracer.ERROR, OBJECT_NAME);
          throw e;
      } finally {
          if ( pstmt != null ) pstmt.close();
      }

      return sa;
    }


    static public String buildSessionsHtml(String sql, Hashtable links, OracleDatabaseConnection database) throws SQLException {

      StringBuffer sb = new StringBuffer();

      PreparedStatement pstmt = null;
      ResultSet rset          = null;

      try {
        pstmt = database.getConn().prepareStatement(sql);
        rset  = pstmt.executeQuery();            
        sb.append(buildTabularDisplay(rset, links, null));

      } catch ( SQLException e ) {
        Tracer.log(e,"Error in getting IO session wait specific information", Tracer.ERROR, OBJECT_NAME);
        throw e;
      } finally {
        try {
            if ( pstmt != null ) pstmt.close();
        } catch ( Exception e ) {}
      }

      return sb.toString();

    }


    static public String buildFileHtml(String sql, long fileId, OracleDatabaseConnection database, ArrayList links) throws SQLException {

      StringBuffer sb = new StringBuffer();
      PreparedStatement pstmt = null;
      ResultSet rset          = null;

      try {
      
        pstmt = database.getConn().prepareStatement(sql);
        pstmt.setLong(1, fileId);
        rset  = pstmt.executeQuery();
        sb.append(buildDetailDisplay(rset, "File Details"));

      } catch ( SQLException e ) {
        Tracer.log(e,"Error in getting IO session wait specific information", Tracer.ERROR, OBJECT_NAME);
        throw e;
      }

      return sb.toString();
    }
    

    static private String buildWaitHtml(String sql, long sid, OracleDatabaseConnection database) throws SQLException {

      StringBuffer sb = new StringBuffer();
      PreparedStatement pstmt = null;
      ResultSet rset          = null;

      try {
        pstmt = database.getConn().prepareStatement(sql);
        pstmt.setLong(1, sid);
        rset  = pstmt.executeQuery();
        sb.append(buildDetailDisplay(rset, "Event Specific Wait Information"));
      } catch ( SQLException e ) {
        Tracer.log(e,"Error in getting IO session wait specific information", Tracer.ERROR, OBJECT_NAME);
        throw e;
      } finally {
        if ( pstmt != null ) pstmt.close();
      }

      return sb.toString();

    }

    static private String buildTsHtml(String sql, String ts, Hashtable links, OracleDatabaseConnection database) throws SQLException {

      StringBuffer sb = new StringBuffer();

      PreparedStatement pstmt = null;
      ResultSet rset          = null;

      try {
        pstmt = database.getConn().prepareStatement(sql);
        pstmt.setString(1, ts);
        pstmt.setString(2, ts);
        rset  = pstmt.executeQuery();
        sb.append(buildTabularDisplay(rset, links, "Tablespace Map"));

      } catch ( SQLException e ) {
        Tracer.log(e,"Error in getting tsmap specific information", Tracer.ERROR, OBJECT_NAME);
        throw e;
      } finally {
        if ( pstmt != null ) {
            pstmt.close();
        }
      }

      return sb.toString();

    }
    
    static public ArrayList getSchemaLetterList(OracleDatabaseConnection db) throws SQLException {

      ArrayList list = new ArrayList();
      PreparedStatement pstmt = null;
      ResultSet rset          = null;
      
      String SQL = "select distinct 'firstletter' ltype, t.first_letter, t.first_letter from " +
                   " (select substr(username,1,1) first_letter from dba_users " +
                   "  union all " +
                   "  select 'P' first_letter from dual) t ";

      try {
        pstmt = db.getConn().prepareStatement(SQL);
        rset  = pstmt.executeQuery();
        for ( short s=1; rset.next(); s++ ) {
          list.add(new SelectEntry(rset.getString(1), rset.getString(1), s));
        }
         
        
        return list;

      } catch ( SQLException e ) {
        Tracer.log(e,"Error in getting list of current users from database", Tracer.ERROR, OBJECT_NAME);
        throw e;
      } finally {
        if ( pstmt != null ) pstmt.close();
      }

    }
      
    static public Hashtable getSessionLists(OracleDatabaseConnection database) throws SQLException {

        Hashtable lists = new Hashtable();
        ArrayList users = new ArrayList();
        ArrayList statuses = new ArrayList();
        ArrayList modules = new ArrayList();
        ArrayList machines = new ArrayList();
        ArrayList statistics = new ArrayList();
        
        PreparedStatement pstmt = null;
        ResultSet rset          = null;
        
        String sql = 
			     "select distinct 'user' ltype, username lvalue, username ldisplay from gv$session " +
			     "  where username is not null and (module != 'Power*DBA' or module is null)" +
			     "union all " +
			     "select distinct 'status' ltype, status lvalue, status ldisplay from gv$session " +
			     "  where username is not null and (module != 'Power*DBA' or module is null) " +
			     "union all " +
			     "select distinct 'module' ltype, nvl(module,'NA') lvalue, module ldisplay from gv$session " +
			     "  where username is not null and (module != 'Power*DBA' or module is null) and module is not null " +
			     "union all " +
			     "select distinct 'machine' ltype, machine lvalue, machine ldisplay from gv$session " +
			     "  where username is not null and (module != 'Power*DBA' or module is null) " +
			     "union all " +
			     "select 'statistic', to_char(statistic#) lvalue, lower(name) ldisplay  from v$statname " +
			     " where (name like '%parse%' or name like '%sort%' " +
			     " or name like '%gets%' or name like '%mamory%' or name='user calls' " +
			     " or name ='recursive calls' or name like '%commits%' or name like '%cursor%' " +
			     " or name like '%pga%' or name='session logical reads') and name not like '%cleanout%' order by 1,3";
        
        try {
            
          pstmt = database.getConn().prepareStatement(sql);
          rset  = pstmt.executeQuery();         
          
          for ( short s=1; rset.next(); s++ ) {
	          String type = rset.getString(1);
	          
	          if ( type.startsWith("user") ) {
	             users.add(new SelectEntry(rset.getString(2), rset.getString(3), s));
	          } else if ( type.startsWith("status") ) {
	             statuses.add(new SelectEntry(rset.getString(2), rset.getString(3), s));
	          } else if ( type.startsWith("module") ) {
	             modules.add(new SelectEntry(rset.getString(2), rset.getString(3), s));
	          } else if ( type.startsWith("machine") ) {
	             machines.add(new SelectEntry(rset.getString(2), rset.getString(3), s));
	          } else if ( type.startsWith("statistic") ) {
		           statistics.add(new SelectEntry(rset.getString(2), rset.getString(3), s));
	          }
              
          }
          statuses.add(new SelectEntry("ANY","ANY",(short) 1000));
          
          lists.put("user", users);
          lists.put("status", statuses);
          lists.put("module", modules);
          lists.put("machine", machines);
          lists.put("statistic", statistics);
          
          Tracer.log("users is size " + users.size(), Tracer.DEBUG, OBJECT_NAME);
          
          return lists;

        } catch ( SQLException e ) {
          Tracer.log(e,"Error in getting session screen lists from oracle", Tracer.ERROR, OBJECT_NAME);
          throw e;
        } finally {
          if ( pstmt != null ) pstmt.close();
        }

      }
    
    static public ArrayList getSessionTypeList(OracleDatabaseConnection database) throws SQLException, Exception {

      ArrayList list = new ArrayList();
      PreparedStatement pstmt = null;
      ResultSet rset          = null;
      
      String sql = "select distinct type from v$session " +
                   "  where type is not null order by type desc";

      try {
        pstmt = database.getConn().prepareStatement(sql);
        rset  = pstmt.executeQuery();
        for ( short s=1; rset.next(); s++ ) {
          list.add(new SelectEntry(rset.getString(1), rset.getString(1), s));
        }
        
        list.add(new SelectEntry("%","ANY",(short) 0));
        
        Tracer.log("Returning session type List of Size: " + list.size(), Tracer.DEBUG, OBJECT_NAME);
        return list;

      } catch ( SQLException e ) {
        Tracer.log(e,"Error in getting list of session types from database", Tracer.ERROR, OBJECT_NAME);
        throw e;
      } finally {
        if ( pstmt != null ) pstmt.close();
      }

    }
    
    static public String getStatId(OracleDatabaseConnection database, String statname) throws SQLException {
    
      if ( database == null || database.getConn() == null ) return "41";

      String rval = null;
      PreparedStatement pstmt = null;
      ResultSet rset          = null;
      
      String sql = "select statistic# from v$statname where name = ?";

      try {
      
        pstmt = database.getConn().prepareStatement(sql);
        pstmt.setString(1, statname);
        rset  = pstmt.executeQuery();
        if ( rset.next() )  {
          rval = rset.getString(1);
        } else {
          throw new SQLException("Default Statistic " + statname + " was not found in v$statname");
        }

        return rval;

      } catch ( SQLException e ) {
        Tracer.log(e,"Error in getting stat number from database", Tracer.ERROR, OBJECT_NAME);
        throw e;
      } finally {
        if ( pstmt != null ) pstmt.close();
      }

    }

    static public String buildDetailDisplay(ResultSet rset, String title) throws SQLException {
        return buildDetailDisplay(rset, new Hashtable(), title, 2);
    }
    
    static public String buildDetailDisplay(ResultSet rset, String title, int columns) throws SQLException {
        return buildDetailDisplay(rset, new Hashtable(), title, columns);
    }

    static public String buildDetailDisplay(ResultSet rset, Hashtable links, String title, int columns) throws SQLException {

      StringBuffer sb = new StringBuffer();
      
      Tracer.log("buildDetailDisplay: columns=" + columns, Tracer.DEBUG, "");
      
      // Determine if there are any rows in the resultset
      //boolean rows = true;       
      //if ( !rset.next() ) {
      //  rows = false;
      //} else {
        //rset.previous();
      //}

      try {

        ResultSetMetaData rmd = rset.getMetaData();
        int cc = rmd.getColumnCount();
        String labelColor = "#73969c"; //"#f7f7e7";
        String dataColor = "#e5e5e5";

        String dt = DateTranslator.getStringDateTime(new Date(System.currentTimeMillis()));

        if ( title != null ) {
          sb.append("<table width=150 bgcolor='" + labelColor + "'>");
          sb.append("<tr>");
          sb.append("<td align=left bgcolor='" + labelColor + "'><b><font  size=-1 color=black>" + title + "&nbsp;</font></td>");
          sb.append("</tr></table>");
        }
        
        //if ( rows ) {
        
          sb.append("<table cellspacing=1 border=0 cellpadding=1 bgcolor='#f7f7e7'>\n");
          String value = null;
  
          int i = 0;
  
          while ( rset.next() ) {
  
            for ( i=1; i<=cc; i++ ) {
            
              if ( i-1 % columns == 0 || i == 1 ) {
                sb.append("<tr>\n");
              }
              sb.append("<th bgcolor='" + labelColor + "' align='right'><img src='images/clear.gif' width=5><font size=-1 color='black'>" +
                        PageLoader.getColumnHeading(rmd.getColumnName(i)) + "&nbsp;</font></th>\n");

              // Try to catch the error where oracle jdbc is returning larger than 2g precision
              // for clob, blob, etc.  
              int precision = 0;
              try {
                precision = rmd.getPrecision(i);
              } catch ( NumberFormatException e ) {
                precision = 1000000;
              }
              
              value = ObjectTranslator.getString(rmd.getColumnClassName(i), precision, rset.getObject(i));
  
              Link link = (Link) links.get(new Integer(i));
  
              if ( link == null ) {
                if ( value == null  || value.equals("null") ) value="&nbsp;";
                sb.append("<td bgColor=" + dataColor + "><font style='verdana' size='-1' color=#111111>&nbsp;" + value + "&nbsp;</font></td>\n");
              } else /* It's a link */ {
                if ( value == null  || value.equals("null") ) {
                  sb.append("<td bgColor=" + dataColor + ">&nbsp;");
                } else {
                  sb.append("<td bgColor=" + dataColor + "><a href='" + link.getFileName());
                  for ( int j=0; j<link.getVariables().size(); j++ ) {
                    LinkVar var = (LinkVar) link.getVariables().get(j);
                    sb.append("&" + var.getVarName() + "=" + rset.getString(var.getIndex()) );
                  }
                  sb.append("'><font style='verdana' size=-1><b>&nbsp;" + value + "&nbsp;</b></font></a></td>\n");
                }
              }
              
              if ( i % columns != 0 ) {
                sb.append("<td bgcolor='white'><img src='images/clear.gif' width=4></td>");
              }
              
              if ( i % columns == 0 ) {
                sb.append("</tr>\n");
              }
  
            }
            
            if ( i % columns == 0 ) {
              sb.append("<td bgcolor='#e5e5e5'>&nbsp;</td>");
              sb.append("<td bgcolor='" + dataColor + "'>&nbsp;</td></tr>"); 
            }
  
          }
          sb.append("</table>");
        //} else {
        //  sb.append("<table><tr><th align='left'><font size=-1 color=#444444>&nbsp;No Rows Returned...</font></th></tr></table>\n");
        //}

      } catch ( SQLException se ) {
        Tracer.log(se, "Error building the detail display", Tracer.ERROR, OBJECT_NAME);
        throw se;
      }

      return sb.toString();
    }
    
    static public String buildWaitDetailDisplay(ResultSet rset, 
                                                Hashtable links, 
                                                String title, int columns, 
                                                String groupName,
                                                OracleDatabaseConnection database) throws SQLException, Exception {

      StringBuffer sb = new StringBuffer();
      
      Tracer.log("buildWaitDetailDisplay: columns=" + columns, Tracer.DEBUG, "");

      try {

        ResultSetMetaData rmd = rset.getMetaData();
        int cc = rmd.getColumnCount();
        String labelColor = "#73969c"; //"#f7f7e7";
        String dataColor = "#e5e5e5";
        String column1Value = null;

        String dt = DateTranslator.getStringDateTime(new Date(System.currentTimeMillis()));

        if ( title != null ) {
          sb.append("<table width=800 bgcolor='" + labelColor + "'>");
          sb.append("<tr>");
          sb.append("<td align=left bgcolor='" + labelColor + "'><b><font  size=-1 color=black>" + title + "&nbsp;</font></td>");
          sb.append("</tr></table>");
        }
        
        sb.append("<table cellspacing=1 border=0 cellpadding=1 bgcolor='#f7f7e7'>\n");
        String value = null;

        int i = 0;

        while ( rset.next() ) {
        
          column1Value = rset.getString(1);

          for ( i=1; i<=cc; i++ ) {
          
            if ( i-1 % columns == 0 || i == 1 ) {
              sb.append("<tr>\n");
            }
            sb.append("<th bgcolor='" + labelColor + "' align='right'><img src='images/clear.gif' width=5><font size=-1 color='black'>" +
                      PageLoader.getColumnHeading(rmd.getColumnName(i)) + ":&nbsp;</font></th>\n");
            value = rset.getString(i);

            Link link = (Link) links.get(new Integer(i));

            if ( link == null ) {
              if ( value == null  || value.equals("null") ) value="&nbsp;";
              sb.append("<td bgColor=" + dataColor + "><font style='verdana' size='-1' color=#111111>&nbsp;" + value + "&nbsp;</font></td>\n");
            } else /* It's a link */ {
              if ( value == null  || value.equals("null") ) {
                sb.append("<td bgColor=" + dataColor + ">&nbsp;");
              } else {
                sb.append("<td bgColor=" + dataColor + "><a href='" + link.getFileName());
                for ( int j=0; j<link.getVariables().size(); j++ ) {
                  LinkVar var = (LinkVar) link.getVariables().get(j);
                  sb.append("&" + var.getVarName() + "=" + rset.getString(var.getIndex()) );
                }
                sb.append("'><font style='verdana' size=-1><b>&nbsp;" + value + "&nbsp;</b></font></a></td>\n");
              }
            }
            
            if ( i % columns != 0 ) {
              sb.append("<td bgcolor='white'><img src='images/clear.gif' width=0></td>");
            }
            
            if ( i % columns == 0 ) {
              sb.append("</tr>\n");
            }

          }
          
          if ( i % columns == 0 ) {
            sb.append("<td bgcolor='#e5e5e5'>&nbsp;</td>");
            sb.append("<td bgcolor='" + dataColor + "'>&nbsp;</td></tr>"); 
          }

        }
        sb.append("</table>");
        
        // Get some extra info about enqueues
        if ( groupName.equals("ENQ") ) {
          sb.append("<BR>");
          sb.append(XmlDAO.getEnqueueDetail(column1Value).toHtml()); 
        }
        if ( groupName.equals("LATCH") ) {
          sb.append("<BR>");
          sb.append(ProcessDAO.getChildLatches(database, column1Value));
        }
              
      } catch ( SQLException se ) {
        Tracer.log(se, "Error building the detail display", Tracer.ERROR, OBJECT_NAME);
        throw se;
      } catch ( Exception e ) {
        Tracer.log(e, "Error building the detail display", Tracer.ERROR, OBJECT_NAME);
        throw e;
      }

      return sb.toString();
    }
    
    static public String buildTabularDisplay(ResultSet rset, String title) throws SQLException {
        return buildTabularDisplay(rset, new Hashtable(), title, true,   90, false);
    }
    
    static public String buildTabularDisplay(ResultSet rset, Hashtable links, String title) throws SQLException {
      return buildTabularDisplay(rset, links, title, true,   90, false);
    }
    
    static public String buildTabularDisplay(ResultSet rset, Hashtable links, String title, boolean border) throws SQLException {
      return buildTabularDisplay(rset, links, title, border, 90, false);
    }

    static public String buildTabularDisplay(ResultSet rset, Hashtable links, String title, boolean border, int width, boolean statistics) throws SQLException {

      StringBuffer sb = new StringBuffer();
      String tableWidthHtml = ""; 

      try {
        ResultSetMetaData rmd = rset.getMetaData();
        int cc = rmd.getColumnCount();

        // Build the title tab
        if ( title != null ) {
          sb.append("\n<table width=150 cellspacing=0 cellpadding=0>");
          sb.append("  <tr>");
          sb.append("    <th align=left bgcolor='#73969c'>");
          sb.append("      <font size=-1 color=black>&nbsp;" + title + "&nbsp;</font>");
          sb.append("    </th>");
          //sb.append("    <td align=left>");
          //sb.append("      <img src='images/corner.gif'  height=30 width=30 border=0>");
          //sb.append("    </td>");
          sb.append("  </tr>\n");
          sb.append("</table>\n");
        }
        
        // Build width
        if ( width > 0 ) tableWidthHtml = "width="+width+"%";

        // Is there a border to the table?
        if ( border ) {
          sb.append("<left><table border=0 cellspacing=1 cellpadding=2 bgcolor='white' " + tableWidthHtml + ">\n");             
        } else {
          sb.append("<left><table border=0 cellspacing=0 cellpadding=2 bgcolor='white' " + tableWidthHtml + ">\n");
        }
        
        // Determine if there are any rows in the resultset
        //boolean rows = true;       
        //if ( !rset.next() ) {
        //  rows = false;
        //} else {
          //rset.previous();
        //}
        
        // Print table Headings
        //if ( rows ) {
          sb.append("<tr>");
          for ( int i=1; i<=cc; i++ ) {
            sb.append("<th scope=\"col\" BGCOLOR=#73969c><font size='-1' color=black>&nbsp;" + 
                      PageLoader.getColumnHeading(rmd.getColumnLabel(i)) + "&nbsp;</font></th>\n");
          }
          sb.append("</tr>\n");
        //} else {
        //  sb.append("<tr><th align='left'><font size=-1 color=#444444>&nbsp;&nbsp;&nbsp;&nbsp;No Rows Returned...</font></th></tr>\n");
        //}

        // Print the Table Cells Detail
        String value = null;
        String valueFromQuery = null;
        int cntr = 1;
        String color = null;
        
        // rows
        while ( rset.next() ) {
        
            if (cntr % 2 == 0) {
              color = "#e5e5e5";
            } else {
              color = "#f5f5f5";
            }
            sb.append("<tr>\n");
            
            // columns
            for ( int i=1; i<=cc; i++ ) {

              if ( rmd.getColumnClassName(i).equals("oracle.sql.TIMESTAMP") ) {
                value = rset.getTimestamp(i).toString();
              } else {
                value = ObjectTranslator.getString(rmd.getColumnClassName(i),2,rset.getObject(i));
              }
              Link link = (Link) links.get(new Integer(i));

              if ( link == null ) {
              
                  if ( value == null  || value.equals("null") ) value="&nbsp;";
                  
                  if ( statistics ) {
                    switch (i) {
                      case 1:
                        String originalValue = value;
                        value = originalValue + "-" + ((String) new OracleStatistic().getStatClasses().get(new Integer(value)));
                        if ( value == null ) value = originalValue; 
                        break;
                        
                      case 3:
                        // Get the stat definition from the xml
                        OracleStatistic os = OracleStatHolder.getStatDef(rset.getString(2));
                        if ( os == null ) {
                          Tracer.log("STAT NOT IN XML!!!! " + rset.getString(2), Tracer.WARNING, "");
                          os = new OracleStatistic(rset.getString(2), Long.parseLong(value), 99999, null, 1, ""); 
                        }
                        // Set the current value of it.
                        if ( value != null ) {
                          os.setValue(Long.parseLong(value));
                        } else {
                          os.setValue(0);
                        }
                        value = os.getAdjustedValue();
                        break;
                    }
                  }

                  sb.append("<td bgColor=" + color + "><font style='verdana' size='-2' color=#111111>&nbsp;" + 
                            value + "&nbsp;</font</td>");

                } else /* It's a link */ {
                    if ( value == null  || value.equals("null") ) {
                      sb.append("<td bgColor=" + color + ">");
                    } else {
                      if ( link.getType() == "Popup" ) {
                        sb.append("<td bgColor=" + color + ">");
                        sb.append("<a class='little' href=\"\" onMouseOver=\"window.status='" + link.getText() + "';");
                        sb.append(" return true;\" onClick=\"" + link.getJsFunction() + "(");
                        for ( int j=0; j<link.getVariables().size(); j++ ) {
                            LinkVar var = (LinkVar) link.getVariables().get(j);
                            sb.append("'"+ rset.getString(var.getIndex())+"'");
                            if ( j < link.getVariables().size() ) {
                              sb.append(",");
                            }
                        }
                        sb.setLength(sb.length()-1); // Lop off the last comma.
                        sb.append("); return false;\" class=\"smallentry\"><font size=-2><b>&nbsp;" + value);
                        sb.append(" &nbsp;</b></font></a>");
                        
                      } else {
                        sb.append("<td bgColor=" + color + "><a class='little' href='" + link.getFileName());
                        for ( int j=0; j<link.getVariables().size(); j++ ) {
                            LinkVar var = (LinkVar) link.getVariables().get(j);
                            sb.append("&" + var.getVarName() + "=" + rset.getString(var.getIndex()) );
                        }
                        sb.append("'><font style='verdana' size=-2><b>&nbsp;" + value + "&nbsp;</b></font></a>");
                      }
                  }
                }
                sb.append("</td>");
            }
            sb.append("</tr>\n");
          cntr++;
          }
          sb.append("</table>");

      } catch (SQLException se) {
          Tracer.log(se, "Error building the tabular display", Tracer.ERROR, OBJECT_NAME);
          throw se;
      }

      return sb.toString();
    }
    
    private static String parseSQL(String sql) {
    
      String rsql = sql;
      
      if ( sql.substring(0,sql.length()-2).indexOf('\n') == 0 ) { 
      
        rsql = StringUtility.replace(rsql, " from ", "<br>&nbsp;&nbsp;from&nbsp;");
        rsql = StringUtility.replace(rsql, " where ", "<br>&nbsp;&nbsp;where&nbsp;");
        rsql = StringUtility.replace(rsql, " and ", "<br>&nbsp;&nbsp;&nbsp;&nbsp;and&nbsp;");
        rsql = StringUtility.replace(rsql, " FROM ", "<br>&nbsp;&nbsp;FROM&nbsp;");
        rsql = StringUtility.replace(rsql, " WHERE ", "<br>&nbsp;&nbsp;WHERE&nbsp;");
        rsql = StringUtility.replace(rsql, " AND ", "<br>&nbsp;&nbsp;&nbsp;&nbsp;AND&nbsp;");
        rsql = StringUtility.replace(rsql, " GROUP BY ", "<br>&nbsp;&nbsp;GROUP BY&nbsp;");
        rsql = StringUtility.replace(rsql, " group by ", "<br>&nbsp;&nbsp;&nbsp;&nbsp;group by&nbsp;");
        //rsql = StringUtility.replace(rsql, ",",",<br>");
      }
      
      return rsql;
    }
       
    private static String parseNoWeb(String sql) {
    
      String rsql = sql;

      if ( sql.substring(0,sql.length()-2).indexOf('\n') == 0 ) {      

        rsql = StringUtility.replace(rsql, " from ", "\n&nbsp;&nbsp;from&nbsp;");
        rsql = StringUtility.replace(rsql, " where ", "\n&nbsp;&nbsp;where&nbsp;");
        rsql = StringUtility.replace(rsql, " and ", "\n&nbsp;&nbsp;&nbsp;&nbsp;and&nbsp;");
        rsql = StringUtility.replace(rsql, " FROM ", "\n&nbsp;&nbsp;FROM&nbsp;");
        rsql = StringUtility.replace(rsql, " WHERE ", "\n&nbsp;&nbsp;WHERE&nbsp;");
        rsql = StringUtility.replace(rsql, " AND ", "\n&nbsp;&nbsp;&nbsp;&nbsp;AND&nbsp;");
        rsql = StringUtility.replace(rsql, " GROUP BY ", "\n&nbsp;&nbsp;GROUP BY&nbsp;");
        rsql = StringUtility.replace(rsql, " group by ", "\n&nbsp;&nbsp;&nbsp;&nbsp;group by&nbsp;");
        //rsql = StringUtility.replace(rsql, ",",",\n");
      }

      
      return rsql;
    }
    
    private static String escape(String sql) {
      return sql;
      //return StringUtility.replace(sql, "'","\\'");
    }
    
    private static boolean isQuoted(String token) {return false;}
    
    public static void testConnection(DbConfig dc) throws SQLException {

      Connection conn = null;
      
      try {
        DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        conn = DriverManager.getConnection(dc.getConnectString(), dc.getLogin(), dc.getPassword());
      } catch ( SQLException e ) {
        throw e;
      } finally {
        try {
          if ( conn != null ) conn.close();
        } catch ( SQLException se) {}
      }
    
    }
    
    public static void execStreamsTrxOverride(OracleDatabaseConnection database, String trxId) throws SQLException {
        
        StringBuffer sb = new StringBuffer();

        CallableStatement cstmt = null;
        ResultSet rset          = null;

        try {
            cstmt = database.getConn().prepareCall("call strm_admin.execute_transaction('" + trxId + "')");
            cstmt.execute();
        } catch ( SQLException e ) {
            Tracer.log(e, "Error executing Streams apply error transaction " + trxId + " with CR override.", Tracer.ERROR, OBJECT_NAME);
            throw e;
        } finally {
            try {
                if ( cstmt != null ) cstmt.close();
            } catch ( Exception e ) {}
        }
       
    }
    
    public static void deleteStreamsTrx(OracleDatabaseConnection database, String trxId) throws SQLException {
        
        StringBuffer sb = new StringBuffer();

        CallableStatement cstmt = null;
        ResultSet rset          = null;

        try {
            cstmt = database.getConn().prepareCall("call dbms_apply_adm.delete_error('" + trxId + "')");
            cstmt.execute();
        } catch ( SQLException e ) {
            Tracer.log(e, "Error deleting Streams apply transaction from error queue " + trxId, Tracer.ERROR, OBJECT_NAME);
            throw e;
        } finally {
            try {
                if ( cstmt != null ) cstmt.close();
            } catch ( Exception e ) {}
        }
       
    }
    
    public static void execStreamsTrx(OracleDatabaseConnection database, String trxId) throws SQLException {
        
        StringBuffer sb = new StringBuffer();

        CallableStatement cstmt = null;
        ResultSet rset          = null;

        try {
            cstmt = database.getConn().prepareCall("call dbms_apply_adm.execute_error('" + trxId + "')");
            cstmt.execute();
        } catch ( SQLException e ) {
            Tracer.log(e, "Error executing Streams apply transaction " + trxId, Tracer.ERROR, OBJECT_NAME);
            throw e;
        } finally {
            try {
                if ( cstmt != null ) cstmt.close();
            } catch ( Exception e ) {}
        }
       
    }
    
    public static void startCapture(OracleDatabaseConnection database, String capture) throws SQLException {

        CallableStatement cstmt = null;

        try {
            cstmt = database.getConn().prepareCall("call dbms_capture_adm.start_capture('" + capture + "')");
            cstmt.execute();
        } catch ( SQLException e ) {
            Tracer.log(e, "Error starting capture process " + capture, Tracer.ERROR, OBJECT_NAME);
            throw e;
        } finally {
            try {
                if ( cstmt != null ) cstmt.close();
            } catch ( Exception e ) {}
        }
       
    }
    
    
    public static void stopCapture(OracleDatabaseConnection database, String capture) throws SQLException {

        CallableStatement cstmt = null;
        String cmd              = "call dbms_capture_adm.stop_capture('" + capture + "')";

        try {
            cstmt = database.getConn().prepareCall(cmd);
            cstmt.execute();
        } catch ( SQLException e ) {
            Tracer.log(e, "Error stopping capture process " + capture + " (" + cmd + ")", Tracer.ERROR, OBJECT_NAME);
            throw e;
        } finally {
            try {
              if ( cstmt != null ) cstmt.close();
            } catch ( Exception e ) {}
        }
       
    }
    
    public static void stopTrace(OracleDatabaseConnection database, long sid) throws SQLException {

        CallableStatement cstmt = null;

        try {
            cstmt = database.getConn().prepareCall("call dbms_capture_adm.start_capture('" + sid + "')");
            cstmt.execute();
        } catch ( SQLException e ) {
            Tracer.log(e, "Error stopping tracing for " + sid, Tracer.ERROR, OBJECT_NAME);
            throw e;
        } finally {
            try {
                if ( cstmt != null ) cstmt.close();
            } catch ( Exception e ) {}
        }
       
    }
    
    public static void startTrace(OracleDatabaseConnection database, long sid) throws SQLException {

        CallableStatement cstmt = null;

        try {
            cstmt = database.getConn().prepareCall("call dbms_capture_adm.start_capture('" + sid + "')");
            cstmt.execute();
        } catch ( SQLException e ) {
            Tracer.log(e, "Error starting tracing for " + sid, Tracer.ERROR, OBJECT_NAME);
            throw e;
        } finally {
            try {
                if ( cstmt != null ) cstmt.close();
            } catch ( Exception e ) {}
        }
       
    }
    
    public static void breakJob(OracleDatabaseConnection database, int jobId) throws SQLException {

        CallableStatement cstmt = null;
        String cmd              = "call dbms_job.broken(?,?)";

        try {
            cstmt = database.getConn().prepareCall(cmd);
            cstmt.setBoolean(jobId, false);
            cstmt.execute();
            database.getConn().commit();
        } catch ( SQLException e ) {
            Tracer.log(e, "Error breaking job " + jobId + " (" + cmd + ")", Tracer.ERROR, OBJECT_NAME);
            throw e;
        } finally {
            try {
              if ( cstmt != null ) cstmt.close();
            } catch ( Exception e ) {}
        } 
    }
    
    public static void unbreakJob(OracleDatabaseConnection database, int jobId) throws SQLException {

        CallableStatement cstmt = null;
        String cmd              = "call dbms_job.broken(" + jobId + ",false)";

        try {
            cstmt = database.getConn().prepareCall(cmd);
            cstmt.execute();
            database.getConn().commit();
        } catch ( SQLException e ) {
            Tracer.log(e, "Error unbreaking job " + jobId + " (" + cmd + ")", Tracer.ERROR, OBJECT_NAME);
            throw e;
        } finally {
            try {
              if ( cstmt != null ) cstmt.close();
            } catch ( Exception e ) {}
        }
    }

}

