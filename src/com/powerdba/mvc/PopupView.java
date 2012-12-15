package com.powerdba.mvc;

import java.sql.SQLException;

import com.powerdba.OracleDatabaseConnection;
import com.powerdba.Query;
import com.powerdba.QueryHolder;
import com.powerdba.jdbc.ConnectionManager;
import com.powerdba.util.Tracer;

public class PopupView {
	
	static public String getQueryPopupHtml(String databaseName, String queryName) throws WsnException {
		
		StringBuffer sb = new StringBuffer();
		
    Query q;
    
		try {
			OracleDatabaseConnection db = null;
			try {
				db = ConnectionManager.getDatabase(databaseName);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			q = QueryHolder.getQuery(db, queryName);
			db.close();
		  sb.append("<table><tr><td><font size=-2><pre>" + q.getSqlString() + "</pre></font></td></tr></table>");
		  
			Tracer.log("Returning HTML " + sb.toString(), Tracer.DEBUG, "PopupView");
			return sb.toString();
			
		} catch (SQLException e) {
			throw new WsnException("Error building Query Popup Text", e.getMessage());
		}

	}

}
