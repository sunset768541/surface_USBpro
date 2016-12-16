package com.example.datausb.DataUtil;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.example.datausb.Fiber.Fiber;
import com.example.datausb.Fiber.FiberA;
import com.example.datausb.Fiber.FiberB;
import com.example.datausb.Fiber.FiberC;
import com.example.datausb.Fiber.FiberD;
import com.example.datausb.Fiber.FiberManager;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

/**
 * Created by sunset on 16/5/30.
 */
public class DataRD {
    public static RandomAccessFile dataInput;//read(byte[] b,off,dataLength)其中,表示把所有数据读到b中，off为数据在b中放置的起始位置;
    public static int dataLength ;//读取记录数据的长度
    public static byte dataBuffer[];//缓存一组数据
    public static long seek;//随机读取数据的指针，指针指向当前开始读取Byte数据的位置
    public static boolean HAVE_READ_FINISEH = false;//文件读取结束标志位，作用是控制histhrDimModel中的数据显示线程中的while循环可以安全的结束
    public static long fileLength;//记录数据文件的长度
    public static int[] tunnelAdata;//四个通道的数据
    public static int[] tunnelA1data;//
    public static int[] tunnelBdata;//
    public static int[] tunnelB1data;//
    public static int fileHeadLength;
    private static int FILE_END_PART_LENGTH = 16;//文件的尾部长度16byte，为一个8byte的结束时间和一个byte的正确结束标志位
    public static float[] clabiraA;
    public static float[] clabiraB;
    public static long fileRecordTimeMS;
    public static long meanTimePerData;
    public static long pureDataLength;
    public static boolean SHOW_DATA_THREAT_FLAG = false;//3维显示数据线程开始显示一组数据的开始和结束标志位
    public static FiberManager fiberManager = new FiberManager();
    private static Context context;

    public static boolean iniReadDataFile(String filepath, Activity activity) throws Exception {//初始化读取数据
        context = activity.getApplicationContext();
        boolean isIni = true;
        dataInput = new RandomAccessFile(filepath, "r");//开启新的随机输入流
        fileLength = getFileLength(filepath);//获取要读取文件的长度
        Log.e("ini", "1");
        if (checkFile(dataInput)) {
            Log.e("ini", "2");
            getFileParameter(dataInput);
            seek = fileHeadLength;//数据读取位置为文件的第一个位置
            HAVE_READ_FINISEH = false;//读取数据标志位为false，表示数据读取没有完成
            SHOW_DATA_THREAT_FLAG = false;//设置显示数据的线程为结束
        } else {
            isIni = false;
        }
        return isIni;
    }

    public static void moveSeek() throws IOException {//将随机读取数据的指针移动到指定的位置
        if ((seek) >= fileLength - FILE_END_PART_LENGTH) {//如果seek指针移动到了文件的末尾，则标识文件读取完成
            seek = 0;//文件读取完成后将seek置0，以便下一次读取
            HAVE_READ_FINISEH = true;//将文件读取完成标志位置为true
        } else
            dataInput.seek(seek);//将读数据指针移动到要读取的位置,seek为绝对位置，不会因多调用而累加
    }

    public static void readonce() throws IOException {//读取四通道的指定光纤长度的数据，这个操作要放到线程中进行，防止数据多线程操作中的数据不安全
        for (int i = 0; i < dataLength; i++) {//从文件中读取指定长度的数据存放到数组中
            dataInput.seek(seek + i);
            dataBuffer[i] = dataInput.readByte();
        }

    }

    public static long getFileLength(String file) {//获取文件的长度
        long filelegth = 0;
        File f = new File(file);
        if (f.exists() && f.isFile()) {
            filelegth = f.length();
        } else {
            Log.e("err", "file doesn't exist or is not a file");
        }
        return filelegth;
    }

    public static void closeReadStream() throws IOException {//关闭数据流
        dataInput.close();
    }

