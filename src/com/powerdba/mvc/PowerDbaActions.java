package com.powerdba.mvc;
import java.util.Enumeration;
import java.util.Hashtable;

public class PowerDbaActions {

  private static Hashtable pages = null; 
  private static Hashtable reversePages = null;

  public PowerDbaActions() {}
  
  public static final int NEW                        = 1;
  public static final int SAVE                       = 2;
  public static final int TEST_CONNECTION            = 3;
  public static final int DELETE                     = 4;
  public static final int CLONE                      = 5;
  public static final int REINIT_POOLS               = 6;
    
  public static final int NEXT_PAGE                  = 10;
  public static final int PREVIOUS_PAGE              = 11;
  public static final int FIRST_PAGE                 = 12;
  public static final int LAST_PAGE                  = 13;
  
  // Sessions
  public static final int DB_SESSIONS                = 1101;
  public static final int DB_LONGOPS_DETAIL          = 1102;
  public static final int DB_SESS_ZOOM               = 1103;    
  public static final int DB_LONGOPS                 = 1104;
  public static final int DB_LONGOPS_SID             = 1105;    
  public static final int DB_SQL_TEXT                = 1106;
  public static final int DB_WAIT_DETAIL             = 1107;    
  public static final int DB_SESSION_LOCKS_HELD      = 1108;
  public static final int DB_SESSION_LOCKS_REQUESTED = 1109;   
  public static final int DB_SQL_PLAN_STATISTICS     = 1110;
  public static final int DB_SESSION_CONNECT_INFO    = 1111;
  public static final int DB_SESSION_ACCESS          = 1112;
  public static final int DB_SQL_CHILDREN            = 1113;
  public static final int LC_OPEN_CURSORS            = 1114;
  public static final int EXPLAIN_PLAN               = 1115;
  public static final int DB_BLOCKERS                = 1116;
  public static final int DB_BG_SESSIONS             = 1117;    
  public static final int DB_SESSION_CHART           = 1118;
  public static final int DB_GLOBAL_SERVICES         = 1119;
  public static final int DB_SESSION_STATS           = 1120;
  public static final int DB_SESSIONS2               = 1121;
  public static final int DB_PQSLAVES                = 1122;
  public static final int DB_PQSTATS                 = 1123;
  public static final int DB_PQSESSION               = 1124;
  public static final int DB_PQTQ                    = 1125;
  public static final int DB_JOBS_WL                 = 1126;
  public static final int DB_POWERDBA_SESSIONS       = 1127;
  public static final int DB_LOCKS                   = 1128;
  public static final int DB_SCHJOBS_WL              = 1129;
  public static final int DB_MEMORY                  = 1130;
  public static final int PGA_DETAILS                = 1131;
  public static final int DB_INSTANCE_MEMORY_SUMMARY = 1132;
  public static final int DB_INSTANCE_MEMORY_DETAIL  = 1133;
  public static final int DB_TRANSACTIONS            = 1134;
  public static final int DB_CARFAX_WEBLOGIC_SUMMARY = 1135;
  public static final int DB_SCHJOBS_HIST            = 1136;
  public static final int SESSIONS_SQL               = 1137;
  public static final int EXPLAIN_PLAN_FROM_MEMORY   = 1138;
  

  
  // Redo Log
  public static final int DB_REDO_LOG                = 1201;
  public static final int DB_REDO_LOGFILE            = 1202;
  public static final int DB_LOG_HISTORY             = 1203;
  public static final int DB_LOG_HISTORY_MINUTE      = 1204;
  public static final int DB_LOG_HISTORY_SECOND      = 1205;
  public static final int DB_LOG_SUMMARY             = 1206;
  
  // Instance Summary
  public static final int DB_INSTANCE                = 1301;
  public static final int DB_PARAMETERS              = 1302;

  public static final int DB_INITORA                 = 1305;
  public static final int DB_HIDDEN_PARMS            = 1306;
  
  // Library Cache
  public static final int LC_SUMMARY                 = 1501;
  public static final int LC_BAD_SQL                 = 1502;
  public static final int LC_PARSING_SUMMARY         = 1503;
  public static final int LC_PARSING_PARMS           = 1504;
  public static final int LC_HEAVY_SQL               = 1505;
  public static final int LC_MOSTEXEC_SQL            = 1506;
  public static final int LC_ELAPSED_SQL             = 1507;
  

