package com.powerdba;

public class SourceObject {
    public SourceObject() {}
    public SourceObject(String owner, String name, String text) 
    {
      setOwner(owner);
      setName(name);
      setText(text);
    }

    private String owner;
    private String name;
    private String text;

    public void setOwner(String owner) { this.owner = owner; }
    public void setName(String name) { this.name = name; }
    public void setText(String text) { this.text = text; }

    public String getOwner() { return this.owner; }
    public String getName() { return this.name; }
    public String getText() { return this.text; }
}
