
package com.powerdba.mvc;

import com.powerdba.mvc.PresentationEnvironment;

/**
 * View objects create dynamic content in a presentable form
 * for Boundary code (JSP, XML, etc.), delivered by Controllers.<p>
 *
 * Instances of View take raw data from an entity and mark it up
 * for display.<p>
 *
 * This is not called 'EntityView' because this class may have operators
 * that provide both Presentation and Entity content.  Indeed, View
 * is coupled with Controller as much as it is with Entity.<p>
 *
 */
public abstract class View {

    protected PresentationEnvironment env;
    protected String                  sessionId;

    protected View(PresentationEnvironment env) {
        this.env       = env;
        this.sessionId = env.getSessionId();
    }
}
