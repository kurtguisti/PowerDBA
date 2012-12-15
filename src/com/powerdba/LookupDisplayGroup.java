package com.powerdba;

import java.util.ArrayList;
import java.util.Hashtable;

public class LookupDisplayGroup {
    
    public LookupDisplayGroup() {
        this.displayGroupData = new Hashtable();
        this.displayTemplate = new ArrayList();        
    }
    
    public LookupDisplayGroup(Hashtable displayGroupData, ArrayList displayTemplate) {
        this.displayGroupData = displayGroupData;
        this.displayTemplate = displayTemplate;
    }

    private Hashtable displayGroupData;
    private ArrayList displayTemplate;

    /**
     * @return Returns the displayGroupData.
     */
    public Hashtable getDisplayGroupData() {
        return displayGroupData;
    }
    /**
     * @param displayGroupData The displayGroupData to set.
     */
    public void setDisplayGroupData(Hashtable displayGroupData) {
        this.displayGroupData = displayGroupData;
    }
    /**
     * @return Returns the displayTemplate.
     */
    public ArrayList getDisplayTemplate() {
        return displayTemplate;
    }
    /**
     * @param displayTemplate The displayTemplate to set.
     */
    public void setDisplayTemplate(ArrayList displayTemplate) {
        this.displayTemplate = displayTemplate;
    }
}
