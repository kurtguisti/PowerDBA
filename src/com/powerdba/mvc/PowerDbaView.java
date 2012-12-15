package com.powerdba.mvc;

import com.powerdba.ActionHolder;
import com.powerdba.LookupDisplayGroup;
import com.powerdba.MenuGenerator;
import com.powerdba.OracleDatabaseConnection;
import com.powerdba.OracleStatHolder;
import com.powerdba.OracleStatistic;
import com.powerdba.PowerDbaAction;
import com.powerdba.gui.Page;
import com.powerdba.gui.PageGenerator;
import com.powerdba.gui.Window;
import com.powerdba.mvc.PowerDbaActions;
import com.powerdba.mvc.jsp.HtmlComponent;
import com.powerdba.mvc.jsp.JspEnvironment;
import com.powerdba.mvc.jsp.SelectEntry;
import com.powerdba.util.StringUtility;
import com.powerdba.util.Tracer;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.commons.lang.StringUtils;

/**
 * This is the base class to handle the PowerDBA Display Layer
 *
 * @author kguisti
 */
public class PowerDbaView {

  public static final String FORM = "powerdba";

  private OracleDatabaseConnection database;
  private ArrayList dbList = new ArrayList();
  private ArrayList statList = new ArrayList();
  private ArrayList sessionStatusList = new ArrayList();
  private ArrayList userList = new ArrayList();
  private ArrayList moduleList = new ArrayList();
  private ArrayList sessionTypeList = new ArrayList();
  private ArrayList schemaLetterList = new ArrayList();
  private LookupDisplayGroup lookupGroup;
  private String html;
  private int currAction;
  private String currActionString;
  private String databaseTime;
  private int refreshInterval;
  private String statistic;
  private boolean connected;
  private String menu1 = null;
  private String menu2 = null ;
  private String menu3 = null;
  private long sid = 0;
  private String address = "";
  private String hash = "0";
  private String oracleUser = "";
  private String sessionStatus = "";
  private String sessionType = "";
  private String message = "";
  private ArrayList dateList = new ArrayList();
  private String currentDate;
  private StringBuffer onLoadActions = new StringBuffer();
  private StringBuffer error = new StringBuffer();
  private String title = new String();
  private Page page = new Page();
  private String sql;
  private Hashtable PowerDBARuntimeStats;
  private long scn;
  private String trx;
  private boolean multiple = false;
  private ArrayList pageSections = new ArrayList();
  private Window window = new Window();
  private JspEnvironment env;
	private long runTime;

	private ArrayList dbShortList;

  //Constructors
  public PowerDbaView(JspEnvironment env) {
  	this.env = env;
  }

  // will run page specific methods that will build each dynamic portion of the page
  
  public void build() throws Exception {
      
    if ( this.currAction != PowerDbaActions.NEW ) {  
      
	    if ( this.menu1 == null ) this.menu1 = window.getMenu1();
	    if ( this.menu2 == null ) this.menu2 = window.getMenu2();
	    if ( this.menu3 == null ) this.menu3 = window.getMenu3();
	    if ( this.title.length() == 0 ) this.title = window.getTitle();
      this.page.setDatabaseLink(this.getDatabase().getName());
	      
	    StringBuffer sb = new StringBuffer();
	  
	    Tracer.log("Initializing the PowerDbaView", Tracer.DEBUG,this); 
	    
	    if ( this.html == null ) {   // html has not been explicitly set, so build it from a Window/Page object
	
        if ( this.pageSections.size() == 0 && this.page.getRowCount() > 0 ) {
        	this.pageSections.add(this.page);
        	Tracer.log("Using the old page type object.  page size " + this.page.getRowCount(), Tracer.DEBUG, this);
        } else if ( this.pageSections.size() == 0 && window.getPages().size() > 0 ){
        	Tracer.log("Using the new Window object to display the results.  page size " + this.page.getRowCount(), Tracer.DEBUG, this);          	
        	this.pageSections.addAll(window.getPages());
        }
        
        if ( this.isMultiple() || window.isMultiple() ) {
            
		      if ( this.pageSections.size() == 0 ) this.pageSections = window.getPages();		      
		      Tracer.log("Processing " + this.getPageSections().size() + " page sections.", Tracer.DEBUG, this);
		      // Process multiple page sections as setup in the manager for certain pages.  Must have set multiple to true.
		      // and setup a pages arraylist.
			    for ( int i=0; i<this.getPageSections().size(); i++ ) {
		        Page page = (Page) this.getPageSections().get(i);
		        if ( i == 1 && window.isRac() == true ) {
	            sb.append("<font size=-1 color=#888888><b>&nbsp;Other instances in the RAC Cluster:</b></font>");
	            sb.append("<BR><BR>");
		        }
		        sb.append(buildOnePageSection(page));
		        sb.append("<br>");
		        this.html = sb.toString();
			    }				    
	      } 		    
	    }
    }
    
    Tracer.log("Menu1 " + this.getMenu1(), Tracer.DEBUG, this);
    Tracer.log("Menu2 " + this.getMenu2(), Tracer.DEBUG, this);
    Tracer.log("Menu3 " + this.getMenu3(), Tracer.DEBUG, this);  
    Tracer.log("Finished Initializing the PowerDbaView", Tracer.DEBUG,this); 
    
  }
  
