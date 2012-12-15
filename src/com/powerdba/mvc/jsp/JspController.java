

package com.powerdba.mvc.jsp;

import com.powerdba.mvc.*;
import com.powerdba.mvc.Controller;
import com.powerdba.mvc.PresentationEnvironment;

public abstract class JspController extends Controller {

    protected int          target    = 0;
    protected int          source    = 0;
    protected int          mode      = 0;
    protected int          action    = 0;
    protected boolean      preFill   = false;
    protected String       formUrl   = "";
    protected StringBuffer resultMsg = new StringBuffer();

    protected JspController(PresentationEnvironment env) {
        super(env);
    }

    // abstract methods
    public abstract View process() throws Exception;
}
