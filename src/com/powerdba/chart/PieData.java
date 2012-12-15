package com.powerdba.chart;

import java.util.Date;
import java.util.Map;
import java.io.Serializable;
import org.jfree.data.DefaultCategoryDataset;
import de.laures.cewolf.DatasetProduceException;
import de.laures.cewolf.DatasetProducer;

import com.powerdba.util.Tracer;

//import org.jfree.chart.tooltips.CategoryToolTipGenerator;
public class PieData implements DatasetProducer, Serializable {    
    
  static final long serialVersionUID = 0;

  // These values would normally not be hard coded but produced by    
  // some kind of data source like a database or a file    
  private final String[] categories =    {"mon", "tue", "wen", "thu", "fri", "sat", "sun"};    
  private final String[] seriesNames =    {"cewolfset.jsp", "tutorial.jsp", "testpage.jsp", "performancetest.jsp"};    
  private final Integer[] [] values = new Integer[seriesNames.length] [categories.length];    

  public Object produceDataset(Map params) throws DatasetProduceException {
       
	DefaultCategoryDataset dataset = new DefaultCategoryDataset();        
	for (int series = 0; series < seriesNames.length; series ++) {            
	  int lastY = (int)(Math.random() * 1000 + 1000);            
	  for (int i = 0; i < categories.length; i++) {                
	    final int y = lastY + (int)(Math.random() * 200 - 100);                
	    lastY = y;                
	    dataset.addValue((double)y, seriesNames[series], categories[i]);            
	  }        
	}        
    	
    	return dataset;    
    }	
    
  public boolean hasExpired(Map params, Date since) {		        
    Tracer.log(getClass().getName() + "hasExpired()", Tracer.DEBUG, this);		
    return (System.currentTimeMillis() - since.getTime())  > 5000;	
  }
  
  public String getProducerId() {		
    return "PageViewCountData DatasetProducer";	
  }

}