package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import java.util.ArrayList;
import java.util.List;
import androidx.appcompat.widget.AppCompatImageView;

// 显示圆点的View
public class CircleView extends AppCompatImageView {

    private int circleId = 0;
    private boolean deleteFlag = false;

    private static List<Circle> circles=new ArrayList<>();

    public CircleView(Context context) {
        super(context);
    }

    public CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public static List<Circle> getCircles(){
        return circles;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(MyApp.reset == true){
            circles.clear();
            MyApp.reset = false;
        }
        super.onDraw(canvas);
        Paint paint=new Paint();
        for (Circle circle : circles) {
            circle.drawSelf(canvas,paint);
        }
    }

    float eventX;
    float eventY;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //获取手指的行为
        int action=event.getAction();
        int action_code=action&0xff;//取低八位，即事件的动作（按下、抬起、滑动）
        //手指的下标Index
        int pointIndex=action>>8;//取高八位，即时间中触控点的信息

        //获取手指的名字ID
        int pointId=event.getPointerId(pointIndex);
        //多点触摸按下时，状态为5
        if (action_code>=5){
            action_code-=5;
        }

        switch (action_code){
            case MotionEvent.ACTION_DOWN://按下
                //获取手指的坐标
                eventX=event.getX(pointIndex);
                eventY=event.getY(pointIndex);
                break;
            case MotionEvent.ACTION_UP://松开
                // 若为点击delete按钮，则删除圆点
                if (MyApp.delete == true){
                    deleteSearch(eventX,eventY);
                }else{
                    //实例化圆
                    circleId++;
                    Circle circle=new Circle(eventX,eventY,pointId,circleId);
                    //将圆添加到集合中
                    circles.add(circle);
                }
                break;
        }
        //重新调用onDraw 重绘
        invalidate();
        return true;
    }

    public void deleteSearch(float eventX, float eventY){
        for (int i=0;i<circles.size();i++){
            // 圆点坐标点落在一定范围内，则判该圆点为被点击圆点
            if( Math.abs(eventX-circles.get(i).x)<50f && Math.abs(eventY-circles.get(i).y)<50f ){
                circles.remove(circles.get(i));
            }
        }
    }

}