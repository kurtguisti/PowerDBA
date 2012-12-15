
package com.powerdba;

import com.powerdba.util.Tracer;
import java.util.Hashtable;

public class OracleStatHolder {

	private static Hashtable stats = null;
  
	public static OracleStatistic getStatDef(String name) {

		OracleStatistic statDef = null;

    if (ensureLoaded()) {
      statDef = (OracleStatistic) stats.get(name.trim());
    }

		return statDef;
	}

	public static boolean isLoaded() {
		return stats != null;
	}

  private static boolean ensureLoaded() {

    if ( !isLoaded() ) {

      Tracer.log("Loading property values from file: " + "main.properties", Tracer.DEBUG, "PropertyHolder");

      try {     
        stats = XmlDAO.getOracleStats("oraclestats-9i.xml");
      } catch (Exception e) {
        System.out.println("OracleStatHolder: FAILED loading");
        return false;
      }
    }

    return true;
  }
}

