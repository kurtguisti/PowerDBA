
package com.powerdba;

import com.powerdba.util.Tracer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;

public class ActionHolder {

	private static Hashtable actions = null;
	private static ArrayList actionList = new ArrayList();
	private static ArrayList actionObjectList = new ArrayList();
  
	public static PowerDbaAction getAction(String name) {

    PowerDbaAction action = null;
    
		if (ensureLoaded()) {
      action = (PowerDbaAction) actions.get(name.trim());
    }

		return action;
	}
	
	public static ArrayList getList() {
		
		ArrayList list = null;
		
		if (ensureLoaded()) {
			list = actionList;
		}
		
		return list;
	}
	
	
	public static ArrayList getObjectList() {
		
		ArrayList list = null;
		
		if (ensureLoaded()) {
			list = actionObjectList;
		}
		
		return list;
	}

	public static boolean isLoaded() {
		return actions != null;
	}

  private static boolean ensureLoaded() {

    if ( !isLoaded() ) {

      Tracer.log("Loading actions from files actions.xml and actions_extend.xml", Tracer.DEBUG, "ActionHolder.ensureLoaded");

      try {
    	
        actions = XmlDAO.getActions("actions.xml");
        actions.putAll(XmlDAO.getActions("actions-extend.xml"));
        
        // Create a List of the action names.
        Enumeration e = actions.keys();
        while ( e.hasMoreElements() ) {
        	actionList.add((String) e.nextElement());
        }
        
        Collections.sort(actionList);
        
        // Create a List of the action objects.
       e = actions.keys();
        while ( e.hasMoreElements() ) {
        	actionObjectList.add((PowerDbaAction) actions.get(e.nextElement()));
        }
        
        Collections.sort(actionObjectList);

        
        Tracer.log("Actions List has a total of " + actionList.size() + " actions.", Tracer.DEBUG, "ActionHolder.ensureLoaded");
         
      } catch (Exception e) {
        System.out.println("ActionHolder: FAILED loading");
        return false;
      }
    }

    return true;
  }
}


