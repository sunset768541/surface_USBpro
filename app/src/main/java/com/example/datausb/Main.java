package com.example.datausb;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.baidu.mapapi.SDKInitializer;
import com.example.datausb.DataUtil.DataBaseOperation;
import com.example.datausb.DataUtil.DataWR;
import com.example.datausb.Fiber.FiberManager;

import java.io.FileOutputStream;

/**
 * Created by sunset on 15/12/11.
 * 主Activity
 */

public class Main extends Activity {

    /**
     * 屏幕下方显示相关信息的部分
     */
    private TextView transmmitSpeedTextView;
    public  TextView loadmodestate;
    /**
     * 实例化3个用于显示不同界面的Fragment
     * da为数据显示界面
     * calibrateModel为数据标定界面
     * tempreatureModel为温度和距离的显示界面
     */
    FiberManager fiberManager=new FiberManager();
    DataModel dataModel = new DataModel();
    CalibrateModel calibrateModel = new CalibrateModel();
    TempreatureModel tempreatureModel = new TempreatureModel();
    SystemSetting systemSetting = new SystemSetting();
    ThreeDimensionModel threeDimensionModel = new ThreeDimensionModel();
    HistoryRecord historyRecord = new HistoryRecord();
    MapModel mapModel=new MapModel();
    ToggleButton tunnelA;
    ToggleButton tunnelB;
    ToggleButton tunnelC;
    ToggleButton tunnelD;

    boolean threedimhide=false;
    /**
     * 定义静态变量TOGGLE_BUTTON，用来标识togglebutton是否是第一次按下，0表示没有按过togglebutton，当按下togglebutton后该值永远为1.
     * 在切换显示模式时，由于会使数据接收线程和处理线程自动启动，那打开关闭设备按键的状态也要切换到正确的状态。
     */
    public static int TOGGLE_BUTTON = 0;
    public static int STOREDATA = 0;//数据存储标志
    public static int TEM_ALERT = 0;//温度报警标志

