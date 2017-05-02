package com.example.datausb;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.datausb.DataUtil.DataRD;
import com.example.datausb.Fiber.Fiber;
import com.example.datausb.ThreeDimUtil.MySurfaceView;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by sunset on 2017/4/26.
 */

public class HistoryTempretureModel extends android.app.Fragment{
    public SeekBar seeekbar;
    private TextView showTime;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout ff = (FrameLayout) getActivity().findViewById(R.id.showdataframe);
        seeekbar=(SeekBar)getActivity().findViewById(R.id.seekBar);
        seeekbar.setEnabled(true);
        showTime=(TextView)getActivity().findViewById(R.id.textView18);
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.histemmode, container, false);
    }
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);//
        /**
         * 获得布局中的surfaceview
         */
        SurfaceView sur = (SurfaceView) getActivity().findViewById(R.id.surb2);
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

    private final int COMPLETED = 0;
    DecimalFormat decimalFormat=new DecimalFormat(".00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
    private Handler handler = new Handler() {//多线程中用于UI的更新
        @Override
        public void handleMessage(Message msg1) {
            if (msg1.what == COMPLETED) {
                try {
                    Log.e("UI更新线程","-----之心");
                    seeekbar.incrementProgressBy((int) DataRD.dataBuffer.length);
                    showTime.setText(decimalFormat.format((float)msg1.arg2/(float)msg1.arg1*100)+"%");
                } catch (NullPointerException e) {
                    Log.e("History",Log.getStackTraceString(e));
                }
            }
        }
    };
    class drawLineSurface extends SurfaceView implements SurfaceHolder.Callback {
        private HisTemhread myThread;
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
            myThread = new HisTemhread(holder1, surfaceView);//创建一个绘图线程
            myThread.start();
        }

        public void surfaceCreated(SurfaceHolder holder) {
            holder.addCallback(this);
        }

        public void surfaceDestroyed(SurfaceHolder holder) {

        }


    }
    class HisTemhread extends Thread {
        private SurfaceHolder holder;
        public boolean isRun;

        int showLineSurfaceViewHeught;
        int showLineSurfaceViewWidth;
        SurfaceView showLineSurfaceView;
        DataChart dataChart;
        public HisTemhread(SurfaceHolder holder, SurfaceView surfaceView) {
            this.holder = holder;
            showLineSurfaceView = surfaceView;
            isRun = true;
            showLineSurfaceViewHeught = showLineSurfaceView.getHeight();
            showLineSurfaceViewWidth = showLineSurfaceView.getWidth();
            dataChart = new DataChart(5000, 0, 100, 0);
            dataChart.setyMax(100);
        }
        public void run() {
            /**
             * 这个sleep是为了解决当开启设备后，从其他模式切换到当前模式时，由于多线程操作，时访问mRender.lovo1.vCont时会出现空指针异常，这是因为
             * lovo1在实例化之前，线程就切换到了该线程执行访问操作，造成了访问空指针异常，通过让当前的线程sleep 500ms，这期间lovo1就可以实例化，然后
             * 就可以被该线程访问了
             */
            try {
                this.sleep(350);

            } catch (InterruptedException e) {
            }
            try {//捕获线程运行中切换界面而产生的的空指针异常，防止程序崩溃。
                seeekbar.setMax((int)DataRD.pureDataLength);
                seeekbar.setProgress(0);
                int readDataLength=0;
                while (!DataRD.HAVE_READ_FINISEH) {//没有读取完成
                    long startTime = System.currentTimeMillis();
                    DataRD.SHOW_DATA_THREAT_FLAG=true;//标志读取一组数据的操作开始
                    try {
                        DataRD.dataInput.read(DataRD.dataBuffer);//这种读取方式十分快，1分钟数据20s读取完成
                        DataRD.dataInput.seek(DataRD.seek + DataRD.dataBuffer.length);
                    } catch (IOException e) {
                        Log.e("出现IO异常", "histhreeDim"+e.toString());
                    }
                    DataRD.decodeData();
                    List<float[]> dataLine = new ArrayList<>();//存数据
                    List<Paint> linePaint = new ArrayList<>();//存画笔

                    for (Map.Entry<String, Fiber> item : ((Main) getActivity()).fiberManager.getFiberMap().entrySet()) {//遍历HashMap获得其中光纤的引用
                        dataLine.add(item.getValue().showcalculateTempreture());//加入计算后的温度数据
                        linePaint.add(item.getValue().getCalibratePaint());//加入1663画笔绘制图线
                    }
                    Canvas c = holder.lockCanvas();
                    dataChart.drawAll(c, dataLine, linePaint);
                    // Log.e("温度模式","ok");
                    /**
                     * 结束锁定画布并显示
                     */
                    holder.unlockCanvasAndPost(c);//结束锁定画图，并提交改变。// ;
                    //Log.e("histRT","4");
                    DataRD.seek = DataRD.seek + DataRD.dataLength;
                    Log.e("读文件大小",Long.toString(DataRD.pureDataLength));
                    Log.e("指针的位置",Long.toString(DataRD.seek-DataRD.fileHeadLength));
                    try {
                        DataRD.moveSeek();
                    }
                    catch (IOException e){
                        Log.e("histreoceDIm","moveseek"+"错误"+Log.getStackTraceString(e));
                    }
                    Message msg1 = new Message();
                    msg1.what = COMPLETED;
                    msg1.arg1=(int)DataRD.pureDataLength;
                    readDataLength=DataRD.dataBuffer.length+readDataLength;
                    msg1.arg2=readDataLength;
                    handler.sendMessage(msg1);
                    long estimatedTime = System.currentTimeMillis() - startTime;
                    try {
                        if(DataRD.meanTimePerData>estimatedTime)
                            Thread.sleep(DataRD.meanTimePerData-estimatedTime);
                    }
                    catch (Exception e){
                        Log.e("His",Log.getStackTraceString(e));
                    }
                    DataRD.SHOW_DATA_THREAT_FLAG=false;//标志读取一组数据操作完成
                }
                try {
                    DataRD.closeReadStream();
                }
                catch (IOException e){
                    Log.getStackTraceString(e);
                }
                Log.e("文件读取完成","文件已经读取完成了");
                DataRD.HAVE_READ_FINISEH=false;

            } catch (NullPointerException e)

            {
                Log.d("threedimModel", "历史记录的3维模式出现空指针异常");

            }
        }


        /**
         * 求数组最大
         *
         * @param a
         * @return
         */
        public float max(float[] a) {
            float b;
            Arrays.sort(a);
            b = a[a.length - 1];
            return b;
        }

        /**
         * 求数组的最小
         *
         * @param a
         * @return
         */
        public float min(float[] a) {
            float b;
            Arrays.sort(a);
            b = a[0];
            return b;
        }

        /**
         * 利用伪颜色增强算法把温度数据转换成RGB的形式，温度的变化图请参考drawable中的tempchangecolor.jpg图片
         * 目前这个算法算出的温度颜色与当前的数据有关系，由当前数据的最大值和最小值决定，以后要改为由传感器的测量温度范围决定
         *
         * @param data
         * @return
         */
        public float[] colorprocess(float[] data) {
            float min = min(data);//获得温度数组中最小的数值，数组中每个数组都减去这个最小值，那么这个数组就没有存在负数的可能
            for (int i = 0; i < data.length; i++) {//该for循环就是将数组中的数据全部变成正数
                data[i] = data[i] - min;
            }
            float max = max(data);//获得数组中的最大值，根据这个数值将温度数据分成四个颜色范围
            float[] color = new float[data.length * 4];//用于存储生成的颜色值
            int j = 0;
            for (int i = 0; i < data.length; i++) {
                if (0 <= data[i] && data[i] < max / 4) {//这个范围为蓝色到浅蓝色范围，最小值为纯蓝色【0，0，1】。数值越大越偏向浅蓝色，可以通过增加绿色分量实现
                    color[j] = 0;//红色分量为0
                    color[j + 1] = data[i] / max;//根据温度数据增加绿色分量，
                    color[j + 2] = 1;//纯蓝色
                    color[j + 3] = 1;//透明度为1
                } else if (max / 4 <= data[i] && data[i] < max / 2) {//这个范围为浅蓝色到绿色，【0，1，1】-->[0,1,0],这个过程当数据越大就越少的蓝色分量，这样就向绿色过渡
                    color[j] = 0;//红色分量为0
                    color[j + 1] = 1;//绿色分量为1
                    color[j + 2] = 1 - data[i] / max;//根据温度数值的大小，去除相应的绿色
                    color[j + 3] = 1;
                } else if (max / 2 <= data[i] && data[i] < max * 3 / 4) {//这个范围为绿色到黄色，【0，1，0】-->[1，1，0],这个过程通过增加红色的分量可以实现绿色到黄色的过渡
                    color[j] = data[i] / max;
                    color[j + 1] = 1;
                    color[j + 2] = 0;
                    color[j + 3] = 1;
                } else {//这个范围为黄色到红色的过渡，【1，1，0】-->【1，0，0】,这个过程通过减少绿色的分量就可以达到黄色到红色的颜色过渡
                    color[j] = 1;
                    color[j + 1] = 1 - data[i] / max;
                    color[j + 2] = 0;
                    color[j + 3] = 1;
                }
                j = j + 4;
            }
            return color;
        }

        /**
         * 将光纤的长度与采集温度数据点的个数进行适配
         *
         * @param data
         * @param w
         * @return
         */
        public float[] screenadapter(float[] data, int w) {
            Log.e("适配进入","ok");
            float[] adptertube = new float[w];//设置屏可以显示在屏幕上的数据长度
            float[] databuf;
            int interval = data.length / w + 1;
            int kkk = 0;
            if (interval <= 1) {
                adptertube = data;
            } else {

                for (int i = 0; i < (data.length / interval) * interval; i = i + interval) {
                    databuf = Arrays.copyOfRange(data, i, i + interval);
                    adptertube[kkk] = max(databuf);//这里出现了空指针异常
                    kkk = kkk + 1;
                }
            }
            Log.e("适配完场","ok");
            return adptertube;
        }
    }
}
