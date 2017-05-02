package com.example.datausb;

import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.example.datausb.Fiber.FiberA;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by sunset on 2016/12/23.
 * 目前单通道
 */

public class MapModel extends android.app.Fragment implements BaiduMap.OnMapDrawFrameCallback {
    MapView mMapView = null;
    BaiduMap mBaiduMap;
    private float[] vertexs;
    private FloatBuffer vertexBuffer;
    private FloatBuffer colorBuffer;
    MapStatus mapStatus;
    float [] colors;
    public List<LatLng> pts = new ArrayList<>();
    LatLng center;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.mapmodel, container, false);
    }
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);//
        center=new LatLng(SystemParameter.centerLatitude,SystemParameter.centerLongitude);
        Thread thread=new Thread(new ReadLatLng());//加载坐标文件；
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        thread.start();
        try {
            thread.join();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        mMapView = (MapView)getActivity().findViewById(R.id.view);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
        mBaiduMap.setOnMapDrawFrameCallback(this);
        final MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLngZoom(center,15);
        mBaiduMap.animateMapStatus(mapStatusUpdate);//以动画的方式更新地图，可以用来主动的刷新地图，当计算完颜色数据后，通过这个来刷新地图
        //启动MapRenderThread线程
    }
    public void refreshMap(){//调用这个方法实现地图的从新绘制
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLngZoom(center,15);
        mBaiduMap.animateMapStatus(mapStatusUpdate);
    }
    public void onMapDrawFrame(GL10 gl, MapStatus drawingMapStatus) {//当地图状态改变的时候调用，当颜色数据处理好了后通过对地图状态做出改变刷新
        if (mBaiduMap.getProjection() != null) {
            calPolylinePoint(drawingMapStatus);
            drawPolyline(gl, vertexBuffer, 5, 4096);
        }
      //  Log.e("onMapdrawo","地图模式");
    }
    public void calPolylinePoint(MapStatus mspStatus) {
        PointF[] polyPoints = new PointF[pts.size()];
        vertexs = new float[3 * pts.size()];
        int i = 0;
        for (LatLng xy : pts) {
            polyPoints[i] = mBaiduMap.getProjection().toOpenGLLocation(xy,mspStatus);
            vertexs[i * 3] = polyPoints[i].x;
            vertexs[i * 3 + 1] = polyPoints[i].y;
            vertexs[i * 3 + 2] = 0.0f;
            i++;
        }

        vertexBuffer = makeFloatBuffer(vertexs);
        getCO();
    }
    private void getCO(){
        colors=colorProcess(testColor());//产生颜色数据，testColor为测试用。
        ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length * 4);
        cbb.order(ByteOrder.nativeOrder());
        colorBuffer = cbb.asFloatBuffer();
        colorBuffer.put(colors);
        colorBuffer.position(0);
    }
    private FloatBuffer makeFloatBuffer(float[] fs) {
        ByteBuffer bb = ByteBuffer.allocateDirect(fs.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(fs);
        fb.position(0);
        return fb;
    }
    private void drawPolyline(GL10 gl, FloatBuffer lineVertexBuffer,
                              float lineWidth, int pointSize) {
        //gl.glEnable(GL10.GL_BLEND);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY); // NEW LINE ADDED.
        //gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);


        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, lineVertexBuffer);
        //gl.glColor4f(colorR, colorG, colorB, colorA);

        gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorBuffer); // NEW LINE ADDED.设置每个点的颜色
        gl.glLineWidth(lineWidth);
        gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, pointSize);

        //gl.glDisable(GL10.GL_BLEND);
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    }
    class ReadLatLng implements Runnable{
        @Override
        public void run() {
            try {
                InputStream in;
                if (SystemParameter.axipath==null)
                 in=getActivity().getAssets().open("locationdata.txt");//fname is the location file name
                else in= new FileInputStream(SystemParameter.axipath);
                InputStreamReader isr=new InputStreamReader(in);
                BufferedReader br=new BufferedReader(isr);
                String temps=null;
                int t=1;
                while((temps=br.readLine())!=null)
                {

                    String[] tempsa=temps.split("[ ]+");
                    pts.add(new LatLng(Double.parseDouble(tempsa[0]),Double.parseDouble(tempsa[1])));
                    t++;
                }
                in.close();
                Log.e("读取位置数据文件","完成,共读取"+Integer.valueOf(t).toString()+"个位置坐标");
            }
            catch (IOException e){
                Log.e("IO出错","读取位置数据文件出错");
            }
        }
    }
    public float[] testColor() {//模拟温度数据测试光纤的颜色
        float[] x = new float[4096];
        Random r=new Random();
        for (int i = 0; i < 4095; i++) {
            if (i>2040&&i<2048){
                    x[i]=90;
            }else
            x[i]=r.nextInt(20)+20;
        }
        return x;
    }
    public float[] colorProcess(float[] data) {
        float min=0;
        float max=100;
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
    class MapRendderThread extends Thread {
        public void run() {
            try {//捕获线程运行中切换界面而产生的的空指针异常，防止程序崩溃。
                while (!((Main) getActivity()).stopMapModelThread) {
                    synchronized (((Main) getActivity()).dataObj) {//所有的等待和唤醒的锁都是同一个，这里选用了Activity中的一个对对象
                        /**
                         * 如果当标志位为false这个线程开始等待
                         */
                        if (!((Main) getActivity()).dataObj.flag1)
                            try {
                                ((Main) getActivity()).dataObj.wait();
                            } catch (InterruptedException ex) {
                                Log.e("地图模式",Log.getStackTraceString(ex));
                            }
                        else {
                            ((Main) getActivity()).dataObj.notifyAll();
                        }
                         colors = colorProcess(FiberA.createFiberA().calculateTempreture());//获取通道A的数据
                         refreshMap();
                        ((Main) getActivity()).dataObj.flag1 = false;
                        ((Main) getActivity()).wakeUpAllMainThread();

                    }
                }
            } catch (NullPointerException e) {
                Log.d("MapModel", Log.getStackTraceString(e));
            }
        }
    }
}
