package com.example.datausb;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.Log;


import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

/**
 * Created by sunset on 16/7/16.
 */
public class DataChart {
    private double xMax;
    private double xMin;
    private double yMax;
    private double yMin;
    private int xAxisColor;
    private int yAxisColor;
    private int XYNumberCOlor;
    private int scale=1;
    private Point center;
    private float [] margin;//new float[]{40,40,20,20};//左右下上，margin必须定义为float类型，若为int类型，计算的时候回降低精度使得测算不准导致绘图不正确
    private String xLabel="x";
    private String yLabel="y";
    private int textColor;
    private int xGridNumber;//
    private int yGridNumber;
    private int gridColor;
    private int backGroundColor;
    private int marginBackGroundColor;
    private double xPixelsPerUnit;
    private double yPixelsPerUnit;
    private float xNumberUnderXAxisPan;
    private float yNumberLeftYAxisPan;
    private float xLabelUnderXAxisPan;
    private float yLabelLeftYAxisPan;
    private float inixMax;
    private float inixMin;
    private float iniyMax;
    private float iniyMin;
    public DecimalFormat xNumberForm = new DecimalFormat("##");
    public DecimalFormat yNumberForm = new DecimalFormat("##0.0");

    public DataChart(float inixMax,float inixMin,float iniyMax,float iniyMin) {
        this.inixMin=inixMin;
        this.inixMax=inixMax;
        this.iniyMax=iniyMax;
        this.iniyMin=iniyMin;
        ini(inixMax,inixMin,iniyMax,iniyMin);
    }
    public  void  drawAll(Canvas canvas,List<float[]> data,List<Paint> dataPaint){
        canvas.drawRGB(15, 15, 15);//清空屏幕
        drawXYNumber(canvas,getGridColor(),getMargin(),getxMax(),getxMin(),getyMax(),getyMin(),getxGridNumber(),getyGridNumber(),getxNumberUnderXAxisPan(),getyNumberLeftYAxisPan());
        drawXYText(canvas,getTextColor(),getMargin(),getxLabel(),getyLabel(),getxLabelUnderXAxisPan(),getyLabelLeftYAxisPan());
        drawGrid(canvas,getGridColor(),getMargin(),getxMax(),getxMin(),getyMax(),getyMin(),getxGridNumber(),getyGridNumber());
        drawAxis(canvas,getxAxisColor(),getMargin());
        drawPath(canvas,data,false,dataPaint,getMargin());
    }
    public void zoomX(float sclae,float touchX,float touchY){
        float x=(float) getxMin()+touchX/(float) getxPixelsPerUnit();
        if ((x-(x-xMin)/sclae)<inixMin){
            setxMin(inixMin);
        }
        else setxMin(x-(x-xMin)/sclae);

        if ((x+(xMax-x)/sclae)>inixMax){
            setxMax(inixMax);
        }
        else setxMax(x+(xMax-x)/sclae);
        if ((x+(xMax-x)/sclae)<(x-(x-xMin)/sclae)){
            setxMax(inixMax);
            setxMin(inixMax-10);
        }
    }
    public void zoomY(float sclae,float touchX,float touchY){
        float y=(float) getyMin()+touchY/(float) getyPixelsPerUnit();
        if ((y-(y-yMin)/sclae)<iniyMin)
            setyMin(iniyMin);
        else  setyMin(y-(y-yMin)/sclae);
        if ((y+(yMax-y)/sclae)>iniyMax)
            setyMax(iniyMax);
        else
        setyMax(y+(yMax-y)/sclae);
        if (((y+(yMax-y)/sclae)<(y-(y-yMin)/sclae))){
            setyMax(iniyMax);
            setyMin(iniyMax-10);
        }
    }
    private void ini(float xMax,float xMin,float yMax,float yMin){
        setxMax(xMax);
        setxMin(xMin);
        setyMax(yMax);
        setyMin(yMin);
        setxAxisColor(Color.WHITE);
        setyAxisColor(Color.WHITE);
        setScale(1);
        setMargin(new float[]{65,40,40,20});
        setxLabel("x");
        setyLabel("y");
        setTextColor(Color.WHITE);
        setxGridNumber(20);
        setyGridNumber(20);
        setGridColor(Color.argb(255,83,83,83));
        setXYNumberCOlor(Color.argb(255,83,83,83));
        setBackGroundColor(Color.argb(255,15,15,15));
        setMarginBackGroundColor(Color.argb(255,15,15,15));
        setxLabelUnderXAxisPan(30);
        setyLabelLeftYAxisPan(5);
        setxNumberUnderXAxisPan(20);
        setyNumberLeftYAxisPan(15);

    }
    public void resetAxis(){
        setxMax(inixMax);
        setxMin(inixMin);
        setyMax(iniyMax);
        setyMin(iniyMin);
        setScale(1);
    }