  // Latches
  public static final int DB_LATCH_PARENT            = 1601;
  public static final int DB_LATCH_CHILDREN          = 1602;
  
  // Buffer Cache
  public static final int BUFFER_CACHE               = 1701;
  public static final int BUFFER_CACHE_SEG           = 1702;
  
  // System Stats
  public static final int DB_SYSSTAT                 = 1901;
  public static final int DB_SYSRATIOS               = 1902;
  public static final int DB_SYSTEM_EVENTS           = 1903;
  
  // General Database
  public static final int DB_FREESPACE               = 2001;
  public static final int DB_TS                      = 2002;
  public static final int DB_FILE                    = 2003;
  public static final int DB_SEGMENT                 = 2004;
  public static final int DB_TABLE                   = 2005;
  public static final int DB_TABLESPACES             = 2006;
  public static final int DB_FILES                   = 2007;
  public static final int DB_JOBS                    = 2008;
  public static final int DB_DATABASE                = 2009;
  public static final int DB_JOBS_DETAIL             = 2010;
  public static final int DB_DIRECTORIES             = 2011;
  public static final int DB_FREESPACE_PROBLEMS      = 2012;

  
  // Schema
  public static final int DB_SCHEMAS                 = 2211;
  public static final int DB_OBJECT                  = 2213;
  public static final int DB_INDEX_PERFVIEW          = 2214;
  public static final int DB_TABLE_PERFVIEW          = 2215;
  public static final int DB_ROLES_ASSIGNED          = 2216;
  public static final int DB_ROLE_ROLES              = 2217;
  public static final int DB_ROLE_OBJECTS            = 2218;
  public static final int DB_SYSTEM_PRIVS            = 2219;
  public static final int DB_DIRECT_GRANTS           = 2220;
  public static final int DB_OBJECT_SUMMARY          = 2221;
  public static final int DB_OBJECT_LIST             = 2222;
  public static final int DB_OBJECT_DETAIL           = 2223; 
  public static final int DB_SYS_PRIVS               = 2224;
  public static final int DB_MVIEW_LOG_DEP           = 2225;
  public static final int DB_MVIEWS                  = 2226;
  public static final int DB_MVIEW_LOGS              = 2227;
  public static final int DB_SCHEMAS_W_OBJECTS       = 2228;
  public static final int DB_SCHEMAS_AF              = 2229;
  public static final int DB_SCHEMAS_GM              = 2230;
  public static final int DB_SCHEMAS_NS              = 2231;
  public static final int DB_SCHEMAS_TZ              = 2232;

  public static final int DB_STREAMS                 = 6101;
  public static final int DB_STREAMS_ERROR_DETAIL    = 6102;
  public static final int DB_STREAMS_RULE_SETS       = 6103;
  public static final int DB_SOURCE                  = 6104;
  public static final int DB_STREAMS_DML_HANDLERS    = 6105;
  public static final int DB_STREAMS_APPLY           = 6106;
  public static final int DB_STREAMS_CAPTURE         = 6107;
  public static final int DB_STREAMS_PROPAGATION     = 6108;
  public static final int DB_STREAMS_RULE_SETS_RN    = 6109;
  public static final int DB_STREAMS_ERROR_QUEUE     = 6110;
  public static final int DB_STREAMS_QUEUES          = 6111;
  public static final int DB_STREAMS_SUMMARY         = 6112;
  public static final int DB_STREAMS_TRX             = 6113;
  public static final int DB_CAPTURE_DETAIL          = 6114;
  public static final int DB_REGISTERED_LOGS         = 6115;
  public static final int DB_QUEUE_STATES            = 6116;
  public static final int DB_STREAMS_BG              = 6117;
  public static final int DB_STREAMS_TRANS           = 6118;
  public static final int DB_STREAMS_SHREDDERS       = 6119;
  public static final int DB_INSTANTIATED_TABLES     = 6120;
  
  
  // Recovery Area
  public static final int DB_RECOVERY_SIZE           = 2601; 
  
