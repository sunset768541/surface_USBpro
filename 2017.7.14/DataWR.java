package com.example.datausb;

import android.content.Context;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by sunset on 16/5/6.
 * 用于存储和读取数据，以二进制的形式进行存取。
 * 对于存储，每个小时的数据为一个文件，防止文件过大造成读取困难
 * 根据年月日建立存储数据的文件夹
 */
public class DataWR {
    private  static SimpleDateFormat fileFormat = new SimpleDateFormat("yyyy-MM-dd-kk-mm");//设置储存文件的显示格式
    private static SimpleDateFormat docFormaty = new SimpleDateFormat("yyyy");//建立文件夹年的名字
    private static SimpleDateFormat docFormatm = new SimpleDateFormat("MM");//建立文件夹月的名字
    private static SimpleDateFormat docFormatd = new SimpleDateFormat("dd");//建立文件夹日的名字
    private static Date date = new Date();
    //public static String SDcardPath="/mnt/external_sd/";
    public static String SDcardPath="/storage/sdcard0/";
    private static String dir = docFormaty.format(date) + "/" + docFormatm.format(date) + "/" + docFormatd.format(date) + "/";//存储数据文件夹的目录
  //  private static  file;//file对象用来建立文件夹
    private static String preFileName = SDcardPath + dir + fileFormat.format(date) + ".dat";//存储数据文件的名字
    private static BufferedOutputStream d;//定义具有缓冲功能的输出流
   // private static  dataname;
    public static float[] cla;
    public static float[] clb;
    private static boolean isExist = false;
    private static boolean doSave = false;//可以往存储文件标识
    private static byte[] paraIndicator=new byte[]{0,0,0,0,0,0,0,0};
    public static int fiberNum=4;
    /**
     * 初始化储存
     *
     */
    public static boolean iniSave(){
        boolean iniOk=true;
        try {
            File file = new File(SDcardPath + dir);
            iniOk = file.exists();
            if (!iniOk) {
                iniOk = file.mkdirs();
                Log.e("初始化储存","创建文件成功");

            }
            if (iniOk) {
               Log.e("初始化储存","文件夹已经存在");
            }
        }
        catch (Exception e){
            Log.e("初始化储存失败",Log.getStackTraceString(e));
            iniOk=false;
        }
        return iniOk;
    }
    /**
     * 储存数据的静态方法
     */
    public static void saveData(byte[] data) {
        date = new Date();
      //  Log.e("dir",dir);
        String dir1 = docFormaty.format(date) + "/" + docFormatm.format(date) + "/" + docFormatd.format(date) + "/";//存储数据文件夹的目录
        //Log.e("dir1",dir1);
        if (!dir.equals(dir1)) {//如果日期变了就创建新的文件夹年/月/日
            dir = dir1;
            File file = new File(SDcardPath + dir);
            boolean isDirectoryCreated=file.exists();
            if (!isDirectoryCreated) {
                isDirectoryCreated= file.mkdirs();
            }
            if(isDirectoryCreated) {
                Log.e("存储数据","--文件夹创建失败");
            }
        }
       // Log.e("pre")
        String currentFileName = SDcardPath + dir + fileFormat.format(date) + ".dat";//根据时间创建保存数据的文件名
        if (!preFileName.equals(currentFileName)) {//每次写入数据之前判断存储数据的文件名是否改变
            doSave = false;
            preFileName = currentFileName;
            try {
                //在数据文件的最后写时间数据之前要判断是d是否执行了d.write()，以确定是在上一个文档中写入数据

                //在建立新的文件之前对上一个文件写入文件尾，内容为结束的时间,时间为毫秒 dataObj.getTime()来获取一个Long型的时间数据，为从1970.1.1到现在的毫秒数
                if (isExist) {//如果存在上一个输出流
                    d.write(long2byte(date.getTime()));
                    d.write(paraIndicator);//在文件写入结束之前写入一个标志位，通过读取这个标志位来确定文件的完整性
                    d.close();
                }
                d = new BufferedOutputStream(new FileOutputStream(preFileName,true));
                isExist = true;
                byte[] ca = new byte[cla.length * 4];
                byte[] cb = new byte[cla.length * 4];
                int jj = 0;
                d.write(getBytesint(8192));//数据的长度
                for (int mm=0;mm<cla.length;mm++){//注意cla里有8193个数据,将标定数据转化为byte数组
                    byte []clai=getBytes(cla[mm]);
                    byte []clao=getBytes(clb[mm]);
                    for (int kk=0;kk<4;kk++){
                        ca[jj]=clai[kk];
                        cb[jj]=clao[kk];
                        jj++;
                    }
                }
                d.write(getBytesint(fiberNum));//写入通道的个数
                d.write(long2byte(date.getTime()));//写入文件的创建时间
                d.write(ca);//写入光纤A的标定数据
                d.write(cb);//写入光纤B的标定数据

/******************************************************************************************
 * 该部分用来验证java的基本数据类型转换为byte数组的正确性，经过验证，这些算法都是正确的
 * ******************************************************************************************
//                d.writeFloat(cla[0]);
//                printHexString(getBytes(cla[0]));
//                Log.e("byt2fcla[0]", Float.valueOf(getFloat(getBytes(cla[0]))).toString());
//                d.writeFloat(clb[0]);
//                printHexString(getBytes(clb[0]));
//                Log.e("byt2fclb[0]", Float.valueOf(getFloat(getBytes(clb[0]))).toString());
//
//                d.writeFloat(cla[cla.length - 1]);
//                printHexString(getBytes(cla[cla.length - 1]));
//                Log.e("byt2fcla[leng-1]", Float.valueOf(getFloat(getBytes(cla[cla.length - 1]))).toString());
//
//                d.writeFloat(clb[clb.length - 1]);
//                printHexString(getBytes(cla[clb.length - 1]));
//                Log.e("byt2clb[clb.len-1]f", Float.valueOf(getFloat(getBytes(clb[clb.length - 1]))).toString());
//
//
//                d.write(new byte[]{4, 3, 2, 1});
//
//                d.writeInt(8192);
//                printHexString(getBytesint(8192));
//                Log.e("byte2int", Integer.valueOf(getInt(getBytesint(8192))).toString());
//
//                d.writeLong(date.getTime());
//                printHexString(long2byte(date.getTime()));
//                Log.e("byt2long", Long.valueOf(getLong(long2byte(date.getTime()))).toString());*/

                Log.e("creation", "创建一个新数据文件成功" + "time " + Integer.valueOf(jj).toString());
                doSave =true;
                //在文件创建的时候为文件添加头，头的内容为数据长度()，标定数据（标定数据的末尾是标定温度）开始时间，时间为毫秒 ,头的长度是固定的
            } catch (Exception e) {
                Log.e("创建数据文件失败", Log.getStackTraceString(e));
            }
        }
        try {
            if (doSave) {
                d.write(data);//写入数据
            }

        } catch (Exception e) {
            Log.e("写入出错", e.toString());
        }
    }


