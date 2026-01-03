package com.appoapp.sukhmanisahib;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

public class SplashActivity extends AppCompatActivity {
    private static final long FALLBACK_TIMEOUT_MS = 3000; // 3 seconds fallback
    private static final int REQUEST_NOTIFICATION_PERMISSION = 101;

    private boolean finished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Ask for notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION
                );
                return; // wait for result before proceeding
            }
        }

        proceedWithSplash();

    }

    // Handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            // Continue regardless of grant or deny
            proceedWithSplash();
        }
    }

    // Main splash flow
    private void proceedWithSplash() {

//        // Try to load and show app open ad
//        AppOpenAdManager.loadAdAndThen(
//                this,
//                () -> AppOpenAdManager.showIfAvailable(
//                        SplashActivity.this,
//                        this::goToMain // Callback after dismiss or failure
//                )
//        );
//
        // Fallback: go to HomeActivity if ad not shown in time
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!finished) {
                goToMain();
            }
        }, FALLBACK_TIMEOUT_MS);
    }

    // Move to main screen
    private void goToMain() {
        if (finished) return;
        finished = true;
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }
}



