package com.powerdba.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.dbcp.BasicDataSource;

import oracle.jdbc.OracleDriver;
import com.powerdba.ProcessDAO;
import com.powerdba.util.Tracer;

public class HeartbeatMonitor extends Thread {

	private DbConfig dbConfig;   
	private static final long MINUTE = 60000;
	private static final String OBJECT_NAME = "HeartbeatMonitor";
	
	HeartbeatMonitor(DbConfig dbConfig) {
		this.dbConfig = dbConfig;
	}
	
	public void run() {
		while (true) {		
	    checkConnection(this.dbConfig);   
		  try {
	      sleep((long) (.5 * MINUTE));
	    } catch (InterruptedException e) {}
		}
	}
	
  public static void checkConnection(DbConfig dbConfig) {
    
    // Now lets check to see if this pools connection is reachable.
   Connection conn = null;
   int transition = 0;

   Tracer.log("Monitor Checking " + dbConfig.getPoolName(), Tracer.DEBUG, OBJECT_NAME);

   int lastStatus = -1;
   long startTime = 0;
   
   String poolName = dbConfig.getPoolName();
   
   try {

	    dbConfig.purgeStates();	
	    
	    lastStatus = ConnectionConfigurationHolder.getPool(dbConfig.getPoolName()).getLastStatus();
	    
	    DbState newState = new DbState(DbState.CHECKING_IN_PROGRESS, 
         "Checking Instance...", 
         0, 
         transition, 
         0,
         System.currentTimeMillis());
	    newState.setStatusDate(System.currentTimeMillis());
	    newState.setSubstatus(0);
	    
     ConnectionConfigurationHolder.getPool(poolName).addState(newState);
     //globalNameHash.put(ProcessDAO.getGlobalName(conn), dbConfig);
      
     java.util.Properties props = new java.util.Properties();
     props.put ("user", dbConfig.getLogin());
     props.put ("password", dbConfig.getPassword());        
     startTime = System.currentTimeMillis();
     ConnectionConfigurationHolder.getPool(poolName).
       getLastState().setSubstatus(DbState.CHECKING_GETCONN);
     BasicDataSource ds = new BasicDataSource();
     ds.setDriverClassName(dbConfig.getDbDriver());
     ds.setUsername(dbConfig.getLogin());
     ds.setPassword(dbConfig.getPassword());
     ds.setUrl(dbConfig.getConnectString());
     conn = ds.getConnection();
     //conn = DriverManager.getConnection(dbConfig.getConnectString(), props);
     //conn = new OracleDriver().connect(dbConfig.getConnectString(), props);	
     ConnectionConfigurationHolder.getPool(poolName).getLastState().setSubstatus(DbState.CHECKING_PARSE);
     Statement stmt = conn.createStatement();
     ConnectionConfigurationHolder.getPool(poolName).getLastState().setSubstatus(DbState.CHECKING_EXEC);
     ResultSet rset = stmt.executeQuery("select count(*) from dual");
     ConnectionConfigurationHolder.getPool(poolName).getLastState().setSubstatus(DbState.CHECKING_FETCH);
     rset.next();
     	
     // Transition state to up
     if ( lastStatus == DbState.DOWN ) {
		    transition = DbState.DOWNTOUP;   
     } else if ( lastStatus == DbState.UP ) {
		    transition = DbState.NOCHANGE;		          
     } else if ( lastStatus == DbState.UNKNOWN ) {
       transition = DbState.UNKTOUP;
     } else {
       transition = DbState.UNKTOUP;
     }
    
     ConnectionConfigurationHolder.getPool(poolName).
        getLastState().setSecondsToGet(System.currentTimeMillis()-startTime);
     
     ConnectionConfigurationHolder.getPool(poolName).
      getLastState().setStatus(DbState.UP);
     
     ConnectionConfigurationHolder.getPool(poolName).
      getLastState().setStatusDate(System.currentTimeMillis());
     
     ConnectionConfigurationHolder.getPool(poolName).
      getLastState().setStatusMessage("Up");
     
     ConnectionConfigurationHolder.globalNameHash.put(ProcessDAO.getGlobalName(conn), 
     		               ConnectionConfigurationHolder.getPool(poolName));

   } catch ( SQLException e ) {
       
     // Assume SQL Error thrown will indicates down condition
     try {
         
	      if ( lastStatus == DbState.UP ) {
				  transition = DbState.UPTODOWN;
		    } else if ( lastStatus == DbState.DOWN ) {
				  transition = DbState.NOCHANGE;
	      } else if ( lastStatus == DbState.UNKNOWN ) {
		      transition = DbState.UNKTODOWN;
	      } else {
	        transition = DbState.UNKTODOWN;
	      }
	      
	      ConnectionConfigurationHolder.getPool(poolName).
        getLastState().setSecondsToGet(System.currentTimeMillis()-startTime);
     
     ConnectionConfigurationHolder.getPool(poolName).
      getLastState().setStatus(DbState.DOWN);
     
     ConnectionConfigurationHolder.getPool(poolName).
      getLastState().setStatusDate(System.currentTimeMillis());
     
     ConnectionConfigurationHolder.getPool(poolName).
      getLastState().setStatusMessage("Down");	
     
     ConnectionConfigurationHolder.globalNameHash.put(ProcessDAO.getGlobalName(conn), 
     		               ConnectionConfigurationHolder.getPool(poolName));

     } catch ( SQLException se) {
     	Tracer.log("Error setting dbconfig", Tracer.WARNING, OBJECT_NAME);
     }
     
     } finally {
       try {
         if ( conn != null ) {
           conn.close();
         }
       } catch ( SQLException se2) {}
   }        
}
	
}
