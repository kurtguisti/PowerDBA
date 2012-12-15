package com.powerdba.jdbc;

import com.powerdba.OracleDatabaseConnection;
import com.powerdba.OracleVersion;
import com.powerdba.ProcessDAO;
import com.powerdba.util.PropertyHolder;
import com.powerdba.util.Tracer;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Properties;

import javax.sql.DataSource;

import oracle.jdbc.pool.OracleDataSource;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;

/**
 * ConnectionManager is a static class designed to provide
 * automatic initialization of the connection pooling subsystem
 * on an as-needed basis.
 * <p>
 * Connection pool initialization is achieved by using DbInitReader
 * to parse a configuration file consisting of one or more connection
 * pool configurations.
 * <p>
 * A single property, <b><code>ConnectionPoolPropertiesFile</code></b>, is
 * read from PropertHolder.  This is the name of the configuration file to load.
 * If for some reason this property is missing or PropertyHolder is uninitialized,
 * the file "dbconfig.properties" will be used instead, if it exists.  If no
 * properties can be loaded, an Exception is thrown.
 * <p>
 * @see DbConfigDAO
 * @see PropertyHolder
 */
public class ConnectionManager {

	static final String DEFAULT_PROPERTIES_FILE = "dbconfig.properties";
	static Hashtable dbPools = new Hashtable();
	static final String OBJECT_NAME = "ConnectionMananger";

	/**
	 * Return a pooled database connection.
	 * If no pooled connections are available, a new one is created.
	 * <p>
	 * The first time this method is called, the connection pool will be intialized.
	 * <p>
	 * @return Connection
	 * @exception java.sql.SQLException
	 */
	
	static public  Connection getConnection(String poolName)	throws SQLException, IOException {
	    
	  Tracer.log("Entering ConnectionManager.getConnection()", Tracer.DEBUG, "ConnectionManager");
  
    try {
    	
    	//this loads the xml file into memory if it hasn't been loaded yet.
    	ConnectionConfigurationHolder.ensureLoaded();
    	
      if ( !initialized(poolName) ) init(poolName);
      
      Connection conn = ( (OracleDataSource) dbPools.get(poolName) ).getConnection();
      
      Tracer.log("Got a connection from the pool named " + poolName, Tracer.DEBUG, "ConnectionPool");
      
      CallableStatement cstmt = conn.prepareCall("call dbms_application_info.set_module(?,null)");
      cstmt.setString(1, "Power*DBA");
      cstmt.execute();
      cstmt.close();
      return conn;
      
    } catch (SQLException e ) {
      throw e;
    }
    
	}
	

	
  static public Connection getNonPooledConnection(String name) throws Exception {
    
    // This creates a new "non-pooled" connection.  

      Tracer.log("Creating a new non-pooled Connection to " + name, Tracer.DEBUG, "ConnectionManager");
      try {

        DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        DbConfig db = ConnectionConfigurationHolder.getDbConfig(name);
        return DriverManager.getConnection(db.getConnectString(), db.getLogin(), db.getPassword());

      } catch ( SQLException e ) {
        throw e;
      } 

  }

