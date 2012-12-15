package com.powerdba;

public class OracleWaitEvent {
    public OracleWaitEvent() {}
    
    public OracleWaitEvent(int eventNumber, String eventName, String p1, String p2, String p3) {
      this.eventNumber = eventNumber;
      this.eventName = eventName;
      this.p1 = p1;
      this.p2 = p2;
      this.p3 = p3;
    }

    private int eventNumber;
    private String eventName;
    private String p1;
    private String p2;
    private String p3;


    public int getEventNumber() { return this.eventNumber; }
    public String getEventName() { return this.eventName; }
    public String getP1() { return this.p1; }
    public String getP2() { return this.p2; }
    public String getP3() { return this.p3; }
    
}