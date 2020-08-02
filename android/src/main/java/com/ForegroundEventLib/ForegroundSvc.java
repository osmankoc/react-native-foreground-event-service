package com.ForegroundEventLib;


import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.ForegroundEventLib.R;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;


//import android.support.v4.app.NotificationCompat;

public class ForegroundSvc extends Service {

    public final static String TAG = "ForegroundSvc";
    private static boolean ALARM_SET = false;
    public static int SVC_INTERVAL = 60000;

    private static AlarmManager mAlarmManager;
    private static PendingIntent mBackgroundServicePendingIntent;

    public static final String CHANNEL_ID = "ForegroundServiceEventChannel";
    @Override
    public void onCreate() {
        super.onCreate();
        if(mAlarmManager == null)
            mAlarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
    }
    @Override

    public int onStartCommand(Intent intent, int flags, int startId) {
        String title = intent.getStringExtra("title");
        String input = intent.getStringExtra("sBody");
        SVC_INTERVAL = intent.getIntExtra("interval", 60000);
        int icon = intent.getIntExtra("icon", 0);

        createNotificationChannel();
        Notification notification = createNotification(title, input, icon);
        startForeground(2, notification);
        //do heavy work on a background thread
        createLocationPendingIntent();
        if(!ALARM_SET) {
            ALARM_SET = true;

            mAlarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis(),
                    SVC_INTERVAL,
                    mBackgroundServicePendingIntent
            );
        }

        return START_STICKY;
    }


    private void createLocationPendingIntent() {
        if(mBackgroundServicePendingIntent != null)
            return;
        Intent i = new Intent(getApplicationContext(), BackgroundWorker.class);
        mBackgroundServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, i, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Event Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }


    private Notification createNotification(String title, String body, int icon) {

        Intent notificationIntent = new Intent(this, getMainActivityClass(getApplicationContext()));
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(icon)
                .setContentIntent(pendingIntent);

        return notificationBuilder.build();
    }


    private Class getMainActivityClass(Context context) {
        String packageName = context.getPackageName();
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent == null || launchIntent.getComponent() == null) {
            Log.e(TAG, "Failed to get launch intent or component");
            return null;
        }
        try {
            return Class.forName(launchIntent.getComponent().getClassName());
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Failed to get main activity class");
            return null;
        }
    }

}
