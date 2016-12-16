package com.example.datausb;

import java.util.Arrays;

/**
 * Created by sunset on 16/7/5.
 */
public class DisplayAdapterUtil {

    //进行屏幕大小适配的方法
    public static float[] displyViewWidthAdapter(float[] needAdapterData, int displayViewWidth) {
        if (needAdapterData.length < displayViewWidth) {
            return needAdapterData;
        } else {
            float[] afterAdapterData = new float [displayViewWidth];//设置屏可以显示在屏幕上的数据长度
            //  int[] dataBuffer;
            int sampleInterval = needAdapterData.length / afterAdapterData.length;
            int afterAdapterDataArrayPionter = 0;
            if (sampleInterval <= 1) {
                afterAdapterData = needAdapterData;
            } else {
                for (int i = 0; i < (needAdapterData.length / sampleInterval) * sampleInterval; i = i + sampleInterval) {//有必要将i先变成整数
                    float[] dataBuffer = Arrays.copyOfRange(needAdapterData, i, i + sampleInterval);
                    afterAdapterData[afterAdapterDataArrayPionter] = getMax(dataBuffer);//这里出现了空指针异常
                    afterAdapterDataArrayPionter = afterAdapterDataArrayPionter + 1;
                }
            }

            return afterAdapterData;
        }
    }
    //对一个数组输出最大值方法
    private static float getMax(float[] dataArray) {
        float max;
        Arrays.sort(dataArray);
        max= dataArray[dataArray.length - 1];
        return max;
    }

    /**
     * 屏幕点数与数据匹配函数
     * 基本思想，利用总的数据长度除以显示控件横向像素点数得出一个整数将这个整数+1就得到了interval，interval的作用就是扩展源数据长度
     * 使其可以整除显示控件的横向像素点数。interval*viewwidth viewwidth就是显示控件（坐标系）的横向像素点数，乘积的结果就是要扩展的
     * 数组的长度。为了保证扩展后的数据图像的趋势与源数组的图像趋势一致，通过对源数组的插值来获得扩展数组。插值的点数就是扩展数组的长度
     * 减去源数组的长度。插值的方法就是在interval的一半处进行插值，所插值通过相邻的两个数据计算平均值得出。并将源数组的数据插入到扩展
     * 数组中。插值完成后，再将源数组中没有插值的最后一段数据全部拷贝到扩展数组中。
     * @param displayViewWidth
     * @param needInterporlationArray
     * @return
     */

    public static float[] arrayInterpolation(int displayViewWidth, float[] needInterporlationArray) {
        if (needInterporlationArray.length < displayViewWidth) {
            return needInterporlationArray;
        } else {

            int integerOnePiexOfDataNumber = needInterporlationArray.length / displayViewWidth + 1;//计算插值后的数组可以整除显示View的宽度，以协助displyViewWidthAdapter函数抽样
            int afterInterporlationArrayLength = integerOnePiexOfDataNumber * displayViewWidth;//扩展数据的长度
            int needInterpolationDataNumber = afterInterporlationArrayLength - needInterporlationArray.length;//需要在源数组中插值的个数
            int interpolationInterval = integerOnePiexOfDataNumber-1;//插值的间隔
            float[] afterIntetpolationArray = new float[afterInterporlationArrayLength];//插值后的数组
            int needInterporlationArrayPointer = 0;//插值数组的指针，指向插值的位置
            for (int i = 0; i < needInterpolationDataNumber; i++) {//插值循环
                //copyOfRange可能发生越界异常，当数组的长度小于2倍的显示控制横向像素点数时发生
                float[] dataBuffer = Arrays.copyOfRange(needInterporlationArray, needInterporlationArrayPointer, needInterporlationArrayPointer + interpolationInterval);//从源数组拷贝出一段数据
                float totalOfDataBufferArray = 0;//dataBufferArray的和
                for (int mm = 0; mm < dataBuffer.length; mm++) {
                    totalOfDataBufferArray = dataBuffer[mm] + totalOfDataBufferArray;
                }
                float interpolationNumber = totalOfDataBufferArray / dataBuffer.length;//计算dataBuffer的平均值获得插值的大小
                afterIntetpolationArray[needInterporlationArrayPointer + interpolationInterval] = interpolationNumber;//将需要插得值放入新的数组中的指定插值位置
                for (int kk = 0; kk < interpolationInterval; kk++) {//将从源数组中拷贝的数据填入到新的数组中的对应位置
                    afterIntetpolationArray[needInterporlationArrayPointer + kk] = dataBuffer[kk];
                }
                needInterporlationArrayPointer = needInterporlationArrayPointer + interpolationInterval + 1;
            }
            //插值完毕后，将剩下的没有插值的一段数据从源数组拷贝到扩展数组中
            int needInterPloArrayPointerPosi = (interpolationInterval) * needInterpolationDataNumber;//源数组插值的结束位置
            int afterInterPloArrayPointerPosi = interpolationInterval * needInterpolationDataNumber + needInterpolationDataNumber;//扩展数组填入数据的结束位置
            int leaftNoInterpoSectionLength = needInterporlationArray.length - needInterPloArrayPointerPosi;//没有插值的一段数据的长度
            float[] noInterpolArraySetion = Arrays.copyOfRange(needInterporlationArray, needInterPloArrayPointerPosi, needInterporlationArray.length);//从源数组拷贝出没有插值的一段数据
            for (int yy = 0; yy < leaftNoInterpoSectionLength; yy++) {//将没有插值的一段数据拷贝到扩展数组的对应位置
                afterIntetpolationArray[yy + afterInterPloArrayPointerPosi] = noInterpolArraySetion[yy];
            }
            return afterIntetpolationArray;
        }
    }
   public static float[] intArray2floatArray(int[] intArray){
       float [] int2float =new float[intArray.length];
       for (int i=0;i<int2float.length;i++){
           int2float[i]=(float)intArray[i];
       }
       return int2float;
   }
}
