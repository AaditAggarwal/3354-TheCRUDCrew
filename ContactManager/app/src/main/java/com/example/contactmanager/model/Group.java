package com.example.contactmanager.model;

/**
 * Model: Represents a contact group.
 * Contacts can be assigned to one group at a time.
 */
public class Group {
    private long id;
    private String name;
    private int contactCount; // Transient, populated from DB query

    public Group() {}

    public Group(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getContactCount() { return contactCount; }
    public void setContactCount(int contactCount) { this.contactCount = contactCount; }
}
