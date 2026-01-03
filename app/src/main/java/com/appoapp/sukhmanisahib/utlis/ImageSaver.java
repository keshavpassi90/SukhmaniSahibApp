package com.appoapp.sukhmanisahib.utlis;

import android.content.ContentValues;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageSaver {

    public static boolean saveImageToGallery(Context context, String imageUrl, String displayName) {
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            inputStream = connection.getInputStream();

            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 1);
            }

            // Insert the content values to get the Uri
            android.content.ContentResolver resolver = context.getContentResolver();
            android.net.Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            if (uri == null) {
                return false;
            }

            outputStream = resolver.openOutputStream(uri);
            if (outputStream == null) {
                return false;
            }

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.flush();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear();
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0);
                resolver.update(uri, contentValues, null, null);
            }

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;

        } finally {
            try {
                if (inputStream != null) inputStream.close();
            } catch (IOException ignored) {}
            try {
                if (outputStream != null) outputStream.close();
            } catch (IOException ignored) {}
        }
    }
}
