package com.powerdba;

import com.powerdba.gui.Page;
import com.powerdba.gui.PageGenerator;
import com.powerdba.gui.PageLoader;
import com.powerdba.gui.PageSet;
import com.powerdba.jdbc.ConnectionConfigurationHolder;
import com.powerdba.jdbc.ConnectionManager;
import com.powerdba.jdbc.DbConfigDAO;
import com.powerdba.mvc.jsp.JspEnvironment;
import com.powerdba.util.Tracer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.sql.Connection;

public class Query {

  public static final int STATISTICS = 1;
  public static final int GENERIC = 0;

  public Query() {}
  
  public Query(String name, String sqlString, String title) {
    this.sqlString = sqlString;
    this.name = name;
    this.title = title;
    this.charts = new ArrayList();
  }
  
  private String name = "";
  private String sqlString = "";
  private Hashtable parms = new Hashtable();
  private Hashtable parmPositions = new Hashtable();
  private Hashtable links = new Hashtable();
  private OracleDatabaseConnection database = null;
  private Connection conn = null;
  private String title = "";
  private PreparedStatement pstmt = null;
  private ArrayList charts = null;
  private ArrayList hiddens = new ArrayList();
  private ArrayList preformats = new ArrayList();
  private int dataType = 0;
  private boolean racEnabled = false;
  private ArrayList lookups = new ArrayList();
  
  public void bindAll(JspEnvironment env) throws SQLException {
      
    // Todo: handle different datatypes.  Currently, assumes all Strings and casts all
    //       value objects to strings.
      
	  Enumeration parmKeys = parms.keys();

    // Loop through each bind variable defined for the sql.  Get the value from the environment.
	  while ( parmKeys.hasMoreElements() ) {
	    String varName = (String) parmKeys.nextElement();
	    BindVar var = (BindVar) parms.get(varName);
	    Tracer.log("Getting value for variable " + varName + " from env.", Tracer.DEBUG, this);
	    String varValue = env.getParameter(varName);
	    Tracer.log("got value of " + varValue, Tracer.DEBUG, this);

      // Do validation of varValue.
		  if ( varValue.equals("") || varValue.equals("-1")) {
		    if ( var.getDefaultValue() != null ) {
		      varValue = (String) var.getDefaultValue();
		    } else {
		      if ( !varValue.equals("-1") )
		        throw new SQLException("Variable '" + varName + "' not found in env and no default value set in xml.");
		    }
		  }  
	    
      // Set the bind variable's value
		  this.setBindValue(varName, varValue);
	  }
  }
  
  public void bindAll(Hashtable parmValues) throws SQLException {
    
    // Todo: handle different datatypes.  Currently, assumes all Strings and casts all
    //       value objects to strings.
      
	  Enumeration parmKeys = parms.keys();

    // Loop through each bind variable defined for the sql.  Get the value from the environment.
	  while ( parmKeys.hasMoreElements() ) {
	    String varName = (String) parmKeys.nextElement();
	    BindVar var = (BindVar) parms.get(varName);
	    Tracer.log("Getting value for variable " + varName + " from Hashtable.", Tracer.DEBUG, this);
	    String varValue = (String) parmValues.get(varName);
	    Tracer.log("got value of " + varValue, Tracer.DEBUG, this);

      // Do validation of varValue.
		  if ( varValue == null || varValue.equals("") || varValue.equals("-1")  ) {
		    if ( var.getDefaultValue() != null ) {
		      varValue = (String) var.getDefaultValue();
		    } else {
		      if ( !varValue.equals("-1") )
		        throw new SQLException("Variable '" + varName + "' not found in hash and no default value set in xml.");
		    }
		  }  
	    
      // Set the bind variable's value
		  this.setBindValue(varName, varValue);
	  }
  }
  
  public void setBindValue(String name, String value) throws SQLException {
    if ( this.getParms().get(name) == null ) {
      throw new SQLException("Query Bind Variable " + name + " not found in query XML.");
    }   
    ((BindVar) this.getParms().get(name)).setValue(value);  
  }
  
