package com.sqlite.store.animationsample2;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.sqlite.store.animationsample2.view.CelestialBodyView;

import java.util.List;

/**
 * Created by LiuJiangHao on 15/8/17.
 */
public class CelestialBodyAdapter {
    private List<CelestialBodyView> viewItemList;
    private Activity context;

    public CelestialBodyAdapter(List<CelestialBodyView> viewItemList, Activity context) {
        this.viewItemList = viewItemList;
        this.context = context;
    }

    public int getCount(){
        return viewItemList == null?0:viewItemList.size();
    }

    /**
     * 正在浮动的View
     * @return
     */
    public CelestialBodyView getFloatView(FrameLayout parent, int position){
        CelestialBodyView itemView = getItem(position);
        if(itemView != null){
            itemView.initImageView(context);
            parent.addView(itemView.getView());
        }
        return itemView;
    }

    /**
     * 获取即将飞出的View
     * @return
     */
    public CelestialBodyView getFlyingView(FrameLayout parent, int position){
        CelestialBodyView itemView = getItem(position + 1);
        if (itemView != null) {
            itemView.initImageView(context);
            itemView.flyInitView();
            parent.addView(itemView.getView());
        }
        return itemView;
    }

    /**
     * 获取底部的View
     * @param position
     * @return
     */
    public CelestialBodyView getBottomView(FrameLayout parent, int position){
        CelestialBodyView itemView = getItem(position);
        if(itemView != null){
            itemView.initImageView(context);
            itemView.bottomInitView();
            parent.addView(itemView.getView());
            itemView.bottomSetSize();

        }
        return itemView;
    }

    private CelestialBodyView getItem(int position){
        return viewItemList == null
                || position<0
                || position>=viewItemList.size()?null:viewItemList.get(position);
    }

    /**
     * 释放不显示并且不即将显示的view
     * @param parent
     * @param position
     * @param view
     */
    private void destoryItem(ViewGroup parent, int position, ImageView view){
        try{
            parent.removeView(view);
            view.destroyDrawingCache();
            view = null;
        }catch (Exception e){
            e.printStackTrace();
        }
    }


}
