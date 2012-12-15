package com.powerdba.jdbc;

import java.util.Hashtable;

import com.powerdba.util.Tracer;

public class DbState {
    
    // States
    public static final int UNKNOWN              = -1;
    public static final int DOWN                 = 0;
    public static final int UP                   = 1;
    public static final int CHECKING_IN_PROGRESS = 2;
    
    // Checking subStates
    public static final int CHECKING_GETCONN = 1001;
    public static final int CHECKING_PARSE   = 1002;
    public static final int CHECKING_EXEC    = 1003;
    public static final int CHECKING_FETCH   = 1004;

    // Transitions
    public static final int UPTODOWN  = 101;
    public static final int DOWNTOUP  = 102;
    public static final int UPTOWARN  = 103;
    public static final int WARNTOUP  = 104;
    public static final int UNKTOUP   = 105;
    public static final int NOCHANGE  = 106;
    public static final int UNKTODOWN = 107;
    
    private int status           = UNKNOWN;
    private int substatus        = UNKNOWN;
    private long statusDate      = 0;
    private String statusMessage = "Unknown";
    private int statusCode       = 0;
    private int transition       = 0;
    private long secondsToGet    = 0;
    private long startTime       = 0;

    public DbState() {}

    public DbState(int status, String statusMessage, int statusCode, 
    		           int transition, long secondsToGet, long startTime) {
	    this.status        = status;
	    this.statusDate    = System.currentTimeMillis();
	    this.statusMessage = statusMessage;
	    this.statusCode    = statusCode;
	    this.transition    = transition;
	    this.secondsToGet  = secondsToGet;
	    this.startTime     = startTime;
    }
    
    public Hashtable getTransitions() {
      Hashtable transitions = new Hashtable();
	    transitions.put(new Integer(101), "Up To Down");
	    transitions.put(new Integer(102), "Down To Up");
	    transitions.put(new Integer(103), "Up to Warning");
	    transitions.put(new Integer(104), "Warning to Up");
	    transitions.put(new Integer(105), "Unknown to Up");
	    transitions.put(new Integer(106), "No Change");
	    transitions.put(new Integer(107), "Unknown To Down");
	    return transitions;
    }
    
    
    
    public int getSubstatus() {
			return substatus;
		}

		public void setSubstatus(int substatus) {
			this.substatus = substatus;
		}

		public long getStartTime() {
			return startTime;
		}

		public void setStartTime(long startTime) {
			this.startTime = startTime;
		}

		/**
     * @return Returns the status.
     */
    public int getStatus() {
        return status;
    }
    /**
     * @param status The status to set.
     */
    public void setStatus(int status) {
        this.status = status;
    }
    /**
     * @return Returns the statusDate.
     */
    public long getStatusDate() {
        return statusDate;
    }
    /**
     * @param statusDate The statusDate to set.
     */
    public void setStatusDate(long statusDate) {
        this.statusDate = statusDate;
    }
    /**
     * @return Returns the statusMessage.
     */
    public String getStatusMessage() {
        return statusMessage;
    }
    /**
     * @param statusMessage The statusMessage to set.
     */
    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
    
    
    /**
     * @return Returns the statusCode.
     */
    public int getStatusCode() {
        return statusCode;
    }
    /**
     * @param statusCode The statusCode to set.
     */
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
    
    /**
     * @return Returns the transition.
     */
    public int getTransition() {
        return transition;
    }
    /**
     * @param transition The transition to set.
     */
    public void setTransition(int transition) {
        this.transition = transition;
    }
    
    /**
     * @return Returns the secondsToGet.
     */
    public long getSecondsToGet() {
        return secondsToGet;
    }
    /**
     * @param secondsToGet The secondsToGet to set.
     */
    public void setSecondsToGet(long secondsToGet) {
        this.secondsToGet = secondsToGet;
    }
    public int compareTo(Object obj) {
        
        if ( obj instanceof DbState ) {
	        
	        DbState tmp = (DbState) obj;
	
	        if ( this.statusDate < tmp.getStatusDate() ) {
	          return -1;
	        } else if ( this.statusDate > tmp.getStatusDate() ) {
	          return 1;
	        }
	        
        } else {
            Tracer.log("obj is not a DbState Object", Tracer.ERROR, this);
        }
        return 0;
    }
    

}
