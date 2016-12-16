package com.example.datausb;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.datausb.Fiber.Fiber;
import com.example.datausb.Fiber.FiberA;
import com.example.datausb.ThreeDimUtil.MySurfaceView;

import java.util.Arrays;
import java.util.Map;

/**
 * Created by sunset on 16/3/3.
 * 三维模式Fragment
 */
public class ThreeDimensionModel extends android.app.Fragment {
    public MySurfaceView mv;
    public static float WIDTH;
    public static float HEIGHT;


    public void wakeup() {
        ((Main) getActivity()).dataObj.notifyAll();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout ff = (FrameLayout) ( getActivity().findViewById(R.id.contineer));
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


    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            for (Map.Entry<String, Fiber> item : ((Main) getActivity()).fiberManager.getFiberMap().entrySet()) {//遍历HashMap获得其中光纤的引用
                item.getValue().setCalibrate();
            }
        } catch (Exception e) {
            Toast.makeText(getActivity().getApplicationContext(), "标定数据不存在，请先在标定模式下进行标定", Toast.LENGTH_SHORT).show();

        }

        // Log.e("onCreat", Integer.valueOf(mv.getcont()).toString());
        return mv;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);//
        boolean datareceive = ((Main) getActivity()).getByteDataProcessComlete();
        RenderThread myThread = new RenderThread(datareceive);//创建一个绘图线程
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

    class RenderThread extends Thread {
        boolean dd;

        public RenderThread(boolean dd) {
            this.dd = dd;
        }

        public void run() {
            /**
             * 这个sleep是为了解决当开启设备后，从其他模式切换到当前模式时，由于多线程操作，时访问mRender.lovo1.vCont时会出现空指针异常，这是因为
             * lovo1在实例化之前，线程就切换到了该线程执行访问操作，造成了访问空指针异常，通过让当前的线程sleep 500ms，这期间lovo1就可以实例化，然后
             * 就可以被该线程访问了
             */
            try {
                sleep(1800);
                /**
                测试光纤的颜色
                 **/
//                float[] colors = new float[mv.mRender.getcont()];//创建用于给光纤模型颜色渲染的数据
//                float[] cc = colorProcess(testColor());
//
//                colors = Arrays.copyOfRange(cc, 0, colors.length);
//
//                mv.mRender.setcolor(colors);
            } catch (InterruptedException e) {
                Log.e("三维模式",Log.getStackTraceString(e));
            }

            try {//捕获线程运行中切换界面而产生的的空指针异常，防止程序崩溃。
                while (!((Main) getActivity()).stopThreeDimenModelThread) {


                    synchronized (((Main) getActivity()).dataObj) {//所有的等待和唤醒的锁都是同一个，这里选用了Activity中的一个对对象
                        /**
                         * 如果当标志位为false这个线程开始等待
                         */
                        if (!((Main) getActivity()).dataObj.flag1)
                            try {
                                ((Main) getActivity()).dataObj.wait();
                            } catch (InterruptedException ex) {
                                Log.e("三维模式",Log.getStackTraceString(ex));
                            }
                        else {
                            ((Main) getActivity()).dataObj.notifyAll();
                        }
                        float[] cc = colorProcess(FiberA.createFiberA().calculateTempreture());
                        mv.mRender.setcolor(cc);
                        ((Main) getActivity()).dataObj.flag1 = false;
                        ((Main) getActivity()).wakeUpAllMainThread();

                    }
                }
            } catch (NullPointerException e) {
                Log.d("threedimModel", Log.getStackTraceString(e));


            }
        }
    }
public float[] testColor(){//模拟温度数据测试光纤的颜色
    float [] x=new float[4096];
    for (int i=0;i<4095;i++){
       if (i==1677)//在1677处光纤上的温度是50度
       {x[i]=50;
        }
    }

    return x;
}
    /**
     * 求数组最大
     *
     * @param a 查找最大值的数组
     * @return 返回一个数组中的最大值
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
     * @param a 查找最小值的数组
     * @return 返回一个最小值
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
     *添加当开启了温度报警时，所有大于设定温度的点的颜色设置为红色
     * @param data 根据传入的数据计算颜色值
     * @return 返回三维空间的顶点颜色
     */
    public float[] colorProcess(float[] data) {
        float min=0;
        float max=80;
        float split=max/4;
        float[] color = new float[data.length * 4];//用于存储生成的颜色值
        int j = 0;
    //    Log.e("1677的温度",Float.valueOf(data[1677]).toString());
        for (float i:data){

            if (i<min){//i<最小值为蓝色
                color[j] = 0;//红色分量为0
                color[j + 1] =0;//根据温度数据增加绿色分量，
                color[j + 2] = 1;//纯蓝色
                color[j + 3] = 1;//透明度为1
            }
            else if (i>max){//i>最大值 为红色
                color[j] = 1;
                color[j + 1] = 0;
                color[j + 2] = 0;
                color[j + 3] = 1;
            }
            else if (min <= i && i < max / 4) {
                color[j] = 0;//红色分量为0
                color[j + 1] = i*4/ max;//根据温度数据增加绿色分量，0-1变化
                color[j + 2] = 1;//纯蓝色
                color[j + 3] = 1;//透明度为1
            } else if (max / 4 <= i && i < max / 2) {//这个范围为浅蓝色到绿色，【0，1，1】-->[0,1,0],这个过程当数据越大就越少的蓝色分量，这样就向绿色过渡
                color[j] = 0;//红色分量为0
                color[j + 1] = 1;//绿色分量为1
                color[j + 2] = 1-(i-split) *4/ max;//根据温度数值的大小，去除相应的绿色, 1-0变化
                color[j + 3] = 1;
            } else if (max / 2 <= i &&i < max * 3 / 4) {//这个范围为绿色到黄色，【0，1，0】-->[1，1，0],这个过程通过增加红色的分量可以实现绿色到黄色的过渡
                color[j] = (i-2*split) *4/max;//变化范围0-1
                color[j + 1] = 1;
                color[j + 2] = 0;
                color[j + 3] = 1;
            } else {//这个范围为黄色到红色的过渡，【1，1，0】-->【1，0，0】,这个过程通过减少绿色的分量就可以达到黄色到红色的颜色过渡
                color[j] = 1;
                color[j + 1] = 1-(i-3*split)*4/ max;//变化范围1-0；
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
     * @param data 待适配的数据
     * @param w 三维中的光纤的点的个数
     * @return 适配后的数据，返回的数据长度与三维场景中的光纤的长度是一样的
     */
    public float[] screenAdapter(float[] data, int w) {
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
