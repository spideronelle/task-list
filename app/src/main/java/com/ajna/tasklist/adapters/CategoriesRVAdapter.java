package com.ajna.tasklist.adapters;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ajna.tasklist.R;
import com.ajna.tasklist.database.CategoriesContract;
import com.ajna.tasklist.model.Category;

public class CategoriesRVAdapter extends RecyclerView.Adapter<CategoriesRVAdapter.ViewHolder> {
    private static final String TAG = "CategoriesRVAdapter";

    private Cursor cursor;
    private OnListClickListener listener;


    public CategoriesRVAdapter(Cursor cursor, OnListClickListener listener) {
        this.cursor = cursor;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: starts");
        if (cursor == null || (cursor.getCount() == 0)) {
            // there should be always at least 1 list in the db, this should not be executed
            holder.tvName.setText(R.string.no_category);
            return;
        }

        if (!cursor.moveToPosition(position)) {
            throw new IllegalStateException("Couldn't move cursor to position " + position);
        }


        final Category category = new Category(cursor.getInt(cursor.getColumnIndex(CategoriesContract.Columns._ID)),
                                            cursor.getString(cursor.getColumnIndex(CategoriesContract.Columns.NAME)));
        holder.tvName.setText(category.getName());
        holder.btnOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(view.getContext(), holder.btnOptions);

                popup.getMenuInflater()
                        .inflate(R.menu.menu_context_task_item, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_context_edit:
                                listener.onCatEditClick();
                                break;
                            case R.id.menu_context_delete:
                                listener.onCatDeleteClick(category);
                                break;
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        if ((cursor == null) || (cursor.getCount() == 0)) {
            return 1;
        } else {
            return cursor.getCount();
        }
    }

    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == cursor) {
            return null;
        }

        final Cursor oldCursor = cursor;
        cursor = newCursor;
        if (newCursor != null) {
            notifyDataSetChanged();
        } else {
            notifyItemRangeRemoved(0, getItemCount());
        }
        return oldCursor;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView btnOptions;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_list_name);
            btnOptions = itemView.findViewById(R.id.btn_list_options);
        }
    }

    public interface OnListClickListener {
        void onCatDeleteClick(Category category);
        void onCatEditClick();
    }
}
