package com.appoapp.sukhmanisahib.model;

import java.util.Map;

public class HukamnamaModel {

    private String dateAdded;
    private Boolean noteAdded;
    private Map<String, String> notes;

    public HukamnamaModel() {
        // required empty constructor
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public Boolean getNoteAdded() {
        return noteAdded;
    }

    // ✅ NAME MUST MATCH FIELD → "notes"
    public Map<String, String> getNotes() {
        return notes;
    }
}

