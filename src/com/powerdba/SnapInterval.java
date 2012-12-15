package com.powerdba;

import java.sql.Timestamp;

public class SnapInterval {
    public SnapInterval() {}
    
    public SnapInterval(long begin, long end) {
        this.begin = begin;
        this.end = end;
    }

    private long begin;
    private long end;
    
    public Timestamp getBeginTimestamp() {
        return new Timestamp(this.getBegin());
    }
    
    public Timestamp getEndTimestamp() {
        return new Timestamp(this.getEnd());
    }

    /**
     * @return Returns the begin.
     */
    public long getBegin() {
        return begin;
    }
    /**
     * @param begin The begin to set.
     */
    public void setBegin(long begin) {
        this.begin = begin;
    }
    /**
     * @return Returns the end.
     */
    public long getEnd() {
        return end;
    }
    /**
     * @param end The end to set.
     */
    public void setEnd(long end) {
        this.end = end;
    }
    
    public long getDuration() {
      return (this.end - this.begin) / 1000;
    }
}

