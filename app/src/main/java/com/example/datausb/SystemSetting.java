package com.example.datausb;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.datausb.DataUtil.DataBaseOperation;
import com.example.datausb.DataUtil.DataWR;
import com.example.datausb.Fiber.Fiber;
import com.example.datausb.Fiber.FiberA;
import com.example.datausb.Fiber.FiberB;
import com.example.datausb.Fiber.FiberC;
import com.example.datausb.Fiber.FiberD;
import com.example.datausb.Fiber.FiberManager;

import java.io.File;
import java.util.Map;

/**
 * Created by wang on 2016/2/23.
 * 系统设置Fragment
 */
public class SystemSetting extends android.app.Fragment {
    private TextView fiberLength;
    private Button sendDataToUsb;
    private Button startAD;
    private Button resetAD;
    private Button stopAD;
    private Button test;
    private Spinner depth;
    private Spinner averageNum;
    private TextView showSendData;
    private char average;
    private char fiberL;
    private byte dev;
    private final String STATR_CODE="";
    private final String sendData="发送16进制数据：";
    private Switch sw;
    private Switch sw2;
    private EditText temalerttube1;
    private EditText temalerttube2;
    private Spinner datasavetrr;
    private Spinner threscence;
    private Spinner plause;
    private byte pla;
    private ToggleButton fiberAOpen;
    private ToggleButton fiberBOpen;
    private ToggleButton fiberCOpen;
    private ToggleButton fiberDOpen;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.systemseting, container, false);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        fiberLength = (TextView) getActivity().findViewById(R.id.textView);
        temalerttube1 = (EditText) getActivity().findViewById(R.id.editText4);
        temalerttube1.setEnabled(false);
        temalerttube1.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (hasFocus) {
                        Log.e("聚焦","ok");
                } else {
                    if (!temalerttube1.getText().toString().equals("")) {

                        //  Log.e("jj",temalerttube2.getText().toString());
                        SystemParameter.TEM_ALERT_TUBE1 = Integer.valueOf(temalerttube1.getText().toString());
                    } else {
                        // Log.e("jj99",temalerttube1.getText().toString());//当getText为""的时候，Log不会执行

                        SystemParameter.TEM_ALERT_TUBE1 = 0;
                    }
                    //  Log.e("通道1的监控温度为：",Integer.valueOf(system.TEM_ALERT_TUBE1)+"ll");


                }
                Log.e("焦点1变化", "" + SystemParameter.TEM_ALERT_TUBE1);
            }
        });

        temalerttube2 = (EditText) getActivity().findViewById(R.id.editText6);
        temalerttube2.setEnabled(false);
        temalerttube2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                        Log.e("hasFocus","ok");
                } else {
                    if (!temalerttube2.getText().toString().equals("")) {

                        //  Log.e("jj",temalerttube2.getText().toString());
                        SystemParameter.TEM_ALERT_TUBE2 = Integer.valueOf(temalerttube2.getText().toString());
                    } else {
                        // Log.e("jj99",temalerttube1.getText().toString());//当getText为""的时候，Log不会执行

                        SystemParameter.TEM_ALERT_TUBE2 = 0;
                    }
                    //  Log.e("通道1的监控温度为：",Integer.valueOf(system.TEM_ALERT_TUBE1)+"ll");
                }
                Log.e("焦点2变化", "" + SystemParameter.TEM_ALERT_TUBE2);
            }
        });


        sendDataToUsb = (Button) getActivity().findViewById(R.id.button10);
        //showSendData = (TextView) getActivity().findViewById(R.id.textView17);
        datasavetrr = (Spinner) getActivity().findViewById(R.id.spinner5);
        threscence = (Spinner) getActivity().findViewById(R.id.spinner4);
        sw2 = (Switch) getActivity().findViewById(R.id.switch2);
        if (Main.TEM_ALERT == 1) {//这个语句是当设置存储打开后，从系统设置fragment切换走又切换回来时，sw显示的是当前的存储状态
            sw.setChecked(true);
            temalerttube1.setEnabled(true);
            temalerttube2.setEnabled(true);
        }
        sw2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    TempreatureAlarm.cla1= DataBaseOperation.mDataBaseOperation.getFromDataBase("tube1data");
                    TempreatureAlarm.cla2=DataBaseOperation.mDataBaseOperation.getFromDataBase("tube2data");
                } catch (Exception e) {
                    Toast.makeText( getActivity().getApplicationContext(), "标定数据不存在，请先在标定模式下进行标定", Toast.LENGTH_SHORT).show();

                }
                if (sw2.isChecked()) {
                    Main.TEM_ALERT = 1;
                    temalerttube2.setEnabled(true);
                    temalerttube1.setEnabled(true);
                    Toast.makeText( getActivity(), "温度报警已经打开", Toast.LENGTH_SHORT).show();

                } else {
                    Main.TEM_ALERT = 0;
                    temalerttube2.setEnabled(false);
                    temalerttube1.setEnabled(false);
                    Toast.makeText(getActivity(), "温度报警已经关闭", Toast.LENGTH_SHORT).show();
                }
            }
        });
        sw = (Switch) getActivity().findViewById(R.id.switch1);
        if (Main.STOREDATA == 1) {//这个语句是当设置存储打开后，从系统设置fragment切换走又切换回来时，sw显示的是当前的存储状态
            sw.setChecked(true);
        }
        sw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(DataWR.SDcardPath+"Android");//用来检测是否插入了sd卡，因为只要插入sd卡，系统就会在这个sd卡下建立LOST.DIR文件夹
               // File file = new File(DataWR.SDcardPath+"LOST.DIR");//用来检测是否插入了sd卡，因为只要插入sd卡，系统就会在这个sd卡下建立LOST.DIR文件夹
                int flag = 0;
                if (file.exists()) {
                    flag = 1;
                }
                try {
                    /**
                     * 遍历FiberManger并设置光纤的标定温度
                     * for (Map.Entry<String,Fiber>item: ((Main)getActivity()).fiberManager.getFiberMap().entrySet())
                     */
                    for (Map.Entry<String,Fiber>item: ((Main)getActivity()).fiberManager.getFiberMap().entrySet()){//如果设置标定温度返回false则不能开启储存
                        if (!item.getValue().setCalibrate()){
                            Main.STOREDATA = 0;
                            sw.setChecked(false);
                        }
                    }

                } catch (Exception e) {
                    Toast.makeText( getActivity().getApplicationContext(), "标定数据不存在，请先在标定模式下进行标定", Toast.LENGTH_SHORT).show();

                }
                if (sw.isChecked() && (file.exists())) {//如果开关sw打开，并且插入了sd卡，那么就可以设置STOREDATA标志
                    Main.STOREDATA = 1;
                    DataWR.iniSave();
                    Toast.makeText(getActivity().getApplication(), "数据存储打开", Toast.LENGTH_SHORT).show();

                } else {
                    sw.setChecked(false);//如果没有检测到sd卡，则不能设置sw为checked状态，也不能设置STOREDATA标志，同时提示没有检测到sd卡
                    Main.STOREDATA = 0;
                    if (flag == 0) {
                        Toast.makeText( getActivity().getApplication(), "没有检测到SD卡,或重启系统", Toast.LENGTH_SHORT).show();
                    }
                }

            }


        });
        //averageTimes=(EditText)getActivity().findViewById(R.id.editText22);
        averageNum=(Spinner)getActivity().findViewById(R.id.spinner7);
        averageNum.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                average=(char) Integer.parseInt(averageNum.getSelectedItem().toString());
                dev=(byte) (Math.log(average)/Math.log(2));
                Log.e("avernum= ",averageNum.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        plause=(Spinner)getActivity().findViewById(R.id.plause);
        plause.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                pla=(byte) Integer.parseInt(plause.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        depth=(Spinner)getActivity().findViewById(R.id.spinner6);
        depth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fiberL=(char)Integer.parseInt(depth.getSelectedItem().toString());
                    Log.e("n= ",Integer.valueOf(dev).toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        resetAD=(Button)getActivity().findViewById(R.id.button6);
        resetAD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte [] setPar=new byte[]{charToBytes(average)[0],charToBytes(average)[1],charToBytes(fiberL)[0],charToBytes(fiberL)[1],1,dev};
                ((Main) getActivity()).UsbSendData(setPar);
                try {
                    Thread.sleep(100);
                }
                catch (Exception e){
                    Log.getStackTraceString(e);
                }
                byte [] setPars=new byte[]{charToBytes(average)[0],charToBytes(average)[1],charToBytes(fiberL)[0],charToBytes(fiberL)[1],0,dev};
                ((Main) getActivity()).UsbSendData(setPars);

            }
        });
        startAD=(Button)getActivity().findViewById(R.id.button11);
        startAD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte [] setPar=new byte[]{charToBytes(average)[0],charToBytes(average)[1],charToBytes(fiberL)[0],charToBytes(fiberL)[1],(byte)(32+pla),dev};
                ((Main) getActivity()).UsbSendData(setPar);
                showSendData.setText(sendData+bytesToHexString(setPar));
                FiberManager.fiberLength=fiberL;
            }
        });
        stopAD=(Button)getActivity().findViewById(R.id.button7);

        stopAD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte [] setPar=new byte[]{charToBytes(average)[0],charToBytes(average)[1],charToBytes(fiberL)[0],charToBytes(fiberL)[1],0,dev};
                ((Main) getActivity()).UsbSendData(setPar);
                showSendData.setText(sendData+bytesToHexString(setPar));
                FiberManager.fiberLength=fiberL;
            }
        });
        test=(Button)getActivity().findViewById(R.id.button12);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte [] setPar=new byte[]{charToBytes(average)[0],charToBytes(average)[1],charToBytes(fiberL)[0],charToBytes(fiberL)[1],-32,dev};//应该脉冲是3
                ((Main) getActivity()).UsbSendData(setPar);
                showSendData.setText(sendData+bytesToHexString(setPar));
                FiberManager.fiberLength=fiberL;
            }
        });
        showSendData=(TextView)getActivity().findViewById(R.id.textView23);
        sendDataToUsb.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //String average=averageTimes.getText().toString();
                byte[] b = (STATR_CODE).getBytes();//String转换为byte[]
                String hex = "0";
                String hh = "";
                for (int i:b) {
                    hex = Integer.toHexString(i & 0xFF);

                    if (hex.length() == 1) {
                        hex = '0' + hex;
                    }
                    hh = hh + hex;

                }
               showSendData.setText(sendData+hh);
                try {
                   ((Main) getActivity()).UsbSendData(b);
                    FiberManager.fiberLength=fiberL;
                } catch (NullPointerException e) {
                    Toast.makeText(getActivity().getApplication(), "USB没有接收数据，请先开始接收数据", Toast.LENGTH_SHORT).show();
                }

            }
        });


        View sysseting = getActivity().findViewById(R.id.sysset);
        /**
         想要一个EditText失去焦点后，其他控件（尤其是别的可以编辑的EditText）不要获得焦点，则可以：
         对于该EditText的父级控件（一般都是LinearLayout），设置对应的focusable和focusableInTouchMode为true：*/
        sysseting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                temalerttube2.clearFocus();
                temalerttube1.clearFocus();
            }
        });


        fiberAOpen=(ToggleButton) getActivity().findViewById(R.id.toggleButton2);
        fiberBOpen=(ToggleButton) getActivity().findViewById(R.id.toggleButton3);
        fiberCOpen=(ToggleButton) getActivity().findViewById(R.id.toggleButton4);
        fiberDOpen=(ToggleButton) getActivity().findViewById(R.id.toggleButton5);
        fiberAOpen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){

                       FiberA fiberA=FiberA.createFiberA();
                       fiberA.setFiberLength(fiberL);
                       fiberA.iniPreData();
                       fiberA.setContext(getActivity().getApplicationContext());
                       //将Fiber加入到FiberManager中
                           ((Main)getActivity()).fiberManager.addFiber('A');
                           ((Main) getActivity()).setTunnelAOn();
                }
                else {
                    ((Main)getActivity()).fiberManager.removeFiber("A");
                    ((Main) getActivity()).setTunnelAOff();
                    //从FiberManager中删除这个光纤
                }

            }
        });
        fiberBOpen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                        FiberB fiberB=FiberB.createFiberB();
                        fiberB.setFiberLength(fiberL);
                    fiberB.iniPreData();
                        fiberB.setContext(getActivity().getApplicationContext());
                        //将Fiber加入到FiberManager中
                        ((Main)getActivity()).fiberManager.addFiber('B');
                        ((Main) getActivity()).setTunnelBOn();
                }
                else {
                    ((Main)getActivity()).fiberManager.removeFiber("B");
                    ((Main) getActivity()).setTunnelBOff();
                    //从FiberManager中删除这个光纤
                }

            }
        });
        fiberCOpen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                        FiberC fiberC= FiberC.createFiberC();
                        fiberC.setFiberLength(fiberL);
                    fiberC.iniPreData();
                        fiberC.setContext(getActivity().getApplicationContext());
                        //将Fiber加入到FiberManager中
                        ((Main)getActivity()).fiberManager.addFiber('C');
                        ((Main) getActivity()).setTunnelCOn();

                }
                else {
                    ((Main)getActivity()).fiberManager.removeFiber("C");
                    ((Main) getActivity()).setTunnelCOff();

                    //从FiberManager中删除这个光纤
                }

            }
        });
        fiberDOpen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                        FiberD fiberD=FiberD.createFiberD();
                        fiberD.setFiberLength(fiberL);
                        fiberD.iniPreData();
                        fiberD.setContext(getActivity().getApplicationContext());
                           //从数据库中读取标定数据
                           //将Fiber加入到FiberManager中
                           ((Main) getActivity()).fiberManager.addFiber('D');
                           ((Main) getActivity()).setTunnelDOn();

                }
                else {
                    ((Main)getActivity()).fiberManager.removeFiber("D");
                    ((Main) getActivity()).setTunnelDOff();
                    //从FiberManager中删除这个光纤
                }

            }
        });
    }
    public  String bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
    public  byte[] charToBytes(char c) {
        byte[] b = new byte[2];
        b[0] = (byte) ((c & 0xFF00) >> 8);
        b[1] = (byte) (c & 0xFF);
        return b;
    }
}
