package com.appoapp.sukhmanisahib.repos;

import com.appoapp.sukhmanisahib.model.AppTextModel;
import com.appoapp.sukhmanisahib.utlis.FirebaseRepository;
import com.google.firebase.firestore.ListenerRegistration;

public class AppRepository {

    public interface Callback {
        void onSuccess(AppTextModel model);
        void onError(Exception e);
    }

    private static ListenerRegistration listener;

    // 🔴 START REALTIME LISTENER
    public static void observeAppText(AppRepository.Callback callback) {

        listener = FirebaseRepository.getDB()
                .collection("AllAppText")
                .document("apptext")
                .addSnapshotListener((snapshot, e) -> {

                    // 🔴 REAL ERROR (permission, etc.)
                    if (e != null) {
                        callback.onError(e);
                        return;
                    }

                    // 🔴 NO DATA CASE (offline + no cache)
                    if (snapshot == null || !snapshot.exists()) {
                        callback.onError(
                                new Exception("No data available (offline & no cache)")
                        );
                        return;
                    }

                    // 🔴 SUCCESS (online OR cached)
                    AppTextModel model = snapshot.toObject(AppTextModel.class);
                    callback.onSuccess(model);
                });
    }

    // 🔴 STOP LISTENER (Memory leak safe)
    public static void removeListener() {
        if (listener != null) {
            listener.remove();
            listener = null;
        }
    }
}