    private float[] adapterArray(float []needAdapter,double xMin,double yMin){
        float []afterAdapter=new float[needAdapter.length*2];
        int mm=0;
        for (int i=0;i<needAdapter.length;i++){
            afterAdapter[mm]=(float) (getxPixelsPerUnit()* (i));
            if(-needAdapter[i]>getyMax()){
                afterAdapter[mm+1]=-(float)(getyPixelsPerUnit()*((getyMax()-getyMin())));
            }
            else if (-needAdapter[i]<getyMin()){
                afterAdapter[mm+1]=0;
            }
            else  if (getyMin()<-needAdapter[i]&&-needAdapter[i]<getyMax()){
                afterAdapter[mm+1]=(float) (getyPixelsPerUnit()* (needAdapter[i]+getyMin()));
//                Log.e("need",Float.valueOf(needAdapter[i]).toString());
//                Log.e("min",Double.valueOf(getyMin()).toString());
//                Log.e("am+1",Float.valueOf(afterAdapter[mm+1]).toString());
            }

            mm=mm+2;
        }
        return afterAdapter;
    }
    private  float[] calculateDrawPoints(float p1x, float p1y, float p2x, float p2y) {//调用次数最多60%
        return new float[]{p1x,p1y,p2x,p2y};
    }

    protected void drawLengend(Canvas canvas, float[] points, Paint paint, boolean circular) {

    }
    private void drawXYText(Canvas canvas,int textColor,float[] margins,String xLabelText,String yLabelText,float xLabelUnderXAxisPan,float yLabelLeftYAxisPan) {
        Paint xyLabel=new Paint();
        xyLabel.setStrokeWidth(1);
        xyLabel.setColor(textColor);//字的颜色和xy轴的颜色一致
        canvas.drawText(xLabelText, (canvas.getWidth()-margins[0]-margins[1])/2+margins[0], canvas.getHeight()-margins[2]+xLabelUnderXAxisPan,xyLabel);
        canvas.drawText(yLabelText, yLabelLeftYAxisPan, (canvas.getHeight()-margins[2]-margins[3])/2+margins[3],xyLabel);
    }
    /**
     * 绘制横纵坐标轴
     */
    private void drawAxis(Canvas canvas,int axisColor,float[] margins) {
        Paint xyAxis=new Paint();
        xyAxis.setColor(axisColor);
        xyAxis.setStrokeWidth(1);
        //绘制纵坐标
        canvas.drawLine(margins[0], margins[3], margins[0], canvas.getHeight() - margins[2], xyAxis);
        //绘制横坐标
        canvas.drawLine(margins[0], canvas.getHeight() - margins[2], canvas.getWidth() - margins[1], canvas.getHeight() - margins[2], xyAxis);//绘制坐标轴
    }

