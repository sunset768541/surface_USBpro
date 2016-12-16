package com.example.datausb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by sunset on 15/11/12.
 */
public class BroadCr2 extends BroadcastReceiver {
    static final String ACTION2 = "android.hardware.usb.action.USB_DEVICE_DETACHED";
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(ACTION2)) {
            Intent serviceIntent2 = new Intent(context, Service2.class);
            context.startService(serviceIntent2);
        }
    }
}

