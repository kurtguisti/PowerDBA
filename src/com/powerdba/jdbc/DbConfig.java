package com.powerdba.jdbc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import com.powerdba.util.PropertyHolder;
import com.powerdba.util.Tracer;

public class DbConfig {
    
    private static final int STATE_RETENTION_DEFAULT = 1;
    
    private String poolName      = "";
    private String dbDriver      = "";
    private String databaseName  = "";
    private String connectString = "";
    private String login         = "";
    private String password      = "";
    private boolean page         = false;
    private boolean email        = false;
    private boolean monitor      = false;
    private boolean hasMetrics   = false;
    private ArrayList states     = new ArrayList();
    private ArrayList snapshots  = new ArrayList();
    private int openConnectionCount = 0;
    private int oracleSessionId  = -1;
    private int connectionCount = 0;
    private int dbConnectionCount = 0;
    private boolean doReset = false;

    public DbConfig() {}

    public DbConfig(String dbDriver, String connectString, String login, String password) {
	    this.dbDriver      = dbDriver;
	    this.connectString = connectString;
	    this.login         = login;
	    this.password      = password;
    }

    /// start gets
    public String getConnectString() {
        return this.connectString;
    }

    public String getDbDriver() {
        return this.dbDriver;
    }

    public String getLogin() {
        return this.login;
    }

    public String getPassword() {
        return this.password;
    }
    
    public boolean hasMetrics() {
      return this.hasMetrics;
  }

    public String getPoolName() {
        return this.poolName;
    }
    
    public String getDatabaseName() {
			return databaseName;
		}

		public void setDatabaseName(String databaseName) {
			this.databaseName = databaseName;
		}

		public void setReset(boolean reset) {
        doReset = reset;
    }
    
    public boolean doReset() {
        return doReset;
    }
    
    

    public void setConnectString(String connectString) {
        this.connectString = connectString;
    }

