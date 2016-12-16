package com.example.datausb.Fiber;

import android.graphics.Color;

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
}
