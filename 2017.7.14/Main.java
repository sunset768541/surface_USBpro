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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.FileOutputStream;
import java.util.Arrays;

/**
 * Created by sunset on 15/12/11.
 */

public class Main extends Activity {
    /**
     * 屏幕下方显示相关信息的部分
     */
    private TextView usbStateTextView;
    private TextView dataSaveTextView;
    private TextView transmmitSpeedTextView;
    /**
     * 实例化3个用于显示不同界面的Fragment
     * da为数据显示界面
     * calibrateModel为数据标定界面
     * tempreatureModel为温度和距离的显示界面
     */
    DataModel dataModel = new DataModel();
    CalibrateModel calibrateModel = new CalibrateModel();
    TempreatureModel tempreatureModel = new TempreatureModel();
    SystemSetting systemSetting = new SystemSetting();
    ThreeDimensionModel threeDimensionModel = new ThreeDimensionModel();
    HistoryRecord historyRecord = new HistoryRecord();

    /**
     * 定义用于携带数据的Bundle
     */
    Bundle data_a = new Bundle();//携带通道A的数据Bundle
    Bundle data_a1 = new Bundle();//携带通道A1的数据的Bundle
    Bundle data_b = new Bundle();//携带通道B的数据Bundle
    Bundle data_b1 = new Bundle();//携带通道B1的数据Bundl
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
     *
     * @param savedInstanceState
     */
    SharedPreferences fiberLengthSharePre;
    SharedPreferences.Editor fiberLengthSharePreEdi;
    int fiberLength;
    int fragmentnumber;
    private EditText fiberLengthEdiText;
    ToggleButton startOrStopToggleButton;
    private  boolean UsbIsInitialize=false;
    private  UsbControl usbControl;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.mainactivity);
        usbControl=UsbControl.create(this);
        UsbIsInitialize=usbControl.InitializeUsb();
        DataBaseOperation.getDataBase(this);
        /**
         * 实例化6个按钮用来切换不同的Fragment
         * datamodle用来切换到数据显示模式
         * calibration用于切换到标定模式
         * temperature用来切换到温度距离显示模式
         * 并为按钮添加监听函数
         */
        ImageButton changeToDataModelButton = (ImageButton) findViewById(R.id.imageButton);

       // changeToDataModelButton.setOnClickListener(new ChangeToDataModelListener());
        changeToDataModelButton.setOnClickListener(new ChnageTDML());
        ImageButton changeToCalibrateModelButton = (ImageButton) findViewById(R.id.imageButton2);
       // changeToCalibrateModelButton.setOnClickListener(new ChangeToCalibrationModelListener());
        changeToCalibrateModelButton.setOnClickListener(new ChangeTCla());
        ImageButton changeToTemperatureModelButton = (ImageButton) findViewById(R.id.imageButton3);

        changeToTemperatureModelButton.setOnClickListener(new ChangeTem());
        //changeToTemperatureModelButton.setOnClickListener(new ChangeToTemperatureModelListener());

        ImageButton systemSetingButton = (ImageButton) findViewById(R.id.imageButton5);
        systemSetingButton.setOnClickListener(new SystemSettingListener());
        ImageButton changeToThreeDimensionModelButton = (ImageButton) findViewById(R.id.imageButton4);
       // changeToThreeDimensionModelButton.setOnClickListener(new ChangetoThreeDimensionModelListener());
        changeToThreeDimensionModelButton.setOnClickListener(new ChangeTThdim());
        ImageButton historyRecordingButton = (ImageButton) findViewById(R.id.imageButton6);
        historyRecordingButton.setOnClickListener(new HistoryRecordListener());
        /**
         *
         */
        startOrStopToggleButton = (ToggleButton) findViewById(R.id.toggleButton);
        startOrStopToggleButton.setOnClickListener(new StartOrStopListener(startOrStopToggleButton));
        usbStateTextView = (TextView) findViewById(R.id.usbstate);
        dataSaveTextView = (TextView) findViewById(R.id.savestate);
        fiberLengthEdiText = (EditText) findViewById(R.id.editText5);
        transmmitSpeedTextView = (TextView) findViewById(R.id.transmitespeed1);
        fiberLengthSharePre = getSharedPreferences("opl", MODE_PRIVATE);
        fiberLengthSharePreEdi = fiberLengthSharePre.edit();
        int oplong = fiberLengthSharePre.getInt("fiberlength", 0);
        if (oplong == 0) {
            Toast.makeText(getApplicationContext(), "还没有设置光纤的长度，请先到系统设置中设置", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * 更新UI数据
     */
    private final int COMPLETED = 0;
    private Handler handler = new Handler() {//多线程中用于UI的更新
        @Override
        public void handleMessage(Message msg1) {
            if (msg1.what == COMPLETED) {
                try {
                    transmmitSpeedTextView.setText("采样数据：" + msg1.obj);
                } catch (NullPointerException e) {
                }
            }
        }
    };


    boolean isByteDataProcessComplete = false;

    public boolean GetByteDataProcessComlete() {
        return isByteDataProcessComplete;
    }

    public void SetByteDataProcessComplete(boolean isComplete) {
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
    /** *****************************************************************************************
     * 此处下面是各个控制按键的监听函数
     * *****************************************************************************************/
    /**
     * 按钮datamodle的监听函数
     * 每个FragmentTransanction的commit只能提交一次，所以我们在按下每个按钮时从新实例化一个FragmentTransanction进行提交操作
     */
    class ChnageTDML extends ButtonClickListener{
        public   void preThreadPrecess(){
            setStopCalibrateModelThread(true);
            setStopTemperatureModelThread(true);
            setStopThreeDimenModelThread(true);
        }
        public   void fragmentTransaction(FragmentTransaction transaction){
            transaction.replace(R.id.contineer, dataModel, "datamodel");//datamodel为fragment的tag值，用来在数据处理线程中找到当前的fragment

        }
        public   void afterThreadPrecess(){
            setStopDataModelThread(false);
        }
        public   void setFragmentnumber(){
            fragmentnumber = 0;
        }
    }
    class ChangeTCla extends ButtonClickListener{
        public   void preThreadPrecess(){
            setStopDataModelThread(true);
            setStopTemperatureModelThread(true);
            setStopThreeDimenModelThread(true);
        }
        public   void fragmentTransaction(FragmentTransaction transaction){
            transaction.replace(R.id.contineer, calibrateModel, "calibratemodel");

        }
        public   void afterThreadPrecess(){
            setStopCalibrateModelThread(false);
        }
        public   void setFragmentnumber(){
            fragmentnumber = 1;
        }
    }
    class ChangeTem extends ButtonClickListener{
        public   void preThreadPrecess(){
            setStopCalibrateModelThread(true);
            setStopDataModelThread(true);
            setStopThreeDimenModelThread(true);
        }
        public   void fragmentTransaction(FragmentTransaction transaction){
            transaction.replace(R.id.contineer, tempreatureModel, "tempreturemodel");

        }
        public   void afterThreadPrecess(){
            setStopTemperatureModelThread(false);
        }
        public   void setFragmentnumber(){
            fragmentnumber = 2;
        }
    }
    class ChangeTThdim extends ButtonClickListener{
        public   void preThreadPrecess(){
            setStopCalibrateModelThread(true);
            setStopDataModelThread(true);
            setStopTemperatureModelThread(true);
        }
        public   void fragmentTransaction(FragmentTransaction transaction){
            transaction.replace(R.id.contineer, threeDimensionModel, "threedimmodel");
        }
        public   void afterThreadPrecess(){
            setStopThreeDimenModelThread(false);
        }
        public   void setFragmentnumber(){
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
     * 打开设备的togglebutton监听函数
     */
    class StartOrStopListener implements View.OnClickListener {
        ToggleButton tgg;
        //byte [] contrlo={127,3,127,63,0,14,0,15};

        public StartOrStopListener(ToggleButton tgg) {
            this.tgg = tgg;

        }

        public void onClick(View v) {
            TOGGLE_BUTTON = 1;
            if (tgg.isChecked()) {
              //  enumerateDevice();//枚举设备，当按下该togglbutton时才打开USB
                 if (!UsbIsInitialize)
                 UsbIsInitialize=usbControl.InitializeUsb();
                 if (UsbIsInitialize) {
                    //findInterface();//找到设备接口
                    //openDevice();//打开设备
                    //assignEndpoint();//指派端点
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
        fiberLengthSharePreEdi.putInt("fiberlength", opl);
        fiberLengthSharePreEdi.commit();
        return true;
    }

    /*******************************************************************************************
     * 数据接收线程，负责从usb的endpoint读取数据
     ******************************************************************************************/
    public class DataReceiveThread extends Thread {//接收数据的线程
        private boolean suspend = false;
        Resource r;

        DataReceiveThread(Resource r) {
            this.r = r;
        }

        private String control = ""; // 只是需要一个对象而已，这个对象没有实际意义

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

                    if (r.flag) {


                        try {
                            r.wait();

                        } catch (InterruptedException ex) {

                        }
                    }
                    byte[] Receivebytes = usbControl.ReceivceDataFromUsb(65536);//接收的数据
                    if (STOREDATA == 1) {
                        try {
                            DataWR.saveData(Receivebytes);//将接收到的数据直接存储为2进制文件
                        } catch (Exception e) {
                        }
                    }
                    r.data = Receivebytes;//拼接好的数据传递出去
                    //resourceObj.speed = t;
                    r.flag = true;
                    r.notify();
                    // Log.d("接收", "数据接收线程完成");
                }
            }

        }
    }

    /**
     * ****************************************************************************************
     * dataProcess线程是将从USB接收的8bit数据合成16bit信息并显示,//分类数据传输到各自的通道
     *****************************************************************************************/
    public class DataProcess extends Thread {//接收数据的线程
        // private  final int COMPLETED = 0;
        Resource r;
        Data dd;

        DataProcess(Resource r, Data dd) {
            this.dd = dd;
            this.r = r;
        }

        private boolean suspend = false;

        private String control = ""; // 只是需要一个对象而已，这个对象没有实际意义

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
                        }

                    long startTime = System.nanoTime();             // 纳秒级
                   /* String st = "0";//数据转化为字符串的中间变量
                    String st1 = "0";//储存接收数据转化后的字符串*/
                    int p;
                    int p1;
                    int i1 = 0;
                    int[] combination = new int[r.data.length / 2];//combination用于储存合并后的16bit数据
                    // for (int i = 0; i < resourceObj.dataObj.length; i++) {//报出空指针异常的原因是接收数据还没有处理完，线程就跳转到了这里
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
                   //     Log.e("combination "+Integer.valueOf(i1).toString()," ="+Integer.valueOf(p+p1).toString());
                        i1 = i1 + 1;
                    }



                   // int[] dadd = new int[2048];
                    // = new int[2048];
                    //int[] dadd2 = new int[2048];
                    //int[] dadd3 = new int[2048];
                    //Log.e("da",Integer.valueOf(dadd3.length).toString());
//我们在实际区分4个通道的数据的时候可以用for遍历combination数组寻找表头然后放入指定的容器
//                    for (int i=0;i<combination.length;i++)//可以用foreach
//                    {
//
//                        if(combination[i]==0){
                   int[] tunnelAdata = Arrays.copyOfRange(combination, 0, combination.length/4);//copyOfRange(resourceObj,inclusive,exclusive),不包含exclusive那个

                    // Log.e("com",Integer.valueOf(combination.length).toString()+"   "+Integer.valueOf(dadd.length).toString());
//                        }
//                        else{
//
//                        }
//                        if (combination[i]==512){
                    int[] tunnelA1data = Arrays.copyOfRange(combination, combination.length / 4, combination.length / 2);
//
//                        }
//                        if (combination[i]==1024){
                    int[] tunnelBdata = Arrays.copyOfRange(combination, combination.length / 2, combination.length - combination.length / 4);

//
//                        }
//                        if(combination[i]==1536){
                    int[] tunnelB1data = Arrays.copyOfRange(combination, combination.length - combination.length / 4, combination.length);
                    //     Log.e("dadd3",Integer.valueOf(dadd3[dadd3.length-1]).toString());
                    //   Log.e("dadd33",Integer.valueOf(dadd3.length).toString());

                    // }

                    //}
                    data_a.putIntArray("tunnelAdata", tunnelAdata);//待传入通道A的数据
                    data_a1.putIntArray("tunnelA1data", tunnelA1data);//待传入通道A1的数据
                    data_b.putIntArray("tunnelBdata", tunnelBdata);//待传入通道B的数据
                    data_b1.putIntArray("tunnelB1data", tunnelB1data);//待传入通道B1的数据
                    if (TEM_ALERT == 1) {
                        if (TempreatureAlarm.alertfinish == 1) {
                            TempreatureAlarm.alertfinish = 0;
                            //TempreatureAlarm.tua1 = tunnelAdata;
                            //TempreatureAlarm.tua2 = tunnelA1data;
                            //TempreatureAlarm.tub1 = tunnelBdata;
                            //TempreatureAlarm.tub2 = tunnelB1data;
                            TempreatureAlarm.gettempalert();
                        }
                    }
                    /***
                     * 这个线程同步的作用是发送处理好的数据交给fragment线程进行处理，在fragment显示线程进行数据显示的过程中，要求当前数据处理线程进行等待
                     */
                    synchronized (dd) {
                        try {

                            set_TubeA1_data(data_a);//将分好类的数据放入Bundle中以供fragment访问
                            set_TubeA1_data1(data_a1);
                            set_TubeA1_data2(data_b);
                            set_TubeA1_data3(data_b1);
                            dd.flag1 = true;//与fragment中的绘图线程进行生产者与消费者

                            switch (fragmentnumber) {
                                case 0:
                                    DataModel frgment1 = (DataModel) getFragmentManager().findFragmentByTag("datamodel");//获取当前的fragment
                                    frgment1.wakeup();//调用fragment中的唤醒方法
                                case 1:
                                    CalibrateModel frgment2 = (CalibrateModel) getFragmentManager().findFragmentByTag("calibratemodel");//获取当前的fragment
                                    frgment2.wakeUp();//调用fragment中的唤醒方法
                                case 2:
                                    TempreatureModel fragment3 = (TempreatureModel) getFragmentManager().findFragmentByTag("tempreturemodel");
                                    fragment3.wakeup();//调用fragment中的唤醒方法
                                case 4:
                                    com.example.datausb.ThreeDimensionModel fragment4 = (ThreeDimensionModel) getFragmentManager().findFragmentByTag("threedimmodel");
                                    fragment4.wakeup();//调用fragment中的唤醒方法
                            }

                            //DataModel frgment1 = (DataModel) getFragmentManager().findFragmentByTag("datamodel");//获取当前的fragment
                            //frgment1.wakeUp();//调用fragment中的唤醒方法
                            /**
                             * 先实现数据处理线程的等待，直到该fragment中显示线程显示数据完成后由显示线程对当前的数据处理线程进行唤醒继续运行
                             */
                            if (dd.flag1) {
                                try {
                                    dd.wait();
                                } catch (InterruptedException ex) {
                                }
                            } else {
                                dd.notifyAll();
                            }

                        } catch (NullPointerException ee) {
                        }
                    }
                    long estimatedTime = System.nanoTime() - startTime;
                    //set_TubeA1_data(tunnelAdata, tunnelA1data, tunnelBdata, tunnelB1data);//调用传入通道A数据函数
                    Message msg1 = new Message();
                    msg1.what = COMPLETED;
                    msg1.obj = tunnelAdata[2] + "+" + tunnelA1data[2];
                    //msg1.obj =combination[0]+"+"+combination[20480]+"+"+combination[30720]+"+"+combination[40959];//要显示的数据，测试使用
                    //msg1.obj =resourceObj.dataObj[0]+"+"+resourceObj.dataObj[40960]+"+"+resourceObj.dataObj[61440]+"+"+resourceObj.dataObj[81919];//要显示的数据，测试使用
                    msg1.arg1 = r.speed;//数据的传输速度
                    msg1.arg2 = (int) estimatedTime;//arg2表示携带的处理速度信息
                    handler.sendMessage(msg1);
                    r.flag = false;
                    r.notify();
                    SetByteDataProcessComplete(true);

                    // Log.d("数据处理", "数据处理线程完成");
                }
            }


        }
    }

    /**
     * 创建四个Bundle数组用来携带每个通道的数据信息以便传递到各个Fragment
     */
    Bundle tubeAdata;
    Bundle tubeA1data;
    Bundle tubeBdata;
    Bundle tubeB1data;

    /**
     * set_TubeA1_data(Bundle a)函数是数据处理线程调用的函数，存放数据
     *
     * @param a
     */
    public void set_TubeA1_data(Bundle a) {

        tubeAdata = a;
    }

    public void set_TubeA1_data1(Bundle a) {

        tubeA1data = a;
    }

    public void set_TubeA1_data2(Bundle a) {

        tubeBdata = a;
    }

    public void set_TubeA1_data3(Bundle a) {

        tubeB1data = a;
    }

    /**
     * 在Frafment中利用 Bundle bb= ((main1) getActivity()).get_TubeA1_data()来从该Avtivity中获取数据
     *
     * @return返回通道的数据Bundle
     */
    public Bundle get_TubeA1_data() {
        return tubeAdata;
    }

    public Bundle get_TubeA1_data1() {
        return tubeA1data;
    }

    public Bundle get_TubeA1_data2() {
        return tubeBdata;
    }

    public Bundle get_TubeA1_data3() {
        return tubeB1data;
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
        Bundle[] DD;
        boolean flag1 = false;
    }

    /**
     * 该语句为实例化一个数据接收的类，re为数据接收线程
     */
    Resource resourceObj = new Resource();
    Data dataObj = new Data();
    public DataReceiveThread dataReceiveThread = new DataReceiveThread(resourceObj);
    public DataProcess dataProcessThread = new DataProcess(resourceObj, dataObj);
    public void UsbSendData(byte[] bytes){
        usbControl.SendDataToUsb(bytes);
    }

   abstract class ButtonClickListener implements View.OnClickListener{
        public void onClick(View v) {
            if (TOGGLE_BUTTON == 1) {
                startOrStopToggleButton.setChecked(true);
            }
            fiberLength = fiberLengthSharePre.getInt("fiberlength", 0);
            if (fiberLength == 0) {
                Toast.makeText(getApplicationContext(), "还没有设置光纤的长度，请先到系统设置中设置", Toast.LENGTH_SHORT).show();
            } else {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                dataReceiveThread.setSuspend(true);
                dataProcessThread.setSuspend(true);
                preThreadPrecess();
                fragmentTransaction(transaction);
                afterThreadPrecess();
                transaction.commit();
                setFragmentnumber();
                dataReceiveThread.setSuspend(false);
                dataProcessThread.setSuspend(false);
            }
        }
        abstract  void preThreadPrecess();
        abstract  void fragmentTransaction(FragmentTransaction transaction);
        abstract  void afterThreadPrecess();
        abstract  void setFragmentnumber();

   }

}
