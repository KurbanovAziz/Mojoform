package org.dev_alex.mojo_qa.mojo.services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.dev_alex.mojo_qa.mojo.App;
import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.activities.AuthActivity;
import org.dev_alex.mojo_qa.mojo.models.Task;
import org.dev_alex.mojo_qa.mojo.models.Variable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AlarmService extends Service {
    private final static String SCHEDULE_PREFERENCES = "task_scheduler";
    public final static String NOTIFICATIONS_CT = "notifications_count";

    public final static String TASK_ID = "task_id";
    public final static String TEMPLATE_ID = "task_id";
    public final static String TASK_NAME = "task_name";
    public final static String MESSAGE = "message";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("mojo-alarm-log", "onStartCommand " + intent.toString());

        if (intent.hasExtra(TASK_ID)) {
            String taskId = intent.getStringExtra(TASK_ID);
            String message = intent.getStringExtra(MESSAGE);
            if (message.startsWith("overdue"))
                new CheckIfTaskFinishedTask(taskId, message, intent.getStringExtra(TASK_NAME)).execute();
            else
                new CheckIfTaskFinishedTask(taskId, message).execute();
        } else {
            if (TokenService.isTokenExists()) {
                new UpdateTasksTask().execute();
                new KeepTokenAliveTask().execute();
            }
        }
        return START_NOT_STICKY;
    }

    public static void scheduleAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, AlarmService.class);
        PendingIntent pi = PendingIntent.getService(context, 5857500, i, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 10 * 60 * 1000, pi);
    }


    private boolean checkIfTaskScheduled(String taskId) {
        SharedPreferences mSettings;
        mSettings = App.getContext().getSharedPreferences(SCHEDULE_PREFERENCES, Context.MODE_PRIVATE);

        String tokenStr = mSettings.getString("task_" + taskId, "");
        return !tokenStr.equals("");
    }

    private void scheduleTask(Task task) {
        Log.d("mojo-alarm-log", "scheduleTask " + task.id);

        SharedPreferences mSettings;
        mSettings = App.getContext().getSharedPreferences(SCHEDULE_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSettings.edit();

        if (task.dueDate.getTime() - new Date().getTime() > 60 * 60 * 1000) {
            Intent intent = new Intent(getApplicationContext(), AlarmService.class);
            intent.putExtra(TASK_ID, task.id);
            intent.putExtra(MESSAGE, getTaskName(task) + ". Время 1 час.");
            createAlarmTask(intent, task.dueDate.getTime() - 60 * 60 * 1000);
        }

        if (task.dueDate.getTime() - new Date().getTime() > 15 * 60 * 1000) {
            Intent intent = new Intent(getApplicationContext(), AlarmService.class);
            intent.putExtra(TASK_ID, task.id);
            intent.putExtra(MESSAGE, getTaskName(task) + ". Время 15 минут.");
            createAlarmTask(intent, task.dueDate.getTime() - 15 * 60 * 1000);
        }

        if (task.dueDate.after(new Date())) {
            Intent intent = new Intent(getApplicationContext(), AlarmService.class);
            intent.putExtra(TASK_ID, task.id);
            intent.putExtra(MESSAGE, getTaskName(task) + ". Сейчас.");
            createAlarmTask(intent, task.dueDate.getTime());
        }

        boolean overdueAlarmSent = false;
        long overDueTime = task.dueDate.getTime() + 60 * 60 * 1000;
        while (!overdueAlarmSent) {
            if (new Date().after(new Date(overDueTime)))
                overDueTime += 60 * 60 * 1000;
            else {
                Intent intent = new Intent(getApplicationContext(), AlarmService.class);
                intent.putExtra(TASK_ID, task.id);
                intent.putExtra(MESSAGE, "overdue");
                intent.putExtra(TASK_NAME, getTaskName(task));
                createAlarmTask(intent, overDueTime);
                overdueAlarmSent = true;
            }
        }

        editor.putString("task_" + task.id, "yea");
        editor.apply();
    }

    private void createAlarmTask(Intent intent, long triggerAt) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        Log.d("mojo-alarm-log", "scheduleTask time= " + sdf.format(triggerAt));


        Random random = new Random(SystemClock.elapsedRealtime());
        PendingIntent pi = PendingIntent.getService(getBaseContext(), (int) Math.abs(SystemClock.elapsedRealtime() * random.nextInt(1002)), intent, 0);

        AlarmManager am = (AlarmManager) getBaseContext().getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, triggerAt, pi);
    }

    private void showNotification(String message, String taskId, String templateId) {
        Log.d("mojo-alarm-log", "showNotification " + message);

        Context context = getApplicationContext();

        Intent notificationIntent = new Intent(context, AuthActivity.class);
        notificationIntent.putExtra(TASK_ID, taskId);
        notificationIntent.putExtra(TEMPLATE_ID, templateId);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.logo)
                .setLargeIcon(getNotificationIcon())
                .setAutoCancel(true)
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setContentTitle("Напоминание о задаче")
                .setContentText(message);

        Notification notification = builder.build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(123321, notification);
    }

    private Bitmap getNotificationIcon() {
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout notificationLayout = (RelativeLayout) inflater.inflate(R.layout.notification_icon_layout, null);
        notificationLayout.setLayoutParams(new RelativeLayout.LayoutParams(getResources().getDisplayMetrics().widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT));
        ((TextView) notificationLayout.findViewById(R.id.notifications_ct)).setText(String.valueOf(getNotificationsCt()));

        Bitmap notificationIcon;
        notificationLayout.setDrawingCacheEnabled(true);
        notificationLayout.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
        notificationLayout.buildDrawingCache();
        try {
            notificationIcon = notificationLayout.getDrawingCache();
            return notificationIcon;
        } finally {
            notificationLayout.setDrawingCacheEnabled(false);
        }
    }

    private int getNotificationsCt() {
        SharedPreferences mSettings;
        mSettings = App.getContext().getSharedPreferences(SCHEDULE_PREFERENCES, Context.MODE_PRIVATE);

        return mSettings.getInt(NOTIFICATIONS_CT, 0);
    }

    private void setNotificationsCt(int notificationsCt) {
        if (notificationsCt < 0)
            notificationsCt = 0;

        SharedPreferences mSettings;
        mSettings = App.getContext().getSharedPreferences(SCHEDULE_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSettings.edit();

        editor.putInt(NOTIFICATIONS_CT, notificationsCt);
        editor.apply();
    }

    private String getTaskName(Task task) {
        if (task.processInstanceId == null) {
            return task.name;
        } else {
            if (task.variables != null)
                for (Variable variable : task.variables) {
                    if (variable.name.equals("TemplateName"))
                        return (variable.value);
                }
            return "";
        }
    }

    private String getTaskTemplateId(Task task) {
        if (task.processInstanceId == null) {
            return task.name;
        } else {
            if (task.variables != null)
                for (Variable variable : task.variables) {
                    if (variable.name.equals("TemplateId"))
                        return (variable.value);
                }
            return "";
        }
    }

    private class KeepTokenAliveTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                Response response = RequestService.createGetRequest("/api/user/info");
                Log.d("mojo-alarm-log", "KeepTokenAliveTask " + response.code());

            } catch (Exception exc) {
                exc.printStackTrace();
            }
            return null;
        }
    }

    private class UpdateTasksTask extends AsyncTask<Void, Void, Boolean> {
        private ArrayList<Task> tasks;

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                String userName = TokenService.getUsername();
                SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault());
                String dateParams;

                Calendar monthCalendar = Calendar.getInstance();
                monthCalendar.setTime(new Date());
                monthCalendar.set(Calendar.DAY_OF_MONTH, 1);
                dateParams = "&dueAfter=" + isoDateFormat.format(monthCalendar.getTime());

                monthCalendar.add(Calendar.MONTH, 1);
                dateParams += "&dueBefore=" + isoDateFormat.format(monthCalendar.getTime());

                String url = "https://activiti.dev-alex.org/activiti-rest/service/runtime/tasks?taskAssignee="
                        + userName + "&includeProcessVariables=TRUE" + dateParams;

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().header("Authorization", Credentials.basic("kermit", "kermit"))
                        .url(url).build();

                Response response = client.newCall(request).execute();
                if (response.code() == 200) {
                    JSONArray tasksJson = new JSONObject(response.body().string()).getJSONArray("data");
                    tasks = new ObjectMapper().readValue(tasksJson.toString(), new TypeReference<ArrayList<Task>>() {
                    });
                    return true;
                }
                return false;

            } catch (Exception exc) {
                exc.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            int notificationsCt = 0;
            if (result) {
                for (Task task : tasks) {
                    if (!checkIfTaskScheduled(task.id))
                        scheduleTask(task);
                    if (task.dueDate.getTime() - new Date().getTime() < 60 * 60 * 1000)
                        notificationsCt++;
                }
                setNotificationsCt(notificationsCt);
            }
        }
    }

    private class CheckIfTaskFinishedTask extends AsyncTask<Void, Void, Boolean> {
        private Task task;
        private String taskId;
        private String message;
        private String taskName;

        CheckIfTaskFinishedTask(String taskId, String message, String taskName) {
            this.taskId = taskId;
            this.message = message;
            this.taskName = taskName;
        }

        CheckIfTaskFinishedTask(String taskId, String message) {
            this.taskId = taskId;
            this.message = message;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                String url = "https://activiti.dev-alex.org/activiti-rest/service/runtime/tasks/" + taskId + "?includeProcessVariables=TRUE";

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().header("Authorization", Credentials.basic("kermit", "kermit"))
                        .url(url).build();

                Response response = client.newCall(request).execute();
                if (response.code() == 200) {
                    task = new ObjectMapper().readValue(response.body().string(), Task.class);
                    return true;
                }
                return false;

            } catch (Exception exc) {
                exc.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            Log.d("mojo-alarm-log", "CheckIfTaskFinishedTask " + result);

            if (result) {
                if (task.suspended != null && !task.suspended) {
                    if (message.startsWith("overdue")) {
                        Log.d("mojo-test-log", "currentDate " + new Date().getTime() + " dueDate " + task.dueDate.getTime());
                        int hoursCt = (int) Math.abs((new Date().getTime() - task.dueDate.getTime()) / (60 * 60 * 1000));
                        message = String.format(Locale.getDefault(), "%s. Просрочено %d час%s(ов)", taskName, hoursCt, hoursCt == 1 ? "" : "a");

                        long overDueTime = task.dueDate.getTime() + (hoursCt + 1) * (60 * 60 * 1000);
                        Intent intent = new Intent(getApplicationContext(), AlarmService.class);
                        intent.putExtra(TASK_ID, task.id);
                        intent.putExtra(MESSAGE, "overdue");
                        intent.putExtra(TASK_NAME, getTaskName(task));
                        createAlarmTask(intent, overDueTime);
                    }
                    showNotification(message, taskId, getTaskTemplateId(task));
                }
            }
        }
    }
}
