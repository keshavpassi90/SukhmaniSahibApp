package com.appoapp.sukhmanisahib.utlis;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

public class NoInternetDialog {

    public static void show(Context context) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setTitle("No Internet Connection");
        builder.setMessage("Please turn on WiFi or Mobile Data to continue.");

        builder.setPositiveButton("Settings", (dialog, which) -> {
            context.startActivity(
                    new Intent(Settings.ACTION_WIRELESS_SETTINGS)
            );
        });

        builder.setNegativeButton("Retry", (dialog, which) -> dialog.dismiss());

        builder.show();
    }
}
