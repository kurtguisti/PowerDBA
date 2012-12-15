package com.powerdba.chart;

import com.powerdba.OracleDatabaseConnection;
import com.powerdba.PerfStatDAO;
import com.powerdba.PerfStatSnapshot;
import com.powerdba.PerfStatStat;
import com.powerdba.ProcessDAO;
import com.powerdba.SnapInterval;
import com.powerdba.gui.PageGenerator;
import com.powerdba.gui.PageLoader;
import com.powerdba.gui.PageSet;
import com.powerdba.mvc.PowerDbaActions;
import com.powerdba.mvc.PresentationEnvironment;
import com.powerdba.mvc.jsp.JspEnvironment;
import com.powerdba.util.DateTranslator;
import com.powerdba.util.PropertyHolder;
import com.powerdba.util.StringUtility;
import com.powerdba.util.Tracer;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.Legend;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.PieDataset;

public class ChartGenerator {

  private static final String OBJECT_NAME = "ChartGenerator";
  
  private static final long HOUR = 3600*1000;
  private static final long MINUTE = 3600*1000/60;

  public ChartGenerator(){}
  
  public static String getChartHtml(TimeSeriesDataSet dataset, PerfStatSnapshot snap) throws Exception {
    return getChartHtml(dataset, Long.toString(System.currentTimeMillis()),"TS","NA",1000,600,null,snap,null);   
  }
  
  public static String getChartHtml(TimeSeriesDataSet dataset,
                                    String url,
                                    PerfStatSnapshot snap) throws Exception {
                                    
    return getChartHtml(dataset, Long.toString(System.currentTimeMillis()),"TS","NA",1000,600,url,snap,null);
    
  }
  
  // Time Series Chart
  public static String getChartHtml(TimeSeriesDataSet dataset,  
                                    String sessionId,
                                    String type,
                                    String name,
                                    int width,
                                    int height,
                                    String URL,
                                    PerfStatSnapshot snap,
                                    OracleDatabaseConnection database) throws IOException, Exception {

    StringBuffer sb = new StringBuffer();  
    JFreeChart chart = ChartFactory.createTimeSeriesChart(dataset.getDescription(), "Time", dataset.getUom(), 
                                                          dataset.getTimeSeries(), true, true, true);  
    chart.setBackgroundPaint(new Color(204,204,204));
    XYPlot plot = chart.getXYPlot();
    DateAxis axis = (DateAxis) plot.getDomainAxis();
    axis.setDateFormatOverride(new SimpleDateFormat(DateTranslator.CHART_MINUTE_NODATE));
  
    if ( snap != null && snap.getHtmlDescription() != null ) sb.append(snap.getHtmlDescription());
  
    try {
	    String tmpDir = PropertyHolder.getProperty("chartTmp");
	    String fileName = type + name + sessionId + System.currentTimeMillis() + ".jpg";
	    String filePath = tmpDir + "/" + fileName;
	    Tracer.log("Writing chart to file " + filePath, Tracer.DEBUG, OBJECT_NAME);      
	    ChartUtilities.saveChartAsJPEG(new File(filePath), chart, width, height);

	    if ( URL == null ) {
	      sb.append("<table><tr><td><img src='images/" + fileName + "'></td></tr></table>");
	    } else {
	      sb.append("<table><tr><td><a href='" + URL + "'><img src='images/" + fileName + "'></a></td></tr></table>");        
	    }
	    
	    OracleDatabaseConnection db = null;
	    if ( database == null )  {
	        db = new OracleDatabaseConnection();
	    } else {
	        db = database;
	    }

      // Print the Interval Bar
	    if ( snap != null && db.getVersion().getVersion1() >= 10 ) {
	        
		    int indentBar = 63;
		    int rightMargin = 8;
		    int anchorBorderAdjustment = 4 * snap.getIntervalCount(5);  // 5 * the number of 5 minute intervals
	      int barWidth = width - indentBar - rightMargin - anchorBorderAdjustment;
		    float pixelRatio = barWidth/snap.getDurationSeconds();
		    ArrayList intervals = snap.getIntervals(5);
		    
		    sb.append("<table cellspacing=0 cellpadding=0 width=" + barWidth + "><tr>");
		    sb.append("<td><img src=images/clear.gif width=" + indentBar + " height=15></td>");
		    String color1="silver";
		    String color2="gray";
		    String color = "";
		    for ( int i=1; i<=intervals.size(); i++ ) {
		        SnapInterval interval = (SnapInterval) intervals.get(i-1);
		        int segmentSize = Math.round(interval.getDuration() * pixelRatio);
		        if ( i%2 == 0 ) {
		            color=color1;
		        } else {
		            color=color2;
		        }
		        String desc = "'" + interval.getBeginTimestamp().toString() + " - " + interval.getEndTimestamp().toString() + "'";
		        sb.append("<td bgcolor=" + color + " width=" + segmentSize + ">");
		        sb.append("<a class='linksmall' onMouseOver=\"window.status=" + desc + "; return true;\" href='chartZoom.jsp?formaction="+PowerDbaActions.OWR_CHART+"&begindetail=" + interval.getBegin() + 
		                  "&enddetail=" + interval.getEnd() +
		                  "&database="+db.getName()+"&chartid=" + name + "'><img src='images/clear.gif' height=15 width=" + segmentSize + 
		                  " alt=" + desc + "></a></td>");
		    }
		    sb.append("</tr></table>");
		    
		    // DEBUGGING INFO	
		    //sb.append("<table<tr><td></td><td><font size=-2>" + StringUtility.replace(snap.toString(),"\n","<br>") + "</font></td></tr></table>");		    
		    //sb.append("<table><tr><td>Ratio= " + pixelRatio + "\n");
		    //for ( int i=0; i<intervals.size(); i++ ) {
		    //    SnapInterval interval = (SnapInterval) intervals.get(i);
		    //    int segmentSize = Math.round(interval.getDuration() * pixelRatio);
		    //    sb.append("Interval Pixel Width = " + segmentSize + "<br>");
		    //}
		    //sb.append("</td></tr></table>");
	    
	    }
	    
	  } catch ( IOException e ) {
      Tracer.log(e, "Error Saving TS Chart to disk", Tracer.DEBUG, OBJECT_NAME);
      throw e;
    } catch ( Exception e ) {
      Tracer.log(e, "Error Getting HTML for TS Chart", Tracer.DEBUG, OBJECT_NAME);
      throw e;
    }
    
    return sb.toString();
  }
  
