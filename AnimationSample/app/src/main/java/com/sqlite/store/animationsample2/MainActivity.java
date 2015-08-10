package com.sqlite.store.animationsample2;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.sqlite.store.animationsample2.pathutil.PathUtil;
import com.sqlite.store.animationsample2.pathutil.PathUtil.CPoint;

import com.sqlite.store.animationsample2.pathutil.EPointF;
import com.sqlite.store.animationsample2.pathutil.MyInterpolator;
import com.sqlite.store.animationsample2.pathutil.PolyBezierPathUtil;
import com.sqlite.store.animationsample2.view.MyPageItem;
import com.sqlite.store.animationsample2.view.MyViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements ViewPager.OnPageChangeListener {
    //    private RelativeLayout rlayoutContainer;
    public static final long DURATION = 1000;
    private float screenWidth;
    private float screenHeight;
    private float interpolated;
    private PathMeasure pathMeasure;
    float[] pos = new float[2];
    private Matrix mMatrix;
    private ViewPager mViewPager;
    private boolean isState = false;
    private CPoint endPoint;

    private boolean left = false;
    private boolean right = false;
    private boolean isScrolling = false;

    private ImageView frontImage;
    private ImageView backImage;
    private ObjectAnimator backAnimator;

    private GestureDetector mGestureDetector;
    List<Integer> picList = new ArrayList<Integer>();
    List<Path> pathList = new ArrayList<Path>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        WindowManager wm = getWindowManager();
        screenWidth = wm.getDefaultDisplay().getWidth();
        screenHeight = wm.getDefaultDisplay().getHeight();
        setContentView(R.layout.activity_main);
        frontImage = (ImageView) findViewById(R.id.iv_front);
        frontImage.setScaleType(ImageView.ScaleType.MATRIX);
        backImage = (ImageView) findViewById(R.id.iv_back);
        backImage.setScaleType(ImageView.ScaleType.MATRIX);
        picList.add(R.drawable.pic1);
        picList.add(R.drawable.pic2);
        picList.add(R.drawable.pic3);
        picList.add(R.drawable.pic4);
        picList.add(R.drawable.pic5);

        CPoint p0 = new CPoint(268, 396);
        CPoint p1 = new CPoint(281, 448);
        CPoint p3 = new CPoint(292, 473);
        //CPoint p2 = new CPoint(293, 478);
        CPoint p2 = new CPoint(245, 478);
        CPoint p6 = new CPoint(368, 598);
        CPoint p4 = new CPoint(330, 550);
        CPoint p5 = new CPoint(409, 652);
        CPoint p8 = new CPoint(389, 1014);
        CPoint p7 = new CPoint(427, 722);
        CPoint p9 = new CPoint(414, 822);
        CPoint p10 = new CPoint(384, 846);

//        List<CPoint> centralPointList = new ArrayList<CPoint>();
//        centralPointList.add(new CPoint(293,577));
//        centralPointList.add(new CPoint(462, 423));
//        centralPointList.add(new CPoint(293, 472));
//        centralPointList.add(new CPoint(778, 484));
//        centralPointList.add(new CPoint(264, 264));
        endPoint = p10;
        Path path = new Path();
        Path path2 = new Path();
        Path path3 = new Path();
        Path path4 = new Path();
        Path path5 = new Path();
        List<CPoint> points = new ArrayList<CPoint>();
        points.add(p0);
        points.add(p1);
        points.add(p2);
        points.add(p3);
        points.add(p4);
        points.add(p6);
        points.add(p7);
        points.add(p9);
        points.add(p10);
        PathUtil.drawPath(path, points, 0.2f);
        pathMeasure = new PathMeasure(path, false);
        mViewPager = (ViewPager) findViewById(R.id.vp_container);
        List<Integer> viewIDs = new ArrayList<Integer>();
        viewIDs.add(R.drawable.text1);
        viewIDs.add(R.drawable.text2);
        viewIDs.add(R.drawable.text3);
        viewIDs.add(R.drawable.text4);
        viewIDs.add(R.drawable.text5);
        MyViewPagerAdapter adapter = new MyViewPagerAdapter(viewIDs, this);
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(this);
        backImage.setImageResource(picList.get(0));
        interpolated = .99869794f;
        mMatrix = new Matrix(backImage.getImageMatrix());
        mMatrix.setScale(1.0f, 1.0f);
        backImage.setImageMatrix(mMatrix);
        Drawable drawable = backImage.getDrawable();
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        backImage.setDrawingCacheEnabled(false);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) backImage.getLayoutParams();
        layoutParams.height = (int) (bitmap.getHeight() * 1.0f);
        layoutParams.width = (int) (bitmap.getWidth() * 1.0f);
