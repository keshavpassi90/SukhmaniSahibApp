package com.appoapp.sukhmanisahib;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.navigation.NavigationView;
import com.appoapp.sukhmanisahib.helpers.CustomTypefaceSpan;
import com.appoapp.sukhmanisahib.helpers.Meanings;
import com.appoapp.sukhmanisahib.helpers.Path;
import com.appoapp.sukhmanisahib.helpers.SqliteHelper;
import com.appoapp.sukhmanisahib.utils.Constants;
import com.appoapp.sukhmanisahib.utils.Preferences;
import com.appoapp.sukhmanisahib.utils.TimeUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, Runnable{
    // PERMISSION request constant, assign any value
    private static final String TAG = "PERMISSION_TAG";    private AdView mAdView;
    ConnectivityManager cm;
    DrawerLayout drawer;
    NavigationView navigationView;
    Toolbar toolbar;
    public static int currentpage = 1;
    SqliteHelper dbHelper;
    ArrayList<Path> pathList;
    ArrayList<Meanings> meaningList;
    public ScrollView scrollView;
    TextView readPath;
    Typeface tf;
    String pagetype="standard";
    Integer maxPages=0;
    Integer minPage=0;
    ArrayList<Integer> ids;
    ArrayList<String> titles;
    Float fontsize;
    String font;
    ArrayList<View> views = new ArrayList<View>();
    ArrayList<Long> list = new ArrayList<>();
    Long downloadID;
    File foundFile;
    static Boolean isPlaying=false;
    String filepath;
    static ImageView play;
    TextView prev,next;
    private DownloadManager downloadManager;
    Intent playbackServiceIntent;
    MusicService service;
    SeekBar seekBar;
    TextView seekBarHint;
    LinearLayout pagenavi;
    private static final int PERMISSION_REQUEST_CODE = 100;

    private static final String[] storage_permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final String[] storage_permissions_33 = { Manifest.permission.READ_MEDIA_VIDEO};

    private boolean isDialogOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        if(Preferences.getBool("darkmode", getApplicationContext()).equals(true)){
//            setTheme(R.style.AppThemeDark);
//        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        MobileAds.initialize(this, initializationStatus -> {});

        drawer = findViewById(R.id.drawer_layout);
        play = findViewById(R.id.playnow);
        downloadManager= (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        navigationView = findViewById(R.id.nav_view);
        scrollView = findViewById(R.id.scrollView);
        pagenavi = findViewById(R.id.pagenavi);
        prev = findViewById(R.id.prev);
        next = findViewById(R.id.next);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);
        font = Constants.fonts[Preferences.getInteger("font",1,getApplicationContext())];
        fontsize = Constants.fontsizeValues[Preferences.getInteger("fontsize",1,getApplicationContext())];
        pagetype = Constants.layout[Preferences.getInteger("homestyle",0,getApplicationContext())];
        tf = Typeface.createFromAsset(getAssets(),font);
        pathList = new ArrayList<>();
        meaningList = new ArrayList<>();
        dbHelper = new SqliteHelper(this);
        dbHelper.openDataBase();

        if(Preferences.getBool("darkmode", getApplicationContext()).equals(true)){
            scrollView.setBackgroundColor(getResources().getColor(R.color.darkthemewhite));
            setTextViewDrawableColor(prev, R.color.colorPrevDark);
            setTextViewDrawableColor(next, R.color.colorPrevDark);
            prev.setTextColor(getResources().getColor(R.color.colorPrevDark));
            next.setTextColor(getResources().getColor(R.color.colorPrevDark));
        }else{
            scrollView.setBackgroundColor(getResources().getColor(R.color.white));
            setTextViewDrawableColor(prev, R.color.colorPrev);
            setTextViewDrawableColor(next, R.color.colorPrev);
            prev.setTextColor(getResources().getColor(R.color.colorPrev));
            next.setTextColor(getResources().getColor(R.color.colorPrev));
        }

        if(pagetype.equals("standard")){
            pagenavi.setVisibility(View.GONE);
            this.getSimplePath();
        }else{
            pagenavi.setVisibility(View.VISIBLE);
            minPage=dbHelper.getMinPages();
            maxPages=dbHelper.getMaxPages();
            this.getMeaningPath(minPage);
        }

        setUpNavigationView();
        this.getMenuItems();

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(PlayerConstants.SONG_PAUSED){
                    PlayerConstants.SONG_STOPPED = false;
                    PlayerConstants.SONG_PAUSED=false;
                    Controls.playControl(getApplicationContext());
                    changeButton(PlayerConstants.SONG_NAME);
                    startMusicService();
                    return;
                }
                if (!PlayerConstants.SONG_STOPPED) {
                    //stopMusicService(playbackServiceIntent);
                    PlayerConstants.SONG_STOPPED = false;
                    PlayerConstants.SONG_PAUSED=true;
                    pauseMusicService();
                    Controls.stopControl(getApplicationContext());
                    changeButton(PlayerConstants.SONG_NAME);
                    return;
                }
                filepath=getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)+"/"+getString(R.string.songfile) + ".mp3";
                foundFile = new File (filepath);
                if(!checkNetwork() && !foundFile.exists()){
                    networkAlert();
                    return;
                }
                PlayerConstants.SONG_NAME = getString(R.string.songfile);
                PlayerConstants.SONG_TITLE = getString(R.string.songtitle);
                PlayerConstants.SONG_URL = getString(R.string.mp3url);
                startIntent();
                Timer timer = new Timer();
                TimerTask seekthread = new TimerTask() {
                    @Override
                    public void run() {
                        new Thread(MainActivity.this).start();
                    }
                };

                timer.schedule(seekthread, 3000);
                changeButton(PlayerConstants.SONG_NAME);
//                        if (checkPermission()) {
//                            beginDownload();
//                        } else {
//                            requestPermission();
//                        }
                if (!checkPermissions()) {
                    requestPermissions();
                }else{
                    beginDownload();
                }
            }
        });
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentpage>minPage) {
                    currentpage--;
                    getMeaningPath(currentpage);
                }
                if(currentpage==1){
                    prev.setVisibility(View.GONE);
                }else{
                    prev.setVisibility(View.VISIBLE);
                }
                if(currentpage<maxPages){
                    next.setVisibility(View.VISIBLE);
                }else {
                    next.setVisibility(View.GONE);
                }
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentpage<maxPages) {
                    currentpage++;
                    getMeaningPath(currentpage);
                }
                showToast(currentpage+" "+maxPages);
                if(currentpage>1){
                    prev.setVisibility(View.VISIBLE);
                }else{
                    prev.setVisibility(View.GONE);
                }

                if(currentpage==maxPages){
                    next.setVisibility(View.GONE);
                }else {
                    next.setVisibility(View.VISIBLE);
                }
            }
        });
        if(currentpage==1){
            prev.setVisibility(View.GONE);
        }else{
            prev.setVisibility(View.VISIBLE);
        }
        if(currentpage<maxPages){
            next.setVisibility(View.VISIBLE);
        }else{
            next.setVisibility(View.GONE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), RECEIVER_EXPORTED);
        }else {
            registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        }
