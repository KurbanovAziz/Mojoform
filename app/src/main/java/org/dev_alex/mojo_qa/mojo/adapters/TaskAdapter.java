package org.dev_alex.mojo_qa.mojo.adapters;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.dev_alex.mojo_qa.mojo.App;
import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.activities.MainActivity;
import org.dev_alex.mojo_qa.mojo.custom_views.scroll.LockableHorizontalScrollView;
import org.dev_alex.mojo_qa.mojo.fragments.TasksFragment;
import org.dev_alex.mojo_qa.mojo.models.Task;
import org.dev_alex.mojo_qa.mojo.services.LoginHistoryService;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;


public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy | HH:mm", Locale.getDefault());
    private ArrayList<Task> tasks;
    private TasksFragment parentFragment;
    private static boolean isFinished = false;

    private static final float CARD_OFFSET_DP = 16.0f;

    public enum TaskType {
        ENDED,
        BUSY,
        PERMANENT
    }

    private TaskType currentTaskType = null;

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        View taskItemContainer;
        CardView taskCard;
        TextView taskTitle;
        TextView taskDate;
        TextView delayed;
        View taskActiveCircle;
        ImageView taskIcon;
        ImageView moreBtn;
        View taskDelete;

        TaskViewHolder(View itemView) {
            super(itemView);
            taskItemContainer = itemView.findViewById(R.id.task_item_container);
            taskCard = itemView.findViewById(R.id.task_card);
            taskDate = itemView.findViewById(R.id.task_date);
            taskTitle = itemView.findViewById(R.id.task_title);
            delayed = itemView.findViewById(R.id.delayed);
            taskActiveCircle = itemView.findViewById(R.id.task_active);
            taskIcon = itemView.findViewById(R.id.task_icon);
            moreBtn = itemView.findViewById(R.id.more_btn);
            taskDelete = itemView.findViewById(R.id.task_delete);

            int offset = (int) getPxFromDp(itemView.getResources(), CARD_OFFSET_DP);
            setViewPadding(taskItemContainer, offset);
            setupViewWidth(itemView.getContext(), taskCard, offset);
        }

        private void setViewPadding(@NonNull View view, int padding) {
            view.setPadding(padding, 0, 0, 0);
        }

        private void setupViewWidth(@NonNull Context context, @NonNull View view, int widthOffsetEnd) {
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            if (windowManager != null) {
                Point point = new Point();
                windowManager.getDefaultDisplay().getSize(point);
                widthOffsetEnd *= 2;
                int minWidth = point.x - widthOffsetEnd;
                view.setMinimumWidth(minWidth);
                if (view.getLayoutParams() != null) view.getLayoutParams().width = minWidth;
            }
        }

        private float getPxFromDp(@NonNull Resources resources, float margin) {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, margin, resources.getDisplayMetrics());
        }
    }


    public TaskAdapter(TasksFragment parentFragment, ArrayList<Task> tasks) {
        this.parentFragment = parentFragment;
        this.tasks = tasks;
        isFinished = false;
    }

    public TaskAdapter(TasksFragment parentFragment, ArrayList<Task> tasks, boolean isFinish) {
        this.parentFragment = parentFragment;
        this.tasks = tasks;
        isFinished = true;
        currentTaskType = TaskType.ENDED;
    }

    public TaskAdapter(TasksFragment parentFragment, ArrayList<Task> tasks, TaskType taskType) {
        this.parentFragment = parentFragment;
        this.tasks = tasks;
        isFinished = false;
        currentTaskType = taskType;
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_task, viewGroup, false);
        return new TaskViewHolder(v);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder viewHolder, int i) {
        final Task task = tasks.get(i);
        SharedPreferences mSettings;
        mSettings = App.getContext().getSharedPreferences("templates", Context.MODE_PRIVATE);
        String templateJson = mSettings.getString(task.id + LoginHistoryService.getCurrentUser().username, "");

        boolean isTaskNotDelayed = task.suspended || templateJson.equals("");

            try {
                ((LockableHorizontalScrollView) viewHolder.itemView).scrollTo(0, 0);
                ((LockableHorizontalScrollView) viewHolder.itemView).setScrollingEnabled(!isTaskNotDelayed);
            } catch (ClassCastException e) {
            }

        viewHolder.delayed.setVisibility(isTaskNotDelayed ? View.GONE : View.VISIBLE);
        viewHolder.taskDelete.setVisibility(isTaskNotDelayed ? View.GONE : View.VISIBLE);
        viewHolder.moreBtn.setVisibility((isTaskNotDelayed ? View.GONE : View.VISIBLE));
        viewHolder.taskActiveCircle.setVisibility((task.suspended) ? View.INVISIBLE : View.VISIBLE);
        Log.e("f", isFinished + "");

        if (task.suspended && !isFinished) {
            viewHolder.taskActiveCircle.setVisibility(View.INVISIBLE);
            if (task.complete_time != null) {
                viewHolder.taskDate.setVisibility(View.VISIBLE);
                viewHolder.delayed.setVisibility(View.VISIBLE);

                viewHolder.taskDate.setText(sdf.format(new Date(task.complete_time / 1000)));
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
        } else if (Objects.equals(task.ref.type, "periodic") || Objects.equals(task.ref.type, "periodic_group")) {
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
            // viewHolder.moreBtn.setVisibility(View.VISIBLE)
            viewHolder.taskCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (task.taskUUID != null) {
                        ((MainActivity) parentFragment.getActivity()).openTask(task.taskUUID, true);
                    } else {
                        parentFragment.showTemplateWindow(task.id, true);
                    }
                }
            });
        } else {
            viewHolder.taskCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (task.taskUUID != null) {
                        ((MainActivity) parentFragment.getActivity()).openTask(task.taskUUID, false);
                    } else {
                        parentFragment.showTemplateWindow(task.id, false);
                    }
                }
            });
        }

        viewHolder.taskDelete.setOnClickListener(v -> removeTask(task, i));
    }


    @Override
    public int getItemCount() {
        return tasks.size();
    }


    private void removeTask(Task task, int position) {
        SharedPreferences prefs = App.getContext().getSharedPreferences("templates", Context.MODE_PRIVATE);
        Map<String, ?> keys = prefs.getAll();
        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            if (entry.getKey().contains(LoginHistoryService.getCurrentUser().username)) {
                String str = entry.getValue().toString();
                try {
                    JSONObject template = new JSONObject(str);
                    if (template.getLong("longId") == task.id) {
                        prefs.edit().remove(entry.getKey()).apply();
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        tasks.remove(task);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount() - position);
    }

}

