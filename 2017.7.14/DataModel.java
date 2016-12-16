package com.example.datausb;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Matrix;
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
import android.widget.FrameLayout;

import com.example.datausb.achartengine.ChartFactory;
import com.example.datausb.achartengine.GraphicalView;
import com.example.datausb.achartengine.chart.PointStyle;
import com.example.datausb.achartengine.model.XYMultipleSeriesDataset;
import com.example.datausb.achartengine.model.XYSeries;
import com.example.datausb.achartengine.renderer.XYMultipleSeriesRenderer;
import com.example.datausb.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by sunset on 15/11/19.
 */
public class DataModel extends android.app.Fragment {
    /**
     * 定义一个SurfaceHolder用来管理surface
     */
    private SurfaceHolder holder;
    public GraphicalView xyPlot;
    private FrameLayout dataModelFram;
    final String[] titles = new String[] { "Crete", "Corfu", "Thassos", "Skiathos" };
    final List<int[]> x = new ArrayList<int[]>();


    /**
     * 这个函数的作用是使Activity可以唤醒fragment中的显示线程
     */
    public void wakeup() {
        ((Main) getActivity()).dataObj.notifyAll();
    }
    public double[] creatdoubel(int arraylength){
        double []da=new double[arraylength];
        for (int k=0;k<arraylength;k++){
            da[k]=(Math.random()*(10.5))+Math.random()*5.2+Math.random()*5.8;
        }
        return da;
    }
    public int[] createX(int length){
        int[] x=new int[length];
        for (int k=0;k<x.length;k++){
            x[k]=k+1;
        }
        return x;
    }
    private XYMultipleSeriesRenderer buildRenderer(int[] colors, PointStyle[] styles) {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        setRenderer(renderer, colors, styles);
        return renderer;
    }

    private void setRenderer(XYMultipleSeriesRenderer renderer, int[] colors, PointStyle[] styles) {
        renderer.setAxisTitleTextSize(16);
        renderer.setChartTitleTextSize(20);
        renderer.setLabelsTextSize(15);
        renderer.setLegendTextSize(15);
        renderer.setPointSize(5f);
        renderer.setMargins(new int[] {30,50, 10, 20 });//上右下
        int length = colors.length;
        for (int i = 0; i < length; i++) {
            XYSeriesRenderer r = new XYSeriesRenderer();
            r.setColor(colors[i]);
            r.setPointStyle(styles[i]);
            renderer.addSeriesRenderer(r);
        }
    }

    private void setChartSettings(XYMultipleSeriesRenderer renderer, String title, String xTitle, String yTitle, float xMin, float xMax, float yMin, float yMax, int axesColor, int labelsColor) {
        renderer.setChartTitle(title);
        renderer.setXTitle(xTitle);
        renderer.setYTitle(yTitle);
        renderer.setXAxisMin(xMin);
        renderer.setXAxisMax(xMax);
        renderer.setYAxisMin(yMin);
        renderer.setYAxisMax(yMax);
        renderer.setAxesColor(axesColor);
        renderer.setLabelsColor(labelsColor);
        renderer.setXLabels(12);
        renderer.setYLabels(10);
        renderer.setShowGrid(true);
        renderer.setXLabelsAlign(Paint.Align.RIGHT);
        renderer.setYLabelsAlign(Paint.Align.RIGHT);
    }

    private XYMultipleSeriesDataset buildDataset(String[] titles, List<int[]> xValues, List<int[]> yValues) {
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        addXYSeries(dataset, titles, xValues, yValues, 0);
        return dataset;
    }