  public static final int DB_AQ_QUEUE_TABLES         = 6201;
  public static final int DB_AQ_PROPAGATIONS         = 6202;
  public static final int DB_AQ_QUEUE_ROUTING        = 6203;
  public static final int DB_AQ_QUEUEMAPS            = 6206;
  public static final int DB_AQ_ENQ_ERRORS           = 6207;
  public static final int DB_AQ_QUEUE_MASTER         = 6208;
  public static final int Q_MESSAGES_EXT             = 6209;
  
  public static final int DB_AQ_DEQ_ERRORS           = 6308;
  public static final int DB_AQ_DEQ_ERROR_XML        = 6310;
  public static final int DB_AQ_MSG_HANDLER_STATS    = 6312;
  public static final int DB_AQ_DEST_MESSAGES        = 6313;
  public static final int DB_AQ_SHREDDERS            = 6305;
  public static final int DB_AQ_MSG_HANDLERS         = 6306;
  public static final int DB_AQ_HANDLER_ERRSUMM     = 6307;
  
  public static final int DB_MESSAGE_LATENCY         = 6001;
  
  
  public static final int DB_ARCHIVING               = 117;

  public static final int PERFSTAT                   = 5001;
  public static final int PERFSTAT2                  = 5002;
  public static final int PERFSTAT_DETAIL            = 5003;
  public static final int PERFSTAT_DETAIL2           = 5004;
  public static final int OWR_CHART                  = 5005;
  
  public static final int EDIT_RULE                  = 10001;
  public static final int EDIT_RULE_SUBMIT           = 10002;
  
  public static final int LOGOFF                     = 10003;
  public static final int ADMIN_LOGIN                = 10004;
  
  public static final int CHARTS_SUMMARY             = 4001;
  public static final int CHARTS_IO                  = 4002;
  public static final int CHARTS_SESSIONS            = 4003;
  public static final int CHARTS_CONTENTION          = 4004;
  public static final int CHARTS_ACTIVITY            = 4005;
  public static final int CHARTS_CLUSTER             = 4006;
  public static final int CHARTS_METRICS             = 4007;
  public static final int CHARTS_CPUIO               = 4008;
  public static final int CHARTS_CPU                 = 4009;
  public static final int CHARTS_SHREDDER_THROUGHPUT      = 4010;
  public static final int CHARTS_MESSAGE_RATE             = 4011;
  public static final int CHARTS_ACTIVE_SESSIONS          = 4012;
  public static final int CHARTS_ACTIVE_SESSIONSBYSERVICE = 4013;
  
  
  public static final int STREAMS_MANAGE_TRX_EXEC    = 20001;
  
