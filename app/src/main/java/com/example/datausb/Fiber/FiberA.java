package com.example.datausb.Fiber;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.PathEffect;

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

}