    private void addXYSeries(XYMultipleSeriesDataset dataset, String[] titles, List<int[]> xValues, List<int[]> yValues, int scale) {//耗时
        int length = titles.length;
        for (int i = 0; i < length; i++) {
            XYSeries series = new XYSeries(titles[i], scale);
            int[] xV = xValues.get(i);//取得所有第一个x
            int[] yV = yValues.get(i);
            int seriesLength = xV.length;
            for (int k = 0; k < seriesLength; k++) {
                series.add(xV[k], yV[k]);
            }
            dataset.addSeries(series);
        }
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.datamodel, container, false);
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);//
        dataModelFram=(FrameLayout)getActivity().findViewById(R.id.datamodelfram);
        for (int i = 0; i < titles.length; i++) {
            x.add(createX(8192));
        }
        final List<int[]> values = new ArrayList<int[]>();
        values.add(new int[8192]);
        values.add(new int[8192]);
        values.add(new int[8192]);
        values.add(new int[8192]);
        int[] colors = new int[] { Color.BLUE, Color.GREEN, Color.CYAN, Color.YELLOW };
        PointStyle[] styles = new PointStyle[] { PointStyle.POINT, PointStyle.POINT, PointStyle.POINT, PointStyle.POINT };
        final XYMultipleSeriesRenderer renderer = buildRenderer(colors,styles);
        renderer.setBackgroundColor(Color.argb(255,15,15,15));
        int length = renderer.getSeriesRendererCount();
        for (int i = 0; i < length; i++) {
            ((XYSeriesRenderer) renderer.getSeriesRendererAt(i)).setFillPoints(true);
        }
        setChartSettings(renderer, "数据模式", "距离(m)", "数据值", 0, 8500, 0, 68000, Color.LTGRAY, Color.LTGRAY);
        //renderer.setZoomButtonsVisible(true);
        renderer.setPanLimits(new double[] { 0, 20, 0, 40 });
        renderer.setZoomLimits(new double[] { -10, 20, -10, 40 });

        xyPlot = ChartFactory.getLineChartView(getActivity(), buildDataset(titles, x, values), renderer);
        xyPlot.setBackgroundColor(Color.argb(255,15,15,15));
        dataModelFram.addView(xyPlot);
        new dataThread().start();

    }

    /**
     * 一个集成Surfaceview并实现了SurfaceHolder.Callback方法的的类
     */



    /**
     * 一个绘图线程类
     */
    class dataThread extends Thread {

        public dataThread() {

        }

        public void run() {
            try {//捕获线程运行中切换界面而产生的的空指针异常，防止程序崩溃。

                while (!((Main) getActivity()).stopDataModelThread) {//while中的语句是保障可以正常的结束线程

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
                       int[] tunnelAdata = ((Main) getActivity()).get_TubeA1_data().getIntArray("tunnelAdata");
                        int[] tunnelA1data = ((Main) getActivity()).get_TubeA1_data1().getIntArray("tunnelA1data");
                        int[]  tunnelBdata = ((Main) getActivity()).get_TubeA1_data2().getIntArray("tunnelBdata");
                        int[] tunnelB1data = ((Main) getActivity()).get_TubeA1_data3().getIntArray("tunnelB1data");


                            final List<int[]> values = new ArrayList<int[]>();
                            values.add(tunnelAdata);
                            values.add(tunnelA1data);
                            values.add(tunnelBdata);
                            values.add(tunnelB1data);
                            xyPlot.setDatadd(buildDataset(titles,x,values));
                           // Log.e("每次刷新时间","-----刷新");//20-30ms一个周期
//                            xyPlot.setDatadd(getpoint(tunnelAdata));//200mms一个周期
//                            xyPlot.setDatadd(getpoint(tunnelA1data));//200mms一个周期
//                            xyPlot.setDatadd(getpoint(tunnelBdata));//200mms一个周期
//                            xyPlot.setDatadd(getpoint(tunnelB1data));//200mms一个周期
                            xyPlot.repaint();//600ms一个周期xx
                            ((Main) getActivity()).dataObj.flag1 = false;
                            ((Main) getActivity()).wakeUpAllMainThread();



                    }


                }
            } catch (NullPointerException e) {
                Log.d("数据模式空指针异常", Log.getStackTraceString(e));


            }

        }
private float[] getpoint(int[] da){
    float[]ff=new float[da.length*2];
    int jj=0;
    for (int kk=0;kk<da.length;kk++){
        ff[jj]=kk;
        ff[jj+1]=da[kk];
        jj=jj+2;
    }
    return ff;
}
    }
}


