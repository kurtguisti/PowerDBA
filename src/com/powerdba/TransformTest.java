package com.powerdba;

import java.sql.*;
import java.io.*;
import com.powerdba.util.*;
import com.powerdba.jdbc.ConnectionManager;

public class TransformTest  {

    private BufferedReader in;
    private String[] args;
    private Connection conn;
    private Thread t;

    public TransformTest() {
    }
    
    private void setConnection(String db) throws Exception {

        try {
            if ( this.conn != null ) {
                try {
                    conn.close();
                    Tracer.log("Closed the current connection (actually only releases it back to the pool)...", Tracer.DEBUG, this);
                } catch ( Exception e3 ) {
                    Tracer.log(e3,"Error closing connection to the database", Tracer.WARNING, this);
                }
            }

            Tracer.log("Opening a new Connection to " + db, Tracer.DEBUG, this);
            this.conn = ConnectionManager.getConnection(db);
            this.conn.setAutoCommit(false);
        } catch ( Exception e ) {
            Tracer.log("Error getting a connection to the database", Tracer.ERROR, this);
            throw e;
        }
    }


	public static void main(String[] args) {

        TransformTest tt = new TransformTest();
        tt.t = new Thread();

        long delay;
        int duration;
        
        if ( args.length == 0 ) {
          delay = 150;  // Milliseconds  
        } else { 
          delay = Long.parseLong(args[0]);
        }

        if ( args.length <= 1 ) {       
          duration = 1; // 1 minute
        } else {
          duration = Integer.parseInt(args[1]);
        }

        float tps = (float)(1000/delay)*(float)1.5;
        
        String vin;
        String sql;
        long insertCount = 0;

        try {

            tt.setConnection("StreamsTest");

            PreparedStatement pstmt = null;
            ResultSet rset          = null;
            long ctime = System.currentTimeMillis();    
            Tracer.log("Starting Test At: " + DateTranslator.getStringDateTime(ctime),Tracer.DEBUG,"");
            Tracer.log("Duration: " + duration + " Minutes",Tracer.DEBUG,"");
            Tracer.log("Delay: " + delay + " Milliseconds",Tracer.DEBUG,"");
            Tracer.log("~tps: " + tps,Tracer.DEBUG,"");

            int i;
            for ( i=1; System.currentTimeMillis() <= ctime+(60000*duration); i++ ) {

              pstmt = tt.conn.prepareStatement("select dbms_flashback.get_system_change_number from dual");
              rset  = pstmt.executeQuery();
              rset.next();
              vin = rset.getString(1) + "EEEEEEE";
              rset.close();
              pstmt.close();

              sql = "insert into web_transform_times (rpt_date,vin,transform_Type,request_type,report_version, " +
                    " xml_len,xform_len,xml_source,xml_execute,xml_compile,xml_complete, " +
                    " cf_lapse_time,build_string,msparser_version) " +
                    " values(sysdate,'" + vin + "','GENCF','DEC','4.0',9741,12500,.02,.02,0,0,.03,'1.01',5)";

              pstmt = tt.conn.prepareStatement(sql);
              //Tracer.log(sql,Tracer.DEBUG,"Main");
              pstmt.execute();
              tt.conn.commit();
              insertCount++;
              pstmt.close();

              if ( i%2 == 0 ) {
                sql = "insert into sws_transform_times (rpt_date,vin,username,loc_gid,transform_Type,request_type,report_version, " +
                       " xml_len,xform_len,xml_source,xml_execute,xml_compile,xml_complete, "  +
                       " cf_lapse_time,build_string,msparser_version) " +
                       " values(sysdate,'" + vin + "','KGUISTI',24761,'GENCF','VHR','4.0',9741,12500,.02,.02,0,0,.03,'1.01',5)";
                pstmt = tt.conn.prepareStatement(sql);
                pstmt.execute();
                tt.conn.commit();
                insertCount++;
                //Tracer.log(sql,Tracer.DEBUG,"Main");
                pstmt.close();
              }

              Thread.sleep((long)(delay));
              //if ( i%1000 == 0 ) {
              //  Tracer.log("Iteration: " + i, Tracer.DEBUG, "Main");
              // }
              if (insertCount%1000 == 0 ) 
              {
                Tracer.log("Rows Inserted: " + insertCount, Tracer.DEBUG, "Main");
              }
            }
            Tracer.log("Ending Test At: " + DateTranslator.getStringDateTime(System.currentTimeMillis()),Tracer.DEBUG,"");
            //Tracer.log("Total Iterations: " + i, Tracer.DEBUG, "");
            Tracer.log("Total Inserts: " + insertCount, Tracer.DEBUG, "");
        } catch ( Exception e ) {
            System.out.println("There was an error processing the SQL");
            e.printStackTrace();
        } finally {
          try {
            if ( tt.conn != null ) {
              tt.conn.close();
            }
          } catch ( Exception e ) {}
        }

        System.exit(1);

	}
}