    public void setDbDriver(String dbDriver) {
        this.dbDriver = dbDriver;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public void setHasMetrics(boolean hasMetrics) {
      this.hasMetrics = hasMetrics;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }
    


    /**
     * @return Returns the openConnectionCount.
     */
    public int getOpenConnectionCount() {
        return openConnectionCount;
    }
    /**
     * @param openConnectionCount The openConnectionCount to set.
     */
    public void setOpenConnectionCount(int openConnectionCount) {
        this.openConnectionCount = openConnectionCount;
    }
    /**
     * @return Returns the snapshots.
     */
    public ArrayList getSnapshots() {
        return snapshots;
    }
    /**
     * @param snapshots The snapshots to set.
     */
    public void setSnapshots(ArrayList snapshots) {
        this.snapshots = snapshots;
    }

    /**
     * @return Returns the oracleSessionId.
     */
    public int getOracleSessionId() {
        return oracleSessionId;
    }
    /**
     * @param oracleSessionId The oracleSessionId to set.
     */
    public void setOracleSessionId(int oracleSessionId) {
        this.oracleSessionId = oracleSessionId;
    }
    /**
     * @return Returns the states.
     */
    public ArrayList getStates() {
      return states;
    }
    /**
     * @param states The states to set.
     */
    public void setStates(ArrayList states) {
      this.states = states;
    }
    
    public void addState(DbState dbState) {
      this.getStates().add(dbState);
    }
    
    
    
    /**
     * @return Returns the email.
     */
    public boolean isEmail() {
        return email;
    }
    /**
     * @param email The email to set.
     */
    public void setEmail(boolean email) {
        this.email = email;
    }
    /**
     * @return Returns the page.
     */
    public boolean isPage() {
        return page;
    }
    /**
     * @param page The page to set.
     */
    public void setPage(boolean page) {
        this.page = page;
    }
    // Returns the DbState object representing the most recent state.
    public DbState getLastState() {
      if ( this.getStates().size() > 0 ) {
	      //sortDbStates();
	      DbState dbState = null;
	      for ( int i=0; i<this.getStates().size(); i++ ) {
	        dbState = (DbState) this.getStates().get(i);
	      }
	      return dbState;
      } else {
        return new DbState();
      }
    }
    
    // The purge to keep cached state info to a certain number of hours.
    public void purgeStates() {

	    int purgeTimeout = 0;        
	    String value = PropertyHolder.getProperty("Monitor.connectionStateRetentionHours");
      
      if ( value == null ) {
          Tracer.log("Property Monitor.connectionStateRetentionHours not set in property file, using default of " 
                     + STATE_RETENTION_DEFAULT + " hours.", Tracer.WARNING, this);
          purgeTimeout = STATE_RETENTION_DEFAULT;        
      } else {
          purgeTimeout = new Integer(value).intValue();
      }
      
      purgeStates(purgeTimeout);
      
    }
    
    // Remove any state records past a specified number of hours.  To prevent buildup of state history in memory
    public void purgeStates(int hours) {
      DbState dbState = null;
      for ( int i=0; i<this.getStates().size(); i++ ) {
        dbState = (DbState) this.getStates().get(i);
        if ( dbState.getStatusDate() < System.currentTimeMillis() - (hours*1000*60*60) ) {
          this.getStates().remove(i);
        }
      }
    }
    
    // Sort the in memory state list by the default comparator
    public void sortDbStates() {
      try {
        Collections.sort(getStates());
      } catch ( Exception e ) {
        Tracer.log(e, "Error sorting list of states", Tracer.ERROR, this);
      }
    }
    
    // Provided as a simple way to get the last status of the db.
    public int getLastStatus() {
      return this.getLastState().getStatus();
    }
    
    /**
     * @return Returns the connectionCount.
     */
    public int getConnectionCount() {
        return connectionCount;
    }
    /**
     * @param connectionCount The connectionCount to set.
     */
    public void setConnectionCount(int connectionCount) {
        this.connectionCount = connectionCount;
    }

    /**
     * @return Returns the dbConnectionCount.
     */
    public int getDbConnectionCount() {
        return dbConnectionCount;
    }
    /**
     * @param dbConnectionCount The dbConnectionCount to set.
     */
    public void setDbConnectionCount(int dbConnectionCount) {
        this.dbConnectionCount = dbConnectionCount;
    }
    
    
    /**
     * @return Returns the monitor.
     */
    public boolean isMonitor() {
        return monitor;
    }
    /**
     * @param monitor The monitor to set.
     */
    public void setMonitor(boolean monitor) {
        this.monitor = monitor;
    }
    // For debugging, use this toString to dispay the values.
    public String toString() {


        StringBuffer returnVal = new StringBuffer();

        returnVal.append("begin DATA\r\n");
        returnVal.append("   poolName=" + this.poolName + "\r\n");
        returnVal.append("   dbDriver=" + this.dbDriver + "\r\n");
        returnVal.append("   connectString=" + this.connectString + "\r\n");
        returnVal.append("   login=" + this.login + "\r\n");
        returnVal.append("   password=" + this.password + "\r\n");
        returnVal.append("   lastStatus=" + this.getLastState().getStatus() + "\r\n");
        returnVal.append("   lastStatusDate=" + new Date(this.getLastState().getStatusDate())  + "\r\n");
        
        returnVal.append("end DATA\r\n");

        return returnVal.toString();
    }
    
    // Comparator to sort on the Pool Name in ascending order.  Not case sensitive.
    static public final Comparator CASE_INSENSITIVE_DISPLAY_ORDER = new Comparator() {
      public int compare(Object o1, Object o2) {
          DbConfig r1 = (DbConfig) o1;
          DbConfig r2 = (DbConfig) o2;
          return -r2.getPoolName().toLowerCase().compareTo(r1.getPoolName().toLowerCase());
      }
    };
}
