package com.example.datausb;

import android.content.Context;
import android.graphics.Canvas;

import android.graphics.Paint;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.datausb.Fiber.Fiber;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by sunset on 15/11/19.
 * /**
 * tempreturemodel要实现的功能就是先将标定的数据取出来，同时计算tuba和tua1的比值既P（SA），然后利用公式1/T=(K/HV)ln(P(SA(T)/P(SA)(0))+1/T0计算出y值，
 * 然后在surfaceview上显示出比值的图形
 */

public class TempreatureModel extends android.app.Fragment {
    /**
     * 定义一个SurfaceHolder用来管理surface
     */

    /**
     * 这个函数的作用是使Activity可以唤醒fragment中的显示线程
     */
    public void wakeup() {
        ((Main) getActivity()).dataObj.notifyAll();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tempreaturemodel, container, false);

    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);//
        try {
            for (Map.Entry<String, Fiber> item : ((Main) getActivity()).fiberManager.getFiberMap().entrySet()) {//遍历HashMap获得其中光纤的引用
                item.getValue().setCalibrate();
            }
        } catch (Exception e) {
            Toast.makeText(getActivity().getApplicationContext(), "标定数据不存在，请先在标定模式下进行标定", Toast.LENGTH_SHORT).show();

        }

        /**
         * 获得布局中的surfaceview
         */
        SurfaceView sur = (SurfaceView) getActivity().findViewById(R.id.surb1);
        /**
         * 将holder和surfaceview绑定
         */

        SurfaceHolder holder = sur.getHolder();
        /**
         * 实例化一个surfaceview
         */
        drawLineSurface v1 = new drawLineSurface(getActivity(), sur);
        /**
         * 调用这个surfaceview的surfaceCreated方法
         */
        v1.surfaceCreated(holder);

    }

    /**
     * 一个继承Surfaceview并实现了SurfaceHolder.Callback方法的的类
     */

    class drawLineSurface extends SurfaceView implements SurfaceHolder.Callback {
        private tempreatureThread myThread;
        SurfaceView surfaceView;

        /**
         * 该类的构造函数
         *
         * @param context 系统上问
         * @param surfaceView，传入sur使绘图线程获得当前surfaceview的大小
         */
        public drawLineSurface(Context context, SurfaceView surfaceView) {
            super(context);
            this.surfaceView = surfaceView;
        }

        public void surfaceChanged(SurfaceHolder holder1, int a, int b, int c) {
            holder1.addCallback(this);
            myThread = new tempreatureThread(holder1, surfaceView);//创建一个绘图线程
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
    class tempreatureThread extends Thread {
        private SurfaceHolder holder;
        public boolean isRun;

        int showLineSurfaceViewHeught;
        int showLineSurfaceViewWidth;
        SurfaceView showLineSurfaceView;
        DataChart dataChart;

        /**
         * 该线程的构造函数
         *
         * @param holder，传入的holder用来指定绘图的surfaceview
         * @param surfaceView，用来获得surfaceview的大小
         */
        public tempreatureThread(SurfaceHolder holder, SurfaceView surfaceView) {
            this.holder = holder;
            showLineSurfaceView = surfaceView;
            isRun = true;
            showLineSurfaceViewHeught = showLineSurfaceView.getHeight();
            showLineSurfaceViewWidth = showLineSurfaceView.getWidth();
            dataChart = new DataChart(5000, 0, 100, 0);
            dataChart.setyMax(100);
        }

        public void run() {
            try {//捕获线程运行中切换界面而产生的的空指针异常，防止程序崩溃。
                while (!((Main) getActivity()).stopTemperatureModelThread) {
                    synchronized (((Main) getActivity()).dataObj) {//所有的等待和唤醒的锁都是同一个，这里选用了Activity中的一个对象
                        /**
                         * 如果当标志位为false这个线程开始等待
                         */
                        if (!((Main) getActivity()).dataObj.flag1)
                            try {
                                ((Main) getActivity()).dataObj.wait();

                            } catch (InterruptedException ex) {
                                Log.e("温度模式",Log.getStackTraceString(ex));
                            }
                        else {
                            ((Main) getActivity()).dataObj.notifyAll();
                        }
                        List<float[]> dataLine = new ArrayList<>();//存数据
                        List<Paint> linePaint = new ArrayList<>();//存画笔

                        for (Map.Entry<String, Fiber> item : ((Main) getActivity()).fiberManager.getFiberMap().entrySet()) {//遍历HashMap获得其中光纤的引用
                            dataLine.add(item.getValue().showcalculateTempreture());//加入标定数据
                            linePaint.add(item.getValue().getCalibratePaint());//加入1663画笔绘制图线
                        }

                        Canvas c = holder.lockCanvas();


                        dataChart.drawAll(c, dataLine, linePaint);
                        // Log.e("温度模式","ok");

                        /**
                         * 结束锁定画布并显示
                         */
                        holder.unlockCanvasAndPost(c);//结束锁定画图，并提交改变。// ;
                        /**
                         * 把标识位置为false
                         * 同时唤醒数据处理线程
                         */

                        ((Main) getActivity()).dataObj.flag1 = false;
                        ((Main) getActivity()).wakeUpAllMainThread();
                    }


                }


            } catch (NullPointerException e) {
                Log.d("tempretureModel", Log.getStackTraceString(e));


            }

        }


    }

}