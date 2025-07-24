package com.appoapp.sukhmanisahib;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.appoapp.sukhmanisahib.utils.Constants;
import com.appoapp.sukhmanisahib.utils.Preferences;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity  {

    Toolbar toolbar;
    TextView versionName, fontsize, homeStyle, font;
    LinearLayout Item2, Item3, Item5, Item6, Item7, translationPanel;
    ImageView enabletranslations;
    Switch englishTranlationShowHide, punjabiTranlationShowHide, teekaTranlationShowHide, teekapadTranlationShowHide, darkModeSwitch;

    String[] listitems, homestyles, fonts;
    AlertDialog.Builder mBuilder;
    AlertDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        toolbar = findViewById(R.id.toolbar_id);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(v -> finish());

        Item2 = findViewById(R.id.list_item2);
        Item3 = findViewById(R.id.list_item3);
        Item5 = findViewById(R.id.list_item5);
        Item6 = findViewById(R.id.list_item6);
        Item7 = findViewById(R.id.list_item7);
        fontsize = findViewById(R.id.changeFont);
        font = findViewById(R.id.font);
        homeStyle = findViewById(R.id.homeStyle);

        listitems = getResources().getStringArray(R.array.fontsizes);
        homestyles = getResources().getStringArray(R.array.homestyles);
        fonts = getResources().getStringArray(R.array.font);

        fontsize.setText(listitems[Preferences.getInteger("fontsize", 1, getApplicationContext())]);
        homeStyle.setText(homestyles[Preferences.getInteger("homestyle", 0, getApplicationContext())]);
        font.setText(fonts[Preferences.getInteger("font", 1, getApplicationContext())]);

        translationPanel = findViewById(R.id.translationPanel);
        enabletranslations = findViewById(R.id.enabletranslations);

        englishTranlationShowHide = findViewById(R.id.englishEnableCheck);
        punjabiTranlationShowHide = findViewById(R.id.punjabiEnableCheck);
        teekaTranlationShowHide = findViewById(R.id.teekaEnableCheck);
        teekapadTranlationShowHide = findViewById(R.id.teekapadEnableCheck);
        darkModeSwitch = findViewById(R.id.darkmodeon);

        mBuilder = new AlertDialog.Builder(SettingsActivity.this);

        darkModeSwitch.setChecked(Preferences.getBool("darkmode", getApplicationContext()));
        englishTranlationShowHide.setChecked(Preferences.getBool("tran_english", getApplicationContext()));
        punjabiTranlationShowHide.setChecked(Preferences.getBool("tran_punjabi", getApplicationContext()));
        teekaTranlationShowHide.setChecked(Preferences.getBool("tran_teeka", getApplicationContext()));
        teekapadTranlationShowHide.setChecked(Preferences.getBool("tran_teekapad", getApplicationContext()));

        Item2.setOnClickListener(v -> {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
            sharingIntent.putExtra(Intent.EXTRA_TEXT,
                    "Hey check out my app at: " + Uri.parse("http://play.google.com/store/apps/details?id=com.appoapp.sukhmanisahib" ));
            startActivity(Intent.createChooser(sharingIntent, "Share app via"));
        });

        Item3.setOnClickListener(v -> launchMarket());

        Item5.setOnClickListener(v -> launchMarket());

        Item6.setOnClickListener(v -> {
            Intent i = new Intent(SettingsActivity.this, WebviewActivity.class);
            i.putExtra(WebviewActivity.PRIVACY_URL, getString(R.string.privacy_url));
            startActivity(i);
        });

        Item7.setOnClickListener(v -> feedback());

        fontsize.setOnClickListener(v -> {
            mBuilder.setTitle("Font Size");
            Integer fontkey = Preferences.getInteger("fontsize", 1, getApplicationContext());
            mBuilder.setSingleChoiceItems(listitems, fontkey, (dialog, i) -> {
                fontsize.setText(listitems[i]);
                dialog.dismiss();
                Preferences.putInteger("fontsize", i, getApplicationContext());
                Constants.change = true;
            });
            mBuilder.setPositiveButton("Cancel", (dialog, which) -> dialog.dismiss());
            mDialog = mBuilder.create();
            mDialog.show();
        });

        homeStyle.setOnClickListener(v -> {
            mBuilder.setTitle("Home Style");
            Integer homestylekey = Preferences.getInteger("homestyle", 0, getApplicationContext());
            mBuilder.setSingleChoiceItems(homestyles, homestylekey, (dialog, i) -> {
                homeStyle.setText(homestyles[i]);
                dialog.dismiss();
                Preferences.putInteger("homestyle", i, getApplicationContext());
                Constants.change = true;
            });
            mBuilder.setPositiveButton("Cancel", (dialog, which) -> dialog.dismiss());
            mDialog = mBuilder.create();
            mDialog.show();
        });

        font.setOnClickListener(v -> {
            mBuilder.setTitle("Font");
            Integer fontstyle = Preferences.getInteger("font", 1, getApplicationContext());
            mBuilder.setSingleChoiceItems(fonts, fontstyle, (dialog, i) -> {
                font.setText(fonts[i]);
                dialog.dismiss();
                Preferences.putInteger("font", i, getApplicationContext());
                Constants.change = true;
            });
            mBuilder.setPositiveButton("Cancel", (dialog, which) -> dialog.dismiss());
            mDialog = mBuilder.create();
            mDialog.show();
        });

        enabletranslations.setOnClickListener(v -> {
            if (translationPanel.getVisibility() == View.GONE) {
                Preferences.putBool("transpanelopen", true, getApplicationContext());
                translationPanel.setVisibility(View.VISIBLE);
                expand(translationPanel);
                enabletranslations.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_up));
            } else {
                Preferences.putBool("transpanelopen", false, getApplicationContext());
                translationPanel.setVisibility(View.GONE);
                enabletranslations.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_down));
            }
        });
        englishTranlationShowHide.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Preferences.putBool("tran_english", isChecked, getApplicationContext());
            Constants.change = true;
        });

        punjabiTranlationShowHide.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Preferences.putBool("tran_punjabi", isChecked, getApplicationContext());
            Constants.change = true;
        });

        teekaTranlationShowHide.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Preferences.putBool("tran_teeka", isChecked, getApplicationContext());
            Constants.change = true;
        });

        teekapadTranlationShowHide.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Preferences.putBool("tran_teekapad", isChecked, getApplicationContext());
            Constants.change = true;
        });

        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Preferences.putBool("darkmode", isChecked, getApplicationContext());
            Constants.themechange = true;
            setResult(isChecked ? 1 : 2);
        });


        versionName = findViewById(R.id.textView9);
        versionName.setText("100");
        try {
            String versionNames = getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName;

            versionName.setText("" + versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (Preferences.getBool("transpanelopen", getApplicationContext())) {
            translationPanel.setVisibility(View.VISIBLE);
            enabletranslations.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_up));
        } else {
            translationPanel.setVisibility(View.GONE);
            enabletranslations.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_down));
        }
    }



    private void showSingleChoiceDialog(String title, String[] items, String key, TextView targetView) {
        mBuilder.setTitle(title);
        int selected = Preferences.getInteger(key, 0, getApplicationContext());
        mBuilder.setSingleChoiceItems(items, selected, (dialog, which) -> {
            targetView.setText(items[which]);
            Preferences.putInteger(key, which, getApplicationContext());
            Constants.change = true;
            dialog.dismiss();
        });
        mBuilder.setPositiveButton("Cancel", (dialog, which) -> dialog.dismiss());
        mDialog = mBuilder.create();
        mDialog.show();
    }

    private void toggleTranslationPanel() {
        if (translationPanel.getVisibility() == View.GONE) {
            Preferences.putBool("transpanelopen", true, getApplicationContext());
            translationPanel.setVisibility(View.VISIBLE);
            expand(translationPanel);
            enabletranslations.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_up));
        } else {
            Preferences.putBool("transpanelopen", false, getApplicationContext());
            translationPanel.setVisibility(View.GONE);
            enabletranslations.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_down));
        }
    }

    private void feedback() {
        Intent Email = new Intent(Intent.ACTION_SENDTO);
        Email.setData(Uri.parse("mailto:"));
        Email.putExtra(Intent.EXTRA_EMAIL, new String[]{getResources().getString(R.string.supportEmail)});
        Email.putExtra(Intent.EXTRA_SUBJECT, "Feedback about Sukhmani Sahib App!");
        Email.putExtra(Intent.EXTRA_TEXT, "Dear App Team,");
        startActivity(Intent.createChooser(Email, "Send Feedback:"));
    }

    private void launchMarket() {
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(myAppLinkToMarket);
        } catch (ActivityNotFoundException e) {
            showToast("Unable to find market app");
        }
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public static void expand(final View v) {
        v.measure(View.MeasureSpec.makeMeasureSpec(((View) v.getParent()).getWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        final int targetHeight = v.getMeasuredHeight();

        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? LinearLayout.LayoutParams.WRAP_CONTENT
                        : (int) (targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setDuration((int) (targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }
}
