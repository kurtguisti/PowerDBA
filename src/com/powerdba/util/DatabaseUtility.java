/*
 * Created by IntelliJ IDEA.
 * User: kguisti
 * Date: Aug 23, 2002
 * Time: 11:50:07 AM
 * To change template for new class use 
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.powerdba.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseUtility {

    static final String OBJECT_NAME = "DatabaseUtility";

    static public long getNewKey(String sequence, Connection conn) throws SQLException {
        ResultSet rset = null;
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement("select " + sequence + ".nextval from dual");
            rset = pstmt.executeQuery();
            if ( rset.next() ) {
                return rset.getLong(1);
            } else {
                return 0;
            }

        } catch ( SQLException e ) {
            Tracer.log("Error getting nextval for sequence " + sequence, Tracer.ERROR, OBJECT_NAME);

            throw e;
        } finally {
            if ( rset != null ) rset.close();
            if ( pstmt != null ) pstmt.close();
        }
    }
}
