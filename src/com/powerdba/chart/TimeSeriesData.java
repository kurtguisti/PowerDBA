package com.powerdba.chart;

import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.io.Serializable;

import de.laures.cewolf.DatasetProduceException;
import de.laures.cewolf.DatasetProducer;

import com.powerdba.util.Tracer;

//import org.jfree.chart.tooltips.CategoryToolTipGenerator;
public class TimeSeriesData implements DatasetProducer, Serializable {    
    
  static final long serialVersionUID = 0;

  public Object produceDataset(Map params) throws DatasetProduceException {
      
    String dbName = (String) params.get("dbName");
    String chartName = (String) params.get("chartName");
    
		try {
	    return ChartDAO.getTsDataSet(dbName, chartName);
		} catch (SQLException e) {
	    Tracer.log(e, "Error getting the dataset from the ChartDAO for " + chartName, Tracer.ERROR, this);
	    throw new DatasetProduceException(e.getMessage());
		}    
	}	
    
  public boolean hasExpired(Map params, Date since) {		        
    Tracer.log(getClass().getName() + "hasExpired()", Tracer.DEBUG, this);		
    return (System.currentTimeMillis() - since.getTime())  > 5000;	
  }
  
  public String getProducerId() {		
    return "PageViewCountData DatasetProducer";	
  }

}