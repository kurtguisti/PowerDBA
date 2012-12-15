
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

import java.util.*;

/**
 * The JspLoader object centralizes the FormHidden list structure
 * and methods that are used to manage it.
 *
 * @author mhaught
 */
public class JspLoader {

    private ArrayList list;

    public JspLoader() {
        list = new ArrayList();
    }

    public void addHidden(FormHidden hidden) {
        boolean add = list.add(hidden);
    }

    public String buildHiddens() {

        StringBuffer hiddenList = new StringBuffer(500);

        if (list.size() > 0) {
            ListIterator listIter = list.listIterator();

            while (listIter.hasNext()) {
                FormHidden hidden = (FormHidden) listIter.next();

                hiddenList.append(hidden);
            }
        }

        return hiddenList.toString();
    }
}
