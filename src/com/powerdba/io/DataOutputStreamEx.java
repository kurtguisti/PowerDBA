
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

import java.io.*;

/**
 * Allows reading of null-terminated character arrays as
 * Java Strings.
 * <p>
 * Also allows the writing of an array of bytes or ints.
 */
public class DataOutputStreamEx extends DataOutputStream {

    public DataOutputStreamEx(OutputStream oStream) {
        super(oStream);
    }

    public void writeString(String s) throws IOException {

        for (int i = 0; i < s.length(); i++) {
            writeByte((byte) s.charAt(i));
        }

        writeByte((byte) 0);    // write terminating null
    }

    public void writeArray(byte[] array) throws IOException {

        for (int i = 0; i < array.length; i++) {
            writeByte(array[i]);
        }
    }

    public void writeArray(int[] array) throws IOException {

        for (int i = 0; i < array.length; i++) {
            writeByte(array[i]);
        }
    }

    public void writeUnsignedInt32(long val) throws IOException {

        int tempVal = (int) val;

        writeByte((byte) ((val >> 24) & 0xFF));
        writeByte((byte) ((val >> 16) & 0xFF));
        writeByte((byte) ((val >> 8) & 0xFF));
        writeByte((byte) ((val) & 0xFF));
    }
}
