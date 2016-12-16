package com.example.datausb;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.datausb.DataUtil.DataBaseOperation;
import com.example.datausb.Fiber.Fiber;
import com.example.datausb.Fiber.FiberA;
import com.example.datausb.Fiber.FiberB;
import com.example.datausb.Fiber.FiberC;
import com.example.datausb.Fiber.FiberD;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * calibratemodel要实现的功能就是将tuba，tuba1的比值既P（SA）和标定温度T0储存起来，然后在surfaceview上显示出比值的图形
 */

public class CalibrateModel extends android.app.Fragment {
    /**
     * 定义一个SurfaceHolder用来管理surface
     */
    private SurfaceHolder holder;
    EditText fiberAclabricateTemperature;
    EditText fiberBclabricateTemperature;
    EditText fiberCclabricateTemperature;
    EditText fiberDclabricateTemperature;
    Button fiberAstartCalibrate;
    Button fiberBstartCalibrate;
    Button fiberCstartCalibrate;
    Button fiberDstartCalibrate;
    /**
     * 这个函数的作用是使Activity可以唤醒fragment中的显示线程
     */
    public void wakeUp() {
        ((Main) getActivity()).dataObj.notifyAll();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.calibratemodel, container, false);
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);//
        fiberAclabricateTemperature = (EditText) getActivity().findViewById(R.id.editText);
        fiberBclabricateTemperature = (EditText) getActivity().findViewById(R.id.editText2);
        fiberCclabricateTemperature = (EditText) getActivity().findViewById(R.id.editText20);
        fiberDclabricateTemperature = (EditText) getActivity().findViewById(R.id.editText21);
        fiberAstartCalibrate = (Button) getActivity().findViewById(R.id.button8);
        fiberBstartCalibrate = (Button) getActivity().findViewById(R.id.button9);
        fiberCstartCalibrate = (Button) getActivity().findViewById(R.id.button4);
        fiberDstartCalibrate = (Button) getActivity().findViewById(R.id.button5);
        fiberAstartCalibrate.setOnClickListener(new ClabriateButtonOnClickListener(FiberA.createFiberA(),fiberAclabricateTemperature));
        fiberBstartCalibrate.setOnClickListener(new ClabriateButtonOnClickListener(FiberB.createFiberB(),fiberBclabricateTemperature));
        fiberCstartCalibrate.setOnClickListener(new ClabriateButtonOnClickListener(FiberC.createFiberC(),fiberCclabricateTemperature));
        fiberDstartCalibrate.setOnClickListener(new ClabriateButtonOnClickListener(FiberD.createFiberD(),fiberDclabricateTemperature));
        /**
         * 获得布局中的surfaceview
         */
        SurfaceView sur = (SurfaceView) getActivity().findViewById(R.id.surb);
        /**
         * 将holder和surfaceview绑定
         */
        holder = sur.getHolder();
        /**
         * 实例化一个surfaceview
         */
        VV v1 = new VV(getActivity(), holder, sur);
        /**
         * 调用这个surfaceview的surfaceCreated方法
         */
        v1.surfaceCreated(holder);

    }

    /**
     * 一个集成Surfaceview并实现了SurfaceHolder.Callback方法的的类
     */

    class VV extends SurfaceView implements SurfaceHolder.Callback {
        private calibrateThread myThread;
        SurfaceView ss;

        /**
         * 该类的构造函数
         *
         * @param context
         * @param holder1，传入holder，给绘图线程使用
         * @param sur，传入sur使绘图线程获得当前surfaceview的大小
         */
        public VV(Context context, SurfaceHolder holder1, SurfaceView sur) {
            super(context);
            ss = sur;
        }

        public void surfaceChanged(SurfaceHolder holder1, int a, int b, int c) {
            holder1.addCallback(this);
            myThread = new calibrateThread(holder1, ss);//创建一个绘图线程
            myThread.start();
        }

        public void surfaceCreated(SurfaceHolder holder) {
            holder.addCallback(this);
        }

        public void surfaceDestroyed(SurfaceHolder holder) {

        }

    }

    /**
     * 一个绘图线程类
     */
    class calibrateThread extends Thread {
        private SurfaceHolder holder;
        public boolean isRun;

        int showLineViewHeigth;
        int showLineViewWidth;
        SurfaceView sss;
        float fiberLength = 2048;
        float maxnum = 16384;
        float[] tp1;
        float[] tp2;
        DataChart dataChart;
        Canvas c;

        /**
         * 该线程的构造函数
         *
         * @param holder，传入的holder用来指定绘图的surfaceview
         * @param ss1，用来获得surfaceview的大小
         */
        public calibrateThread(SurfaceHolder holder, SurfaceView ss1) {
            this.holder = holder;
            sss = ss1;
            isRun = true;
            showLineViewHeigth = sss.getHeight();
            showLineViewWidth = sss.getWidth();
            dataChart = new DataChart(5000,0,10,0);
            dataChart.setyMax(10);
        }

        public void run() {
            try {//捕获线程运行中切换界面而产生的的空指针异常，防止程序崩溃。
                while (!((Main) getActivity()).stopCalibrateModelThread) {
                    synchronized (((Main) getActivity()).dataObj) {//所有的等待和唤醒的锁都是同一个，这里选用了Activity中的一个对对象
                        /**
                         * 如果当标志位为false这个线程开始等待
                         */
                        if (!((Main) getActivity()).dataObj.flag1)
                            try {
                                ((Main) getActivity()).dataObj.wait();

                            } catch (InterruptedException ex) {

                            }
                        else {
                            ((Main) getActivity()).dataObj.notifyAll();
                        }

                        synchronized (holder) {
                            List<float[]> dataLine = new ArrayList<>();//存数据
                            List<Paint> linePaint=new ArrayList<>();//存画笔
                            for (Map.Entry<String,Fiber>item: ((Main)getActivity()).fiberManager.getFiberMap().entrySet()) {//遍历HashMap获得其中光纤的引用
                                dataLine.add(item.getValue().showcCalculateCalibrate());//加入标定数据
                                linePaint.add(item.getValue().getCalibratePaint());//加入1663画笔绘制图线
                            }
                            c = holder.lockCanvas();
                            dataChart.drawAll(c, dataLine, linePaint);
                            //Log.e("标定熟悉", "时间");
                            /**
                             * 结束锁定画布并显示
                             */
                            holder.unlockCanvasAndPost(c);//结束锁定画图，并提交改变。// ;
                            /**
                             * 把标识位置为false
                             * 同时唤醒数据处理线程
                             */

                            ((Main) getActivity()).dataObj.flag1 = false;//不要在该语句前加Log输出
                            ((Main) getActivity()).wakeUpAllMainThread();
                        }


                    }


                }

            } catch (NullPointerException e) {
                Log.d("CalibrateModel", "标定模式出现空指针异常");

            }

        }
    }
    class ClabriateButtonOnClickListener implements View.OnClickListener{//执行标定按钮的监听函数
        String calibrateTempreture;
        Fiber fiber;
        String fiberName;
        EditText editText;
        public ClabriateButtonOnClickListener(Fiber fiber,EditText editText){
            this.fiber=fiber;
            fiberName=fiber.getFiberName();
            this.editText=editText;
        }
        @Override
        public void onClick(View v) {
            calibrateTempreture=editText.getText().toString().trim();
            if(TextUtils.isEmpty(calibrateTempreture)){
                Toast.makeText(getActivity().getApplicationContext(), "请输入当前的标定温度", Toast.LENGTH_SHORT).show();
            }
            else {
                //建立表格的sql命令，存在就不建立，不存在就建立
                String stu_table = "create table if not exists "+fiberName+"(_id integer primary key autoincrement,calibriateTempreture FLOAT,calibriateData text)";
                DataBaseOperation.mDataBaseOperation.creatOrGetTable(stu_table);
                try {
                    new Thread(){
                        public void run(){
                          //  float k=
                            DataBaseOperation.mDataBaseOperation.updataDataBase(Float.parseFloat(calibrateTempreture)+273.15f,fiber.calculatePsa(),fiberName);
                            fiber.setCalibrate();
                            Looper.prepare();
                            Toast.makeText(getActivity().getApplicationContext(), fiberName+"标定完成", Toast.LENGTH_SHORT).show();
                            Looper.loop();

                        }
                    }.start();


                }
                catch (NullPointerException e){
                    Toast.makeText( getActivity().getApplicationContext(), fiberName+"通道1无数据输入", Toast.LENGTH_SHORT).show();

                }
            }


        }


    }
}


