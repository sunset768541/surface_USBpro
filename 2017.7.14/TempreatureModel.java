package com.example.datausb;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
//import android.sutube1ort.v4.atube1.Fragment;
import android.os.Bundle;
//import android.atube1.Fragment;
//import android.sutube1ort.v4.atube1.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

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
    private SurfaceHolder holder;
   float [] caliPSA;
    float[] caliPSB;
    /**
     * 这个函数的作用是使Activity可以唤醒fragment中的显示线程
     */
    public void wakeup() {
        ((Main) getActivity()).dataObj.notifyAll();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tempreaturemodel, container, false);
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);//
        try{
            caliPSA=DataBaseOperation.mDataBaseOperation.getFromDataBase("tube1data");
            caliPSB=DataBaseOperation.mDataBaseOperation.getFromDataBase("tube2data");
        }
        catch (Exception e){
            Toast.makeText(getActivity().getApplicationContext(), "标定数据不存在，请先在标定模式下进行标定", Toast.LENGTH_SHORT).show();

        }

        /**
         * 获得布局中的surfaceview
         */
        SurfaceView sur = (SurfaceView) getActivity().findViewById(R.id.surb1);
        /**
         * 将holder和surfaceview绑定
         */
        holder = sur.getHolder();
        /**
         * 实例化一个surfaceview
         */
        drawLineSurface v1 = new drawLineSurface(getActivity(), holder, sur);
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
         * @param context
         * @param holder1，传入holder，给绘图线程使用
         * @param surfaceView，传入sur使绘图线程获得当前surfaceview的大小
         */
        public drawLineSurface(Context context, SurfaceHolder holder1, SurfaceView surfaceView) {
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
        float fiberLength = 2048;
        float maxnum = 16384;
        List<Float> T1p=new ArrayList<>();
        List<Float> T2p=new ArrayList<>();
        float[]tp1;
        float[]tp2;
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

                            }
                        else {
                            ((Main) getActivity()).dataObj.notifyAll();
                        }
                        //下面的语句是从Activity中获取数据
                        int[] tuba = ((Main) getActivity()).get_TubeA1_data().getIntArray("tunnelAdata");
                        int[] tuba1 = ((Main) getActivity()).get_TubeA1_data1().getIntArray("tunnelA1data");
                        int[] tubeb = ((Main) getActivity()).get_TubeA1_data2().getIntArray("tunnelBdata");
                        int[] tubeb1 = ((Main) getActivity()).get_TubeA1_data3().getIntArray("tunnelB1data");
                        float[] T1=new float[tuba.length];
                        float[] T2=new float[tuba.length];
                        float [] PSA1=new float[tuba.length];
                        float [] PSA2=new float[tuba.length];
                        for (int i=0;i<tuba.length;i++){
                            if(tuba[i]==0){
                                PSA1[i]=0;
                            }
                            else PSA1[i]=(float)tuba1[i]/tuba[i];
                            if(tubeb[i]==0){
                                PSA2[i]=0;
                            }
                            else PSA2[i]=(float)tubeb1[i]/tubeb[i];
                        }
                        /**
                         * 由公式计算出温度
                         */
                        for (int i=0;i<tuba.length;i++){
                            double bb1=(double)PSA1[i]/caliPSA[i];
                            double bb2=(double)PSA2[i]/caliPSB[i];
                            float tt1=(float)(Math.log(bb1)+1/caliPSA[caliPSA.length-1]);
                            float tt2=(float)(Math.log(bb2)+1/caliPSB[caliPSB.length-1]);

                            T1[i]=1/tt1;
                            T2[i]=1/tt2;
                        }
                        /**
                         * 定义了两支画笔
                         * paxis用来画横纵坐标轴
                         * axe用来绘制坐标轴中的坐标
                         */
                        Paint paxis = new Paint();
                        Paint axe = new Paint();
                        Paint zuobioa = new Paint();

                        axe.setARGB(255, 83, 83, 83);
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
                        c.drawLine(40, 20, 40, showLineSurfaceViewHeught - 40, paxis);
                        c.drawText("n", 40, 10, zuobioa);
                        c.drawText("m", showLineSurfaceViewWidth - 10, showLineSurfaceViewHeught - 20, zuobioa);
                        c.drawLine(40, showLineSurfaceViewHeught - 40, showLineSurfaceViewWidth - 10, showLineSurfaceViewHeught - 40, paxis);//绘制坐标轴
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
                            float k = showLineSurfaceViewHeught - (i * (showLineSurfaceViewHeught - 40) / ci + 40);//画横轴直线需要的y坐标

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
                            float m = i * (showLineSurfaceViewWidth - 40) / ci + 40;//纵轴间距
                            c.drawLine(40, k, showLineSurfaceViewWidth - 40, k, axe);//画横轴(40,k,showLineSurfaceViewWidth-40,k)-->(x1,y1,x2,y2)画的横轴的长度是w-80,为每个循环得到画横轴的纵坐标的值
                            c.drawText(Integer.valueOf((int) y).toString(), (float) 18, (float) k, zuobioa);//在纵坐标上画字符
                            c.drawText(Integer.valueOf((int) x).toString(), (float) m, (float) (showLineSurfaceViewHeught - 10), zuobioa);//在横坐标上画字符
                            c.drawLine(m, showLineSurfaceViewHeught / (ci), m, showLineSurfaceViewHeught - 40, axe);//画纵轴m为每次画纵轴的x坐标，2h-40-showLineViewHeigth/ci为该纵轴的长度
                        }


                        /**
                         * 绘制各个通道的图像
                         * 共新建4个画笔和4个路径
                         */
                        synchronized (holder) {
                            Path fiber1Path = new Path();
                            Path fiber2Path = new Path();


                            Paint fiber1Paint = new Paint();
                            Paint fiber2Paint = new Paint();
                            PathEffect pathEffect = new CornerPathEffect(10);

                            fiber1Paint.setColor(Color.RED);
                            fiber1Paint.setStyle(Paint.Style.STROKE);
                            fiber1Paint.setAntiAlias(true);
                            fiber1Paint.setStrokeWidth(1);
                            fiber1Paint.setPathEffect(pathEffect);

                            fiber2Paint.setColor(Color.GREEN);
                            fiber2Paint.setStyle(Paint.Style.STROKE);
                            fiber2Paint.setAntiAlias(true);
                            fiber2Paint.setStrokeWidth(1);
                            fiber2Paint.setPathEffect(pathEffect);

                            c.translate(40, (float) showLineSurfaceViewHeught -40);
                            tp1=new float[T1.length*2];
                            tp2=new float[T1.length*2];
                            int jj=0;
                            for (int kk=0;kk<T1.length;kk++){
                                tp1[jj]=kk;
                                tp2[jj]=kk;
                                tp1[jj+1]=T1[kk];
                                tp2[jj+1]=T2[kk];
                                jj=jj+2;
//                                T1p.add((float)kk);
//                                T1p.add(T1[kk]);
//                                T2p.add((float)kk);
//                                T2p.add(T2[kk]);
                            }
                            drawPath(c,tp1,fiber1Paint,true);
                            drawPath(c,tp2,fiber2Paint,true);
                            Log.e("熟悉","时间");
//                            float[]hh1=DisplayAdapterUtil.arrayInterpolation(showLineSurfaceViewWidth -80,T1);//插值
//                            float[]hh2=DisplayAdapterUtil.arrayInterpolation(showLineSurfaceViewWidth -80,T2);
//                            float [] adp1=DisplayAdapterUtil.displyViewWidthAdapter(hh1, showLineSurfaceViewWidth -80);//适配
//                            float [] adp2=DisplayAdapterUtil.displyViewWidthAdapter(hh2, showLineSurfaceViewWidth -80);
//                            fiber1Path.moveTo(0, -adp1[0]-20);
//                            fiber2Path.moveTo(0, -adp2[0]-20);
//                            for (int i = 1; i < adp1.length; i++) {
//                                fiber1Path.lineTo(i, -adp1[i]-20);
//                                fiber2Path.lineTo(i, -adp2[i]-20);
//                            }
//                            c.drawPath(fiber1Path, fiber1Paint);
//                            c.drawPath(fiber2Path, fiber2Paint);


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


                }
                }
            catch (NullPointerException e) {
                Log.d("tempretureModel", Log.getStackTraceString(e));


            }

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

        /**
         * The graphical representation of a path.
         *
         * @param canvas the canvas to paint to
         * @param points the points that are contained in the path to paint
         * @param paint the paint to be used for painting
         * @param circular if the path ends with the start point
         */
        protected void drawPath(Canvas canvas, List<Float> points, Paint paint, boolean circular) {
            Path path = new Path();
            int height = showLineSurfaceViewHeught;
            int width = showLineSurfaceViewWidth-80;

            float[] tempDrawPoints;
            if (points.size() < 4) {
                return;
            }
            tempDrawPoints = calculateDrawPoints(points.get(0), points.get(1), points.get(2),
                    points.get(3), height, width);
            path.moveTo(tempDrawPoints[0], tempDrawPoints[1]);
            path.lineTo(tempDrawPoints[2], tempDrawPoints[3]);

            int length = points.size();
           //  Log.e("chartpoint长度",Integer.valueOf(length).toString());
            for (int i = 4; i < length; i += 2) {
                if ((points.get(i - 1) < 0 && points.get(i + 1) < 0)
                        || (points.get(i - 1) > height && points.get(i + 1) > height)) {
                    continue;
                }
                tempDrawPoints = calculateDrawPoints(points.get(i - 2), points.get(i - 1), points.get(i),
                        points.get(i + 1), height, width);
                if (!circular) {
                    path.moveTo(tempDrawPoints[0], tempDrawPoints[1]);
                }
                path.lineTo(tempDrawPoints[2], tempDrawPoints[3]);
            }
            if (circular) {
                path.lineTo(points.get(0), points.get(1));
            }
            canvas.drawPath(path, paint);
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