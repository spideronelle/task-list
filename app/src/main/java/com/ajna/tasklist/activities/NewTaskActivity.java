package com.ajna.tasklist.activities;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ajna.tasklist.R;
import com.ajna.tasklist.database.CategoriesContract;
import com.ajna.tasklist.database.TasksContract;

import java.util.ArrayList;

public class NewTaskActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "NewTaskActivity";
    public static final String EXTRA_DEFAULT_CATEGORY = "ExtraDefaultCategory";

    public static final int LOADER_ID = 2;
    Cursor cursor = null;

    private EditText etTitle;
    private EditText etDescription;
    private TextView tvCategory;
    private String defaultCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);

        Intent intent = getIntent();
        defaultCategory = intent.getStringExtra(EXTRA_DEFAULT_CATEGORY);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
        }

        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        tvCategory = findViewById(R.id.tv_category);

        tvCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCategorySelectionDialog();
            }
        });

        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_new_task, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_save:
                saveNote();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveNote() {
        String title = etTitle.getText().toString();
        String details = etDescription.getText().toString();
        if ((title.trim().length() > 0) || (details.trim().length() > 0)) {
            ContentResolver contentResolver = getContentResolver();

            ContentValues values = new ContentValues();
            values.put(TasksContract.Columns.TITLE, title);
            values.put(TasksContract.Columns.DETAILS, details);
            int listId = 0;
            if((cursor != null) && cursor.getCount() != 0){
                cursor.moveToFirst();
                do{
                    if(tvCategory.getText().toString().equals(cursor.getString(cursor.getColumnIndex(CategoriesContract.Columns.NAME)))){
                        listId = cursor.getInt(cursor.getColumnIndex(CategoriesContract.Columns._ID));
                        break;
                    }
                } while (cursor.moveToNext());
            }
            values.put(TasksContract.Columns.CATEGORY_ID, listId);

            contentResolver.insert(TasksContract.CONTENT_URI, values);
        } else {
            Toast.makeText(this, "Can't save task without title and details.", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void showCategorySelectionDialog(){
        ArrayList<String> categories = new ArrayList<>();
        if((cursor != null) && cursor.getCount() != 0){
            cursor.moveToFirst();
            do{
                categories.add(cursor.getString(cursor.getColumnIndex(CategoriesContract.Columns.NAME)));
            } while (cursor.moveToNext());
        }
        final String[] listsArray = categories.toArray(new String[0]);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(listsArray, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int position) {
                        tvCategory.setText(listsArray[position]);
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Log.d(TAG, "onCreateLoader: starts");
        String[] projection = {CategoriesContract.Columns._ID, CategoriesContract.Columns.NAME};
        return new CursorLoader(this, CategoriesContract.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        Log.d(TAG, "onLoadFinished: ");
        this.cursor = cursor;
        if(defaultCategory == null) {
            cursor.moveToLast();
            tvCategory.setText(cursor.getString(cursor.getColumnIndex(CategoriesContract.Columns.NAME)));
        } else {
            tvCategory.setText(defaultCategory);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
}
