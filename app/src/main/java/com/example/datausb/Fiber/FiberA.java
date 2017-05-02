package com.example.datausb.Fiber;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.PathEffect;

import com.example.datausb.SystemParameter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by sunset on 16/7/28.
 */
public class FiberA extends Fiber {
    private  static FiberA fiberA;
    private  FiberA(){

    }
    public static FiberA createFiberA(){
        if (fiberA==null){
            fiberA=new FiberA();
            fiberA.setFiberName("FiberA");
            fiberA.setFiberId('A');
            fiberA.setOptical1440Head(48048);//AAA056784
            fiberA.setOptical1663Head(56784);//BBB0
        }
        return fiberA;

    }

    @Override
    public void setFiberColor() {
        fiberColor=Color.CYAN;
    }

    @Override
    public Map alterTem() {
        calculateTempreture();
        Map<Integer,Float> mp=new LinkedHashMap<Integer, Float>();
        for (int i=0;i<calculateTempreture_tem.length;i++) {
            if (calculateTempreture_tem[i] > SystemParameter.TEM_ALERT_FIBERA)
                mp.put(i, calculateTempreture_tem[i]);
        }
        return mp;
    }
}
