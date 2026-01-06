package com.appoapp.sukhmanisahib;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.appoapp.sukhmanisahib.repos.AppLaunchTracker;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class SundarGutkaApp extends Application implements Application.ActivityLifecycleCallbacks{
    private static SundarGutkaApp instance;

    private AppOpenAd appOpenAd;
    private Activity currentActivity;

    private boolean isAdShowing = false;
    private boolean splashReady = false;
    private boolean isFromBackground = false;
    private boolean isColdStart = true; // 🔥 IMPORTANT

    private int activityCount = 0;
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
        instance = this;
        MobileAds.initialize(this);
        registerActivityLifecycleCallbacks(this);
        loadAd();

        // 🔥 Track app launch here (once per app open)
        AppLaunchTracker.trackAppLaunch(this);

    }

    // ================= SPLASH SIGNAL =================
    public static void notifySplashReady() {
        if (instance != null) {
            instance.splashReady = true;
            instance.tryShowAd();
        }
    }

    // ================= LOAD AD =================
    private void loadAd() {
        if (appOpenAd != null) return;

        AppOpenAd.load(
                this,
                getString(R.string.app_open_id),
                new AdRequest.Builder().build(),
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                new AppOpenAd.AppOpenAdLoadCallback() {

                    @Override
                    public void onAdLoaded(@NonNull AppOpenAd ad) {
                        appOpenAd = ad;
                        tryShowAd();
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError error) {
                        openMainIfColdStart();
                    }
                }
        );
    }

    // ================= SHOW DECISION =================
    private void tryShowAd() {

        if (isAdShowing) return;
        if (appOpenAd == null) return;
        if (currentActivity == null) return;

        // 🔹 Cold start
        if (isColdStart && splashReady && currentActivity instanceof SplashActivity) {
            showAd();
            return;
        }

        // 🔹 Background → Foreground
        if (!isColdStart && isFromBackground) {
            showAd();
        }
    }

    private void showAd() {
        isAdShowing = true;
        isFromBackground = false;

        appOpenAd.setFullScreenContentCallback(new FullScreenContentCallback() {

            @Override
            public void onAdDismissedFullScreenContent() {
                cleanupAfterAd();
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                cleanupAfterAd();
            }
        });

        appOpenAd.show(currentActivity);
    }

    private void cleanupAfterAd() {
        appOpenAd = null;
        isAdShowing = false;
        splashReady = false;
        loadAd();

        if (isColdStart) {
            openMain();
            isColdStart = false;
        }
    }

    // ================= OPEN MAIN =================
    private void openMain() {
        Intent i = new Intent(this, HomeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

    private void openMainIfColdStart() {
        if (isColdStart) {
            openMain();
            isColdStart = false;
        }
    }

    // ================= LIFECYCLE =================
    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        activityCount++;
        if (activityCount == 1 && !isColdStart) {
            // Background → Foreground
            isFromBackground = true;
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        currentActivity = activity;
        tryShowAd();
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        activityCount--;
    }

    @Override public void onActivityCreated(Activity a, Bundle b) {}
    @Override public void onActivityPaused(Activity activity) {}
    @Override public void onActivitySaveInstanceState(Activity a, Bundle b) {}
    @Override public void onActivityDestroyed(Activity activity) {}

}
