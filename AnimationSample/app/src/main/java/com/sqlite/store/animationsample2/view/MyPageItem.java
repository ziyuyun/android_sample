package com.sqlite.store.animationsample2.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.sqlite.store.animationsample2.R;

import java.lang.ref.WeakReference;

/**
 * Created by LiuJiangHao on 15/8/4.
 */
public class MyPageItem{
    private Activity mContext;
    private int screenWidth;
    private int screenHeight;
    public View getPageItemView(Activity context, int drawableId){
        this.mContext = context;
        WindowManager wm = mContext.getWindowManager();
        screenHeight = wm.getDefaultDisplay().getHeight();
        screenWidth = wm.getDefaultDisplay().getWidth();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.vp_item_guide, null, false);
        ImageView imageView = (ImageView) view.findViewById(R.id.iv_text);
        loadImage(imageView, drawableId);
        return view;
    }

    public void loadImage(ImageView imageView, int drawableId) {
        //imageview size;
        int viewWidth = imageView.getWidth();
        int viewHeight = imageView.getHeight();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(mContext.getResources(), drawableId, options);

        int scale = 1;
        if (viewHeight != 0 && viewWidth != 0) {
            scale = Math.max(options.outWidth / viewWidth, options.outHeight / viewHeight);
        }else{
            scale = Math.max(options.outWidth/ screenWidth, options.outHeight/ screenHeight);
        }
        scale = scale>1?scale:1;
        options.inJustDecodeBounds = false;
        options.inScaled =true;
        options.inSampleSize = scale;
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), drawableId, options);
        WeakReference<Bitmap> bitmapWeakReference = new WeakReference<Bitmap>(bitmap);
        bitmap = null;
        imageView.setImageBitmap(bitmapWeakReference.get());
    }



}
