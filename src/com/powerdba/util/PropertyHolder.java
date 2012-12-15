
package com.powerdba.util;

import java.io.*;
import java.util.Enumeration;
import java.util.Properties;
import java.net.URL;


/**
 * PropertyHolder is an entirely static class providing global access
 * to a set of properties.
 * <p>
 * It is conventional to load the properties file in main() using
 * argv[0].
 * <p>
 * A properties file will usually be named according to the application,
 * followed by ".properties" (an example would be "tracer.properties")
 */
public class PropertyHolder {

	private static Properties myProperties = null;
  
	public static String getProperty(String name) {

		String returnVal = null;

    if (ensureLoaded()) {
      returnVal = (String) myProperties.getProperty(name.toLowerCase().trim());
      if (returnVal != null) {
        returnVal = returnVal.trim();
      }
    }

		return returnVal;
	}
	
	public static String getProperty(String name, String defaultValue) {

		String returnVal = null;
		
		returnVal = getProperty(name);
		
		if ( returnVal == null ) {
			returnVal = defaultValue;
		} 

		return returnVal;
	}
	
	public static String dumpProperties() throws IOException {
	    
			StringBuffer sb = new StringBuffer();
			Enumeration e = myProperties.keys();
			
			while ( e.hasMoreElements() ) {
			  String key = (String) e.nextElement();
			  String value = (String) myProperties.getProperty(key);
			  sb.append(key + " = " + value + "\n");
			}

			return sb.toString();
  }

	public static void loadProperties(String fileName) throws IOException {
  
    // Open the Properties file on disk
    PropertyHolder ph = new PropertyHolder();
    InputStream stream = ph.getClass().getResourceAsStream(fileName);

    // Instantiate the static class
 		myProperties = new Properties();
    
    // Load the static class with the contents of the file
		myProperties.load(stream);

    // Instantiate a new Properties class to hold case-insensitive names
		Properties props = new Properties();
    
		Enumeration e = myProperties.propertyNames();

		// Loop through the properties read from disk and write to the new Properties class making name case-insensitive.
		while (e.hasMoreElements()) {
			String name = (String) e.nextElement();
			String value = myProperties.getProperty(name);
			props.setProperty(name.toLowerCase(), value);
		}
		myProperties = props;
		
	}

	//Returns a boolean indicated whether or not PropertyHolder has been loaded.
	public static boolean isLoaded() {
		return myProperties != null;
	}

  private static boolean ensureLoaded() {
  	
  	String defaultPropertiesFile = "powerdba.properties";

    if ( !isLoaded() ) {
    	
    	String propertiesFile = defaultPropertiesFile;
    	
      URL url = PropertyHolder.class.getResource(propertiesFile);

      System.out.println("Loading property values from file: " + url.getFile().toString() );
       
      try {     
        loadProperties(propertiesFile);
        System.out.println(dumpProperties());
      } catch (Exception e) {
        System.out.println("PropertyHolder: FAILED loading: system propery file = " + propertiesFile);
        return false;
      }
    }

    return true;
  }
}

