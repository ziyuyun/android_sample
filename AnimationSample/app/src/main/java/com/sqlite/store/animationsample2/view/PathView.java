package com.sqlite.store.animationsample2.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by LiuJiangHao on 15/8/3.
 */
public class PathView extends View {
    public PathView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PathView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint pathPaint = new Paint();
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setStrokeWidth(3.5f);
        Path path = new Path();
        path.addCircle(getWidth()/2, getHeight()/2, 2*getWidth()/7, Path.Direction.CW);

        path.addCircle(2*getWidth()/5, 3*getHeight()/7, 5*pathPaint.getStrokeWidth(), Path.Direction.CW);

        path.addCircle(3*getWidth()/5, 3*getHeight()/7, 5*pathPaint.getStrokeWidth(), Path.Direction.CW);

        path.addArc(new RectF(
                        getWidth() / 2 - 2 * getWidth() / 7,
                        getHeight() / 2 - 3 * getWidth() / 7,
                        getWidth() / 2 + 2 * getWidth() / 7,
                        getHeight() / 2 + getWidth() / 7
                ),
                60,
                60);
        pathPaint.setAntiAlias(true);
        canvas.drawPath(path, pathPaint);
    }
}