  private String buildOnePageSection(Page page) throws Exception {
      
        String html = new String();
      
        Tracer.log("Building Page Section", Tracer.DEBUG,this); 

        try {
          
	        if ( page != null ) {
	      
	          PageGenerator pg = new PageGenerator(page);
	          pg.setColor1("#e5e5e5");
	          pg.setColor2("#f5f5f5");
	          pg.setHeadingBackgoundColor("#73969c");
	          pg.setCellBorderColor("#cccccc");
	          pg.setForm("powerdba");
	          pg.setJsp("powerdba.jsp");
	          pg.setDatabase(page.getDatabaseLink());
	          pg.setSql(page.getSql());
	  
	          if ( ActionHolder.getList().contains(this.currActionString ) ) {    
	            html = pg.getHtmlNew();
	          } else {
	            html = pg.getHtml();
	          }            
	        }
	      
          Tracer.log("Finished builing page section", Tracer.DEBUG, this);
          
          return html;
          
        } catch ( Exception e ) {
    	    Tracer.log(e,"Error occurred somewhere in the build() method " + e.getMessage(), Tracer.ERROR, this);
    	    throw e;
        }
      }

  // Public Getter Methods
  public String getHtml() throws Exception {
    String rval;
    if ( this.html == null ) {
      rval = "";
    } else {
      rval = this.html;
    }
    return rval;
  }

  public String getMessage() {
    return this.message;
  }
  
  public int getRefreshInterval() { return this.refreshInterval; }
  
  public String getRefreshMeta() {
    String rval = "";
    if ( this.refreshInterval > 0 ) 
      rval = "<META HTTP-EQUIV=\"Refresh\" CONTENT=\""+ this.getRefreshInterval() + 
             ";URL=" + this.getAllInstancesRefreshURL() + "\">";

    return rval;
  }
  
  public String getHTMLForInstanceLinks(ArrayList instances) {
  	StringBuffer sb = new StringBuffer();
  	String instanceName = null;
    for ( int i=0; i<instances.size(); i++ ) {
    	instanceName = (String) instances.get(i);
    	sb.append("<a href='" + getInstanceLevelRefreshURL(instanceName)+"'>"+instanceName+"</a>");
      sb.append("&nbsp;");
    }
  	return sb.toString();
  }
  
  public String getInstanceLevelRefreshURL(String instance) {
  	
	  StringBuffer url = new StringBuffer();
	
	  url.append("powerdba.jsp");
	  
		if ( StringUtils.isNumeric(env.getParameter("formaction")) ) {
		  url.append("?formaction=" + this.currAction);
	  } else {
	  	url.append("?formaction=" + this.currActionString);
	  }
	
    url.append("&database="+ instance + "&refreshinterval=" + this.refreshInterval);
    
    Enumeration<String> e = env.getReqParameterNames();
    
    ArrayList<String> filterThese = new ArrayList<String>();
    filterThese.add("formaction");
    filterThese.add("refreshinterval");
    filterThese.add("database");
    filterThese.add("allinstances");
    filterThese.add("dbapassword");
    filterThese.add("dbausername");
    
    while ( e.hasMoreElements() ) {
    	String parmName = (String) e.nextElement();
    	String parmValue = env.getParameter(parmName);
    	if ( !filterThese.contains(parmName) )
    	  url.append("&" + parmName + "=" + StringUtility.replace(parmValue, "%", "%25"));
    }
    url.append("&allinstances=N");
    
    return url.toString();
    
}
  
