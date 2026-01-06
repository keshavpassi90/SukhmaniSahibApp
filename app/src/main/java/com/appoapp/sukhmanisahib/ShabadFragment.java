package com.appoapp.sukhmanisahib;

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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.appoapp.sukhmanisahib.adapter.NitnemAdapter;
import com.appoapp.sukhmanisahib.databinding.FragmentShabadBinding;
import com.appoapp.sukhmanisahib.model.AppTextModel;
import com.appoapp.sukhmanisahib.model.NitnemModel;
import com.appoapp.sukhmanisahib.repos.AppRepository;
import com.appoapp.sukhmanisahib.repos.NitnemRepository;
import com.appoapp.sukhmanisahib.repos.ShabadRepository;
import com.appoapp.sukhmanisahib.utlis.LanguagePref;
import com.appoapp.sukhmanisahib.utlis.NetworkUtil;
import com.appoapp.sukhmanisahib.utlis.NoInternetDialog;

import java.util.ArrayList;
import java.util.Map;

public class ShabadFragment extends Fragment {
    private String selectedLanguage;

    private FragmentShabadBinding binding;
    public ArrayList<NitnemModel> baniList = new ArrayList<NitnemModel>();

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentShabadBinding.inflate(inflater, container, false);
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
        showLoader();
        getShabadText();

        binding.settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(ShabadFragment.this)
                        .navigate(R.id.settingFragment);

            }
        });

    }


    // to get all the app text for home only
    void getShabadText(){
        AppRepository.observeAppText(
                new AppRepository.Callback() {


                    @Override
                    public void onSuccess(AppTextModel model) {
                        if (model == null || model.getShabad() == null) return;
                        Map<String, Map<String, String>> homeData = model.getShabad();

                        // 🔴 heading
                        Map<String, String> headingData = homeData.get("title");
                        String headingText = headingData.get(selectedLanguage);
                        if (headingText == null) {
                            headingText = headingData.values().iterator().next();
                        }
                        binding.headingTV.setText(headingText);

                        getShabadList();
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

    // get all Shabad list
    void getShabadList(){
        ShabadRepository.observeShabad(new ShabadRepository.Callback() {
            @Override
            public void onSuccess(ArrayList<NitnemModel> list) {
                baniList = list;
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


        binding.shabadRV.setLayoutManager(new LinearLayoutManager(requireActivity()));
        NitnemAdapter adapter = new NitnemAdapter(baniList,
                (model, position) -> {

                    Bundle bundle = new Bundle();
                    bundle.putParcelable("nitnemModel", model);

                    NavHostFragment.findNavController(this)
                            .navigate(R.id.detailFragment, bundle);
                },selectedLanguage);
        binding.shabadRV.setAdapter(adapter);

    }


}
