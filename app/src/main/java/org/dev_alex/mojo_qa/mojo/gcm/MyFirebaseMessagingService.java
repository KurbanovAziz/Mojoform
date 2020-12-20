package org.dev_alex.mojo_qa.mojo.gcm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.activities.AuthActivity;

import java.util.Map;
import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static final String CHANNEL_ID = "mojo_channel_1";
    private static final String TAG = "MyFirebaseMsgService";

    public final static String TASK_ID = "taskID";
    public final static String NOTIFY_ID = "notifyID";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage == null || remoteMessage.getNotification() == null) {
            return;
        }

        String title = remoteMessage.getNotification().getTitle();
        String body = remoteMessage.getNotification().getBody();
        Map<String, String> data = remoteMessage.getData();
        if (data == null) {
            return;
        }

        try {
            sendNotification(title, body, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createNotificationChannel() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "mojo_channel";
            String Description = "Mojo Main Channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            mChannel.enableLights(true);
            mChannel.setLightColor(ContextCompat.getColor(this, R.color.colorPrimary));
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mChannel.setShowBadge(false);

            if (notificationManager != null)
                notificationManager.createNotificationChannel(mChannel);
        }
    }

    private void sendNotification(String title, String body, Map<String, String> data) {
        Intent intent = new Intent(this, AuthActivity.class);

        for (Map.Entry<String, String> entry : data.entrySet()) {
            Log.d(TAG, entry.getKey() + " " + entry.getValue());
            intent.putExtra(entry.getKey(), entry.getValue());
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, new Random().nextInt(222222), intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        createNotificationChannel();
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.watermark_logo)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        int notificationId = 4213;

        if (data.containsKey(NOTIFY_ID)) {
            notificationId = Integer.parseInt(data.get(NOTIFY_ID));
        } else if (data.containsKey(TASK_ID)) {
            notificationId = Integer.parseInt(data.get(TASK_ID));
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, notificationBuilder.build());
    }
}