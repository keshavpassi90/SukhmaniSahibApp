package com.appoapp.sukhmanisahib.repos;

import com.appoapp.sukhmanisahib.model.HukamnamaModel;
import com.appoapp.sukhmanisahib.utlis.FirebaseRepository;
import com.google.firebase.firestore.ListenerRegistration;

public class HukamnamaRepository {

    public interface Callback {
        void onSuccess(HukamnamaModel model);
        void onError(Exception e);
    }

    private static ListenerRegistration listener;

    // 🔴 START REALTIME LISTENER
    public static void observeTodayHukamnama(Callback callback) {

        listener = FirebaseRepository.getDB()
                .collection("DailyStatus")
                .document("status")
                .addSnapshotListener((snapshot, e) -> {

                    if (e != null) {
                        callback.onError(e);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        HukamnamaModel model =
                                snapshot.toObject(HukamnamaModel.class);
                        callback.onSuccess(model);
                    }
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
