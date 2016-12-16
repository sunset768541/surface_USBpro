package com.example.datausb;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
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

import java.util.List;

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
    Button fiberAstartCalibrate;
    Button fiberBstartCalibrate;
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
        fiberAstartCalibrate = (Button) getActivity().findViewById(R.id.button8);
        fiberBstartCalibrate = (Button) getActivity().findViewById(R.id.button9);
        fiberAstartCalibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] tuba;
                int[] tuba1;
                String tem= fiberAclabricateTemperature.getText().toString().trim();
                if(TextUtils.isEmpty(tem)){
                    Toast.makeText(getActivity().getApplicationContext(), "请输入当前的标定温度", Toast.LENGTH_SHORT).show();
                }
                else {
                    Log.e("tt", fiberAclabricateTemperature.getText().toString());
                    final  String tablename="tube1data";//要建立表格的命字
                    //建立表格的sql命令，存在就不建立，不存在就建立
                    String stu_table = "create table if not exists tube1data(_id integer primary key autoincrement,calibtem INTEGER,tubedata text)";
                    DataBaseOperation.mDataBaseOperation.creatOrGetTable(stu_table);
                    try {
                        tuba = ((Main) getActivity()).get_TubeA1_data().getIntArray("tunnelAdata");
                        tuba1 = ((Main) getActivity()).get_TubeA1_data1().getIntArray("tunnelA1data");
                        final float [] PSA=new float[tuba.length];
                        for (int i=0;i<tuba.length;i++){
                            if(tuba[i]==0){
                                PSA[i]=0;
                            }

                            else {

                                PSA[i]=(float)tuba1[i]/tuba[i];
                                // Log.e("PAS",Float.valueOf(PSA[i]).toString());
                            }

                        }
                        new Thread(){
                            public void run(){
                                DataBaseOperation.mDataBaseOperation.updataDataBase(Integer.valueOf(fiberAclabricateTemperature.getText().toString()),PSA,tablename);
                                Looper.prepare();

                                Toast.makeText(getActivity().getApplicationContext(), "传感通道1标定完成", Toast.LENGTH_SHORT).show();
                                Looper.loop();

                            }
                        }.start();


                    }
                    catch (NullPointerException e){
                        Toast.makeText( getActivity().getApplicationContext(), "通道1无数据输入", Toast.LENGTH_SHORT).show();

                    }
                }

            }



        });
        fiberBstartCalibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] tubeb;
                int[] tubeb1;
                String tem= fiberBclabricateTemperature.getText().toString().trim();
                if(TextUtils.isEmpty(tem)){
                    Toast.makeText(getActivity().getApplicationContext(), "请输入当前的标定温度", Toast.LENGTH_SHORT).show();
                }
                else {
                    final  String tablename = "tube2data";
                    String stu_table = "create table if not exists tube2data(_id integer primary key autoincrement,calibtem INTEGER,tubedata text)";
                    DataBaseOperation.mDataBaseOperation.creatOrGetTable(stu_table);
                    try{
                        tubeb = ((Main) getActivity()).get_TubeA1_data2().getIntArray("tunnelBdata");
                        tubeb1 = ((Main) getActivity()).get_TubeA1_data3().getIntArray("tunnelB1data");
                        final     float [] PSA1=new float[tubeb.length];
                        for (int i=0;i<tubeb.length;i++){
                            if(tubeb[i]==0){
                                PSA1[i]=0;
                            }
                            else PSA1[i]=(float)tubeb1[i]/tubeb[i];
                        }

                        //标定直接计算出P（SA）储存。
                        new Thread(){
                            public void run(){
                                DataBaseOperation.mDataBaseOperation.updataDataBase(Integer.valueOf(fiberAclabricateTemperature.getText().toString()),PSA1,tablename);
                                Looper.prepare();

                                Toast.makeText(getActivity().getApplicationContext(), "传感通道2标定完成", Toast.LENGTH_SHORT).show();
                                Looper.loop();

                            }
                        }.start();

                    }

                    catch (NullPointerException e){
                        Toast.makeText(getActivity().getApplicationContext(), "通道2无数据输入", Toast.LENGTH_SHORT).show();

                    }


                }

            }
        });
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
        float[]tp1;
        float[]tp2;
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
                        //下面的语句是从Activity中获取数据
                        int[] tuba = ((Main) getActivity()).get_TubeA1_data().getIntArray("tunnelAdata");
                        int[] tuba1 = ((Main) getActivity()).get_TubeA1_data1().getIntArray("tunnelA1data");
                        int[] tubeb = ((Main) getActivity()).get_TubeA1_data2().getIntArray("tunnelBdata");
                        int[] tubeb1 = ((Main) getActivity()).get_TubeA1_data3().getIntArray("tunnelB1data");
                        /**
                         * 下面是根据通道1234的数据计算标定光功率的PSA
                         */
                        float [] PSA1=new float[tuba.length];
                        float [] PSA2=new float[tuba.length];
                        for (int i=0;i<tuba.length;i++){
                            if(tuba[i]==0){
                                PSA1[i]=0;
                            }
                            else
                            {
                                PSA1[i]=(float)tuba1[i]/tuba[i];
                            }
                            if(tubeb[i]==0){
                                PSA2[i]=0;
                            }
                            else PSA2[i]=(float)tubeb1[i]/tubeb[i];
                        }

                        /**
                         * 定义了两支画笔
                         * paxis用来画横纵坐标轴
                         * axe用来绘制坐标轴中的坐标
                         */
                        Paint paxis = new Paint();
                        Paint axe = new Paint();
                        Paint zuobioa = new Paint();
                        axe.setARGB(70, 0, 189, 30);
                        axe.setStyle(Paint.Style.STROKE);
                        axe.setStrokeWidth(1);
                        paxis.setARGB(255, 83, 83, 83);
                        paxis.setStyle(Paint.Style.STROKE);
                        paxis.setStrokeWidth(3);
                        zuobioa.setColor(Color.WHITE);
                        zuobioa.setStyle(Paint.Style.FILL);
                        zuobioa.setTextAlign(Paint.Align.CENTER);
                        zuobioa.setTextSize(10);

                        /**
                         * c为锁定suface时获得的一个画布，我们可以在上面画图
                         */
                        Canvas c = holder.lockCanvas();
                        /**
                         * 绘制整个画布的颜色
                         */
                        c.drawRGB(15, 15, 15);
                        /**
                         * 绘制横纵坐标轴
                         */
                        c.drawLine(40, 20, 40, showLineViewHeigth - 40, paxis);
                        c.drawText("n", 40, 10, zuobioa);
                        c.drawText("m", showLineViewWidth - 10, showLineViewHeigth - 20, zuobioa);
                        c.drawLine(40, showLineViewHeigth - 40, showLineViewWidth - 10, showLineViewHeigth - 40, paxis);//绘制坐标轴
                        /**
                         * 绘制横纵轴各画ci条线
                         */
                        int ci = 21;
                        for (int i = 0; i < ci; i++) {
                            float y = i * maxnum / (ci - 1);
                            float x = i * fiberLength / (ci - 1);
                            /**
                             * (0,0)-------------------------------------->
                             *     |
                             *     | (40,k)----------------------(showLineSurfaceViewWidth-40,k)
                             *     |
                             *     | (40,k)----------------------(showLineSurfaceViewWidth-40,k)
                             *     |
                             *     | (40,k)----------------------(showLineSurfaceViewWidth-40,k)
                             *     |
                             *     | (40,k)----------------------(showLineSurfaceViewWidth-40,k)
                             *     |
                             *     | (40,k)----------------------(showLineSurfaceViewWidth-40,k)
                             *     |
                             *     | (40,k)----------------------(showLineSurfaceViewWidth-40,k)
                             *     |
                             *     | (40,k)----------------------(showLineSurfaceViewWidth-40,k)
                             */
                            float k = showLineViewHeigth - (i * (showLineViewHeigth - 40) / ci + 40);//画横轴直线需要的y坐标

                            /**
                             * (m,showLineViewHeigth/ci)
                             *     |        |       |       |       |
                             *     |        |       |       |       |
                             *     |        |       |       |       |
                             *     |        |       |       |       |
                             *     |        |       |       |       |
                             *     |        |       |       |       |
                             *     |        |       |       |       |
                             * (m,showLineViewHeigth-40)
                             */
                            float m = i * (showLineViewWidth - 40) / ci + 40;//纵轴间距
                            c.drawLine(40, k, showLineViewWidth - 40, k, axe);//画横轴(40,k,showLineSurfaceViewWidth-40,k)-->(x1,y1,x2,y2)画的横轴的长度是w-80,为每个循环得到画横轴的纵坐标的值
                            c.drawText(Integer.valueOf((int) y).toString(), (float) 18, (float) k, zuobioa);//在纵坐标上画字符
                            c.drawText(Integer.valueOf((int) x).toString(), (float) m, (float) (showLineViewHeigth - 10), zuobioa);//在横坐标上画字符
                            c.drawLine(m, showLineViewHeigth / (ci), m, showLineViewHeigth - 40, axe);//画纵轴m为每次画纵轴的x坐标，2h-40-showLineViewHeigth/ci为该纵轴的长度
                        }


                        /**
                         * 绘制各个通道的图像
                         * 共新建4个画笔和4个路径
                         */
                        synchronized (holder) {
                            Path p1 = new Path();
                            Path p2 = new Path();


                            Paint tube1 = new Paint();
                            Paint tube2 = new Paint();

                            tube1.setColor(Color.RED);
                            tube1.setStyle(Paint.Style.STROKE);
                            tube1.setAntiAlias(true);
                            tube1.setStrokeWidth(1);
                            PathEffect pe1 = new CornerPathEffect(10);
                            tube1.setPathEffect(pe1);

                            tube2.setColor(Color.CYAN);
                            tube2.setStyle(Paint.Style.STROKE);
                            tube2.setAntiAlias(true);
                            tube2.setStrokeWidth(1);
                            tube2.setPathEffect(pe1);
                            /**
                             *
                              */

                            tp1=adapterArray(PSA1,40,showLineViewWidth-40,20,showLineViewHeigth);
                            tp2=adapterArray(PSA2,40,showLineViewWidth-40,20,showLineViewHeigth);

                            drawPath(c,tp1,tube1,true);
                            drawPath(c,tp2,tube2,true);
                            Log.e("标定熟悉","时间");




/**
 *
 *
 *
**/


//                            float[]hh1=DisplayAdapterUtil.arrayInterpolation(showLineViewWidth -80,PSA1);
//                            float[]hh2=DisplayAdapterUtil.arrayInterpolation(showLineViewWidth -80,PSA2);
//                            float [] adp1=DisplayAdapterUtil.displyViewWidthAdapter(hh1, showLineViewWidth -80);
//                            float [] adp2=DisplayAdapterUtil.displyViewWidthAdapter(hh2, showLineViewWidth -80);
//                            p1.moveTo(0, -adp1[0]-20);
//                            p2.moveTo(0, -adp2[0]-20);
//                            for (int i = 1; i < adp1.length; i++) {
//                                p1.lineTo(i,-adp1[i]-20);
//                                p2.lineTo(i,-adp2[i]-20);
//                            }
//                            c.translate(40, (float) showLineViewHeigth-40);
//                            c.drawPath(p1, tube1);
//                            c.drawPath(p2, tube2);
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
        private float[] adapterArray(float []needAdapter,int mariginLeft,int mariginRight,int marginBottom,int mariginTop){
            float []afterAdapter=new float[needAdapter.length*2];
            int scale=0;
            double minX=0;
            double maxX=8192;
            double minY=0;
            double maxY=65536;
            double xPixelsPerUnit= (mariginRight - mariginLeft) / (maxX - minX);//maxX为横坐标的最大值，minX为横坐标最小值
            double yPixelsPerUnit= (float) ((marginBottom - mariginTop) / (maxY - minY));
            int mm=0;
            for (int i=0;i<needAdapter.length;i++){
                afterAdapter[mm]=(float) (mariginLeft + xPixelsPerUnit* (i - minX));
                afterAdapter[mm+1]=(float) (marginBottom - yPixelsPerUnit* (needAdapter[i] - minY));
                mm=mm+2;
            }
            return afterAdapter;
        }
        private  float[] calculateDrawPoints(float p1x, float p1y, float p2x, float p2y,
                                             int screenHeight, int screenWidth) {
            float drawP1x;
            float drawP1y;
            float drawP2x;
            float drawP2y;
            if (p1y > screenHeight) {
                // Intersection with the top of the screen
                float m = (p2y - p1y) / (p2x - p1x);
                drawP1x = (screenHeight - p1y + m * p1x) / m;
                drawP1y = screenHeight;

                if (drawP1x < 0) {
                    // If Intersection is left of the screen we calculate the intersection
                    // with the left border
                    drawP1x = 0;
                    drawP1y = p1y - m * p1x;
                } else if (drawP1x > screenWidth) {
                    // If Intersection is right of the screen we calculate the intersection
                    // with the right border
                    drawP1x = screenWidth;
                    drawP1y = m * screenWidth + p1y - m * p1x;
                }
            } else if (p1y < 0) {
                float m = (p2y - p1y) / (p2x - p1x);
                drawP1x = (-p1y + m * p1x) / m;
                drawP1y = 0;
                if (drawP1x < 0) {
                    drawP1x = 0;
                    drawP1y = p1y - m * p1x;
                } else if (drawP1x > screenWidth) {
                    drawP1x = screenWidth;
                    drawP1y = m * screenWidth + p1y - m * p1x;
                }
            } else {
                // If the point is in the screen use it
                drawP1x = p1x;
                drawP1y = p1y;
            }

            if (p2y > screenHeight) {
                float m = (p2y - p1y) / (p2x - p1x);
                drawP2x = (screenHeight - p1y + m * p1x) / m;
                drawP2y = screenHeight;
                if (drawP2x < 0) {
                    drawP2x = 0;
                    drawP2y = p1y - m * p1x;
                } else if (drawP2x > screenWidth) {
                    drawP2x = screenWidth;
                    drawP2y = m * screenWidth + p1y - m * p1x;
                }
            } else if (p2y < 0) {
                float m = (p2y - p1y) / (p2x - p1x);
                drawP2x = (-p1y + m * p1x) / m;
                drawP2y = 0;
                if (drawP2x < 0) {
                    drawP2x = 0;
                    drawP2y = p1y - m * p1x;
                } else if (drawP2x > screenWidth) {
                    drawP2x = screenWidth;
                    drawP2y = m * screenWidth + p1y - m * p1x;
                }
            } else {
                // If the point is in the screen use it
                drawP2x = p2x;
                drawP2y = p2y;
            }

            return new float[] { drawP1x, drawP1y, drawP2x, drawP2y };
        }


        protected void drawPath(Canvas canvas, float[] points, Paint paint, boolean circular) {
            Path path = new Path();
            int height = canvas.getHeight();
            int width = canvas.getWidth();

            float[] tempDrawPoints;
            if (points.length < 4) {
                return;
            }
            tempDrawPoints = calculateDrawPoints(points[0], points[1], points[2], points[3], height, width);
            path.moveTo(tempDrawPoints[0], tempDrawPoints[1]);
            path.lineTo(tempDrawPoints[2], tempDrawPoints[3]);

            int length = points.length;
            for (int i = 4; i < length; i += 2) {
                if ((points[i - 1] < 0 && points[i + 1] < 0)
                        || (points[i - 1] > height && points[i + 1] > height)) {
                    continue;
                }
                tempDrawPoints = calculateDrawPoints(points[i - 2], points[i - 1], points[i], points[i + 1],
                        height, width);
                if (!circular) {
                    path.moveTo(tempDrawPoints[0], tempDrawPoints[1]);
                }
                path.lineTo(tempDrawPoints[2], tempDrawPoints[3]);
            }
            if (circular) {
                path.lineTo(points[0], points[1]);
            }
            canvas.drawPath(path, paint);
        }
    }

}


