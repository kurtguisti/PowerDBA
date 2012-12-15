
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
import java.io.*;

/**
 * Wraps a Hashtable in a runnable, auto-saving wrapper.
 * <p>
 * @author nroe
 */
public class PersistantHashWrapper implements Runnable {

    Hashtable hash;
    String    fileName;
    Thread    runner;
    boolean   running;

    /**
     * Constructor takes a filename.  It attempts to open the file and
     * read a serialized Hashtable from it.  Failing that, a new, empty
     * Hashtable is created.
     * <p>
     * The wrapper then creates a new Thread for auto-saving.
     */
    public PersistantHashWrapper(String fileName) {

        // The constructor goes out and attempts to read in the contents of the saved file...
        this.fileName = fileName;

        try {

            // Attempt to load from file
            FileInputStream   fis = new FileInputStream(fileName);
            ObjectInputStream ois = new ObjectInputStream(fis);

            this.hash = (Hashtable) ois.readObject();

            fis.close();
        } catch (Exception e) {

            // Failing that, create a new one.
            this.hash = new Hashtable();
        }

        running = true;

        // Start up a new Thread
        Thread runner = new Thread(this);

        if (runner != null) {
            runner.start();
        }
    }

    // Saves the Hashtable, whole, whether changed or not, every 10 seconds
    // to the file specified in the constructor.

    public void run() {

        while (running) {
            try {

                Thread.sleep(10000);    // sleep for ten seconds.

                FileOutputStream   fos = new FileOutputStream(this.fileName);
                ObjectOutputStream oos = new ObjectOutputStream(fos);

                oos.writeObject(this.hash);

                oos.flush();
                fos.close();

            } catch (Exception e) {
                Tracer.log(e, "tried, but failed, to save the hashtable in file " + this.fileName, Tracer.ERROR, this);
            }
        }
    }

    /**
     * Stop running.  This will (permanently) stop the Thread running the auto-saves.
     */
    public void stop() {
        running = false;
    }

    /**
     * Return the wrapped Hashtable
     */
    public Hashtable getHashtable() {
        return hash;
    }

    /**
     * Set the wrapped Hashtable.
     */
    public void setHashtable(Hashtable hash) {
        this.hash = hash;
    }
}