//        registerReceiver(onComplete,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        playbackServiceIntent = new Intent(this, MusicService.class);

        seekBar = findViewById(R.id.seekbar);
        seekBarHint = findViewById(R.id.seekhint);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

                seekBarHint.setVisibility(View.VISIBLE);
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                seekBarHint.setVisibility(View.VISIBLE);
                // time util class
                String formattedTime = TimeUtils.formatTime(progress);

                seekBarHint.setText("" + formattedTime);

                if (progress > 0 && (service == null || PlayerConstants.SONG_STOPPED)) {
                    seekBar.setProgress(0);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (service != null && !PlayerConstants.SONG_STOPPED) {
                    service.seekTo(seekBar.getProgress());
                }
            }
        });

        cm = (ConnectivityManager)this.getSystemService(CONNECTIVITY_SERVICE);
        if (getString(R.string.showAds).equalsIgnoreCase("true") && getString(R.string.showBannerAd).equalsIgnoreCase("true") && checkNetwork()) {
            mAdView = findViewById(R.id.adView);
            mAdView.setVisibility(View.VISIBLE);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }

    }
    private void setTextViewDrawableColor(TextView textView, int color) {
        for (Drawable drawable : textView.getCompoundDrawables()) {
            if (drawable != null) {
                drawable.setColorFilter(new PorterDuffColorFilter(getResources().getColor(color), PorterDuff.Mode.SRC_IN));
            }
        }
    }

    public static int[] splitToComponentTimes(BigDecimal biggy)
    {
        long longVal = biggy.longValue();
        int hours = (int) longVal / 3600;
        int remainder = (int) longVal - hours * 3600;
        int mins = remainder / 60;
        remainder = remainder - mins * 60;
        int secs = remainder;

        int[] ints = {hours , mins , secs};
        return ints;
    }

    @Override
    public void run() {
        if(service != null) {
            int currentPosition = service.getCurrentPosition();
            int total = service.getDuration();

            while (!PlayerConstants.SONG_STOPPED && currentPosition < total) {
                try {
                    Thread.sleep(1000);
                    if (!PlayerConstants.SONG_STOPPED) {
                        currentPosition = service.getCurrentPosition();
                    }
                } catch (InterruptedException e) {
                    return;
                } catch (Exception e) {
                    return;
                }

                seekBar.setProgress(currentPosition);
            }

            if(currentPosition == total){
                currentPosition=0;
                seekBar.setProgress(currentPosition);
            }
        }
    }

    private static ArrayList<View> getViewsByTag(ViewGroup root, String tag){
        final int childCount = root.getChildCount();
        ArrayList<View> views = new ArrayList<View>();
        for (int i = 0; i < childCount; i++) {
            final View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                views.addAll(getViewsByTag((ViewGroup) child, tag));
            }

            final Object tagObj = child.getTag();
            if (tagObj != null && tagObj.equals(tag)) {
                views.add(child);
            }

        }
        return views;
    }

    public void getMeaningPath(Integer page) {
        currentpage=page;
        meaningList=dbHelper.getMeaningsOfPage(page);

        LinearLayout ll = findViewById(R.id.layoutinside);
        ll.removeAllViews();
        for(int i=0;i<meaningList.size();i++){
            Meanings cont = meaningList.get(i);
            TextView tv1 = new TextView(this);
            TextView tv2 = new TextView(this);
            TextView tv3 = new TextView(this);
            TextView tv4 = new TextView(this);
            TextView tv5 = new TextView(this);
            tv1.setTag("line1");
            tv2.setTag("line2");
            tv3.setTag("line3");
            tv4.setTag("line4");
            tv5.setTag("line5");
            tv1.setGravity(Gravity.CENTER);
            tv2.setGravity(Gravity.CENTER);
            tv3.setGravity(Gravity.CENTER);
            tv4.setGravity(Gravity.CENTER);
            tv5.setGravity(Gravity.CENTER);
            tv1.setText(cont.getline1().trim());
            tv2.setText(cont.getline2().trim());
            tv3.setText(cont.getline3().trim());
            tv4.setText(cont.getline4().trim());
            tv5.setText(cont.getline5().trim());
            tv1.setTypeface(tf,Typeface.NORMAL);
            tv2.setTypeface(tf,Typeface.NORMAL);
            tv3.setTypeface(tf,Typeface.NORMAL);
            tv4.setTypeface(tf,Typeface.NORMAL);
            tv1.setTextSize(fontsize);

            tv2.setTextSize(fontsize);
            tv3.setTextSize(fontsize);
            tv4.setTextSize(fontsize);
            tv5.setTextSize(fontsize);
            if(Preferences.getBool("darkmode", getApplicationContext()).equals(true)){
                tv1.setTextColor(getResources().getColor(R.color.white));
            }else{
                tv1.setTextColor(getResources().getColor(R.color.colorA));
            }
            tv2.setTextColor(getResources().getColor(R.color.colorB));
            tv3.setTextColor(getResources().getColor(R.color.colorC));
            tv4.setTextColor(getResources().getColor(R.color.colorD));
            tv5.setTextColor(getResources().getColor(R.color.colorE));
            ll.addView(tv1);
            ll.addView(tv2);
            ll.addView(tv3);
            ll.addView(tv4);
            ll.addView(tv5);
        }
        if(pagetype.equals("linebyline")) {
            views = getViewsByTag(scrollView, "line1");
            for (int i = 0; i < views.size(); i++) {
                if(i==0) continue;
                TextView t = (TextView) views.get(i);
                String ed_text = t.getText().toString().trim();
                if(ed_text.contains("slok") || ed_text.contains("AstpdI")){
                    ed_text=System.getProperty("line.separator")+""+ed_text;
                    t.setText(ed_text);
                }
            }
            views = getViewsByTag(scrollView, "line2");
            for (int i = 0; i < views.size(); i++) {
                views.get(i).setVisibility(View.GONE);
            }
            views = getViewsByTag(scrollView, "line3");
            for (int i = 0; i < views.size(); i++) {
                views.get(i).setVisibility(View.GONE);
            }
            views = getViewsByTag(scrollView, "line4");
            for (int i = 0; i < views.size(); i++) {
                views.get(i).setVisibility(View.GONE);
            }
            views = getViewsByTag(scrollView, "line5");
            for (int i = 0; i < views.size(); i++) {
                views.get(i).setVisibility(View.GONE);
            }
        }else{
            if(Preferences.getBool("tran_english", getApplicationContext()).equals(true)
                    || Preferences.getBool("tran_teeka", getApplicationContext()).equals(true)
                    || Preferences.getBool("tran_teekapad", getApplicationContext()).equals(true)
                    || Preferences.getBool("tran_punjabi", getApplicationContext()).equals(true)){
                views = getViewsByTag(scrollView, "line1");
                for (int i = 0; i < views.size(); i++) {
                    if(i==0) continue;
                    TextView t = (TextView) views.get(i);
                    String ed_text = t.getText().toString().trim();
                    ed_text=System.getProperty("line.separator")+""+ed_text;
                    t.setText(ed_text);
                }
            }
            if (!Preferences.getBool("tran_english", getApplicationContext()).equals(true)) {
                views = getViewsByTag(scrollView, "line5");
                for (int i = 0; i < views.size(); i++) {
                    views.get(i).setVisibility(View.GONE);
                }
            }else{
                views = getViewsByTag(scrollView, "line5");
                for (int i = 0; i < views.size(); i++) {
                    TextView t = (TextView) views.get(i);
                    String ed_text = t.getText().toString().trim();
                    if(ed_text.length() > 0)
                    {
                        views.get(i).setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        views.get(i).setVisibility(View.GONE);
                    }
                }
            }
            if (!Preferences.getBool("tran_teeka", getApplicationContext()).equals(true)) {
                views = getViewsByTag(scrollView, "line4");
                for (int i = 0; i < views.size(); i++) {
                    views.get(i).setVisibility(View.GONE);
                }
            }else{
                views = getViewsByTag(scrollView, "line4");
                for (int i = 0; i < views.size(); i++) {
                    //views.get(i).setVisibility(View.VISIBLE);
                    TextView t = (TextView) views.get(i);
                    String ed_text = t.getText().toString().trim();
                    if(ed_text.length() > 0)
                    {
                        views.get(i).setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        views.get(i).setVisibility(View.GONE);
                    }
                }
            }
            if (!Preferences.getBool("tran_teekapad", getApplicationContext()).equals(true)) {
                views = getViewsByTag(scrollView, "line3");
                for (int i = 0; i < views.size(); i++) {
                    views.get(i).setVisibility(View.GONE);
                }
            }else{
                views = getViewsByTag(scrollView, "line3");
                for (int i = 0; i < views.size(); i++) {
                    TextView t = (TextView) views.get(i);
                    String ed_text = t.getText().toString().trim();
                    if(ed_text.length() > 0)
                    {
                        views.get(i).setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        views.get(i).setVisibility(View.GONE);
                    }
                }
            }
            if (!Preferences.getBool("tran_punjabi", getApplicationContext()).equals(true)) {
                views = getViewsByTag(scrollView, "line2");
                for (int i = 0; i < views.size(); i++) {
                    views.get(i).setVisibility(View.GONE);
                }
            }else{
                views = getViewsByTag(scrollView, "line2");
                for (int i = 0; i < views.size(); i++) {
                    TextView t = (TextView) views.get(i);
                    String ed_text = t.getText().toString().trim();
                    if(ed_text.length() > 0)
                    {
                        views.get(i).setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        views.get(i).setVisibility(View.GONE);
                    }
                }
            }
        }

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                scrollView.smoothScrollTo(0,0);
            }
        }, 500);
    }

    public void getSimplePath(){
        pathList = dbHelper.getDetails();
        ids = new ArrayList<Integer>();
        titles = new ArrayList<String>();
        String msg ="";

        LinearLayout ll = findViewById(R.id.layoutinside);
        ll.removeAllViews();

        for(int i=0;i<pathList.size();i++){
            ids.add(i);
            TextView tv1 = new TextView(this);
            tv1.setTag("readtext-" + i);
            tv1.setGravity(Gravity.CENTER);
            Path cont = pathList.get(i);
            //msg = cont.gettitle()+" \n "+cont.getdata()+"\n\
            msg = cont.getdata()+"\n\n";
            titles.add(cont.gettitle());
            tv1.setText(msg);
            tv1.setLineSpacing(tv1.getLineHeight(), 0.6f);
            tv1.setTypeface(tf,Typeface.NORMAL);
            tv1.setTextSize(fontsize);
            if(Preferences.getBool("darkmode", getApplicationContext()).equals(true)){
                tv1.setTextColor(getResources().getColor(R.color.white));
            }else{
                tv1.setTextColor(getResources().getColor(R.color.colorA));
            }
            ll.addView(tv1);
        }
        scrollView.smoothScrollTo(0,0);

    }

    public void getMenuItems(){
        if(pagetype.equals("standard")) {
            Menu menu = navigationView.getMenu();
            menu.clear();
            int listSize = ids.size();
            for (int i = 0; i < listSize; i++) {
//                SpannableStringBuilder title = new SpannableStringBuilder(titles.get(i));
//                title.setSpan(new StyleSpan(tf.getStyle()),0,title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                SpannableString title = new SpannableString(titles.get(i));
                title.setSpan(new RelativeSizeSpan(1.5f), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                title.setSpan(new CustomTypefaceSpan("" , tf), 0 , title.length(),  Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                menu.add(R.id.main, ids.get(i), 2, title).setIcon(R.drawable.ic_file);
            }
        }else{
            Menu menu = navigationView.getMenu();
            menu.clear();
            for (int i = minPage; i<=maxPages; i++){
                SpannableString title = new SpannableString("AstpdI "+i);
                title.setSpan(new RelativeSizeSpan(1.5f), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                title.setSpan(new CustomTypefaceSpan("" , tf), 0 , title.length(),  Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                menu.add(R.id.main, i , 2, title).setIcon(R.drawable.ic_file);
            }
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivityForResult(intent,0);
            return true;
        }

        //this.getMenuItems();

        return super.onOptionsItemSelected(item);
    }

    private void setUpNavigationView() {

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawer.addDrawerListener(actionBarDrawerToggle);
        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int itemID = item.getItemId();

        //Log.e("page",itemID+"");
        // if user select the current navigation menu again, don't do anything
        // just close the navigation drawer
        switch (itemID) {
            default:
                if(pagetype.equals("standard")){
                    readPath = scrollView.findViewWithTag("readtext-" + itemID );
                    this.scrollToView(scrollView,readPath);
                }else{
                    readPath = scrollView.findViewWithTag("readtext-" + itemID );
                    if(itemID>currentpage && itemID<=maxPages){
                        this.getMeaningPath(itemID);
                    }else if(itemID<currentpage && itemID>=minPage){
                        this.getMeaningPath(itemID);
                    }

                    if(currentpage>1){
                        prev.setVisibility(View.VISIBLE);
                    }else{
                        prev.setVisibility(View.GONE);
                    }

                    if(currentpage==maxPages){
                        next.setVisibility(View.GONE);
                    }else {
                        next.setVisibility(View.VISIBLE);
                    }
                }
        }

        drawer.closeDrawer(GravityCompat.START);

        return true;
    }
   @Override
    public void onBackPressed() {

        super.onBackPressed();
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawers();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Exit Application")
                    .setMessage("Are you sure you want to exit?")
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface arg0, int arg1) {
                            MainActivity.super.onBackPressed();
                        }
                    }).create().show();
        }

    }

    public void seekBarsetMax(int position){
//        try{
//            seekBar.setMax(position);
//        }catch (Exception e){
//            e.printStackTrace();
//        }
        seekBar.setMax(position);
    }


    /**
     * Used to scroll to the given view.
     *
     * @param scrollViewParent Parent ScrollView
     * @param view View to which we need to scroll.
     */
    public void scrollToView(final ScrollView scrollViewParent, final View view) {
        // Get deepChild Offset
        Point childOffset = new Point();
        getDeepChildOffset(scrollViewParent, view.getParent(), view, childOffset);
        // Scroll to child.
        scrollViewParent.smoothScrollTo(0, childOffset.y);
    }

    /**
     * Used to get deep child offset.
     * <p/>
     * 1. We need to scroll to child in scrollview, but the child may not the direct child to scrollview.
     * 2. So to get correct child position to scroll, we need to iterate through all of its parent views till the main parent.
     *
     * @param mainParent        Main Top parent.
     * @param parent            Parent.
     * @param child             Child.
     * @param accumulatedOffset Accumulated Offset.
     */

    private void getDeepChildOffset(final ViewGroup mainParent, final ViewParent parent, final View child, final Point accumulatedOffset) {
        ViewGroup parentGroup = (ViewGroup) parent;
        accumulatedOffset.x += child.getLeft();
        accumulatedOffset.y += child.getTop();
        if (parentGroup.equals(mainParent)) {
            return;
        }
        getDeepChildOffset(mainParent, parentGroup.getParent(), parentGroup, accumulatedOffset);
    }
    public void startMusicService() {
        Intent intent = new Intent(this, MusicService.class);
        intent.setAction("ACTION_PLAY");
        startService(intent);
    }

    public void pauseMusicService() {
        Intent intent = new Intent(this, MusicService.class);
        intent.setAction("ACTION_PAUSE");
        startService(intent);
    }

    public void stopMusicService() {
        Intent intent = new Intent(this, MusicService.class);
        intent.setAction("ACTION_STOP");
        startService(intent);
    }


    BroadcastReceiver onComplete = new BroadcastReceiver() {

        public void onReceive(Context ctxt, Intent intent) {

            long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            //Log.e("IN", "" + referenceId);
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(referenceId);
            Cursor cursor = downloadManager.query(query);
            if( cursor != null && cursor.moveToFirst() ) {
                int fileNameIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
                String savedFilePath = cursor.getString(fileNameIndex);
                String currentFileName = savedFilePath.substring(savedFilePath.lastIndexOf('/')+1, savedFilePath.length());

                /*String currentFileName = savedFilePath.substring(savedFilePath.lastIndexOf("/"), savedFilePath.length());
                currentFileName = currentFileName.substring(1);*/
                //Log.e("Current file name", currentFileName);
                File oldFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),currentFileName);
                File newFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),currentFileName.replace(".tmp",""));
                oldFile.renameTo(newFile);
                cursor.close();
            }
            list.remove(referenceId);
            if (list.isEmpty())
            {
                //Log.e("INSIDE", "" + referenceId);

            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onComplete);
        stopMusicService(playbackServiceIntent);
    }


    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void beginDownload(){
        filepath=getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)+"/"+PlayerConstants.SONG_NAME + ".mp3";
        foundFile = new File(filepath);
        String tmpFilePath = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)+"/"+PlayerConstants.SONG_NAME + ".mp3.tmp";
        File foundFile2 = new File(tmpFilePath);
        if (!foundFile.exists() && !foundFile2.exists() ) {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(PlayerConstants.SONG_URL))
                    .setTitle(PlayerConstants.SONG_TITLE)// Title of the Download Notification
                    .setDescription("Saving offline")// Description of the Download Notification
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)// Visibility of the download Notification
                    //.setDestinationUri(Uri.fromFile(file))// Uri of the destination file
                    .setAllowedOverMetered(true)// Set if download is allowed on Mobile network
                    .setAllowedOverRoaming(true) // Set if download is allowed on roaming network
                    .setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, PlayerConstants.SONG_NAME + ".mp3.tmp");
            downloadID = downloadManager.enqueue(request);// enqueue puts the download request in the queue.
            list.add(downloadID);
        }
    }



    public void startIntent(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(playbackServiceIntent);
        }else{
            startService(playbackServiceIntent);
        }
        bindService(playbackServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
        PlayerConstants.SONG_STOPPED=false;
    }


    private String[] getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return storage_permissions_33;
        } else {
            return storage_permissions;
        }
    }

    private boolean checkPermissions() {
        String[] permissions = getPermissions();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestPermissions() {
        String[] permissions = getPermissions();
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                Log.e("Data", "permission ok");
                // All permissions are granted
                beginDownload();
            } else {
                // Some permissions are denied
                if (!isDialogOpen) {
                    // Optionally, show a dialog to inform the user about the necessity of the permissions
                    showErrorDialog(this, "Go to settings. Please enable the required permissions in Settings.", getString(R.string.app_name));
                }
            }
        }
    }

    public  void showErrorDialog(final Context context, String message, String title) {
        try {
            if (!isDialogOpen) {
                isDialogOpen = true;
                AlertDialog.Builder builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert)
                        .setTitle(title)
                        .setMessage(message)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                                isDialogOpen = false;
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                                intent.setData(uri);
                                context.startActivity(intent);
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert);
                builder.show();
            }
        } catch (WindowManager.BadTokenException e) {
            Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static void changeButton(String filename){

        if(PlayerConstants.SONG_STOPPED || PlayerConstants.SONG_PAUSED){
            play.setImageResource(R.drawable.ic_play);
            isPlaying=false;
        }else{
            play.setImageResource(R.drawable.ic_pause);
            isPlaying=true;
        }
    }

    protected void onResume() {
        if(currentpage==1){
            prev.setVisibility(View.GONE);
        }else{
            prev.setVisibility(View.VISIBLE);
        }
        if(currentpage<maxPages){
            next.setVisibility(View.VISIBLE);
        }else{
            next.setVisibility(View.GONE);
        }
        super.onResume();
        if(Constants.change){
            font = Constants.fonts[Preferences.getInteger("font",1,getApplicationContext())];
            fontsize = Constants.fontsizeValues[Preferences.getInteger("fontsize",1,getApplicationContext())];
            pagetype = Constants.layout[Preferences.getInteger("homestyle",1,getApplicationContext())];
            tf = Typeface.createFromAsset(getAssets(),font);
            if(pagetype.equals("standard")){
                pagenavi.setVisibility(View.GONE);
                this.getSimplePath();
            }else{
                pagenavi.setVisibility(View.VISIBLE);
                minPage=dbHelper.getMinPages();
                maxPages=dbHelper.getMaxPages();
                this.getMeaningPath(currentpage);
            }

            this.getMenuItems();
            Constants.change=false;
        }
        try{
            boolean isServiceRunning = MusicService.serviceRunning;
            if (isServiceRunning) {
                changeButton(PlayerConstants.SONG_NAME);
            }
        }catch(Exception e){}

        if (Constants.themechange) {
            Constants.themechange = false;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 2 || resultCode == 1 ) {
            if(resultCode == 1){
                scrollView.setBackgroundColor(getResources().getColor(R.color.darkthemewhite));
                setTextViewDrawableColor(prev, R.color.colorPrevDark);
                setTextViewDrawableColor(next, R.color.colorPrevDark);
                prev.setTextColor(getResources().getColor(R.color.colorPrevDark));
                next.setTextColor(getResources().getColor(R.color.colorPrevDark));
            }else{
                scrollView.setBackgroundColor(getResources().getColor(R.color.white));
                setTextViewDrawableColor(prev, R.color.colorPrev);
                setTextViewDrawableColor(next, R.color.colorPrev);
                prev.setTextColor(getResources().getColor(R.color.colorPrev));
                next.setTextColor(getResources().getColor(R.color.colorPrev));
            }
            font = Constants.fonts[Preferences.getInteger("font",1,getApplicationContext())];
            fontsize = Constants.fontsizeValues[Preferences.getInteger("fontsize",1,getApplicationContext())];
            pagetype = Constants.layout[Preferences.getInteger("homestyle",1,getApplicationContext())];
            tf = Typeface.createFromAsset(getAssets(),font);
            if(pagetype.equals("standard")){
                pagenavi.setVisibility(View.GONE);
                this.getSimplePath();
            }else{
                pagenavi.setVisibility(View.VISIBLE);
                minPage=dbHelper.getMinPages();
                maxPages=dbHelper.getMaxPages();
                this.getMeaningPath(currentpage);
            }
            this.getMenuItems();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder _service) {
            MusicService.LocalBinder binder = (MusicService.LocalBinder) _service;
            service = binder.getServiceInstance(); //Get instance of your service!

            service.registerClient(MainActivity.this); //Activity register in the service as client for callabcks!

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            service = null;

        }
    };

    public void stopMusicService(Intent intent) {
        if (service != null) {
            try {
                service.stop();
                stopService(intent);
                unbindService(mConnection);
                service = null;
            } catch (IllegalArgumentException ex) {
                stopService(intent);
                service = null;
                ex.printStackTrace();
            }

            changeButton(PlayerConstants.SONG_NAME);
            seekBar.setProgress(0);
        }else{
            Log.d("stop", "stopMusicService: ");
        }
        PlayerConstants.SONG_STOPPED=true;
    }

    public void cancelNotify(String filename) {
        if (service != null && playbackServiceIntent!=null) {
            try {
                service.stop();
                stopService(playbackServiceIntent);
                unbindService(mConnection);
                service = null;
//                Intent intent = new Intent(MainActivity.this, MainActivity.class);
//                startActivity(intent);
            } catch (IllegalArgumentException ex) {
                stopService(playbackServiceIntent);
                service = null;
                ex.printStackTrace();
            }
            seekBar.setProgress(0);
            changeButton(filename);
        }else{
            Log.d("stop", "stopMusicService: ");
        }
    }

    @SuppressWarnings("deprecation")
    private boolean checkNetwork(){
        boolean result = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (cm != null) {
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        result = true;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        result = true;
                    }
                }
            }
        } else {
            if (cm != null) {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork != null) {
                    // connected to the internet
                    result = activeNetwork.isConnectedOrConnecting();
                }
            }
        }
        return result;

    }

    public void networkAlert(){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
        mBuilder.setTitle("Network Error");
        mBuilder.setMessage("Please check your internet connection");
        mBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }

}
