package com.appoapp.sukhmanisahib;

import static android.content.ContentValues.TAG;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.appoapp.sukhmanisahib.databinding.FragmentWebviewBinding;

public class WebViewFragment extends Fragment {
    private FragmentWebviewBinding binding;

    private void showLoader() {
        binding.progressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoader() {
        binding.progressBar.setVisibility(View.GONE);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentWebviewBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.backRL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(WebViewFragment.this).popBackStack();
            }
        });
        showLoader();
       binding.dataWV.getSettings().setLoadsImagesAutomatically(true);

        binding.dataWV.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        binding.dataWV.getSettings().setJavaScriptEnabled(true);
        binding.dataWV.setWebViewClient(new WebViewClient() {
            @Override
            @SuppressWarnings("deprecation")
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.i(TAG, "Processing webview url click...");
                view.loadUrl(url);
                return true;
            }

            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return true;
            }


            public void onPageFinished(WebView view, String url) {
                Log.i(TAG, "Finished loading URL: " + url);
               hideLoader();

            }
            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Log.e(TAG, "Error: " + description);
                Toast.makeText(requireActivity(), "Oh no! " + description, Toast.LENGTH_SHORT).show();

            }

            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
                // Redirect to deprecated method, so you can use it in all SDK versions
                onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
            }

        });

        binding.dataWV.loadUrl(getString(R.string.privacy_policy_url));
    }
}
