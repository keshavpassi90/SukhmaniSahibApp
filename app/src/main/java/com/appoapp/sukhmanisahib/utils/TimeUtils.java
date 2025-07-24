package com.appoapp.sukhmanisahib.utils;

public class TimeUtils {
    public static String formatTime(long milliseconds) {
        long hours = milliseconds / (1000 * 60 * 60);
        long minutes = (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (milliseconds % (1000 * 60)) / 1000;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
