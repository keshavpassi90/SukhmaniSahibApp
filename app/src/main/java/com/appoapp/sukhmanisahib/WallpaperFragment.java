package com.appoapp.sukhmanisahib;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appoapp.sukhmanisahib.adapter.ImageGridAdapter;
import com.appoapp.sukhmanisahib.databinding.FragmentWallpaperBinding;
import com.appoapp.sukhmanisahib.model.AppTextModel;
import com.appoapp.sukhmanisahib.repos.AppRepository;
import com.appoapp.sukhmanisahib.retrofit.RetrofitClient;
import com.appoapp.sukhmanisahib.retrofit.wallpaper_model.PhotosItem;
import com.appoapp.sukhmanisahib.retrofit.wallpaper_model.WallPaperModel;
import com.appoapp.sukhmanisahib.utlis.DownloadImageTask;
import com.appoapp.sukhmanisahib.utlis.LanguagePref;
import com.appoapp.sukhmanisahib.utlis.NetworkUtil;
import com.appoapp.sukhmanisahib.utlis.NoInternetDialog;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WallpaperFragment extends Fragment {

    private FragmentWallpaperBinding binding;
    private String selectedLanguage;
    private static final int REQUEST_WRITE_PERMISSION = 101;
    private String fileName ="";
    private String selectedImageURl ="";
    private boolean isLoading = false;
    private int currentPage = 1;
    private final int visibleThreshold = 2;
    private boolean isPaginationWork = false;
    private List<PhotosItem> imageUrls;
    private ImageGridAdapter adapter;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentWallpaperBinding.inflate(inflater, container, false);
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
        getWallpaperText();
        binding.settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(WallpaperFragment.this)
                        .navigate(R.id.settingFragment);

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
hideLoader();
                        fetchWallPaperImage();
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

    // fetch API to get the wallpaper
    private void fetchWallPaperImage() {
        if (!NetworkUtil.isInternetAvailable(requireContext())) {
            NoInternetDialog.show(requireActivity());
            return;
        }
        if(isLoading == false){
            showLoader();
        }

        RetrofitClient.getApi()
                .searchPhotos("sikhism", currentPage, 10)
                .enqueue(new Callback<WallPaperModel>() {
                    @Override
                    public void onResponse(Call<WallPaperModel> call, Response<WallPaperModel> res) {
                        if(isLoading == false){
                          hideLoader();
                        }

                        if (res.isSuccessful() && res.body() != null ) {
                            WallPaperModel model = res.body();
                            if(model.getNextPage()==null){
                                isPaginationWork =true;
                            }

                            if(isLoading == false){
                                imageUrls = model.getPhotos();
                                setUpAdapter();
                            }else{
                                binding.loader.setVisibility(View.GONE);
                                imageUrls.addAll(model.getPhotos());
                                adapter.notifyDataSetChanged();
                                isLoading=false;

                            }


                        } else {
                            toast("Empty or error: " + res.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<WallPaperModel> call, Throwable throwable) {
                        if(isLoading == false){
                            hideLoader();
                        }
                        toast("Failed: " + throwable.getMessage());
                    }


                });
    }


    // set up adapter
    void setUpAdapter(){
// 2. Set layout manager for 2-column grid
       binding. wallPaperRV.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        // 4. Initialize Adapter with click listener
        adapter = new ImageGridAdapter(getActivity(), imageUrls, new ImageGridAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String imageUrl, int position,boolean isDownload) {
                if(isDownload==true){
// If you use AndroidX Lifecycle and Coroutines support in Java (or run on background thread yourself):
                    Calendar calendar = Calendar.getInstance();
                    fileName ="SundarGutka"+calendar.getTimeInMillis()+".png";
                    selectedImageURl=imageUrl;
                    checkPermissionAndDownload();
                }else{

                    Bundle bundle = new Bundle();
                    bundle.putString("imageURL", imageUrl);

                    NavHostFragment.findNavController(WallpaperFragment.this)
                            .navigate(R.id.detailWallpaperFragment, bundle);
                }

            }
        });

        // 5. Set adapter to RecyclerView
        binding.wallPaperRV.setAdapter(adapter);


        // 6. pagination
        binding.wallPaperRV.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (isPaginationWork == false) {
                    GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                    if (layoutManager == null) return;

                    int totalItemCount = layoutManager.getItemCount();
                    int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();

                    if (!isLoading && totalItemCount <= (lastVisibleItemPosition + visibleThreshold)) {
                        // Load more
                        isLoading = true;
                        binding.loader .setVisibility(View.VISIBLE);
                        loadMoreImages();

                    }
                }
            }
        });
    }

    private void loadMoreImages() {
        // Simulate loading delay (use real API in production)
        new Handler().postDelayed(() -> {
            currentPage++;
            fetchWallPaperImage();

        }, 2000);
    }

    private void toast(String m) {
        Toast.makeText(getActivity(), "Error: " + m, Toast.LENGTH_SHORT).show();

    }

    private void checkPermissionAndDownload() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
            } else {
                startDownload();
            }
        } else {
            startDownload();
        }
    }

    private void startDownload() {
        new DownloadImageTask(requireContext(), fileName).execute(selectedImageURl);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startDownload();
            } else {
                showSettingsDialog();
            }
        }
    }

    private void showSettingsDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Permission Required")
                .setMessage("Storage permission is required to save images. Please enable it in settings.")
                .setPositiveButton("Open Settings", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}