package com.sqlite.store.animationsample2.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Display;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.sqlite.store.animationsample2.pathutil.PathUtil;

import java.util.List;

/**
 * Created by LiuJiangHao on 15/8/14.
 */
public class CelestialBodyView {
    private static final int DOWN_OFFSET = 1000; //天体下移时的最大偏移距离
    private static final float INTENSITY = 0.2f; //Bezier曲线曲度
    private static final int DURATION = 1000;     //悬浮动画时常
    private ImageView mIvCelestial;
    private Path mPath;
    private PathMeasure mPathMeasure;
    private Matrix mMatrix;
    private float[] mCurrPos = new float[2];                  //天体变换过程中的时刻位置点
    private PathUtil.CPoint mStopPoint;       //天体前景停靠点
    private List<PathUtil.CPoint> mPathPointList;//天体Bezier曲线路径控制点集合
    private ObjectAnimator mFloatAnimator;      //天体悬浮动画
    private int mResID;

    /**
     * 天体飞行（飞近/飞远）
     */
    public void flying(float interpolated){
        mMatrix = new Matrix(mIvCelestial.getImageMatrix());
        mMatrix.setScale(interpolated * interpolated, interpolated * interpolated);
        mIvCelestial.setImageMatrix(mMatrix);
        mIvCelestial.setAlpha(0.7f + interpolated);
        mIvCelestial.setDrawingCacheEnabled(true);
        Drawable drawable = mIvCelestial.getDrawable();
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        mIvCelestial.setDrawingCacheEnabled(false);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mIvCelestial.getLayoutParams();
        layoutParams.height = (int) (bitmap.getHeight() * interpolated * interpolated);
        layoutParams.width = (int) (bitmap.getWidth() * interpolated * interpolated);
        mIvCelestial.setLayoutParams(layoutParams);
        mPathMeasure.getPosTan(mPathMeasure.getLength() * interpolated, mCurrPos, null);
        setCoords(mIvCelestial, mCurrPos[0], mCurrPos[1]);
    }

    /**
     * 天体下移
     */
    public void moveDown(float interpolated){
        setCoords(mIvCelestial, mStopPoint.x, mStopPoint.y + DOWN_OFFSET * interpolated * interpolated);
    }


    /**
     * 天体上移
     */
    public void moveUp(float interpolated){
        setCoords(mIvCelestial, mStopPoint.x, mStopPoint.y + DOWN_OFFSET * (1 - interpolated * interpolated));
    }

    private void setCoords(ImageView view, float x, float y) {
        //计算根据中心点坐标计算左上角的坐标点
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mIvCelestial.getLayoutParams();
        float ltx = x - layoutParams.width / 2;
        float lty = y - layoutParams.height / 2;
        view.setX(ltx);
        view.setY(lty);
    }

    public CelestialBodyView(Activity context, List<PathUtil.CPoint> pathPointList, int resId, String tag) {
        this.mPathPointList = pathPointList;
        initPath(pathPointList);
        this.mResID = resId;
        //initImageView(context, resId);
    }

    public void initImageView(Activity context){
        mIvCelestial = new ImageView(context);
//        mIvCelestial.setImageResource(mResID);
        loadImage(mIvCelestial, mResID);
//        Display display = context.getWindowManager().getDefaultDisplay();
//        loadImage(mIvCelestial, mResID, display.getWidth(), display.getHeight());
        mIvCelestial.setScaleType(ImageView.ScaleType.MATRIX);
    }



    private void initPath(List<PathUtil.CPoint> pathPointList){
        mPath = new Path();
        PathUtil.drawPath(mPath, pathPointList, INTENSITY);
        mPathMeasure = new PathMeasure(mPath, false);
        mStopPoint = pathPointList.get(pathPointList.size() - 1);
    }

    private void initFloatAnimator(){
        float y = calculateYCoords(mStopPoint.y);
        mFloatAnimator = ObjectAnimator.ofFloat(mIvCelestial, "y", y - 10, y + 10);
        mFloatAnimator.setDuration(DURATION);
        mFloatAnimator.setRepeatMode(ValueAnimator.REVERSE);
        mFloatAnimator.setRepeatCount(ValueAnimator.INFINITE);
    }

    private float calculateYCoords(float y){
        return y-mIvCelestial.getMeasuredHeight()/2;
    }

    public void startFloatAnimator(){
        if(mFloatAnimator == null) {
            initFloatAnimator();
        }
        mFloatAnimator.start();
    }

    public void stopFloatAnimator(){
        if(mFloatAnimator != null && mFloatAnimator.isRunning()) {
            mFloatAnimator.cancel();
            mFloatAnimator.end();
        }
    }

    /**
     * 设置当前天体在前景浮动
     */
    public void setViewFloat(){
        mMatrix = new Matrix(mIvCelestial.getImageMatrix());
        mMatrix.setScale(1.0f, 1.0f);
        mIvCelestial.setImageMatrix(mMatrix);
        Drawable drawable = mIvCelestial.getDrawable();
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        mIvCelestial.setDrawingCacheEnabled(false);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mIvCelestial.getLayoutParams();
        layoutParams.height = bitmap.getHeight();
        layoutParams.width = bitmap.getWidth();
        mIvCelestial.setLayoutParams(layoutParams);
        setCoords(mIvCelestial, mStopPoint.x, mStopPoint.y);
        mIvCelestial.requestLayout();
        startFloatAnimator();
    }

    public ImageView getView(){
        return mIvCelestial;
    }

    private void loadImage(ImageView imageView, int resId){
        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeResource(imageView.getResources(), resId, options);
//        int originalWidth = options.outWidth;
//        int originalHeight = options.outHeight;
//        int scale = Math.min(Math.round((float)originalHeight/screenHeight), Math.round((float)originalWidth/screenWidth));
//        final float totalPixels = originalWidth * originalHeight;
//
//        final float totalReqPixelsCap = screenWidth * screenHeight * 2;
//
//        while (totalPixels / (scale * scale) > totalReqPixelsCap) {
//            scale++;
//        }
        options.inJustDecodeBounds = false;
        options.inSampleSize = 1;
        options.inScaled = true;
        options.inPreferredConfig = Bitmap.Config.ALPHA_8;
        Bitmap bitmap = BitmapFactory.decodeResource(imageView.getResources(), resId, options);
        imageView.setImageBitmap(bitmap);
        bitmap = null;
    }

    public void flyInitView(){
        mMatrix = new Matrix((mIvCelestial.getImageMatrix()));
        mMatrix.setScale(.0f, .0f);
        mIvCelestial.setImageMatrix(mMatrix);
    }

    public void bottomInitView(){
        mMatrix = new Matrix((mIvCelestial.getImageMatrix()));
        mMatrix.setScale(1.0f, 1.0f);
        mIvCelestial.setImageMatrix(mMatrix);
    }

    public void bottomSetSize(){
        Drawable drawable = mIvCelestial.getDrawable();
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        mIvCelestial.setDrawingCacheEnabled(false);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mIvCelestial.getLayoutParams();
        layoutParams.height = bitmap.getHeight();
        layoutParams.width = bitmap.getWidth();
        mIvCelestial.setLayoutParams(layoutParams);
    }
}
