package com.powerdba.util;

import java.util.*;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.TTCCLayout;


public final class Tracer {

  public static final byte OFF       = -1;
	public static final byte ERROR     = 0;
	public static final byte WARNING   = 1;
	public static final byte MAJOR     = 2;   // DEPRECATED
	public static final byte NOTICE    = 2;
	public static final byte MINOR     = 3;   // DEPRECATED
	public static final byte INFO      = 3;
	public static final byte INTERFACE = 4;   // DEPRECATED
	public static final byte DEBUG     = 5;
	public static final byte METHOD    = 5;   // DEPRECATED
	
	private static boolean initiated = false;

	
	private static Logger logger = Logger.getLogger(Tracer.class);

	/**
	 * Used by presentation objects (View and Controller types), this
	 * writes a <b> log </b> message to the log file.<p>
	 *
	 * Constants for traceLevel are in the Log interface.<p>
	 *
	 * Instance methods should pass the keyword "this" as the Object argument,
	 * while static methods should pass the class name as a String.
	 */
	static public void log(String traceMsg, byte traceLevel, String sessionId, Object obj) {
		if ( !initiated ) init();
		logger.log(getObjectDescription(obj), getPriority(traceLevel), getObjectDescription(obj) + " - " + traceMsg, null);
	}

	/**
	 * Used by presentation objects (View and Controller types), this
	 * writes the <b> contents of a Collection </b> to the log file.<p>
	 *
	 * Constants for traceLevel are in the Log interface.<p>
	 *
	 * Instance methods should pass the keyword "this" as the Object argument,
	 * while static methods should pass the class name as a String.
	 */
	static public void log(Collection collection, byte traceLevel, String sessionId, Object obj) {
		if ( !initiated ) init();
		logger.log(getObjectDescription(obj), getPriority(traceLevel), collection, null);
	}

	/**
	 * Used by presentation objects (View and Controller types), this
	 * writes the <b> contents of an Array </b> to the log file.<p>
	 *
	 * Constants for traceLevel are in the Log interface.<p>
	 *
	 * Instance methods should pass the keyword "this" as the Object argument,
	 * while static methods should pass the class name as a String.
	 */
	static public void log(Object[] array, byte traceLevel, String sessionId, Object obj) {
		if ( !initiated ) init();
		Collection collection = Arrays.asList(array);
		logger.log(getObjectDescription(obj), getPriority(traceLevel), collection, null);
	}

	/**
	 * Used by presentation objects (View and Controller types), this
	 * writes a <b> log message plus exception stack strace </b> message to the log file.<p>
	 *
	 * Constants for traceLevel are in the Log interface.<p>
	 *
	 * Instance methods should pass the keyword "this" as the Object argument,
	 * while static methods should pass the class name as a String.
	 */
	static public void log(Throwable throwable, String traceMsg, byte traceLevel, String sessionId, Object obj) {

		if ( !initiated ) init();		
		logger.log(getObjectDescription(obj), getPriority(traceLevel), getObjectDescription(obj) + " - " + traceMsg, throwable);
	}

	/**
	 * Used by objects for which a user sessionId does not apply.  This
	 * writes a <b> log </b> message to the log file.<p>
	 *
	 * Constants for traceLevel are in the Log interface.<p>
	 *
	 * Instance methods should pass the keyword "this" as the Object argument,
	 * while static methods should pass the class name as a String.
	 */
	static public void log(String traceMsg, byte traceLevel, Object obj) {
		if ( !initiated ) init();
		logger.log(getObjectDescription(obj), getPriority(traceLevel), getObjectDescription(obj) + " - " + traceMsg, null);
	}

	/**
	 * Used by objects for which a user sessionId does not apply.  This
	 * writes the <b> contents of a Collection </b> to the log file.<p>
	 *
	 * Constants for traceLevel are in the Log interface.<p>
	 *
	 * Instance methods should pass the keyword "this" as the Object argument,
	 * while static methods should pass the class name as a String.
	 */
	static public void log(Collection collection, byte traceLevel, Object obj) {
		if ( !initiated ) init();
		logger.log(getObjectDescription(obj), getPriority(traceLevel), collection, null);
	}

	/**
	 * Used by objects for which a user sessionId does not apply.  This
	 * writes the <b> contents of an Array </b> to the log file.<p>
	 *
	 * Constants for traceLevel are in the Log interface.<p>
	 *
	 * Instance methods should pass the keyword "this" as the Object argument,
	 * while static methods should pass the class name as a String.
	 */
	static public void log(Object[] array, byte traceLevel, Object obj) {
		if ( !initiated ) init();
		logger.log(getObjectDescription(obj), getPriority(traceLevel), array, null);
	}

	/**
	 * Used by objects for which a user sessionId does not apply.  This
	 * writes a <b> log message plus exception stack strace </b> message to the log file.<p>
	 *
	 * Constants for traceLevel are in the Log interface.<p>
	 *
	 */
	static public void log(Throwable throwable, String traceMsg, byte traceLevel, Object obj) {
		if ( !initiated ) init();
		logger.log(getObjectDescription(obj), getPriority(traceLevel), getObjectDescription(obj) + " - " + traceMsg, throwable);
	}

	// Tracer Class Initializer.
	static private void init() {
		
    // Init Logging
    TTCCLayout layout = new TTCCLayout();
    FileAppender appender = null;
    
    String loggingDir = PropertyHolder.getProperty("traceFileDirectory");
    String loggingFile = null;
    if ( loggingDir == null ) {
    	loggingFile = "PowerDBA.log";
    } else {
      loggingFile = loggingDir + "//" + "PowerDBA.log";
    }
    
    try {
       appender = new FileAppender(layout,loggingFile,false);
    } catch(Exception e) {}      
    logger.addAppender(appender);

    String loggingLevel = PropertyHolder.getProperty("traceVerbosity");
    if ( loggingLevel == null ) loggingLevel = "INFO";
    if ( loggingLevel.equals("WARNING") ) loggingLevel = "WARN";

    logger.setLevel(Level.toLevel(loggingLevel));
    logger.trace("Set logging level to " + loggingLevel);
    
    initiated = true;
    
  }

  
  static private Priority getPriority(byte traceLevel) {
    Priority priority = null;
  	if ( traceLevel == DEBUG || traceLevel == METHOD ) {
  		priority = Priority.DEBUG;
  	} else if ( traceLevel == ERROR ) {
  		priority = Priority.ERROR;
  	} else if ( traceLevel == WARNING ) {
  		priority = Priority.WARN;
  	} else if ( traceLevel == INFO || traceLevel == INTERFACE ) {
  		priority = Priority.INFO;
  	} else {
  		priority = Priority.INFO;
  	}
  	return priority;
  }
  
  private static String getObjectDescription(Object obj) {
  	String rval = null;
	  if ( obj.getClass().getName().equals("java.lang.String") ) {
			rval = obj.toString();
		} else {
			rval = obj.getClass().getName();
		}
	  return rval;
  }
  

  static public void main(String argv[]) {  
  	Tracer.log("This is a message",Tracer.DEBUG,"Tracer Test");
  	
  }
	

}
