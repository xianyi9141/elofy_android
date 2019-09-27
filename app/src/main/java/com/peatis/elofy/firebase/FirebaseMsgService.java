package com.peatis.elofy.firebase;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.peatis.elofy.Elofy;
import com.peatis.elofy.R;
import com.peatis.elofy.activities.BaseActivity;
import com.peatis.elofy.activities.LoginActivity;
import com.peatis.elofy.activities.MainActivity;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class FirebaseMsgService extends FirebaseMessagingService {
    static final String NOTIFICATION_CHANNEL_ID = "channel-1";

    @Override
    public void onNewToken(String token) {
        if (token != null) {
            LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
            Intent intent = new Intent(Elofy.KEY_FIREBASE_TOKEN);
            intent.putExtra(Elofy.KEY_FIREBASE_TOKEN, token);
            broadcastManager.sendBroadcast(intent);

            Log.d(BaseActivity.TAG, "Firebase token generated: " + token);
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Check login
        if (Elofy.string(this, Elofy.KEY_AUTH_TOKEN) == null) {
            return;
        }

        // Show notification
        notification(remoteMessage.getData().get("title"), remoteMessage.getData().get("body"));
    }

    private void notification(String title, String body) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Elofy Notifications", NotificationManager.IMPORTANCE_HIGH);

            // Configure the notification channel.
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "channel-1");

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        notificationBuilder
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(bitmap)
                .setTicker("elofy")
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(false);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
        } else {
            notificationBuilder.setPriority(NotificationManager.IMPORTANCE_HIGH);
        }

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Elofy.KEY_NOTIFIED, true);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(intent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(pendingIntent);

        notificationManager.notify(1, notificationBuilder.build());
    }
}
