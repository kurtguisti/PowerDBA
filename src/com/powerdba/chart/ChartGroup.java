
package com.powerdba.chart;
import java.util.ArrayList;

public class ChartGroup {

  public ChartGroup() {}
  
  public ChartGroup(String name, int width, int height, int columns, String description) {
    this.name = name;
    this.width = width;
    this.height = height;
    this.columns = columns;
    this.description = description;
    this.charts = new ArrayList();
  }

  private String name = "";
  private int width;
  private int height;
  private int columns;
  private String description;
  private ArrayList charts;
  
  public ArrayList getNonPerfStatCharts() {
    ArrayList nonPerf = new ArrayList();
    for ( int i=0; i<charts.size(); i++ ) {
      String chartName = (String) charts.get(i);
      Chart chart = ChartHolder.getChart(chartName);
      if ( !chart.isPerfstatChart() ) nonPerf.add(chartName);
    }
    return nonPerf;
  }

  public void setName(String name)
  {
    this.name = name;
  }


  public String getName()
  {
    return name;
  }


  public void setWidth(int width)
  {
    this.width = width;
  }


  public int getWidth()
  {
    return width;
  }


  public void setHeight(int height)
  {
    this.height = height;
  }


  public int getHeight()
  {
    return height;
  }


  public void setColumns(int columns)
  {
    this.columns = columns;
  }


  public int getColumns()
  {
    return columns;
  }


  public void setCharts(ArrayList charts)
  {
    this.charts = charts;
  }


  public ArrayList getCharts()
  {
    return charts;
  }


  public void setDescription(String description)
  {
    this.description = description;
  }


  public String getDescription()
  {
    return description;
  }
    
}