
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
 * FormHidden is an JSP helper class that stores hidden
 * parameters in an editable format that can be flushed
 * by the specific loader classes when the view is ready
 * to build the form tag.
 *
 * @author mhaught
 */
public class FormHidden {

    private String name;
    private String value;

    public FormHidden(String name, String value) {
        this.name  = name;
        this.value = value;
    }

    public FormHidden(String name, int intValue) {
        this.name  = name;
        this.value = Integer.toString(intValue);
    }

    public FormHidden(String name, long longValue) {
        this.name  = name;
        this.value = Long.toString(longValue);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String toString() {
        return "<input type=\"hidden\" name=\"" + name + "\" value=\""
               + value + "\">\n";
    }
}
