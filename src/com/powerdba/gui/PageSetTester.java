package com.powerdba.gui;

//import com.carfax.ade.TctDAO;
import com.powerdba.mvc.jsp.HtmlComponent;
import com.powerdba.util.Tracer;
import com.powerdba.gui.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Enumeration;

public class PageSetTester 
{
  public PageSetTester() {}

  	static public void main(String argv[]) {
      Connection conn;
      
      String rval = null;
      
		  try {
      
          System.out.println("Got " + PageLoader.getColumnHeading("KURT_GUISTI"));

          //DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
          //conn = DriverManager.getConnection("jdbc:oracle:thin:@cxu02:1521:dcfxn2", 
          //                                   "ade_admin", 
          //                                   "dba_admind");
                                             
           //PageSet pageSet = null;

           //try {        

             //pageSet = TctDAO.getTctList("11", "100", null, -1, conn);
             //System.out.println(new PageGenerator(pageSet.getFirstPage()).getHtml());
             /*System.out.println(PageGenerator.getHtml(pageSet.getNextPage()));
             System.out.println(PageGenerator.getHtml(pageSet.getPreviousPage())); 
             System.out.println(PageGenerator.getHtml(pageSet.getNextPage()));
             System.out.println(PageGenerator.getHtml(pageSet.getNextPage()));
             System.out.println(PageGenerator.getHtml(pageSet.getNextPage()));
             System.out.println(PageGenerator.getHtml(pageSet.getNextPage()));
             System.out.println(PageGenerator.getHtml(pageSet.getNextPage()));
             System.out.println(PageGenerator.getHtml(pageSet.getNextPage()));
             System.out.println(PageGenerator.getHtml(pageSet.getNextPage()));
             System.out.println(PageGenerator.getHtml(pageSet.getNextPage())); 
             System.out.println(PageGenerator.getHtml(pageSet.getNextPage()));
             System.out.println(PageGenerator.getHtml(pageSet.getNextPage()));
             System.out.println(PageGenerator.getHtml(pageSet.getNextPage()));
             System.out.println(PageGenerator.getHtml(pageSet.getNextPage())); 
             System.out.println(PageGenerator.getHtml(pageSet.getLastPage()));*/

           //} catch ( SQLException e ) {
               //Tracer.log("SQL Exception: Error on mmy_id " + rset.getLong(1), Tracer.ERROR, "");
          //     e.printStackTrace();
          // } catch ( Exception e ) {
           //    Tracer.log("Exception: Error in Page Set Tester", Tracer.ERROR, "");
            //   e.printStackTrace();
          // }

	  	} catch (Exception e) {
         System.out.println("Error: " + e.getMessage()); 
         e.printStackTrace();
      }

		System.exit(0);
	}
}