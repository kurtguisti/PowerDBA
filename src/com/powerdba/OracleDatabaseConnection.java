package com.powerdba;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import com.powerdba.jdbc.ConnectionManager;
import com.powerdba.util.Tracer;

public class OracleDatabaseConnection {

  public OracleDatabaseConnection() {}
  
  public OracleDatabaseConnection(String name, OracleVersion version, OracleVersion compatible, String database, String instance) {
    this.name = name;
    this.version = version;
  }
  
  public OracleDatabaseConnection(String name) {
	  this.name = name;
	  try {
      this.conn = ConnectionManager.getConnection(name);
    } catch (SQLException e) {
      Tracer.log(e, "SQL Error Getting Connection to " + name, Tracer.ERROR, this);
    } catch (IOException e) {
      Tracer.log(e, "IO Error Getting Connection to " + name, Tracer.ERROR, this);
    }
	  this.pooledConnection = true;
  }
  
  private String name = "";
  private OracleVersion version = new OracleVersion();
  private String stringVersion = "";
  private OracleVersion compatible = new OracleVersion();
  private String database = "";
  private String instance = "";
  private boolean rac = false;
  private boolean hasCapture = false;
  private boolean hasApply = false;
  private boolean hasProp = false;
  private boolean hasMetrics = false;
  private boolean pooledConnection;
  private int instanceCount = 1;
  private ArrayList otherRacInstances = new ArrayList();
  private ArrayList otherRacDescriptors = new ArrayList();
  private Timestamp dateTime = new Timestamp(0);
  private Connection conn;
  
  public String toString() {
      
        StringBuffer sb = new StringBuffer();
        
        sb.append("Oracle Database: \n");
        sb.append("  Connect Descriptor Name  : " + this.name + "\n");
        sb.append("  Version                  : " + this.version.toString() + "\n");
        sb.append("  Compatible               : " + this.compatible.toString() + "\n");
        sb.append("  database name            : " + this.database + "\n");
        sb.append("  instance name            : " + this.instance + "\n");
        sb.append("  is rac?                  : " + ((this.rac)?"Yes":"No") + "\n");
        sb.append("  is pooled?               : " + ((this.pooledConnection)?"Yes":"No") + "\n");
        sb.append("  has capture?             : " + ((this.hasCapture)?"Yes":"No") + "\n");
        sb.append("  has apply?               : " + ((this.hasApply)?"Yes":"No") + "\n");
        sb.append("  has prop?                : " + ((this.hasProp)?"Yes":"No") + "\n");
        sb.append("  time                     : " + this.dateTime);
        sb.append("  other instances          : " );
        for ( int i=0; i<this.getOtherRacInstances().size(); i++ ) {
          sb.append((String) this.getOtherRacInstances().get(i) + ",");
        }
        
        sb.append("  other descriptors          : " );
        for ( int i=0; i<this.getOtherRacDescriptors().size(); i++ ) {
          sb.append((String) this.getOtherRacDescriptors().get(i) + ",");
        }
        
        return sb.toString();
  }
  
  public String toHtml() {
  
    StringBuffer sb = new StringBuffer();
    
    sb.append("<table>");
    sb.append("<tr>");
    sb.append("<th><font size=-1>Database Name:</font></th><td><font size=-1>"+this.name+"</font><td>");
    sb.append("</tr>");
    sb.append("<tr>");
    sb.append("<th><font size=-1>Version:</font></th><td><font size=-1>"+this.version+"</font><td>");
    sb.append("</tr>");
    sb.append("</table>");
    
    return sb.toString();
  }

  public void close() {
    try {
			this.conn.close();
		} catch (SQLException e) {}
  }
  
  public void setName(String name)
  {
    this.name = name;
  }


  public String getName()
  {
    return name;
  }


  public void setVersion(OracleVersion version)
  {
    this.version = version;
  }


  public OracleVersion getVersion()
  {
    return version;
  }


  public void setCompatible(OracleVersion compatible)
  {
    this.compatible = compatible;
  }


  public OracleVersion getCompatible()
  {
    return compatible;
  }


  public void setDatabase(String database)
  {
    this.database = database;
  }


  public String getDatabase()
  {
    return database;
  }


