package com.appoapp.sukhmanisahib;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends AppCompatActivity {
    private static final int REQUEST_POST_NOTIFICATIONS = 1;
    private AppOpenAd appOpenAd;
    private boolean isLoadingAd = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                // Configuration or initialization logic if needed
            }
        });

        // Setting up test device IDs
        List<String> testDevices = new ArrayList<>();
        testDevices.add(getResources().getString(R.string.testDevice));

        RequestConfiguration requestConfiguration = new RequestConfiguration.Builder()
                .setTestDeviceIds(testDevices)
                .build();
        MobileAds.setRequestConfiguration(requestConfiguration);

        // Load the App Open Ad
        loadAppOpenAd();

        // Handler to start the MainActivity
        new Handler().postDelayed(() -> {
            // Check and request notification permission
            notificationPermissionCheck();
        }, 5000); // Delay before checking the ad status
    }

    private void loadAppOpenAd() {
        if (isLoadingAd) return;

        isLoadingAd = true;
        AdRequest adRequest = new AdRequest.Builder().build();

        AppOpenAd.load(this, getResources().getString(R.string.app_open_id), adRequest,
                new AppOpenAd.AppOpenAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull AppOpenAd ad) {
                        appOpenAd = ad;
                        isLoadingAd = false;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                        Log.e("ERROR", "App Open Ad failed to load: " + adError.getMessage());
                        isLoadingAd = false;
                    }
                });
    }

    private void startMainActivity() {
        Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    // Notification permission check
    void notificationPermissionCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted, request it
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_POST_NOTIFICATIONS);
            } else {
                // Permission is already granted, handle the ad logic
                handleAdLogic();
            }
        } else {
            // Permission is not needed on older versions, handle the ad logic
            handleAdLogic();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted, handle the ad logic
                handleAdLogic();
            } else {
                // Permission is denied, proceed without ads
                startMainActivity();
            }
        }
    }

    // Handle the ad logic based on ad status
    private void handleAdLogic() {
        if (appOpenAd == null) {
            startMainActivity();
        } else {
            appOpenAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    appOpenAd = null; // Clear the ad reference
                    startMainActivity(); // Proceed to the main activity
                    loadAppOpenAd(); // Load another ad for the next time
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    Log.e("ERROR", "Ad failed to show: " + adError.getMessage());
                    appOpenAd = null; // Clear the ad reference
                    startMainActivity(); // Proceed to the main activity
                }
            });
            appOpenAd.show(SplashActivity.this);
        }
    }
}
