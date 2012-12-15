package com.powerdba;

import com.powerdba.mvc.jsp.JspEnvironment;
import com.powerdba.mvc.jsp.SelectEntry;
import com.powerdba.util.Tracer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

public class QueryHolder {
    
  private final static String OBJECT_NAME = "QueryHolder";

	private static Hashtable queries9i = null;
  private static Hashtable queries10g = null;
  private static Hashtable queries11g = null;
  private static Hashtable queriesCommon = null;
  private static Hashtable lookupQueries = null;
  private static Hashtable lookups = null;
  
  // Just return the unbound query object, mainly for reference purposes.
  public Hashtable get10gQueries() {
  	ensureLoaded(10);
  	return queries10g;
  }
  
  public static Query getQuery(OracleDatabaseConnection database, String name) throws SQLException {
  	
  	    Tracer.log("in getquery", Tracer.DEBUG, "QueryHolder");

  	    Query query = null;    
  	    OracleVersion oracleVersion = database.getVersion();
  	    
  	    Tracer.log("getting query for database " + database.toString(), Tracer.DEBUG, "QueryHolder");
  	     
  	    Tracer.log("got oracle version " + oracleVersion.getVersion1(), Tracer.DEBUG, "QueryHolder");
  	  
  	    if ( oracleVersion.getVersion1() == 9 ) {   
  	      if ( ensureLoaded(9) ) {
  	        query = (Query) queries9i.get(name.trim());
  	        if ( query == null && ensureLoaded(0) ) { 
  	          query = (Query) queriesCommon.get(name.trim());
  	          if ( query == null ) throw new SQLException("Query " + name + " was not found.");
  	        }
  	      }     
  	    }
  	    
  	    if ( oracleVersion.getVersion1() == 10 ) {
  	      if ( ensureLoaded(10) ) {
  	        query = (Query) queries10g.get(name.trim());
  	        if ( query == null && ensureLoaded(0) ) { 
  	          query = (Query) queriesCommon.get(name.trim());
  	          if ( query == null ) throw new SQLException("Query " + name + " was not found.");
  	        }
  	      }      
  	    }
  	    
  	    if ( oracleVersion.getVersion1() == 11 ) {
  	    	Tracer.log("Version: " + oracleVersion.getVersion1(), Tracer.DEBUG, "QueryHolder");
  	      if ( ensureLoaded(11) ) {
  	        query = (Query) queries11g.get(name.trim());
  	        if ( query == null && ensureLoaded(0) ) { 
  	          query = (Query) queriesCommon.get(name.trim());
  	          if ( query == null ) throw new SQLException("Query " + name + " was not found.");
  	        }
  	      }      
  	    }
  	    
  	    // Set the database
  	    if ( database.getConn()!=null )
  	      query.setDatabase(database);
  	    
  	    Tracer.log("Returning query", Tracer.DEBUG, "QueryHolder");

  			return query;

  }
  
  public static Query getQuery(OracleDatabaseConnection database, JspEnvironment env, String name) throws SQLException {

    Query query = null;    
    OracleVersion oracleVersion = database.getVersion();
  
    if ( oracleVersion.getVersion1() == 9 ) {   
      if ( ensureLoaded(9) ) {
        query = (Query) queries9i.get(name.trim());
        if ( query == null && ensureLoaded(0) ) { 
          query = (Query) queriesCommon.get(name.trim());
          if ( query == null ) throw new SQLException("Query " + name + " was not found.");
        }
      }     
    }
    
    if ( oracleVersion.getVersion1() == 10 ) {
      if ( ensureLoaded(10) ) {
        query = (Query) queries10g.get(name.trim());
        if ( query == null && ensureLoaded(0) ) { 
          query = (Query) queriesCommon.get(name.trim());
          if ( query == null ) throw new SQLException("Query " + name + " was not found.");
        }
      }      
    }
    
    if ( oracleVersion.getVersion1() == 11 ) {
      if ( ensureLoaded(11) ) {
        query = (Query) queries11g.get(name.trim());
        if ( query == null && ensureLoaded(0) ) { 
          query = (Query) queriesCommon.get(name.trim());
          if ( query == null ) throw new SQLException("Query " + name + " was not found.");
        }
      }      
    }
    
    // Set the database
    if ( database.getConn()!=null )
      query.setDatabase(database);
    
    // Bind against the JspEnvironment
    if ( env.getPageContext() != null )
      query.bindAll(env);
    
    Tracer.log("Returning query", Tracer.DEBUG, "QueryHolder");

		return query;
	}
  
  // TODO: refactor this and the above method to reduce duplicate code
  // Hashtable is of key value pairs to bind, instead of an env object.
  public static Query getQuery(OracleDatabaseConnection database, Hashtable hash, String name) throws SQLException {
    
    Query query = null;    
    OracleVersion oracleVersion = database.getVersion();
    
    // Decide which XML file to pull the sql from based on the release of Oracle
    // we are connected to.  
  
    if ( oracleVersion.getVersion1() == 9 ) {   
      if ( ensureLoaded(9) ) {
        query = (Query) queries9i.get(name.trim());
        if ( query == null && ensureLoaded(0) ) { 
          query = (Query) queriesCommon.get(name.trim());
          if ( query == null ) throw new SQLException("Query " + name + " was not found.");
        }
      }     
    }
    
    if ( oracleVersion.getVersion1() == 10 ) {
      if ( ensureLoaded(10) ) {
        query = (Query) queries10g.get(name.trim());
        if ( query == null && ensureLoaded(0) ) { 
          query = (Query) queriesCommon.get(name.trim());
          if ( query == null ) throw new SQLException("Query " + name + " was not found.");
        }
      }      
    }
    
    if ( oracleVersion.getVersion1() == 11 ) {
      if ( ensureLoaded(10) ) {
        query = (Query) queries11g.get(name.trim());
        if ( query == null && ensureLoaded(0) ) { 
          query = (Query) queriesCommon.get(name.trim());
          if ( query == null ) throw new SQLException("Query " + name + " was not found.");
        }
      }      
    }
    
    // Set the database
    query.setDatabase(database);
    
    // Bind against the JspEnvironment
    if ( hash != null )
      query.bindAll(hash);

		return query;
	}
  
