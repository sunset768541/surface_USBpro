package com.example.datausb;

import android.widget.EditText;

import java.io.FileWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Created by sunset on 16/5/26.
 * 该模块为字符串存储模块将字符串存储到sd卡的指定文件
 */
public class StrStore {
    public static String s1;
    static FileWriter oss;
    //public static String s2;
    public static void cresave(){
        try {
             oss=new FileWriter("/mnt/external_sd/temdata.txt");

        }
        catch (Exception e){

        }
    }

    public static void saves() {
        try {
            oss.write(s1);
        }
        catch (Exception e){

        }

       // oss.println(s2);
    }
}
