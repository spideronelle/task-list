package com.ajna.tasklist.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public class MyContentProvider extends ContentProvider {
    public static final String TAG = "MyContentProvider";

    private MyDBHelper dbHelper;

    public static final UriMatcher uriMatcher = buildUriMatcher();

    public static final String CONTENT_AUTHORITY = "com.ajna.tasklist.provider";
    public static final Uri CONTENT_AUTHORITY_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final int TASKS = 100;
    public static final int TASKS_ID = 101;
    
    public static final int LISTS = 200;
    public static final int LISTS_ID = 201;

    public static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        matcher.addURI(CONTENT_AUTHORITY, TasksContract.TABLE_NAME, TASKS);
        matcher.addURI(CONTENT_AUTHORITY, TasksContract.TABLE_NAME + "/#", TASKS_ID); 
        
        matcher.addURI(CONTENT_AUTHORITY, CategoriesContract.TABLE_NAME, LISTS);
        matcher.addURI(CONTENT_AUTHORITY, CategoriesContract.TABLE_NAME + "/#", LISTS_ID);

        return matcher;
    }


    @Override
    public boolean onCreate() {
        dbHelper = MyDBHelper.getInstance(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final int match = uriMatcher.match(uri);
        Log.d(TAG, "query: match = " + match);

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        switch (match) {
            case TASKS:
                // Tasks is always queried with list that it is in
                queryBuilder.setTables(TasksContract.TABLE_NAME + " INNER JOIN " + CategoriesContract.TABLE_NAME +
                        " ON " + TasksContract.TABLE_NAME + "." + TasksContract.Columns.CATEGORY_ID +
                        " = " + CategoriesContract.TABLE_NAME + "." + CategoriesContract.Columns._ID);

                break;
            case TASKS_ID:
                queryBuilder.setTables(TasksContract.TABLE_NAME + " INNER JOIN " + CategoriesContract.TABLE_NAME +
                        " ON " + TasksContract.TABLE_NAME + "." + TasksContract.Columns.CATEGORY_ID +
                        " = " + CategoriesContract.TABLE_NAME + "." + CategoriesContract.Columns._ID);
                long taskId = TasksContract.getTaskId(uri);
                queryBuilder.appendWhere(TasksContract.Columns._ID + " = " + taskId);
                break;
            case LISTS:
                queryBuilder.setTables(CategoriesContract.TABLE_NAME);
                break;
            case LISTS_ID:
                queryBuilder.setTables(CategoriesContract.TABLE_NAME);
                long listId = CategoriesContract.getId(uri);
                queryBuilder.appendWhere(CategoriesContract.Columns._ID + " = " + listId);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = uriMatcher.match(uri);

        switch (match) {
            case TASKS:
                return TasksContract.CONTENT_TYPE;
            case TASKS_ID:
                return TasksContract.CONTENT_TYPE_ITEM;
            case LISTS:
                return CategoriesContract.CONTENT_TYPE;
            case LISTS_ID:
                return CategoriesContract.CONTENT_TYPE_ITEM;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = uriMatcher.match(uri);

        final SQLiteDatabase db;

        Uri resultUri;
        long recordId;

        switch (match){
            case TASKS:
                db = dbHelper.getWritableDatabase();
                recordId = db.insert(TasksContract.TABLE_NAME, null, contentValues);
                if(recordId >= 0){
                    resultUri = TasksContract.buildTaskUri(recordId);
                } else {
                    throw new SQLException("Failed to insert into " + uri.toString());
                }
                break;
            case LISTS:
                db = dbHelper.getWritableDatabase();
                recordId = db.insert(CategoriesContract.TABLE_NAME, null, contentValues);
                if(recordId >= 0){
                    resultUri = CategoriesContract.buildUri(recordId);
                } else {
                    throw new SQLException("Failed to insert into " + uri.toString());
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return resultUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = uriMatcher.match(uri);

        final SQLiteDatabase db;
        int count;
        String selectionCriteria;
        switch (match){
            case TASKS:
                db = dbHelper.getWritableDatabase();
                count = db.delete(TasksContract.TABLE_NAME, selection, selectionArgs);
                break;
            case TASKS_ID:
                db = dbHelper.getWritableDatabase();
                long taskId = TasksContract.getTaskId(uri);
                selectionCriteria = TasksContract.Columns._ID + " = " + taskId;

                if(selection != null && selection.length() > 0){
                    selectionCriteria += " AND (" + selection + ")";
                }
                count = db.delete(TasksContract.TABLE_NAME, selectionCriteria, selectionArgs);
                break;
            case LISTS:
                db = dbHelper.getWritableDatabase();
                count = db.delete(TasksContract.TABLE_NAME, selection, selectionArgs);
                break;
            case LISTS_ID:
                db = dbHelper.getWritableDatabase();
                long listId = CategoriesContract.getId(uri);
                selectionCriteria = CategoriesContract.Columns._ID + " = " + listId;

                if(selection != null && selection.length() > 0){
                    selectionCriteria += " AND (" + selection + ")";
                }
                count = db.delete(CategoriesContract.TABLE_NAME, selectionCriteria, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }
        if(count > 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = uriMatcher.match(uri);

        final SQLiteDatabase db;
        int count;
        String selectionCriteria;
        switch (match){
            case TASKS:
                db = dbHelper.getWritableDatabase();
                count = db.update(TasksContract.TABLE_NAME, contentValues, selection, selectionArgs);
                break;

            case TASKS_ID:
                db = dbHelper.getWritableDatabase();
                long taskId = TasksContract.getTaskId(uri);
                selectionCriteria = TasksContract.Columns._ID + " = " + taskId;

                if(selection != null && selection.length() > 0){
                    selectionCriteria += " AND (" + selection + ")";
                }
                count = db.update(TasksContract.TABLE_NAME, contentValues, selectionCriteria, selectionArgs);
                break;

            case LISTS:
                db = dbHelper.getWritableDatabase();
                count = db.update(CategoriesContract.TABLE_NAME, contentValues, selection, selectionArgs);
                break;

            case LISTS_ID:
                db = dbHelper.getWritableDatabase();
                long listId = CategoriesContract.getId(uri);
                selectionCriteria = CategoriesContract.Columns._ID + " = " + listId;

                if(selection != null && selection.length() > 0){
                    selectionCriteria += " AND (" + selection + ")";
                }
                count = db.update(CategoriesContract.TABLE_NAME, contentValues, selectionCriteria, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }
        if(count > 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }
}
