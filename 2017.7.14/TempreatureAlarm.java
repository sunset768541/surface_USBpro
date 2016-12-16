package com.example.datausb;

import android.util.Log;

/**
 * Created by sunset on 16/5/25.
 * 这是一个温度报警模块，开启一个新的线程根据设定的报警温度对输入的数据进行处理并对比，发现超过报警温度后就报警
 * （1）首先根据串过来的数据换算成温度，得到温度数组
 * （2）对温度数组进行遍历，发现某个位置的数组值大于设置温度值则进行报警
 */
public class TempreatureAlarm {
    public static kk tt;
    public static double[] tua1;
    public static double[] tua2;
    public static double[] tub1;
    public static double[] tub2;
    public static float[] cla1;
    public static float[] cla2;
    public static int  alertfinish=1;//这个标志的作用就是对报警温度进行遍历判断时，新采集到的数据不会使正在执行的遍历发生中断
    public static void gettempalert(){
        tt=new kk();
        tt.start();
    }
    static class kk extends Thread{
        public void run(){
            float [] PSA11=new float[tua1.length];
            float [] PSA22=new float[tua1.length];
            //对数组进行处理并报警

            for (int i=0;i<tua1.length;i++){
                if(tua1[i]==0){
                    PSA11[i]=0;
                }
                else PSA11[i]=(float)tua2[i]/(float) tua1[i];
                if(tub1[i]==0){
                    PSA22[i]=0;
                }
                else PSA22[i]=(float)tub2[i]/(float) tub1[i];
            }
            /**
             * 由公式计算出温度，同时对温度进行监测，发生异常时开始报警
             */
            for (int i=0;i<tua1.length;i++){
                double bb11=(double)PSA11[i]/cla1[i];
                double bb21=(double)PSA22[i]/cla2[i];
                //Log.e("calopsa","当前温度"+Float.valueOf(caliPSA[caliPSA.length-1]).toString()+"Acli"+Float.valueOf(caliPSA[i]).toString()+"Bcli"+Float.valueOf(caliPSB[i]).toString());
                float tt11=(float)(Math.log(bb11)+1/cla1[cla1.length-1]);
                float tt21=(float)(Math.log(bb21)+1/cla2[cla2.length-1]);
                //如果发现通道1的某一温度大于设定的报警温度
               // Log.e("tt1","第 "+Integer.valueOf(i).toString()+" 米温度为 "+Float.valueOf(tt1).toString());
                //Log.e("tt2","第 "+Integer.valueOf(i).toString()+" 米温度为 "+Float.valueOf(tt2).toString());
                if((1/tt11)> SystemParameter.TEM_ALERT_TUBE1){
                   Log.e("警报。。警报。。","通道1的 "+Integer.valueOf(i).toString()+" 米处发生了温度异常"+"--异常温度为:"+Float.valueOf(1/tt11).toString());
                }
                //如果发现通道2的某一点的温度大于设定的报警温度
                if ((1/tt21)> SystemParameter.TEM_ALERT_TUBE2){
                   Log.e("警报。。警报。。","通道2的 "+Integer.valueOf(i).toString()+" 米处发生了温度异常"+"--异常温度为:"+Float.valueOf(1/tt21).toString());
                }
                alertfinish=1;

            }

        }
    }

}
