
package com.powerdba.util;

import java.io.File;

/**
 * This utility determines a data resource file pathname. <p>
 *
 * @author  cbernard
 */
public class SourceResourceLoader {

    private static final String ROOT_PROPERTY = "treeRoot";

    /**
     * Return the full path name of a resource (data file).
     */
    public static String getResourcePathName(Class cls, String resource) {

        String sourceRoot = System.getProperty(ROOT_PROPERTY);

        if (sourceRoot == null || sourceRoot.length() == 0) {
            throw new RuntimeException("System Property '" + ROOT_PROPERTY + "' is not defined");
        }

        StringBuffer sb = new StringBuffer(sourceRoot);

        if (sb.charAt(sb.length() - 1) != File.separatorChar) {
            sb.append(File.separator);
        }

        sb.append(StringUtility.replace(
                cls.getName().substring(0, cls.getName().lastIndexOf(".")), ".", File.separator));

        Tracer.log("class name: [" + sb + "]", Tracer.DEBUG, "");

        Tracer.log("file path separator  : [" + File.pathSeparator + "]", Tracer.DEBUG, "");

        sb.append(File.separator + resource);

        return sb.toString();
    }


}
