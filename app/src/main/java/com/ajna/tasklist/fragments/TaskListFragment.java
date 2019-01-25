package com.ajna.tasklist.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ajna.tasklist.R;
import com.ajna.tasklist.adapters.TasksRVAdapter;
import com.ajna.tasklist.database.CategoriesContract;
import com.ajna.tasklist.database.TasksContract;
import com.ajna.tasklist.model.Task;

public class TaskListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        TasksRVAdapter.OnTaskClickListener {
    private static final String TAG = "TaskListFragment";
    public static final String LIST_FILTER_ARG = "ListFilterArg";
    public static final int LOADER_ID = 1;

    TasksRVAdapter rvAdapter;
    String listFilter = null;

    private OnFragmentInteractionListener mListener;


    public TaskListFragment() {
        // Required empty public constructor
    }


    public static TaskListFragment newInstance() {
        return new TaskListFragment();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated: ");
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add:
                if (mListener != null) {
                    mListener.onNewTaskClicked(listFilter);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        View view = inflater.inflate(R.layout.fragment_task_list, container, false);

        RecyclerView rvTasks = view.findViewById(R.id.rv_tasks);
        rvTasks.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvAdapter = new TasksRVAdapter(null, this);
        rvTasks.setAdapter(rvAdapter);

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onNewTaskClicked(listFilter);
            }
        });

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.d(TAG, "onCreateLoader: starts");


        if(bundle != null && bundle.size() != 0  && !("All notes".equals(bundle.getString(LIST_FILTER_ARG)))) {
            listFilter = bundle.getString(LIST_FILTER_ARG);
        }

        String[] projection = {TasksContract.TABLE_NAME + "." + TasksContract.Columns._ID,
                TasksContract.TABLE_NAME + "." + TasksContract.Columns.TITLE,
                TasksContract.TABLE_NAME + "." + TasksContract.Columns.DETAILS,
                CategoriesContract.TABLE_NAME + "." + CategoriesContract.Columns.NAME};

        String selection = null;
        String[] selectionArgs = {""};
        if (listFilter != null) {
            Log.d(TAG, "onCreateLoader: listFilter = " + listFilter);
            selection = CategoriesContract.Columns.NAME + " = ? ";
            selectionArgs[0] = listFilter;
        } else {
            selectionArgs = null;
        }
        return new CursorLoader(getActivity(), TasksContract.CONTENT_URI, projection, selection, selectionArgs, null);

    }

    public void filterTasks(String listName) {
        Bundle bundle = new Bundle();
        if (listName.equals("All notes")) {
            bundle.putString(LIST_FILTER_ARG, null);
        } else {
            bundle.putString(LIST_FILTER_ARG, listName);
        }
        getLoaderManager().restartLoader(LOADER_ID, bundle, this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        rvAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        rvAdapter.swapCursor(null);
    }

    @Override
    public void onDeleteClick(Task task) {
        ContentResolver contentResolver = getActivity().getContentResolver();
        contentResolver.delete(TasksContract.buildTaskUri(task.getId()), null, null);
    }

    public interface OnFragmentInteractionListener {
        void onNewTaskClicked(String defaultCategory);
    }
}
