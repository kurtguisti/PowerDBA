package com.powerdba.jdbc;

import com.powerdba.util.PropertyHolder;
import com.powerdba.util.Tracer;
import java.io.*;
import java.util.*;

import org.jdom.*;
import org.jdom.input.*;

public class DbConfigDAO {

    public DbConfigDAO() {}
    
    public Hashtable getHash(String fileName) throws FileNotFoundException, IOException, Exception { 
    	return getHash(fileName, true);
    }
    
    public Hashtable getHash(String fileName, boolean decrypt) throws FileNotFoundException, IOException, Exception {
    
      Vector connections = readFile(fileName, decrypt);
      Hashtable newHash = new Hashtable();

      for ( int i=0; i<connections.size(); i++) {     
        DbConfig dc = (DbConfig) connections.get(i);
        newHash.put(dc.getPoolName().toLowerCase(), dc);        
      }      
      return newHash;
    }
    
    public Hashtable getDbHash(String fileName) throws FileNotFoundException, IOException, Exception {
      
      Vector connections = readFile(fileName);
      Hashtable dbHash = new Hashtable();
      ArrayList poolList = new ArrayList();

      for ( int i=0; i<connections.size(); i++) {     
      	
        DbConfig dbConfig = (DbConfig) connections.get(i);
        
        if (dbHash.containsKey(dbConfig.getDatabaseName())) {
        	poolList = (ArrayList) dbHash.get(dbConfig.getDatabaseName());
        	poolList.add(dbConfig.getPoolName());
        	dbHash.put(dbConfig.getDatabaseName(), poolList);
        } else {
        	poolList = new ArrayList();
        	poolList.add(dbConfig.getPoolName());
        	dbHash.put(dbConfig.getDatabaseName(),poolList);
        }       

      }      
      return dbHash;
    }
    
    public Hashtable getUrlHash(String fileName) throws FileNotFoundException, IOException, Exception {
        
      Vector connections = readFile(fileName);
      Hashtable newHash = new Hashtable();

      for ( int i=0; i<connections.size(); i++) {     
        DbConfig dc = (DbConfig) connections.get(i);
        newHash.put(dc.getConnectString(), dc);        
      }      
      return newHash;
    }

    public Hashtable getInstanceHash(String fileName) throws FileNotFoundException, IOException, Exception {
        
      // TODO: This is a bit hokey in that it simply parses the JDBC URL to get the instance.
    	//       So, it assumes a certain jdbc format.
    	
    	Vector connections = readFile(fileName);
      Hashtable newHash = new Hashtable();

      for ( int i=0; i<connections.size(); i++) {     
        DbConfig dc = (DbConfig) connections.get(i);
        String url = dc.getConnectString();
        
        Enumeration e = new StringTokenizer(url,"@");
        e.nextElement();
        String part2 = (String) e.nextElement();
        
        e = new StringTokenizer(part2,":");
        for ( int j=1; e.hasMoreElements(); j++ ) {
          String parsedElement = (String) e.nextElement();
          if (j == 3) { 
             newHash.put(parsedElement.toLowerCase(), dc.getPoolName().toLowerCase());
             Tracer.log("Put " + parsedElement.toLowerCase() + "," + dc.getPoolName().toLowerCase() + " into instanceName Hashmap", Tracer.DEBUG,this);
          }
        } 
      }      
      return newHash;
    }
    
