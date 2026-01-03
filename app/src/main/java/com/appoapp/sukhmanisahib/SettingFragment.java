package com.appoapp.sukhmanisahib;

import static androidx.core.app.ActivityCompat.recreate;

import android.app.AlertDialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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

import com.appoapp.sukhmanisahib.databinding.FragmentSettingsBinding;
import com.appoapp.sukhmanisahib.model.AppTextModel;
import com.appoapp.sukhmanisahib.model.ChooseLanguageModel;
import com.appoapp.sukhmanisahib.repos.AppRepository;
import com.appoapp.sukhmanisahib.repos.ChooseLanguageRepository;
import com.appoapp.sukhmanisahib.utlis.LanguagePref;
import com.appoapp.sukhmanisahib.utlis.NetworkUtil;
import com.appoapp.sukhmanisahib.utlis.NoInternetDialog;

import java.util.ArrayList;
import java.util.Map;

public class SettingFragment extends Fragment {
    private FragmentSettingsBinding binding;
    public ArrayList<ChooseLanguageModel> chooseLanguageArray = new ArrayList<ChooseLanguageModel>();
    private String chooseLanguageTitle;
    private String selectedLanguage;
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();

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
        showLoader();
        PackageInfo pInfo = null;
        try {

            pInfo = requireActivity().getPackageManager().getPackageInfo(requireActivity().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if(pInfo!=null){
            binding.textView9.setText(pInfo.versionName);
        }
        getSettingText();

binding.backRL.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        NavHostFragment.findNavController(SettingFragment.this).popBackStack();
    }
});

binding.listItemLanguage.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        showLanguageDialog();
    }
});
    }

    // to get all the app text for Settings only
    void getSettingText(){
        AppRepository.observeAppText(
                new AppRepository.Callback() {


                    @Override
                    public void onSuccess(AppTextModel model) {
                        if (model == null || model.getSettings() == null) return;
                        Map<String, Map<String, String>> homeData = model.getSettings();

                        // 🔴 heading
                        Map<String, String> headingData = homeData.get("heading");
                        String headingText = headingData.get(selectedLanguage);
                        if (headingText == null) {
                            headingText = headingData.values().iterator().next();
                        }
                        binding.headingTV.setText(headingText);

                        // 🔴 share app
                        Map<String, String> shareData = homeData.get("share");
                        String shareText = shareData.get(selectedLanguage);
                        if (shareText == null) {
                            shareText = shareData.values().iterator().next();
                        }

                        binding.textView4.setText(shareText);

                        // 🔴 share sub app
                        Map<String, String> shareSubData = homeData.get("shareSub");
                        String shareSubText = shareSubData.get(selectedLanguage);
                        if (shareSubText == null) {
                            shareSubText = shareSubData.values().iterator().next();
                        }

                        binding.textView5.setText(shareSubText);
// 🔴 rate app
                        Map<String, String> rateData = homeData.get("rate");
                        String rateText = rateData.get(selectedLanguage);
                        if (rateText == null) {
                            rateText = rateData.values().iterator().next();
                        }

                        binding.textView6.setText(rateText);
// 🔴 rate sub app
                        Map<String, String> rateSubData = homeData.get("rateSub");
                        String rateSubText = rateSubData.get(selectedLanguage);
                        if (rateSubText == null) {
                            rateSubText = rateSubData.values().iterator().next();
                        }

                        binding.textView8.setText(rateSubText);
// 🔴 feedback app
                        Map<String, String> feedbackData = homeData.get("feedback");
                        String feedbackText = feedbackData.get(selectedLanguage);
                        if (feedbackText == null) {
                            feedbackText = feedbackData.values().iterator().next();
                        }

                        binding.textView12.setText(feedbackText);

// 🔴 Privacy policy app
                        Map<String, String> privacyPolicyData = homeData.get("privacyPolicy");
                        String privacyPolicyText = privacyPolicyData.get(selectedLanguage);
                        if (privacyPolicyText == null) {
                            privacyPolicyText = privacyPolicyData.values().iterator().next();
                        }

                        binding.textView11.setText(privacyPolicyText);

// 🔴 Check Update app
                        Map<String, String> checkUpdateData = homeData.get("checkUpdate");
                        String checkUpdateText = checkUpdateData.get(selectedLanguage);
                        if (checkUpdateText == null) {
                            checkUpdateText = checkUpdateData.values().iterator().next();
                        }

                        binding.textView10.setText(checkUpdateText);
// 🔴 Check Update app
                        Map<String, String> appVersionData = homeData.get("appVersion");
                        String appVersionText = appVersionData.get(selectedLanguage);
                        if (appVersionText == null) {
                            appVersionText = appVersionData.values().iterator().next();
                        }

                        binding.textView7.setText(appVersionText);

// 🔴  app Language
                        Map<String, String> appLanguageData = homeData.get("languageHead");
                        String appLanguageText = appLanguageData.get(selectedLanguage);
                        if (appLanguageText == null) {
                            appLanguageText = appLanguageData.values().iterator().next();
                        }
                        chooseLanguageTitle =appLanguageText;
                        binding.languageTV.setText(appLanguageText);

                        getAllLanguages();
                    }

                    @Override
                    public void onError(Exception e) {
                        hideLoader();
                        Toast.makeText(requireActivity(),
                                "Failed to load App Text",
                                Toast.LENGTH_SHORT).show();

                    }
                });

    }

    // getting all languages List
    void getAllLanguages(){
        ChooseLanguageRepository.observeChooseLanguage(
                new ChooseLanguageRepository.Callback() {


                    @Override
                    public void onSuccess(ArrayList<ChooseLanguageModel> list) {
                        chooseLanguageArray =list;
                        for (ChooseLanguageModel model : chooseLanguageArray) {

                            if (model.getCode().equalsIgnoreCase(selectedLanguage)) {

                                binding.tvSelectedLanguage.setText(model.getTitle());
                                break;
                            }
                        }

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void showLanguageDialog() {


        String[] languages = new String[chooseLanguageArray.size()];
        int checkedItem = -1;

        String savedLang = LanguagePref.getLanguage(requireActivity());

        for (int i = 0; i < chooseLanguageArray.size(); i++) {
            languages[i] = chooseLanguageArray.get(i).getTitle();
            if (chooseLanguageArray.get(i).getCode().equals(savedLang)) {
                checkedItem = i;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(chooseLanguageTitle);

        builder.setSingleChoiceItems(languages, checkedItem,
                (dialog, which) -> {

                    ChooseLanguageModel selected = chooseLanguageArray.get(which);

                    // Save selected language
                    LanguagePref.setLanguage(requireActivity(), selected.getCode());

                    dialog.dismiss();

                    // Reload activity to apply text
                    LanguagePref.setLanguage(requireActivity(), selected.getCode());
                    selectedLanguage = selected.getCode();
                    showLoader();
                    getSettingText();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showLoader() {
        binding.progressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoader() {
        binding.progressBar.setVisibility(View.GONE);
    }

}
