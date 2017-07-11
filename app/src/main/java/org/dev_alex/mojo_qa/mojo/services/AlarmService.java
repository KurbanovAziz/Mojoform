package org.dev_alex.mojo_qa.mojo.services;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.dev_alex.mojo_qa.mojo.App;
import org.dev_alex.mojo_qa.mojo.Data;
import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.activities.AuthActivity;
import org.dev_alex.mojo_qa.mojo.activities.MainActivity;
import org.dev_alex.mojo_qa.mojo.models.Task;
import org.dev_alex.mojo_qa.mojo.models.Variable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AlarmService extends Service {
    private final static String SCHEDULE_PREFERENCES = "task_scheduler";
    public final static String NOTIFICATIONS_CT = "notifications_count";

    public final static String TASK_ID = "task_id";
    public final static String NODE_FOR_TASKS = "task_node_id";
    public final static String TASK_NAME = "task_name";
    public final static String TEMPLATE_ID = "template_id";
    public final static String MESSAGE = "message";
    public final static String DUE_DATE = "due_date";
    public final static String INITIATOR = "initiator";
    public final static String SITE_ID = "site_id";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra(TASK_ID)) {
            Log.d("mojo-alarm-log", "onStartCommand task_id =" + intent.getStringExtra(TASK_ID));

            String taskId = intent.getStringExtra(TASK_ID);
            String taskName = intent.getStringExtra(TASK_NAME);
            String initiator = intent.getStringExtra(INITIATOR);
            String siteId = intent.getStringExtra(SITE_ID);
            String templateId = intent.getStringExtra(TEMPLATE_ID);
            String nodeForTasksID = intent.getStringExtra(NODE_FOR_TASKS);
            String message = intent.getStringExtra(MESSAGE);
            new CheckIfTaskFinishedTask(taskId, taskName, templateId, message, nodeForTasksID, initiator, siteId).execute();
        } else {
            Log.d("mojo-alarm-log", "onStartCommand update tasks" + new Date().toString());
            if (TokenService.isTokenExists()) {
                new UpdateTasksTask().execute();
                new KeepTokenAliveTask().execute();
            }
        }

        return START_STICKY;
    }

    public static void scheduleAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, AlarmService.class);
        PendingIntent pi = PendingIntent.getService(context, 5857500, i, PendingIntent.FLAG_UPDATE_CURRENT);
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 10 * 60 * 1000, pi);
    }

    private boolean checkIfTaskScheduled(String taskId) {
        SharedPreferences mSettings;
        mSettings = App.getContext().getSharedPreferences(SCHEDULE_PREFERENCES, Context.MODE_PRIVATE);

        String tokenStr = mSettings.getString("task_" + taskId, "");
        return !tokenStr.equals("");
    }

    private void scheduleTask(Task task) {
        Random random = new Random(SystemClock.elapsedRealtime());
        Log.d("mojo-alarm-log", "scheduleTask " + task.id);

        SharedPreferences mSettings;
        mSettings = App.getContext().getSharedPreferences(SCHEDULE_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSettings.edit();

        if (task.dueDate.getTime() - new Date().getTime() > 60 * 60 * 1000) {
            Intent intent = new Intent(getApplicationContext(), AlarmService.class);
            intent.putExtra(TASK_ID, task.id);
            intent.putExtra(TASK_NAME, getTaskName(task));
            intent.putExtra(SITE_ID, getTaskSiteId(task));
            intent.putExtra(INITIATOR, getTaskInitiator(task));
            intent.putExtra(TEMPLATE_ID, getTaskTemplateId(task));
            intent.putExtra(NODE_FOR_TASKS, getTaskNodeId(task));
            intent.putExtra(MESSAGE, "Через час.");
            createAlarmTask(random, intent, task.dueDate.getTime() - 60 * 60 * 1000);
        }

        if (task.dueDate.getTime() - new Date().getTime() > 15 * 60 * 1000) {
            Intent intent = new Intent(getApplicationContext(), AlarmService.class);
            intent.putExtra(TASK_ID, task.id);
            intent.putExtra(TASK_NAME, getTaskName(task));
            intent.putExtra(SITE_ID, getTaskSiteId(task));
            intent.putExtra(INITIATOR, getTaskInitiator(task));
            intent.putExtra(TEMPLATE_ID, getTaskTemplateId(task));
            intent.putExtra(NODE_FOR_TASKS, getTaskNodeId(task));
            intent.putExtra(MESSAGE, "Через 15 минут.");
            createAlarmTask(random, intent, task.dueDate.getTime() - 15 * 60 * 1000);
        }

        if (task.dueDate.after(new Date())) {
            Intent intent = new Intent(getApplicationContext(), AlarmService.class);
            intent.putExtra(TASK_ID, task.id);
            intent.putExtra(TASK_NAME, getTaskName(task));
            intent.putExtra(SITE_ID, getTaskSiteId(task));
            intent.putExtra(INITIATOR, getTaskInitiator(task));
            intent.putExtra(TEMPLATE_ID, getTaskTemplateId(task));
            intent.putExtra(NODE_FOR_TASKS, getTaskNodeId(task));
            intent.putExtra(MESSAGE, "Сейчас.");
            createAlarmTask(random, intent, task.dueDate.getTime());
        }

        boolean overdueAlarmSent = false;
        long overDueTime = task.dueDate.getTime() + 60 * 60 * 1000;
        while (!overdueAlarmSent) {
            if (new Date().after(new Date(overDueTime)))
                overDueTime += 60 * 60 * 1000;
            else {
                if (overDueTime > new Date().getTime() + 3 * 60 * 60 * 1000)
                    break;

                Intent intent = new Intent(getApplicationContext(), AlarmService.class);
                intent.putExtra(TASK_ID, task.id);
                intent.putExtra(TASK_NAME, getTaskName(task));
                intent.putExtra(SITE_ID, getTaskSiteId(task));
                intent.putExtra(INITIATOR, getTaskInitiator(task));
                intent.putExtra(TEMPLATE_ID, getTaskTemplateId(task));
                intent.putExtra(NODE_FOR_TASKS, getTaskNodeId(task));
                intent.putExtra(MESSAGE, "overdue");
                createAlarmTask(random, intent, overDueTime, 60 * 60 * 1000);
                overdueAlarmSent = true;
            }
        }

        editor.putString("task_" + task.id, "yea");
        editor.apply();
    }

    private void createAlarmTask(Random random, Intent intent, long triggerAt) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        Log.d("mojo-alarm-log", "scheduleTask time= " + sdf.format(triggerAt));

        PendingIntent pi = PendingIntent.getService(getApplicationContext(), (int) Math.abs(SystemClock.elapsedRealtime() * random.nextInt(10012)), intent, 0);
        AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, triggerAt, pi);
    }

    private void createAlarmTask(Random random, Intent intent, long triggerAt, long interval) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        Log.d("mojo-alarm-log", "scheduleTask time= " + sdf.format(triggerAt));

        PendingIntent pi = PendingIntent.getService(getApplicationContext(), (int) Math.abs(SystemClock.elapsedRealtime() * random.nextInt(10012)), intent, 0);
        AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, triggerAt, interval, pi);
    }

    private void showNotification(String taskName, String message, String taskId, String templateId,
                                  String nodeForTasksId, long dueDate, String initiator, String siteId) {
        Log.d("mojo-alarm-log", "showNotification " + message);
        try {
            if (Data.currentTaskId != null && Data.currentTaskId.equals(taskId) && isForeground(MainActivity.class.getPackage().getName()))
                return;
        } catch (Exception exc) {
            exc.printStackTrace();
        }

        if (taskName.isEmpty())
            taskName = "NoName";

        Context context = getApplicationContext();

        Intent notificationIntent = new Intent(context, AuthActivity.class);
        notificationIntent.putExtra(TASK_ID, taskId);
        notificationIntent.putExtra(TEMPLATE_ID, templateId);
        notificationIntent.putExtra(NODE_FOR_TASKS, nodeForTasksId);
        notificationIntent.putExtra(DUE_DATE, dueDate);
        notificationIntent.putExtra(SITE_ID, siteId);
        notificationIntent.putExtra(INITIATOR, initiator);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(context, (int) new Date().getTime(), notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification.Builder builder = new Notification.Builder(context);
        Bitmap notificationIcon = getNotificationIcon();
        if (notificationIcon == null)
            notificationIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        builder.setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.logo_small)
                .setLargeIcon(notificationIcon)
                .setAutoCancel(true)
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setContentTitle(taskName)
                .setContentText(message);

        Notification notification = builder.build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(123321, notification);
    }

    private Bitmap getNotificationIcon() {
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout notificationLayout = (RelativeLayout) inflater.inflate(R.layout.notification_icon_layout, null);
        ((TextView) notificationLayout.findViewById(R.id.notifications_ct)).setText(String.valueOf(getNotificationsCt()));

        notificationLayout.setDrawingCacheEnabled(true);
        notificationLayout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        notificationLayout.layout(0, 0, notificationLayout.getMeasuredWidth(), notificationLayout.getMeasuredHeight());
        notificationLayout.buildDrawingCache(true);

        Bitmap notificationIcon;
        try {
            notificationIcon = Bitmap.createBitmap(notificationLayout.getDrawingCache());
            BitmapService.resizeBitmap(notificationIcon, 128);
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

    private String getTaskNodeId(Task task) {
        if (task.processInstanceId == null) {
            return task.name;
        } else {
            if (task.variables != null)
                for (Variable variable : task.variables) {
                    if (variable.name.equals("DocumentFolderUUID"))
                        return (variable.value);
                }
            return "";
        }
    }

    private String getTaskTemplateId(Task task) {
        for (Variable variable : task.variables) {
            if (variable.name.equals("TemplateId"))
                return (variable.value);
        }
        return "";
    }

    private String getTaskSiteId(Task task) {
        for (Variable variable : task.variables) {
            if (variable.name.equals("DocumentSiteId"))
                return (variable.value);
        }
        return "gzip";
    }

    private String getTaskInitiator(Task task) {
        for (Variable variable : task.variables) {
            if (variable.name.equals("initiator"))
                return (variable.value);
        }
        return "admin";
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
                isoDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                String dateParams;

                Calendar monthCalendar = Calendar.getInstance();
                monthCalendar.setTime(new Date());
                monthCalendar.set(Calendar.DAY_OF_MONTH, 1);
                dateParams = "&dueAfter=" + isoDateFormat.format(monthCalendar.getTime());

                monthCalendar.add(Calendar.MONTH, 1);
                dateParams += "&dueBefore=" + isoDateFormat.format(monthCalendar.getTime());
                String sortParams = "&sort=dueDate&order=desc&size=100";

                String url = App.getTask_host() + "/runtime/tasks?assignee="
                        + userName + "&includeProcessVariables=TRUE" + dateParams + sortParams;

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().header("Authorization", Credentials.basic(userName, Data.taskAuthPass))
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
            try {
                int notificationsCt = 0;
                if (result) {
                    for (Task task : tasks) {
                        if (!checkIfTaskScheduled(task.id))
                            scheduleTask(task);
                        if (task.dueDate.getTime() - new Date().getTime() < 60 * 60 * 1000)
                            if (task.assignee.toLowerCase().equals(TokenService.getUsername().toLowerCase()))
                                notificationsCt++;
                    }
                    setNotificationsCt(notificationsCt);
                }
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }

    private class CheckIfTaskFinishedTask extends AsyncTask<Void, Void, Boolean> {
        private Task task;
        private String taskId;
        private String taskName;
        private String nodeForTasksId;
        private String templateId;
        private String message;
        private String initiator;
        private String siteId;

        CheckIfTaskFinishedTask(String taskId, String taskName, String templateId, String message,
                                String nodeForTasksId, String initiator, String siteId) {
            this.taskId = taskId;
            this.taskName = taskName;
            this.templateId = templateId;
            this.message = message;
            this.nodeForTasksId = nodeForTasksId;
            this.initiator = initiator;
            this.siteId = siteId;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                String url = App.getTask_host() + "/runtime/tasks/" + taskId;

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().header("Authorization", Credentials.basic(TokenService.getUsername(), Data.taskAuthPass))
                        .url(url).build();

                Response response = client.newCall(request).execute();
                if (response.code() == 200) {
                    task = new ObjectMapper().readValue(response.body().string(), Task.class);
                    Log.d("mojo-alarm-log", "CheckIfTaskFinishedTask task= " + task.toString());
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
            try {
                Log.d("mojo-alarm-log", "CheckIfTaskFinishedTask " + result);

                if (result) {
                    if (task.suspended != null && !task.suspended && task.assignee.toLowerCase().equals(TokenService.getUsername().toLowerCase())) {
                        if (message.startsWith("overdue")) {
                            Log.d("mojo-test-log", "currentDate " + new Date().getTime() + " dueDate " + task.dueDate.getTime());
                            int hoursCt = (int) Math.abs((new Date().getTime() - task.dueDate.getTime()) / (60 * 60 * 1000));
                            message = String.format(Locale.getDefault(), "Просрочено %d час%s(ов)", hoursCt, hoursCt == 1 ? "" : "a");
                        }
                        showNotification(taskName, message, taskId, templateId, nodeForTasksId,
                                task.dueDate != null ? task.dueDate.getTime() : new Date().getTime(), initiator, siteId);
                    }
                }
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }

    public boolean isForeground(String myPackage) {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1);
        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
        return componentInfo.getPackageName().equals(myPackage);
    }
}
