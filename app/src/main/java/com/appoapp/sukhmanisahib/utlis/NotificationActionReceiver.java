package com.appoapp.sukhmanisahib.utlis;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        Intent serviceIntent = new Intent(context, MusicPlayerService.class);
        serviceIntent.putExtra("ACTION", action);

        context.startService(serviceIntent);
    }
}