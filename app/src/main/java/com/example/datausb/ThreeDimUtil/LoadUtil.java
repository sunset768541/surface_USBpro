package com.example.datausb.ThreeDimUtil;

import android.content.res.Resources;
import android.util.Log;

import com.threed.jpct.SimpleVector;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class LoadUtil 
{

	public static SimpleVector[] loadFromFile(FileInputStream fname) throws IOException
	{

		//返回一个Polyline的集合，然后将Ployline设置后加入到world

		ArrayList<SimpleVector> alv=new ArrayList<SimpleVector>();
	//	alv.add(new SimpleVector(0,0,0));
		try
		{
			InputStreamReader isr=new InputStreamReader(fname);
			BufferedReader br=new BufferedReader(isr);
			String temps=null;

			while((temps=br.readLine())!=null)
			{
				String[] tempsa=temps.split("[ ]+");
				if(tempsa[0].trim().equals("v"))
				{
					alv.add(new SimpleVector(Float.parseFloat(tempsa[1]), Float.parseFloat(tempsa[2]), Float.parseFloat(tempsa[3])));
				}
			}
			Log.e("加载线","ok leng="+ Integer.valueOf(alv.size()).toString());
		}
		catch(Exception e)
		{
			Log.d("load error", "load error");
			e.printStackTrace();
		}
		SimpleVector[] sp=new SimpleVector[alv.size()];
		for (int i=0;i<alv.size();i++){
			sp[i]=alv.get(i);
		}
	//	Polyline pp=new Polyline(sp, RGBColor.GREEN);
		return sp;
	}
}