    private void drawGrid(Canvas canvas,int gridColor,float[] margins,double xMax,double xMin,double yMax,double yMin,int xGridNumber,int yGridNumber) {
        Paint grid=new Paint();
        grid.setColor(gridColor);
        grid.setStrokeWidth(1);
        //绘制纵Grid
        float xGridPan=(canvas.getWidth()-margins[0]-margins[1])/xGridNumber;
        for (int i=0;i<yGridNumber+1;i++){
            canvas.drawLine(margins[0]+i*xGridPan,margins[3],margins[0]+i*xGridPan,canvas.getHeight()-margins[2],grid);
        }
        //绘制横Grid
        float yGridPan=(canvas.getHeight()-margins[2]-margins[3])/yGridNumber;
        for (int j=0;j<xGridNumber;j++){
            canvas.drawLine(margins[0],margins[3]+j*yGridPan,canvas.getWidth()-margins[1],margins[3]+j*yGridPan,grid);
        }


    }
    private void drawXYNumber(Canvas canvas,int gridColor,float[] margins,double xMax,double xMin,double yMax,double yMin,int xGridNumber,int yGridNumber,float xNumberUnderXAxisPan,float yNumberLeftYAxisPan) {
        Paint grid=new Paint();
        grid.setColor(gridColor);
        grid.setStrokeWidth(1);
        //绘制纵Grid
        float xGridPan=(canvas.getWidth()-margins[0]-margins[1])/xGridNumber;
        for (int i=0;i<yGridNumber+1;i++){
            canvas.drawText(xNumberForm.format(getxMin()+i*((xMax-xMin)/(xGridNumber))),margins[0]+i*xGridPan,canvas.getHeight()-margins[2]+xNumberUnderXAxisPan,grid);
        }
        //绘制横Grid
        float yGridPan=(canvas.getHeight()-margins[2]-margins[3])/yGridNumber;
        for (int j=0;j<xGridNumber+1;j++){
            canvas.drawText(yNumberForm.format(yMax-j*((yMax-yMin)/(yGridNumber))),yNumberLeftYAxisPan,margins[3]+j*yGridPan,grid);
        }


    }
    private void drawPath(Canvas canvas, List<float[]> data, boolean circular,List<Paint> dataPaint,float []margins) {

        if (data.size()<1){
            return;
        }

        setxPixelsPerUnit(canvas,margins);
        setyPixelsPerUnit(canvas,margins);
        canvas.translate(margins[0],canvas.getHeight()-margins[2]);
        for (int datalength=0;datalength<data.size();datalength++){
            Path path = new Path();

//            Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);//开启抗锯齿使得图线变细
//            paint.setStyle(Paint.Style.STROKE);
//            paint.setColor(dataColors[datalength]);
//            paint.setStrokeWidth(1f);

            //取出指定长度的数据进行绘制
            int start=(int)getxMin();
            int end=(int)getxMax();

            if (start<0){
                start=0;
            }
            else if (start>data.get(datalength).length){
                start=data.get(datalength).length;
            }
            if (end>data.get(datalength).length){
                end=data.get(datalength).length;
            }

            else if (end<0){
                end=0;
            }
           // Log.e("start",Integer.valueOf(start).toString());
            //Log.e("end",Integer.valueOf(end).toString());
            float [] sclaedata=Arrays.copyOfRange(data.get(datalength),start,end);
            //Log.e("sclaedatat",Integer.valueOf(sclaedata.length).toString());
            float[]points=adapterArray(sclaedata,getxMin(),getyMin());
            float[] tempDrawPoints;
            if (points.length < 4) {
                return;
            }
            tempDrawPoints = calculateDrawPoints(points[0], points[1], points[2], points[3]);
            path.moveTo(tempDrawPoints[0], tempDrawPoints[1]);
            path.lineTo(tempDrawPoints[2], tempDrawPoints[3]);
            int length = points.length;
            for (int i = 4; i < length; i += 2) {
//                if ((-points[i - 1] < 0 && -points[i + 1] < 0)
//                        || (-points[i - 1] > canvas.getHeight()-margins[3] && -points[i + 1] > canvas.getHeight()-margins[3])) {
//                    continue;
//                }
                if (!circular) {
                    path.moveTo(points[i - 2], points[i - 1]);
                }
                path.lineTo(points[i], points[i + 1]);
            }
            if (circular) {
                path.lineTo(points[0], points[1]);

            }

            canvas.drawPath(path, dataPaint.get(datalength));


        }
    }

    public double getxMax() {
        return xMax;
    }