	/**
	 * Initialize connection pool.
	 * <p>
	 * In an application, this can be called on startup to pre-initialize.
	 * Otherwise, it is called automatically whenever the first connection
	 * is requested.
	 */
	static public synchronized void init(String poolName) throws SQLException {

    if ( !initialized(poolName) ) {

	    Tracer.log("\t===== Initializing JDBC Connection Pool " + poolName + " ====", Tracer.METHOD, "ConnectionManager");
	
			try {
				
				OracleDataSource ods = new OracleDataSource();
		    DbConfig cfg = (DbConfig) ConnectionConfigurationHolder.getPoolHash().get(poolName);
        // Set datasource cache attributes
        ods.setUser(cfg.getLogin());
        ods.setPassword(cfg.getPassword());               
        ods.setURL(cfg.getConnectString());
//        ods.setConnectionCachingEnabled(true);
//        ods.setFastConnectionFailoverEnabled(false); // Enable fcf
//        ods.setImplicitCachingEnabled(true);
//        ods.setConnectionCacheName(poolName);
//        
//        // setup the cache properties
//        java.util.Properties prop = new java.util.Properties();
//        prop.setProperty("InitialLimit",  "1"); // Initial number of pre-spawned connections
//        prop.setProperty("MinLimit", "1");         // Once exceeded, the minimum retained
//        prop.setProperty("MaxLimit","4");         // Maximum number of possible pool connections.
//        prop.setProperty("MaxStatementsLimit", "1000");  // The statement cache
//        prop.setProperty("InactivityTimeout", "900"); // How long not used before reclaimed.
//        prop.setProperty("AbandonedConnectionTimeout", "0"); // How long not used before reclaimed.
//        prop.setProperty("TimeToLiveTimeout", "0"); // How long not used before reclaimed.
//        prop.setProperty("ConnectionWaitTimeout", "3"); // How long not used before reclaimed.
//        prop.setProperty("PropertyCheckInterval", "300"); // Enforce checking every 300 seconds
//        
//        // Associate the Cache properties with the Cache
//        ods.setConnectionCacheProperties(prop);
				
				//DataSource ds = setupDataSource(poolName);
        dbPools.put(poolName, ods);

			} catch (Exception e) {
				Tracer.log(e, "Could not initalize Pool " + poolName, Tracer.ERROR, "ConnectionManager");
			}
    }
    
	}
	
	static private synchronized boolean initialized(String poolName) {
		return dbPools.get(poolName) != null;

	}
	
	
	static public OracleDatabaseConnection getNonPooledDatabase(String db) throws Exception {
		return getDatabase(db, false);
	}
	
	static public OracleDatabaseConnection getDatabase(String db) throws Exception {
		return getDatabase(db, true);
	}
	
  static private OracleDatabaseConnection getDatabase(String db, boolean pooled) throws Exception {

      OracleDatabaseConnection database = null;
      Hashtable metaData;
      
      try {
      	
        // Instantiate the configuraton from the xml config file
        DbConfig dbConfig = ConnectionConfigurationHolder.getDbConfig(db);   
        
        // Instantiate the Oracle Database Objects
        database = new OracleDatabaseConnection();
        database.setName(db);
        
        if ( pooled ) {
          database.setConn(ConnectionManager.getConnection(db));
        } else {
        	database.setConn(ConnectionManager.getNonPooledConnection(db));
        }
        database.getConn().setAutoCommit(false);
        
        metaData = ProcessDAO.getDatabaseMetadata(database);  //get all the db stuff in one db call
                                                              //for performance reasons
        database.setVersion((OracleVersion)metaData.get("instance_version"));
        database.setCompatible((OracleVersion)metaData.get("compatible"));
        database.setStringVersion((String)metaData.get("version_string"));
        database.setInstance((String)metaData.get("instance_name"));
        database.setDatabase((String)metaData.get("global_name"));
        database.setDateTime((Timestamp) metaData.get("sysdate"));
        Tracer.log("Getting the other descriptors for database " + db, Tracer.WARNING,"ConnectionManager");
        String currentDatabaseNameFromDBConfig = ConnectionConfigurationHolder.getDbConfig(db).getDatabaseName();
        Tracer.log("This is the database name from the dbconfig: " + currentDatabaseNameFromDBConfig,Tracer.WARNING,"ConnectionManager");
        ArrayList descriptors = (ArrayList) ConnectionConfigurationHolder.getDBHash().get(currentDatabaseNameFromDBConfig);
        ArrayList otherDescriptors = new ArrayList();
        for (int i=0; i<descriptors.size(); i++) {
        	String desc = (String) descriptors.get(i);
        	otherDescriptors.add(desc);
        }
        
        //database.setOtherRacInstances(ProcessDAO.getOtherInstances(database));
        database.setOtherRacDescriptors(otherDescriptors);
        database.setPooledConnection(true);
        database.setHasMetrics(dbConfig.hasMetrics());
        
        Tracer.log("Successfully instatiated an oracle database connection object from database: \n" + database.toString(), Tracer.DEBUG, "ConnectionManager");       
        return database;

      } catch ( SQLException e ) {     
        String msg = "SQL Error creating connection to connect descriptor [" + db + "].";
        Tracer.log(e, msg, Tracer.ERROR, "ConnectionManager");
        msg = msg + "<BR><b>Root Cause:</b>  " + e.getMessage() + "\n";
        throw new SQLException(msg);
      } catch ( Exception e ) {
        String msg = "Error creating connection to connect descriptor " + db + ".";
        Tracer.log(e, msg, Tracer.ERROR, "ConnectionManager");
        msg = msg + "<BR><b>Root Cause:</b>  " + e.getMessage() + "\n";
        throw new SQLException(msg);
      } 
      
    }
  
