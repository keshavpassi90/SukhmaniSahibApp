package com.appoapp.sukhmanisahib.utlis;

import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseRepository {

    private static FirebaseFirestore db;

    public static FirebaseFirestore getDB() {
        if (db == null) {
            db = FirebaseFirestore.getInstance();
        }
        return db;
    }
}

