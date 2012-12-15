

package com.powerdba.mvc;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

public interface PresentationEnvironment {

	/****************************
	** Session Management
	*/

	/**
	 * Creates a new session and returns it's unique session identifier.
	 */
	String createSession();

	/**
	 * Returns the current session's unique identifier.
	 */
	String getSessionId();

	/**
	 * Destroys the current session.
	 */
	void invalidateSession();

	/*****************************
	** Session Object Binding
	*/

	/**
	 * Returns the object designated by 'name', bound to
	 * the current session.  Returns null if no such
	 * object is found.
	 */
	Object getSessionAttribute(String name);

	/**
	 * Binds the object designated by 'name' to the
	 * current session.  If there is no current session,
	 * this does nothing.
	 */
	void setSessionAttribute(String name, Object value);

	/**
	 * Removes the object designated by 'name' from the
	 * current session.  If there is no current session,
	 * this does nothing.
	 */
	void removeSessionAttribute(String name);

	/*******************************
	** Request Object Access
	*/

	/**
	 * Returns the object designated by 'name' from the
	 * current request, null if not found.
	 */
	Object getReqAttribute(String name);

	/**
	 * Returns an Enumeration of attribute names from
	 * the current request.
	 */
	Enumeration<String> getReqAttributeNames();

	/**
	 * Will add an object into a request object for use when forwarding to another jsp
	 */
	void setReqAttribute(String name, Object o);

	/*******************************
	** Request String Argument Access
	*/

	/**
	 * Returns the request parameter designated by
	 * 'name', null if not found.<p>
	 *
	 * Only use this if you are sure the parameter
	 * has only one value.  Otherwise, use getReqParameterValues().
	 */
	String getReqParameter(String name);

	/**
	 * Returns an Enumeration of all request parameter names.
	 */
	Enumeration<String> getReqParameterNames();

	/**
	 * Returns an array of all request parameter values.
	 */
	String[] getReqParameterValues(String name);

	/*******************************
	** Presentation Style
	*/

	/*******************************
	** Environment Request Helper
   *  these methods load a group of hashmaps that can
   *  efficiently store and manage the request so
   *  that controller objects are streamlined when
   *  grabbing the data from the request.
	*/

	/**
   *  returns a parameter as a string from the hashmap
	*/
	String getParameter(String name);

	/**
   *  returns a group of parameters as a collection
   *  from the hashmap that stores arrayLists of parameters
   *  under the same name.
	*/
	Collection getParameterList(String name);

	/**
   *  returns an attribute that was set by another java/jsp
   *  who then forwarded the request. These attributes are
   *  objects and are stored in an Object hashmap.
	*/
	Object getAttribute(String name);

	/**
   *  returns an int from the standard parameter hashmap.
   *  it does its own error checking and will return 0 as
   *  a default if that parameter does not exist.
	*/
	int getInt(String name);

	/**
   *  returns a long from the standard parameter hashmap.
   *  it does its own error checking and will return 0 as
   *  a default if that parameter does not exist.
	*/
	long getLong(String name);

	/**
	 * returns an object that is the pageContext, in a jsp
	 * environment.  This allows other types of view
	 * implementation to use it differently.
	 */
	Object getPageContext();

    HttpServletRequest getHttpRequest();


}
