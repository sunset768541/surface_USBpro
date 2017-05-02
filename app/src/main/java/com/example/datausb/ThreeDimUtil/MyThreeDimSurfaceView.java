package com.example.datausb.ThreeDimUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

import com.example.datausb.Main;
import com.example.datausb.R;
import com.example.datausb.SystemParameter;
import com.example.datausb.ThreeDimensionModel;
import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Loader;
import com.threed.jpct.Logger;
import com.threed.jpct.Object3D;
import com.threed.jpct.Polyline;
import com.threed.jpct.Primitives;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;
import com.threed.jpct.util.MemoryHelper;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

public class MyThreeDimSurfaceView extends GLSurfaceView
{
    Camera cam;
    Object3D[]myobj;
    FrameBuffer buffer=null;
    SimpleVector[] sp;
    private FrameBuffer fb = null;
    private World world = null;
    private float touchTurn = 0;
    private float touchTurnUp = 0;
    private float xpos = -1;
    private float ypos = -1;
    Texture tt;
    TextureManager tm;
    private Object3D cube = null;
    private int fps = 0;
    Bitmap bp;
    private Light sun = null;
    List<Polyline> myployline=new ArrayList<>();
    RGBColor[] mycolor;
    Random random;
    List<SimpleVector[]> ls=new ArrayList<>();
    public MyRenderer mRenderer;//������Ⱦ��
    public static String fiberModel="";
    public static String buildingModel="";
    private float mPreviousY;
    private float mPreviousX;
	TextView loadmodelstate;
    ThreeDimensionModel threeDimensionModel;
	public MyThreeDimSurfaceView(Context context,ThreeDimensionModel threeDimensionModel) {
        super(context);
   //     this.setEGLContextClientVersion(2);
        this.threeDimensionModel=threeDimensionModel;
        bp= Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.txww),0,0,64,64);
        tm= TextureManager.getInstance();
        tt=new Texture(bp);
