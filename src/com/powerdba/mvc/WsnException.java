
/*
 * (C) Copyright 2001 StreamWorks Technologies
 * All rights reserved
 * This software is the property of SWT.
 * All use, reproduction, modification, or
 * distribution of this software is only permitted
 * in strict compliance with an express written
 * agreement with SWT.
 * This software contains and implements SWT
 * PROPRIETARY INFORMATION
 * Use or disclosure of SWT PROPRIETARY INFORMATION
 * is only permitted in strict compliance with an
 * express written agreement with SWT
 */

package com.powerdba.mvc;

public class WsnException extends Exception {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
		protected String title;
    protected String header;
    protected String details = "None Defined";
    protected String url;
    protected int    target;
    protected int    action;
    protected Throwable t;

    public WsnException(int target, int action) {
        this.target = target;
        this.action = action;
        url = "/error.jsp";
    }

    public WsnException(int target, int action, String url) {

        this.target = target;
        this.action = action;
        this.url    = url;
    }

    public WsnException(String title, String details, String url) {
        this.title = title;
        this.details = details;
        this.url = url;
    }

    public WsnException(String title, String details) {
        this.title = title;
        this.details = details;
        this.url = "/error.jsp";
    }
    
    public WsnException(String title, String details, Throwable t) {
      this.title = title;
      this.details = details;
      this.t = t;
      this.url = "/error.jsp";
  }

    public WsnException() {
        target = 0;
        action = 0;
        url = "/error.jsp";
    }

    public int getTarget() {
        return target;
    }

    public int getAction() {
        return action;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getHeader() {
        return header;
    }

    public String getDetails() {
        return details;
    }
    
    public Throwable getT() {
    	return t;
    }

    public void setTarget(int target) {
        this.target = target;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