  public void setBindValue(String name, Timestamp value) throws SQLException {     
		if ( this.getParms().get(name) == null ) {
		  throw new SQLException("Query Bind Variable " + name + " not found in query XML.");
		}
		((BindVar) this.getParms().get(name)).setValue(value);  
  }

  public PageSet getPageSet() throws SQLException { 
  
    PageSet pageSet = null;
    
    try {
      // Build a Series of pages by calling the PageLoader.  
    	//  This returns a set of pages, not html yet.
      pageSet = PageLoader.buildPageSetNew(this.executeQuery(), 0, 500, 0, this.links, this.hiddens, this.preformats,
      		                                 null, this.title, 500, this.charts, this.getDataType());
      // To potentially display the SQL at some point to the user.
      pageSet.setSql(this.name);
    } catch ( SQLException e ) {
      throw e;
    } finally {
    	try{
        if ( this.pstmt != null ) 
        	  this.pstmt.close();
    	} catch (SQLException e) {}
    }
    
    return pageSet;

  }
  
  public ArrayList getPageSections() throws SQLException, Exception { 
      
        // Inoking this method returns a list of pages or sections, it only allows page 1 to be returned, so pagination is not allowed
        // in this type of processing.
      
        ArrayList pageSections = new ArrayList();
        
		        try {
		            
		            Page otherPage = null;
		            
		            // Generate the original page and add it to the list
		            long startTime = System.currentTimeMillis();
		            Page originalPg = this.getPageSet().getPage(1);
		            originalPg.setGenTime(System.currentTimeMillis()-startTime);
		            
		            originalPg.setSql(originalPg.getSql());
		            Tracer.log("Set original page sql to " + originalPg.getSql(), Tracer.DEBUG, this);
		            
		            originalPg.setDatabaseLink(this.database.getName());
		            pageSections.add(originalPg);            
		            OracleDatabaseConnection otherDatabase = null;

		            Tracer.log("Running SQL query " + this.name, Tracer.DEBUG, this);
		              
                if ( this.isRacEnabled() ) {
                	
  		            Tracer.log("This action is  RAC enabled.", Tracer.DEBUG, this);
  		            
		              originalPg.setTitle(this.database.getInstance());
                	
  		            ArrayList otherDescriptors = database.getOtherRacDescriptors();
  		            // Remove the current instance...
  		            otherDescriptors.remove(database.getInstance());
  		            Collections.sort(otherDescriptors);
		            
			            for ( int i=0; i<otherDescriptors.size(); i++ ) {   
			            	
			            	String otherDescriptor = (String) otherDescriptors.get(i);
			            	Tracer.log("Getting a connection to " + otherDescriptor + " for a RAC enabled query.", Tracer.DEBUG, this);
			
			              try {  
			              	
			              	//if ( ConnectionConfigurationHolder.instanceIsSick(otherDescriptor) ) {
			                //  Page p = new Page();
			                //  p.setTitle(otherDescriptor);
			                //  p.setErrorMessage("Instance failed health check, skipping...");
			                //  pageSections.add(p);         		
			              	//} else {	                
				                // Make a connection to another database in the cluster and run the same query.  Put page 1 of the resultset into a list.
				                startTime = System.currentTimeMillis();
			              		otherDatabase = ConnectionManager.getDatabase(otherDescriptor);
				                this.setDatabase(otherDatabase);
				                startTime = System.currentTimeMillis();
				                otherPage = this.getPageSet().getPage(1);
				                otherPage.setGenTime(System.currentTimeMillis()-startTime);
				                otherPage.setDatabaseLink(otherDatabase.getName());  // This is so that hyperlinks written out by the PageGenerator are generated with the correct descriptor
				                otherPage.setTitle(otherDescriptor); // Set the title to the instance name.
				                pageSections.add(otherPage);    // Add it to the list of page sections 		
			              	//}
			              	
			              } catch ( SQLException se ) {			                  
			                  Page p = new Page();
			                  p.setTitle(otherDescriptor);
			                  p.setErrorMessage(se.getMessage());
			                  Tracer.log(se,"Error running query on this database.", Tracer.ERROR, this);
			                  pageSections.add(p);
			                  
			              } finally {
			  	              // close each other database connection in here.  the original connection is closed out in the Manager as always.
			  	              if ( otherDatabase != null && otherDatabase.getConn() != null ) {  
			  		    	        try {
			  		    	          //Tracer.log("Closing connection in pool " + otherDatabase.getName(), Tracer.DEBUG, this);
			  		    	          otherDatabase.getConn().close();
			  		    	          Tracer.log("Closed connection to " + otherDatabase.getName(), Tracer.DEBUG, this);
			  		      	      } catch ( Exception e ) {}
			  	              }                
			              }
			              
			            } 
                }
		
		            
		        } catch ( SQLException e ) {
		          throw e;
		        } catch (Exception e2) {	
		            // TODO Auto-generated catch block
		            throw e2;
		        } finally {
		          if ( this.pstmt != null ) this.pstmt.close();
		        }    
        
        return pageSections;

      }
  
