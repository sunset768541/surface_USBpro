package com.example.datausb;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
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
 * 第一次创建Fragment时要将上次的配置还原
 */
public class SystemSetting extends android.app.Fragment {
    private TextView fiberLength;
    private TextView envriomentModel;//环境模型名字，需要从sp中还原
    private TextView fiberModel;//光纤模型名字，需要从sp中还原
    private TextView axiname;//地图模式坐标数据名字，需要从sp中还原
    private Button chooseEnvModel;
    private Button chooseFiberModel;
    private Button sendDataToUsb;
    private Button startAD;
    private Button resetAD;
    private Button stopAD;
    private Button test;
    private Button reset;
    private Button loadaxi;
    private Button resetaxi;
    private Spinner depth;//深度设置Spinner控件，需要还原参数
    private Spinner averageNum;//平均次数Spinner控件，需要从sp还原参数
    private Spinner plause;//脉冲设置spinner，需要从sp还原参数
    private TextView showSendData;
    private final String STATR_CODE="";
    private final String sendData="发送16进制数据：";
    private Switch dataSaveSwitch;
    private Switch temAlertSwitch;
    private EditText temAlertFiberA;//A通道的报警温度,启动软件需要从sharePreference中还原
    private EditText temAlertFiberB;
    private EditText temAlertFiberC;
    private EditText temAlertFiberD;
    private EditText longitude;//地图模式中心点的经度坐标，数值需要从sp中还原
    private EditText latitude;//地图模式中心点维度坐标
    private Spinner datasavetrr;//
    private Spinner threscence;

