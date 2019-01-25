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
import com.ajna.tasklist.database.TasksContract;
import com.ajna.tasklist.model.Task;

public class TasksRVAdapter extends RecyclerView.Adapter<TasksRVAdapter.ViewHolder>{
    private static final String TAG = "TasksRVAdapter";

    private Cursor cursor;
    private OnTaskClickListener listener;

    public interface OnTaskClickListener{
        void onDeleteClick(Task task);
    }

    public TasksRVAdapter(Cursor cursor, OnTaskClickListener listener) {
        Log.d(TAG, "TasksRVAdapter: starts");
        this.cursor = cursor;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        if(cursor == null || (cursor.getCount() == 0)){
            holder.tvTitle.setText(R.string.no_task);
            holder.btnPopupMenu.setVisibility(View.GONE);
            holder.tvDetails.setVisibility(View.GONE);
        } else {
            if(!cursor.moveToPosition(position)){
                throw new IllegalStateException("Couldn't move cursor to position " + position);
            }

            final Task task = new Task(cursor.getInt(cursor.getColumnIndex(TasksContract.Columns._ID)),
                    cursor.getString(cursor.getColumnIndex(TasksContract.Columns.TITLE)),
                    cursor.getString(cursor.getColumnIndex(TasksContract.Columns.DETAILS)),
                    cursor.getInt(cursor.getColumnIndex(CategoriesContract.Columns.NAME)));

            holder.btnPopupMenu.setVisibility(View.VISIBLE);
            holder.tvDetails.setVisibility(View.VISIBLE);
            holder.tvTitle.setText(task.getTitle());
            holder.tvDetails.setText(task.getDetails());

            holder.btnPopupMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    PopupMenu popup = new PopupMenu(view.getContext(), holder.btnPopupMenu);

                    popup.getMenuInflater()
                            .inflate(R.menu.menu_context_task_item, popup.getMenu());

                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()){
                                case R.id.menu_context_edit:
                                    break;
                                case R.id.menu_context_delete:
                                    listener.onDeleteClick(task);
                                    break;
                            }
                            return true;
                        }
                    });

                    popup.show(); //showing popup menu
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if((cursor==null) || (cursor.getCount() ==0)){
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

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvTitle;
        TextView tvDetails;

        ImageView btnPopupMenu;

        public ViewHolder(View itemView) {
            super(itemView);
            this.tvTitle = itemView.findViewById(R.id.tv_title);
            this.tvDetails = itemView.findViewById(R.id.tv_details);
            this.btnPopupMenu = itemView.findViewById(R.id.btn_popup_menu);
        }
    }
}