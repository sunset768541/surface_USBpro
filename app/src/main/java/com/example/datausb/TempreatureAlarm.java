package com.example.datausb;

import android.util.Log;

import com.example.datausb.Fiber.Fiber;
import com.example.datausb.Fiber.FiberManager;

import java.util.Map;

/**
 * Created by sunset on 16/5/25.
 * 这是一个温度报警模块，开启一个新的线程根据设定的报警温度对输入的数据进行处理并对比，发现超过报警温度后就报警
 *对温度数组进行遍历，发现某个位置的数组值大于设置温度值则进行报警
 */
 class TempreatureAlarm {
    private static FindTemExceedThread findTemExceedThread;
   public static FiberManager fiberManager;
    public static int alertFinish =1;//这个标志的作用就是对报警温度进行遍历判断时，新采集到的数据不会使正在执行的遍历发生中断
    public static void getTempAlert(){
        findTemExceedThread=new FindTemExceedThread();
        findTemExceedThread.start();
    }
    private static class FindTemExceedThread extends Thread{
        public void run(){
            for (Map.Entry<String, Fiber> item : fiberManager.getFiberMap().entrySet()) {//遍历FiberManager中的光纤
                Map mp=item.getValue().alterTem();//调用计算报警温度的方法
                if (!mp.isEmpty()){//如果有温度超出报警温度则mp不为空,对mp进行遍历就可以得到每个超出警报温度的位置和对应的温度值
                    Log.e(item.getKey()," 温度警报");//打印光纤的名字和警报警报
                }
             }
            alertFinish=1;
            }
        }
}


