package com.appoapp.sukhmanisahib.model;

public class ChooseLanguageModel {
    private String code;
    private String title;
    // 🔹 REQUIRED empty constructor (Firestore)
    public ChooseLanguageModel() {}
    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

}