  public String getRefreshURL() {
  	
  	  StringBuffer url = new StringBuffer();
  	
  	  url.append("powerdba.jsp");
  	  
  		if ( StringUtils.isNumeric(env.getParameter("formaction")) ) {
  		  url.append("?formaction=" + this.currAction);
  	  } else {
  	  	url.append("?formaction=" + this.currActionString);
  	  }
  	
      url.append("&database="+ this.database.getName() + "&refreshinterval=" + this.refreshInterval);
      
      Enumeration<String> e = env.getReqParameterNames();
      
      ArrayList<String> filterThese = new ArrayList<String>();
      filterThese.add("formaction");
      filterThese.add("database");
      filterThese.add("refreshinterval");
      filterThese.add("dbapassword");
      filterThese.add("dbausername");
      
      while ( e.hasMoreElements() ) {
      	String parmName = (String) e.nextElement();
      	String parmValue = env.getParameter(parmName);
      	if ( !filterThese.contains(parmName) )
      	  url.append("&" + parmName + "=" + StringUtility.replace(parmValue, "%", "%25"));
      }
      
      return url.toString();
      
  }
  
  public String getOnlyLocalRefreshURL() {
  	
	  StringBuffer url = new StringBuffer();
	
	  url.append("powerdba.jsp");
	  
		if ( StringUtils.isNumeric(env.getParameter("formaction")) ) {
		  url.append("?formaction=" + this.currAction);
	  } else {
	  	url.append("?formaction=" + this.currActionString);
	  }
	
    url.append("&database=" + this.database.getName() + "&refreshinterval=" + this.refreshInterval
        + "&allinstances=N");    
    Enumeration<String> e = env.getReqParameterNames();
    
    ArrayList<String> filterThese = new ArrayList<String>();
    filterThese.add("formaction");
    filterThese.add("database");
    filterThese.add("refreshinterval");
    filterThese.add("dbapassword");
    filterThese.add("dbausername");
    filterThese.add("allinstances");
    
    while ( e.hasMoreElements() ) {
    	String parmName = (String) e.nextElement();
    	String parmValue = env.getParameter(parmName);
    	if ( !filterThese.contains(parmName) )
    	  url.append("&" + parmName + "=" + StringUtility.replace(parmValue, "%", "%25"));
    }
    
    return url.toString();
    
}
  
  public String getAllInstancesRefreshURL() {
  	
	  StringBuffer url = new StringBuffer();
	  
	  url.append("powerdba.jsp");
	  
		if ( StringUtils.isNumeric(env.getParameter("formaction")) ) {
		  url.append("?formaction=" + this.currAction);
	  } else {
	  	url.append("?formaction=" + this.currActionString);
	  }
	
    url.append("&database=" + this.database.getName() + "&refreshinterval=" + this.refreshInterval
    + "&allinstances=Y");
    
    Enumeration<String> e = env.getReqParameterNames();
    
    ArrayList<String> filterThese = new ArrayList<String>();
    filterThese.add("formaction");
    filterThese.add("database");
    filterThese.add("refreshinterval");
    filterThese.add("dbapassword");
    filterThese.add("dbausername");
    filterThese.add("allinstances");
    
    while ( e.hasMoreElements() ) {
    	String parmName = (String) e.nextElement();
      String parmValue = env.getParameter(parmName);
    	if ( !filterThese.contains(parmName) )
    	    url.append("&" + parmName + "=" + StringUtility.replace(parmValue, "%", "%25"));
    }

    return url.toString();
    
}
  
