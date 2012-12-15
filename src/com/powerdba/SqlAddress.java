/*
 * Created by IntelliJ IDEA.
 * User: kguisti
 * Date: Nov 3, 2002
 * Time: 12:01:45 AM
 * To change template for new class use 
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.powerdba;

public class SqlAddress {
    public SqlAddress () {}
    
    public SqlAddress (String address, String hashValue) {
      this.address = address;
      this.hashValue = hashValue;
    }
    
    public SqlAddress(String sqlId, int childNumber) {
    	this.sqlId = sqlId;
    	this.childNumber = childNumber;
    }
    
    private String address;
    private String hashValue;
    private String sqlId;
    private int childNumber;
    public void setAddress(String address) { this.address = address;}
    public void setHashValue(String hashValue) {this.hashValue = hashValue;}
    public String getAddress() {return this.address;}
    public String getHashValue() {return this.hashValue;}
    public String getSqlId() {return this.sqlId;}
    public int getChildNumber() {return this.childNumber;}
}