  public static final int METRICS7_IO_WAITS          = 30001;
  public static final int METRICS7_LOGICAL_READS     = 30002;
  


  
  private static void loadPageDefinitions(){ 
  
    pages = new Hashtable();
  
    pages.put("NEW",new Integer(NEW));
    pages.put("SAVE",new Integer(SAVE));
    pages.put("TEST_CONNECTION",new Integer(TEST_CONNECTION));
    pages.put("DELETE",new Integer(DELETE));
    pages.put("CLONE",new Integer(CLONE));
    
    pages.put("NEXT_PAGE",new Integer(NEXT_PAGE));
    pages.put("PREVIOUS_PAGE",new Integer(PREVIOUS_PAGE));
    pages.put("FIRST_PAGE",new Integer(FIRST_PAGE));
    pages.put("LAST_PAGE",new Integer(LAST_PAGE));
    
    pages.put("DB_AQ_QUEUE_TABLES",new Integer(DB_AQ_QUEUE_TABLES));
    pages.put("DB_AQ_PROPAGATIONS",new Integer(DB_AQ_PROPAGATIONS));
    pages.put("DB_AQ_QUEUE_ROUTING",new Integer(DB_AQ_QUEUE_ROUTING));
    pages.put("DB_AQ_MSG_HANDLERS",new Integer(DB_AQ_MSG_HANDLERS));
    pages.put("DB_AQ_MSG_HANDLER_STATS",new Integer(DB_AQ_MSG_HANDLER_STATS));
    pages.put("DB_AQ_SHREDDERS",new Integer(DB_AQ_SHREDDERS));
    pages.put("DB_AQ_QUEUEMAPS",new Integer(DB_AQ_QUEUEMAPS));
    pages.put("DB_AQ_ENQ_ERRORS",new Integer(DB_AQ_ENQ_ERRORS));
    pages.put("DB_AQ_DEQ_ERRORS",new Integer(DB_AQ_DEQ_ERRORS));
    pages.put("DB_AQ_DEQ_ERROR_XML",new Integer(DB_AQ_DEQ_ERROR_XML));
    pages.put("DB_AQ_QUEUE_MASTER",new Integer(DB_AQ_QUEUE_MASTER));
    pages.put("DB_MESSAGE_LATENCY",new Integer(DB_MESSAGE_LATENCY));
    pages.put("DB_AQ_DEST_MESSAGES",new Integer(DB_AQ_DEST_MESSAGES));
    pages.put("DB_AQ_HANDLER_ERRSUMM",new Integer(DB_AQ_HANDLER_ERRSUMM));

    pages.put("DB_DIRECTORIES",new Integer(DB_DIRECTORIES));
    pages.put("DB_LOCKS",new Integer(DB_LOCKS));
    pages.put("DB_SESSIONS",new Integer(DB_SESSIONS));
    pages.put("SESSIONS_SQL",new Integer(SESSIONS_SQL));
    pages.put("DB_POWERDBA_SESSIONS",new Integer(DB_POWERDBA_SESSIONS));
    pages.put("DB_SESSIONS2",new Integer(DB_SESSIONS2));
    pages.put("DB_PQSLAVES", new Integer(DB_PQSLAVES));
    pages.put("DB_PQSTATS", new Integer(DB_PQSTATS));
    pages.put("DB_PQSESSION", new Integer(DB_PQSESSION));
    pages.put("DB_PQTQ", new Integer(DB_PQTQ));
    pages.put("DB_SESSION_CHART", new Integer(DB_SESSION_CHART));
    pages.put("DB_FREESPACE",new Integer(DB_FREESPACE));
    pages.put("DB_FREESPACE_PROBLEMS",new Integer(DB_FREESPACE_PROBLEMS));
    pages.put("DB_TS",new Integer(DB_TS));
    pages.put("DB_SESS_ZOOM",new Integer(DB_SESS_ZOOM));
    pages.put("DB_SYSSTAT",new Integer(DB_SYSSTAT));  
    pages.put("DB_SYSRATIOS",new Integer(DB_SYSRATIOS));  
    pages.put("DB_RECOVERY_SIZE",new Integer(DB_RECOVERY_SIZE)); 
    pages.put("DB_JOBS_WL",new Integer(DB_JOBS_WL)); // Workload menu jobs option
    pages.put("DB_SCHJOBS_WL",new Integer(DB_SCHJOBS_WL)); // Workload schedular menu jobs option
    pages.put("DB_SCHJOBS_HIST",new Integer(DB_SCHJOBS_HIST)); // Workload schedular menu jobs option
    pages.put("DB_BLOCKERS", new Integer(DB_BLOCKERS));
    pages.put("DB_BG_SESSIONS", new Integer(DB_BG_SESSIONS));
    pages.put("DB_TRANSACTIONS", new Integer(DB_TRANSACTIONS));
    pages.put("PGA_DETAILS", new Integer(PGA_DETAILS));
    pages.put("DB_CARFAX_WEBLOGIC_SUMMARY", new Integer(DB_CARFAX_WEBLOGIC_SUMMARY));
    
    pages.put("DB_FILE",new Integer(DB_FILE));
    pages.put("DB_MEMORY",new Integer(DB_MEMORY));
    pages.put("DB_REGISTERED_LOGS", new Integer(DB_REGISTERED_LOGS));
    
    pages.put("DB_SEGMENT",new Integer(DB_SEGMENT));
    pages.put("DB_TABLE",new Integer(DB_TABLE));
    pages.put("EXPLAIN_PLAN",new Integer(EXPLAIN_PLAN));
    pages.put("EXPLAIN_PLAN_FROM_MEMORY",new Integer(EXPLAIN_PLAN_FROM_MEMORY));
    pages.put("DB_STREAMS",new Integer(DB_STREAMS));
    pages.put("DB_STREAMS_ERROR_DETAIL",new Integer(DB_STREAMS_ERROR_DETAIL));
    pages.put("DB_STREAMS_ERROR_QUEUE",new Integer(DB_STREAMS_ERROR_QUEUE));
    pages.put("DB_STREAMS_RULE_SETS",new Integer(DB_STREAMS_RULE_SETS));
    pages.put("DB_INSTANTIATED_TABLES",new Integer(DB_INSTANTIATED_TABLES));
    pages.put("DB_CAPTURE_DETAIL",new Integer(DB_CAPTURE_DETAIL));
    pages.put("DB_SOURCE",new Integer(DB_SOURCE));
    pages.put("DB_STREAMS_DML_HANDLERS",new Integer(DB_STREAMS_DML_HANDLERS));
    pages.put("DB_INSTANCE",new Integer(DB_INSTANCE));
    pages.put("DB_PARAMETERS",new Integer(DB_PARAMETERS));
    pages.put("DB_ARCHIVING",new Integer(DB_ARCHIVING));
    pages.put("DB_SYSTEM_EVENTS",new Integer(DB_SYSTEM_EVENTS));
    pages.put("DB_REDO_LOG",new Integer(DB_REDO_LOG));
    pages.put("DB_REDO_LOGFILE",new Integer(DB_REDO_LOGFILE));
    pages.put("DB_STREAMS_APPLY",new Integer(DB_STREAMS_APPLY));
    pages.put("DB_STREAMS_CAPTURE",new Integer(DB_STREAMS_CAPTURE));
    pages.put("DB_STREAMS_PROPAGATION",new Integer(DB_STREAMS_PROPAGATION));
    pages.put("DB_STREAMS_RULE_SETS_RN",new Integer(DB_STREAMS_RULE_SETS_RN));
    pages.put("DB_QUEUE_STATES",new Integer(DB_QUEUE_STATES));
    pages.put("DB_STREAMS_TRX",new Integer(DB_STREAMS_TRX));
    pages.put("DB_STREAMS_TRANS",new Integer(DB_STREAMS_TRANS));
    pages.put("DB_STREAMS_BG",new Integer(DB_STREAMS_BG));
    pages.put("DB_STREAMS_SHREDDERS",new Integer(DB_STREAMS_SHREDDERS));
    pages.put("DB_LONGOPS",new Integer(DB_LONGOPS));
    pages.put("DB_LONGOPS_SID",new Integer(DB_LONGOPS_SID));
    pages.put("DB_TABLESPACES",new Integer(DB_TABLESPACES));
    pages.put("DB_FILES",new Integer(DB_FILES));
    pages.put("DB_JOBS",new Integer(DB_JOBS));
    pages.put("DB_DATABASE",new Integer(DB_DATABASE));
    pages.put("DB_GLOBAL_SERVICES",new Integer(DB_GLOBAL_SERVICES));
    pages.put("DB_STREAMS_QUEUES",new Integer(DB_STREAMS_QUEUES));
    pages.put("DB_STREAMS_SUMMARY",new Integer(DB_STREAMS_SUMMARY));
    pages.put("DB_JOBS_DETAIL",new Integer(DB_JOBS_DETAIL));
    pages.put("DB_LONGOPS_DETAIL",new Integer(DB_LONGOPS_DETAIL));
    pages.put("DB_SQL_TEXT",new Integer(DB_SQL_TEXT));
    pages.put("DB_WAIT_DETAIL",new Integer(DB_WAIT_DETAIL));
    pages.put("DB_LOG_HISTORY",new Integer(DB_LOG_HISTORY));
    pages.put("DB_SCHEMAS",new Integer(DB_SCHEMAS));
    pages.put("DB_INSTANCE_MEMORY_SUMMARY",new Integer(DB_INSTANCE_MEMORY_SUMMARY));
    pages.put("DB_INSTANCE_MEMORY_DETAIL",new Integer(DB_INSTANCE_MEMORY_DETAIL));
    pages.put("DB_SQL_PLAN_STATISTICS",new Integer(DB_SQL_PLAN_STATISTICS));
    pages.put("DB_LOG_HISTORY_MINUTE",new Integer(DB_LOG_HISTORY_MINUTE));
    pages.put("DB_LOG_HISTORY_SECOND",new Integer(DB_LOG_HISTORY_SECOND));
    pages.put("Q_MESSAGES_EXT",new Integer(Q_MESSAGES_EXT)); 
    pages.put("DB_SESSION_LOCKS_HELD",new Integer(DB_SESSION_LOCKS_HELD));
    pages.put("DB_SESSION_LOCKS_REQUESTED",new Integer(DB_SESSION_LOCKS_REQUESTED));
    pages.put("DB_LOG_SUMMARY",new Integer(DB_LOG_SUMMARY));
    pages.put("DB_SQL_CHILDREN",new Integer(DB_SQL_CHILDREN));
    pages.put("DB_SESSION_CONNECT_INFO",new Integer(DB_SESSION_CONNECT_INFO));
    pages.put("DB_SESSION_ACCESS",new Integer(DB_SESSION_ACCESS));
    pages.put("DB_LATCH_PARENT",new Integer(DB_LATCH_PARENT));
    pages.put("DB_LATCH_CHILDREN",new Integer(DB_LATCH_CHILDREN));
    pages.put("DB_OBJECT",new Integer(DB_OBJECT));
    pages.put("DB_OBJECT_LIST",new Integer(DB_OBJECT_LIST));    
    pages.put("DB_OBJECT_DETAIL",new Integer(DB_OBJECT_DETAIL));   
    pages.put("DB_INDEX_PERFVIEW",new Integer(DB_INDEX_PERFVIEW));
    pages.put("DB_TABLE_PERFVIEW",new Integer(DB_TABLE_PERFVIEW));
    pages.put("DB_INITORA",new Integer(DB_INITORA));
    pages.put("DB_ROLES_ASSIGNED",new Integer(DB_ROLES_ASSIGNED));
    pages.put("DB_ROLE_ROLES",new Integer(DB_ROLE_ROLES));
    pages.put("DB_ROLE_OBJECTS",new Integer(DB_ROLE_OBJECTS));
    pages.put("DB_SYSTEM_PRIVS",new Integer(DB_SYSTEM_PRIVS));
    pages.put("DB_DIRECT_GRANTS",new Integer(DB_DIRECT_GRANTS));
    pages.put("DB_OBJECT_SUMMARY",new Integer(DB_OBJECT_SUMMARY));
    pages.put("BUFFER_CACHE", new Integer(BUFFER_CACHE));
    pages.put("BUFFER_CACHE_SEG", new Integer(BUFFER_CACHE_SEG));
    pages.put("DB_SYS_PRIVS", new Integer(DB_SYS_PRIVS));
    pages.put("DB_SESSION_STATS", new Integer(DB_SESSION_STATS));
    pages.put("DB_MVIEW_LOG_DEP", new Integer(DB_MVIEW_LOG_DEP));
    pages.put("DB_MVIEWS", new Integer(DB_MVIEWS));
    pages.put("DB_MVIEW_LOGS", new Integer(DB_MVIEW_LOG_DEP));
    pages.put("DB_SCHEMAS_W_OBJECTS", new Integer(DB_SCHEMAS_W_OBJECTS));
    pages.put("DB_SCHEMAS_AF", new Integer(DB_SCHEMAS_AF));
    pages.put("DB_SCHEMAS_GM", new Integer(DB_SCHEMAS_GM));
    pages.put("DB_SCHEMAS_NS", new Integer(DB_SCHEMAS_NS));
    pages.put("DB_SCHEMAS_TZ", new Integer(DB_SCHEMAS_TZ));
    pages.put("DB_SCHEMAS_TZ", new Integer(DB_HIDDEN_PARMS));
  
    pages.put("LC_SUMMARY",new Integer(LC_SUMMARY));
    pages.put("LC_BAD_SQL",new Integer(LC_BAD_SQL));
    pages.put("LC_PARSING_SUMMARY",new Integer(LC_PARSING_SUMMARY));
    pages.put("LC_PARSING_PARMS",new Integer(LC_PARSING_PARMS));
    pages.put("LC_HEAVY_SQL",new Integer(LC_HEAVY_SQL));
    pages.put("LC_ELAPSED_SQL",new Integer(LC_ELAPSED_SQL));
    pages.put("LC_MOSTEXEC_SQL",new Integer(LC_MOSTEXEC_SQL));
    pages.put("LC_OPEN_CURSORS",new Integer(LC_OPEN_CURSORS));
    
    pages.put("PERFSTAT",new Integer(PERFSTAT));
    pages.put("PERFSTAT2",new Integer(PERFSTAT2));
    pages.put("PERFSTAT_DETAIL",new Integer(PERFSTAT_DETAIL));
    pages.put("OWR_CHART", new Integer(OWR_CHART));
    
    pages.put("EDIT_RULE",new Integer(EDIT_RULE));
    pages.put("EDIT_RULE_SUBMIT",new Integer(EDIT_RULE_SUBMIT));
    
    pages.put("LOGOFF",new Integer(LOGOFF));
    pages.put("ADMIN_LOGIN",new Integer(ADMIN_LOGIN));
    
    pages.put("CHARTS_SUMMARY", new Integer(CHARTS_SUMMARY));
    pages.put("CHARTS_IO", new Integer(CHARTS_IO));
    pages.put("CHARTS_SESSIONS", new Integer(CHARTS_SESSIONS));
    pages.put("CHARTS_CONTENTION", new Integer(CHARTS_CONTENTION));
    pages.put("CHARTS_CLUSTER", new Integer(CHARTS_CLUSTER));
    pages.put("CHARTS_METRICS", new Integer(CHARTS_METRICS));
    pages.put("CHARTS_CPUIO", new Integer(CHARTS_CPUIO));
    pages.put("CHARTS_CPU", new Integer(CHARTS_CPU));
    pages.put("CHARTS_SHREDDER_THROUGHPUT", new Integer(CHARTS_SHREDDER_THROUGHPUT));
    pages.put("CHARTS_MESSAGE_RATE", new Integer(CHARTS_MESSAGE_RATE));
    pages.put("CHARTS_ACTIVE_SESSIONS", new Integer(CHARTS_ACTIVE_SESSIONS));
    pages.put("CHARTS_ACTIVE_SESSIONSBYSERVICE", new Integer(CHARTS_ACTIVE_SESSIONSBYSERVICE));
    
    pages.put("STREAMS_MANAGE_TRX_EXEC", new Integer(STREAMS_MANAGE_TRX_EXEC)); 
  
  }
  
