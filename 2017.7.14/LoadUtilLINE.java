package com.example.datausb;

import android.content.res.Resources;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

public class LoadUtilLINE
{


	//从obj文件中加载携带顶点信息的物体，并自动计算每个顶点的平均法向量
    public static LoadedObjectVertexNormalTextureLINE loadFromFile
    (String fname, Resources r,MySurfaceView mv)
    {
    	//加载后物体的引用
    	LoadedObjectVertexNormalTextureLINE lo=null;
    	//原始顶点坐标列表--直接从obj文件中加载
    	ArrayList<Float> alv=new ArrayList<Float>();
    	//顶点组装面索引列表--根据面的信息从文件中加载
    	ArrayList<Integer> alFaceIndex=new ArrayList<Integer>();
    	//结果顶点坐标列表--按面组织好
    	ArrayList<Float> alvResult=new ArrayList<Float>();    	
    	//平均前各个索引对应的点的法向量集合Map
    	//此HashMap的key为点的索引， value为点所在的各个面的法向量的集合
    	//HashMap<Integer,HashSet<Normal>> hmn=new HashMap<Integer,HashSet<Normal>>();
    	//原始纹理坐标列表
    	ArrayList<Float> alt=new ArrayList<Float>();  
    	//纹理坐标结果列表
    	ArrayList<Float> altResult=new ArrayList<Float>();  
    	
    	try
    	{
    		InputStream in=r.getAssets().open(fname);
    		InputStreamReader isr=new InputStreamReader(in);
    		BufferedReader br=new BufferedReader(isr);
    		String temps=null;
    		
    		//扫面文件，根据行类型的不同执行不同的处理逻辑
		    while((temps=br.readLine())!=null) 
		    {
		    	//用空格分割行中的各个组成部分
		    	String[] tempsa=temps.split("[ ]+");
		      	if(tempsa[0].trim().equals("v"))
		      	{//此行为顶点坐标
		      	    //若为顶点坐标行则提取出此顶点的XYZ坐标添加到原始顶点坐标列表中
		      		alv.add(Float.parseFloat(tempsa[1]));
		      		alv.add(Float.parseFloat(tempsa[2]));
		      		alv.add(Float.parseFloat(tempsa[3]));
		      	}

		    } 
		    
		    //生成顶点数组
		    int size=alv.size();
		    float[] vXYZ=new float[size];
		    for(int i=0;i<size;i++)
		    {
		    	vXYZ[i]=alv.get(i);
		    }
			Random rand = new Random();
			float [] colors=new float[(vXYZ.length/3)*4];
			for(int i=0;i<(vXYZ.length/3)*4;i++){
				colors[i]=rand.nextFloat();
			}

		    //创建3D物体对象
		    lo=new LoadedObjectVertexNormalTextureLINE(mv,vXYZ);
    	}
    	catch(Exception e)
    	{
    		Log.d("load error", "load error");
    		e.printStackTrace();
    	}    	
    	return lo;
    }
}
