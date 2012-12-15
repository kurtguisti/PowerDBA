package com.powerdba.mvc;

import javax.servlet.jsp.PageContext;

import com.powerdba.mvc.jsp.JspEnvironment;
import com.powerdba.util.Tracer;

public class LoginManager {

    private LoginView loginView;
    private JspEnvironment env;

    //Constructors
    public LoginManager(JspEnvironment env) throws Exception {      
	    Tracer.log("In LoginManager constructor.", Tracer.DEBUG, this);
	    this.env = env;
	    loginView = new LoginView(env);
    }

    // Main Process
    public LoginView process() throws Exception {

	    Tracer.log("***Starting loginManager process() method", Tracer.MAJOR, this);
      PageContext page = (PageContext) env.getPageContext();
      boolean isConnected = false;
	
	    try {
	      Object isConnectedObj = env.getSessionAttribute("adminconnected");
	      if ( isConnectedObj != null ) {
	        isConnected = ((Boolean) isConnectedObj).booleanValue();
	      }
	      
	      if ( isConnected ) {
          Tracer.log("Forwarding to status page...", Tracer.DEBUG, this);
          page.forward("/dbStatus.jsp");	          
	      }
	  
	      String userName = env.getParameter("adminusername");
	      String password = env.getParameter("adminpassword");
	
	      if ( userName.equals("admin")  && !password.equals("") ) {
	          env.setSessionAttribute("adminconnected", new Boolean(true));

	          Tracer.log("Forwarding to status page...", Tracer.DEBUG, this);
	          page.forward("/dbStatus.jsp");
	      } else if ( userName.equals("") || password.equals("") ) {
	          loginView.build();
	          Tracer.log("*** Done constructing loginView", Tracer.MAJOR, this);
	      }
	      
	    } catch (Exception e) {
	        throw e;
	    }
	
	    Tracer.log("***Returning loginView", Tracer.DEBUG, this);
	
	    return loginView;
    }

}