//        tm.addTexture("my",tt);
        cube = Primitives.getCube(10);
        random=new Random();
        mRenderer = new MyRenderer();
        setRenderer(mRenderer);
    }

    public boolean onTouchEvent(MotionEvent me) {

        if (me.getAction() == MotionEvent.ACTION_DOWN) {
            xpos = me.getX();
            ypos = me.getY();
            return true;
        }

        if (me.getAction() == MotionEvent.ACTION_UP) {
            xpos = -1;
            ypos = -1;
            touchTurn = 0;
            touchTurnUp = 0;
            return true;
        }

        if (me.getAction() == MotionEvent.ACTION_MOVE) {
            float xd = me.getX() - xpos;
            float yd = me.getY() - ypos;

            xpos = me.getX();
            ypos = me.getY();

            touchTurn = yd / -50f;
            touchTurnUp = xd / -50f;
            return true;
        }

        try {
            Thread.sleep(15);
        } catch (Exception e) {
            // No need for this...
        }

        return super.onTouchEvent(me);
    }

    protected boolean isFullscreenOpaque() {
        return true;
    }
    class LoadEnvAndFiberModelThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                //  FileInputStream is=new FileInputStream("/storage/sdcard0/module/build.obj");
               // FileInputStream is=new FileInputStream("/sdcard/module/reaxi.obj");
                FileInputStream is=new FileInputStream(SystemParameter.envPath);
                Log.e("start","载入");
               // myobj= Loader.loadOBJ(is,null,1f);
                ThreeDimObjectCatch.myobj= Loader.loadOBJ(is,null,1f);
                Log.e("end","载入");
                is.close();
                Log.e("startload","载入line");
              //  FileInputStream il=new FileInputStream("/sdcard/module/lineop.obj");
                FileInputStream il=new FileInputStream(SystemParameter.fiberPath);
                //sp=LoadUtil.loadFromFile(il);
                ThreeDimObjectCatch.sp=LoadUtil.loadFromFile(il);
                ThreeDimObjectCatch.ls.clear();
                ThreeDimObjectCatch.myployline.clear();
                for (int i = 0; i< ThreeDimObjectCatch.sp.length-1; i++){
                    ThreeDimObjectCatch.ls.add(new SimpleVector[]{ThreeDimObjectCatch.sp[i], ThreeDimObjectCatch.sp[i+1]});
                   ThreeDimObjectCatch.myployline.add(new Polyline(ThreeDimObjectCatch.ls.get(i), RGBColor.BLUE));
                }
                //mycolor=new RGBColor[myployline.size()];
                ThreeDimObjectCatch.mcolor=new RGBColor[ThreeDimObjectCatch.myployline.size()];
                Log.e("startload","载入linefinifh");
                il.close();
                SystemParameter.preenvPath= SystemParameter.envPath;
                SystemParameter.prefiberPath= SystemParameter.fiberPath;
            }
            catch (Exception e){
                Log.e("载入obj",e.toString());
            }
        }
    }
    class RandomGenColorThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (true){
                for (int i=0;i<mycolor.length;i++){
                    RGBColor rc=new RGBColor(random.nextInt()*255,random.nextInt()*255,random.nextInt()*255);
                    mycolor[i]=rc;
                }
                try {
                    Thread.sleep(500);
                }
                catch (Exception e){

                }
            }
        }
    }
  public  class MyRenderer implements GLSurfaceView.Renderer {//The renderer will be called on a separate thread

        private long time = System.currentTimeMillis();
        public void setcolor(float [] colors){
            for (int i=0,j=0;i<mycolor.length;i++,j=j+4){
               // RGBColor rc=new RGBColor((int)colors[j]*255,(int)colors[j+1]*255,(int)colors[j+2]*255,255);
                RGBColor rc=new RGBColor(random.nextInt()*255,random.nextInt()*255,random.nextInt()*255);
                mycolor[i]=rc;
            }
        }

        public MyRenderer() {
        }

        public void onSurfaceChanged(GL10 gl, int w, int h) {
            if (buffer != null) {
                buffer.dispose();
            }
            world = new World();
            world.setAmbientLight(50, 50, 50);

            sun = new Light(world);
            sun.setIntensity(250, 250, 250);

            buffer=new FrameBuffer(gl,w,h);

            if (ThreeDimObjectCatch.myobj==null|(!SystemParameter.envPath.equals(SystemParameter.preenvPath))|!SystemParameter.fiberPath.equals(SystemParameter.prefiberPath)){

                //Log.e("判断",Boolean.valueOf(ThreeDimObjectCatch.myobj==null).toString()+" "+!ThreeDimObjectCatch.envPath.equals(ThreeDimObjectCatch.preenvPath))+" "+!ThreeDimObjectCatch.fiberPath.equals(ThreeDimObjectCatch.prefiberPath)));
            LoadEnvAndFiberModelThread lt=new LoadEnvAndFiberModelThread();//开启线程加载环境和光纤模型如果已经加载过一遍就不再开启这个线程加载
            lt.start();
            try {

                lt.join();//等待环境模型和光纤模型加载完成加载完成后将当前path和prepath置为一样

            }catch (Exception e){
                e.printStackTrace();
            }
            }
            myobj= ThreeDimObjectCatch.myobj;
            mycolor= ThreeDimObjectCatch.mcolor;
            sp= ThreeDimObjectCatch.sp;
            myployline= ThreeDimObjectCatch.myployline;
            ls= ThreeDimObjectCatch.ls;
            new RandomGenColorThread().start();
            for (int i=0;i<myobj.length;i++) {
                myobj[i].setCulling(false);
                myobj[i].setTransparency(-1);
                myobj[i].build();
                myobj[i].setCenter(new SimpleVector(0,0,0));
                world.addObject(myobj[i]);
                Log.e("我的buffer", Integer.valueOf(i).toString());
            }
            for(Polyline pl:myployline)
                world.addPolyline(pl);
            cam = world.getCamera();
            cam.moveCamera(Camera.CAMERA_MOVEOUT, 50);
            cam.lookAt(new SimpleVector(0,0,0));
            SimpleVector sv = new SimpleVector();
            sv.set(new SimpleVector(0,0,0));
            sv.y -= 300;
            sv.z -= 300;
            sun.setPosition(sv);
            Message msg1 = new Message();
            msg1.arg1=1;
            ((Main) (threeDimensionModel.getActivity())).handler.sendMessage(msg1);
            MemoryHelper.compact();
        }

        @Override
        public void onSurfaceCreated(GL10 gl10, javax.microedition.khronos.egl.EGLConfig eglConfig) {
            Log.e("","");
        }
        public void onDrawFrame(GL10 gl) {
            if (touchTurn != 0) {
                for (Object3D i:myobj)
                    i.rotateY(touchTurn);
                for(SimpleVector[] sv:ls){
                    sv[0].rotateY(touchTurn);
                }
                ls.get(ls.size()-1)[1].rotateY(touchTurn);
                touchTurn = 0;
            }
            if (touchTurnUp != 0) {
                for (Object3D i:myobj)
                    i.rotateX(touchTurnUp);
                for(SimpleVector[] sv:ls){
                    sv[0].rotateX(touchTurnUp);
                }
                ls.get(ls.size()-1)[1].rotateX(touchTurnUp);
                touchTurnUp = 0;
            }
            for (int i=0;i<myployline.size();i++){
                myployline.get(i).update(ls.get(i));
                myployline.get(i).setColor(mycolor[i]);
                myployline.get(i).setWidth(3);
            }
            world.renderScene(buffer);
            world.draw(buffer);
            buffer.display();
            if (System.currentTimeMillis() - time >= 1000) {
                Logger.log(fps + "fps");
                fps = 0;
                time = System.currentTimeMillis();
            }
            fps++;
        }
    }
}
