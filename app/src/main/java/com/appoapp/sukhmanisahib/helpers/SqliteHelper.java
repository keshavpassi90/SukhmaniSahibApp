package com.appoapp.sukhmanisahib.helpers;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SqliteHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "db_path.sqlite";
    private static final String DB_PATH_SUFFIX = "/databases/";
    private static Context ctx;
    public SqliteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        ctx = context;
    }
    public ArrayList<Path> getDetails() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Path> contList = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM path_lyrics", null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Path cont = new Path(cursor.getInt(0), cursor.getString(2), cursor.getString(3));
                contList.add(cont);
            }
            cursor.close();
            db.close();
        }
        return contList;
    }
    public ArrayList<Meanings> getMeanings() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Meanings> contList = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM tableb", null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Meanings cont = new Meanings(cursor.getInt(0),cursor.getInt(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6));
                contList.add(cont);
            }
            cursor.close();
            db.close();
        }
        return contList;
    }
    public ArrayList<Meanings> getMeaningsOfPage(Integer page) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Meanings> contList = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM tableb where page="+page, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Meanings cont = new Meanings(cursor.getInt(0),cursor.getInt(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6));
                contList.add(cont);
            }
            cursor.close();
            db.close();
        }
        return contList;
    }

    public Integer getMaxPages() {
        SQLiteDatabase db = this.getReadableDatabase();
        Integer max =0;
        Cursor cursor = db.rawQuery("SELECT * FROM tableb WHERE (page) IN ( SELECT MAX(page) FROM tableb )  limit 1", null);
        if (cursor != null) {
            cursor.moveToFirst();
            max = cursor.getInt(1);
            cursor.close();
            db.close();
        }
        return max;
    }
    public Integer getMinPages() {
        SQLiteDatabase db = this.getReadableDatabase();
        Integer min =0;
        Cursor cursor = db.rawQuery("SELECT * FROM tableb WHERE (page) IN ( SELECT MIN(page) FROM tableb )  limit 1", null);
        if (cursor != null) {
            cursor.moveToFirst();
            min = cursor.getInt(1);
            cursor.close();
            db.close();
        }
        return min;
    }

    private void CopyDataBaseFromAsset() throws IOException {
        InputStream myInput = ctx.getAssets().open(DATABASE_NAME);
// Path to the just created empty db
        String outFileName = getDatabasePath();

// if the path doesn't exist first, create it
        File f = new File(ctx.getApplicationInfo().dataDir + DB_PATH_SUFFIX);
        if (!f.exists())
            f.mkdir();

// Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

// transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }
// Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();

    }
    private static String getDatabasePath() {
        return ctx.getApplicationInfo().dataDir + DB_PATH_SUFFIX
                + DATABASE_NAME;
    }
    public SQLiteDatabase openDataBase() throws SQLException {
        File dbFile = ctx.getDatabasePath(DATABASE_NAME);
        if (!dbFile.exists()) {
            try {
                CopyDataBaseFromAsset();
                System.out.println("Copying sucess from Assets folder");
            } catch (IOException e) {
                throw new RuntimeException("Error creating source database", e);
            }
        }
        return SQLiteDatabase.openDatabase(dbFile.getPath(), null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.CREATE_IF_NECESSARY);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}