    public static String[] read(String year, String month, String day, Context cc) {

        String[] dataname = null;
        String finddatafile = SDcardPath + year + "/" + month + "/" + day;
        Log.e("s", finddatafile);
        File datapathfile = new File(finddatafile);
        if (!datapathfile.exists()) {
             dataname = null;
        } else {
            dataname = datapathfile.list();
        }
        return dataname;

    }

    private static byte[] long2byte(long data) {//将long型数据转换为byte数组
        byte[] bytes = new byte[8];
        bytes[0] = (byte) (data & 0xff);
        bytes[1] = (byte) ((data >> 8) & 0xff);
        bytes[2] = (byte) ((data >> 16) & 0xff);
        bytes[3] = (byte) ((data >> 24) & 0xff);
        bytes[4] = (byte) ((data >> 32) & 0xff);
        bytes[5] = (byte) ((data >> 40) & 0xff);
        bytes[6] = (byte) ((data >> 48) & 0xff);
        bytes[7] = (byte) ((data >> 56) & 0xff);
        return bytes;
    }

    private static long getLong(byte[] bytes) {//将byte数组转换为long型
        return (0xffL & (long) bytes[0]) | (0xff00L & ((long) bytes[1] << 8)) | (0xff0000L & ((long) bytes[2] << 16)) | (0xff000000L & ((long) bytes[3] << 24))
                | (0xff00000000L & ((long) bytes[4] << 32)) | (0xff0000000000L & ((long) bytes[5] << 40)) | (0xff000000000000L & ((long) bytes[6] << 48)) | (0xff00000000000000L & ((long) bytes[7] << 56));
    }

    private static byte[] getBytes(float data) {//将float值转换为byte数组
        int intBits = Float.floatToIntBits(data);
        return getBytesint(intBits);
    }

    private static float getFloat(byte[] bytes) {//将byte数组转换为float值
        return Float.intBitsToFloat(getInt(bytes));
    }

    private static int getInt(byte[] bytes) {//将byte数组转换为int值
        return (0xff & bytes[0]) | (0xff00 & (bytes[1] << 8)) | (0xff0000 & (bytes[2] << 16)) | (0xff000000 & (bytes[3] << 24));
    }

    private static byte[] getBytesint(int data) {//将int型数据转换为byte数组
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (data & 0xff);
        bytes[1] = (byte) ((data & 0xff00) >> 8);
        bytes[2] = (byte) ((data & 0xff0000) >> 16);
        bytes[3] = (byte) ((data & 0xff000000) >> 24);
        return bytes;
    }

    private static void printHexString(byte[] b) {//将byte数组以16进制的方式打印
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            Log.e("16", hex.toUpperCase());
        }

    }

}
