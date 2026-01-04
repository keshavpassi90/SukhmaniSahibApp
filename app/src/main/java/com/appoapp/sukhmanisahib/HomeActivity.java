package com.appoapp.sukhmanisahib;

import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.appoapp.sukhmanisahib.databinding.ActivityHomeBinding;

public class HomeActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityHomeBinding binding;
    NavController        navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        MobileAds.initialize(this, initializationStatus -> {});


        AdRequest adRequest = new AdRequest.Builder().build();
        binding.adView.loadAd(adRequest);

        // 1⃣ Grab the NavHostFragment defined in activity_home.xml
        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment == null) {
            throw new RuntimeException("NavHostFragment not found");
        }
        // 2⃣ Get its NavController
        navController = navHostFragment.getNavController();

        NavigationUI.setupWithNavController(binding.bottomNavigation, navController);

        // (Optional) If you ever need manual tab change callbacks:
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
            return handled;
        });

        int selectedColor = ContextCompat.getColor(this, R.color.theme_selected_nav);
        int unSelectedColor = ContextCompat.getColor(this, R.color.theme_unselected_nav);

        int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{-android.R.attr.state_checked}
        };

        int[] colors = new int[]{
                selectedColor,
                unSelectedColor
        };

        ColorStateList colorStateList = new ColorStateList(states, colors);

        binding.bottomNavigation.setItemIconTintList(colorStateList);
        binding.bottomNavigation.setItemTextColor(colorStateList);
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.settingFragment || destination.getId() == R.id.detailFragment|| destination.getId() == R.id.allNitenamsFragment|| destination.getId() == R.id.detailWallpaperFragment|| destination.getId() == R.id.webViewFragment) { // yahan apne fragment ka ID daalo
                binding.bottomNavigation.setVisibility(View.GONE);
              if(destination.getId() ==R.id.settingFragment|| destination.getId() == R.id.webViewFragment){
                  binding.adView.setVisibility(View.GONE);   // ✅ AD HIDE

              }else{
                  binding.adView.setVisibility(View.VISIBLE);   // ✅ AD HIDE

              }
            } else {
                binding.bottomNavigation.setVisibility(View.VISIBLE);
                binding.adView.setVisibility(View.VISIBLE);   // ✅ AD HIDE

            }
        });

    }

    @Override
    protected void onPause() {
        if (binding.adView != null) binding.adView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (binding.adView != null) binding.adView.resume();
    }

    @Override
    protected void onDestroy() {
        if (binding.adView != null) binding.adView.destroy();
        super.onDestroy();
    }


}