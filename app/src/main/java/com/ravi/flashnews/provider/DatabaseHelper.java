package com.ravi.flashnews.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // The name of the database
    private static final String DATABASE_NAME = "favorites.db";

    // If you change the database schema, you must increment the database version
    private static final int VERSION = 1;


    // Constructor
    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_TABLE = "CREATE TABLE " + FavoritesContract.FavoritesEntry.TABLE_NAME + " (" +
                FavoritesContract.FavoritesEntry._ID + " INTEGER PRIMARY KEY, " +
                FavoritesContract.FavoritesEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                FavoritesContract.FavoritesEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                FavoritesContract.FavoritesEntry.COLUMN_AUTHOR + " TEXT NOT NULL, " +
                FavoritesContract.FavoritesEntry.COLUMN_FULL_URL + " TEXT NOT NULL, " +
                FavoritesContract.FavoritesEntry.COLUMN_IMAGE_PATH + " TEXT NOT NULL, " +
                FavoritesContract.FavoritesEntry.COLUMN_SOURCE + " TEXT NOT NULL, " +
                FavoritesContract.FavoritesEntry.COLUMN_PUBLISHED_DATE + " TEXT NOT NULL);";

        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FavoritesContract.FavoritesEntry.TABLE_NAME);
        onCreate(db);
    }
}
