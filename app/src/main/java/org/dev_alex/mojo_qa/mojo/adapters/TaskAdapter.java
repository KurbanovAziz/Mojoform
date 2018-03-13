package org.dev_alex.mojo_qa.mojo.adapters;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.dev_alex.mojo_qa.mojo.App;
import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.fragments.TasksFragment;
import org.dev_alex.mojo_qa.mojo.models.Task;
import org.dev_alex.mojo_qa.mojo.services.LoginHistoryService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy | HH.mm", Locale.getDefault());
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
            taskDate = (TextView) itemView.findViewById(R.id.task_date);
            taskTitle = (TextView) itemView.findViewById(R.id.task_title);
            delayed = (TextView) itemView.findViewById(R.id.delayed);
            taskActiveCircle = itemView.findViewById(R.id.task_active);
            taskIcon = (ImageView) itemView.findViewById(R.id.task_icon);
            moreBtn = (ImageView) itemView.findViewById(R.id.more_btn);
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
        String templateId = null;
        String nodeForTasksId = null;
        String siteId = null;
        String initiator = null;
        String resultDocId = null;

        viewHolder.delayed.setVisibility(View.GONE);
        viewHolder.taskActiveCircle.setVisibility((task.suspended) ? View.INVISIBLE : View.VISIBLE);

        if (task.suspended) {
            viewHolder.taskActiveCircle.setVisibility(View.INVISIBLE);

            if (task.complete_time != null) {
                viewHolder.taskDate.setVisibility(View.VISIBLE);
                viewHolder.taskDate.setText(sdf.format(new Date(task.complete_time)));
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

        viewHolder.taskIcon.setImageResource(R.drawable.file_icon);
        viewHolder.moreBtn.setVisibility(View.GONE);

        viewHolder.taskTitle.setText(task.ref.name);


        if (task.suspended) {
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    parentFragment.showTemplateWindow(task.id,true);
                }
            });
        } else {
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    parentFragment.showTemplateWindow(task.id,false);
                }
            });

            SharedPreferences mSettings;
            mSettings = App.getContext().getSharedPreferences("templates", Context.MODE_PRIVATE);
            String templateJson = mSettings.getString(task.id + LoginHistoryService.getCurrentUser().username, "");

            viewHolder.delayed.setVisibility(templateJson.equals("") ? View.GONE : View.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

}

