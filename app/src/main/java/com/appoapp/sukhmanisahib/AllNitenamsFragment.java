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
import com.appoapp.sukhmanisahib.databinding.FragmentAllNitenamsBinding;
import com.appoapp.sukhmanisahib.databinding.FragmentHomeBinding;
import com.appoapp.sukhmanisahib.model.AppTextModel;
import com.appoapp.sukhmanisahib.model.NitnemModel;
import com.appoapp.sukhmanisahib.repos.AppRepository;
import com.appoapp.sukhmanisahib.repos.NitnemRepository;
import com.appoapp.sukhmanisahib.utlis.LanguagePref;
import com.appoapp.sukhmanisahib.utlis.NetworkUtil;
import com.appoapp.sukhmanisahib.utlis.NoInternetDialog;

import java.util.ArrayList;
import java.util.Map;

public class AllNitenamsFragment extends Fragment {
    private String selectedLanguage;

    private FragmentAllNitenamsBinding binding;
    public ArrayList<NitnemModel> baniList = new ArrayList<NitnemModel>();

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentAllNitenamsBinding.inflate(inflater, container, false);
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
        getHomeText();
        binding.backRL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(AllNitenamsFragment.this).popBackStack();
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
                        Map<String, String> titlesData = homeData.get("titles");
                        String titlesText = titlesData.get(selectedLanguage);
                        if (titlesText == null) {
                            titlesText = titlesData.values().iterator().next();
                        }
                        binding.headingTV.setText(titlesText);



                        getNitenamsList();

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
        binding.nitenamesRV.setLayoutManager(new LinearLayoutManager(requireActivity()));
        NitnemAdapter adapter = new NitnemAdapter(baniList,
                (model, position) -> {

                    Bundle bundle = new Bundle();
                    bundle.putParcelable("nitnemModel", model);

                    NavHostFragment.findNavController(this)
                            .navigate(R.id.detailFragment, bundle);
                },selectedLanguage);
        binding.nitenamesRV.setAdapter(adapter);

    }


}
