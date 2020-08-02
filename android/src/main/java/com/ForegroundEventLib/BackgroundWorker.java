package com.ForegroundEventLib;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.Nullable;

public class BackgroundWorker extends IntentService {

    public BackgroundWorker() {
        super(BackgroundWorker.class.getName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Intent eventIntent = new Intent(RNForegroundEventServiceModule.nativeEventName);
        getApplicationContext().sendBroadcast(eventIntent);
    }
}
