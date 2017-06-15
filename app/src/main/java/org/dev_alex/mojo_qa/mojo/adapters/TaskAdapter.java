package org.dev_alex.mojo_qa.mojo.adapters;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.fragments.TasksFragment;
import org.dev_alex.mojo_qa.mojo.models.Task;
import org.dev_alex.mojo_qa.mojo.models.Variable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private Context context;
    private ArrayList<Task> tasks;
    private TasksFragment parentFragment;

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitle;
        TextView taskDate;
        View taskActiveCircle;
        ImageView taskIcon;
        ImageView moreBtn;

        TaskViewHolder(View itemView) {
            super(itemView);
            taskDate = (TextView) itemView.findViewById(R.id.task_date);
            taskTitle = (TextView) itemView.findViewById(R.id.task_title);
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

        viewHolder.taskActiveCircle.setVisibility((task.suspended == null || task.suspended) ? View.INVISIBLE : View.VISIBLE);
        if (task.suspended != null && !task.suspended) {
            ((GradientDrawable) viewHolder.taskActiveCircle.getBackground()).setColor(task.dueDate.after(new Date()) ? Color.GREEN : Color.RED);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy | HH.mm", Locale.getDefault());
        viewHolder.taskDate.setText(sdf.format(task.dueDate));

        if (task.processInstanceId == null) {
            viewHolder.taskIcon.setImageResource(R.drawable.profile_icon);
            viewHolder.taskTitle.setText(task.name);
            viewHolder.moreBtn.setVisibility(View.VISIBLE);
        } else {
            viewHolder.taskIcon.setImageResource(R.drawable.file_icon);
            viewHolder.moreBtn.setVisibility(View.GONE);

            viewHolder.taskTitle.setText("");
            if (task.variables != null)
                for (Variable variable : task.variables) {
                    if (variable.name.equals("TemplateName"))
                        viewHolder.taskTitle.setText(variable.value);
                    if (variable.name.equals("TemplateId"))
                        templateId = variable.value;
                    if (variable.name.equals("DocumentFolderUUID"))
                        nodeForTasksId = variable.value;
                    if (variable.name.equals("DocumentSiteId"))
                        siteId = variable.value;
                    if (variable.name.equals("initiator"))
                        initiator = variable.value;
                }
        }

        if (templateId != null) {
            final String finalTemplateId = templateId;
            final String finalNodeForTasksId = nodeForTasksId;

            if (task.suspended == null || task.suspended)
                viewHolder.itemView.setOnClickListener(null);
            else {
                if (initiator == null || initiator.isEmpty())
                    initiator = "admin";
                if (siteId == null || siteId.isEmpty())
                    initiator = "gzip";

                final String finalInitiator = initiator;
                final String finalSiteId = siteId;
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        parentFragment.showFillTemplateWindow(finalTemplateId,
                                task.id, finalNodeForTasksId, task.dueDate.getTime(), finalInitiator, finalSiteId);
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }
}

