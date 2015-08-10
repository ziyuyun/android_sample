package com.sqlite.store.animationsample2.pathutil;

import android.animation.TimeInterpolator;
import android.graphics.Interpolator;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class PathAnimation extends Animation {
    private PathMeasure measure;
    private float[] pos = new float[2];
    public PathAnimation(Path path) {
        measure = new PathMeasure(path, false);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t){
        measure.getPosTan(measure.getLength() * interpolatedTime, pos,null);
        t.getMatrix().setTranslate(pos[0], pos[1]);
        Log.i("PathAnimation", "interpolatedTime="+interpolatedTime);
    }

}