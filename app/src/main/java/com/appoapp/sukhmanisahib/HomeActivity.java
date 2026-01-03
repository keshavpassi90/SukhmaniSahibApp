package com.appoapp.sukhmanisahib;

import android.content.res.ColorStateList;
import android.os.Bundle;

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

        // 1⃣ Grab the NavHostFragment defined in activity_home.xml
        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

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
            if (destination.getId() == R.id.settingFragment || destination.getId() == R.id.detailFragment|| destination.getId() == R.id.allNitenamsFragment|| destination.getId() == R.id.detailWallpaperFragment) { // yahan apne fragment ka ID daalo
                binding.bottomNavigation.setVisibility(View.GONE);
            } else {
                binding.bottomNavigation.setVisibility(View.VISIBLE);
            }
        });

    }


}