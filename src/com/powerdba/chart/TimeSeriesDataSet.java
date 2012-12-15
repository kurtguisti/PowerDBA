package com.powerdba.chart;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

public class TimeSeriesDataSet 
{
  public TimeSeriesDataSet(){}
  
  public TimeSeriesDataSet(String desc, String uom, TimeSeriesCollection tsc ) {
    this.description = desc;
    this.uom = uom;
    this.timeSeries = tsc;
  }
  
  public TimeSeriesDataSet(TimeSeriesCollection tsc ) {
      this.description = "";
      this.uom = "";
      this.timeSeries = tsc;
    }

  private String uom;
  private String description;
  private TimeSeriesCollection timeSeries;


  public void setUom(String uom)
  {
    this.uom = uom;
  }


  public String getUom()
  {
    return uom;
  }
  
  public TimeSeries getSeries(int i) {
    return this.timeSeries.getSeries(i);
  }
  
  public int getSeriesCount() {
    return this.timeSeries.getSeriesCount();
  }


  public void setDescription(String description)
  {
    this.description = description;
  }


  public String getDescription()
  {
    return description;
  }


  public void setTimeSeries(TimeSeriesCollection timeSeries)
  {
    this.timeSeries = timeSeries;
  }


  public TimeSeriesCollection getTimeSeries()
  {
    return timeSeries;
  }
  
  

}

