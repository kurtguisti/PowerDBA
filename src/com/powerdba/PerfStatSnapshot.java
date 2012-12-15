package com.powerdba;

import com.powerdba.util.DateTranslator;

import java.sql.*;
import java.util.ArrayList;

public class PerfStatSnapshot {
    
  public PerfStatSnapshot(){}

  public PerfStatSnapshot(Timestamp beginTime, long beginId, short beginLevel) {
    this.beginTime = beginTime;
    this.beginId = beginId;
    this.beginLevel = beginLevel;
  }
  
  public PerfStatSnapshot(long beginId, long endId) {
    this.beginId = beginId;
    this.endId = endId;
  }
  
  public PerfStatSnapshot(Timestamp begin, Timestamp end) {
    this.setBeginTime(begin);
    this.setEndTime(end);
  }
  
  private Timestamp beginTime;
  private Timestamp endTime;
  private long beginId =0;
  private long endId = 0;
  private short beginLevel;
  private short endLevel;
  
  public long getCount() {
    return endId - beginId;
  }
  
  public float getDurationSeconds() {
    return (endTime.getTime() - beginTime.getTime())/1000;
  }
  
  public int getIntervalCount(int minutes) {
    return this.getIntervals(minutes).size();
  }

  public void setBeginTime(Timestamp beginTime) {
    this.beginTime = beginTime;
  }
  
  public Timestamp getBeginTime() {
    return beginTime;
  }

  public void setEndTime(Timestamp endTime) {
    this.endTime = endTime;
  }

  public Timestamp getEndTime() {
    return endTime;
  }

  public void setBeginId(long beginId) {
    this.beginId = beginId;
  }

  public long getBeginId() {
    return beginId;
  }

  public void setEndId(long endId) {
    this.endId = endId;
  }

  public long getEndId()
  {
    return endId;
  }

  public void setBeginLevel(short beginLevel)
  {
    this.beginLevel = beginLevel;
  }

  public short getBeginLevel()
  {
    return beginLevel;
  }

  public void setEndLevel(short endLevel)
  {
    this.endLevel = endLevel;
  }

  public short getEndLevel()
  {
    return endLevel;
  }
  
  public ArrayList getIntervals(int minutes) {

   ArrayList rval = new ArrayList();
   
   long intervalMs = 1000 * 60 * minutes;
   
   long begin = beginTime.getTime();
   long end = endTime.getTime();
   long current = begin;
   long last = begin;
   
   for ( int i=1; current<end; i++ ) {

     if ( i > 1 ) {
       rval.add(new SnapInterval(last, current));
     }
     
     last = current;
     current =  current + intervalMs;    

   }
   
   rval.add(new SnapInterval(last, end));

   return rval;

  }
  
  public String getHtmlDescription() {
  
    return "<table><tr><td><font size=-1>Actual Report Time Range is from  <b>" + 
           DateTranslator.getStringDate(this.getBeginTime(), DateTranslator.BASIC_DATETIME_FORMAT2) + 
           "</b> to <b>" + 
           DateTranslator.getStringDate(this.getEndTime(), DateTranslator.BASIC_DATETIME_FORMAT2) + 
           "</b> a total of about <b>" + 
           (this.getEndTime().getTime() - this.getBeginTime().getTime())/1000/60 + " Minutes</font></b></td></tr></table>";
  }
  
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("Begin Id: " + this.getBeginId() + "\n");
    sb.append("End Id: " + this.getEndId() + "\n");
    sb.append("Begin Time: " + this.getBeginTime().toString() + "\n");
    sb.append("End Time: " + this.getEndTime().toString() + "\n");
    sb.append("5 minute Intervals: " + this.getIntervalCount(5) + "\n");
    sb.append("Seconds: " + this.getDurationSeconds() + "\n");
    ArrayList intervals = this.getIntervals(5);
    for ( int i=0; i<intervals.size(); i++ ) {
      SnapInterval s = (SnapInterval) intervals.get(i);
      sb.append(s.getBeginTimestamp() + " " + s.getEndTimestamp() + " " + s.getDuration() + "\n");
    }
    return sb.toString();
  }
  
}