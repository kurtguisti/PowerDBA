
package com.powerdba;


import com.powerdba.util.Tracer;

import com.powerdba.util.XmlHandle;

import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import org.jdom.*;

public class WaitDAO {

    private static final String OBJECT_NAME = "WaitDAO";

    public WaitDAO() {}
    
    static public String getWaitDetail(OracleDatabaseConnection database, 
                                       String event,
                                       String p1, 
                                       String p2, 
                                       String p3,
                                       String sid) throws SQLException, Exception {   
                                       
      Tracer.log("Building Wait Detail Html ", Tracer.DEBUG, OBJECT_NAME);
                                       
      String sql = null;
      String query = null;
      PreparedStatement pstmt = null;
      ResultSet rset          = null;
      
      OracleWaitEvent ev = null;           // This is the data from oracle regarding the wait event  
      OracleWaitEventDetail detail = null; // This is user added info about the wait event
      OracleWaitEventGroup group   = null; // This is info about the user defined group  
      boolean idleEvent = false;
      
      try {
      
        String eventInfo = "";
      
        Hashtable eventDetails = getWaitEventDetails();           
        Hashtable groups = getWaitEventGroups();        
        Hashtable dbWaits = getDBWaitEvents(database);
        
        if ( WaitDAO.idleEvents().contains(event) ) idleEvent = true;
        
        ev = (OracleWaitEvent) dbWaits.get(event);    
        detail = (OracleWaitEventDetail) eventDetails.get(event);
        
        eventInfo   += "<br><center><table width=80% bgcolor=#73969c><tr><td><left>" +
                       "<table width=100%><tr><td bgcolor='#e5e5e5'><font size='-1'>" + 
                       "<br>" +
                       "<b>Session " + sid + " is waiting on the following event:<br><br></b>&nbsp;&nbsp;" + event + "&nbsp;&nbsp;" + 
                       (idleEvent?"<font color=black>(This is considered an Idle Event.)</font>":"") + "<br>";

        if ( p1.equals("") ) {
          eventInfo += "<br><b>Explanation/Recommendations for '" + event + "':</b>";    
          if ( detail == null ) {
            eventInfo += "<br><br>&nbsp;No further information has been setup for this wait event.";
          } else {
            eventInfo += "&nbsp;<pre><font size=0 face=verdana>" + detail.getExplanation() + "</font></pre>";
          }
        } else {
                                     
          if ( detail == null ) { 
    
            eventInfo += "<br><b>Parameters:</b> <br><br>" + 
                         "&nbsp;&nbsp;" + ev.getP1() + " = <b>" + p1 + "</b><br>" + 
                         "&nbsp;&nbsp;" + ev.getP2() + " = <b>" + p2 + "</b><br> " + 
                         "&nbsp;&nbsp;" + ev.getP3() + " = <b>" + p3 + "</b><br>";
                         
            eventInfo += "<br><br>&nbsp;No further information has been setup for this wait event.";
            
          } else { 
          
            group = (OracleWaitEventGroup) groups.get((String)detail.getGroupName());
            
            if ( group != null ) {
          
	            short p1Count = group.getP1Count();
	            short p2Count = group.getP2Count();
	            short p3Count = group.getP3Count();
	            
	            eventInfo += "<b><br>Parameters:</b> <br><br>" + 
	                         "&nbsp;&nbsp;" + ev.getP1() + " = <b>" + p1 + "</b><br>" + 
	                         "&nbsp;&nbsp;" + ev.getP2() + " = <b>" + p2 + "</b><br> " + 
	                         "&nbsp;&nbsp;" + ev.getP3() + " = <b>" + p3 + "</b><br>";   
	            
	            eventInfo += "<br><b>Explanation/Recommendations for '" + event + "':</b>";        
	            eventInfo += "&nbsp;<pre><font size=0 face=verdana>" + detail.getExplanation() + "</font></pre>";      
	                
	            if ( group != null ) {
	              // get the query out of the wait event group object
	              sql = group.getQuery();
	              if ( sql.length() > 0 ) {           
	                pstmt = database.getConn().prepareStatement(sql);          
	                int boundCount = 1;
	                for ( int i=0; i < p1Count; i++ ) {
	                  pstmt.setString(boundCount, p1);
	                  boundCount++;
	                }
	                for ( int i=0; i < p2Count; i++ ) {
	                  pstmt.setString(boundCount, p2);
	                  boundCount++;
	                }
	                for ( int i=0; i < p3Count; i++ ) {
	                  pstmt.setString(boundCount, p3);
	                  boundCount++;
	                }
	                rset  = pstmt.executeQuery();
	    
	                eventInfo += "<br><b>Additional Information:</b><br>";
	                eventInfo += ProcessDAO.buildWaitDetailDisplay(rset, new Hashtable(), 
	                                                               "Session " + sid + " waiting on " + event, 5,
	                                                               group.getName(),
	                                                               database);
	                
	  
	              } else {
	                eventInfo += "<br><br>&nbsp;Power*DBA Note: No query was found for this event group." +
	                             "&nbsp; (groupName=" + group.getName() + ")<br>";
	              }
	            } else {
	              eventInfo += "<br><br>&nbsp;Warning: No group information found for this wait event.<br>";
	            }
	          }
          }
        }
        
        eventInfo += "<br></td></tr></table></left></td></tr></table></center>";          
        return eventInfo;
              
        } catch ( SQLException e ) {
          Tracer.log(e, "Database Error in building wait html " + sql, Tracer.ERROR, OBJECT_NAME);
          throw new SQLException(e.getMessage());
        } finally {
          try {
              if ( pstmt != null ) pstmt.close();
          } catch ( Exception e ) {}
        }
                                               
    }
    
