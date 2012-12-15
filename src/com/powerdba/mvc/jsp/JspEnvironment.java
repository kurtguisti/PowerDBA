
package com.powerdba.mvc.jsp;

import com.powerdba.util.PropertyHolder;
import com.powerdba.util.Tracer;

import javax.servlet.http.*;
import javax.servlet.jsp.PageContext;
import java.util.*;

/**
 * Provides information from the Servlet environment for controller objects.<p>
 *
 * This allows controllers to polymorphically use PresentationEnvironment,
 * implementing the logic to support the case when the calling environment
 * consists of Java Server Pages.<p>
 *
*/
public class JspEnvironment {

	public static final String  propertiesFile = "main.properties";
	private PageContext         pageContext;
	private HttpServletRequest  httpRequest;
	private HttpServletResponse httpResponse;
	private HttpSession         httpSession;
	private String              sessionId;
	private HashMap             request;
	private HashMap             arrayRequest;
	private HashMap             objRequest;
	
	public JspEnvironment() {}

	public JspEnvironment(HttpServletRequest httpRequest,
                        HttpServletResponse httpResponse,
						            PageContext pageContext) {

		this.pageContext  = pageContext;
		this.httpResponse = httpResponse;
		this.httpRequest  = httpRequest;
		httpSession       = httpRequest.getSession();
    
    Tracer.log("In JspEnvironment class...", Tracer.DEBUG, this);

		if ( httpSession != null )  {
		  this.sessionId = httpSession.getId();
		  Tracer.log("Existing Session Found, id " + this.sessionId, Tracer.DEBUG, this);
		} else {
			Tracer.log("httpRequest session is null, creating new session.", Tracer.DEBUG, this);		
	    this.setSessionTimeout(960);
		}

		// processes the request and loads into our hashmap structures
		loadHashMap();
    
    Tracer.log("All Done", Tracer.DEBUG, this);
	}

	public Object getPageContext() {
		return pageContext;
	}

	public HttpServletRequest getHttpRequest() {
		return httpRequest;
	}

	public HttpServletResponse getHttpResponse() {
		return httpResponse;
	}

	public void setReqAttribute(String name, Object o) {
		httpRequest.setAttribute(name, o);
	}

