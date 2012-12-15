
package com.powerdba.util;

import java.io.StringWriter;
import java.io.PrintWriter;

/**
 * This utility helps deal with Exception stacktraces.
 *
 * @author  cbernard
 */
public class StackTrace {

    /**
     * This gets the full StackTrace from an exception as a String.
     * Though not necessary for Tracing (since Tracer does this already
     * when the Exception is passed as an arg), this is handy when the
     * full stacktrace should be sent somewhere besides to trace log, like
     * returned via CORBA to a client.
     */
    static public String get(Exception e) {

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);

        return "\n" + sw.toString();
    }
}