  public void makeRac() {
    if ( racEnabled ) {
      //sqlString = StringUtility.replace(sqlString, "v$", "gv$");
      //sqlString = StringUtility.replace(sqlString, "select ", "select inst_id,");
    }
  }
  
  public String getHtml(int displayType) throws SQLException, Exception { 
    return getHtml(displayType, 4);
  }
  
  public String getHtml(int displayType, int columns) throws SQLException, Exception { 
  
    String html = null;
    
    try {   

      if ( displayType == ProcessDAO.TABLE ) {
          
        PageSet ps = this.getPageSet();  // Executes the query
        
        Page page = ps.getPage(1);
        page.setTitle(ps.getTitle());  // Set the title
    
        PageGenerator pg = new PageGenerator(page);
        pg.setDatabase(this.database.getName());
        pg.setColor1("#e5e5e5");
        pg.setColor2("#f5f5f5");
        pg.setHeadingBackgoundColor("#73969c");    
        pg.setCellBorderColor("#cccccc");
        pg.setForm("powerdba");
        pg.setJsp("powerdba.jsp");
        
        html = pg.getHtmlNew();

      } else {
          
        html = ProcessDAO.buildDetailDisplay(this.executeQuery(),this.links, this.title, columns);

      }
      
      return html;
      
    } catch ( SQLException e ) {
      throw e;
    } finally {
      if ( this.pstmt != null ) this.pstmt.close();
    }
    
  }
  
  public String getAscii() throws SQLException, Exception { 
    
    String asciiOut = null;
    
    try {   
          
        PageSet ps = this.getPageSet();  // Executes the query
        
        Page page = ps.getPage(1); // Assume one page for each Query
        page.setTitle(ps.getTitle());  // Set the title
    
        PageGenerator pg = new PageGenerator(page);
        pg.setDatabase(this.database.getName());  
        asciiOut = pg.getAscii();
      
        return asciiOut;
      
    } catch ( SQLException e ) {
      throw e;
    }
    
  }
  
  //////////////////////////////////////////////////////////////
  
  private ResultSet executeQuery() throws SQLException {
      
    if ( database == null || database.getConn() == null ) 
    	throw new SQLException("Database Connection was not set on query object.");

    try {
      Tracer.log("Setting up the xml defined query \n" + sqlString, Tracer.DEBUG, this );
      //this.pstmt = database.getConn().prepareStatement(sqlString, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      this.pstmt = database.getConn().prepareStatement(sqlString);
      Tracer.log("Start Binding values into the xml defined query.", Tracer.DEBUG, this );    
      bind();
      Tracer.log("Starting to execute the xml defined query.", Tracer.DEBUG, this );
      return this.pstmt.executeQuery();
    } catch ( SQLException e ) {
      throw new SQLException("Error during execution of this query:\n" + sqlString + "\n" + e.toString());
    }
  }
  
