package com.example.test;

import android.graphics.Canvas;
import android.graphics.Paint;

public class Circle {
    public float x;
    public float y;
    public float imgWidth; // 图片长度
    public float imgHeight;// 图片高度
    public float imgx; // 图片x比例
    public float imgy; // 图片y比例
    public int r=10;
    public int pointId;
    public int circleId;
    public int color;
    public int red,green,blue;


    public Circle(float x, float y, int pointId, int circleId){
        this.x = x;
        this.y = y;
        this.pointId = pointId;
        this.circleId = circleId;
        color = MyApp.getColorTips();//圆点的颜色
    }

    public void drawSelf(Canvas canvas, Paint paint){
        paint.setColor(color);
        canvas.drawCircle(x,y,r,paint);
    }
}