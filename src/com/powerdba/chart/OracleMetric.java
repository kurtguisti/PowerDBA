package com.powerdba.chart;

public class OracleMetric {

	/**
	 * @param metricName
	 * @param groupName
	 * @param groupId
	 * @param metricId
	 */
	public OracleMetric(String metricName, String groupName, int metricId, int groupId) {
	    super();
	    this.metricName = metricName;
	    this.groupName = groupName;
	    this.groupId = groupId;
	    this.metricId = metricId;
	}
	
  public OracleMetric() {}

  private String metricName;
  private String groupName;
  private int metricId;
  private int groupId;

  

  /**
   * @return Returns the groupName.
   */
  public String getGroupName() {
      return groupName;
  }
  /**
   * @param groupName The groupName to set.
   */
  public void setGroupName(String groupName) {
      this.groupName = groupName;
  }
  /**
   * @return Returns the metricName.
   */
  public String getMetricName() {
      return metricName;
  }
  /**
   * @param metricName The metricName to set.
   */
  public void setMetricName(String metricName) {
      this.metricName = metricName;
  }

	/**
	 * @return Returns the groupId.
	 */
	public int getGroupId() {
	    return groupId;
	}
	/**
	 * @param groupId The groupId to set.
	 */
	public void setGroupId(int groupId) {
	    this.groupId = groupId;
	}
	/**
	 * @return Returns the metricId.
	 */
	public int getMetricId() {
	    return metricId;
	}
	/**
	 * @param metricId The metricId to set.
	 */
	public void setMetricId(int metricId) {
	    this.metricId = metricId;
	}
}