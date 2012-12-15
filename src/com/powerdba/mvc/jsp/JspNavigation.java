

package com.powerdba.mvc.jsp;

import com.powerdba.mvc.*;
import com.powerdba.util.Tracer;

import javax.servlet.jsp.PageContext;

public class JspNavigation  {

	// Defined Error locations
	public static void toWsnError(WsnException wsn, JspEnvironment env) throws Exception {

        PageContext page = (PageContext) env.getPageContext();
		    String address = wsn.getUrl();

        // pop wsn into session for retrieval in the error jsp
        env.setSessionAttribute("wsnException", wsn);

		// forward while adding log.
		Tracer.log("forwarding to " + address, Tracer.MAJOR, env.getSessionId(), "JspNavigation");
		page.forward(address);

	}

	// forward
	public static void jspForward(JspForward forward, PresentationEnvironment env)
			throws Exception {

		PageContext    page = (PageContext) env.getPageContext();
		String         url  = forward.getUrl();

		Tracer.log("jspForward =" + url, Tracer.MAJOR, env.getSessionId(), "JspNavigation");

		// check url before forwarding.
		if ((url == null) || url.equals("")) {
			// no forwarding
			Tracer.log("not forwarding", Tracer.MAJOR, env.getSessionId(), "JspNavigation");
		} else {
			// forward
			page.forward(url);
		}
	}
}
