package com.example.datausb.Fiber;

import android.content.Context;
import android.util.Log;

import com.example.datausb.Main;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sunset on 16/7/28.
 */

public class FiberManager {
    private Map<String,Fiber> fiberMap= new HashMap<>();
    public static int fiberLength;
    private Context context;
    public void addFiber(char tunnelCode){
        switch (tunnelCode){
            case 'A':
                getFiberMap().put(String.valueOf(tunnelCode),FiberA.createFiberA());
                FiberA.createFiberA().setContext(context);
                Log.e("加入FM","A");
                break;
            case 'B':
                getFiberMap().put(String.valueOf(tunnelCode),FiberB.createFiberB());
                FiberB.createFiberB().setContext(context);
                Log.e("加入FM","B");
                break;
            case 'C':
                getFiberMap().put(String.valueOf(tunnelCode),FiberC.createFiberC());
                FiberC.createFiberC().setContext(context);
                Log.e("加入FM","C");
                break;
            case 'D':
                getFiberMap().put(String.valueOf(tunnelCode),FiberD.createFiberD());
                FiberD.createFiberD().setContext(context);
                Log.e("加入FM","D");
                break;
            default:throw new IllegalArgumentException("错误的通道名字，只可以为A,B,C,D");
        }
    }
    public void removeFiber(String tunnelCode){
        try {
            getFiberMap().remove(tunnelCode);
            Log.e("从FM移除"," "+tunnelCode);
        }
        catch (Exception e){
            Log.e("删除Fiber异常","Fiber没有加入到FiberManger中");
        }
    }
    public void decodeData(int [] data){//解析数据，根据fibeMap里光纤的head14和head16得到各光纤的数据，将得到的数据保存在各自光纤中

            for (Map.Entry<String,Fiber>item: getFiberMap().entrySet()) {//遍历HashMap获得其中光纤的引用
        //                int []unfind=new int[item.getValue().getFiberLength()];
                for(int i=0;i<data.length;i++){
                  //  Log.e("s数据",Integer.valueOf(data[i]).toString());
                    if (item.getValue().getOptical1440Head()==data[i]){//获得一个光纤后，取出识别码并且与遍历的数据对比
                        int []a=Arrays.copyOfRange(data,i,i+item.getValue().getFiberLength());
                        a[0]=a[1];
                        item.getValue().setOptical1440Data(a);//.，找到后就将这个识别码后的光纤长度个数据存入相应的光纤item中
                        item.getValue().setPre1440Data(a);
                       // i=i+item.getValue().getFiberLength();//跳过数据
                      //  Log.e(item.getValue().getFiberName()+"1440第一个数据= ",Integer.valueOf(a[0]).toString());
                      //  Log.e(item.getValue().getFiberName()+"1440最后一数据= ",Integer.valueOf(a[a.length-1]).toString());
                    }
                   else item.getValue().setOptical1440Data(item.getValue().getPre1440Data());
                    if (item.getValue().getOptical1663Head()==data[i]){
                       int [] b=Arrays.copyOfRange(data,i,i+item.getValue().getFiberLength());
                        b[0]=b[1];
                        item.getValue().setOptical1663Data(b);
                        item.getValue().setPre1663Data(b);
                        //i=i+item.getValue().getFiberLength();
                        //Log.e(item.getValue().getFiberName()+"1663第一个数据= ",Integer.valueOf(b[0]).toString());
                        //Log.e(item.getValue().getFiberName()+"1663最后一数据= ",Integer.valueOf(b[b.length-1]).toString());
                    }
                    else item.getValue().setOptical1663Data(item.getValue().getPre1663Data());
                }

            }
        }

    public Map<String, Fiber> getFiberMap() {
        return fiberMap;
    }
    public  int getFibeNumber(){
        return fiberMap.size();
    }
    public Context getContext() {
        return context;
    }
    public void pushState(){
        for (Map.Entry<String,Fiber>item: fiberMap.entrySet()){
            item.getValue().pushClaLength();
        }
    }
    public void popState(){
        for (Map.Entry<String,Fiber>item: fiberMap.entrySet()){
            item.getValue().popClaLegnth();
        }
    }
    public void setContext(Context context) {
        this.context = context;
    }
}