	public String setSessionTimeout(int timeoutValue) {

		Tracer.log("Starting createSession(). . .", Tracer.METHOD, "0", this);
		httpSession = httpRequest.getSession(true);

		// Set session timeout value.
		String timeout      = "960";    // default to 4 hours

		try {
			if (!PropertyHolder.isLoaded()) {
				PropertyHolder.loadProperties(JspEnvironment.propertiesFile);
			}

			if (PropertyHolder.getProperty("SessionTimeout") != null || PropertyHolder.getProperty("SessionTimeout").length() != 0) {
				timeout = PropertyHolder.getProperty("SessionTimeout");
			}
		} catch (Exception e) {
			Tracer.log(e, "Problem loading PropertyHolder", Tracer.ERROR, sessionId, this);
		}

		try {
			timeoutValue = Integer.parseInt(timeout) * 60;    // convert minutes to seconds
		} catch (Exception e) {
			Tracer.log(e, "Invalid session timeout value in property file", Tracer.ERROR, this);
		}

		httpSession.setMaxInactiveInterval(timeoutValue);

		sessionId = httpSession.getId();

		Tracer.log("Current http session: [" + sessionId + "]", Tracer.MINOR, sessionId, this);
		Tracer.log("Setting http session timeout to: [" + timeoutValue + "]", Tracer.MINOR, sessionId, this);
    Tracer.log("Current Timeout set to " + httpSession.getMaxInactiveInterval(), Tracer.DEBUG, this);

		return sessionId;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void invalidateSession() {
		if (httpSession != null) httpSession.invalidate();
	}

	public Object getSessionAttribute(String name) {

		if (httpSession != null) {
			return httpSession.getAttribute(name);
		} else {
			return null;
		}
	}

	public void setSessionAttribute(String name, Object value) {
		if (httpSession != null) httpSession.setAttribute(name, value);
	}

	public void removeSessionAttribute(String name) {
		if (httpSession != null) httpSession.removeAttribute(name);
	}

	public Object getReqAttribute(String name) {
		return httpRequest.getAttribute(name);
	}

	public Enumeration<String> getReqAttributeNames() {
		return httpRequest.getAttributeNames();
	}

	public String getReqParameter(String name) {
		return httpRequest.getParameter(name);
	}

	public Enumeration<String> getReqParameterNames() {
		return httpRequest.getParameterNames();
	}

	public String[] getReqParameterValues(String name) {
		return httpRequest.getParameterValues(name);
	}

	// get methods for fetching data from the hash maps
	public String getParameter(String name) {

		Object param    = request.get(name);
		String paramStr = "";

		if (param != null) {
			paramStr = param.toString();
		}

		return paramStr;
	}

	public Collection getParameterList(String name) {
		return (Collection) arrayRequest.get(name);
	}

	public Object getAttribute(String name) {
		return objRequest.get(name);
	}

	public int getInt(String name) {

		String intStr   = getParameter(name);
		int    intValue = 0;

		if ((intStr != null) && (intStr != "")) {
			Tracer.log("intStr:" + name + " != \"\"/null - was true - casting to Int [" + intStr + "] ", Tracer.MINOR, sessionId, this);
			intValue = Integer.parseInt(intStr);
		}

		return intValue;
	}

	public long getLong(String name) {

		String longStr   = getParameter(name);
		long   longValue = 0;

		if ((longStr != null) && (longStr != "")) {
			Tracer.log("longStr:" + name + " != \"\"/null - was true - casting to Long [" + longStr + "] ", Tracer.MINOR, sessionId, this);
			longValue = Long.parseLong(longStr);
		}

		return longValue;
	}

	// will load and parse the parameter and attributes into our efficient hashmaps
	private void loadHashMap() {

		request      = new HashMap();
		arrayRequest = new HashMap();
		objRequest   = new HashMap();

		ArrayList values     = new ArrayList();
		String    key        = "";
		String    value      = "";
		Object    attribute  = new Object();
		int       valueCount = 0;

    // REQUEST PARAMETERS 
		
		// logging buffer for tracking parameters.
		StringBuffer log = new StringBuffer("\nParameters : \n");

		// process the incoming parameters from request parameters
		Enumeration paramNames = httpRequest.getParameterNames();

		while (paramNames.hasMoreElements()) {

			// get key
			key = (String) paramNames.nextElement();

			// get params
			String[] rawValues = httpRequest.getParameterValues(key);

			key = key.toLowerCase();

			// check the values and load into the right hashmap
			if (rawValues != null) {

				// grab value count
				valueCount = rawValues.length;

				// more than one element?
				if (valueCount > 1) {
					values = new ArrayList(Arrays.asList(rawValues));
					arrayRequest.put(key, values);
					value = rawValues[0] + " + more";
				} else {
					value = rawValues[0];
					request.put(key, value);
				}
			} else {
				value = null;
				request.put(key, value);
			}

			// add key/value into buffer
			log.append(key + ": " + value + "\n");
		}

		Tracer.log("Request Parameter Values: " + log.toString(), Tracer.MAJOR, sessionId, this);
		
		///////////////

		log = new StringBuffer("\nAttributes : \n");

		Enumeration attrNames = httpRequest.getAttributeNames();

		while (attrNames.hasMoreElements()) {

			// get key
			key = (String) attrNames.nextElement();

			// get params
			attribute = httpRequest.getAttribute(key);
			key       = key.toLowerCase();

			// check our attribute if it's a string
			if (attribute.getClass().isInstance(key)) {

				// load into our standard request hashmap
				request.put(key, attribute.toString());
			}

			// otherwise we must load into a special hashmap for complex objects
			else {
				objRequest.put(key, attribute);
			}

			// add key/value into buffer
			log.append(key + ": " + attribute.toString() + "\n");
		}

		Tracer.log("Request Attributes (Objects and Values): \n" + log.toString(), Tracer.MAJOR, sessionId, this);
    
		Tracer.log("JSP Environment Counts [\nrequest Values:" + request.size() + "\nRequest Arrays:" + arrayRequest.size()
					   + "\nrequest Objects:" + objRequest.size() + "\n]", Tracer.MAJOR, sessionId, this);
	}
  
}