  public static final String getPageId(String pageCode) throws Exception {

    String returnVal = "";

    if ( ensureLoaded() ) {
    	if ( pages.get(pageCode) == null  ) {
    		returnVal = pageCode;
    	} else {
        returnVal = ((Integer) pages.get(pageCode)).toString();
      }      
    }
    
    return returnVal;
  }
  
  public static final String getPageCode(int pageId) {
  
    String returnVal = null;
  
    if ( ensureLoaded() ) {
      returnVal = (String) reversePages.get(new Integer(pageId));
    }
    
    return returnVal;
  }
  
	private static boolean isLoaded() {
		return pages != null;
	}
  
  private static boolean ensureLoaded() {

    if ( !isLoaded() ) {

      try {     
        loadPageDefinitions();
        loadReversePageDefinitions();
      } catch (Exception e) {
        System.out.println("FAILED loading page definitions");
        return false;
      }
    }

    return true;
  }

  
  private static void loadReversePageDefinitions() 
  {
    reversePages = new Hashtable();
  
    Enumeration e = pages.keys();
    while ( e.hasMoreElements() ) {
      String pageCode = (String) e.nextElement();
      reversePages.put((Integer) pages.get(pageCode), pageCode);
    }
  }


 
  
/**
 * @return Returns the reversePages.
 */
public static Hashtable getReversePages() {
    return reversePages;
}
/**
 * @return Returns the pages.
 */
public static Hashtable getPages() {
    return pages;
}
}