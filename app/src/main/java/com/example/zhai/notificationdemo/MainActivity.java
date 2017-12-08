package com.example.zhai.notificationdemo;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.View;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MainActivity extends FragmentActivity {

    private static final String TAG = "MainActivity";
    private int type = 100;
    private static final String CHECK_OP_NO_THROW = "checkOpNoThrow";
    private static final String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";
    private boolean isNotificationStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void sendMessage(View view) {
        checkNotificationStatus();
        startNotification();
    }

    private void startNotification() {

        // 点击后响应的Intent
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("com.zhai.notification");
        broadcastIntent.putExtra("type", type);
        PendingIntent pendingIntent = PendingIntent.
                getBroadcast(this, 0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // 滑动删除通知时响应的Intent
        Intent deleteIntent = new Intent(this, MyService.class);
        int requestCode = (int) SystemClock.currentThreadTimeMillis();
        deleteIntent.putExtra("type", type);
        PendingIntent detelePendingIntent = PendingIntent.getService(this, requestCode, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // 注册notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle("ForexRoo")
                .setTicker("有新的交易")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setDeleteIntent(detelePendingIntent)
                .setAutoCancel(true)
//                .setOngoing(true)
                .setDefaults(Notification.DEFAULT_ALL);

        // 启动notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(type, builder.build());
    }

    private void checkNotificationStatus() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            isNotificationStatus = getNotificationStatus(this);
            // 跳转到设置界面
            if (!isNotificationStatus) {
                Log.d(TAG, "跳转到设置界面");
                //通知栏的权限让用户拒绝，只能调到设置页面让用户手动开启
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Intent intent = new Intent();
                    intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                    intent.putExtra("app_package", this.getPackageName());
                    intent.putExtra("app_uid", this.getApplicationInfo().uid);
                    startActivity(intent);
                } else if (android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.setData(Uri.parse("package:" + this.getPackageName()));
                    startActivity(intent);
                }
            }
        }
        Log.d(TAG, "isNotificationStatus="+isNotificationStatus);
    }

    //使用反射获取到通知栏的通知权限是否打开，但改方法只限api19以上，因为AppOpsManager类api19以上才有的
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private boolean getNotificationStatus(Context context) {
        AppOpsManager mAppOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);

        ApplicationInfo appInfo = context.getApplicationInfo();

        String pkg = context.getApplicationContext().getPackageName();

        int uid = appInfo.uid;

        Class appOpsClass = null; /* Context.APP_OPS_MANAGER */

        try {
            appOpsClass = Class.forName(AppOpsManager.class.getName());

            Method checkOpNoThrowMethod = appOpsClass.getMethod(CHECK_OP_NO_THROW, Integer.TYPE, Integer.TYPE, String.class);

            Field opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION);
            int value = (int) opPostNotificationValue.get(Integer.class);

            boolean boo = ((int) checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg) == AppOpsManager.MODE_ALLOWED);
            return boo;

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 该方法试用compileSdkVersion=26，以下可能会有问题
    private boolean getNotificationStatus2() {
        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        return manager.areNotificationsEnabled();
    }
}
