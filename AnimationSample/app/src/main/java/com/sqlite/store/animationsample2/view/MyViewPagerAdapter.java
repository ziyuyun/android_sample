package com.sqlite.store.animationsample2.view;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by LiuJiangHao on 15/8/4.
 */
public class MyViewPagerAdapter extends PagerAdapter {
    private Activity mContext;
    public MyViewPagerAdapter(List<Integer> viewIDList, Activity mContext) {
        this.viewIDList = viewIDList;
        this.mContext = mContext;
    }
    private List<Integer> viewIDList;
    @Override
    public int getCount() {
        return viewIDList == null?0:viewIDList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public int getItemPosition(Object object) {
        return super.getItemPosition(object);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        MyPageItem item = new MyPageItem();
        container.removeView(((View) object));
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = new MyPageItem().getPageItemView(mContext, viewIDList.get(position));
        container.addView(view);
        return view;
    }
}
