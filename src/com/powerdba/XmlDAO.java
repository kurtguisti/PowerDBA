
package com.powerdba;

import com.powerdba.mvc.PowerDbaActions;
import com.powerdba.mvc.jsp.SelectEntry;
import com.powerdba.util.Tracer;
import com.powerdba.util.XmlHandle;
import com.powerdba.gui.Link;
import com.powerdba.gui.LinkVar;
import com.powerdba.chart.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

public class XmlDAO {

    private static final String OBJECT_NAME = "XmlDAO";

    public XmlDAO() {}

    /**
     *  Creates the hash map or Oracle wait events from the xml file
     */
    static public Hashtable getWaitEventDetails() throws JDOMException, Exception {
        
      Hashtable eventsHash = new Hashtable();

      try {
      
        XmlDAO w = new XmlDAO();
        InputStream stream = w.getClass().getResourceAsStream("waitevents.xml");

        XmlHandle xmlHandle = new XmlHandle();
        xmlHandle.setInput(stream);
        Document eventDoc = xmlHandle.getDocument();
        Element events = eventDoc.getRootElement().getChild("wait-events");  

        if ( events == null ) 
            throw new JDOMException("Missing outer events block in the waitevent.xml file");

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
    
    static public Hashtable getEnqueueDetails() throws JDOMException, Exception {
        
      Hashtable enqueuesHash = new Hashtable();

      try {
      
        XmlDAO w = new XmlDAO();
        InputStream stream = w.getClass().getResourceAsStream("waitevents.xml");

        XmlHandle xmlHandle = new XmlHandle();
        xmlHandle.setInput(stream);
        Document eventDoc = xmlHandle.getDocument();
        Element enqueues = eventDoc.getRootElement().getChild("enqueues");  

        if ( enqueues == null ) throw new JDOMException("Missing outer enqueues block in the waitevent.xml file");

        List enqueuesList = enqueues.getChildren("enqueue");
        Tracer.log("Length of enqueues list is " + enqueuesList.size(), Tracer.DEBUG, OBJECT_NAME);

        for ( int i=0; i < enqueuesList.size(); i++ ) {
        
          Element element = (Element) enqueuesList.get(i);
      
          String code = element.getChild("code").getTextTrim();
          String name = element.getChild("name").getTextTrim();
          String desc = element.getChild("description").getTextTrim();
          
          Tracer.log("Creating enqueue [" + code + "][" + name + "]", Tracer.DEBUG, OBJECT_NAME);

          enqueuesHash.put(code, new OracleEnqueue(code, name, desc));
        }
        
      } catch ( Exception e ) {
        Tracer.log(e, "Error building events list from xml", Tracer.ERROR, OBJECT_NAME);
        throw e;
      }

        Tracer.log("Events hash created...", Tracer.DEBUG, OBJECT_NAME);
        return enqueuesHash;
    }
    
    static public Hashtable getOracleStats(String fileName) throws JDOMException, Exception {

      
      Hashtable hash = new Hashtable();

      try {

        XmlDAO w = new XmlDAO();
        InputStream stream = w.getClass().getResourceAsStream(fileName);

        XmlHandle xmlHandle = new XmlHandle();
        xmlHandle.setInput(stream);
        Document eventDoc = xmlHandle.getDocument();
        Element objects = eventDoc.getRootElement().getChild("oracle-stats");  

        if ( objects == null ) 
            throw new JDOMException("Missing outer enqueues block in the oraclestats.xml file");

        List list = objects.getChildren("statistic");
        Tracer.log("Length of xml list is " + list.size(), Tracer.DEBUG, OBJECT_NAME);

        for ( int i=0; i < list.size(); i++ ) {
        
          Element element = (Element) list.get(i);
      
          String name = element.getChild("name").getTextTrim();
          String number = element.getChild("number").getTextTrim();
          String sclass = element.getChild("sclass").getTextTrim();
          String uom = element.getChild("unit-of-measure").getTextTrim();
          String multiplier = element.getChild("multiplier").getTextTrim();
          String desc = element.getChild("desc").getTextTrim();
                    
          //Tracer.log("Creating OracleStat in hash by name [" + number + "][" + name + "][" + multiplier + "]", Tracer.DEBUG, OBJECT_NAME);

          hash.put(name, 
                   new OracleStatistic(name, Long.parseLong(number), Integer.parseInt(sclass), 
                                       uom, Float.parseFloat(multiplier), desc));                                       
        }

      } catch ( Exception e ) {
        Tracer.log(e, "Error building stat list from xml", Tracer.ERROR, OBJECT_NAME);
        throw e;
      }

      Tracer.log("Stats hash created...", Tracer.DEBUG, OBJECT_NAME);
      return hash;
      
    }
    
    static public Hashtable getCharts(String fileName) throws JDOMException, Exception {

      Hashtable hash = new Hashtable();

      try {

        XmlDAO w = new XmlDAO();
        InputStream stream = w.getClass().getResourceAsStream(fileName);

        XmlHandle xmlHandle = new XmlHandle();
        xmlHandle.setInput(stream);
        Document eventDoc = xmlHandle.getDocument();
        Element objects = eventDoc.getRootElement().getChild("charts");  

        if ( objects == null ) 
            throw new JDOMException("Missing outer enqueues block in the charts.xml file");

        List list = objects.getChildren("chart");
        Tracer.log("Length of xml list is " + list.size(), Tracer.DEBUG, OBJECT_NAME);

        for ( int i=0; i < list.size(); i++ ) {
        
          Element element = (Element) list.get(i);
      
          String name = element.getAttribute("name").getValue();
          Tracer.log("Added Chart " + name, Tracer.DEBUG, OBJECT_NAME);
          String desc = element.getChild("description").getTextTrim();
          String query = element.getChild("query").getTextTrim();
          String type = element.getChild("type").getTextTrim();
          
          Element uomElement = element.getChild("uom");
          String uom = null;
          if ( uomElement != null ) {
            uom = uomElement.getTextTrim();
          }
          
          Element onClickElement = element.getChild("on-click");
          String onClick = null;
          if ( onClickElement != null ) {
            onClick = onClickElement.getTextTrim();
          }
          
          Element categoryQueryElement = element.getChild("category-query");
          String categoryQuery = null;
          if ( categoryQueryElement != null ) {
            categoryQuery = categoryQueryElement.getTextTrim();
          }
          
          Element intervalElement = element.getChild("time-interval");          
          String interval = null;
          if ( intervalElement != null ) {
            interval = intervalElement.getTextTrim();
          }
          
          Element clusterElement = element.getChild("cluster-enabled");          
          String clusterEnabled = null;
          if ( clusterElement != null ) {
            clusterEnabled = clusterElement.getTextTrim();
          } else {
            clusterEnabled = "N";
          }
          
          Element topNElement = element.getChild("topn");          
          int topN = 0;
          if ( topNElement != null ) {
            topN = Integer.parseInt(topNElement.getTextTrim());
          }
          
          Chart chart = new Chart(name, query, type, desc, uom, categoryQuery, interval, onClick, topN);
          chart.setClusterEnabled(clusterEnabled.toUpperCase().equals("Y")?true:false);
          
          // Add the metric list to the chart.  This is only for OWR charts
          Element metricsElement = element.getChild("owr-metrics");
          if ( metricsElement != null ) {
            List metricElementList = metricsElement.getChildren("metric");
            String metricId = null;
            for ( int j=0; j<metricElementList.size(); j++ ) {
              Element metricElement = (Element) metricElementList.get(j);
              metricId = metricElement.getAttribute("id").getValue();
              Tracer.log("Adding metric " + metricId + " to the chart", Tracer.DEBUG, OBJECT_NAME);
              chart.getMetrics().add(metricId);          
            }
          } else {
            Tracer.log("metricsElement is null", Tracer.DEBUG, OBJECT_NAME);
          }
          
          //Tracer.log("Loading chart \n" + chart.toString(), Tracer.DEBUG, OBJECT_NAME);
          
          hash.put(name, chart);
          
        }

      } catch ( Exception e ) {
        Tracer.log(e, "Error building chart definition hash from xml", Tracer.ERROR, OBJECT_NAME);
        throw e;
      }

      Tracer.log("Charts hash created...", Tracer.DEBUG, OBJECT_NAME);
      return hash;
      
    }
    
    static public Hashtable getChartGroups(String fileName) throws JDOMException, Exception {

      Hashtable hash = new Hashtable();

      try {

        XmlDAO w = new XmlDAO();
        InputStream stream = w.getClass().getResourceAsStream(fileName);

        XmlHandle xmlHandle = new XmlHandle();
        xmlHandle.setInput(stream);
        Document eventDoc = xmlHandle.getDocument();
        Element objects = eventDoc.getRootElement().getChild("chart-groups");  

        if ( objects == null ) throw new JDOMException("Missing outer block in the charts.xml file");

        List list = objects.getChildren("chart-group");
        Tracer.log("Length of xml list is " + list.size(), Tracer.DEBUG, OBJECT_NAME);

        for ( int i=0; i < list.size(); i++ ) {
        
          Element element = (Element) list.get(i);
      
          String name = element.getAttribute("name").getValue();
          String desc = element.getChild("description").getTextTrim();
          int width = Integer.parseInt(element.getChild("width").getTextTrim());
          int height = Integer.parseInt(element.getChild("height").getTextTrim());
          int columns = Integer.parseInt(element.getChild("columns").getTextTrim());

          ChartGroup chartGroup = new ChartGroup( name, width, height, columns, desc);
          
          Element chartsElement = element.getChild("charts");
          if ( chartsElement != null ) {
            List chartsElementList = chartsElement.getChildren("chart");
            for ( int j=0; j < chartsElementList.size(); j++ ) { 
              Element chartElement = (Element) chartsElementList.get(j);
              Tracer.log("adding chart " + chartElement.getAttribute("name").getValue(), Tracer.DEBUG, OBJECT_NAME);
              chartGroup.getCharts().add(chartElement.getAttribute("name").getValue());
            }
          }  
                    
          hash.put(name, chartGroup);                                    
        }

      } catch ( Exception e ) {
        Tracer.log(e, "Error building chart list from xml", Tracer.ERROR, OBJECT_NAME);
        throw e;
      }

      Tracer.log("Charts hash created...", Tracer.DEBUG, OBJECT_NAME);
      return hash;
      
    }
    
    static public Hashtable getQueries(String fileName) throws JDOMException, Exception {
     
      Hashtable hash = new Hashtable();

      try {
    
        XmlDAO w = new XmlDAO();
        InputStream stream = w.getClass().getResourceAsStream(fileName);

        XmlHandle xmlHandle = new XmlHandle();
        xmlHandle.setInput(stream);
        Document eventDoc = xmlHandle.getDocument();
        Element objects = eventDoc.getRootElement().getChild("queries");  

        if ( objects == null ) throw new JDOMException("Missing outer block in the " + fileName + " file");

        List queryElementList = objects.getChildren("query");
        Tracer.log("Length of query xml list is " + queryElementList.size(), Tracer.DEBUG, OBJECT_NAME);

        for ( int i=0; i < queryElementList.size(); i++ ) {
        
          Element queryElement = (Element) queryElementList.get(i);
          
          // Instantiate a Query object...
          Query query = new Query(queryElement.getChild("name").getTextTrim(),
                                  queryElement.getChild("sql").getTextTrim(),
                                  queryElement.getChild("title").getTextTrim());
          
          Element racElement = (Element) queryElement.getChild("rac-enabled");
          if ( racElement != null ) query.setRacEnabled(true);
                                  
          query.setDataType(getIntValue(queryElement.getChild("data-type")));

          // Get the bind variable definitions and add them to the query object.
          Element bindvarsElement = queryElement.getChild("bind-variables");
          if ( bindvarsElement != null ) {
	          List bindvarsElementList = bindvarsElement.getChildren("var");
	          for ( int j=0; j < bindvarsElementList.size(); j++ ) {
	            Element varElement = (Element) bindvarsElementList.get(j);
	            query.getParms().put(varElement.getChild("name").getTextTrim(),
	                                 new BindVar(varElement.getChild("name").getTextTrim(),
	                                             varElement.getChild("datatype") == null?"java.lang.String":varElement.getChild("datatype").getTextTrim(),                      
	      	                                     varElement.getChild("default-value") == null?"%":varElement.getChild("default-value").getTextTrim()
	                                 ));       
	          }
	          
	          // Build query position -> name xref
	          // This is a hashtable I use to get the name by position for backward compatibility???
	          for ( int j=0; j < bindvarsElementList.size(); j++ ) {
	            Element varElement = (Element) bindvarsElementList.get(j);
	            query.getParmPositions().put(varElement.getChild("position").getTextTrim(),
	                                         varElement.getChild("name").getTextTrim());       
	          }
          }
          
          // Get the chart definitions and add them to the query object.  If charts is not defined then don't try
          // to load the query with charts so we don't throw a null pointer exception.  If a charts element is defined,
          // then you must fully define at least one chart.
          Element chartsElement = queryElement.getChild("charts");
          if ( chartsElement != null ) {
            List chartsElementList = chartsElement.getChildren("chart");
            for ( int j=0; j < chartsElementList.size(); j++ ) { 
              Element chartElement = (Element) chartsElementList.get(j);
              query.getCharts().add(chartElement.getAttribute("name").getValue());
            }
          }
          
          // Get the hidden/filtered out columns list and create it in the query object
          Element hiddensElement = queryElement.getChild("hiddens");
          if ( hiddensElement != null ) {
            List hiddensElementList = hiddensElement.getChildren("hidden");
            for ( int j=0; j < hiddensElementList.size(); j++ ) { 
              Element hiddenElement = (Element) hiddensElementList.get(j);
              query.getHiddens().add(hiddenElement.getAttribute("column-name").getValue());
            }
          }
          
          // Get the preformatted columns list and create it in the query object
          Tracer.log("Loading preformat info from xml", Tracer.DEBUG,"XMLDAO");
          Element preformatsElement = queryElement.getChild("preformats");
          if ( preformatsElement != null ) {
            List preformatsElementList = preformatsElement.getChildren("preformat");
            for ( int j=0; j < preformatsElementList.size(); j++ ) { 
              Element preformatElement = (Element) preformatsElementList.get(j);
              query.getPreformats().add(preformatElement.getAttribute("column-name").getValue());
            }
          }
          
          // Get the Link definitions and add them to the query object.
          Element linksElement = queryElement.getChild("links");
          if ( linksElement != null ) {
	          List linksElementList = linksElement.getChildren("link");
	          for ( int j=0; j < linksElementList.size(); j++ ) {
	
	            Element linkElement = (Element) linksElementList.get(j);
	            String column = linkElement.getChild("column").getTextTrim();
	            String pageId = PowerDbaActions.getPageId(linkElement.getChild("action").getTextTrim());
	            
	            // Description
	            Element descElement = linkElement.getChild("desc");
	            String descString = null;
	            if ( descElement == null ) {
	              descString = "Get Detailed Information about this " + column + ".";
	            } else {
	              descString = descElement.getTextTrim();
	            }
	            Link link = new Link(pageId, column, descString);
	            link.setGetAltFrom(getStringValue(linkElement.getChild("get-alt-from")));
	                                 
	            // Dip down into the link vars and create child list
	            Element linkVarsElement = linkElement.getChild("linkvars");
	            List linkVarsElementList = linkVarsElement.getChildren("var");
	            for ( int k = 0; k < linkVarsElementList.size(); k++ ) {
	              Element linkVarElement = (Element) linkVarsElementList.get(k);
	              link.getVariables().add(new LinkVar(linkVarElement.getChild("from-column").getTextTrim(),
	                                                  linkVarElement.getChild("varname").getTextTrim()));
	            }
	            query.getLinks().put(column.toLowerCase(), link);
	          }
          }
                    
          Tracer.log("Creating Query in hash by name [" + query.getName() + "] titled [" + query.getTitle() + "]", Tracer.DEBUG, OBJECT_NAME);

          hash.put(query.getName(), query);
                                       
        }
        
      } catch ( Exception e ) {
        Tracer.log(e, "Error building query list from xml from file " + fileName, Tracer.ERROR, OBJECT_NAME);
        throw e;
      }

      Tracer.log("Queries hash created from xml configuration...", Tracer.DEBUG, OBJECT_NAME);
      return hash;
      
    }
    
    static public Hashtable getLookupQueries(String fileName) throws JDOMException, Exception {
        
         Hashtable hash = new Hashtable();

         try {
       
           XmlDAO w = new XmlDAO();
           InputStream stream = w.getClass().getResourceAsStream(fileName);

           XmlHandle xmlHandle = new XmlHandle();
           xmlHandle.setInput(stream);
           Document eventDoc = xmlHandle.getDocument();
           Element objects = eventDoc.getRootElement().getChild("lookup-queries");  

           if ( objects == null ) throw new JDOMException("Missing outer  block in the " + fileName + " file");

           List queryElementList = objects.getChildren("lookup-query");
           Tracer.log("Length of query xml list is " + queryElementList.size(), Tracer.DEBUG, OBJECT_NAME);

           for ( int i=0; i < queryElementList.size(); i++ ) {
           
             Element queryElement = (Element) queryElementList.get(i);           
             hash.put(queryElement.getAttribute("name").getValue(), 
                      queryElement.getChild("sql").getTextTrim());
                                          
           }
           
           return hash;
           
         } catch ( Exception e ) {
           Tracer.log(e, "Error building lookup query list from xml from file " + fileName, Tracer.ERROR, OBJECT_NAME);
           throw e;
         }
        
    }
    
    static public Hashtable getLookups(String fileName) throws JDOMException, Exception {
        
         Hashtable hash = new Hashtable();
         ArrayList lookups = new ArrayList();

         try {
       
           XmlDAO w = new XmlDAO();
           InputStream stream = w.getClass().getResourceAsStream(fileName);

           XmlHandle xmlHandle = new XmlHandle();
           xmlHandle.setInput(stream);
           Document eventDoc = xmlHandle.getDocument();
           Element objects = eventDoc.getRootElement().getChild("display-groups");  

           if ( objects == null ) 
               throw new JDOMException("Missing outer  block (<lookups>) in the " + fileName + " file");        

           List lookupGroupElementList = objects.getChildren("display-group");
           Tracer.log("Length of display groups xml list is " + lookupGroupElementList.size(), Tracer.DEBUG, OBJECT_NAME);

           for ( int i=0; i < lookupGroupElementList.size(); i++ ) {
          
               Element lookupGroupElement = (Element) lookupGroupElementList.get(i); 
   
               String lookupGroupName = lookupGroupElement.getAttribute("name").getValue();
               Element lookupsElement = lookupGroupElement.getChild("lookups");
               
               List lookupsElementList = lookupsElement.getChildren("lookup");
               Tracer.log("Length of lookups for group " + lookupGroupName + " is " + lookupsElementList.size(), Tracer.DEBUG, OBJECT_NAME);
   	           lookups = new ArrayList();
               for ( int j=0; j < lookupsElementList.size(); j++ ) {

     	           Element lookupElement = (Element) lookupsElementList.get(j);
     	           
     	           Element selectEntriesElement = lookupElement.getChild("select-entries");
                 ArrayList entries = new ArrayList();
                 
     	           if ( selectEntriesElement != null ) {
		     	           List selectEntriesList = selectEntriesElement.getChildren("select-entry");
		     	           
		     	           for ( int k=0; k<selectEntriesList.size(); k++ ) {
		     	               
		     	               Element selectEntryElement = (Element) selectEntriesList.get(k);
		     	               
		     	               entries.add(new SelectEntry(selectEntryElement.getChild("value").getTextTrim(),
		     	                                           selectEntryElement.getChild("display").getTextTrim(),
		     	                                           Short.parseShort(selectEntryElement.getChild("order").getTextTrim())));
		     	               
		     	           }
     	           }
     	           
		     	       Lookup lookup = new Lookup(lookupElement.getAttribute("name").getValue(),
                                            lookupElement.getChild("label").getTextTrim(),
                                            Integer.parseInt(lookupElement.getChild("display-priority").getTextTrim()),
                                            lookupElement.getChild("initial-value")==null?"":lookupElement.getChild("initial-value").getTextTrim());
		     	       lookup.setPrepopulatedEntries(entries);
     	           
                 lookups.add(lookup);
                 
                 Tracer.log("Added lookup list for attribute " + lookupElement.getAttribute("name").getValue(), Tracer.DEBUG, OBJECT_NAME);

     	         }  
               
               //Collections.sort(lookups);
     	         
     	         hash.put(lookupGroupName, lookups);

           }
           
           return hash;
           
         } catch ( Exception e ) {
           Tracer.log(e, "Error building lookups list from xml from file " + fileName, Tracer.ERROR, OBJECT_NAME);
           throw e;
         }
        
    }
    
    static public Hashtable getActions(String fileName) throws JDOMException, Exception {
      
      Hashtable actions = new Hashtable();
      PowerDbaAction action = null;

      try {
      
        XmlDAO w = new XmlDAO();
        InputStream stream = w.getClass().getResourceAsStream(fileName);

        XmlHandle xmlHandle = new XmlHandle();
        xmlHandle.setInput(stream);
        Document eventDoc = xmlHandle.getDocument();
        Element events = eventDoc.getRootElement().getChild("actions");  

        if ( events == null ) 
            throw new JDOMException("Missing outer actions block in the actions.xml file");

        List actionsList = events.getChildren("action");
        Tracer.log("Length of actions list is " + actionsList.size(), Tracer.DEBUG, OBJECT_NAME);

        for ( int i=0; i < actionsList.size(); i++ ) {
        
          Element element = (Element) actionsList.get(i);
      
          String name = element.getAttribute("name").getValue();
          
          boolean isRac = false;
          Element racElement = element.getChild("is-rac");
          if ( racElement != null ) {
            String isRacString = racElement.getTextTrim();
	          if ( isRacString.toUpperCase().equals("TRUE") ) {
	          	isRac = true;
	          }
          }

          List queriesElementList = element.getChild("queries").getChildren("query");
          
          ArrayList queries = new ArrayList();
          
          for ( int j=0; j<queriesElementList.size(); j++ ) {
          	Element queryElement = (Element) queriesElementList.get(j);
          	queries.add(queryElement.getAttribute("name").getValue());
          }
          
          action = new PowerDbaAction(name, 
          		                        element.getChild("title")==null?"":element.getChild("title").getTextTrim(),
          		                        element.getChild("menu1")==null?"":element.getChild("menu1").getTextTrim(),		
          		                        element.getChild("menu2")==null?"":element.getChild("menu2").getTextTrim(),
          		                        element.getChild("menu3")==null?"":element.getChild("menu3").getTextTrim(),
          		                        isRac,
          		                        queries);
          
          if ( fileName.contains("extend") ) action.setExtended(true);
          
          Tracer.log("Creating action [" + name + "]", Tracer.DEBUG, OBJECT_NAME);

          actions.put(name, action);
        }
        
      } catch ( Exception e ) {
        Tracer.log(e, "Error building actions list from xml", Tracer.ERROR, OBJECT_NAME);
        throw e;
      }

        Tracer.log("actions hash created...  Size is " + actions.size(), Tracer.DEBUG, OBJECT_NAME);
        return actions;
    }
    
    static public OracleEnqueue getEnqueueDetail(String code) throws Exception {
      OracleEnqueue oe =  (OracleEnqueue) getEnqueueDetails().get(code);
      if ( oe == null ) {
        oe = new OracleEnqueue();
      }       
      return oe;
    }
    
    private static int getIntValue(Element element) {

      int rval = 0;
      if ( element != null ) {
        rval = Integer.parseInt(element.getTextTrim());
      }
      
      return rval;
    }
    
    private static long getLongValue(Element element) {

      long rval = 0;
      if ( element != null ) {
        rval = Long.parseLong(element.getTextTrim());
      }
      
      return rval;
    }
    
    private static String getStringValue(Element element) {

      String rval = null;
      if ( element != null ) {
        rval = element.getTextTrim();
      }
      
      return rval;
    }

    public static void main(String[] args) {
        try {
            /*Hashtable hash = WaitXmlDAO.getWaitEvents();
            Enumeration enum = hash.keys();
            while ( enum.hasMoreElements() ) {
              String eventName = (String) enum.nextElement();
              System.out.println("eventName: " + eventName);
            } */
        } catch ( Exception e ) {
            Tracer.log(e, "main exception", Tracer.ERROR, "static");
        }

        System.exit(1);
    }

}
