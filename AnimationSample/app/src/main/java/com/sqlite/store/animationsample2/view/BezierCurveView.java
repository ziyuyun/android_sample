package com.sqlite.store.animationsample2.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

import com.sqlite.store.animationsample2.pathutil.EPointF;
import com.sqlite.store.animationsample2.pathutil.PolyBezierPathUtil;

import java.util.List;

/**
 * Created by LiuJiangHao on 15/8/3.
 */
public class BezierCurveView extends View {
    private List<EPointF> knots;
    public BezierCurveView(Context context, List<EPointF> knots) {
        super(context);
        this.knots = knots;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        PolyBezierPathUtil pathUtil = new PolyBezierPathUtil();
        Path path = pathUtil.computePathThroughKnots(knots);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(3.5f);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(path, paint);
    }
}

