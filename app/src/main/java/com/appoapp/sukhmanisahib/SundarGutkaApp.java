package com.appoapp.sukhmanisahib;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class SundarGutkaApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // ✅ Initialize Firebase first
        FirebaseApp.initializeApp(this);

        FirebaseFirestoreSettings settings =
                new FirebaseFirestoreSettings.Builder()
                        .setPersistenceEnabled(true)
                        .build();

        FirebaseFirestore.getInstance().setFirestoreSettings(settings);
    }
}
