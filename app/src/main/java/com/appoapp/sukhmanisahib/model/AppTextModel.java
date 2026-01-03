package com.appoapp.sukhmanisahib.model;

import java.util.Map;

public class AppTextModel {

    // home -> heading -> en/pa
    // home -> dailyHukumnama -> en/pa
    private Map<String, Map<String, String>> home;
    private Map<String, Map<String, String>> settings;
    private Map<String, Map<String, String>> shabad;
    private Map<String, Map<String, String>> wallpaper;

    public AppTextModel() {}

    public Map<String, Map<String, String>> getHome() {
        return home;
    }
    public Map<String, Map<String, String>> getSettings() {
        return settings;
    }
    public Map<String, Map<String, String>> getShabad() {
        return shabad;
    }
    public Map<String, Map<String, String>> getWallpaper() {
        return wallpaper;
    }
}
