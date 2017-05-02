package com.example.datausb.Fiber;

import android.graphics.Color;

import com.example.datausb.SystemParameter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by sunset on 16/7/28.
 */
public class FiberD extends Fiber {
    private  static FiberD fiberD;
    private  FiberD(){

    }
    public static FiberD createFiberD(){
        if (fiberD==null){
            fiberD=new FiberD();
            fiberD.setFiberName("FiberD");
            fiberD.setFiberId('D');
            fiberD.setOptical1663Head(3276);
            fiberD.setOptical1440Head(3549);
        }
        return fiberD;
    }

    @Override
    public void setFiberColor() {
        fiberColor=Color.YELLOW;
    }
    @Override
    public Map alterTem() {
        calculateTempreture();
        Map<Integer,Float> mp=new LinkedHashMap<Integer, Float>();
        for (int i=0;i<calculateTempreture_tem.length;i++) {
            if (calculateTempreture_tem[i] > SystemParameter.TEM_ALERT_FIBERD)
                mp.put(i, calculateTempreture_tem[i]);
        }
        return mp;
    }
}
