package com.sqlite.store.animationsample2.view;

/**
 * Created by LiuJiangHao on 15/8/14.
 */
public class BackgroundState{
    private float scale;
    private float alpha;
    private float yCoords;

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public float getyCoords() {
        return yCoords;
    }

    public void setyCoords(float yCoords) {
        this.yCoords = yCoords;
    }

    public BackgroundState(float scale, float alpha, float yCoords) {
        this.scale = scale;
        this.alpha = alpha;
        this.yCoords = yCoords;
    }
}
