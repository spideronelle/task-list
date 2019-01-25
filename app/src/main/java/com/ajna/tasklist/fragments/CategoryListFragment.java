package com.ajna.tasklist.fragments;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.ajna.tasklist.R;
import com.ajna.tasklist.adapters.CategoriesRVAdapter;
import com.ajna.tasklist.database.CategoriesContract;
import com.ajna.tasklist.model.Category;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CategoryListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CategoryListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CategoryListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        CategoriesRVAdapter.OnListClickListener {

    private OnFragmentInteractionListener mListener;
    public static final int LOADER_ID = 3;

    RecyclerView rvCategories;
    FloatingActionButton fab;
    CategoriesRVAdapter rvAdapter;
    Cursor mCursor;

    public CategoryListFragment() {
        // Required empty public constructor
    }

    public static CategoryListFragment newInstance() {
        return new CategoryListFragment();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_categories_list, container, false);

        rvCategories = view.findViewById(R.id.rv_lists_list);
        rvAdapter = new CategoriesRVAdapter(null, this);
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCategories.setAdapter(rvAdapter);

        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNewListDialog();
            }
        });

        mListener.onFragmentViewCreated(R.string.categories);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_categories, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add:
                if (mListener != null) {
                    showNewListDialog();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
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

    private void showNewListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View view = inflater.inflate(R.layout.dialog_new_list, null);
        builder.setView(view)
                .setTitle(R.string.new_category)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        EditText etName = view.findViewById(R.id.et_name);
                        saveList(etName.getText().toString());
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void saveList(String name){
        ContentResolver contentResolver = getActivity().getContentResolver();

        ContentValues values = new ContentValues();
        values.put(CategoriesContract.Columns.NAME, name);

        contentResolver.insert(CategoriesContract.CONTENT_URI, values);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = {CategoriesContract.Columns._ID, CategoriesContract.Columns.NAME};
        return new CursorLoader(getActivity(), CategoriesContract.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        rvAdapter.swapCursor(cursor);
        mCursor = cursor;
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        rvAdapter.swapCursor(null);
    }


    @Override
    public void onCatDeleteClick(Category category) {
        if(mCursor.getCount() > 1){
            ContentResolver contentResolver = getActivity().getContentResolver();
            contentResolver.delete(CategoriesContract.buildUri(category.getId()), null, null);
        } else {
            Toast.makeText(getContext(), "There must be at least 1 category.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCatEditClick() {
        // TODO
    }

    public interface OnFragmentInteractionListener {
        void onFragmentViewCreated(int title);
    }
}