//        Log.i("MainActivity", "Layout.height=" + layoutParams.height + ", Layout.width=" + layoutParams.width);
        backImage.setLayoutParams(layoutParams);
        backImage.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            boolean isFirst = true;//默认调用两次，这里只让它执行一次回调
            @Override
            public void onGlobalLayout() {
                if (isFirst) {
                    isFirst = false;
                    pathMeasure.getPosTan(pathMeasure.getLength()*interpolated, pos, null);
                    //现在布局全部完成，可以获取到任何View组件的宽度、高度、左边、右边等信息
                    setCoords(backImage, endPoint.x, endPoint.y);
                    backImage.requestLayout();
                    float y = backImage.getY();
                    backAnimator = ObjectAnimator.ofFloat(backImage, "y", y - 10, y + 10);
                    backAnimator.setDuration(DURATION);
                    backAnimator.setRepeatMode(ValueAnimator.REVERSE);
                    backAnimator.setRepeatCount(ValueAnimator.INFINITE);
                    backAnimator.start();
                }
            }
        });
        //setCoords(backImage, endPoint.x, endPoint.y);
        //setCoords(backImage, endPoint.x, endPoint.y, layoutParams.width/2, layoutParams.height/2);

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        interpolated = positionOffset;
//        Log.i("MainActivity", "interpolated=" + interpolated);
            if (interpolated < 1 && interpolated > 0.0f) {
                if (position < picList.size() - 1) {
                    frontImage.setImageResource(picList.get(position + 1));
                }
                mMatrix = new Matrix(frontImage.getImageMatrix());
                if(interpolated<0.9) {
                    mMatrix.setScale(interpolated * interpolated, interpolated * interpolated);
                }else{
                    mMatrix.setScale(1.0f, 1.0f);
                }
                frontImage.setImageMatrix(mMatrix);
                frontImage.setAlpha(0.3f+interpolated);
                frontImage.setDrawingCacheEnabled(true);
                Drawable drawable = frontImage.getDrawable();
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                frontImage.setDrawingCacheEnabled(false);
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) frontImage.getLayoutParams();
                if(interpolated<0.9){
                    layoutParams.height = (int) (bitmap.getHeight() * interpolated*interpolated);
                    layoutParams.width = (int) (bitmap.getWidth() * interpolated*interpolated);
                }else{
                    layoutParams.height = (int) (bitmap.getHeight() * 1);
                    layoutParams.width = (int) (bitmap.getWidth() * 1);
                }
//                Log.i("MainActivity", "Layout.height=" + layoutParams.height + ", Layout.width=" + layoutParams.width);
                frontImage.setLayoutParams(layoutParams);
                pathMeasure.getPosTan(pathMeasure.getLength() * interpolated, pos, null);
                setCoords(frontImage, pos[0], pos[1]);
                if(backAnimator.isRunning()){
                    backAnimator.cancel();
                }
                FrameLayout.LayoutParams backLayoutParams = (FrameLayout.LayoutParams) backImage.getLayoutParams();
                backLayoutParams.topMargin = (int)(backImage.getHeight()*interpolated*interpolated);
            }else{

            }
    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if(state == 0){
            float y = frontImage.getY();
            ObjectAnimator animator = ObjectAnimator.ofFloat(frontImage, "y", y - 10, y + 10);
            animator.setDuration(DURATION);
            animator.setRepeatMode(ValueAnimator.REVERSE);
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.start();
        }
    }

    public void setCoords(ImageView view, float x, float y) {

        //计算根据中心点坐标计算左上角的坐标点
        float ltx = x - view.getMeasuredWidth() / 2;
        float lty = y - view.getMeasuredHeight() / 2;
        view.setX(ltx);
        view.setY(lty);
    }

    public void setCoords(ImageView view, float x, float y , float dx , float dy){
        view.setX(x - dx);
        view.setY(y - dy);
    }


}