  private void bind() throws SQLException {
  
    try {  
      for ( int i=1; i<=this.getParmPositions().size(); i++ ) {
        // Get the variable name from the name -> position xref
        String varName = (String) this.getParmPositions().get(Integer.toString(i));
        
        // Get the variable definition by name.
        BindVar var = (BindVar) this.getParms().get(varName);
        
        Tracer.log("Binding variable " + varName + "=" + var.getValue() + ", type=" + var.getDataType() + " into position " + i + " in query.", Tracer.DEBUG, this);
        
        if ( var.getDataType().equals("java.lang.String") ) {
          this.pstmt.setString(i, (String) var.getValue());
        } else if ( var.getDataType().equals("java.sql.TimeStamp") ) {
          this.pstmt.setTimestamp(i, (Timestamp) var.getValue());
        }
      }
    } catch ( SQLException e) {
      Tracer.log(e, "Error Binding values to query " + this.name, Tracer.ERROR, this);
      throw e;
    }
    
  }
  
  private void loadSql() {}

  public void setSqlString(String sqlString)
  {
    this.sqlString = sqlString;
  }


  public String getSqlString()
  {
    return sqlString;
  }
  
  public void setConnection(Connection conn) {
  	this.conn = conn;
  }


  public void setName(String name)
  {
    this.name = name;
  }


  public String getName()
  {
    return name;
  }


  public void setParms(Hashtable parms)
  {
    this.parms = parms;
  }


  public Hashtable getParms()
  {
    return parms;
  }


  public void setLinks(Hashtable links)
  {
    this.links = links;
  }


  public Hashtable getLinks()
  {
    return links;
  }


  public void setTitle(String title)
  {
    this.title = title;
  }


  public String getTitle()
  {
    return title;
  }


  public void setParmPositions(Hashtable parmPositions)
  {
    this.parmPositions = parmPositions;
  }


  public Hashtable getParmPositions()
  {
    return parmPositions;
  }
  
  public String toString() {
  
    StringBuffer sb = new StringBuffer();
    
    sb.append("Query Object: " + this.name + "\n");
    sb.append("  sqlString: \n" + this.sqlString + "\n");
    sb.append("  title: " + this.title + "\n");
    sb.append("  database: " + this.database + "\n");
    sb.append("  parms: \n");
    Enumeration enm = this.parms.keys();
    while ( enm.hasMoreElements() ) {
      String varName = (String) enm.nextElement();
      sb.append( ((BindVar) this.parms.get(varName)).getVarName()  + " = ");
      sb.append( ((BindVar) this.parms.get(varName)).getValue() + "\n");
    }
    
    sb.append("\nparm positions: \n");
    enm = this.parmPositions.keys();
    while ( enm.hasMoreElements() ) {
      String varPosition = (String) enm.nextElement();
      sb.append( "Position: " + varPosition + "  ");
      sb.append( "Name: " + ((String) this.parmPositions.get(varPosition)) + "\n");
    }
    
    sb.append("\nLinks: \n");
    enm = this.links.keys();
    while ( enm.hasMoreElements() ) {
      Link link = (Link) links.get( (String) enm.nextElement() ) ;
      sb.append(link.toString());
    }
    
    return sb.toString();
        
  }


  public void setCharts(ArrayList charts)
  {
    this.charts = charts;
  }


  public ArrayList getCharts()
  {
    return charts;
  }


  public void setDataType(int dataType)
  {
    this.dataType = dataType;
  }


  public int getDataType()
  {
    return dataType;
  }


	public void setDatabase(OracleDatabaseConnection database)
  {
    this.database = database;
  }


  public OracleDatabaseConnection getDatabase()
  {
    return database;
  }



	/**
	 * @return Returns the racEnabled.
	 */
	public boolean isRacEnabled() {
	    return racEnabled;
	}
	/**
	 * @param racEnabled The racEnabled to set.
	 */
	public void setRacEnabled(boolean racEnabled) {
	    this.racEnabled = racEnabled;
	}
	
	
/**
 * @return Returns the hiddens.
 */
public ArrayList getHiddens() {
    return hiddens;
}
/**
 * @param hiddens The hiddens to set.
 */
public void setHiddens(ArrayList hiddens) {
    this.hiddens = hiddens;
}


/**
 * @return Returns the lookups.
 */
public ArrayList getLookups() {
    return lookups;
}
/**
 * @param lookups The lookups to set.
 */
public void setLookups(ArrayList lookups) {
    this.lookups = lookups;
}

public ArrayList getPreformats() {
	return preformats;
}

public void setPreformats(ArrayList preformats) {
	this.preformats = preformats;
}



}