    static public Hashtable getWaitEventDetails() throws JDOMException, Exception {
    
      Hashtable eventsHash = new Hashtable();

      try {
      
        WaitDAO w = new WaitDAO();
        InputStream stream = w.getClass().getResourceAsStream("waitevents.xml");

        XmlHandle xmlHandle = new XmlHandle();
        xmlHandle.setInput(stream);
        Document eventDoc = xmlHandle.getDocument();
        Element events = eventDoc.getRootElement().getChild("wait-events");  

        if ( events == null ) 
            throw new JDOMException("Missing outer events block in the wiatevent.xml file");

        List eventsList = events.getChildren("wait-event");
        Tracer.log("Length of wait-event list is " + eventsList.size(), Tracer.DEBUG, OBJECT_NAME);

        for ( int i=0; i < eventsList.size(); i++ ) {
        
          Element element = (Element) eventsList.get(i);
      
          String name = element.getChild("event-name").getTextTrim();
          String desc = element.getChild("description").getTextTrim();
          String grp =  element.getChild("event-group").getTextTrim();
          
          Tracer.log("Creating event [" + name + "][" + desc + "][" + grp + "]", Tracer.DEBUG, OBJECT_NAME);

          eventsHash.put(name, new OracleWaitEventDetail(name, grp, desc));
        }
        
      } catch ( Exception e ) {
        Tracer.log(e, "Error building events list from xml", Tracer.ERROR, OBJECT_NAME);
        throw e;
      }

        Tracer.log("Events hash created...", Tracer.DEBUG, OBJECT_NAME);
        return eventsHash;
    }
    
    static public Hashtable getWaitEventGroups() throws JDOMException, Exception {
    
        WaitDAO w = new WaitDAO();
        InputStream stream = w.getClass().getResourceAsStream("waitevents.xml");
    
        XmlHandle xmlHandle = new XmlHandle();
        xmlHandle.setInput(stream);
        Document eventDoc = xmlHandle.getDocument();

        Hashtable eventGroupsHash = new Hashtable();

        Element rootEventGroups = eventDoc.getRootElement().getChild("eventgroups");

        if ( rootEventGroups == null ) {
            throw new JDOMException("Missing outer eventgroups block in the event xml file");
        }

        List eventGroupsList = rootEventGroups.getChildren("eventgroup");

        for ( int i=0; i < eventGroupsList.size(); ++i ) {
        
            Tracer.log("About to get the element from the list", Tracer.DEBUG, OBJECT_NAME);
        
            Element elem = (Element) eventGroupsList.get(i);
            
            Tracer.log("Got the element", Tracer.DEBUG, OBJECT_NAME);
        
            String name = elem.getChild("group-name").getTextTrim();
            String desc = elem.getChild("description").getTextTrim();
            String query =  elem.getChild("query").getTextTrim();
            String p1Count = elem.getChild("p1-count").getTextTrim();
            String p2Count = elem.getChild("p2-count").getTextTrim();
            String p3Count = elem.getChild("p3-count").getTextTrim();
            Tracer.log("Creating event [" + name + "][" + desc + "][" + query + "]", Tracer.DEBUG, OBJECT_NAME);

            eventGroupsHash.put(name, new OracleWaitEventGroup(name, query, desc,
                                                               Short.parseShort(p1Count),
                                                               Short.parseShort(p2Count),
                                                               Short.parseShort(p3Count)));
        }

        Tracer.log("Event Groups hash created...", Tracer.DEBUG, OBJECT_NAME);
        return eventGroupsHash;
    }      
    
    static public Hashtable getDBWaitEvents(OracleDatabaseConnection database) throws SQLException {
    
        StringBuffer sb = new StringBuffer();

        Hashtable events = new Hashtable();
        PreparedStatement pstmt = null;
        ResultSet rset          = null;

        String SQL = "select * from v$event_name";
        
        try {    
            pstmt = database.getConn().prepareStatement(SQL);
            rset  = pstmt.executeQuery();    
            while ( rset.next() ) {
              events.put(rset.getString("name"), 
                         new OracleWaitEvent(rset.getInt("event#"),
                                             rset.getString("name"),
                                             rset.getString("parameter1"),
                                             rset.getString("parameter2"),
                                             rset.getString("parameter3")));
            }
            return events;

        } catch ( SQLException e ) {
            Tracer.log("Error in building session list", Tracer.ERROR, OBJECT_NAME);
            throw e;
        } finally {
            try {
                if ( rset != null ) rset.close(); if ( pstmt != null ) pstmt.close();
            } catch ( Exception e ) {}
        }
    }
    
    static public ArrayList idleEvents()  {
        ArrayList list = new ArrayList(); 
        list.add("smon timer");
        list.add("pmon timer");
        list.add("rdbms ipc message");
        list.add("Null event");
        list.add("parallel query dequeue");
        list.add("pipe get");
        list.add("client message");
        list.add("SQL*Net message to client");
        list.add("SQL*Net message from client");
        list.add("SQL*Net more data from client");
        list.add("dispatcher timer");
        list.add("virtual circuit status");
        list.add("lock manager wait for remote message");
        list.add("PX Idle Wait");
        list.add("PX Deq: Execution Msg");
        list.add("PX Deq: Table Q Normal");
        list.add("wakeup time manager");
        list.add("slave wait");
        list.add("i/o slave wait");
        list.add("jobq slave wait");
        list.add("null event");
        list.add("gcs remote message");
        list.add("gcs for action");
        list.add("ges remote message");
        list.add("queue messages");
        list.add("wait for unread message on broadcast channel");
        return list;        
    }
    

    
}

