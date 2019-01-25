package com.ajna.tasklist.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.ajna.tasklist.R;
import com.ajna.tasklist.database.CategoriesContract;
import com.ajna.tasklist.fragments.CategoryListFragment;
import com.ajna.tasklist.fragments.TaskListFragment;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        TaskListFragment.OnFragmentInteractionListener,
        CategoryListFragment.OnFragmentInteractionListener {

    private static final String TAG = "MainActivity";
    public static final int LOADER_ID = 2;

    private ActionBar actionbar;
    private DrawerLayout drawerLayout;
    private MenuItem previousItem;
    private NavigationView navView;
    private boolean isFirstCatLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);

        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);

        previousItem = navView.getMenu().getItem(0);
        previousItem.setChecked(true);

        actionbar.setTitle(previousItem.getTitle().toString());


        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                drawerLayout.closeDrawers();

                if ((previousItem == item)) {
                    return true;
                }

                item.setChecked(true);
                previousItem.setChecked(false);

                if (item.getGroupId() == R.id.submenu_top) {
                    String listName = item.getTitle().toString();
                    actionbar.setTitle(listName);

                    if (previousItem.getGroupId() == R.id.submenu_top) {
                        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_main);

                        // when both previous and picked item are displaying list of tasks
                        // then we don't create new fragment, just filter list again
                        if (fragment instanceof TaskListFragment) {
                            ((TaskListFragment) fragment).filterTasks(listName);
                        }
                    } else {
                        TaskListFragment taskListFragment = new TaskListFragment();
                        FragmentManager fm = getSupportFragmentManager();
                        fm.beginTransaction().replace(R.id.fragment_main, taskListFragment).commit();
                        fm.executePendingTransactions();
                        taskListFragment.filterTasks(listName);
                    }
                } else {
                    switch (item.getItemId()) {
                        case R.id.drawer_edit_categories:
                            CategoryListFragment categoriesFragment = CategoryListFragment.newInstance();

                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_main, categoriesFragment).commit();
                            break;
                        default:
                            break;
                    }
                }

                previousItem = item;
                return true;
            }
        });

        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        TaskListFragment taskListFragment = new TaskListFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_main, taskListFragment)
                .commit();

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentViewCreated(int title) {
        getSupportActionBar().setTitle(title);
    }


    @Override
    public void onNewTaskClicked(String defaultCategory) {
        isFirstCatLoad = true;
        Intent intent = new Intent(this, NewTaskActivity.class);
        intent.putExtra(NewTaskActivity.EXTRA_DEFAULT_CATEGORY, defaultCategory);
        startActivity(intent);
    }


    private void updateDrawerMenu(Cursor cursor) {
        Menu menu = navView.getMenu();
        menu.removeGroup(R.id.submenu_top);

        MenuItem menuItem = menu.add(R.id.submenu_top, Menu.NONE, Menu.NONE,"All notes");
        menuItem.setIcon(R.drawable.ic_list_black_24dp);
        if(isFirstCatLoad){
            previousItem.setChecked(false);
            menuItem.setChecked(true);
            previousItem = menuItem;
        }

        addCategoriesToDrawer(cursor);
    }
    private void addCategoriesToDrawer(Cursor cursor){
        Menu menu = navView.getMenu();
        MenuItem menuItem;
        cursor.moveToFirst();
        do {
            String item = cursor.getString(cursor.getColumnIndex(CategoriesContract.Columns.NAME));
            menuItem = menu.add(R.id.submenu_top, Menu.NONE, Menu.NONE, item);
            menuItem.setIcon(R.drawable.ic_label_outline_white_24dp);
            Log.d(TAG, "updateDrawerMenu: " + item);
        } while (cursor.moveToNext());
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
        Log.d(TAG, "onLoadFinished: starts");
        updateDrawerMenu(cursor);
        isFirstCatLoad = false;
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset: starts");
        updateDrawerMenu(null);

    }
}