  public void setInstance(String instance)
  {
    this.instance = instance;
  }


  public String getInstance()
  {
    return instance;
  }
  
  public boolean hasMetrics()
  {
    return hasMetrics;
  }
  
  public void setHasMetrics(boolean hasMetrics)
  {
    this.hasMetrics = hasMetrics;
  }


  public void setConn(Connection conn)
  {
    this.conn = conn;
  }


  public Connection getConn()
  {
    return conn;
  }


  public void setStringVersion(String stringVersion)
  {
    this.stringVersion = stringVersion;
  }


  public String getStringVersion()
  {
    return stringVersion;
  }
  


  public void setRac(boolean rac)
  {
    this.rac = rac;
  }


  public boolean isRac()
  {
    return this.getOtherRacInstances().size()>1?true:false;
  }


  public void setPooledConnection(boolean pooledConnection)
  {
    this.pooledConnection = pooledConnection;
  }


  public boolean isPooledConnection()
  {
    return pooledConnection;
  }


public String getOtherInstancesString() {
    StringBuffer sb = new StringBuffer();
    for ( int i=0; i<this.getOtherRacInstances().size(); i++ ) {
        sb.append((String) this.getOtherRacInstances().get(i) + " ");
    }
    return sb.toString();
}

public String getOtherDescriptorsString() {
    StringBuffer sb = new StringBuffer(" ");
    for ( int i=0; i<this.getOtherRacDescriptors().size(); i++ ) {
        sb.append((String) this.getOtherRacDescriptors().get(i) + " ");
    }
    return sb.toString();
}

//TODO: Change this to be HTML with links to the db_session action.
public String getOtherDescriptorsStringHTML() {
  StringBuffer sb = new StringBuffer(" ");
  for ( int i=0; i<this.getOtherRacDescriptors().size(); i++ ) {
      sb.append((String) this.getOtherRacDescriptors().get(i) + " ");
  }
  return sb.toString();
}

/**
 * @return Returns the otherRacDescriptors.
 */
public ArrayList getOtherRacDescriptors() {
    return otherRacDescriptors;
}
/**
 * @param otherRacDescriptors The otherRacDescriptors to set.
 */
public void setOtherRacDescriptors(ArrayList otherRacDescriptors) {
    this.otherRacDescriptors = otherRacDescriptors;
}
/**
 * @return Returns the otherRacInstances.
 */
public ArrayList getOtherRacInstances() {
    return otherRacInstances;
}

public boolean isCluster() {
	  return this.getInstanceCount()==1?false:true;
}
/**
 * @param otherRacInstances The otherRacInstances to set.
 */
public void setOtherRacInstances(ArrayList otherRacInstances) {
    this.otherRacInstances = otherRacInstances;
}
/**
 * @return Returns the hasApply.
 */
public boolean isHasApply() {
    return hasApply;
}
/**
 * @param hasApply The hasApply to set.
 */
public void setHasApply(boolean hasApply) {
    this.hasApply = hasApply;
}
/**
 * @return Returns the hasCapture.
 */
public boolean isHasCapture() {
    return hasCapture;
}
/**
 * @param hasCapture The hasCapture to set.
 */
public void setHasCapture(boolean hasCapture) {
    this.hasCapture = hasCapture;
}
/**
 * @return Returns the hasProp.
 */
public boolean isHasProp() {
    return hasProp;
}
/**
 * @param hasProp The hasProp to set.
 */
public void setHasProp(boolean hasProp) {
    this.hasProp = hasProp;
}


/**
 * @return Returns the intanceCount.
 */
public int getInstanceCount() {
	  return this.getOtherRacDescriptors().size() + 1;
}
/**
 * @param intanceCount The intanceCount to set.
 */
public void setInstanceCount(int instanceCount) {
    this.instanceCount = instanceCount;
}

/**
 * @return Returns the dateTime.
 */
public Timestamp getDateTime() {
    return dateTime;
}
/**
 * @param dateTime The dateTime to set.
 */
public void setDateTime(Timestamp dateTime) {
    this.dateTime = dateTime;
}



public ArrayList getAllDescriptors() {
  ArrayList list = new ArrayList();
  list.add(this.getName());
  list.addAll(this.getOtherRacDescriptors());
  return list;
}
}