    public static int[] bytes2Int(byte[] bytestyle) {//将2个Byte数据合并为一个整数，注意java中的Byte为-128到127
        int p;
        int p1;
        int i1 = 0;
        int[] combination = new int[bytestyle.length / 2];//combination用于储存合并后的16bit数据
        // for (int i = 0; i < resourceObj.dataObj.length; i++) {//报出空指针异常的原因是接收数据还没有处理完，线程就跳转到了这里
        for (int i = 0; i < bytestyle.length; i = i + 2) {//数据组合

            if (bytestyle[i] < 0) {
                p = 256 + bytestyle[i];

            } else {
                p = bytestyle[i];
            }

            if (bytestyle[i + 1] < 0) {
                p1 = (256 + bytestyle[i + 1]) << 8;

            } else {
                p1 = bytestyle[i + 1] << 8;
            }
            combination[i1] = p + p1;
            i1 = i1 + 1;
        }
        return combination;
    }

    public static void decodeData() {//将数据分为4个通道
        //Log.e("decode","解码数据"+Integer.valueOf(dataBuffer.length).toString());
        int[] dd = bytes2Int(dataBuffer);

       // Log.e("组合数据","ok");
        fiberManager.decodeData(dd);
    }

    public static void getFileParameter(RandomAccessFile randomAccessFile) throws Exception {//获取的参数有每个通道的数据长度，光纤的条数,文件的创建时间，光纤的标定温度，文件结束的时间
        /**
         * 读取光纤的长度，光纤的长度存储在文件的起始处为一个int数据占4 byte
         */
        randomAccessFile.seek(0);
        byte[] fiberLength = new byte[4];
        randomAccessFile.read(fiberLength);
        int fiberLeng = getInt(fiberLength);//获取每个通道的数据长度
       // Log.e("光纤长度= ",Integer.valueOf(fiberLeng).toString());
        /**
         * 读取光纤的通道个数,该参数存储在光纤长度之后也是一个int数据占用4字节
         */
        randomAccessFile.seek(fiberLength.length);
        byte[] fiberNumByte = new byte[4];
        randomAccessFile.read(fiberNumByte);
        int fiberNum = getInt(fiberNumByte);
      //  Log.e("通道个数= ",Integer.valueOf(fiberNum).toString());
        /**
         * 读取fibeId和对应的标定数据，根据上一步得出的通道个数进行遍历，通道编号存储在通道个数之后，为char类型数据，占用2字节，根据
         * 读取的fiberId创建对应编号的光纤并设置光纤长度和标定数据，将其添加到fiberManager中
         */
        for (int i = 0; i < fiberNum; i++) {
            randomAccessFile.seek(fiberLength.length + fiberNumByte.length + i * 2);//指针移动到fibeId的
            byte[] fiberId = new byte[2];
            randomAccessFile.read(fiberId);
            char id = byteToChar(fiberId);
            byte[] cla = new byte[(fiberLeng + 1) * 4];
            randomAccessFile.seek(fiberLength.length + fiberNumByte.length + 2 * fiberNum + i * cla.length);//移动到fiberId对应的标定温度数据起始处
            switch (id) {
                case 'A':
                    FiberA fiberA = FiberA.createFiberA();
                    fiberA.setFiberLength(fiberLeng);
                    randomAccessFile.read(cla);
                    fiberA.caliPSA = getFloatArry(cla);
                    fiberManager.addFiber('A');
                    break;
                case 'B':
                    FiberB fiberB = FiberB.createFiberB();
                    fiberB.setFiberLength(fiberLeng);
                    randomAccessFile.read(cla);
                    fiberB.caliPSA = getFloatArry(cla);
                    fiberManager.addFiber('B');
                    break;
                case 'C':
                    FiberC fiberC = FiberC.createFiberC();
                    fiberC.setFiberLength(fiberLeng);
                    randomAccessFile.read(cla);
                    fiberC.caliPSA = getFloatArry(cla);
                    fiberManager.addFiber('C');
                    break;
                case 'D':
                    FiberD fiberD = FiberD.createFiberD();
                    fiberD.setFiberLength(fiberLeng);
                    randomAccessFile.read(cla);
                    fiberD.caliPSA = getFloatArry(cla);
                    fiberManager.addFiber('D');
                    break;
            }
        }

        /**
         * 读取文件创建时间，该数据是一个long型数据占用8byte，位于标定数据之后
         */
        randomAccessFile.seek(fiberLength.length + fiberNumByte.length + 2 * fiberNum + 4 * (fiberLeng + 1) * fiberNum);
        byte[] startTimeByte = new byte[8];
        randomAccessFile.read(startTimeByte);
        long startTime = getLong(startTimeByte);
        //Log.e("文件创建时间",Long.valueOf(startTime).toString());
        /**
         * 读取文件结束时间，该数据是一个long型数据占用8byte，位于文件的尾部倒数8byte，文件的最后8byte为判断文件完整性标志位
         */
        //Log.e("完场","0");
        randomAccessFile.seek(fileLength -16);
        //Log.e("moveseek","1");
        byte[] endTimeBytes = new byte[8];
        //Log.e("newbyte","2");
        randomAccessFile.read(endTimeBytes);
        //Log.e("read","3");
        //Log.e("end",Byte.valueOf(endTimeBytes[0]).toString()+Byte.valueOf(endTimeBytes[1]).toString());
        long endTime = getLong(endTimeBytes);
        //Log.e("文件结束时间",Long.valueOf(endTime).toString());

        fileRecordTimeMS = endTime - startTime;
        fileHeadLength = fiberLength.length + fiberNumByte.length + startTimeByte.length + 2 * fiberNum + 4 * fiberNum * (fiberLeng + 1);
        meanTimePerData = fileRecordTimeMS * (fiberNum * fiberLeng * 2 * 2) / (fileLength - fileHeadLength - 16);//存每帧数据所用的时间
        pureDataLength = fileLength - fileHeadLength - 16;//纯数据长度
        Log.e("纯的数据长度 =",Long.valueOf(pureDataLength).toString());
        dataLength=fiberNum * fiberLeng * 2 * 2;
        dataBuffer = new byte[dataLength];//重新设置数组
        Log.e("每帧数据长度",Integer.valueOf(dataLength).toString());
        //Log.e("每个数据存储的平均时间", Long.valueOf(meanTimePerData).toString());
        randomAccessFile.seek(0);//文件参数信息读取完毕设置seek到文件的初始位置
    }


