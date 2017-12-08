package com.example.zhai.notificationdemo;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Mastra on 2017/11/27.
 */

public class DeleteNotifyBrocast extends BroadcastReceiver {

    private static final String TAG = "DeleteNotifyBrocast";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG ,"onReceive");
        int type = intent.getIntExtra("type", -1);
        if (type == -1) {
            Log.d(TAG ,"cancel");
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(type);
        }
    }
}
