
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

package com.powerdba.io;

import com.powerdba.util.Tracer;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * LogRotationOutputStream provides a way to automatically rotate log
 * files on a a daily basis.  It is used in the same manner as any other
 * OutputStream.
 * <p>
 * Log files are <b>always</b> opened in append mode.
 */
public class LogRotationOutputStream extends OutputStream {

    /** log rotates when this many days have passed */
    private static final int DEFAULT_MAX_DAYS = 1;

    /** number of milliseconds in a day */
    private static final long DAY_MILLIS = 86400000;
    private int               maxDays;
    private int               numOldLogs;
    private FileOutputStream  fileOutputStream;
    private String            baseFileName;
    private static String     mostRecentDateString = "";
    private volatile boolean  rotateNow;
    private Object            rotateMutex;
    private long              prevDay;    // day counter; we rotate the logs by number of days.
    private Thread            runner;
    private boolean           running;

    // *************  Contstructors  *****************

    /**
     * Create a new LogRotationOutputStream using baseFileName as the base file
     * name.  Actual log files will have names of the format
     * &lt;baseFileName&gt;&lt;year&gt;&lt;day of year&gt;
     * <p>
     * By convention, baseFileName shoulde consist of the application identifier,
     * followed by ".log" or ".trace" depending on it's intended function.
     * <i>E.g.</i>, <b>srp.trace</b>
     */
    public LogRotationOutputStream(String baseFileName)
            throws FileNotFoundException, IOException {

        this.baseFileName = baseFileName;

        setMaxDays(DEFAULT_MAX_DAYS);

        // open the output stream, appending if it exists.
        fileOutputStream = new FileOutputStream(generateFileName(), true);

        // today is the "previous day".
        prevDay     = System.currentTimeMillis() / DAY_MILLIS;
        rotateNow   = false;
        rotateMutex = new Object();

        // fire up a new thread to check the day every minute.
        running = true;
        runner  = new Thread(new Runnable() {

            public void run() {
                checkDay();
            }
        });

        runner.start();
    }

    // *********** File Name Generation **************
    public String generateFileName() {

        SimpleDateFormat formatter  = new SimpleDateFormat("yyyyDDD");    // year, Julian day (day in year)

        synchronized (mostRecentDateString) {
            mostRecentDateString = formatter.format(new Date());
        }

        return baseFileName + mostRecentDateString;
    }

    // **********  Getters and Setters  **************
    public void setMaxDays(int maxDays) {
        this.maxDays = maxDays;
    }

    public int getMaxDays() {
        return maxDays;
    }

    /**
     * This is mainly for a Tracer error message.
     */
    public static String getMostRecentDateString() {
        return mostRecentDateString;
    }

    // ******  File Rotation/Write methods and OutputStream implementation  **********

    /**
     * If today is differs from prevday by maxDays then set rotateNow to true.
     * The next time anything is written to this stream, a new file
     * will be created, and thus log rotation accomplished.
     */
    protected void checkDay() {

        // loop forever
        while (running) {

            // check the day
            if (System.currentTimeMillis() / DAY_MILLIS - prevDay
                    >= maxDays) {
                prevDay = System.currentTimeMillis() / DAY_MILLIS;

                synchronized (rotateMutex) {
                    rotateNow = true;
                }
            }

            // wait a minute
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {}
        }
    }

    protected void testRotate() throws IOException {

        synchronized (rotateMutex) {
            if (rotateNow) {

                // open a new output stream, appending if it exists (why would it exist?).
                fileOutputStream.close();

                fileOutputStream = new FileOutputStream(generateFileName(), true);

                rotateNow        = false;

                Tracer.log("Rotated log " + baseFileName, Tracer.MINOR, this);
            }
        }
    }

    public void write(byte[] b) throws IOException {
        testRotate();
        fileOutputStream.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        testRotate();
        fileOutputStream.write(b, off, len);
    }

    public void write(int b) throws IOException {
        testRotate();
        fileOutputStream.write(b);
    }

    public void flush() throws IOException {
        fileOutputStream.flush();
    }

    public void close() throws IOException {

        fileOutputStream.flush();
        fileOutputStream.close();

        running = false;

        runner.interrupt();
    }

    // ******  Testing Harness  **********

    /**
     * For testing purposes only.
     */
    public static void main(String argv[]) throws IOException {

        try {
            LogRotationOutputStream lros =
                new LogRotationOutputStream("test.file.xxx");

            while (true) {
                lros.write(("Stuff " + (new Date()) + "\n").getBytes());

                try {
                    Thread.sleep(12000);    // sleep 52 seconds
                } catch (InterruptedException e) {}
            }
        } catch (IOException ioe) {

            // testing only, so System.err.println() is okay.
            System.err.println("err: " + ioe);
        }
    }
}
