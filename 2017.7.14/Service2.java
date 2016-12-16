package com.example.datausb;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by sunset on 15/11/12.
 */
public class Service2 extends Service {
    public NotificationManager nm;
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        send();
    }
    public void send(){

        Notification noti=new Notification.Builder(this)
                .setAutoCancel(true)
                .setTicker("USB有消息")
                .setSmallIcon(R.drawable.ic)
                .setContentTitle("USB移除")
                .setContentText("USB外设已成功移除")
                .getNotification();
        nm.notify(0, noti);
        nm.cancel(1);
    }
}