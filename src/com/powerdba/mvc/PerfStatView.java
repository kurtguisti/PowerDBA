/*
 * Created by IntelliJ IDEA.
 * User: kguisti
 * Date: Oct 23, 2002
 * Time: 5:35:28 PM
 * To change template for new class use 
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.powerdba.mvc;

import com.powerdba.OracleDatabaseConnection;
import com.powerdba.OracleVersion;
import com.powerdba.PerfStatSnapshot;

import java.sql.Timestamp;
import java.text.*;
import java.util.*;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.PreparedStatement;

import com.powerdba.mvc.*;
import com.powerdba.mvc.jsp.*;
import com.powerdba.util.*;

/**
 * This is the base class to handle the PowerDBA Display Layer
 *
 * @author kguisti
 */
public class PerfStatView {

  private static final String DETAILFONT = "verysmallentry";
  private static final String FONT = "smallentry";
  private static final boolean debug = true;

  private int currAction;
  private OracleDatabaseConnection database;
  private ArrayList statList;
  private Date dateTime;
  private Date dateTime2;
  private String offSetHours;
  private String html;
  private String stat;
  private String message = "";
  private String title = new String();
  private boolean isConnected;
  private ArrayList dbList;
  private Timestamp databaseTime;
  private String databaseVersion;
  private int refreshInterval;
  private long dateTimeLong;
  private ArrayList beginSnapList;
  private ArrayList endSnapList;
  private long beginSnapId;
  private long endSnapId;
  private OracleVersion version;
  private OracleVersion compatible;
  private long sid;
  private String address = "";
  private String hash = "0";
  private String pageType;
  private PerfStatSnapshot snapshot;
  private String refreshURL = "";

  private StringBuffer onLoadActions = new StringBuffer();

  private Connection   conn;

  //Constructors
  public PerfStatView(JspEnvironment env) {}

  // will run page specific methods that will build each dynamic portion of the page
  public void build() throws Exception {
      Tracer.log("Building PerfStat View", Tracer.DEBUG,this);

      try {

      } catch ( Exception e ) {
          Tracer.log(e,"Error occurred somewhere in the build() method " + e.getMessage(), Tracer.ERROR, this);
          throw e;
      }
  }


  // Private Methods...
  

  

  public String getOnLoadActions() { return this.onLoadActions.toString(); }

  public void setStatList(ArrayList statList)
  {
    this.statList = statList;
  }


  public ArrayList getStatList()
  {
    return statList;
  }

  public void setHtml(String html)
  {
    this.html = html;
  }


  public String getHtml()
  {
    return html;
  }


  public void setStat(String stat)
  {
    this.stat = stat;
  }


  public String getStat()
  {
    return stat;
  }


  public void setMessage(String message)
  {
    this.message = message;
  }


  public String getMessage()
  {
    return message;
  }


  public void setIsConnected(boolean isConnected)
  {
    this.isConnected = isConnected;
  }


  public boolean isConnected()
  {
    return isConnected;
  }


  public void setDbList(ArrayList dbList)
  {
    this.dbList = dbList;
  }


  public ArrayList getDbList()
  {
    return dbList;
  }



  public String getCurrDatabase()
  {
    return this.database.getName();
  }


  public void setCurrAction(int currAction)
  {
    this.currAction = currAction;
  }


  public int getCurrAction()
  {
    return currAction;
  }


  public void setDatabaseTime(Timestamp databaseTime)
  {
    this.databaseTime = databaseTime;
  }


  public String getCurrentTime()
  {
    return DateTranslator.getStringDate(this.databaseTime, DateTranslator.BASIC_DATETIME_FORMAT);
  }


  public void setDatabaseVersion(String version)
  {
    this.databaseVersion = version;
  }


  public String getDatabaseVersion()
  {
    return databaseVersion;
  }
  
  public ArrayList getRefreshIntervalList() {
    ArrayList iList = new ArrayList();
    iList.add(new SelectEntry("0", "None", (short)1));
    iList.add(new SelectEntry("5", "5 Secs", (short)2));
    iList.add(new SelectEntry("10", "10 Secs", (short)3));
    iList.add(new SelectEntry("30", "30 Secs", (short)4));
    iList.add(new SelectEntry("60", "1 Min", (short)5));
    return iList;
  }


  public void setDateTime(Date dateTime)
  {
    this.dateTime = dateTime;
  }


  public Date getDateTime()
  {
    return dateTime;
  }


  public void setOffSetHours(String offSetHours)
  {
    this.offSetHours = offSetHours;
  }


  public String getOffSetHours()
  {
    return offSetHours;
  }


  public void setDateTimeLong(long dateTimeLong)
  {
    this.dateTimeLong = dateTimeLong;
  }


  public long getDateTimeLong()
  {
    return dateTimeLong;
  }


  public void setBeginSnapList(ArrayList beginSnapList)
  {
    this.beginSnapList = beginSnapList;
  }


  public ArrayList getBeginSnapList()
  {
    return beginSnapList;
  }


  public void setEndSnapList(ArrayList endSnapList)
  {
    this.endSnapList = endSnapList;
  }


  public ArrayList getEndSnapList()
  {
    return endSnapList;
  }


  public void setBeginSnapId(long beginSnapId)
  {
    this.beginSnapId = beginSnapId;
  }


  public long getBeginSnapId()
  {
    return beginSnapId;
  }


  public void setEndSnapId(long endSnapId)
  {
    this.endSnapId = endSnapId;
  }


  public long getEndSnapId()
  {
    return endSnapId;
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


  public void setSid(long sid)
  {
    this.sid = sid;
  }


  public long getSid()
  {
    return sid;
  }


  public void setAddress(String address)
  {
    this.address = address;
  }


  public String getAddress()
  {
    return address;
  }


  public void setHash(String hash)
  {
    this.hash = hash;
  }


  public String getHash()
  {
    return hash;
  }


  public void setRefreshInterval(int refreshInterval)
  {
    this.refreshInterval = refreshInterval;
  }


  public int getRefreshInterval()
  {
    return refreshInterval;
  }


  public void setPageType(String pageType)
  {
    this.pageType = pageType;
  }


  public String getPageType()
  {
    return pageType;
  }


  public void set_dateTime(Date dateTime)
  {
    this.dateTime = dateTime;
  }


  public Date get_dateTime()
  {
    return dateTime;
  }


  public void setDateTime2(Date dateTime2)
  {
    this.dateTime2 = dateTime2;
  }


  public Date getDateTime2()
  {
    return dateTime2;
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
	 * @return Returns the snapshot.
	 */
	public PerfStatSnapshot getSnapshot() {
	    return snapshot;
	}
	/**
	 * @param snapshot The snapshot to set.
	 */
	public void setSnapshot(PerfStatSnapshot snapshot) {
	    this.snapshot = snapshot;
	}

	/**
	 * @return Returns the title.
	 */
	public String getTitle() {
	    return title;
	}
	/**
	 * @param title The title to set.
	 */
	public void setTitle(String title) {
	    this.title = title;
	}
	
	
/**
 * @return Returns the refreshURL.
 */
public String getRefreshURL() {
    return refreshURL;
}

public long getRunTime() {
	return 0;
}

public ArrayList getActionSelectList() {
	return new ArrayList();
}

/**
 * @return Returns the refreshURL.
 */
public String getAllInstancesRefreshURL() {
    return "";
}
/**
 * @param refreshURL The refreshURL to set.
 */
public void setRefreshURL(String refreshURL) {
    this.refreshURL = refreshURL;
}
}

