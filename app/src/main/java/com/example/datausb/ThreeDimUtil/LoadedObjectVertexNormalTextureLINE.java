package com.example.datausb.ThreeDimUtil;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

//加载后的物体——仅携带顶点信息，颜色随机
public class LoadedObjectVertexNormalTextureLINE
{
	int mProgram;//自定义渲染管线着色器程序id
    int muMVPMatrixHandle;//总变换矩阵引用
    int muMMatrixHandle;//位置、旋转变换矩阵
    int maPositionHandle; //顶点位置属性引用
    int maNormalHandle; //顶点法向量属性引用
    int maLightLocationHandle;//光源位置属性引用
    int maCameraHandle; //摄像机位置属性引用
    int maColor; //顶点纹理坐标属性引用
    public float [] co;
    String mVertexShader;//顶点着色器代码脚本
    String mFragmentShader;//片元着色器代码脚本

	FloatBuffer   mVertexBuffer;//顶点坐标数据缓冲
	FloatBuffer   mNormalBuffer;//顶点法向量数据缓冲
	FloatBuffer   mColorBuffer;//顶点纹理坐标数据缓冲
    public int vCount=0;

    public LoadedObjectVertexNormalTextureLINE(MySurfaceView mv, float[] vertices)
    {    	
    	//初始化顶点坐标与着色数据
    	initVertexData(vertices);
    	//初始化shader        
    	initShader(mv);
    }
    
    //初始化顶点坐标与着色数据的方法
    public void initVertexData(float[] vertices)
    {
    	//顶点坐标数据的初始化================begin============================
    	vCount=vertices.length/3;   
		co=new float[vCount*4];
        //创建顶点坐标数据缓冲
        //vertices.length*4是因为一个整数四个字节
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length*4);
        vbb.order(ByteOrder.nativeOrder());//设置字节顺序
        mVertexBuffer = vbb.asFloatBuffer();//转换为Float型缓冲
        mVertexBuffer.put(vertices);//向缓冲区中放入顶点坐标数据
        mVertexBuffer.position(0);//设置缓冲区起始位置
        //特别提示：由于不同平台字节顺序不同数据单元不是字节的一定要经过ByteBuffer
        //转换，关键是要通过ByteOrder设置nativeOrder()，否则有可能会出问题
        //顶点坐标数据的初始化================end============================
        /**
        //顶点法向量数据的初始化================begin============================  
        ByteBuffer cbb = ByteBuffer.allocateDirect(normals.length*4);
        cbb.order(ByteOrder.nativeOrder());//设置字节顺序
        mNormalBuffer = cbb.asFloatBuffer();//转换为Float型缓冲
        mNormalBuffer.put(normals);//向缓冲区中放入顶点法向量数据
        mNormalBuffer.position(0);//设置缓冲区起始位置
        //特别提示：由于不同平台字节顺序不同数据单元不是字节的一定要经过ByteBuffer
        //转换，关键是要通过ByteOrder设置nativeOrder()，否则有可能会出问题
        //顶点着色数据的初始化================end============================
         */
        //顶点纹理坐标数据的初始化================begin============================  
        ByteBuffer tbb = ByteBuffer.allocateDirect(co.length*4);
        tbb.order(ByteOrder.nativeOrder());//设置字节顺序
        mColorBuffer = tbb.asFloatBuffer();//转换为Float型缓冲
        mColorBuffer.put(co);//向缓冲区中放入顶点纹理坐标数据
        mColorBuffer.position(0);//设置缓冲区起始位置
        //特别提示：由于不同平台字节顺序不同数据单元不是字节的一定要经过ByteBuffer
        //转换，关键是要通过ByteOrder设置nativeOrder()，否则有可能会出问题
        //顶点纹理坐标数据的初始化================end============================

    }

    //初始化shader
    public void initShader(MySurfaceView mv)
    {
    	//加载顶点着色器的脚本内容
        mVertexShader= ShaderUtil.loadFromAssetsFile("vertexline.sh", mv.getResources());
        //加载片元着色器的脚本内容
        mFragmentShader=ShaderUtil.loadFromAssetsFile("fragline.sh", mv.getResources());
        //基于顶点着色器与片元着色器创建程序
        //获取程序中总变换卣笠�
        mProgram = ShaderUtil.createProgram(mVertexShader, mFragmentShader);
        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        //获取位置、旋转变换矩阵引用
        //muMMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMMatrix");
        //Log.d("即将创建程序","ok");
        //获取位置、旋转变换矩阵引用
        muMMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMMatrix");
        //获取程序中顶点位置属性引用  
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        //获取程序中顶点颜色属性引用
        maColor= GLES20.glGetAttribLocation(mProgram, "aColor");

    }
    
    public void drawSelf()
    {        
    	 //制定使用某套着色器程序
    	 GLES20.glUseProgram(mProgram);
         //将最终变换矩阵传入着色器程序
         GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, MatrixState.getFinalMatrix(), 0);
         //将位置、旋转变换矩阵传入着色器程序
         GLES20.glUniformMatrix4fv(muMMatrixHandle, 1, false, MatrixState.getMMatrix(), 0);
         // 将顶点位置数据传入渲染管线
         GLES20.glVertexAttribPointer  
         (
         		maPositionHandle,   
         		3, 
         		GLES20.GL_FLOAT, 
         		false,
                3*4,   
                mVertexBuffer
         );
        //顶点纹理坐标数据的初始化================begin============================
        ByteBuffer tbb = ByteBuffer.allocateDirect(co.length*4);
        tbb.order(ByteOrder.nativeOrder());//设置字节顺序
        mColorBuffer = tbb.asFloatBuffer();//转换为Float型缓冲
        mColorBuffer.put(co);//向缓冲区中放入顶点纹理坐标数据
        mColorBuffer.position(0);//设置缓冲区起始位置
        GLES20.glVertexAttribPointer
                (
                        maColor,
                        4,
                        GLES20.GL_FLOAT,
                        false,
                        4*4,
                        mColorBuffer
                );/**
         //将顶点法向量数据传入渲染管线
         GLES20.glVertexAttribPointer  
         (
        		maNormalHandle, 
         		3,   
         		GLES20.GL_FLOAT, 
         		false,
                3*4,   
                mNormalBuffer
         );   
         //为画笔指定顶点纹理坐标数据
         GLES20.glVertexAttribPointer  
         (
        		maTexCoorHandle, 
         		2, 
         		GLES20.GL_FLOAT, 
         		false,
                2*4,   
                mTexCoorBuffer
         );
     */
         //启用顶点位置、法向量、纹理坐标数据
         GLES20.glEnableVertexAttribArray(maPositionHandle);
         GLES20.glEnableVertexAttribArray(maColor);
        // GLES20.glEnableVertexAttribArray(maNormalHandle);
         //GLES20.glEnableVertexAttribArray(maTexCoorHandle);
         //绑定纹理
        // GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
         //GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId);
         //绘制加载的物体
        GLES20.glLineWidth(5);
         GLES20.glDrawArrays(GLES20.GL_LINE_STRIP,0, vCount);
    }
}
