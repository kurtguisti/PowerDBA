package com.powerdba.jdbc;

import com.powerdba.util.PropertyHolder;
import com.powerdba.util.Tracer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;

public class ConnectionConfigurationHolder {

  private static final String OBJECT_NAME = "ConnectionConfigurationHolder";  
  private static Hashtable poolHash = null;
  private static Hashtable UrlHash = new Hashtable();
  private static Hashtable instanceNameHash = new Hashtable();
  static Hashtable globalNameHash = new Hashtable();
  private static Hashtable<String, ArrayList<String>> dbHash = new Hashtable<String, ArrayList<String>>();
  
  public static boolean instanceIsSick(String name) {
    boolean rval = true;
  	try {
			DbConfig config = (DbConfig) getPool(name);
	  	Tracer.log("Checking the real time health of the instance " + config.toString(), Tracer.DEBUG, "ConnectionConfigurationHolder.java");
			DbState ls = config.getLastState();
			if (ls.getStatus()       == DbState.DOWN || 
					ls.getStatus()       == DbState.UNKNOWN ||
					ls.getStatusDate()   < System.currentTimeMillis() - 600000 || 
					ls.getSecondsToGet() > 30000 ||
					(ls.getStatus() == DbState.CHECKING_IN_PROGRESS && System.currentTimeMillis()-ls.getStartTime()>5000) ) {
				rval=true;
			}
		} catch (SQLException e) {
			rval = true;  // return false if any problem determining health
		}
  	return rval;
  }
  
  public static boolean ensureLoaded() {
    
    HeartbeatMonitor monitor;

    if ( !isLoaded() ) {
    
      String fileName = "dbconfig.xml";
      Tracer.log("Loading db configs from " + fileName + " into the ConnectionConfigurationHolder Object's Static Memory", Tracer.DEBUG, "ConnectionConfigurationHolder");

      try {     
        poolHash         = new DbConfigDAO().getHash(fileName);
        instanceNameHash = new DbConfigDAO().getInstanceHash(fileName);
        UrlHash          = new DbConfigDAO().getUrlHash(fileName);
        dbHash           = new DbConfigDAO().getDbHash(fileName);
      } catch (Exception e) {
        System.out.println("ConnectionConfigHolder: FAILED loading from xml file " + fileName);
        return false;
      }
      
      // Get the property that says whether or not to turn on the monitor
      boolean enableMonitoring = false;
      String propVal = PropertyHolder.getProperty("Monitor.enable");
      if ( propVal != null ) 
         enableMonitoring = PropertyHolder.getProperty("Monitor.enable").equals("true")?true:false;

      if ( enableMonitoring ) {
        ArrayList<DbConfig> poolList = getPoolList();
        for ( int i=0; i<poolList.size(); i++ ) {
          DbConfig dbConfig = (DbConfig) poolList.get(i);        
          monitor = new com.powerdba.jdbc.HeartbeatMonitor(dbConfig);
          monitor.setName(dbConfig.getPoolName() + " Monitor");
	        monitor.start();
        }
      }            
    }
    
    return true;
  }

  	 	
	public static DbConfig getDbConfig(String name) {
  
    DbConfig dbConfig = null;
  
    if (ensureLoaded() ) {
      dbConfig = (DbConfig) poolHash.get(name.trim());
    }
		return dbConfig;
	}
	
	public static ArrayList getPoolList() {
	  ArrayList rval = new ArrayList();
	  if ( ensureLoaded() ) {
	    Enumeration enm = getPoolHash().keys();
	    while ( enm.hasMoreElements() ) {
	      rval.add((DbConfig) getPoolHash().get((String) enm.nextElement()));
	    }
	  }
    Collections.sort(rval, DbConfig.CASE_INSENSITIVE_DISPLAY_ORDER);
	  return rval;
	}
	
	public static Hashtable getPoolHash() {
		  Hashtable rval = null;
		  if ( ensureLoaded() ) {
		    rval = poolHash;
		  }
		  return rval;
		}
	
	public static Hashtable getInstanceNameHash() {
		  Hashtable rval = null;
		  if ( ensureLoaded() ) {
		    rval = instanceNameHash;
		  }
		  return rval;
		}
	
	public static Hashtable getUrlHash() {
		  Hashtable rval = null;
		  if ( ensureLoaded() ) {
		    rval = UrlHash;
		  }
		  return rval;
		}
	
	public static Hashtable getDBHash() {
	  Hashtable rval = null;
	  if ( ensureLoaded() ) {
	    rval = dbHash;
	  }
	  return rval;
	}

	public static boolean isLoaded() {  
    return poolHash != null;
	}
	
	static public DbConfig getPool(String name) throws SQLException {
		  return (DbConfig) ConnectionConfigurationHolder.getPoolHash().get(name);
	}
	
	public static void forceReload() {
	   poolHash=null;
	   UrlHash=null;
	   instanceNameHash=null;
	   ensureLoaded();
	}

  
	/**
	 * @param poolHash The poolHash to set.
	 */
	public static void setPoolHash(Hashtable poolHash) {
	    ConnectionConfigurationHolder.poolHash = poolHash;
	}
	
 
}