    public static boolean checkFile(RandomAccessFile randomAccessFile) throws Exception {//移动到读取文件结束时间的前面读取指示位，为一个long类型的数字，值为0
        boolean fileCheckOk = true;
        byte[] ind = new byte[8];
        randomAccessFile.seek(fileLength - 8);
        randomAccessFile.read(ind);
        long indicator = getLong(ind);
        if (indicator == 0) {//如果结束前的时间的指示位为0，说明文件是完整的
            Log.e("数据文件完整检测", "------ 文件完整");
        } else {
            Log.e("数据文件完整检测", "------ 文件损坏");

            fileCheckOk = false;
        }
        randomAccessFile.seek(0);
        return fileCheckOk;

    }

    private static long getLong(byte[] bytes) {//将byte数组转换为long型
        return (0xffL & (long) bytes[0]) | (0xff00L & ((long) bytes[1] << 8)) | (0xff0000L & ((long) bytes[2] << 16)) | (0xff000000L & ((long) bytes[3] << 24))
                | (0xff00000000L & ((long) bytes[4] << 32)) | (0xff0000000000L & ((long) bytes[5] << 40)) | (0xff000000000000L & ((long) bytes[6] << 48)) | (0xff00000000000000L & ((long) bytes[7] << 56));
    }

    private static int getInt(byte[] bytes) {//将byte数组转换为int值
        return (0xff & bytes[0]) | (0xff00 & (bytes[1] << 8)) | (0xff0000 & (bytes[2] << 16)) | (0xff000000 & (bytes[3] << 24));
    }

    private static float getFloat(byte[] bytes) {//将byte数组转换为float值
        return Float.intBitsToFloat(getInt(bytes));
    }

    private static float[] getFloatArry(byte[] bytes) {//将一个byte 数组装换为一个float数组
        float[] a = new float[bytes.length / 4];
        int j = 0;
        for (int i = 0; i < a.length; i++) {
            a[i] = getFloat(Arrays.copyOfRange(bytes, j, j + 4));
            j = j + 4;
        }
        return a;
    }

    private static void printHexString(byte[] b) {//将byte数组以16进制的方式打印
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            Log.e("以16进制打印byte数组", hex.toUpperCase());
        }

    }

    public static char byteToChar(byte[] b) {
        char c = (char) (((b[0] & 0xFF) << 8) | (b[1] & 0xFF));
        return c;
    }
}



