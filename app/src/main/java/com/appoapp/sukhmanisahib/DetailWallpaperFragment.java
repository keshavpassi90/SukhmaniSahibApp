package com.appoapp.sukhmanisahib;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.appoapp.sukhmanisahib.databinding.FragmentDetailWallpaperBinding;
import com.appoapp.sukhmanisahib.model.AppTextModel;
import com.appoapp.sukhmanisahib.model.NitnemModel;
import com.appoapp.sukhmanisahib.repos.AppRepository;
import com.appoapp.sukhmanisahib.utlis.LanguagePref;
import com.appoapp.sukhmanisahib.utlis.NetworkUtil;
import com.appoapp.sukhmanisahib.utlis.NoInternetDialog;
import com.appoapp.sukhmanisahib.utlis.SetWallpaperTask;
import com.bumptech.glide.Glide;

import java.util.Map;

public class DetailWallpaperFragment extends Fragment {
    private String imageURL;
    private String selectedLanguage;

    private FragmentDetailWallpaperBinding binding;
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentDetailWallpaperBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }
    private void showLoader() {
        binding.progressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoader() {
        binding.progressBar.setVisibility(View.GONE);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String lang = LanguagePref.getLanguage(requireActivity());
        if (lang != null && !lang.isEmpty()) {
            // value available
            Log.d("LANG", "Saved language = " + lang);
            selectedLanguage =lang;
        } else {
            // value not saved yet → default language
            selectedLanguage = "pa";
        }
        if (getArguments() != null) {
            imageURL = getArguments().getString("imageURL");
        }

        Uri uri = Uri.parse(imageURL);
        Log.e("Data","URL == "+imageURL);
        Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.splash_logo) // optional
                .into(binding.wallpaperIMG);


        showLoader();
        getWallpaperText();

        binding.backRL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(DetailWallpaperFragment.this).popBackStack();
            }
        });

        binding.setWallPaperCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SetWallpaperTask(getActivity()).execute(imageURL);

            }
        });
    }
    // to get all the app text for Wallpaper only
    void getWallpaperText(){
        AppRepository.observeAppText(
                new AppRepository.Callback() {


                    @Override
                    public void onSuccess(AppTextModel model) {
                        if (model == null || model.getWallpaper() == null) return;
                        Map<String, Map<String, String>> homeData = model.getWallpaper();

                        // 🔴 heading
                        Map<String, String> headingData = homeData.get("title");
                        String headingText = headingData.get(selectedLanguage);
                        if (headingText == null) {
                            headingText = headingData.values().iterator().next();
                        }
                        binding.headingTV.setText(headingText);
                        // 🔴 heading
                        Map<String, String> buttonTextData = homeData.get("buttonText");
                        String buttonText = buttonTextData.get(selectedLanguage);
                        if (buttonText == null) {
                            buttonText = buttonTextData.values().iterator().next();
                        }
                        binding.wallpaperBTN.setText(buttonText);

                        hideLoader();
                    }

                    @Override
                    public void onError(Exception e) {
                        hideLoader();
                        // 🔴 Sirf tab popup dikhao jab net bhi off ho
                        if (!NetworkUtil.isInternetAvailable(requireActivity())) {
                            NoInternetDialog.show(requireActivity());
                        } else {
                            Toast.makeText(requireActivity(),
                                    "Failed to load App Text",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });

    }

}