    public String saveDbConfig(DbConfig dc, String fileName) throws IOException {
    	
    	Hashtable connectionHash = null;
    	
      try {
				connectionHash = new DbConfigDAO().getHash("dbconfig.xml");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			connectionHash.put(dc.getPoolName(), dc);
			
			saveHash(connectionHash, fileName);
			
			return "Saved Definition for pool " + dc.getPoolName() + " to XML...  <BR>You must restart the Power*DBA Server for this change to take effect.";
    	
    }
    
    public String saveHash(Hashtable connectionHash, String fileName) throws IOException {   
      return saveHash(connectionHash, fileName, null);      
    }
    
    public String saveHash(Hashtable connectionHash, String fileName, String directory) throws IOException {
    
      Enumeration en = connectionHash.keys();
      String connectionsFileName = PropertyHolder.getProperty("connectionPoolPropertiesFile");
      String connectionsFullPath = null;
      boolean defaultLocation = false;
      
      if ( directory == null ) {
        directory = System.getProperty("user.dir");  // this is the default location for startup
                                                     // on a windows service it's in the WINNT/System dir.
        defaultLocation = true;
      }
      
      connectionsFullPath = directory + "/" + connectionsFileName;

      File file = new File(connectionsFullPath);
      FileWriter fw = new FileWriter(file);
      fw.write("<powerdba-access>\n");
      fw.write("  <db-connections>\n");

      while ( en.hasMoreElements() ) {
        DbConfig dc = (DbConfig) connectionHash.get( (String) en.nextElement() );
        // encrypt and encode the password and stick it back in the dc object.
        dc.setPassword(dc.getPassword());
        
        Tracer.log("Saving:\n" + dc.toString(), Tracer.DEBUG, "");
        fw.write("\n    <db>\n");
        fw.write("      <poolname>" + dc.getPoolName() + "</poolname>\n");
        fw.write("      <dbdriver>" + dc.getDbDriver() + "</dbdriver>\n");
        fw.write("      <connectstring>" + dc.getConnectString() + "</connectstring>\n");
        fw.write("      <login>" + dc.getLogin() + "</login>\n");
        fw.write("      <password>" + dc.getPassword() + "</password>\n");
        fw.write("      <email>" + (dc.isEmail()?"Y":"N") + "</email>\n");
        fw.write("      <page>" + (dc.isPage()?"Y":"N") + "</page>\n");
        fw.write("      <monitor>" + (dc.isMonitor()?"Y":"N") + "</monitor>\n");
        fw.write("      <database>" + (dc.getDatabaseName()) + "</database>\n");
        fw.write("    </db>\n");
        Tracer.log("Wrote out DbConfig: \n" + dc.toString(), Tracer.DEBUG, this);
      }
      fw.write("  </db-connections>\n");
      fw.write("</powerdba-access>\n");
      
      fw.close();
      
      String returnMessage = null;
      if ( defaultLocation ) {
        returnMessage = "Configuration Saved...";
      } else {
        returnMessage = "Configuration Saved to file " + connectionsFullPath + "...";
      }
      
      // Reload the configurations.  This will only reload the hash maps used to keep
      // track of status and generate the list of databases.  It currently will NOT reload
      // the actual pool definitions!!!!  
      ConnectionConfigurationHolder.ensureLoaded();
      //try {
      //  ConnectionManager.init();
      //} catch (SQLException e) {
        // TODO Auto-generated catch block
      //  e.printStackTrace();
      //}
      
      return returnMessage;
      
    }
    
    public Vector getOrderedConfigList(String fileName) throws FileNotFoundException, IOException, Exception {
      Vector v = this.readFile(fileName);
      Collections.sort(v, DbConfig.CASE_INSENSITIVE_DISPLAY_ORDER);
      return v;
    }
    
    public Vector readFile(String fileName) throws FileNotFoundException, IOException, Exception {
    	return readFile(fileName, true);
    }
      
    public Vector readFile(String fileName, boolean decrypt) throws FileNotFoundException, IOException, Exception {
      
      try {
      
        Vector vector = new Vector();       
        File file = new File(PropertyHolder.getProperty("connectionPoolPropertiesFile"));       
        Tracer.log("About to load dbconfig file from " + file.getCanonicalPath(), Tracer.DEBUG, this);
        FileInputStream stream = new FileInputStream(file);         
        SAXBuilder builder = new SAXBuilder(false);
        Document doc = builder.build(stream);
        
        Element configs = doc.getRootElement().getChild("db-connections");  

        if ( configs == null ) 
          throw new JDOMException("Missing outer db-connections block in the xml file");

        List configList = configs.getChildren("db");
        Tracer.log("Length of db list is " + configList.size(), Tracer.DEBUG, this);

        for ( int i=0; i < configList.size(); i++ ) {
        
          DbConfig dbConfig = new DbConfig();
        
          Element xmlConfig = (Element) configList.get(i);
          Tracer.log("Getting poolname", Tracer.DEBUG, this);
          dbConfig.setPoolName(xmlConfig.getChild("poolname").getTextTrim().toLowerCase());
          Tracer.log("Getting database", Tracer.DEBUG, this);
          dbConfig.setDatabaseName(xmlConfig.getChild("database").getTextTrim().toLowerCase());
          Tracer.log("Getting dbdriver", Tracer.DEBUG, this);
          dbConfig.setDbDriver(xmlConfig.getChild("dbdriver").getTextTrim());
          Tracer.log("Getting connectstring", Tracer.DEBUG, this);
          dbConfig.setConnectString(xmlConfig.getChild("connectstring").getTextTrim());
          dbConfig.setLogin(xmlConfig.getChild("login").getTextTrim());
          if ( decrypt ) {
            dbConfig.setPassword(xmlConfig.getChild("password").getTextTrim());
          } else {
          	dbConfig.setPassword(xmlConfig.getChild("password").getTextTrim());
          }
          Element e = xmlConfig.getChild("email");
          if ( e == null ) {
            dbConfig.setEmail(false);
          } else {
            dbConfig.setEmail(xmlConfig.getChild("email").getTextTrim().toUpperCase().equals("Y")?true:false);
          }
          
          e = xmlConfig.getChild("page");
          if ( e == null ) {
            dbConfig.setPage(false);
          } else {
            dbConfig.setPage(xmlConfig.getChild("page").getTextTrim().toUpperCase().equals("Y")?true:false);
          }
          
          e = xmlConfig.getChild("monitor");
          if ( e == null ) {
            dbConfig.setMonitor(true);  // Default to true.
          } else {
            dbConfig.setMonitor(xmlConfig.getChild("monitor").getTextTrim().toUpperCase().equals("Y")?true:false);
          }
          
          e = xmlConfig.getChild("metric-server");
          if ( e == null ) {
            dbConfig.setHasMetrics(true);  // Default to true.
          } else {
            dbConfig.setHasMetrics(xmlConfig.getChild("metric-server").getTextTrim().toUpperCase().equals("Y")?true:false);
          }
          
          vector.addElement(dbConfig);
        }
        
        return vector;
        
      } catch ( Exception e ) {
        Tracer.log(e, "Error building db configuration list from xml", Tracer.ERROR, this);
        throw e;
      }
      
    }


}
