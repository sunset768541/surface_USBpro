package com.example.datausb.Fiber;

import android.graphics.Color;

import com.example.datausb.SystemParameter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by sunset on 16/7/28.
 */
public class FiberC extends Fiber {
    private  static FiberC fiberC;
    private  FiberC(){

    }
    public static FiberC createFiberC(){
        if (fiberC==null){
            fiberC=new FiberC();
            fiberC.setFiberName("FiberC");
            fiberC.setFiberId('C');
            fiberC.setOptical1440Head(2370);
            fiberC.setOptical1663Head(3003);
        }
        return fiberC;
    }

    @Override
    public void setFiberColor() {
        fiberColor=Color.GREEN;
    }
    @Override
    public Map alterTem() {
        calculateTempreture();
        Map<Integer,Float> mp=new LinkedHashMap<Integer, Float>();
        for (int i=0;i<calculateTempreture_tem.length;i++) {
            if (calculateTempreture_tem[i] > SystemParameter.TEM_ALERT_FIBERC)
                mp.put(i, calculateTempreture_tem[i]);
        }
        return mp;
    }
}
