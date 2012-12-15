package com.powerdba;

import java.sql.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

import com.powerdba.jdbc.*;
import com.powerdba.mvc.jsp.JspEnvironment;


public class PowerDba {
    
public PowerDba() {}

	static public void main(String argv[]) {
		
	  OracleDatabaseConnection odb = null;

    if ( argv[0].equals("-h") ) { 	
    	for ( int i=0; i<ActionHolder.getList().size(); i++ ) {
    		System.out.println(ActionHolder.getList().get(i));
    	}
    } else if (argv[2].equals("-h") ) {   
    	try {
				odb = getDatabase(argv[1]);
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    	try {
				PowerDbaAction action = getAction(argv[0]);
				for ( int i=0; i<action.getQueries().size(); i++ ) {
					String queryName = (String) action.getQueries().get(i);
					System.out.println(queryName);
					Query q = QueryHolder.getQuery(odb, new JspEnvironment(), queryName);
					Enumeration e = q.getParms().keys();
					while ( e.hasMoreElements() ) {
						String key = (String) e.nextElement();
						BindVar bv = (BindVar) q.getParms().get(key);
						System.out.print( bv.getVarName());
						System.out.print(" - ");
						System.out.print( bv.getDataType());
						System.out.print(" - ");
						System.out.print( bv.getDefaultValue());
				    System.out.print("\n");		
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    } else {
    	
    	try {
				odb = getDatabase(argv[1]);
			} catch (SQLException e3) {
				// TODO Auto-generated catch block
				e3.printStackTrace();
			}
		
		  String actionName = argv[0];
		  PowerDbaAction action = null;
			try {
				action = getAction(actionName);
			} catch (Exception e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			
	    boolean loop = true;
	    int parmIndex = 4;
	    Hashtable inputValues = new Hashtable();
	    
	    // Get the parameters
	    while ( loop ) {
	    	try {
	    	  String arg = argv[parmIndex];
	    	  Enumeration e = new StringTokenizer(arg, "=");
	    	  String name = (String) e.nextElement();
	    	  String value = (String) e.nextElement();
	    	  inputValues.put(name,value);
	    	  
	  	  	System.out.print(name + " = " + value + " ");
	    	  parmIndex++; 	  
	    	} catch ( Exception e ) {
	    		loop = false;
	    	}
	    }
	    
	    // Get each query for the action into an ArrayList
		  Query q = null;
		  ArrayList queries = new ArrayList();
		  
	    for ( int i=0; i<action.getQueries().size(); i++) {
	    	
		    	String queryName = (String)action.getQueries().get(i);
		    	System.out.println("Loading query " + queryName);
	
					try {
						q = QueryHolder.getQuery(odb, queryName);
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
						  
				  try {
						q.bindAll(inputValues);
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					queries.add(q);			
	    }
	    
	    if ( argv[0].equals("-h") ) { 	
	    	for ( int i=0; i<ActionHolder.getList().size(); i++ ) {
	    		System.out.println(ActionHolder.getList().get(i));
	    	}
	    }
					
	
	    // Run
	    String output=null;
		  
		  System.out.println("Running " + actionName + " against database " + odb.getName());	  
		  
	    for ( int i=0; i<Integer.parseInt(argv[3]); i++ ) {
	    	
	    	for ( int j=0; j<queries.size(); j++ ) {
		  
	        q = (Query) queries.get(j);
	    		
	    		try {
						output = q.getAscii();
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				  
				  System.out.println(output);
				  
	    	}
		
			  try {
					Thread.sleep(Integer.parseInt(argv[2]) * 1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		
	    }
	    
	    // Close the connection to the database.
	    try {
				odb.getConn().close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
    }

	  System.exit(0);
	}
	
	private static PowerDbaAction getAction(String actionName) throws Exception {
	  PowerDbaAction action = ActionHolder.getAction(actionName);
	  if ( action == null ) {
	  	throw new Exception("Action is undefined in actions.xml");
	  }
	  return action;
	}
	
	private static OracleDatabaseConnection getDatabase(String name) throws SQLException {
		OracleDatabaseConnection odb =null;
		try {
			odb = ConnectionManager.getNonPooledDatabase(name);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return odb;
	}
	
}
