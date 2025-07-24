package com.appoapp.sukhmanisahib;

import android.content.Context;

public class Controls {
    static String LOG_CLASS = "Controls";
    public static void playControl(Context context) {
        sendMessage(context.getResources().getString(R.string.play));
    }

    public static void stopControl(Context context) {
        sendMessage(context.getResources().getString(R.string.stop));
    }

    public static void cancelControl(Context context) {
        sendMessage(context.getResources().getString(R.string.cancelNotify));
    }


    private static void sendMessage(String message) {
        try{
            PlayerConstants.PLAY_STOP_HANDLER.sendMessage(PlayerConstants.PLAY_STOP_HANDLER.obtainMessage(0, message));
        }catch(Exception e){}
    }
}