  // Pie Chart 
  public static String getChartHtml(PieDataset dataset, 
                                    String title,  
                                    String sessionId,
                                    String type,
                                    String name, 
                                    int width,
                                    int height,
                                    String URL) throws IOException, Exception {
  
    StringBuffer sb = new StringBuffer();  

    JFreeChart jfc = null;
    Legend legend  = null;
    LegendTitle lt = null;
    //lt.
    
    try { 
    	
      Tracer.log("about to create pie chart", Tracer.DEBUG, OBJECT_NAME);       
	    jfc = ChartFactory.createPieChart3D(title, dataset, true, true, true);
	    Tracer.log("Successfully created the pie chart", Tracer.DEBUG, OBJECT_NAME);
	    jfc.setBackgroundPaint(new Color(204,204,204));

    	//legend = Legend.createInstance(jfc);
    	//legend.
	    
      String tmpDir = PropertyHolder.getProperty("chartTmp");
      String fileName = type + name + sessionId + System.currentTimeMillis() + ".jpg";
      String filePath = tmpDir + "/" + fileName;
      Tracer.log("Writing chart to file " + filePath, Tracer.DEBUG, OBJECT_NAME);      
      ChartUtilities.saveChartAsJPEG(new File(filePath), jfc, width, height);
      if ( URL == null ) {
        sb.append("<table><tr><td><img src='images/" + fileName + "'></td></tr></table>");
      } else {
        sb.append("<table><tr><td><a href='" + URL + "'><img src='images/" + fileName + "'></a></td></tr></table>");        
      }
      
    } catch ( IOException e ) {
      Tracer.log(e, "IO Error Saving Pie Chart jpg to disk", Tracer.DEBUG, OBJECT_NAME);
      throw e;
    } catch ( Exception e ) {
      Tracer.log(e, "Error Getting HTML for Pie Chart", Tracer.DEBUG, OBJECT_NAME);
    }
    
    return sb.toString();
    
  }
  
