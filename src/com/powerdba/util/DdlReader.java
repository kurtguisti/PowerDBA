
/*
 * (C) Copyright 2001 StreamWorks Technologies
 * All rights reserved
 * This software is the property of SWT.
 * All use, reproduction, modification, or
 * distribution of this software is only permitted
 * in strict compliance with an express written
 * agreement with SWT.
 * This software contains and implements SWT
 * PROPRIETARY INFORMATION
 * Use or disclosure of SWT PROPRIETARY INFORMATION
 * is only permitted in strict compliance with an
 * express written agreement with SWT
 */

package com.powerdba.util;

import java.util.*;
import java.sql.*;
import java.io.*;

/**
 *  Will read a given text file and execute all ddl statements in that file.
 *  parameters:
 *      1:  jdbc connect string
 *      2:  username
 *      3:  password
 *      4:  DDL/SQL filename
 *
 * @author ncuster
 */
public class DdlReader  {

    private Connection conn;
    private BufferedReader in;
    private String[] args;
    private int argIdx;

    public DdlReader() {
    }

    public void setupArgs(String[] args) {
        this.args   = args;
        argIdx      = 4;
    }

    /**
     *  Where the real work is done.  After getting setup, this is the method
     *  that will actually parse the file and execute the SQL
     */
    public void processFile() throws IOException {
        String inputLine = "";
        StringBuffer sql = new StringBuffer();
        int counter      = 0;
        int argsIdx      = 0;

        while ( (inputLine = in.readLine()) != null ) {
            inputLine = variableSubstitution(inputLine);

            if ( isComment(inputLine) ) {
            } else {
                //  Next check for a semicolon or / which would indicate "execute", otherwise just add the current line to the sql string
                if ( isExecutable(inputLine) ) {
                    sql.append(inputLine.trim().substring(0, inputLine.length() - 1));

                    System.out.println("REM  ---  Executing sql stmt [" + (++counter) + "]\n" + sql + ";\n");

//                    executeStatement(sql);

                    sql.setLength(0);  // reset the string
                } else {
                    sql.append(inputLine);
                }
            }
        }
    }

    private String variableSubstitution(String inputLine) throws IOException {
        StringTokenizer st      = new StringTokenizer(inputLine, "& \n\t(),;/'", true);
        StringBuffer returnVal  = new StringBuffer();

        while ( st.hasMoreTokens() ) {
            String token = st.nextToken();

            if ( token.startsWith("&") ) {
                String paramName = st.nextToken();

                /*
                byte[] varVal        = new byte[256];
                System.out.print("Enter value for " + paramName + ":  ");
                System.in.read(varVal);
                String variableValue = new String(varVal).replace('\n', ' ').trim();
                */
                if ( argIdx+1 > args.length ) {
                    throw new IOException("Error in substitution, not enough parameters given on command line");
                }

                String variableValue = args[argIdx++];

                System.out.println("Replacing parameter [" + paramName + "] with [" + variableValue + "]");

                returnVal.append(variableValue);
            } else {
                returnVal.append(token);
            }
        }

        return returnVal.toString();
    }

    private void executeStatement(StringBuffer sql) {
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            stmt.execute(sql.toString());
        } catch ( SQLException sqle ) {
            System.out.println("Error in executing SQL:  \n[" + sql + "]");
            sqle.printStackTrace();
        } finally {
            try {
                stmt.close();
            } catch ( SQLException sqle2 ) {
                System.out.println("Could not cleanup db stmt");
                sqle2.printStackTrace();
            }
        }
    }


    /**
     *  Checks to see if the string is a comment
     */
    private boolean isComment(String inputLine) {
        if ( inputLine.trim().startsWith("#")                       ||
             inputLine.trim().toLowerCase().startsWith("rem")       ||
             inputLine.trim().toLowerCase().startsWith("prompt")    ||
             inputLine.trim().startsWith("--")      ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *  Checks to see if this is a terminating, or execution invocation line (e.g. ; or / in oracle)
     */
    private boolean isExecutable(String inputLine) {
        if ( inputLine.trim().endsWith(";") || inputLine.trim().endsWith("/") ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *  Create the jdbc connection
     */
    private void setupJdbc(String connectString, String username, String password) throws SQLException {
        DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());

        conn = DriverManager.getConnection(connectString, username, password);
        conn.setAutoCommit(false);
    }

    /**
     *  init the file input for the ddl/sql
     */
    private void setupFileReader(String filename) throws Exception {
        in = new BufferedReader(new FileReader(filename));
    }

    public void cleanup() {
        try {
            if ( conn != null ) conn.close();
        } catch ( Exception e ) {
//            Tracer.log(e, "Unable to cleanup db resources", Tracer.ERROR, this);
        }
    }

	/**
	 * Test code
	 */
	public static void main(String[] args) {
        if ( args.length < 4 ) {
            System.out.println("usage:  DdlReader jdbcConnectString username password SQLFilename");
            System.exit(1);
        }

        DdlReader ddl = null;
        try {
            ddl = new DdlReader();
            ddl.setupJdbc(args[0], args[1], args[2]);
            ddl.setupFileReader(args[3]);
            ddl.setupArgs(args);
            ddl.processFile();
        } catch ( Exception e ) {
            System.out.println("There was an error processing the SQL");
            e.printStackTrace();
        } finally {
            ddl.cleanup();
        }

        System.exit(1);

	}
}
