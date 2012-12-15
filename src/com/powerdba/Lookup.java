package com.powerdba;

import java.util.ArrayList;

import com.powerdba.mvc.jsp.SelectEntry;

public class Lookup {
    
    public Lookup() {}
    
    public Lookup(String name, String label, int displayPriority, String initialValue) {
        this.name = name;
        this.label = label;
        this.displayPriority = displayPriority;
        this.initialValue = initialValue;
    }

    private String name;
    private int displayPriority;
    private String label;
    private ArrayList prepopulatedEntries;
    private String initialValue;
   
    /**
     * @return Returns the displayPriority.
     */
    public int getDisplayPriority() {
        return displayPriority;
    }
    /**
     * @param displayPriority The displayPriority to set.
     */
    public void setDisplayPriority(int displayPriority) {
        this.displayPriority = displayPriority;
    }
    /**
     * @return Returns the lookupQueryName.
     */
    public String getLabel() {
        return label;
    }
    /**
     * @param lookupQueryName The lookupQueryName to set.
     */
    public void setLabel(String label) {
        this.label = label;
    }
    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * @return Returns the prepopulatedEntries.
     */
    public ArrayList getPrepopulatedEntries() {
        return prepopulatedEntries;
    }
    /**
     * @param prepopulatedEntries The prepopulatedEntries to set.
     */
    public void setPrepopulatedEntries(ArrayList prepopulatedEntries) {
        this.prepopulatedEntries = prepopulatedEntries;
    }

    /**
     * @return Returns the initalValue.
     */
    public String getInitialValue() {
        return initialValue;
    }
    /**
     * @param initalValue The initalValue to set.
     */
    public void setInitialValue(String initialValue) {
        this.initialValue = initialValue;
    }

    public int compareTo(Object obj) {
        Lookup tmp = (Lookup) obj;

        if ( this.displayPriority < tmp.getDisplayPriority() ) {
          return -1;
        } else if ( this.displayPriority > tmp.getDisplayPriority() ) {
          return 1;
        }

        return 0;
    }

}