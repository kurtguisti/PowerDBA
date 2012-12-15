package com.powerdba.chart;

import com.powerdba.XmlDAO;
import com.powerdba.util.PropertyHolder;
import com.powerdba.util.Tracer;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

public class ChartHolder {
    
  private static final String OBJECT_NAME = "ChartHolder";

  private static Hashtable charts = null;
  
  public static void cleanCharts (int hours) throws IOException {
     String dirName = PropertyHolder.getProperty("chartTmp");
     File directory = new File(dirName);
     File[] files = directory.listFiles();
     long cntr = 0;
     for ( int i=0; i<files.length; i++ ) {
         File f = files[i];
         if ( f.getName().substring(0,2).equals("TS") || f.getName().substring(0,3).equals("PIE") ) {
	         if ( f.lastModified() < System.currentTimeMillis() - 30000 ) {
	             f.delete();
	             cntr++;
	         }
         }
     }
     Tracer.log("Removed " + cntr + " chart images.", Tracer.DEBUG, OBJECT_NAME);
  }
  
	public static Chart getChart(String name) {
  
    Chart chart = null;
  
    if (ensureLoaded() ) {
      chart = (Chart) charts.get(name.trim());
    }
		return chart;
	}

	public static boolean isLoaded() {  
    return charts != null;
	}

  private static boolean ensureLoaded() {

    if ( !isLoaded() ) {
    
      String fileName = "charts.xml";
      Tracer.log("Loading chart definitions from file: " + fileName, Tracer.DEBUG, "ChartHolder");

      try {     
        Tracer.log("Loading chart into static memory", Tracer.DEBUG, "ChartHolder");
        charts = XmlDAO.getCharts(fileName);
      } catch (Exception e) {
        System.out.println("ChartHolder: FAILED loading from xml");
        return false;
      }
      
    }

    return true;
  }
}