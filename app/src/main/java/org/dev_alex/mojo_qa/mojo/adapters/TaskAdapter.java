package org.dev_alex.mojo_qa.mojo.adapters;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.dev_alex.mojo_qa.mojo.App;
import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.activities.MainActivity;
import org.dev_alex.mojo_qa.mojo.fragments.TasksFragment;
import org.dev_alex.mojo_qa.mojo.models.Task;
import org.dev_alex.mojo_qa.mojo.services.LoginHistoryService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;


public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy | HH:mm", Locale.getDefault());
    private ArrayList<Task> tasks;
    private TasksFragment parentFragment;

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitle;
        TextView taskDate;
        TextView delayed;
        View taskActiveCircle;
        ImageView taskIcon;
        ImageView moreBtn;

        TaskViewHolder(View itemView) {
            super(itemView);
            taskDate = itemView.findViewById(R.id.task_date);
            taskTitle = itemView.findViewById(R.id.task_title);
            delayed = itemView.findViewById(R.id.delayed);
            taskActiveCircle = itemView.findViewById(R.id.task_active);
            taskIcon = itemView.findViewById(R.id.task_icon);
            moreBtn = itemView.findViewById(R.id.more_btn);
        }
    }


    public TaskAdapter(TasksFragment parentFragment, ArrayList<Task> tasks) {
        this.parentFragment = parentFragment;
        this.tasks = tasks;
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_task, viewGroup, false);
        return new TaskViewHolder(v);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder viewHolder, int i) {
        final Task task = tasks.get(i);

        viewHolder.delayed.setVisibility(View.GONE);
        viewHolder.taskActiveCircle.setVisibility((task.suspended) ? View.INVISIBLE : View.VISIBLE);

        viewHolder.moreBtn.setVisibility(View.GONE);
        if (task.suspended) {
            viewHolder.taskActiveCircle.setVisibility(View.INVISIBLE);

            if (task.complete_time != null) {
                viewHolder.taskDate.setVisibility(View.VISIBLE);
                viewHolder.taskDate.setText(sdf.format(new Date(task.complete_time)));
                viewHolder.taskDate.setTextColor(Color.parseColor("#ffcc0000"));
            } else
                viewHolder.taskDate.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.taskActiveCircle.setVisibility(View.VISIBLE);

            if (task.expire_time != null) {
                viewHolder.taskDate.setVisibility(View.VISIBLE);

                ((GradientDrawable) viewHolder.taskActiveCircle.getBackground()).setColor(new Date(task.expire_time).after(new Date()) ? Color.GREEN : Color.RED);

                viewHolder.taskDate.setText(sdf.format(task.expire_time));
            } else {
                viewHolder.taskDate.setVisibility(View.INVISIBLE);
                viewHolder.taskActiveCircle.setVisibility(View.INVISIBLE);
            }
        }
        if (Objects.equals(task.ref.type, "openlinks") || Objects.equals(task.ref.type, "openlinks_group")) {
            viewHolder.taskIcon.setImageResource(R.drawable.ic_open_link);
        } else if (Objects.equals(task.ref.type, "closedlinks") || Objects.equals(task.ref.type, "closedlinks_group")) {
            viewHolder.taskIcon.setImageResource(R.drawable.ic_close_link);
        } else if (Objects.equals(task.ref.type, "constantly") || Objects.equals(task.ref.type, "constantly_group")) {
            viewHolder.taskIcon.setImageResource(R.drawable.file_icon);
        } else if (Objects.equals(task.ref.type, "periodic") || Objects.equals(task.ref.type, "periodic_group")){
            viewHolder.taskIcon.setImageResource(R.drawable.ic_periodical);
        } else if (Objects.equals(task.ref.type, "oneshot") || Objects.equals(task.ref.type, "oneshot_group")) {
            viewHolder.taskIcon.setImageResource(R.drawable.ic_oneshot);
        }
        viewHolder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parentFragment.showPopUpWindow(task);
            }
        });

        viewHolder.taskTitle.setText(task.ref.name);


        if (task.suspended) {
           // viewHolder.moreBtn.setVisibility(View.VISIBLE);
            viewHolder.delayed.setVisibility( View.VISIBLE );

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (task.taskUUID != null){
                        ((MainActivity) parentFragment.getActivity()).openTask(task.taskUUID, true);
                    }
                    else {
                        parentFragment.showTemplateWindow(task.id, true);}
                }
            });
        } else {
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (task.taskUUID != null){
                        ((MainActivity) parentFragment.getActivity()).openTask(task.taskUUID, false);
                    }
                    else {
                    parentFragment.showTemplateWindow(task.id, false);}
                }
            });

            SharedPreferences mSettings;
            mSettings = App.getContext().getSharedPreferences("templates", Context.MODE_PRIVATE);
            String templateJson = mSettings.getString(task.id + LoginHistoryService.getCurrentUser().username, "");

            viewHolder.delayed.setVisibility((task.suspended || templateJson.equals("")) ? View.GONE : View.VISIBLE);
            viewHolder.moreBtn.setVisibility((task.suspended || templateJson.equals("")) ? View.GONE : View.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

}

