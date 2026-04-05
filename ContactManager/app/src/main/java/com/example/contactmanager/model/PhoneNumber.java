package com.example.contactmanager.model;

public class PhoneNumber {
    private long id;
    private long contactId;
    private String number;
    private String type;

    public PhoneNumber() {
        this.type = "mobile";
    }

    public PhoneNumber(long id, long contactId, String number, String type) {
        this.id = id;
        this.contactId = contactId;
        this.number = number;
        this.type = type;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getContactId() { return contactId; }
    public void setContactId(long contactId) { this.contactId = contactId; }

    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