    /**
     * preferences为读取参数的SharePreference的实例
     * editor为修改参数的Shareferecxes.Editor的实例
     */
    public SharedPreferences systemSettingSharePre;
    public SharedPreferences.Editor systemSettingSharePreEdi;
    int fiberLength;
    int fragmentnumber;
    ToggleButton startOrStopToggleButton;
    private  boolean UsbIsInitialize=false;
    private  UsbControl usbControl;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.mainactivity);
        usbControl=UsbControl.create(this);
        UsbIsInitialize=usbControl.initializeUsb();
        DataBaseOperation.getDataBase(this);

        /**
         * 实例化6个按钮用来切换不同的Fragment
         * datamodle用来切换到数据显示模式
         * calibration用于切换到标定模式
         * temperature用来切换到温度距离显示模式
         * 并为按钮添加监听函数
         */
        loadmodestate=(TextView)findViewById(R.id.loadmoedlstate);
        ImageButton changeToDataModelButton = (ImageButton) findViewById(R.id.imageButton);

        changeToDataModelButton.setOnClickListener(new ChangeTDML());
        ImageButton changeToCalibrateModelButton = (ImageButton) findViewById(R.id.imageButton2);
        changeToCalibrateModelButton.setOnClickListener(new ChangeTCla());
        ImageButton changeToTemperatureModelButton = (ImageButton) findViewById(R.id.imageButton3);

        changeToTemperatureModelButton.setOnClickListener(new ChangeTem());

        ImageButton systemSetingButton = (ImageButton) findViewById(R.id.imageButton5);
        systemSetingButton.setOnClickListener(new SystemSettingListener());
        ImageButton changeToThreeDimensionModelButton = (ImageButton) findViewById(R.id.imageButton4);
        changeToThreeDimensionModelButton.setOnClickListener(new ChangeTThdim());
        ImageButton historyRecordingButton = (ImageButton) findViewById(R.id.imageButton6);
        historyRecordingButton.setOnClickListener(new HistoryRecordListener());
        ImageButton mapmodel =(ImageButton)findViewById(R.id.mapmodel);
        mapmodel.setOnClickListener(new MapModelListener());
        /**
         *
         */
        startOrStopToggleButton = (ToggleButton) findViewById(R.id.toggleButton);
        startOrStopToggleButton.setOnClickListener(new StartOrStopListener(startOrStopToggleButton));
        //fiberLengthEdiText = (EditText) findViewById(R.id.editText5);
        transmmitSpeedTextView = (TextView) findViewById(R.id.transmitespeed1);
        systemSettingSharePre = getSharedPreferences("systemSetting", MODE_PRIVATE);//systemSetting为用来保存参数的文件，当这个文件不存下时，通过获取edit就会创建一个名字为systemSetting的shareP
        systemSettingSharePreEdi = systemSettingSharePre.edit();
        iniSystemSetting();
        tunnelA=(ToggleButton)findViewById(R.id.fiberA);
        tunnelB=(ToggleButton)findViewById(R.id.fiberB);
        tunnelC=(ToggleButton)findViewById(R.id.fiberC);
        tunnelD=(ToggleButton)findViewById(R.id.fiberD);
        fiberManager.setContext(getApplicationContext());
        historyRecord.setFiberManager(fiberManager);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveSystemSetting();
    }

    public void setTunnelAOn(){
        tunnelA.setChecked(true);
    }
    public void setTunnelAOff(){
        tunnelA.setChecked(false);
    }
    public void setTunnelBOn(){
        tunnelB.setChecked(true);
    }
    public void setTunnelBOff(){
        tunnelB.setChecked(false);
    }
    public void setTunnelCOn(){
        tunnelC.setChecked(true);
    }
    public void setTunnelCOff(){
        tunnelC.setChecked(false);
    }
    public void setTunnelDOn(){
        tunnelD.setChecked(true);
    }
    public void setTunnelDOff(){
        tunnelD.setChecked(false);
    }


    /**
     * 更新UI数据
     */
    private final int COMPLETED = 0;
    public   Handler handler = new Handler() {//多线程中用于UI的更新
        @Override
        public void handleMessage(Message msg1) {
            if (msg1.what == COMPLETED) {
                try {
                    transmmitSpeedTextView.setText("采样数据：" + msg1.obj);
                } catch (NullPointerException e) {
                    Log.e("Main",Log.getStackTraceString(e));
                }
            }
            if (msg1.arg1==1){
                loadmodestate.setText("");
            }
        }
    };


    boolean isByteDataProcessComplete = false;

    public boolean getByteDataProcessComlete() {
        return isByteDataProcessComplete;
    }

    public void setByteDataProcessComplete(boolean isComplete) {
        isByteDataProcessComplete = isComplete;
    }

    /****************************************************************************************
     * 在fragment中调用已换唤醒main中的线程
     ***************************************************************************************/
    public void wakeUpAllMainThread() {
        dataObj.notifyAll();
    }
    /*****************************************************************************************
     * 在fragment中通过判断stopdatamodelthread来确定是否终止线程
     * setStopDataModelThread（）方法用来在模式切换按钮按下时改变stopdatamodelthread状态使得该线程终止
     *****************************************************************************************/
    boolean stopDataModelThread = false;
    public boolean setStopDataModelThread(boolean isStop) {
        stopDataModelThread = isStop;
        return isStop;
    }

    boolean stopCalibrateModelThread = false;

    public boolean setStopCalibrateModelThread(boolean isStop) {
        stopCalibrateModelThread = isStop;
        return isStop;
    }

    boolean stopTemperatureModelThread = false;

    public boolean setStopTemperatureModelThread(boolean isStop) {
        stopTemperatureModelThread = isStop;
        return isStop;
    }

    boolean stopThreeDimenModelThread = false;

    public boolean setStopThreeDimenModelThread(boolean isStop) {
        stopThreeDimenModelThread = isStop;
        return isStop;
    }
    boolean stopMapModelThread=false;
    public boolean setMapModelThread(boolean isStop){
        stopMapModelThread=isStop;
        return stopMapModelThread;
    }
    /** *****************************************************************************************
     * 此处下面是各个控制按键的监听函数
     * *****************************************************************************************/
    /**
     * 按钮datamodle的监听函数
     * 每个FragmentTransanction的commit只能提交一次，所以我们在按下每个按钮时从新实例化一个FragmentTransanction进行提交操作
     *         systemSettingSharePreEdi = systemSettingSharePre.edit();
     if (currentFragment != targetFragment) {
     if (!targetFragment.isAdded()) {
     ft.hide(currentFragment).add(
     id.my_frame, targetFragment);
     } else {
     ft.hide(currentFragment).show(targetFragment);
     }
     } else {
     if (!targetFragment.isAdded()) {
     ft.add(id.my_frame, targetFragment)
     .show(targetFragment);
     }
     }
     */

    class ChangeTDML extends ButtonClickListener{
        public   void preThreadProcess(FragmentTransaction transaction){
            setStopCalibrateModelThread(true);
            setStopTemperatureModelThread(true);
            setMapModelThread(true);
            setStopThreeDimenModelThread(true);
        }
        public   void fragmentTransaction(FragmentTransaction transaction){
            transaction.replace(R.id.contineer, dataModel, "datamodel");//datamodel为fragment的tag值，用来在数据处理线程中找到当前的fragment
        }
        public   void afterThreadProcess(){
            setStopDataModelThread(false);
        }
        public   void setFragmentNumber(){
            fragmentnumber = 0;
        }
    }
    class ChangeTCla extends ButtonClickListener{
        public   void preThreadProcess(FragmentTransaction transaction){
            setStopDataModelThread(true);
            setMapModelThread(true);
            setStopTemperatureModelThread(true);
            setStopThreeDimenModelThread(true);
        }
        public   void fragmentTransaction(FragmentTransaction transaction){
            transaction.replace(R.id.contineer, calibrateModel, "calibratemodel");
        }
        public   void afterThreadProcess(){
            setStopCalibrateModelThread(false);
        }
        public   void setFragmentNumber(){
            fragmentnumber = 1;
        }
    }
    class ChangeTem extends ButtonClickListener{
        public   void preThreadProcess(FragmentTransaction transaction){
            setStopCalibrateModelThread(true);
            setMapModelThread(true);
            setStopDataModelThread(true);
            setStopThreeDimenModelThread(true);
        }
        public   void fragmentTransaction(FragmentTransaction transaction){
            transaction.replace(R.id.contineer, tempreatureModel, "tempreturemodel");

        }
        public   void afterThreadProcess(){
            setStopTemperatureModelThread(false);
        }
        public   void setFragmentNumber(){
            fragmentnumber = 2;
        }
    }
    class ChangeTThdim extends ButtonClickListener{
        public   void preThreadProcess(FragmentTransaction fragmentTransaction){
            setStopCalibrateModelThread(true);
            setMapModelThread(true);
            setStopDataModelThread(true);
            setStopTemperatureModelThread(true);
        }
        public   void fragmentTransaction(FragmentTransaction transaction){

                 transaction.replace(R.id.contineer, threeDimensionModel, "threedimmodel");//利用replace显示fragement每次都会重新调用fragment的各个生命周期，降低效率
        //    }
        }
        public   void afterThreadProcess(){
            setStopThreeDimenModelThread(false);
        }
        public   void setFragmentNumber(){
            fragmentnumber =4;
        }
    }

    /**
     * 系统设置按键的监听类
     */
    class SystemSettingListener implements View.OnClickListener {
        public void onClick(View v) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.contineer, systemSetting, "systemseting");
            transaction.commit();
        }


    }

    /**
     * 历史记录按键的监听类
     */
    class HistoryRecordListener implements View.OnClickListener {
        public void onClick(View v) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.contineer, historyRecord, "historyrecording");
            transaction.commit();
        }
    }
    /**
     * MapModel按键监听类
     */
    class MapModelListener extends ButtonClickListener{
        public   void preThreadProcess(FragmentTransaction fragmentTransaction){
            setStopCalibrateModelThread(true);
            setStopThreeDimenModelThread(true);
            setStopDataModelThread(true);
            setStopTemperatureModelThread(true);
        }
        public   void fragmentTransaction(FragmentTransaction transaction){

            transaction.replace(R.id.contineer, mapModel, "mapmodel");//利用replace显示fragement每次都会重新调用fragment的各个生命周期，降低效率

        }
        public   void afterThreadProcess(){
            setMapModelThread(false);
        }
        public   void setFragmentNumber(){
            fragmentnumber =5;
        }
    }
    /**
     * 打开设备的togglebutton监听函数
     */
    class StartOrStopListener implements View.OnClickListener {
        ToggleButton tgg;
        public StartOrStopListener(ToggleButton tgg) {
            this.tgg = tgg;

        }

        public void onClick(View v) {
            TOGGLE_BUTTON = 1;
            if (tgg.isChecked()) {
                 if (!UsbIsInitialize)
                 UsbIsInitialize=usbControl.initializeUsb();
                 if (UsbIsInitialize) {
                    if (dataReceiveThread.isAlive()) {
                        dataReceiveThread.setSuspend(false);
                        dataProcessThread.setSuspend(false);
                    } else {
                        dataReceiveThread.start();
                        dataProcessThread.start();//先不进行数据的处理
                    }
                } else {

                    Toast.makeText(getApplicationContext(), "无法打开设备，USB设备非数据采集卡，请确认后重试",
                            Toast.LENGTH_SHORT).show();
                }

            }
            // 当按钮再次被点击时候响应的事件
            else {
                dataReceiveThread.setSuspend(true);
                dataProcessThread.setSuspend(true);
            }


        }
    }

    /**
     * 设置光纤长度参数的方法
     */
    public boolean setFiberLength(int opl) {
        systemSettingSharePreEdi.putInt("fiberlength", opl);
        systemSettingSharePreEdi.commit();
        return true;
    }
    public void iniSystemSetting(){
        if(systemSettingSharePre.getBoolean("hasSave",true)){
            systemSettingSharePreEdi.putBoolean("hasSave",false);
            systemSettingSharePreEdi.putInt("TEM_ALERT_FIBERA",60);
            systemSettingSharePreEdi.putInt("TEM_ALERT_FIBERB",60);
            systemSettingSharePreEdi.putInt("TEM_ALERT_FIBERC",60);
            systemSettingSharePreEdi.putInt("TEM_ALERT_FIBERD",60);
            systemSettingSharePreEdi.putFloat("centerLongitude",121.89835090371952f);
            systemSettingSharePreEdi.putFloat("centerLatitude",38.978000183472237f);
            systemSettingSharePreEdi.putString("axipath",null);
            systemSettingSharePreEdi.putString("envPath",null);
            systemSettingSharePreEdi.putString("fiberPath",null);
            systemSettingSharePreEdi.putString("preenvPath",null);
            systemSettingSharePreEdi.putString("prefiberPath",null);
            systemSettingSharePreEdi.putInt("average",128);
            systemSettingSharePreEdi.putInt("fiberL",256);
            systemSettingSharePreEdi.putInt("dev",8);
            systemSettingSharePreEdi.putInt("pla",3);
            systemSettingSharePreEdi.commit();
        }
        else {
            SystemParameter.TEM_ALERT_FIBERA=systemSettingSharePre.getInt("TEM_ALERT_FIBERA",60);
            SystemParameter.TEM_ALERT_FIBERB=systemSettingSharePre.getInt("TEM_ALERT_FIBERB",60);
            SystemParameter.TEM_ALERT_FIBERC=systemSettingSharePre.getInt("TEM_ALERT_FIBERC",60);
            SystemParameter.TEM_ALERT_FIBERD=systemSettingSharePre.getInt("TEM_ALERT_FIBERD",60);
            SystemParameter.centerLatitude=systemSettingSharePre.getFloat("centerLatitude",38.978000183472237f);
            SystemParameter.centerLongitude=systemSettingSharePre.getFloat("centerLongitude",121.89835090371952f);
            SystemParameter.axipath=systemSettingSharePre.getString("axipath",null);
            SystemParameter.envPath=systemSettingSharePre.getString("envPath",null);
            SystemParameter.fiberPath=systemSettingSharePre.getString("fiberPath",null);
            SystemParameter.preenvPath=systemSettingSharePre.getString("preenvPath",null);
            SystemParameter.prefiberPath=systemSettingSharePre.getString("prefiberPath",null);
            SystemParameter.average=(char)systemSettingSharePre.getInt("average",128);
            SystemParameter.fiberL=(char)systemSettingSharePre.getInt("fiberL",256);
            SystemParameter.dev=(byte)systemSettingSharePre.getInt("dev",8);
            SystemParameter.pla=(byte)systemSettingSharePre.getInt("pla",3);
        }
    }
    public void saveSystemSetting(){
        systemSettingSharePreEdi.putBoolean("hasSave",false);
        systemSettingSharePreEdi.putInt("TEM_ALERT_FIBERA",SystemParameter.TEM_ALERT_FIBERA);
        systemSettingSharePreEdi.putInt("TEM_ALERT_FIBERB",SystemParameter.TEM_ALERT_FIBERB);
        systemSettingSharePreEdi.putInt("TEM_ALERT_FIBERC",SystemParameter.TEM_ALERT_FIBERC);
        systemSettingSharePreEdi.putInt("TEM_ALERT_FIBERD",SystemParameter.TEM_ALERT_FIBERD);
        systemSettingSharePreEdi.putFloat("centerLongitude",SystemParameter.centerLongitude);
        systemSettingSharePreEdi.putFloat("centerLatitude",SystemParameter.centerLatitude);
        systemSettingSharePreEdi.putString("axipath",SystemParameter.axipath);
        systemSettingSharePreEdi.putString("envPath",SystemParameter.envPath);
        systemSettingSharePreEdi.putString("fiberPath",SystemParameter.fiberPath);
        systemSettingSharePreEdi.putString("preenvPath",SystemParameter.preenvPath);
        systemSettingSharePreEdi.putString("prefiberPath",SystemParameter.prefiberPath);
        systemSettingSharePreEdi.putInt("average",SystemParameter.average);
        systemSettingSharePreEdi.putInt("fiberL",SystemParameter.fiberL);
        systemSettingSharePreEdi.putInt("dev",SystemParameter.dev);
        systemSettingSharePreEdi.putInt("pla",SystemParameter.pla);
        systemSettingSharePreEdi.commit();
    }

    /*******************************************************************************************
     * 数据接收线程，负责从usb的endpoint读取数据
     ******************************************************************************************/
    public class DataReceiveThread extends Thread {//接收数据的线程
        private boolean suspend = false;
        final Resource r;
        DataReceiveThread(Resource r) {
            this.r = r;
        }
        private final String control = ""; // 只是需要一个对象而已，这个对象没有实际意义
         void setSuspend(boolean suspend) {
            if (!suspend) {
                synchronized (control) {
                    control.notifyAll();
                }
            }
            this.suspend = suspend;
        }
        public boolean isSuspend() {
            return this.suspend;
        }
        public void run() {
            while (true) {
                synchronized (control) {
                    if (suspend) {
                        try {
                            control.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                synchronized (r) {
                    if (r.flag) {
                        try {
                            r.wait();
                        } catch (InterruptedException ex) {
                            Log.e("Main",Log.getStackTraceString(ex));
                        }
                    }
                    byte[] Receivebytes = usbControl.receiveDataFromUsb(4*fiberManager.getFibeNumber()*FiberManager.fiberLength);
                    if (STOREDATA == 1) {
                        try {
                            DataWR.saveData(Receivebytes,fiberManager);//将接收到的数据直接存储为2进制文件
                        } catch (Exception e) {
                            Log.e("Main",Log.getStackTraceString(e));
                        }
                    }
                    r.data = Receivebytes;//
                    r.flag = true;
                    r.notify();
                }
        }

        }
    }

    /**
     * ****************************************************************************************
     * dataProcess线程是将从USB接收的8bit数据合成16bit信息并显示,//分类数据传输到各自的通道
     *****************************************************************************************/
    public class DataProcess extends Thread {//接收数据的线程
        final Resource r;
        public final Data dd;

        DataProcess(Resource r, Data dd) {
            this.dd = dd;
            this.r = r;
        }

        private boolean suspend = false;

        private final String control = ""; // 只是需要一个对象而已，这个对象没有实际意义

        public void setSuspend(boolean suspend) {
            if (!suspend) {
                synchronized (control) {
                    control.notifyAll();
                }
            }
            this.suspend = suspend;
        }

        public boolean isSuspend() {
            return this.suspend;
        }

        public void run() {

            while (true) {

                synchronized (control) {
                    if (suspend) {
                        try {
                            control.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                synchronized (r) {
                    if (!r.flag)
                        try {
                            r.wait();
                        } catch (InterruptedException ex) {
                            Log.e("Main",Log.getStackTraceString(ex));
                        }

                    int p;
                    int p1;
                    int i1 = 0;
                    int[] combination = new int[r.data.length / 2];//combination用于储存合并后的16bit数据
                    for (int i = 0; i < r.data.length; i = i + 2) {//数据组合
                        if (r.data[i] < 0) {
                            p = 256 + r.data[i];
                        } else {
                            p = r.data[i];
                        }
                        if (r.data[i + 1] < 0) {
                            p1 = (256 + r.data[i + 1]) << 8;
                        } else {
                            p1 = r.data[i + 1] << 8;
                        }
                        combination[i1] = p + p1;
                        i1 = i1 + 1;
                    }
                   fiberManager.decodeData(combination);//每次整合完都把数据送到FiberManager中进行解析，将数据分配到特定的光纤中
                    if (TEM_ALERT == 1) {//当条件成立的时候执行温度报警功能
                        TempreatureAlarm.fiberManager=fiberManager;
                        if (TempreatureAlarm.alertFinish == 1) {
                            TempreatureAlarm.alertFinish = 0;
                            TempreatureAlarm.getTempAlert();
                        }
                    }
                    /***
                     * 这个线程同步的作用是发送处理好的数据交给fragment线程进行处理，在fragment显示线程进行数据显示的过程中，要求当前数据处理线程进行等待
                     */
                    synchronized (dd) {
                        try {
                            dd.flag1 = true;//与fragment中的绘图线程进行生产者与消费者
                            switch (fragmentnumber) {
                                case 0:
                                    DataModel frgment1 = (DataModel) getFragmentManager().findFragmentByTag("datamodel");//获取当前的fragment
                                    frgment1.wakeup();//调用fragment中的唤醒方法
                                    break;
                                case 1:
                                    CalibrateModel frgment2 = (CalibrateModel) getFragmentManager().findFragmentByTag("calibratemodel");//获取当前的fragment
                                    frgment2.wakeUp();//调用fragment中的唤醒方法
                                    break;
                                case 2:
                                    TempreatureModel fragment3 = (TempreatureModel) getFragmentManager().findFragmentByTag("tempreturemodel");
                                    fragment3.wakeup();//调用fragment中的唤醒方法
                                    break;
                                case 4:
                                    ThreeDimensionModel fragment4 = (ThreeDimensionModel) getFragmentManager().findFragmentByTag("threedimmodel");
                                    fragment4.wakeup();//调用fragment中的唤醒方法
                                    break;
                            }
                            /**
                             * 先实现数据处理线程的等待，直到该fragment中显示线程显示数据完成后由显示线程对当前的数据处理线程进行唤醒继续运行
                             */
                            if (dd.flag1) {
                                try {
                                    dd.wait();
                                } catch (InterruptedException ex) {
                                    Log.e("Main",Log.getStackTraceString(ex));
                                }
                            } else {
                                dd.notifyAll();
                            }
                        } catch (NullPointerException ee) {
                            Log.e("Main",Log.getStackTraceString(ee));
                        }
                    }
                    Message msg1 = new Message();
                    msg1.what = COMPLETED;
                    if (combination.length>2)
                        msg1.obj = combination[2] + "+" + combination[combination.length-1];
                    msg1.arg1 = r.speed;//数据的传输速度
                    handler.sendMessage(msg1);
                    r.flag = false;
                    r.notify();
                    setByteDataProcessComplete(true);
                    // Log.d("数据处理", "数据处理线程完成");
                }
            }


        }
    }



    /**
     * 数据的存储，储存的路径为/mnt/external_sd，文件名为data.txt
     */
    private void write(byte[] content) {//注意储存字符串传入的参数为String content，储存二进制传入的参数为byte[] content
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                FileOutputStream raf = new FileOutputStream("/mnt/external_sd/dataObj.txt", true);//储存二进制文件
                Log.d("chu", "sunce");
                // FileWriter raf=new FileWriter("/mnt/external_sd/dataObj.txt",true);//储存为字符串
                raf.write(content);
                raf.close();
            } else {
                Toast.makeText(getApplication(), "没有检测到SD卡", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    /**
     * 这个resource类的作用是提供一个共享的数据储存区域，封装数据用于储存
     */
    class Resource {
        byte[] data;
        int speed;
        boolean flag = false;
    }

    class Data {
        public boolean flag1 = false;
    }

    /**
     * 该语句为实例化一个数据接收的类，re为数据接收线程
     */
    Resource resourceObj = new Resource();
    public Data dataObj = new Data();
    public DataReceiveThread dataReceiveThread = new DataReceiveThread(resourceObj);
    public DataProcess dataProcessThread = new DataProcess(resourceObj, dataObj);
    public void UsbSendData(byte[] bytes){
        int i=usbControl.sendDataToUsb(bytes);
        if (i<0)
            Toast.makeText(getApplication(), "发送指令失败，USB连接错误", Toast.LENGTH_SHORT).show();
    }

   abstract class ButtonClickListener implements View.OnClickListener{//模板设计模式
        public void onClick(View v) {
            if (TOGGLE_BUTTON == 1) {
                startOrStopToggleButton.setChecked(true);
            }
            fiberLength = systemSettingSharePre.getInt("fiberlength", 0);
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                dataReceiveThread.setSuspend(true);
                dataProcessThread.setSuspend(true);
                preThreadProcess(transaction);
                fragmentTransaction(transaction);
                afterThreadProcess();
                transaction.commit();
                setFragmentNumber();
                dataReceiveThread.setSuspend(false);
                dataProcessThread.setSuspend(false);
        }
        abstract  void preThreadProcess(FragmentTransaction transaction);
        abstract  void fragmentTransaction(FragmentTransaction transaction);
        abstract  void afterThreadProcess();
        abstract  void setFragmentNumber();
   }


}