    private ToggleButton fiberAOpen;
    private ToggleButton fiberBOpen;
    private ToggleButton fiberCOpen;
    private ToggleButton fiberDOpen;
    private boolean isFirstCreate=true;
    public void iniParameter(){//还原上一次的配置
        isFirstCreate=false;
        if (SystemParameter.envPath!=null)
        envriomentModel.setText(SystemParameter.envPath.substring(SystemParameter.envPath.lastIndexOf("/")+1,SystemParameter.envPath.length()));
        if (SystemParameter.fiberPath!=null)
        fiberModel.setText(SystemParameter.fiberPath.substring(SystemParameter.fiberPath.lastIndexOf("/")+1,SystemParameter.fiberPath.length()));
        if (SystemParameter.axipath!=null)
        axiname.setText(SystemParameter.axipath.substring(SystemParameter.axipath.lastIndexOf("/")+1,SystemParameter.axipath.length()));
        depth.setSelection(caculateDepPosition(SystemParameter.fiberL));
        plause.setSelection(caculatePalusePosition(SystemParameter.pla));
        averageNum.setSelection(caculateAverageNumPosition(SystemParameter.average));
        temAlertFiberA.setText(String.valueOf(SystemParameter.TEM_ALERT_FIBERA));
        temAlertFiberB.setText(String.valueOf(SystemParameter.TEM_ALERT_FIBERB));
        temAlertFiberC.setText(String.valueOf(SystemParameter.TEM_ALERT_FIBERC));
        temAlertFiberD.setText(String.valueOf(SystemParameter.TEM_ALERT_FIBERD));
        longitude.setText(String.valueOf(SystemParameter.centerLongitude));
        latitude.setText(String.valueOf(SystemParameter.centerLatitude));
    }
    public int caculateDepPosition(char num){//根据SystemParamenter中的fiberL数值计算出Spinner的选择位置
        int position=0;
        switch (num){
            case 256:
                position=0;
                break;
            case 512:
                position=1;
                break;
            case 1024:
                position=2;
                break;
            case 2048:
                position=3;
                break;
            case 4096:
                position=4;
                break;
            case 8192:
                position=5;
                break;
            case 16384:
                position=6;
                break;
        }
        return  position;
    }
    public int caculateAverageNumPosition(char num){//根据SystemParamenter中的average数值计算出Spinner的选择位置
        int position=0;
        switch (num){
            case 128:
                position=0;
                break;
            case 256:
                position=1;
                break;
            case 512:
                position=2;
                break;
            case 1024:
                position=3;
                break;
            case 2048:
                position=4;
                break;
            case 4096:
                position=5;
                break;
            case 8192:
                position=6;
                break;
            case 16384:
                position=7;
                break;
        }
        return  position;
    }
    public int caculatePalusePosition(byte num){//根据SystemParamenter中的pla数值计算出Spinner的选择位置
        int position=0;
        switch (num){
            case 3:
                position=0;
                break;
            case 4:
                position=1;
                break;
            case 5:
                position=2;
                break;
            case 6:
                position=3;
                break;
            case 7:
                position=4;
                break;
            case 8:
                position=5;
                break;
            case 9:
                position=6;
                break;
            case 10:
                position=7;
                break;
            case 11:
                position=8;
                break;
            case 12:
                position=9;
                break;
            case 13:
                position=10;
                break;
            case 14:
                position=11;
                break;
            case 15:
                position=12;
                break;
            case 16:
                position=13;
                break;
            case 17:
                position=14;
                break;
            case 18:
                position=15;
                break;
            case 19:
                position=16;
                break;
            case 20:
                position=17;
                break;
        }
        return  position;
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.systemseting, container, false);
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                if (SystemParameter.preenvPath!=null)
                    SystemParameter.preenvPath= SystemParameter.envPath;
                Uri uri = data.getData();
                envriomentModel.setText(uri.getPath().substring(uri.getPath().lastIndexOf("/")+1,uri.getPath().length()));
                SystemParameter.envPath=uri.getPath();
                if (SystemParameter.preenvPath==null)
                    SystemParameter.preenvPath= SystemParameter.envPath;
                Toast.makeText(getActivity(), "环境模型路径："+uri.getPath(), Toast.LENGTH_SHORT).show();
            }
            if (requestCode == 2) {
                if (SystemParameter.prefiberPath!=null)
                    SystemParameter.prefiberPath= SystemParameter.fiberPath;
                 Uri uri = data.getData();
                 fiberModel.setText(uri.getPath().substring(uri.getPath().lastIndexOf("/")+1,uri.getPath().length()));
                SystemParameter.fiberPath=uri.getPath();
                if (SystemParameter.prefiberPath==null)
                    SystemParameter.prefiberPath= SystemParameter.fiberPath;
                 Toast.makeText(getActivity(), "光纤模型路径："+uri.getPath(), Toast.LENGTH_SHORT).show();
            }
            if (requestCode == 3) {

                Uri uri = data.getData();
                axiname.setText(uri.getPath().substring(uri.getPath().lastIndexOf("/")+1,uri.getPath().length()));
                SystemParameter.axipath=uri.getPath();
            }

        }
    }
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        envriomentModel=(TextView)getActivity().findViewById(R.id.textView4);
        fiberModel=(TextView)getActivity().findViewById(R.id.textView6);
        axiname =(TextView)getActivity().findViewById(R.id.axiname);
        if (SystemParameter.envPath==null|| SystemParameter.fiberPath==null){
                    envriomentModel.setText("默认");
                    fiberModel.setText("默认");
        }
        else {
            envriomentModel.setText(SystemParameter.envPath.substring(SystemParameter.envPath.lastIndexOf("/")+1, SystemParameter.envPath.length()));
            fiberModel.setText(SystemParameter.fiberPath.substring(SystemParameter.fiberPath.lastIndexOf("/")+1, SystemParameter.fiberPath.length()));
        }
        if (SystemParameter.axipath==null){
            axiname.setText("默认");
        }
        else axiname.setText(SystemParameter.axipath.substring(SystemParameter.axipath.lastIndexOf("/")+1,SystemParameter.axipath.length()));
        loadaxi=(Button)getActivity().findViewById(R.id.loadaxi);
        loadaxi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent,3);
            }
        });
        resetaxi=(Button)getActivity().findViewById(R.id.resetaxi);
        resetaxi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SystemParameter.axipath=null;
                SystemParameter.centerLongitude=121.89835090371952f;
                SystemParameter.centerLatitude=38.978000183472237f;
                axiname.setText("默认");
            }
        });
        chooseEnvModel=(Button)getActivity().findViewById(R.id.chooseenvmodel);
        chooseEnvModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent,1);
            }
        });
        reset=(Button)getActivity().findViewById(R.id.resetenvfiber);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SystemParameter.envPath=null;
                SystemParameter.fiberPath=null;
                envriomentModel.setText("默认");
                fiberModel.setText("默认");
            }
        });
        chooseFiberModel=(Button)getActivity().findViewById(R.id.choosefibermodel);
        chooseFiberModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent,2);
            }
        });
        fiberLength = (TextView) getActivity().findViewById(R.id.textView);
        temAlertFiberA = (EditText) getActivity().findViewById(R.id.editText4);
        temAlertFiberA.setEnabled(false);
        temAlertFiberA.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (hasFocus) {
                        Log.e("聚焦","ok");
                } else {
                    if (!temAlertFiberA.getText().toString().equals("")) {

                        //  Log.e("jj",temAlertFiberB.getText().toString());
                        SystemParameter.TEM_ALERT_FIBERA = Integer.valueOf(temAlertFiberA.getText().toString());
                    } else {
                        // Log.e("jj99",temAlertFiberA.getText().toString());//当getText为""的时候，Log不会执行

                        SystemParameter.TEM_ALERT_FIBERA = 60;
                    }
                    //  Log.e("通道1的监控温度为：",Integer.valueOf(system.TEM_ALERT_FIBERA)+"ll");


                }
                Log.e("焦点1变化", "" + SystemParameter.TEM_ALERT_FIBERA);
            }
        });

        temAlertFiberB = (EditText) getActivity().findViewById(R.id.editText6);
        temAlertFiberB.setEnabled(false);
        temAlertFiberB.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                        Log.e("hasFocus","ok");
                } else {
                    if (!temAlertFiberB.getText().toString().equals("")) {
                        SystemParameter.TEM_ALERT_FIBERB = Integer.valueOf(temAlertFiberB.getText().toString());
                    } else {
                        SystemParameter.TEM_ALERT_FIBERB = 60;
                    }
                }
                Log.e("焦点2变化", "" + SystemParameter.TEM_ALERT_FIBERB);
            }
        });
        temAlertFiberC=(EditText)getActivity().findViewById(R.id.editText18);
        temAlertFiberC.setEnabled(false);
        temAlertFiberC.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b){
                    Log.e("hasFocus","ok");
                }
                else {
                    if (!temAlertFiberC.getText().toString().equals("")){
                        SystemParameter.TEM_ALERT_FIBERC=Integer.valueOf(temAlertFiberC.getText().toString());
                    }else{
                        SystemParameter.TEM_ALERT_FIBERC=60;
                    }
                }
            }
        });
        temAlertFiberD=(EditText)getActivity().findViewById(R.id.editText19);
        temAlertFiberD.setEnabled(false);
        temAlertFiberD.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b){
                    Log.e("hasFocus","ok");
                }
                else {
                    if (!temAlertFiberD.getText().toString().equals("")){
                        SystemParameter.TEM_ALERT_FIBERD=Integer.valueOf(temAlertFiberD.getText().toString());
                    }else {
                        SystemParameter.TEM_ALERT_FIBERD=60;
                    }
                }
            }
        });
        longitude = (EditText)getActivity().findViewById(R.id.editText14);
        //longitude.setEnabled(false);
        longitude.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b){
                    Log.e("hasFocus","ok");
                }
                else {
                    if (!longitude.getText().toString().equals("")){
                                SystemParameter.centerLongitude=Float.parseFloat(longitude.getText().toString());
                                Log.e("centerLongitude=",Double.valueOf(SystemParameter.centerLongitude).toString());
                    }else {
                        Log.v("mapCenterlongitude","default");
                    }
                }
            }
        });
        latitude = (EditText)getActivity().findViewById(R.id.editText13);
        latitude.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b){
                    Log.e("hasFocus","ok");
                }
                else {
                    if (!latitude.getText().toString().equals("")){
                        SystemParameter.centerLatitude=Float.parseFloat(latitude.getText().toString());
                        Log.e("ceterLatitude=",Double.valueOf(SystemParameter.centerLatitude).toString());
                    }else {
                        Log.v("mapCenterlatitude","default");
                    }
                }
            }
        });
        sendDataToUsb = (Button) getActivity().findViewById(R.id.button10);
        //showSendData = (TextView) getActivity().findViewById(R.id.textView17);
        datasavetrr = (Spinner) getActivity().findViewById(R.id.spinner5);

        temAlertSwitch = (Switch) getActivity().findViewById(R.id.switch2);
        if (Main.TEM_ALERT == 1) {//这个语句是当设置存储打开后，从系统设置fragment切换走又切换回来时，sw显示的是当前的存储状态
            dataSaveSwitch.setChecked(true);
            temAlertFiberA.setEnabled(true);
            temAlertFiberB.setEnabled(true);
            temAlertFiberC.setEnabled(true);
            temAlertFiberD.setEnabled(true);
        }
        temAlertSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (temAlertSwitch.isChecked()) {
                    Main.TEM_ALERT = 1;
                    temAlertFiberB.setEnabled(true);
                    temAlertFiberA.setEnabled(true);
                    temAlertFiberD.setEnabled(true);
                    temAlertFiberC.setEnabled(true);
                    Toast.makeText( getActivity(), "温度报警已经打开", Toast.LENGTH_SHORT).show();

                } else {
                    Main.TEM_ALERT = 0;
                    temAlertFiberB.setEnabled(false);
                    temAlertFiberA.setEnabled(false);
                    temAlertFiberC.setEnabled(false);
                    temAlertFiberD.setEnabled(false);
                    Toast.makeText(getActivity(), "温度报警已经关闭", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dataSaveSwitch = (Switch) getActivity().findViewById(R.id.switch1);
        if (Main.STOREDATA == 1) {//这个语句是当设置存储打开后，从系统设置fragment切换走又切换回来时，sw显示的是当前的存储状态
            dataSaveSwitch.setChecked(true);
        }
        dataSaveSwitch.setOnClickListener(new View.OnClickListener() {
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
                            dataSaveSwitch.setChecked(false);
                        }
                    }

                } catch (Exception e) {
                    Toast.makeText( getActivity().getApplicationContext(), "标定数据不存在，请先在标定模式下进行标定", Toast.LENGTH_SHORT).show();

                }
                if (dataSaveSwitch.isChecked() && (file.exists())) {//如果开关sw打开，并且插入了sd卡，那么就可以设置STOREDATA标志
                    Main.STOREDATA = 1;
                    DataWR.iniSave();
                    Toast.makeText(getActivity().getApplication(), "数据存储打开", Toast.LENGTH_SHORT).show();

                } else {
                    dataSaveSwitch.setChecked(false);//如果没有检测到sd卡，则不能设置sw为checked状态，也不能设置STOREDATA标志，同时提示没有检测到sd卡
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
                SystemParameter.average=(char) Integer.parseInt(averageNum.getSelectedItem().toString());
                SystemParameter.dev=(byte) (Math.log(SystemParameter.average)/Math.log(2));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        plause=(Spinner)getActivity().findViewById(R.id.plause);
        plause.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SystemParameter.pla=(byte) Integer.parseInt(plause.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        depth=(Spinner)getActivity().findViewById(R.id.spinner6);
        depth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SystemParameter.fiberL=(char)Integer.parseInt(depth.getSelectedItem().toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        resetAD=(Button)getActivity().findViewById(R.id.button6);
        resetAD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte [] setPar=new byte[]{charToBytes(SystemParameter.average)[0],charToBytes(SystemParameter.average)[1],charToBytes(SystemParameter.fiberL)[0],charToBytes(SystemParameter.fiberL)[1],1,SystemParameter.dev};
                ((Main) getActivity()).UsbSendData(setPar);
                try {
                    Thread.sleep(100);
                }
                catch (Exception e){
                    Log.getStackTraceString(e);
                }
                byte [] setPars=new byte[]{charToBytes(SystemParameter.average)[0],charToBytes(SystemParameter.average)[1],charToBytes(SystemParameter.fiberL)[0],charToBytes(SystemParameter.fiberL)[1],0,SystemParameter.dev};
                ((Main) getActivity()).UsbSendData(setPars);

            }
        });
        startAD=(Button)getActivity().findViewById(R.id.button11);
        startAD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte [] setPar=new byte[]{charToBytes(SystemParameter.average)[0],charToBytes(SystemParameter.average)[1],charToBytes(SystemParameter.fiberL)[0],charToBytes(SystemParameter.fiberL)[1],(byte)(32+SystemParameter.pla),SystemParameter.dev};
                ((Main) getActivity()).UsbSendData(setPar);
                showSendData.setText(sendData+bytesToHexString(setPar));
                FiberManager.fiberLength=SystemParameter.fiberL;
            }
        });
        stopAD=(Button)getActivity().findViewById(R.id.button7);

        stopAD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte [] setPar=new byte[]{charToBytes(SystemParameter.average)[0],charToBytes(SystemParameter.average)[1],charToBytes(SystemParameter.fiberL)[0],charToBytes(SystemParameter.fiberL)[1],0,SystemParameter.dev};
                ((Main) getActivity()).UsbSendData(setPar);
                showSendData.setText(sendData+bytesToHexString(setPar));
                FiberManager.fiberLength=SystemParameter.fiberL;
            }
        });
        test=(Button)getActivity().findViewById(R.id.button12);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte [] setPar=new byte[]{charToBytes(SystemParameter.average)[0],charToBytes(SystemParameter.average)[1],charToBytes(SystemParameter.fiberL)[0],charToBytes(SystemParameter.fiberL)[1],-32,SystemParameter.dev};//应该脉冲是3
                ((Main) getActivity()).UsbSendData(setPar);
                showSendData.setText(sendData+bytesToHexString(setPar));
                FiberManager.fiberLength=SystemParameter.fiberL;
            }
        });
        showSendData=(TextView)getActivity().findViewById(R.id.textView23);
        sendDataToUsb.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
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
                    FiberManager.fiberLength=SystemParameter.fiberL;
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
                temAlertFiberB.clearFocus();
                temAlertFiberA.clearFocus();
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
                       fiberA.setFiberLength(SystemParameter.fiberL);
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
                        fiberB.setFiberLength(SystemParameter.fiberL);
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
                        fiberC.setFiberLength(SystemParameter.fiberL);
                    fiberC.iniPreData();
                        fiberC.setContext(getActivity().getApplicationContext());
                        //将Fiber加入到FiberManager中
                        ((Main)getActivity()).fiberManager.addFiber('C');
                        ((Main) getActivity()).setTunnelCOn();

                }
                else {
                    ((Main)getActivity()).fiberManager.removeFiber("C");
                    ((Main) getActivity()).setTunnelCOff();
                }

            }
        });
        fiberDOpen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                        FiberD fiberD=FiberD.createFiberD();
                        fiberD.setFiberLength(SystemParameter.fiberL);
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
        if (isFirstCreate)
        iniParameter();
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
