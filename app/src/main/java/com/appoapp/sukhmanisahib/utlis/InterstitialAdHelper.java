package com.appoapp.sukhmanisahib.utlis;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.appoapp.sukhmanisahib.R;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class InterstitialAdHelper {

    private static InterstitialAd mInterstitialAd;
    private static int adCounter = 0;  // Count user actions
    private static final int SHOW_AFTER_COUNT = 4; // Show every 3rd action

    public interface AdCallback {
        void onAdFinished(); // Run this after ad is closed or failed
    }

    public static void loadAd(Context context) {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(context,
                context.getString(R.string.interstitial_ad_id),
                adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        mInterstitialAd = null;
                    }
                });
    }

    public static void showAdIfAvailable(Activity activity, AdCallback callback) {
        adCounter++;

        // Only show after certain actions
        if (adCounter % SHOW_AFTER_COUNT != 0) {
            if (callback != null) callback.onAdFinished();
            return;
        }

        if (mInterstitialAd != null) {
            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    mInterstitialAd = null;
                    loadAd(activity); // Preload next ad
                    if (callback != null) callback.onAdFinished();
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    mInterstitialAd = null;
                    if (callback != null) callback.onAdFinished();
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    mInterstitialAd = null;
                }
            });

            mInterstitialAd.show(activity);
        } else {
            if (callback != null) callback.onAdFinished(); // No ad, continue
        }
    }
}
