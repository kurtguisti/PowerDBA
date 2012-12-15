
package com.powerdba.chart;

import java.util.ArrayList;

public class Chart {
    

    public Chart() {}
    
    public Chart(String name, String query, String type, String description, 
                 String uom, String categoryQuery, String timeInterval, String onClick, int topN) {
      this.name = name;
      this.query = query;
      this.type = type;
      this.description = description;
      this.uom = uom;
      this.categoryQuery = categoryQuery;
      this.timeInterval = timeInterval;
      this.onClick = onClick;
      this.topN = topN;
    }
    
    private String name = "";
    private String query = "";
    private String categoryQuery;
    private String type = "";
    private String path = "";
    private String description = "";
    private String uom;
    private String timeInterval;
    private String onClick;
    private int topN;
    private ArrayList metrics = new ArrayList();
    private boolean clusterEnabled = false;


  public void setName(String name) {
    this.name = name;
  }
  
  public boolean isPerfstatChart () {
    return this.getQuery().toUpperCase().indexOf("PERFSTAT:") != -1;
  }
  
  public boolean isOWRChart () {
      return this.getQuery().equals("OWR");
  }


  public String getName()
  {
    return name;
  }


  public void setQuery(String query)
  {
    this.query = query;
  }


  public String getQuery()
  {
    return query;
  }


  public void setType(String type)
  {
    this.type = type;
  }


  public String getType()
  {
    return type;
  }


  public void setPath(String path)
  {
    this.path = path;
  }


  public String getPath()
  {
    return path;
  }


  public void setDescription(String description)
  {
    this.description = description;
  }


  public String getDescription()
  {
    return description;
  }


  public void setUom(String uom)
  {
    this.uom = uom;
  }


  public String getUom()
  {
    return uom;
  }


  public void setCategoryQuery(String categoryQuery)
  {
    this.categoryQuery = categoryQuery;
  }


  public String getCategoryQuery()
  {
    return categoryQuery;
  }


  public void setTimeInterval(String timeInterval)
  {
    this.timeInterval = timeInterval;
  }


  public String getTimeInterval()
  {
    return timeInterval;
  }


  public void setOnClick(String onClick)
  {
    this.onClick = onClick;
  }


  public String getOnClick()
  {
    return onClick;
  }


  public void setTopN(int topN)
  {
    this.topN = topN;
  }


  public int getTopN()
  {
    return topN;
  }

    
	/**
	 * @return Returns the metrics.
	 */
	public ArrayList getMetrics() {
	    return metrics;
	}
	/**
	 * @param metrics The metrics to set.
	 */
	public void setMetrics(ArrayList metrics) {
	    this.metrics = metrics;
	}
	
	
    /**
     * @return Returns the clusterEnabled.
     */
    public boolean isClusterEnabled() {
        return clusterEnabled;
    }
    /**
     * @param clusterEnabled The clusterEnabled to set.
     */
    public void setClusterEnabled(boolean clusterEnabled) {
        this.clusterEnabled = clusterEnabled;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Chart " + this.getName() + "\n");
        sb.append("rac-enabled? " + this.isClusterEnabled() + "\n");
        return sb.toString();
    }
}