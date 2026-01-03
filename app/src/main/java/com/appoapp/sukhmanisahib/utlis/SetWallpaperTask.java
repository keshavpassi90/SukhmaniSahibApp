package com.appoapp.sukhmanisahib.utlis;


import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SetWallpaperTask extends AsyncTask<String, Void, Boolean> {

    private Context context;

    public SetWallpaperTask(Context context) {
        this.context = context.getApplicationContext();  // use application context to avoid leaks
    }

    @Override
    protected Boolean doInBackground(String... urls) {
        String imageUrl = urls[0];
        Bitmap bitmap = null;

        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap originalBitmap = BitmapFactory.decodeStream(input);

            if (originalBitmap == null) return false;

            // Get screen size
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics metrics = new DisplayMetrics();
            if (wm != null) {
                wm.getDefaultDisplay().getMetrics(metrics);
            } else {
                return false; // can't get screen size
            }
            int screenWidth = metrics.widthPixels;
            int screenHeight = metrics.heightPixels;

            // Scale bitmap to fit screen exactly (fitXY style)
            bitmap = Bitmap.createScaledBitmap(originalBitmap, screenWidth, screenHeight, true);

            originalBitmap.recycle();

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (bitmap == null) return false;

        try {
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
            wallpaperManager.setBitmap(bitmap);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            Toast.makeText(context, "Wallpaper set successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Failed to set wallpaper.", Toast.LENGTH_SHORT).show();
        }
    }
}