  public String getEnvHiddens() {
  	
	  StringBuffer sb = new StringBuffer();
	
    Enumeration<String> e = env.getReqParameterNames();
    
    ArrayList<String> doThese = new ArrayList<String>();
    doThese.add("sid");
    doThese.add("address");
    doThese.add("hash");
    doThese.add("allinstances");
    
    while ( e.hasMoreElements() ) {
    	String parmName = (String) e.nextElement();
    	String parmValue = env.getParameter(parmName);
    	if ( doThese.contains(parmName) )
    	  sb.append("<input type=\"hidden\" name=\"" + parmName + "\" value=\"" +parmValue + "\">\n");
    }
    
    return sb.toString();
    
}

  public String getError() {
    if ( error.length() == 0 ) {
        return "<br>";
    } else {
        return "Error: " + error.toString();
    }
  }

  public String getDbListHtml() throws Exception {
    return HtmlComponent.getSelect("database", 
                                   dbList, 
                                   database.getName(), 
                                   30, 
                                   "Select an Instance", 
                                   "submit()", 
                                   "smallentry", 
                                   false);
  }
  
  public ArrayList getRefreshIntervalList() {
    ArrayList iList = new ArrayList();
    iList.add(new SelectEntry("0", "None", (short)1));
    //iList.add(new SelectEntry("5", "5 Seconds", (short)4));
    iList.add(new SelectEntry("10", "10 Seconds", (short)5));
    iList.add(new SelectEntry("30", "30 Seconds", (short)6));
    iList.add(new SelectEntry("60", "1 Minute", (short)7));
    iList.add(new SelectEntry("180", "3 Minutes", (short)7));
    return iList;
  }
  
  public ArrayList getActionSelectList() {
  	ArrayList rval = new ArrayList();
  	for (int i=0; i < ActionHolder.getObjectList().size(); i++ ) {
  		PowerDbaAction pda = (PowerDbaAction) ActionHolder.getObjectList().get(i);
  		if ( pda.isExtended() ) 
  		  rval.add(new SelectEntry(pda.getName(),pda.getTitle()));
  	}
  	Tracer.log("Returning the select list for Actions.", Tracer.DEBUG, "PowerDBAView");
		return rval;
  }
  
  public ArrayList getExtendedActionLinks() {
  	ArrayList rval = new ArrayList();
  	for (int i=0; i < ActionHolder.getObjectList().size(); i++ ) {
  		PowerDbaAction pda = (PowerDbaAction) ActionHolder.getObjectList().get(i);
  		if ( pda.isExtended() ) 
  		  rval.add("<tr><td><a href='powerdba.jsp?formaction="+pda.getName()+"&"+this.getCurrentDatabase()+">"+pda.getTitle()+"</a></td></tr>");
  	}
  	Tracer.log("Returning the extended hyperlinks list for Actions.", Tracer.DEBUG, "PowerDBAView");
		return rval;
  }

  public String getTitle() { return this.title; }

  public String getOnLoadActions() { return this.onLoadActions.toString(); }
  
  public int getCurrAction() {
  	return this.currAction; 
  }
  
  public String getCurrDatabase() { return this.database.getName(); }

  public String getCurrentTime() { 
    if ( databaseTime != null ) {
      return databaseTime;
    } else { 
      return "";
    }
  }

  public String getDatabaseVersion() { 
    return database.getStringVersion();
  }

  // Public Setter Methods
  public void setDbList(ArrayList dbs) { this.dbList= dbs; }
  public void setCurrAction(int act) { this.currAction=act; }
  public void setHtml(String html) { this.html = html; }
  public void setTitle(String title) { this.title = title; }
  public void setPageTitle(String title) { this.title = title; }
  public void setDatabaseTime(String dbTime) { this.databaseTime = dbTime; }
  public void setRefreshInterval(int value) { this.refreshInterval = value; }

  // Private Methods...

  public String getHiddens() {
    return HtmlComponent.getHidden("database",database.getName());
  }

  public void setPage(Page page)
  {
    this.page = page;
  }

  public Page getPage()
  {
    return page;
  }

  public String getForm()
  {
    return FORM;
  }


  public void setStatList(ArrayList statList)
  {
    this.statList = statList;
  }


