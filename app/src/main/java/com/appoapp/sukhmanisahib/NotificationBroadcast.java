package com.appoapp.sukhmanisahib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        if (intent.getAction().equals(MusicService.NOTIFY_PLAY)) {
//            Controls.playControl(context);
//        } else if (intent.getAction().equals(MusicService.NOTIFY_DELETE)) {
//            Controls.cancelControl(context);
//            Intent i = new Intent(context, MusicService.class);
//            context.stopService(i);
//            Intent in = new Intent(context, MainActivity.class);
//            in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(in);
//        } else if (intent.getAction().equals(MusicService.NOTIFY_STOP)) {
//            Controls.stopControl(context);
//        }
    }

    public String ComponentName() {
        return this.getClass().getName();
    }
}
