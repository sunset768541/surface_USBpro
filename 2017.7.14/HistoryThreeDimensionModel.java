package com.example.datausb;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;

/**
 * Created by sunset on 16/3/3.
 */
public class HistoryThreeDimensionModel extends android.app.Fragment {
    public MySurfaceView mv;
    static float WIDTH;
    static float HEIGHT;
    public SeekBar seeekbar;
    private threedimThread myThread;
    private TextView showTime;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout ff = (FrameLayout) getActivity().findViewById(R.id.showdataframe);
        float x = ff.getWidth();
        float y = ff.getHeight();
        if (x > y) {
            WIDTH = x;
            HEIGHT = y;
        } else {
            WIDTH = y;
            HEIGHT = x;
        }
        mv = new MySurfaceView(getActivity());
        mv.requestFocus();//获取焦点
        mv.setFocusableInTouchMode(true);//设置为可触控
        seeekbar=(SeekBar)getActivity().findViewById(R.id.seekBar);
        seeekbar.setEnabled(true);
        showTime=(TextView)getActivity().findViewById(R.id.textView18);


    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = mv;
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);//
    }
    public void threadstart(){
        myThread = new threedimThread();//创建一个绘图线程
        myThread.start();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    private final int COMPLETED = 0;
    DecimalFormat decimalFormat=new DecimalFormat(".00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
    private Handler handler = new Handler() {//多线程中用于UI的更新
        @Override
        public void handleMessage(Message msg1) {
            if (msg1.what == COMPLETED) {
                try {
                    Log.e("UI更新线程","-----之心");
                    seeekbar.incrementProgressBy((int)DataRD.dataBuffer.length);
                    showTime.setText(decimalFormat.format((float)msg1.arg2/(float)msg1.arg1*100)+"%");
                } catch (NullPointerException e) {
                }
            }
        }
    };

    class threedimThread extends Thread {


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
                while (!DataRD.HAVE_READ_FINISEH) {
                    long startTime = System.currentTimeMillis();
                    DataRD.SHOW_DATA_THREAT_FLAG=true;//标志读取一组数据的操作开始
                    try {
                        DataRD.dataInput.read(DataRD.dataBuffer);//这种读取方式十分快，1分钟数据20s读取完成
                        DataRD.dataInput.seek(DataRD.seek + DataRD.dataBuffer.length);

                    } catch (IOException e) {
                        Log.e("出现IO异常", "histhreeDim"+e.toString());
                    }
                    DataRD.seperateTunnelData();

                    int[] tuba = DataRD.tunnelAdata;
                    int[] tuba1 = DataRD.tunnelA1data;
                    int[] tubeb = DataRD.tunnelBdata;
                    int[] tubeb1 = DataRD.tunnelB1data;
                    float[] T1 = new float[tuba.length];
                    float[] T2 = new float[tuba.length];
                    float[] PSA1 = new float[tuba.length];
                    float[] PSA2 = new float[tuba.length];
                    for (int i = 0; i < tuba.length; i++) {
                        if (tuba[i] == 0) {
                            PSA1[i] = 0;
                        } else PSA1[i] = (float) tuba1[i] / tuba[i];
                        if (tubeb[i] == 0) {
                            PSA2[i] = 0;
                        } else PSA2[i] = (float) tubeb1[i] / tubeb[i];
                    }

                    /**
                     * 由公式计算出温度
                     */
                    for (int i = 0; i < tuba.length; i++) {
                        double bb1 = (double) PSA1[i] / DataRD.clabiraA[i];
                        double bb2 = (double) PSA2[i] / DataRD.clabiraB[i];
                        float tt1 = (float) (Math.log(bb1) + 1 / DataRD.clabiraA[DataRD.clabiraA.length - 1]);
                        float tt2 = (float) (Math.log(bb2) + 1 / DataRD.clabiraB[DataRD.clabiraB.length - 1]);
                        T1[i] = 1 / tt1;
                        T2[i] = 1 / tt2;
                    }
                    float[] TR = screenadapter(T2, mv.mRender.getcont() / 4);//选择T1通道的温度进行显示
                    float[] colors = new float[mv.mRender.getcont()];//创建用于给光纤模型颜色渲染的数据
                    float[] cc = colorprocess(TR);
                    colors = Arrays.copyOfRange(cc, 0, colors.length);
                    mv.mRender.setcolor(colors);
                    DataRD.seek = DataRD.seek + DataRD.dataLength;
                    Log.e("读文件大小",Long.toString(DataRD.fileLength));
                    Log.e("指针的位置",Long.toString(DataRD.seek));
                    try {
                        DataRD.moveSeek();
                    }
                    catch (IOException e){

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

            return adptertube;
        }
    }
}