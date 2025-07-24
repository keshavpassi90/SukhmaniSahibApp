package com.appoapp.sukhmanisahib;

import android.os.Handler;

public class PlayerConstants {
    //song number which is playing right now from SONGS_LIST
    public static String SONG_NAME = "";
    public static String SONG_URL = "";
    public static String SONG_TITLE = "";
    //song is playing or paused
    public static boolean SONG_STOPPED= true;
    public static boolean SONG_PAUSED= false;
    //song changed (next, previous)
    public static boolean SONG_CHANGED = false;
    //handler for song changed(next, previous) defined in service(SongService)
    public static Handler SONG_CHANGE_HANDLER;
    //handler for song play/pause defined in service(SongService)
    public static Handler PLAY_PAUSE_HANDLER;
    public static Handler PLAY_STOP_HANDLER;
    //handler for showing song progress defined in Activities(MainActivity, AudioPlayerActivity)
    public static Handler PROGRESSBAR_HANDLER;
}
