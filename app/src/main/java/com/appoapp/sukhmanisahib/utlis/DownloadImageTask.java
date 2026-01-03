package com.appoapp.sukhmanisahib.utlis;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public class DownloadImageTask extends AsyncTask<String, Void, Boolean> {

    private Context context;
    private String fileName;

    public DownloadImageTask(Context context, String fileName) {
        this.context = context;
        this.fileName = fileName;
    }

    @Override
    protected Boolean doInBackground(String... urls) {
        String imageUrl = urls[0];
        return ImageSaver.saveImageToGallery(context, imageUrl, fileName);
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            Toast.makeText(context, "Image saved to gallery", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }
}

