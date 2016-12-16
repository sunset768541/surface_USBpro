package com.example.datausb;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.support.v7.app.NotificationCompat;

/**
 * Created by sunset on 15/11/12.
 */
public class BroadCr1 extends BroadcastReceiver {
    static final String ACTION = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    static final String ACTION2 = "android.hardware.usb.action.USB_DEVICE_DETACHED";
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(ACTION)) {
            Intent serviceIntent = new Intent(context, Service1.class);
            // 启动Service1
            context.startService(serviceIntent);


        }
    }

}