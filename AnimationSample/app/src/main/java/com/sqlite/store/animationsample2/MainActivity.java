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
import android.view.Display;
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
import com.sqlite.store.animationsample2.view.BackgroundState;
import com.sqlite.store.animationsample2.view.CelestialBodyView;
import com.sqlite.store.animationsample2.view.MyViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements ViewPager.OnPageChangeListener {
    private float interpolated;             //滑动偏移百分比

    private ViewPager mViewPager;
    private List<CPoint> leftPointList;                  //左边出来球的运动路径
    private List<CPoint> rightPointList;                 //右边出来球的运动路径
    private List<CPoint> lastPointList;                  //最后一个球的运动路径

    private boolean left = false;           //是否向左滑动
    private boolean right = false;          //是否向右滑动
    private float startX;
    private int currState = 0;
    private int screenWidth;

    private FrameLayout mfLayoutContainer;
    private ImageButton btnLand;
    private ImageView mIvMiklyWay;          //银河图
    private ImageView mIvPurpleLight;       //紫光图
    private ImageView mIvStarA;             //A点星星图
    private ImageView mIvStarB;             //B点星星图
    private boolean isChange = false;       //viewpager是否已经切换

    private int[] yOffsetArray;    //Y轴偏移的数值数组，分别表示A1,A2,A3,A4的偏移点

    private BackgroundState a1, a2, a3, a4, aMilkyWayInit;
    private List<CelestialBodyView> mCelestialViews;    //天体View集合
    private CelestialBodyView mFloatCelestialBody;      //当前浮动天体
    private CelestialBodyView mFlyCelestialBody;
    private CelestialBodyView mMoveUpCelestialBody;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Display display = getWindowManager().getDefaultDisplay();
        screenWidth = display.getWidth();
        initLeftPath();
        initRightPath();
        initLastPath();
        initView();
        initData();
        final ViewTreeObserver vto = mIvPurpleLight.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            boolean isFirst = true;
            @Override
            public void onGlobalLayout() {
                if (isFirst) {
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
        a1 = new BackgroundState(1.0f, 0.5f, yOffsetArray[1]);
        a2 = new BackgroundState(1.0f, 1.0f, yOffsetArray[2]);
        a3 = new BackgroundState(.0f, 2.0f, yOffsetArray[3]);
        a4 = new BackgroundState(.0f, 0.25f, yOffsetArray[0]);
        aMilkyWayInit = new BackgroundState(1.0f, 1.0f, yOffsetArray[0]);
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
        if (state == 1) {//停止浮动天体的动画
            if(mFloatCelestialBody != null){
                mFloatCelestialBody.stopFloatAnimator();
            }
        } else if (state == 0) {
            if (isChange) {
                isChange = false;
                if (left) {
                    mFloatCelestialBody = mFlyCelestialBody;
                    changeStateLeft();
                } else {
                    mFloatCelestialBody = mMoveUpCelestialBody;
                    changeStateRight();
                }
            }
            mFloatCelestialBody.setViewFloat();
        }
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
            backgroundChangeLeft(interpolated);
            if(position<mCelestialViews.size() - 1) {//未到达最后一页
                //当前位置的天体下移，下一个天体飞近
                mCelestialViews.get(position).moveDown(interpolated);
                mFlyCelestialBody = mCelestialViews.get(position + 1);
                mFlyCelestialBody.flying(interpolated);
            }
        }
    }

    /**
     * 向右滑动
     *
     * @param positionOffset
     * @param position
     */
    protected void moveToRight(float positionOffset, int position) {
        interpolated = 1-positionOffset;
        if (interpolated < 1 && interpolated > .0f) {
            if (position == 3 && interpolated > 0.1) {
                btnLand.setVisibility(View.INVISIBLE);
            }
            backgroundChangeRight(interpolated);
            if(position>=0){
                //前一个位置天体飞远，当前位置天体上移
                mFlyCelestialBody = mCelestialViews.get(position+1);
                mFlyCelestialBody.flying(1-interpolated);
                mMoveUpCelestialBody = mCelestialViews.get(position);
                mMoveUpCelestialBody.moveUp(interpolated);
            }
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
        setViewState(aMilkyWayInit, mIvMiklyWay);
        setViewState(a1, mIvStarA);//A1
        setViewState(a4, mIvStarB);//B1(A4)
    }

    private void setBackgroundYCoords(View view, float y) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
        float lty = y - layoutParams.height / 2;
        float ltx = screenWidth / 2 - layoutParams.width / 2;
        view.setY(lty);
        view.setX(ltx);
    }

    protected void initView() {
        mfLayoutContainer = (FrameLayout) findViewById(R.id.flayout_container);
        btnLand = (ImageButton) findViewById(R.id.btn_land);
        mViewPager = (ViewPager) findViewById(R.id.vp_container);
        mIvMiklyWay = (ImageView) findViewById(R.id.iv_milky_way);
        mIvPurpleLight = (ImageView) findViewById(R.id.iv_purple_light);
        mIvStarA = (ImageView) findViewById(R.id.iv_stars_A);
        mIvStarB = (ImageView) findViewById(R.id.iv_stars_B);
        mCelestialViews = new ArrayList<CelestialBodyView>();
        mCelestialViews.add(new CelestialBodyView(this,leftPointList, R.drawable.pic1));    //左边轨迹飞行
        mCelestialViews.add(new CelestialBodyView(this, leftPointList, R.drawable.pic2));   //左边轨迹飞行
        mCelestialViews.add(new CelestialBodyView(this, rightPointList, R.drawable.pic3));  //右边轨迹飞行
        mCelestialViews.add(new CelestialBodyView(this, leftPointList, R.drawable.pic4));   //左边轨迹飞行
        mCelestialViews.add(new CelestialBodyView(this, lastPointList, R.drawable.pic5));   //最后一张单独的飞行轨迹
        for (int i= 0;i<mCelestialViews.size();i++){
            mfLayoutContainer.addView(mCelestialViews.get(i).getView());
        }
    }

    /**
     * 初始化路径和偏移点
     */
    protected void initLeftPath() {
        CPoint p0 = new CPoint(dp2Px(143), dp2Px(211));
        CPoint p1 = new CPoint(dp2Px(150), dp2Px(239));
        CPoint p2 = new CPoint(dp2Px(131), dp2Px(255));
        CPoint p3 = new CPoint(dp2Px(156), dp2Px(252));
        CPoint p4 = new CPoint(dp2Px(176), dp2Px(293));
        CPoint p5 = new CPoint(dp2Px(196), dp2Px(319));
        CPoint p6 = new CPoint(dp2Px(228), dp2Px(385));
        CPoint p7 = new CPoint(dp2Px(221), dp2Px(438));
        CPoint p8 = new CPoint(screenWidth / 2,1036);
        leftPointList = new ArrayList<CPoint>();
        leftPointList.add(p0);
        leftPointList.add(p1);
        leftPointList.add(p2);
        leftPointList.add(p3);
        leftPointList.add(p4);
        leftPointList.add(p5);
        leftPointList.add(p6);
        leftPointList.add(p7);
        leftPointList.add(p8);
    }

    protected void initRightPath(){
        CPoint rp0 = new CPoint(dp2Px(275),dp2Px(268));//new CPoint(516, 503);
        CPoint rp1 = new CPoint(dp2Px(210), dp2Px(277));//new CPoint(394, 519);
        CPoint rp2 = new CPoint(dp2Px(227), dp2Px(380));//new CPoint(425, 712);
        CPoint rp3 = new CPoint(dp2Px(220), dp2Px(339));//new CPoint(412, 635);
        CPoint rp4 = new CPoint(screenWidth / 2, dp2Px(553));//new CPoint(screenWidth / 2,1036);
        rightPointList = new ArrayList<CPoint>();
        rightPointList.add(rp0);
        rightPointList.add(rp1);
        rightPointList.add(rp2);
        rightPointList.add(rp3);
        rightPointList.add(rp4);
    }

    protected void initLastPath(){
        lastPointList = new ArrayList<CPoint>();
        CPoint lastP0 = new CPoint(dp2Px(162), dp2Px(241));//new CPoint(303, 451);
        CPoint lastP1 = new CPoint(dp2Px(219), dp2Px(265));//new CPoint(410, 496);
        CPoint lastP2 = new CPoint(dp2Px(177), dp2Px(399));//new CPoint(332, 749);
        CPoint lastP3 = new CPoint(dp2Px(334), dp2Px(197));//new CPoint(626, 370);
        CPoint lastP4 = new CPoint(dp2Px(207), dp2Px(380));//new CPoint(388, 712);
        lastPointList.add(lastP0);
        lastPointList.add(lastP1);
        lastPointList.add(lastP2);
        lastPointList.add(lastP3);
        lastPointList.add(lastP4);
    }


    /**
     * 初始化前景图和后景图（前景图指在前面浮动现实的图，后景图指下次滑动时浮现出的图）
     */
    protected void initImage() {
        mFloatCelestialBody = mCelestialViews.get(0);
        interpolated = 1.0f;
        mfLayoutContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            boolean isFirst = true;//默认调用两次，这里只让它执行一次回调
            @Override
            public void onGlobalLayout() {
                if (isFirst) {
                    isFirst = false;
                    //设置第一个天体在停靠点浮动
                    mCelestialViews.get(0).setViewFloat();
                }
            }
        });
    }

    private void scaleView(ImageView view, float scale) {
        Matrix matrix = view.getImageMatrix();
        matrix.setScale(scale, scale);
        view.setImageMatrix(matrix);
        view.setDrawingCacheEnabled(true);
        Drawable drawable = view.getDrawable();
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        view.setDrawingCacheEnabled(false);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
        layoutParams.height = (int) (bitmap.getHeight() * scale);
        layoutParams.width = (int) (bitmap.getWidth() * scale);
        view.setLayoutParams(layoutParams);
    }

    private void setViewAlpha(ImageView view, float alpha) {
        view.setAlpha(alpha);
    }

    /**
     * 左滑动时背景变换
     *
     * @param interpolated A1(B2): alpha 100%, scale 50%
     *                     A2(B3): alpha 100%, scale 100%
     *                     A3(B4): alpha 0%,   scale 200%
     *                     A4(B1): alpha 0%,   scale 25%
     */
    private void backgroundChangeLeft(float interpolated) {
        switch (currState) {//A4, B1
            case 0:
                /**
                 * A1-A2: mIvStartA
                 * B1-B2(A4-A1):mIvStartB
                 * Amilkyway-A1:mIvMilkyWay
                 */
                exchangeState(a1, a3, mIvStarA, interpolated);
                exchangeState(aMilkyWayInit, a1, mIvMiklyWay, interpolated);
                exchangeState(a4, a1, mIvStarB, interpolated);
                break;
            case 1:
                /**
                 * A2-A3:mIvStartA
                 * B2-B3(A1-A2):mIvMilkyWay, mIvStartB
                 */
                exchangeState(a2, a3, mIvStarA, interpolated);
                exchangeState(a1, a2, mIvMiklyWay, interpolated);
                exchangeState(a1, a2, mIvStarB, interpolated);
                break;
            case 2:
                /**
                 * A4-A1:mIvStartA, mIvPurpleLight
                 * B3-B4(A2-A3):mIvStartB, mIvMilkyWay
                 */
                exchangeState(a4, a1, mIvStarA, interpolated);
                exchangeState(a2, a3, mIvStarB, interpolated);
                exchangeState(a2, a3, mIvMiklyWay, interpolated);
                //这里紫光横向上应该有点向左移动，整体是一个向右下方运动的轨迹
                exchangeState(a4, a1, mIvPurpleLight, interpolated);
                break;
            case 3:
                /**
                 * A1-A2:mIvStartA, mIvPurpleLight
                 * B1-B2(A4-A1):mIvStartB
                 */
                exchangeState(a1, a2, mIvStarA, interpolated);
                exchangeState(a4, a1, mIvStarB, interpolated);
                //这里实际上紫光应该放大再向上移动
                exchangeState(a1, a2, mIvPurpleLight, interpolated);
                break;
            case 4:
                /**
                 * A2-A3:mIvStartA, mIvPurpleLight
                 * B2-B3(A1-A2):mIvStartB
                 */
                exchangeState(a2, a3, mIvStarA, interpolated);
                exchangeState(a1, a2, mIvStarB, interpolated);
                //这里紫光实际上应该继续放大，并且向上移动
                exchangeState(a2, a3, mIvPurpleLight, interpolated);
                break;
        }
    }

    /**
     * 左滑结束时背景状态切换
     */
    private void changeStateLeft() {
        switch (currState) {
            case 0:
                setViewState(a2, mIvStarA);
                setViewState(a1, mIvStarB);
                setViewState(a1, mIvMiklyWay);
                currState = 1;
                break;
            case 1:
                currState = 2;
                mIvPurpleLight.setVisibility(View.VISIBLE);
                setViewState(a2, mIvMiklyWay);
                setViewState(a2, mIvStarB);
                //change state from a3 to a4
                setViewState(a4, mIvStarA);
                setViewState(a4, mIvPurpleLight);
                break;
            case 2:
                setViewState(a1, mIvStarA);
                setViewState(a1, mIvPurpleLight);
                //change state from a3 to a4
                setViewState(a4, mIvStarB);
                currState = 3;
                break;
            case 3:
                setViewState(a2, mIvStarA);
                setViewState(a2, mIvPurpleLight);
                setViewState(a1, mIvStarB);
                currState = 4;
                break;
            case 4:
                setViewState(a3, mIvStarA);
                setViewState(a3, mIvPurpleLight);
                setViewState(a2, mIvStarB);
                currState = 5;
                break;
        }
    }

    /**
     * 右滑结束时背景状态切换
     */
    private void changeStateRight() {
        switch (currState) {
            case 5:
                setViewState(a2, mIvStarA);
                setViewState(a2, mIvPurpleLight);
                setViewState(a1, mIvStarB);
                currState = 4;
                break;
            case 4:
                setViewState(a1, mIvStarA);
                setViewState(a1, mIvPurpleLight);
                //change state from a4 to a3
                setViewState(a3, mIvStarB);
                currState = 3;
                break;
            case 3:
                setViewState(a2, mIvMiklyWay);
                setViewState(a2, mIvStarB);
                //change state from a4 to a3
                setViewState(a3, mIvStarA);
                setViewState(a3, mIvPurpleLight);
                mIvPurpleLight.setVisibility(View.INVISIBLE);
                currState = 2;
                break;
            case 2:
                setViewState(a2, mIvStarA);
                setViewState(a1, mIvStarB);
                setViewState(a1, mIvMiklyWay);
                currState = 1;
                break;
            case 1:
                setViewState(a1, mIvStarA);
                setViewState(a4, mIvStarB);
                setViewState(aMilkyWayInit, mIvMiklyWay);
                currState = 0;
                break;

        }
    }

    /**
     * 右滑动时背景变换
     *
     * @param interpolated A1(B2): alpha 100%, scale 50%
     *                     A2(B3): alpha 100%, scale 100%
     *                     A3(B4): alpha 0%,   scale 200%
     *                     A4(B1): alpha 0%,   scale 25%
     */
    private void backgroundChangeRight(float interpolated) {
        switch (currState) {
            case 5:
                /**
                 * A3-A2:mIvStartA, mIvPurpleLight
                 * B3-B2(A2-A1):mIvStartB
                 */
                exchangeState(a3, a2, mIvStarA, interpolated);
                exchangeState(a2, a1, mIvStarB, interpolated);
                //这里紫光实际上应该缩小，并且向下移动
                exchangeState(a3, a2, mIvPurpleLight, interpolated);
                break;
            case 4:
                /**
                 * A2-A1:mIvStartA, mIvPurpleLight
                 * B2-B1(A1-A4):mIvStartB
                 */
                exchangeState(a2, a1, mIvStarA, interpolated);
                exchangeState(a1, a4, mIvStarB, interpolated);
                exchangeState(a2, a1, mIvPurpleLight, interpolated);
                break;
            case 3:
                /**
                 * A1-A4:mIvStartA, mIvPurpleLight
                 * B4-B3(A3-A2):mIvStartB, mIvMilkyWay
                 */
                exchangeState(a1, a4, mIvStarA, interpolated);
                exchangeState(a1, a4, mIvPurpleLight, interpolated);
                exchangeState(a3, a2, mIvStarB, interpolated);
                exchangeState(a3, a2, mIvMiklyWay, interpolated);
                break;
            case 2:
            /*
             * A3-A2:mIvStartA
             * B3-B2(A2-A1):mIvStartB, mIvMilkyWay
             */
                exchangeState(a3, a2, mIvStarA, interpolated);
                exchangeState(a2, a1, mIvStarB, interpolated);
                exchangeState(a2, a1, mIvMiklyWay, interpolated);
                break;
            case 1:
            /*
             * A2-A1:mIvStartA
             * B2-B1(A1-A4):mIvStartB
             * A1-Amilkyway:mIvMilkyWay
             */
                exchangeState(a2, a1, mIvStarA, interpolated);
                exchangeState(a1, a4, mIvStarB, interpolated);
                exchangeState(a1, aMilkyWayInit, mIvMiklyWay, interpolated);
                break;
        }
    }

    private void exchangeState(BackgroundState startState, BackgroundState endState, ImageView target, float interpolated) {
        setViewAlpha(target, startState.getAlpha() + interpolated * (endState.getAlpha() - startState.getAlpha()));
        scaleView(target, startState.getScale() + interpolated * (endState.getScale() - startState.getScale()));
        setBackgroundYCoords(target, startState.getyCoords() + interpolated * (endState.getyCoords() - startState.getyCoords()));
    }

    private void setViewState(BackgroundState state, ImageView target) {
        setViewAlpha(target, state.getAlpha());
        scaleView(target, state.getScale());
        setBackgroundYCoords(target, state.getyCoords());
    }
}