  public static String getDataHtml(TimeSeriesDataSet dataset, PerfStatSnapshot snap) throws Exception {
    
    StringBuffer sb = new StringBuffer();
    
    PageSet ps = PageLoader.buildPageSet(dataset);
    PageGenerator pg;

    try {
      pg = new PageGenerator(ps.getPage(1));
    } catch (Exception e ) {
      throw e;
    }
    pg.setColor1("#e5e5e5");
    pg.setColor2("#f5f5f5");
    pg.setHeadingBackgoundColor("#73969c");
    pg.setCellBorderColor("#cccccc");
    
    sb.append(snap.getHtmlDescription());
    sb.append(pg.getHtmlNew());
    
    return sb.toString();
  }
  
  public static String getChartGroupHtml(ChartGroup chartGroup, JspEnvironment env, OracleDatabaseConnection database) 
    throws SQLException, Exception {
    
    int hoursBack = 1;
    int topN = 5;
    
    StringBuffer sb = new StringBuffer();
    
    try {
        
      sb.append("<table cellspacing=0 cellpadding=0>");
      
      ArrayList charts;
      if ( !PerfStatDAO.isPerfstatInstalled(database) ) {
        charts = chartGroup.getNonPerfStatCharts();
      } else {
        charts = chartGroup.getCharts();
      }
      
      int cntr = 1;
      int cols = chartGroup.getColumns();
      int perfStatChartId = 0;
      
      // Loop through the list of charts in the chart group
      for ( int i=0; i<charts.size(); i++ ) {
      
        // Get the chart definition from static memory
        String chartName = (String) charts.get(i);
        Chart chart = ChartHolder.getChart(chartName); 
        
        if ( chart == null ) throw new SQLException("Chart " + chartName + " not found in xml configuration file.");
        
        // Line Break in the html
        if ( cntr == 1 || (cntr-1)%cols == 0 ) sb.append("<tr>");
  
        // Chart cell
        sb.append("<td>");
        
        ArrayList categories = null;
        PerfStatSnapshot snap = null;
        
        // If the chart query definition has PERFSTAT in it, then get the perfstat data, this is a little different since
        // we need to do time based deltas.      
        
        if ( chart.isPerfstatChart() ) { 
        
          // Extract the perfstat id from the chart query
          StringTokenizer st = new StringTokenizer(chart.getQuery(), ":");
          st.nextToken();
          perfStatChartId = Integer.parseInt((String) st.nextToken());
          
          // Get the perfstat stat definition object
          PerfStatStat statDef = null;
          try {
            statDef = (PerfStatStat) PerfStatDAO.getPerfStatStats().get(new Integer(perfStatChartId));
          } catch ( Exception e ) {
            //throw new SQLException("Error Retrieving the Perfstat definition for " + perfStatChartId);
          }
          
          // Get the snapshot object
          snap = PerfStatDAO.getSnapshot(database, 
                                         new Date(database.getDateTime().getTime() - (HOUR*1) ),
                                         new Date(database.getDateTime().getTime() + (MINUTE*5)) );                                       
          Tracer.log("Perfstat Snapshot in use is \n" + snap.toString(), Tracer.DEBUG, OBJECT_NAME);
          
          // Get the top n categories for that stat in the given time period
          // or the list from the query in the chart definition.
          // If there is a query in the chart definition then use it.
          try {
            if ( chart.getCategoryQuery() == null ) {
              categories = PerfStatDAO.getTopNCategories(statDef, snap, database, topN);
            } else {
              categories = ChartDAO.getTimeSeriesCategories(database, chart.getCategoryQuery());
            }
          } catch (SQLException e) {
            throw new SQLException("Error Getting the top N categories for " + statDef.getName());
          }
  
          // Build the timeseries collection by passing the list of categories to perfstat.
          TimeSeriesDataSet dataset = PerfStatDAO.getPerfStatTSDataset(perfStatChartId, 
                                                                       snap, 
                                                                       categories, 
                                                                       database);

          // Override the title and/or unit of measure desc if it was defined in chart.xml.  Otherwise use the generic title for the stat.                                                                            
          if ( !chart.getDescription().equals("") ) dataset.setDescription(chart.getDescription());
          if ( !chart.getUom().equals("") ) dataset.setUom(chart.getUom());
          
          // Build the html for the chart
          sb.append(ChartGenerator.getChartHtml(dataset, 
                                                env.getSessionId(),
                                                chart.getType(),
                                                chart.getName(),
                                                chartGroup.getWidth(),
                                                chartGroup.getHeight(),
                                                ChartDAO.buildChartURL(PowerDbaActions.PERFSTAT_DETAIL2, 
                                                                       database.getName(), 
                                                                       perfStatChartId, 
                                                                       dataset ),
                                                null,
                                                null));
          
          
        } else if ( chart.isOWRChart() ) {   
            
          // Instantiate a snapshot with owr related start/stop times  
          Timestamp begin = PerfStatDAO.getEarliestOwrSnap(database);
          Timestamp end = database.getDateTime();             
          PerfStatSnapshot owrSnap = new PerfStatSnapshot(begin, end);
          
          //Tracer.log("OWR Snapshot: \n" + owrSnap.toString(), Tracer.DEBUG, OBJECT_NAME);

	        // Get the list of categories from the chart definition
	        ArrayList metrics = ChartDAO.getOWRTimeSeriesMetrics(database, chart.getMetrics());
	
	        // Build the timeseries dataSet (Different if chart is cluster enabled)
	        TimeSeriesDataSet dataset;
	        if ( chart.isClusterEnabled() ) {
	          dataset = PerfStatDAO.getOWRTSDatasetRAC(metrics, owrSnap, database);
	        } else {
	          dataset = PerfStatDAO.getOWRTSDataset(metrics, owrSnap, database);
	        }
	        
	        // Override the title and/or unit of measure desc if it was defined in chart.xml.
	        
	        // If the local db is rac then display the local db name if the chart IS NOT cluster oriented
	        // If the chart IS cluster oriented, then indicate as such.
	        // Otherwise use the generic title for the stat.                                                                            
	        if ( !chart.getDescription().equals("") && chart.isClusterEnabled() && database.isRac() ) {
	          dataset.setDescription(chart.getDescription() + "");
	        } else if ( !chart.getDescription().equals("") && database.isRac() ) {
	          dataset.setDescription(chart.getDescription() + "(" + database.getName() + ")");
	        } else if ( !chart.getDescription().equals("") ) {
		        dataset.setDescription(chart.getDescription());
	        }
	        
	        if ( !chart.getUom().equals("") ) dataset.setUom(chart.getUom());
	        
          // Build the html for the chart
          sb.append(ChartGenerator.getChartHtml(dataset, 
                                                env.getSessionId(),
                                                chart.getType(),
                                                chart.getName(),
                                                chartGroup.getWidth(),
                                                chartGroup.getHeight(),
                                                ChartDAO.buildChartURL("OWR_CHART", 
                                                                       chart.getName(), 
                                                                       database.getName()),
                                                null,
                                                null));           
          
        } else if ( chart.getType().equals("PIE") ) {
          Tracer.log("Building html pie chart " + chart.getName(), Tracer.DEBUG, OBJECT_NAME);
          sb.append(ChartGenerator.getChartHtml(ChartDAO.getPieDataSet(database, chart), 
                                                chart.getDescription(), 
                                                env.getSessionId(), 
                                                chart.getType(),
                                                chart.getName(),
                                                chartGroup.getWidth(), 
                                                chartGroup.getHeight(),
                                                ChartDAO.buildChartURL(chart.getOnClick(), database.getName())));
            
		    } else if ( chart.getType().equals("TS") ) {
		      TimeSeriesDataSet dataset = new TimeSeriesDataSet(chart.getDescription(), chart.getUom(), ChartDAO.getTsDataSet(database, chart));
   	      sb.append(ChartGenerator.getChartHtml(dataset,
	                                            env.getSessionId(),
	                                            chart.getType(),
	                                            chart.getName(),
	                                            chartGroup.getWidth(),
	                                            chartGroup.getHeight(),
	                                            ChartDAO.buildChartURL(PowerDbaActions.PERFSTAT_DETAIL2, 
                                                                     database.getName(), 
	                                                                   perfStatChartId, 
	                                                                   dataset),
		                                          null,
		                                          null));

		    } else if ( chart.getType().equals("BAR") ) {
		         
		    } 
    
        
        sb.append("</td>\n");       
        cntr++;      
        if ( (cntr-1)%cols == 0 ) sb.append("</tr>\n");
        
      }
  
      sb.append("</table>");

    } catch (Exception e) {
      Tracer.log(e, "Error Generating HTML for a chart group", Tracer.ERROR, OBJECT_NAME);
      throw e;
    }
    
    return sb.toString();
  
  }
  
}