  public ArrayList getStatList()
  {
    return statList;
  }


  public void set_currAction(int currAction)
  {
    this.currAction = currAction;
  }


  public int get_currAction()
  {
    return currAction;
  }


  public void setStatistic(String statistic)
  {
    this.statistic = statistic;
  }


  public String getStatistic()
  {
    return statistic;
  }


  public ArrayList getDbList()
  {
    return dbList;
  }


  public void setConnected(boolean connected) {
    this.connected = connected;
  }

  public boolean isConnected() {
    return connected;
  }
  
  
  
  
  
  public void setMenus(String menu1, String menu2) {
    setMenu1(menu1);
    setMenu2(menu2);
  }
  
  public void setMenus(int menu1, int menu2) {
    setMenu1(menu1);
    setMenu2(menu2);
  }
  
  public void setMenus(String menu1) {
    this.menu1 = menu1;
  }
  
  public void setMenus(int menu1) {
    setMenu1(menu1);
  }


  public void setMenu1(String menu1) {
    this.menu1 = menu1;
  }
  
  public void setMenu1(int menu1) {
    this.menu1 = MenuGenerator.getMenuStringFromId(menu1);
  }


  public int getMenu1(){
  	int menuId = -1;
  	if ( menu1 != null ) {
  		menuId =  MenuGenerator.getMenuIdFromString(menu1);
  	} 
    return menuId;
  }


  public void setMenu2(String menu2){
    this.menu2 = menu2;
  }
  
  public void setMenu2(int menu2){
    this.menu2 = MenuGenerator.getMenuStringFromId(menu2);
  }


  public int getMenu2(){
  	int menuId = -1;
  	if ( menu1 != null ) {
  		menuId =  MenuGenerator.getMenuIdFromString(menu2);
  	} 
    return menuId;
  }
  

  public void setMenu3(String menu3) {
    this.menu3 = menu3;
  }
  
  public void setMenu3(int menu3) {
    this.menu3 = MenuGenerator.getMenuStringFromId(menu3);
  }


  public int getMenu3(){
  	int menuId = -1;
  	if ( menu1 != null ) {
  		menuId =  MenuGenerator.getMenuIdFromString(menu3);
  	} 
    return menuId;
  }

  public void setSid(long sid)
  {
    this.sid = sid;
  }


  public long getSid()
  {
    return sid;
  }


  public void setMessage(String message)
  {
    this.message = message;
  }


