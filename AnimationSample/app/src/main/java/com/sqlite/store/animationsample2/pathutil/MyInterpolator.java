package com.sqlite.store.animationsample2.pathutil;

import android.animation.TimeInterpolator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Interpolator;

/**
 * Created by LiuJiangHao on 15/8/4.
 * 滑动距离小于屏幕距离一半时，插值由滑动距离决定，滑动距离超过一般时插值根据时间计算
 */
public class MyInterpolator extends View implements View.OnTouchListener {

    private float mStartX;  //触摸摁下时x轴坐标
    private float mStartY;  //触摸摁下时y轴坐标
    private float distanceX;//X轴移动距离
    private float screenWidth;//屏幕宽度

    public float interpolatoredValue;

    public MyInterpolator(Context context) {
        super(context);
        this.setOnTouchListener(this);
    }

    public MyInterpolator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyInterpolator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mStartX = event.getX();
                mStartY = event.getY();
                Log.i("MyInterpolator", "mStartX="+mStartX);
                break;
            case MotionEvent.ACTION_MOVE:
                distanceX = getX()-mStartX;
                Log.i("MyInterpolator", "distanceX="+distanceX);
                //设置插值
                if(Math.abs(distanceX)<screenWidth) {
                    interpolatoredValue = distanceX / screenWidth;
                }else{
                    interpolatoredValue = -1;
                }
                Log.i("MyInterpolat", "interpolatorValue="+interpolatoredValue);
        }
        return false;
    }

    public void setScreenWidth(float width){
        this.screenWidth = width;
    }
}
