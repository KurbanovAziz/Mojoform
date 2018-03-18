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
import android.graphics.Color;
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
import org.dev_alex.mojo_qa.mojo.models.User;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import okhttp3.Response;

public class AlarmService extends Service {
    private final static String SCHEDULE_PREFERENCES = "task_scheduler";
    public final static String NOTIFICATIONS_CT = "notifications_count";

    public final static String TASK_ID = "task_id";
    public final static String MESSAGE = "message";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("mojo-alarm-log", "onStartCommand");

        if (intent != null && intent.hasExtra(TASK_ID)) {
            Log.d("mojo-alarm-log", "onStartCommand task_id =" + intent.getStringExtra(TASK_ID));

            long taskId = intent.getLongExtra(TASK_ID, 0);
            String message = intent.getStringExtra(MESSAGE);
            new CheckIfTaskFinishedTask(taskId, message).execute();
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
        PendingIntent pi = PendingIntent.getService(context, 58550, i, PendingIntent.FLAG_UPDATE_CURRENT);
        if (am != null)
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + 100, 10 * 60 * 1000, pi);
    }

    private boolean checkIfTaskScheduled(long taskId) {
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

        if (task.expire_time == null)
            return;


        if (task.expire_time - new Date().getTime() > 60 * 60 * 1000) {
            Intent intent = new Intent(getApplicationContext(), AlarmService.class);
            intent.putExtra(TASK_ID, task.id);
            intent.putExtra(MESSAGE, "Через час.");
            createAlarmTask(random, intent, task.expire_time - 60 * 60 * 1000);
        }

        if (task.expire_time - new Date().getTime() > 15 * 60 * 1000) {
            Intent intent = new Intent(getApplicationContext(), AlarmService.class);
            intent.putExtra(TASK_ID, task.id);
            intent.putExtra(MESSAGE, "Через 15 минут.");
            createAlarmTask(random, intent, task.expire_time - 15 * 60 * 1000);
        }

        if (new Date(task.expire_time).after(new Date())) {
            Intent intent = new Intent(getApplicationContext(), AlarmService.class);
            intent.putExtra(TASK_ID, task.id);
            intent.putExtra(MESSAGE, "Сейчас.");
            createAlarmTask(random, intent, task.expire_time);
        }

        boolean overdueAlarmSent = false;
        long overDueTime = task.expire_time + 60 * 60 * 1000;
        while (!overdueAlarmSent) {
            if (new Date().after(new Date(overDueTime)))
                overDueTime += 60 * 60 * 1000;
            else {
                Intent intent = new Intent(getApplicationContext(), AlarmService.class);
                intent.putExtra(TASK_ID, task.id);
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

    private void showNotification(long taskId, String taskName, String message) {
        Log.d("mojo-alarm-log", "showNotification " + message);
        try {
            if (Data.currentTaskId != null && Data.currentTaskId.equals(taskId) && isForeground(MainActivity.class.getPackage().getName()))
                return;
        } catch (Exception exc) {
            exc.printStackTrace();
        }

        Context context = getApplicationContext();

        Intent notificationIntent = new Intent(context, AuthActivity.class);
        notificationIntent.putExtra(TASK_ID, taskId);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(context, (int) new Date().getTime(), notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification.Builder builder = new Notification.Builder(context);
        Bitmap notificationIcon = getNotificationIcon();
        if (notificationIcon == null)
            notificationIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        builder.setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.logo_notification)
                .setLargeIcon(notificationIcon)
                .setAutoCancel(true)
                .setLights(Color.parseColor("#632E83"), 1000, 3000)
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

    private class KeepTokenAliveTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                Response response = RequestService.createGetRequest("/api/user/");
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
                Response response = RequestService.createGetRequest("/api/user/");
                if (!response.isSuccessful()) {
                    JSONObject requestJson = new JSONObject();
                    requestJson.put("app", "android");
                    requestJson.put("username", LoginHistoryService.getCurrentUser().username);
                    requestJson.put("refresh_token", LoginHistoryService.getCurrentUser().refresh_token);
                    response = RequestService.createPostRequest("/api/user/login/app", requestJson.toString());

                    if (response.isSuccessful()) {
                        String userJson = response.body().string();
                        User user = new ObjectMapper().readValue(userJson, User.class);

                        LoginHistoryService.setCurrentUser(user);
                        TokenService.updateToken(user.token, user.username);
                    } else {
                        return false;
                    }
                }

                response = RequestService.createGetRequest("/api/tasks/active?order=expire&filter=oneshot,periodic");
                //Log.d("mojo-alarm-log", "tasks upd response code = " + response.code());

                if (response.code() == 200) {
                    JSONArray tasksJson = new JSONArray(response.body().string());
                    tasks = new ObjectMapper().readValue(tasksJson.toString(), new TypeReference<ArrayList<Task>>() {
                    });

                    for (Task task : tasks)
                        task.fixTime();

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
                Log.d("mojo-alarm-log", "tasks size = " + ((result == null || !result) ? "exc" : tasks.size()));

                int notificationsCt = 0;
                if (result) {
                    for (Task task : tasks) {
                        if (!checkIfTaskScheduled(task.id))
                            scheduleTask(task);
                        if (task.expire_time != null && (task.expire_time - new Date().getTime() < 60 * 60 * 1000))
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
        private long taskId;
        private boolean isSuspended;
        private String message;

        CheckIfTaskFinishedTask(long taskId, String message) {
            this.taskId = taskId;
            this.message = message;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Response response = RequestService.createGetRequest("/api/user/");
                if (!response.isSuccessful()) {
                    JSONObject requestJson = new JSONObject();
                    requestJson.put("app", "android");
                    requestJson.put("username", LoginHistoryService.getCurrentUser().username);
                    requestJson.put("refresh_token", LoginHistoryService.getCurrentUser().refresh_token);
                    response = RequestService.createPostRequest("/api/user/login/app", requestJson.toString());

                    if (response.isSuccessful()) {
                        String userJson = response.body().string();
                        User user = new ObjectMapper().readValue(userJson, User.class);

                        LoginHistoryService.setCurrentUser(user);
                        TokenService.updateToken(user.token, user.username);
                    } else {
                        return false;
                    }
                }

                response = RequestService.createGetRequest("/api/tasks/" + taskId);
                if (response.code() == 200) {
                    String jsonStr = response.body().string();
                    task = new ObjectMapper().readValue(jsonStr, Task.class);
                    isSuspended = new JSONObject(jsonStr).has("document");
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
                    if (!isSuspended) {
                        if (message.startsWith("overdue")) {
                            Log.d("mojo-test-log", "currentDate " + new Date().getTime() + " dueDate " + task.expire_time);
                            int hoursCt = (int) Math.abs((new Date().getTime() - task.expire_time) / (60 * 60 * 1000));
                            message = String.format(Locale.getDefault(), "Просрочено %d час%s(ов)", hoursCt, hoursCt == 1 ? "" : "a");

                            if (hoursCt < 4)
                                showNotification(taskId, message, task.ref.name);
                        } else {
                            showNotification(taskId, message, task.ref.name);
                        }
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
