package com.powerdba.chart;

import com.powerdba.XmlDAO;
import com.powerdba.util.Tracer;
import java.util.Hashtable;

public class ChartGroupHolder {

  private static Hashtable chartGroups = null;
  
	public static ChartGroup getChartGroup(String name) {
  
    ChartGroup chartGroup = null;
  
    if (ensureLoaded() ) {
      chartGroup = (ChartGroup) chartGroups.get(name.trim());
    }
		return chartGroup;
	}

	public static boolean isLoaded() {  
    return chartGroups != null;
	}

  private static boolean ensureLoaded() {

    if ( !isLoaded() ) {
    
      String fileName = "chart-groups.xml";

      Tracer.log("Loading chart definitions from file: " + fileName, Tracer.DEBUG, "ChartGroupHolder");

      try {     
        Tracer.log("Loading chart group into static memory", Tracer.DEBUG, "ChartGroupHolder");
        chartGroups = XmlDAO.getChartGroups(fileName);
      } catch (Exception e) {
        System.out.println("ChartGroupHolder: FAILED loading");
        return false;
      }
      
    }

    return true;
  }
}