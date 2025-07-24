package com.appoapp.sukhmanisahib;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.appoapp.sukhmanisahib.utils.Preferences;

import java.util.Objects;

public class WebviewActivity extends AppCompatActivity {
    public static final String PRIVACY_URL = "privacy_url";
    private WebView wv1;
    Toolbar toolbar;
    Dialog dialog;
    private static final String TAG = "Main";
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(Preferences.getBool("darkmode", getApplicationContext()).equals(true)){
            setTheme(R.style.AppThemeDark);
        }
        super.onCreate(savedInstanceState);
        String url  = getIntent().getStringExtra(PRIVACY_URL);
        if (url == null || url.isEmpty()) finish();
        setContentView(R.layout.activity_webview);

        toolbar = (Toolbar) findViewById(R.id.toolbar_id);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Privacy");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();

        wv1 = findViewById(R.id.webView);
        setDialog(true);
        wv1.getSettings().setLoadsImagesAutomatically(true);
        wv1.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        wv1.getSettings().setJavaScriptEnabled(true);
        wv1.setWebViewClient(new WebViewClient() {
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
                if(dialog.isShowing()){
                    setDialog(false);
                }

            }
            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Log.e(TAG, "Error: " + description);
                Toast.makeText(WebviewActivity.this, "Oh no! " + description, Toast.LENGTH_SHORT).show();
                alertDialog.setTitle("Error");
                alertDialog.setMessage(description);
                alertDialog.setButton(Dialog.BUTTON_POSITIVE,"OK",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                alertDialog.show();
            }

            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
                // Redirect to deprecated method, so you can use it in all SDK versions
                onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
            }

        });

        wv1.loadUrl(url);
    }

    private void setDialog(boolean show){
        if (show){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder.setView(R.layout.loader);
            }else{
                @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.loader,null);
                builder.setView(view);
            }
            builder.setCancelable(false);
            dialog = builder.create();
            Window window = dialog.getWindow();
            if (window != null) {
                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                layoutParams.copyFrom(window.getAttributes());
                layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
                layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                window.setAttributes(layoutParams);
                window.setGravity(Gravity.CENTER);
                window.setBackgroundDrawableResource(android.R.color.transparent);
            }
            dialog.show();
        }
        else dialog.dismiss();
    }
}
