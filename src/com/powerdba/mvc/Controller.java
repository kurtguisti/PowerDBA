

package com.powerdba.mvc;

import com.powerdba.mvc.PresentationEnvironment;

public abstract class Controller {

    protected PresentationEnvironment env;
    protected String                  sessionId;

    protected Controller(PresentationEnvironment env) {
        this.env       = env;
        this.sessionId = env.getSessionId();
    }
}
