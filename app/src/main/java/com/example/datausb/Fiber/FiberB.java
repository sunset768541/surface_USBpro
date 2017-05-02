package com.example.datausb.Fiber;

import android.graphics.Color;

import com.example.datausb.SystemParameter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by sunset on 16/7/28.
 */
public class FiberB extends Fiber {
    private  static FiberB fiberB;
    private  FiberB(){

    }
    public static FiberB createFiberB(){
        if (fiberB==null){
            fiberB=new FiberB();
            fiberB.setFiberName("FiberB");
            fiberB.setFiberId('B');
            fiberB.setOptical1440Head(52416);//ccc0
            fiberB.setOptical1663Head(43680);//DDD0
        }
        return fiberB;
    }

    @Override
    public void setFiberColor() {
        fiberColor=Color.RED;
    }
    @Override
    public Map alterTem() {
        calculateTempreture();
        Map<Integer,Float> mp=new LinkedHashMap<Integer, Float>();
        for (int i=0;i<calculateTempreture_tem.length;i++) {
            if (calculateTempreture_tem[i] > SystemParameter.TEM_ALERT_FIBERB)
                mp.put(i, calculateTempreture_tem[i]);
        }
        return mp;
    }
}