  public static synchronized DataSource setupDataSource(String poolName) {
  	
    //
    // First, we'll need a ObjectPool that serves as the
    // actual pool of connections.
    //
    // We'll use a GenericObjectPool instance, although
    // any ObjectPool implementation will suffice.
    //
    ObjectPool connectionPool = new GenericObjectPool(null);
    
    DbConfig cfg = (DbConfig) ConnectionConfigurationHolder.getPoolHash().get(poolName);
    
    try {
        Class.forName(cfg.getDbDriver());
    } catch (ClassNotFoundException e) {
        Tracer.log(e,"Error Loading JDBC underlying Class " + cfg.getDbDriver(), Tracer.ERROR, OBJECT_NAME);
    }

    //
    // Next, we'll create a ConnectionFactory that the
    // pool will use to create Connections.
    // We'll use the DriverManagerConnectionFactory,
    // using the connect string passed in the command line
    // arguments.
    //
    
    PoolingDataSource dataSource = null;
    Properties dbProps = null;
    ConnectionFactory connectionFactory = null;
    
    
	    dbProps = new Properties();
	    dbProps.put("username", cfg.getLogin());
	    dbProps.put("password", cfg.getPassword());

	    dbProps.put("maxActive",PropertyHolder.getProperty("jdbc.maxActive", "2"));
	    dbProps.put("initialSize", PropertyHolder.getProperty("jdbc.initialSize", "1"));
	    dbProps.put("maxIdle", PropertyHolder.getProperty("jdbc.maxIdle", "2"));
	    dbProps.put("minIdle", PropertyHolder.getProperty("jdbc.minIdle", "1"));
	    dbProps.put("maxWait", PropertyHolder.getProperty("jdbc.maxWait","3"));
	    dbProps.put("poolPreparedStatements", PropertyHolder.getProperty("jdbc.poolPreparedStatements", "true"));
	    dbProps.put("maxOpenPreparedStatements", PropertyHolder.getProperty("jdbc.maxOpenPreparedStatements", "100"));
	    dbProps.put("defaultAutoCommit", PropertyHolder.getProperty("jdbc.defaultAutoCommit","false"));
	    dbProps.put("testOnReturn", PropertyHolder.getProperty("jdbc.testOnReturn","false"));
	    dbProps.put("testOnBorrow", PropertyHolder.getProperty("jdbc.testOnBorrow","false"));
	    dbProps.put("testWhileIdle", PropertyHolder.getProperty("jdbc.testWhileIdle","false"));
	    //dbProps.put("timeBetweenEvictionRunsMillis", PropertyHolder.getProperty("jdbc.timeBetweenEvictionRunsMillis"));
	    //dbProps.put("minEvictableIdleTimeMillis", PropertyHolder.getProperty("jdbc.minEvictableIdleTimeMillis"));
	    dbProps.put("validationQuery", PropertyHolder.getProperty("jdbc.validationQuery", "select 1 from dual"));
	    dbProps.put("testWhileIdle", PropertyHolder.getProperty("jdbc.testWhileIdle","false"));
	
	    connectionFactory = new DriverManagerConnectionFactory(cfg.getConnectString(),cfg.getLogin(),cfg.getPassword());
	    //
	    // Now we'll create the PoolableConnectionFactory, which wraps
	    // the "real" Connections created by the ConnectionFactory with
	    // the classes that implement the pooling functionality.
	    //
	    PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory,connectionPool,null,null,false,true);
	    //
	    // Finally, we create the PoolingDriver itself,
	    // passing in the object pool we created.
	    //
	    dataSource = new PoolingDataSource(connectionPool);
	    
	    Tracer.log("Got the datasource", Tracer.DEBUG, OBJECT_NAME);

    return dataSource;
  }
  
	static public String dumpDbPoolsHash() {
		return "Size of DB Pools is "  + dbPools.size();
	}

}
