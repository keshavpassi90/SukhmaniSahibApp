package com.appoapp.sukhmanisahib.repos;

import com.appoapp.sukhmanisahib.model.ChooseLanguageModel;
import com.appoapp.sukhmanisahib.model.ChooseLanguageWrapper;
import com.appoapp.sukhmanisahib.model.NitnemModel;
import com.appoapp.sukhmanisahib.model.NitnemWrapper;
import com.appoapp.sukhmanisahib.utlis.FirebaseRepository;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;




public class ChooseLanguageRepository {

    public interface Callback {
        void onSuccess(ArrayList<ChooseLanguageModel> list);
        void onError(Exception e);
    }

    private static ListenerRegistration listener;

    // 🔴 START REALTIME LISTENER
    public static void observeChooseLanguage(Callback callback) {

        listener = FirebaseRepository.getDB()
                .collection("chooseLanguages")
                .document("languages")
                .addSnapshotListener((snapshot, e) -> {

                    if (e != null) {
                        callback.onError(e);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {

                        ChooseLanguageWrapper wrapper =
                                snapshot.toObject(ChooseLanguageWrapper.class);


                        if (wrapper != null && wrapper.getLanguages() != null) {

                            // 🔥 List → ArrayList
                            ArrayList<ChooseLanguageModel> list =
                                    new ArrayList<>(wrapper.getLanguages());

                            callback.onSuccess(list);
                        }                    }
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

