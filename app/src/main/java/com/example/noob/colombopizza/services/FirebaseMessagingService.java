package com.example.noob.colombopizza.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.noob.colombopizza.Cart;
import com.example.noob.colombopizza.Common.Common;
import com.example.noob.colombopizza.R;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    String TAG = "FirebaseMessagingService";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        System.out.println("token --->"+token);
        Common.FCM_Token = token;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "onMessageReceived: " + remoteMessage.getFrom());
        Log.d(TAG, "onMessage: " + remoteMessage);


        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "onMessageReceived Notification Body: " + remoteMessage.getNotification().getBody());
            String mm = remoteMessage.getData().get("id");

            Log.d(TAG, "onMessageReceived: id is ---->  "+mm);

            initChannels(this);
            generateNotification(this, remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    public void initChannels(Context context) {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel channel = new NotificationChannel("default",
                "Channel name",
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Channel description");
        notificationManager.createNotificationChannel(channel);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void generateNotification(Context context, String message) {

        int icon = R.drawable.ic_attach_money_black_24dp;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String title = context.getString(R.string.app_name);

        Intent intent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent,0);
        Notification.Builder notificationBuilder = new Notification.Builder(context)
                .setTicker(title).setContentTitle(title)
                .setContentText(message).setSmallIcon(R.drawable.logo)
                .setContentIntent(pendingIntent);
        Notification notification = notificationBuilder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        assert nm != null;
        nm.notify(0,notification);



//        Notification.Builder mBuilder = new Notification.Builder(context, "default")
//                .setSmallIcon(icon)
//                .setContentTitle(title)
//                .setContentText(message);
//        Notification notification = mBuilder.build();
//
//        notification.flags |= Notification.FLAG_AUTO_CANCEL;
//        notification.defaults |= Notification.DEFAULT_VIBRATE;
//
//        notificationManager.notify(10, notification);

        //Move to relevant activity


        PowerManager powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wakeLock = powerManager.newWakeLock((PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "GCMOnMessage");

    }

}
