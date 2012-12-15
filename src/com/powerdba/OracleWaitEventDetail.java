package com.powerdba;

public class OracleWaitEventDetail {
    public OracleWaitEventDetail() {}
    
    public OracleWaitEventDetail(String eventName, 
                                 String groupName, 
                                 String explanation) {
      this.eventName = eventName;
      this.groupName = groupName;
      this.explanation = explanation;
    }

    private String eventName;
    private String groupName;
    private String explanation;
    
    public void setEventName(String value) { this.eventName = value; }
    public void setGroupName(String value) { this.groupName = value; }
    public void setExplanation(String value) { this.explanation = value; }

    public String getEventName() { return this.eventName; }
    public String getGroupName() { return this.groupName; }
    public String getExplanation() { return this.explanation; }
    
}