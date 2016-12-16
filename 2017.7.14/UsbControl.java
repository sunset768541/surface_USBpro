package com.example.datausb;

import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by sunset on 16/7/1.
 */
public class UsbControl{
    private UsbManager myUsbManager;
    private UsbDevice myUsbDevice;
    private UsbInterface myInterface;
    private UsbDeviceConnection myDeviceConnection;
    private UsbEndpoint epOut;
    private UsbEndpoint epIn;
    public static final int PID=241;
    public static final int VID=1204;
    private static UsbControl usbControl;
    private static Context mContext;
    private byte[] Buffer = new byte[512];//接收数据缓冲区,注意接收数据务必要为2的n次方，并且bulk的最大为16384，否侧会出现Fatal signal 11 (SIGSEGV)错误
    private  byte [] Receivebytes;
     private UsbControl(){}

    public static synchronized UsbControl create(Context context)
    {
        mContext=context;
       if (usbControl==null){
           usbControl=new UsbControl();
       }
        return usbControl;
    }
    public   boolean InitializeUsb(){
        myUsbManager = (UsbManager) mContext.getSystemService(mContext.USB_SERVICE);
        boolean isInitialization=true;
        try {
            EnumerateDevice();
            FindInterface();
            OpenDevice();
            AssignEndpoint();
        }
        catch (Exception e){
            Log.e("初始化USB："," -------Fail");
        }
        if (myUsbDevice==null){
             isInitialization=false;
        }
        return isInitialization;
    }
    /**
     * SendDataToUsb为一个用来发送数据的函数
     */
    public int SendDataToUsb(byte[] bytes) {//发送数据的函数
        myDeviceConnection.claimInterface(myInterface, true);
        int isSend = myDeviceConnection.bulkTransfer(epOut, bytes, bytes.length, 0); //do in another thread
        return isSend;//flag标记是否发送成功，成功flag>1,不成功-1
    }
    public byte[] ReceivceDataFromUsb(int receiveDataLength){
        if (Receivebytes==null){
           Receivebytes=new byte[receiveDataLength];
        }
        for (int i = 0; i < Receivebytes.length / Buffer.length; i++) {
            int xxx = myDeviceConnection.bulkTransfer(epIn,Buffer, Buffer.length, 0); //do in another thread
            for (int j = 0; j < 512; j++) {
                Receivebytes[j + i * 512] = Buffer[j];
            }

        }
        return Receivebytes;
    }

    /**
     * 分配端点，IN | OUT，即输入输出；此处我直接用1为OUT端点，0为IN，当然你也可以通过判断
     */
    //USB_ENDPOINT_XFER_BULK
     /*
     #define USB_ENDPOINT_XFER_CONTROL 0 --控制传输
     #define USB_ENDPOINT_XFER_ISOC 1 --等时传输
     #define USB_ENDPOINT_XFER_BULK 2 --块传输
     #define USB_ENDPOINT_XFER_INT 3 --中断传输
     * */
    private void AssignEndpoint() {
        if (myInterface != null) {             //这一句不加的话 很容易报错  导致很多人在各大论坛问:为什么报错呀
            epIn = myInterface.getEndpoint(1);//设置输入的数据的端点
            epOut = myInterface.getEndpoint(0);//设置输出数据的端点
            Log.d("指定USB端点 ","---------- Success");
              /* int num=myInterface.getEndpointCount();//可以检测当前接口上的端点数目
              deviceState.setText("当前接口的端点的数目"+num);//用文本显示端点的个数
              for (int i = 0; i < myInterface.getEndpointCount(); i++) {//该for循环用来找到指定的端点类型
              UsbEndpoint ep = myInterface.getEndpoint(i);//ep代表第i个端点
              if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
              //infdeviceState.setText("找到断点"+i);//查找usb设备某个类型的端点
              if (ep.getDirection() == UsbConstants.USB_DIR_IN) {
              deviceState.setText("该端点的类型端点号为："+i);//显示指定方向端点的标号
             } else {

             }
             }
             }*/
        }
        else Log.d("指定USB端点 ","---------- Fail");

    }

    /**
     * 打开设备
     */
    private void OpenDevice() {
        if (myInterface != null) {
            UsbDeviceConnection usbDeviceConnection = null;
            // 在open前判断是否有连接权限；对于连接权限可以静态分配，也可以动态分配权限，可以查阅相关资料
            if (myUsbManager.hasPermission(myUsbDevice)) {
                usbDeviceConnection = myUsbManager.openDevice(myUsbDevice);
                Log.d("打开USB设备 ","---------- Success");
            }
            if (usbDeviceConnection == null) {
                Log.d("打开USB设备 ","---------- Fail");
                return;
            }
            if (usbDeviceConnection.claimInterface(myInterface, true)) {
                myDeviceConnection = usbDeviceConnection;
            } else {
                Log.d("打开USB设备 ","---------- Fail");
                usbDeviceConnection.close();
            }
        }

    }

    /**
     * 找设备接口
     */
    private void FindInterface() {
        if (myUsbDevice != null) {
            //info1.setText(Integer.toString(myUsbDevice.getInterfaceCount()));//获得当前设备的接口数目
            myInterface = myUsbDevice.getInterface(0);
            Log.d("找到USB接口 ","---------- Success");
            /**for (int i = 0; i < myUsbDevice.getInterfaceCount(); i++) {
             UsbInterface intf = myUsbDevice.getInterface(i);
             // 根据手上的设备做一些判断，其实这些信息都可以在枚举到设备时打印出来
             if (intf.getInterfaceClass() == 8//找到指定类型的端点
             && intf.getInterfaceSubclass() == 6
             && intf.getInterfaceProtocol() == 80) {
             myInterface = intf;
             }
             break;
             }*/
        }
        else
        Log.d("寻找USB接口 ","---------- Fail");
    }

    /**
     * 枚举设备
     */
    private void EnumerateDevice() {
        if (myUsbManager == null){
            Log.d("枚举USB设备 ","---------- Fail");
            return;
        }
        HashMap<String, UsbDevice> deviceList = myUsbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();//历遍器
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
           // Log.d("vid",Integer.valueOf(device.getVendorId()).toString()+" pid "+Integer.valueOf(device.getProductId()).toString());
            if (device.getVendorId() == VID && device.getProductId() == PID) {//根据VID,PID枚举设备
                myUsbDevice = device;
                Log.d("枚举USB设备 ","---------- Success");
            }
        }
        if (myUsbDevice==null){
            Log.d("枚举USB设备 ","---------- 没有枚举到设备");
        }
    }
}
