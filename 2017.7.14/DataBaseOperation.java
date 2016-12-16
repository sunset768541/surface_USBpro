package com.example.datausb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.regex.Pattern;

/**
 * Created by sunset on 16/7/1.
 */
public class DataBaseOperation {
   public static   SQLiteDatabase mDatabase;
   public static DataBaseOperation mDataBaseOperation;
    private static Context mContex;
    /***********************************************
     * 数据库的操作方法
     **********************************************/
   private DataBaseOperation(){}
    public static DataBaseOperation getDataBase(Context context){
        if (mDataBaseOperation==null){
            mDataBaseOperation=new DataBaseOperation();
            mContex=context;
            initialDataBase();
        }
        return mDataBaseOperation;
    }
    private static void initialDataBase(){
        mDatabase = SQLiteDatabase.openOrCreateDatabase(mContex.getFilesDir().toString() + "datausb.db3", null);
    }

    //创建或者获得表格的方法
    public void creatOrGetTable(String creattable) {
        mDatabase.execSQL(creattable);
        //Log.d("数据库操作", "数据库建立完成");
    }

    //向数据库中插入数据,执行一次就向表格中插入一组数据
    public void insert(SQLiteDatabase db, int currrenttemp, float[] tuebdata, String tablename) {
        //实例化常量值
        ContentValues cValue = new ContentValues();
        cValue.put("calibtem", currrenttemp);
        String ss = "";
        for (int i = 0; i < tuebdata.length; i++) {
            ss = ss + Float.toString(tuebdata[i]) + "!!";
        }
        cValue.put("tubedata", ss);
        //调用insert()方法插入数据
        db.insert(tablename, null, cValue);
    }

    //从数据库中读取数据
    public float[] getFromDataBase(String tablename) {
        Cursor cursor = mDatabase.query(tablename, null, null, null, null, null, null);
        //moveToFirst为指针指向了表格的行数
        cursor.moveToFirst();
        int currenttem = cursor.getInt(1);//得到第一行的第二个数据
        String tubedata = cursor.getString(2);//得到第一行的第三个数据
        String[] result = Pattern.compile("!!").split(tubedata);
        float[] temp = new float[result.length + 1];
        temp[result.length] = currenttem;//最后一个存放当前的额温度
        for (int i = 0; i < result.length; i++) {
            temp[i] = Float.parseFloat(result[i]);
        }
        return temp;
    }

    //更新数据库中的数据
    public void updataDataBase(int currenttemp, float[] tubedata, String tablenaem) {
        String ss = "";
        for (int i = 0; i < tubedata.length; i++) {
            ss = ss + Float.toString(tubedata[i]) + "!!";
        }
        ContentValues cValue = new ContentValues();
        cValue.put("calibtem", currenttemp);//
        cValue.put("tubedata", ss);
        int affet = mDatabase.update(tablenaem, cValue, "_id=?", new String[]{Integer.toString(1)});//修改主健值为1的一行中的数据
        if (affet == 0) {//这样就用updata储存数据而不必用insert了，因为如果每次都标定都用insert那会使数据库一直在递增数据，这样就保证了数据库中的表格中只包含一行标定数据
            insert(mDatabase, currenttemp, tubedata, tablenaem);
        }
    }


}
