package com.appoapp.sukhmanisahib;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.appoapp.sukhmanisahib.adapter.NitnemAdapter;
import com.appoapp.sukhmanisahib.databinding.FragmentHomeBinding;
import com.appoapp.sukhmanisahib.model.AppTextModel;
import com.appoapp.sukhmanisahib.model.HukamnamaModel;
import com.appoapp.sukhmanisahib.model.NitnemModel;
import com.appoapp.sukhmanisahib.repos.AppRepository;
import com.appoapp.sukhmanisahib.repos.HukamnamaRepository;
import com.appoapp.sukhmanisahib.repos.NitnemRepository;
import com.appoapp.sukhmanisahib.utlis.LanguagePref;
import com.appoapp.sukhmanisahib.utlis.NetworkUtil;
import com.appoapp.sukhmanisahib.utlis.NoInternetDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {
    private String selectedLanguage;

    private FragmentHomeBinding binding;
    public ArrayList<NitnemModel> baniList = new ArrayList<NitnemModel>();

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    private void showLoader() {
        binding.progressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoader() {
        binding.progressBar.setVisibility(View.GONE);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
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
        getHomeText();
        binding.settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(HomeFragment.this)
                        .navigate(R.id.settingFragment);

            }
        });

        binding.seeAllCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(HomeFragment.this)
                        .navigate(R.id.allNitenamsFragment);

            }
        });
    }

    // get the status
    void getDailyStatus() {
        HukamnamaRepository.observeTodayHukamnama(
                new HukamnamaRepository.Callback() {

                    @Override
                    public void onSuccess(HukamnamaModel model) {

                        if (model == null || model.getNotes() == null) {
                            binding.noteTV.setText("No data");
                            return;
                        }

                        Map<String, String> textMap = model.getNotes();

                        // 🔁 dynamic language (example)
                        String text = textMap.get(selectedLanguage);
                        if (text == null) {
                            text = textMap.values().iterator().next();
                        }

                        String dateAdded = model.getDateAdded();

                        binding.dateTV.setText(dateAdded);

                        binding.noteTV.setText(text);
                        getNitenamsList();
                    }

                    @Override
                    public void onError(Exception e) {
                        hideLoader();
                        Toast.makeText(requireActivity(),
                                "Failed to load Hukamnama",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // to get all the app text for home only
    void getHomeText(){
        AppRepository.observeAppText(
                new AppRepository.Callback() {


                    @Override
                    public void onSuccess(AppTextModel model) {
                        if (model == null || model.getHome() == null) return;
                        Map<String, Map<String, String>> homeData = model.getHome();

                        // 🔴 heading
                        Map<String, String> headingData = homeData.get("heading");
                        String headingText = headingData.get(selectedLanguage);
                        if (headingText == null) {
                            headingText = headingData.values().iterator().next();
                        }
                        binding.headingTV.setText(headingText);

                        // 🔴 sub heading
                        Map<String, String> subheadingData = homeData.get("subHeading");
                        String subHeadingText = subheadingData.get(selectedLanguage);
                        if (subHeadingText == null) {
                            subHeadingText = subheadingData.values().iterator().next();
                        }
                        binding.subheadingTV.setText(subHeadingText);

                        // 🔴 sub heading
                        Map<String, String> imageTextData = homeData.get("image");
                        String imageText = imageTextData.get(selectedLanguage).replace("\\n", "\n")
                                .replace("\\\"", "\"")
                                .replace("\\\\", "\\"); // extra backslashes

                        binding.waheguruJiTV.setText(imageText);

                        // 🔴 sub heading
                        Map<String, String> dailyStatusData = homeData.get("dailyHukumnama");
                        String dailyStatusText = dailyStatusData.get(selectedLanguage);
                        if (dailyStatusText == null) {
                            dailyStatusText = dailyStatusData.values().iterator().next();
                        }
                        binding.headingHukumanamTV.setText(dailyStatusText);
                        // 🔴 sub titles
                        Map<String, String> titlesData = homeData.get("titles");
                        String titlesText = titlesData.get(selectedLanguage);
                        if (titlesText == null) {
                            titlesText = titlesData.values().iterator().next();
                        }
                        binding.nitenamesTV.setText(titlesText);
 // 🔴 view all button
                        Map<String, String> seeAllData = homeData.get("seeAllBtn");
                        String seeAllText = seeAllData.get(selectedLanguage);
                        if (seeAllText == null) {
                            seeAllText = seeAllData.values().iterator().next();
                        }
                        binding.nitenamsBTN.setText(seeAllText);


                        getDailyStatus();

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

    // get all nitenams list
    void getNitenamsList(){
        NitnemRepository.observeNitnams(new NitnemRepository.Callback() {
            @Override
            public void onSuccess(ArrayList<NitnemModel> list) {
                int limit = Math.min(3, list.size());
                baniList = new ArrayList<>(list.subList(0, limit));
                setUpAdapter();
                hideLoader();
            }

            @Override
            public void onError(Exception e) {
                hideLoader();
                // handle error
                Toast.makeText(requireActivity(),
                        "Failed to load Nitenams",
                        Toast.LENGTH_SHORT).show();

            }
        });

    }

    // setUpAdapter
    void setUpAdapter(){


        binding.nitenamesRV.setLayoutManager(new LinearLayoutManager(requireActivity()));
        binding.nitenamesRV.setNestedScrollingEnabled(false);
        NitnemAdapter adapter = new NitnemAdapter(baniList,
                (model, position) -> {

                    Bundle bundle = new Bundle();
                    bundle.putParcelable("nitnemModel", model);

                    NavHostFragment.findNavController(this)
                            .navigate(R.id.detailFragment, bundle);
                },selectedLanguage);
        binding.nitenamesRV.setAdapter(adapter);

    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        HukamnamaRepository.removeListener();
        binding = null;
    }




}