    public void setxMax(double xMax) {
        this.xMax = xMax;
    }

    public double getxMin() {
        return xMin;
    }

    public void setxMin(double xMin) {
        this.xMin = xMin;
    }

    public double getyMax() {
        return yMax;
    }

    public void setyMax(double yMax) {
        this.yMax = yMax;
    }

    public double getyMin() {
        return yMin;
    }

    public void setyMin(double yMin) {
        this.yMin = yMin;
    }

    public int getxAxisColor() {
        return xAxisColor;
    }

    public void setxAxisColor(int xAxisColor) {
        this.xAxisColor = xAxisColor;
    }

    public int getyAxisColor() {
        return yAxisColor;
    }

    public void setyAxisColor(int yAxisColor) {
        this.yAxisColor = yAxisColor;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public float[] getMargin() {
        return margin;
    }

    public void setMargin(float[] margin) {
        this.margin = margin;
    }

    public String getxLabel() {
        return xLabel;
    }

    public void setxLabel(String xLabel) {
        this.xLabel = xLabel;
    }

    public String getyLabel() {
        return yLabel;
    }

    public void setyLabel(String yLabel) {
        this.yLabel = yLabel;
    }

    public int getxGridNumber() {
        return xGridNumber;
    }

    public void setxGridNumber(int xGridNumber) {
        this.xGridNumber = xGridNumber;
    }

    public int getyGridNumber() {
        return yGridNumber;
    }

    public void setyGridNumber(int yGridNumber) {
        this.yGridNumber = yGridNumber;
    }

    public int getGridColor() {
        return gridColor;
    }

    public void setGridColor(int gridColor) {
        this.gridColor = gridColor;
    }

    public int getBackGroundColor() {
        return backGroundColor;
    }

    public void setBackGroundColor(int backGroundColor) {
        this.backGroundColor = backGroundColor;
    }

    public int getMarginBackGroundColor() {
        return marginBackGroundColor;
    }

    public void setMarginBackGroundColor(int marginBackGroundColor) {
        this.marginBackGroundColor = marginBackGroundColor;
    }

    private double getxPixelsPerUnit() {
        return xPixelsPerUnit;
    }

    private void setxPixelsPerUnit(Canvas canvas,float[]marigins) {
        xPixelsPerUnit= ((canvas.getWidth() - marigins[0]-marigins[1]) / (xMax - xMin));//maxX为横坐标的最大值，minX为横坐标最小值

    }

    private double getyPixelsPerUnit() {
        return yPixelsPerUnit;
    }

    private void setyPixelsPerUnit(Canvas canvas,float[]marigins) {
         yPixelsPerUnit= (float) ((canvas.getHeight() - marigins[2]-marigins[3]) / (yMax - yMin));

    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }


    public float getxNumberUnderXAxisPan() {
        return xNumberUnderXAxisPan;
    }

    public void setxNumberUnderXAxisPan(float xNumberUnderXAxisPan) {
        this.xNumberUnderXAxisPan = xNumberUnderXAxisPan;
    }

    public float getyNumberLeftYAxisPan() {
        return yNumberLeftYAxisPan;
    }

    public void setyNumberLeftYAxisPan(float yNumberLeftYAxisPan) {
        this.yNumberLeftYAxisPan = yNumberLeftYAxisPan;
    }

    public float getxLabelUnderXAxisPan() {
        return xLabelUnderXAxisPan;
    }

    public void setxLabelUnderXAxisPan(float xLabelUnderXAxisPan) {
        this.xLabelUnderXAxisPan = xLabelUnderXAxisPan;
    }

    public float getyLabelLeftYAxisPan() {
        return yLabelLeftYAxisPan;
    }

    public void setyLabelLeftYAxisPan(float yLabelLeftYAxisPan) {
        this.yLabelLeftYAxisPan = yLabelLeftYAxisPan;
    }

    public int getXYNumberCOlor() {
        return XYNumberCOlor;
    }

    public void setXYNumberCOlor(int XYNumberCOlor) {
        this.XYNumberCOlor = XYNumberCOlor;
    }
}
