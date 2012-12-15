
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

package com.powerdba.mvc.jsp;

/**
 * JspForward handles the url and forwarding request for a controller
 * object.  It delievers this information to the JspNavigation in order
 * to forward the request.  It is passed into a controller and is then loaded
 * if needed.  Then it is set into the jsp to check isSet in order to determine
 * if we should build the jsp page or forward the request on to another jsp.
 *
 * @author mhaught
 */
public class JspForward {

    private String  url;
    private boolean status = false;

    public JspForward() {}

    public JspForward(String url) {
        setForward(url);
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getUrl() {
        return url;
    }

    public boolean getStatus() {
        return status;
    }

    public void setForward(String url) {
        this.url    = url;
        this.status = true;
    }

    public boolean isSet() {
        return status;
    }
}
