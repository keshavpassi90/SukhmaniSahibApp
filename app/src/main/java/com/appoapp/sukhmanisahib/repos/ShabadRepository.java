package com.appoapp.sukhmanisahib.repos;

import com.appoapp.sukhmanisahib.model.NitnemModel;
import com.appoapp.sukhmanisahib.model.NitnemWrapper;
import com.appoapp.sukhmanisahib.utlis.FirebaseRepository;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;

public class ShabadRepository {
    public interface Callback {
        void onSuccess(ArrayList<NitnemModel> list);
        void onError(Exception e);
    }

    private static ListenerRegistration listener;

    // 🔴 START REALTIME LISTENER
    public static void observeShabad(ShabadRepository.Callback callback) {

        listener = FirebaseRepository.getDB()
                .collection("shabad")
                .document("shabadlist")
                .addSnapshotListener((snapshot, e) -> {

                    if (e != null) {
                        callback.onError(e);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {

                        NitnemWrapper wrapper =
                                snapshot.toObject(NitnemWrapper.class);


                        if (wrapper != null && wrapper.getNamsList() != null) {

                            // 🔥 List → ArrayList
                            ArrayList<NitnemModel> list =
                                    new ArrayList<>(wrapper.getNamsList());
                            callback.onSuccess(list);
                        }
                    }
                });
    }

    // 🔴 STOP LISTENER (Memory-leak safe)
    public static void removeListener() {
        if (listener != null) {
            listener.remove();
            listener = null;
        }
    }
}