  public static LookupDisplayGroup getLookupGroup(OracleDatabaseConnection database, String name) throws SQLException {
      
      Hashtable populatedSelectLists = new Hashtable();
      ArrayList list = new ArrayList();
      LookupDisplayGroup rval = null;
      
      Tracer.log("Looking up lookup group name " + name, Tracer.DEBUG, "QueryHolder");

      // Make sure that the static lookup group info has been loaded in from the xml configuration file
      // If it is not loaded, then it will be loaded by the method that checks it.  Done once for the life of the JVM.
      // This check does NOT load the actual static lists only there definition.  The code in the if statment will
      // load the data from the database to populate the lists each time getLookupGroup is called.
      // We load the lookup data at run time so it is real time.
      
      if ( ensureLookupLoaded() ) {
          
          ResultSet rset = null;
          PreparedStatement pstmt = null;
          
          String sql = (String) lookupQueries.get(name);
          
          try {
                
            pstmt = database.getConn().prepareStatement(sql);
            rset  = pstmt.executeQuery();   
            String lastType = "xxxx";
            
            for ( short s=1; rset.next(); s++ ) {
                
  	          String type = rset.getString(1);
  	          
  	          if ( !type.equals(lastType) ) {
  	            if ( (ArrayList) populatedSelectLists.get(type) == null ) {
  	              populatedSelectLists.put(type, new ArrayList());
  	            }
  	          }
  	          
  	          // Add a new select entry to the list gotten from the hashtable.
  	          ((ArrayList) populatedSelectLists.get(type)).add(new SelectEntry(rset.getString(2), rset.getString(3), s));
Tracer.log("Added new selectEntry " + rset.getString(2) + " for type " + type, Tracer.DEBUG, OBJECT_NAME);
  	          
  	          lastType = type;          
            }


          } catch ( SQLException e ) {
            Tracer.log(e,"Error in getting select lists from oracle for " + name, Tracer.ERROR, OBJECT_NAME);
            throw e;
          } finally {
            if ( pstmt != null ) pstmt.close();
          }
      }
      
      ArrayList displayTemplate = (ArrayList) lookups.get(name);
      
Tracer.log("Returning a populatedSelectLists has size " + populatedSelectLists.size(), Tracer.DEBUG, OBJECT_NAME);
Tracer.log("Returning a displayTemplate has size " + displayTemplate.size(), Tracer.DEBUG, OBJECT_NAME);

      
      return new LookupDisplayGroup(populatedSelectLists, displayTemplate);
      
  }      

	public static boolean isLoaded(int version) {
  
    boolean rval = true;
    
    if ( version == 9 ) {
      rval = queries9i != null;
    }
    
    if ( version == 10  ) {
      rval = queries10g != null;
    }
    
    if ( version == 11 ) {
      rval = queries11g != null;
    }    
    
    if ( version == 0 ) {
      rval = queriesCommon != null;
    }
    
    return rval;
	}
	
	public static boolean isLookupLoaded() {
	  return lookupQueries != null;
	}
	
  private static boolean ensureLookupLoaded() {
      
    try {

      if ( !isLookupLoaded() ) {
          lookupQueries = XmlDAO.getLookupQueries("lookup-queries.xml");
          lookups = XmlDAO.getLookups("lookups.xml");         
      }
      
      return true;
      
    } catch (Exception e) {
        System.out.println("QueryHolder: FAILED loading Lookup Querys/Lists");
        return false;
    }      
      
  }

  private static boolean ensureLoaded(int version) {

    if ( !isLoaded(version) ) {

      Tracer.log("Loading query definitions from file.", Tracer.DEBUG, "QueryHolder");

      try {   
          
        if ( version == 9 ) {
          Tracer.log("Loading 9i queries into static memory", Tracer.DEBUG, "QueryHolder");
          queries9i = XmlDAO.getQueries("sql-" + version + ".xml");
        } 
        if ( version == 10) {
        	version = 10;
          Tracer.log("Loading 10g queries into static memory", Tracer.DEBUG, "QueryHolder");
          queries10g = XmlDAO.getQueries("sql-" + version + ".xml");
          queries10g.putAll(XmlDAO.getQueries("sql-extend.xml"));
        }
        if ( version == 11) {
        	version = 10;
          Tracer.log("Loading 10g queries into static memory", Tracer.DEBUG, "QueryHolder");
          queries11g = XmlDAO.getQueries("sql-" + version + ".xml");
          queries11g.putAll(XmlDAO.getQueries("sql-extend.xml"));
        }
        if ( version == 0 ) {
          Tracer.log("Loading Common queries into static memory", Tracer.DEBUG, "QueryHolder");
          queriesCommon = XmlDAO.getQueries("sql-common.xml");
        }
        
      } catch (Exception e) {
        System.out.println("QueryHolder: FAILED loading");
        return false;
      }

    }
    return true;
  }
  
}