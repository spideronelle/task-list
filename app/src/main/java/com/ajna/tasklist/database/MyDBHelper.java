package com.ajna.tasklist.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.ajna.tasklist.R;

public class MyDBHelper extends SQLiteOpenHelper {
    private static final String TAG = "AppDatabase";
    public static String DATABASE_NAME = "Category.db";
    public static final int DATABASE_VERSION = 2;

    private static MyDBHelper instance = null;

    private MyDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    static MyDBHelper getInstance(Context context) {
        if (instance == null) {
            instance = new MyDBHelper(context);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql;

        sql = "CREATE TABLE " + TasksContract.TABLE_NAME + " (" +
                TasksContract.Columns._ID + " INTEGER PRIMARY KEY NOT NULL, " +
                TasksContract.Columns.TITLE + " TEXT NOT NULL, " +
                TasksContract.Columns.DETAILS + " TEXT, " +
                TasksContract.Columns.CATEGORY_ID + " INTEGER NOT NULL);";
        Log.d(TAG, "onCreate: " + sql);
        db.execSQL(sql);

        sql = "CREATE TABLE " + CategoriesContract.TABLE_NAME + " (" +
                CategoriesContract.Columns._ID + " INTEGER PRIMARY KEY NOT NULL, " +
                CategoriesContract.Columns.NAME + " TEXT NOT NULL);";
        Log.d(TAG, "onCreate: " + sql);
        db.execSQL(sql);

        sql = "INSERT INTO " + CategoriesContract.TABLE_NAME + " VALUES (" + "0, 'General'" + ");";
        Log.d(TAG, "onCreate: " + sql);
        db.execSQL(sql);
        sql = "INSERT INTO " + CategoriesContract.TABLE_NAME + " VALUES (" + "1, 'Work'" + ");";
        Log.d(TAG, "onCreate: " + sql);
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1:
                addTrigger(sqLiteDatabase);
                break;
            default:
                throw new IllegalStateException("onUpgrade() with unknown newVersion " + newVersion);
        }
    }

    private void addTrigger(SQLiteDatabase db){
        String sql;
        sql = "CREATE TRIGGER Remove_Task"
                + " AFTER DELETE ON " + CategoriesContract.TABLE_NAME
                + " FOR EACH ROW"
                + " BEGIN"
                + " DELETE FROM " + TasksContract.TABLE_NAME
                + " WHERE " + TasksContract.Columns.CATEGORY_ID + " = OLD." + CategoriesContract.Columns._ID + ";"
                + " END;";
        db.execSQL(sql);
    }
}
