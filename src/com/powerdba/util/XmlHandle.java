
package com.powerdba.util;


/**
 *  Helper class that will setup jdom and a given parser (currently xerces).  This is primarily used in the
 *  interface objects as a way of abstracting out the XML interface commonality.
 *
 *  @author:  kguisti
 *
 */

import org.jdom.*;
import org.jdom.input.*;

import java.io.*;

public class XmlHandle {
    Document doc;

    public XmlHandle() {}

    /**
     *  Get the document element.  This should be used when parsing XML
     */
    public Document getDocument() {
        return doc;
    }

    /**
     *  Sets the internal document element.  This is used more when XML is being created as opposed to parsed
     */
    public void setDocument(Document doc) {
        this.doc = doc;
    }

    /**
     *  Sets up the inputstream and creates the XML doc object
     */
    public void setInput(InputStream in) throws JDOMException {
        SAXBuilder builder = new SAXBuilder(false);
        doc = builder.build(in);  
    }
    
    public void setInput(File in) throws JDOMException {
      SAXBuilder builder = new SAXBuilder(false);
      doc = builder.build(in);
    }
    

}