  public String get_message()
  {
    return message;
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


  public void setDateList(ArrayList dateList)
  {
    this.dateList = dateList;
  }


  public ArrayList getDateList()
  {
    return dateList;
  }


  public void setCurrentDate(String currentDate)
  {
    this.currentDate = currentDate;
  }


  public String getCurrentDate()
  {
    return currentDate;
  }



  public void setSql(String sql)
  {
    this.sql = sql;
  }
  
  public String getCurrentDatabase() 
  {
    return this.database.getName();
  }
  


  public String getSql()
  {
    return sql;
  }
  
  public String getEscapedSql(){
  
    String rval = null;
  
    if ( this.sql == null ) {
      rval = ""; 
    } else {
      rval = StringUtility.replace(sql, "\"", "&quot;");    
      //rval = StringUtility.replace(sql, "\"", "\\\"");
      rval = StringUtility.replace(rval, ">", "&gt;");
      rval = StringUtility.replace(rval, "<", "&lt;");      
    }
    return rval;
  }

  public long getPowerDBAStat(String key) {
    return Long.parseLong((String) this.getPowerDBARuntimeStats().get(key));
  }
  
  public ArrayList getPowerDBAStatList() {

    // converts hash table of name, value statistics to an arraylist of OracleStatistic Objects.
    Enumeration e = this.getPowerDBARuntimeStats().keys();
    ArrayList rval = new ArrayList();
    while ( e.hasMoreElements() ) {
      String key = (String) e.nextElement();
      OracleStatistic os = OracleStatHolder.getStatDef(key);
      os.setValue(Long.parseLong((String) this.getPowerDBARuntimeStats().get( key )));
      rval.add(os);
    }
    return rval;
  }


  public void setPowerDBARuntimeStats(Hashtable PowerDBARuntimeStats)
  {
    this.PowerDBARuntimeStats = PowerDBARuntimeStats;
  }


  public Hashtable getPowerDBARuntimeStats()
  {
    return PowerDBARuntimeStats;
  }


  public void setSessionStatusList(ArrayList sessionStatusList)
  {
    this.sessionStatusList = sessionStatusList;
  }


  public ArrayList getSessionStatusList()
  {
    return sessionStatusList;
  }


  public void setUserList(ArrayList userList)
  {
    this.userList = userList;
  }


  public ArrayList getUserList()
  {
    return userList;
  }


  public void setOracleUser(String oracleUser)
  {
    this.oracleUser = oracleUser;
  }


  public String getOracleUser()
  {
    return oracleUser;
  }


  public void setSessionStatus(String sessionStatus)
  {
    this.sessionStatus = sessionStatus;
  }


  public String getSessionStatus()
  {
    return sessionStatus;
  }


  public void setSessionTypeList(ArrayList sessionTypeList)
  {
    this.sessionTypeList = sessionTypeList;
  }


  public ArrayList getSessionTypeList()
  {
    return sessionTypeList;
  }


  public void setSessionType(String sessionType)
  {
    this.sessionType = sessionType;
  }


  public String getSessionType()
  {
    return sessionType;
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
	 * @return Returns the scn.
	 */
	public long getScn() {
	    return scn;
	}
	/**
	 * @param scn The scn to set.
	 */
	public void setScn(long scn) {
	    this.scn = scn;
	}
	
/**
 * @return Returns the trx.
 */
public String getTrx() {
    return trx;
}
/**
 * @param trx The trx to set.
 */
public void setTrx(String trx) {
    this.trx = trx;
}


/**
 * @return Returns the multiple.
 */
public boolean isMultiple() {
    return multiple;
}
/**
 * @param multiple The multiple to set.
 */
public void setMultiple(boolean multiple) {
    this.multiple = multiple;
}

/**
 * @return Returns the pageSections.
 */
public ArrayList getPageSections() {
    return pageSections;
}
/**
 * @param pageSections The pageSections to set.
 */
public void setPageSections(ArrayList pageSections) {
    this.pageSections = pageSections;
}



/**
 * @return Returns the moduleList.
 */
public ArrayList getModuleList() {
    return moduleList;
}
/**
 * @param moduleList The moduleList to set.
 */
public void setModuleList(ArrayList moduleList) {
    this.moduleList = moduleList;
}



/**
 * @return Returns the schemaLetterList.
 */
public ArrayList getSchemaLetterList() {
    return schemaLetterList;
}
/**
 * @param schemaLetterList The schemaLetterList to set.
 */
public void setSchemaLetterList(ArrayList schemaLetterList) {
    this.schemaLetterList = schemaLetterList;
}



/**
 * @return Returns the lookupGroup.
 */
public LookupDisplayGroup getLookupGroup() {
    return lookupGroup;
}
/**
 * @param lookupGroup The lookupGroup to set.
 */
public void setLookupGroup(LookupDisplayGroup lookupGroup) {
    this.lookupGroup = lookupGroup;
}

/**
 * @return Returns the window.
 */
public Window getWindow() {
    return window;
}
/**
 * @param window The window to set.
 */
public void setWindow(Window window) {
    this.window = window;
}

public String getCurrActionString() {
	return currActionString;
}

public void setCurrActionString(String currActionString) {
	this.currActionString = currActionString;
}

public String getActionOrActionString() {
	String rval;
	if ( StringUtils.isNumeric(env.getParameter("formaction")) ) {
		rval = new Integer(this.currAction).toString();
	} else {
		rval = this.currActionString;		
	}	

	return rval;
}

public void setRunTime(long l) {
	this.runTime = l;
}

public long getRunTime() {
	return this.runTime;
}

public void setShortDbList(ArrayList dbShortList) {
	// TODO Auto-generated method stub
	this.dbShortList = dbShortList;
}

public ArrayList getShortDbList() {
	return this.dbShortList;
}

}