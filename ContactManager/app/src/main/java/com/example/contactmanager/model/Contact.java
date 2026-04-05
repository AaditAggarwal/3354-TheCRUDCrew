package com.example.contactmanager.model;

import java.util.ArrayList;
import java.util.List;

public class Contact {
    private long id;
    private String name;
    private List<PhoneNumber> phoneNumbers;
    private String photoUri;
    private boolean blacklisted;
    private long groupId; // -1 means no group

    public Contact() {
        this.phoneNumbers = new ArrayList<>();
        this.groupId = -1;
    }

    public Contact(long id, String name, String photoUri, boolean blacklisted, long groupId) {
        this.id = id;
        this.name = name;
        this.photoUri = photoUri;
        this.blacklisted = blacklisted;
        this.groupId = groupId;
        this.phoneNumbers = new ArrayList<>();
    }

    // --- Getters and Setters ---

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<PhoneNumber> getPhoneNumbers() { return phoneNumbers; }
    public void setPhoneNumbers(List<PhoneNumber> phoneNumbers) { this.phoneNumbers = phoneNumbers; }
    public void addPhoneNumber(PhoneNumber phoneNumber) { this.phoneNumbers.add(phoneNumber); }

    public String getPhotoUri() { return photoUri; }
    public void setPhotoUri(String photoUri) { this.photoUri = photoUri; }

    public boolean isBlacklisted() { return blacklisted; }
    public void setBlacklisted(boolean blacklisted) { this.blacklisted = blacklisted; }

    public long getGroupId() { return groupId; }
    public void setGroupId(long groupId) { this.groupId = groupId; }

    public String getPrimaryPhoneNumber() {
        if (phoneNumbers != null && !phoneNumbers.isEmpty()) {
            return phoneNumbers.get(0).getNumber();
        }
        return null;
    }

    public String getInitial() {
        if (name != null && !name.isEmpty()) {
            return String.valueOf(name.charAt(0)).toUpperCase();
        }
        return "?";
    }
}
