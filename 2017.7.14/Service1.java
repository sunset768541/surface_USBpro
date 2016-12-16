package com.example.datausb;

import android.app.Notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.IBinder;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by sunset on 15/11/12.
 */
public class Service1 extends Service {
    public NotificationManager nm;
    //  public  NotificationManager nm2;
    //public Notification;
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        PendingIntent pendingIntent;
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        String ss = "检测到设备接入";
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            if (device.getVendorId() == 1204 && device.getProductId() == 241) {
                ss = ss + "FPGA开发板接入系统成功";
                Intent intent1 = new Intent(this, Main.class);
                pendingIntent = PendingIntent.getActivity(this, 0, intent1, 0);

            } else {
                Intent intent1 = new Intent();
                pendingIntent = PendingIntent.getActivity(this, 0, intent1, 0);
            }

            send(ss, pendingIntent);
        }
    }
    public void send(String s1,PendingIntent pp) {
        Notification noti = new Notification.Builder(this).setContentIntent(pp)
                .setAutoCancel(true)
                .setTicker("USB有消息")
                .setSmallIcon(R.drawable.ic)
                .setContentTitle("USB接入")
                .setContentText(s1)
                .getNotification()
                ;
        nm.notify(1, noti);
        nm.cancel(0);

    }
}


