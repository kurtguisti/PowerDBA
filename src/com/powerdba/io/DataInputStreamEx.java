
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
 * Also allows read of unsigned 32-bit integers as a signed 64-bit long.
 */
public class DataInputStreamEx extends DataInputStream {

    public DataInputStreamEx(InputStream iStream) {
        super(iStream);
    }

    public String readString() throws IOException {

        StringBuffer buf = new StringBuffer();
        byte         b;

        while ((b = readByte()) != 0) {
            buf.append((char) b);
        }

        return buf.toString();
    }

    public long readUnsignedInt32() throws IOException {

        long returnVal = (long) readInt();

        if (returnVal < 0) {
            returnVal = returnVal + ((long) Integer.MAX_VALUE + 1) * 2;
        }

        return returnVal;
    }

    public void readArray(byte[] array) throws IOException {

        for (int i = 0; i < array.length; i++) {
            array[i] = readByte();
        }
    }
}
