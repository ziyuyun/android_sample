package com.sqlite.store.animationsample2;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.sqlite.store.animationsample2.pathutil.PathUtil;
import com.sqlite.store.animationsample2.pathutil.PathUtil.CPoint;
import com.sqlite.store.animationsample2.view.MyViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements ViewPager.OnPageChangeListener {
    public static final long DURATION = 1000;
    private float screenWidth;
    private float screenHeight;
    private float interpolated;             //滑动偏移百分比
    private PathMeasure pathMeasure;
    float[] mCurrPos = new float[2];        //当前位置点
    private Matrix mMatrix;
    private ViewPager mViewPager;           
    private CPoint endPoint;                //结束位置点坐标
    private Path pathLeft;                  //左边出来球的运动路径
    private Path pathRight;                 //右边出来球的运动路径
    private Path pathLast;                  //最后一个球的运动路径

    private boolean left = false;           //是否向左滑动
    private boolean right = false;          //是否向右滑动
    private float startX;
    private int currState = 0;

    private ImageView mIvBack;              //后景图
    private ImageView mIvFront;             //前景图
    private ImageButton btnLand;
    private ImageView mIvMiklyWay;          //银河图
    private ImageView mIvPurpleLight;       //紫光图
    private ImageView mIvStarA;             //A点星星图
    private ImageView mIvStarB;             //B点星星图
    private ObjectAnimator mFrontAnimator;  //前景图动画
    private int mFrontHeight;               //前景图高度
    private int mBackHeight;                //后景图高度
    private boolean isChange = false;       //viewpager是否已经切换

    private int[] yOffsetArray;    //Y轴偏移的数值数组，分别表示A1,A2,A3,A4的偏移点
    private int mDy;                //Y轴各状态点之间的间隔;

    List<Integer> picList = new ArrayList<Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        WindowManager wm = getWindowManager();
        screenWidth = wm.getDefaultDisplay().getWidth();
        screenHeight = wm.getDefaultDisplay().getHeight();
        setContentView(R.layout.activity_main);
        initView();
        initPath();
        initData();
        final ViewTreeObserver vto = mIvPurpleLight.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            boolean isFirst = true;
            @Override
            public void onGlobalLayout() {
                if(isFirst) {
                    isFirst = false;
                    initBackground();
                }
            }
        });

        initImage();
        initListener();
    }

    private void initListener() {
        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float endX = event.getX();
                        if (endX > startX) {
                            right = true;
                            left = false;
                        } else if (endX < startX) {
                            right = false;
                            left = true;
                        } else {
                            left = right = false;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }
                return false;
            }
        });
        mViewPager.addOnPageChangeListener(this);
    }

    private void initData() {
        yOffsetArray = new int[]{dp2Px(305), dp2Px(351), dp2Px(397), dp2Px(444)};
        mDy = dp2Px(44);
        picList.add(R.drawable.pic1);
        picList.add(R.drawable.pic2);
        picList.add(R.drawable.pic3);
        picList.add(R.drawable.pic4);
        picList.add(R.drawable.pic5);
        List<Integer> viewIDs = new ArrayList<Integer>();
        viewIDs.add(R.drawable.text1);
        viewIDs.add(R.drawable.text2);
        viewIDs.add(R.drawable.text3);
        viewIDs.add(R.drawable.text4);
        viewIDs.add(R.drawable.text5);
        MyViewPagerAdapter adapter = new MyViewPagerAdapter(viewIDs, this);
        mViewPager.setAdapter(adapter);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (right) {
            moveToRight(positionOffset, position);
        } else if (left) {
            moveToLeft(positionOffset, position);
        }
    }

    @Override
    public void onPageSelected(int position) {
        isChange = true;
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == 1) {
            if (mFrontAnimator.isRunning()) {
                mFrontAnimator.cancel();
            }
        } else if (state == 0 && isChange) {
            ImageView tempImage;
            tempImage = mIvFront;
            mIvFront = mIvBack;
            mIvBack = tempImage;
            setCoords(mIvFront, endPoint.x, endPoint.y);
            float y = mIvFront.getY();
            mFrontAnimator = ObjectAnimator.ofFloat(mIvFront, "y", y - 10, y + 10);
            mFrontAnimator.setDuration(DURATION);
            mFrontAnimator.setRepeatMode(ValueAnimator.REVERSE);
            mFrontAnimator.setRepeatCount(ValueAnimator.INFINITE);
            mFrontAnimator.start();
            isChange = false;
            mBackHeight = mIvBack.getHeight();
            mFrontHeight = mIvFront.getHeight();
            changeState();
        }
    }

    public void setCoords(ImageView view, float x, float y) {
        //计算根据中心点坐标计算左上角的坐标点
        float ltx = x - view.getMeasuredWidth() / 2;
        float lty = y - view.getMeasuredHeight() / 2;
        view.setX(ltx);
        view.setY(lty);
    }

    /**
     * 左滑动
     */
    private void moveToLeft(float positionOffset, int position) {
        interpolated = positionOffset;
        if (interpolated < 1 && interpolated > 0.0f) {
            if (position == 3 && interpolated > 0.9) {
                btnLand.setVisibility(View.VISIBLE);
            }
            if (position < picList.size() - 1) {
                mIvBack.setImageResource(picList.get(position + 1));
            }
            backgroundChange(interpolated);
            mMatrix = new Matrix(mIvBack.getImageMatrix());
            mMatrix.setScale(interpolated * interpolated, interpolated * interpolated);
            mIvBack.setImageMatrix(mMatrix);
            mIvBack.setAlpha(0.7f + interpolated);
            mIvBack.setDrawingCacheEnabled(true);
            Drawable drawable = mIvBack.getDrawable();
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            mIvBack.setDrawingCacheEnabled(false);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mIvBack.getLayoutParams();
            layoutParams.height = (int) (bitmap.getHeight() * interpolated * interpolated);
            layoutParams.width = (int) (bitmap.getWidth() * interpolated * interpolated);
            mIvBack.setLayoutParams(layoutParams);
            pathMeasure.getPosTan(pathMeasure.getLength() * interpolated, mCurrPos, null);
            setCoords(mIvBack, mCurrPos[0], mCurrPos[1]);
            setCoords(mIvFront, endPoint.x, endPoint.y + (screenHeight - endPoint.y + mFrontHeight / 2) * 1.5f * interpolated * interpolated);
        }
    }

    /**
     * 向右滑动
     * @param positionOffset
     * @param position
     */
    protected void moveToRight(float positionOffset, int position) {
        interpolated = 1 - positionOffset;
        if (interpolated < 1 && interpolated > .0f) {
            if (position >= 0) {
                mIvBack.setImageResource(picList.get(position));

            }
            if (position == 3 && interpolated < 0.9) {
                btnLand.setVisibility(View.INVISIBLE);
            }
            mMatrix = new Matrix(mIvBack.getImageMatrix());
            mMatrix.setScale(1.0f, 1.0f);
            mIvBack.setImageMatrix(mMatrix);
            mIvBack.setDrawingCacheEnabled(true);
            Drawable drawable = mIvBack.getDrawable();
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            mIvBack.setDrawingCacheEnabled(false);
            mIvBack.setAlpha(1.0f);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mIvBack.getLayoutParams();
            layoutParams.height = bitmap.getHeight();
            layoutParams.width = bitmap.getWidth();
            mIvBack.setLayoutParams(layoutParams);
            mMatrix = new Matrix(mIvFront.getImageMatrix());
            mMatrix.setScale((1 - interpolated) * (1 - interpolated), (1 - interpolated) * (1 - interpolated));
            mIvFront.setImageMatrix(mMatrix);
            mIvFront.setAlpha(0.7f + (1 - interpolated));
            mIvFront.setDrawingCacheEnabled(true);
            drawable = mIvFront.getDrawable();
            bitmap = ((BitmapDrawable) drawable).getBitmap();
            mIvFront.setDrawingCacheEnabled(false);
            layoutParams = (FrameLayout.LayoutParams) mIvFront.getLayoutParams();
            layoutParams.height = (int) (bitmap.getHeight() * (1 - interpolated) * (1 - interpolated));
            layoutParams.width = (int) (bitmap.getWidth() * (1 - interpolated) * (1 - interpolated));
            mIvFront.setLayoutParams(layoutParams);
            pathMeasure.getPosTan(pathMeasure.getLength() * (1 - interpolated), mCurrPos, null);
            setCoords(mIvFront, mCurrPos[0], mCurrPos[1]);
            setCoords(mIvBack, endPoint.x, endPoint.y + (screenHeight - endPoint.y + mBackHeight / 2) * 1.5f * (1 - interpolated) * (1 - interpolated));
        }
    }

    protected int dp2Px(float dp) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + .5f);
    }

    protected int px2Dp(float px) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (px / scale + .5f);
    }


    protected void initBackground() {
        //将紫光图隐藏
        mIvPurpleLight.setVisibility(View.GONE);
        //计算银河的位置点
        setViewAlpha(mIvMiklyWay, .0f);
        scaleView(mIvMiklyWay, 0.25f);
        setBackgroundYCoords(mIvMiklyWay, yOffsetArray[0]);
        setViewAlpha(mIvStarA, 0.5f);
        scaleView(mIvStarA, 0.5f);
        setBackgroundYCoords(mIvStarA, yOffsetArray[1]);
        scaleView(mIvStarB, 0.25f);
        setViewAlpha(mIvStarB, .0f);
        setBackgroundYCoords(mIvStarB, yOffsetArray[0]);
    }

    private void setBackgroundYCoords(View view, float y){
        float lty = y - view.getMeasuredHeight()/2;
        view.setY(lty);
    }

    protected void initView() {
        mIvBack = (ImageView) findViewById(R.id.iv_front);
        btnLand = (ImageButton) findViewById(R.id.btn_land);
        mIvFront = (ImageView) findViewById(R.id.iv_back);
        mViewPager = (ViewPager) findViewById(R.id.vp_container);
        mIvMiklyWay = (ImageView) findViewById(R.id.iv_milky_way);
        mIvPurpleLight = (ImageView) findViewById(R.id.iv_purple_light);
        mIvStarA = (ImageView) findViewById(R.id.iv_stars_A);
        mIvStarB = (ImageView) findViewById(R.id.iv_stars_B);
    }

    /**
     * 初始化路径和偏移点
     */
    protected void initPath(){
        CPoint p0 = new CPoint(dp2Px(143), dp2Px(211));
        CPoint p1 = new CPoint(dp2Px(150), dp2Px(239));
        CPoint p2 = new CPoint(dp2Px(131), dp2Px(255));
        CPoint p3 = new CPoint(dp2Px(156), dp2Px(252));
        CPoint p4 = new CPoint(dp2Px(176), dp2Px(293));
        CPoint p6 = new CPoint(dp2Px(196), dp2Px(319));
        CPoint p7 = new CPoint(dp2Px(228), dp2Px(385));
        CPoint p8 = new CPoint(dp2Px(207), dp2Px(541));
        CPoint p9 = new CPoint(dp2Px(221), dp2Px(438));
        CPoint p10 = new CPoint(screenWidth / 2, dp2Px(520));
        endPoint = p10;
        Path path = new Path();
        List<CPoint> points = new ArrayList<CPoint>();
        points.add(p0);
        points.add(p1);
        points.add(p2);
        points.add(p3);
        points.add(p4);
        points.add(p6);
        points.add(p7);
        //points.add(p8);
        points.add(p9);
        points.add(p10);
        PathUtil.drawPath(path, points, 0.2f);
        pathMeasure = new PathMeasure(path, false);
    }

    /**
     * 初始化前景图和后景图（前景图指在前面浮动现实的图，后景图指下次滑动时浮现出的图）
     */
    protected void initImage() {
        mIvBack.setScaleType(ImageView.ScaleType.MATRIX);
        mIvFront.setScaleType(ImageView.ScaleType.MATRIX);
        mIvFront.setImageResource(picList.get(0));
        interpolated = 1.0f;
        Drawable drawable = mIvFront.getDrawable();
        final Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        mIvFront.setDrawingCacheEnabled(false);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mIvFront.getLayoutParams();
        layoutParams.height = bitmap.getHeight();
        layoutParams.width = bitmap.getWidth();
        mIvFront.setLayoutParams(layoutParams);
        mIvFront.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            boolean isFirst = true;//默认调用两次，这里只让它执行一次回调

            @Override
            public void onGlobalLayout() {
                if (isFirst) {
                    isFirst = false;
                    pathMeasure.getPosTan(pathMeasure.getLength() * interpolated, mCurrPos, null);
                    //现在布局全部完成，可以获取到任何View组件的宽度、高度、左边、右边等信息
                    setCoords(mIvFront, endPoint.x, endPoint.y);
                    float y = mIvFront.getY();
                    mFrontAnimator = ObjectAnimator.ofFloat(mIvFront, "y", y - 10, y + 10);
                    mFrontAnimator.setDuration(DURATION);
                    mFrontAnimator.setRepeatMode(ValueAnimator.REVERSE);
                    mFrontAnimator.setRepeatCount(ValueAnimator.INFINITE);
                    mFrontAnimator.start();
                    mFrontHeight = mIvFront.getHeight();
                }
            }
        });
    }

    private void scaleView(ImageView view, float scale){
        Matrix matrix = view.getImageMatrix();
        matrix.setScale(scale, scale);
        view.setImageMatrix(matrix);
        view.setDrawingCacheEnabled(true);
        Drawable drawable = view.getDrawable();
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        view.setDrawingCacheEnabled(false);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
        layoutParams.height = bitmap.getHeight();
        layoutParams.width = bitmap.getWidth();
        view.setLayoutParams(layoutParams);
    }

    private void setViewAlpha(ImageView view, float alpha){
        view.setAlpha(alpha);
    }

    private void backgroundChange(float interpolated){
        switch (currState){//A4, B1
            case 0://A4,B1
                setViewAlpha(mIvMiklyWay, interpolated);
                scaleView(mIvMiklyWay, 0.25f*(1+interpolated));
                setBackgroundYCoords(mIvMiklyWay, yOffsetArray[0]+mDy*interpolated);
                scaleView(mIvStarA, 0.5f*(1+interpolated));
                setBackgroundYCoords(mIvStarA, yOffsetArray[1]+mDy*interpolated);
                scaleView(mIvStarB, 0.25f*(1+interpolated));
                setViewAlpha(mIvStarB, interpolated);
                setBackgroundYCoords(mIvStarB, yOffsetArray[0]+mDy*interpolated);
                break;
            case 1://A1,B2
                scaleView(mIvMiklyWay, 0.5f * (1 + interpolated));
                setBackgroundYCoords(mIvMiklyWay, yOffsetArray[1] + mDy * interpolated);
                scaleView(mIvStarA, 1.0f * (1 + interpolated));
                setBackgroundYCoords(mIvStarA, yOffsetArray[2] + mDy * interpolated);
                setViewAlpha(mIvStarA, 1.0f * (1 - interpolated));
                scaleView(mIvStarB, 0.5f * (1 + interpolated));
                setBackgroundYCoords(mIvStarB, yOffsetArray[1] + mDy * interpolated);
                break;
            case 2://A2,B3
                scaleView(mIvStarB, 1.0f * (1 + interpolated));
                setBackgroundYCoords(mIvStarB, yOffsetArray[2] + mDy * interpolated);
                setViewAlpha(mIvStarB, 1.0f * (1 - interpolated));
                scaleView(mIvMiklyWay, 1.0f * (1 + interpolated));
                setBackgroundYCoords(mIvMiklyWay, yOffsetArray[2] + mDy * interpolated);
                setViewAlpha(mIvMiklyWay, 1.0f * (1 - interpolated));
                scaleView(mIvStarA, 0.25f*(1+interpolated));
                setViewAlpha(mIvStarA, interpolated);
                setBackgroundYCoords(mIvStarA, yOffsetArray[0] + mDy * interpolated);
                break;
            case 3://A3,B4||A4,B1
                scaleView(mIvStarB, 0.25f * (1 + interpolated));
                setViewAlpha(mIvStarB, interpolated);
                setBackgroundYCoords(mIvStarB, yOffsetArray[0] + mDy * interpolated);
                scaleView(mIvStarA, 0.5f * (1 + interpolated));
                setBackgroundYCoords(mIvStarA, yOffsetArray[1]+mDy*interpolated);
                scaleView(mIvPurpleLight, 0.25f * (1 + interpolated));
                setViewAlpha(mIvPurpleLight, interpolated);
                setBackgroundYCoords(mIvPurpleLight, yOffsetArray[0] + mDy * interpolated);
                break;
            case 4://A1,B2
                scaleView(mIvStarA, 1.0f * (1 + interpolated));
                setBackgroundYCoords(mIvStarA, yOffsetArray[2] + mDy * interpolated);
                setViewAlpha(mIvStarA, 1.0f * (1 - interpolated));
                scaleView(mIvPurpleLight, 0.5f * (1 + interpolated));
                setBackgroundYCoords(mIvPurpleLight, yOffsetArray[1] + mDy * interpolated);
                scaleView(mIvStarB, 0.5f * (1 + interpolated));
                setBackgroundYCoords(mIvStarB, yOffsetArray[1] + mDy * interpolated);
                break;
        }
    }

    private void changeState(){
        switch (currState){
            case 0:
                currState = 1;
                break;
            case 1:
                currState = 2;
                scaleView(mIvStarA, 0.25f);
                setViewAlpha(mIvStarA, .0f);
                setBackgroundYCoords(mIvStarA, yOffsetArray[0]);
                break;
            case 2:
                currState = 3;
                scaleView(mIvStarB, 0.25f);
                setViewAlpha(mIvStarB, .0f);
                setBackgroundYCoords(mIvStarB, yOffsetArray[0]);
                mIvPurpleLight.setVisibility(View.VISIBLE);
                scaleView(mIvPurpleLight, 0.25f);
                setViewAlpha(mIvPurpleLight, .0f);
                setBackgroundYCoords(mIvPurpleLight, yOffsetArray[0]);
                break;
            case 3:
                currState = 4;
                break;
        }
    }
}
