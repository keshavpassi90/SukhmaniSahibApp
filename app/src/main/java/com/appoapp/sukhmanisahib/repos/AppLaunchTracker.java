package com.appoapp.sukhmanisahib.repos;


import android.os.Build;
import android.provider.Settings;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AppLaunchTracker {

    private static final String COLLECTION_NAME = "AppLaunch";

    public static void trackAppLaunch(android.content.Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String deviceName = Build.MANUFACTURER + " " + Build.MODEL;
        String androidId = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());

        Map<String, Object> data = new HashMap<>();
        data.put("deviceName", deviceName);
        data.put("androidId", androidId);
        data.put("dateTime", dateTime);

        db.collection(COLLECTION_NAME)
                .add(data)
                .addOnSuccessListener(doc -> {
                    // Optional: success log
                    // Log.d("AppLaunchTracker", "Logged: " + doc.getId());
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }
}

