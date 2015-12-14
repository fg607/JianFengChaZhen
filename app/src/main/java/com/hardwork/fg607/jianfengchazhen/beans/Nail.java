package com.hardwork.fg607.jianfengchazhen.beans;

import android.graphics.Bitmap;

/**
 * Created by fg607 on 15-12-14.
 */
public class Nail {

    Bitmap nailBitmap;
    Integer rotate;

    public Nail(Bitmap nailBitmap, Integer rotate) {
        this.nailBitmap = nailBitmap;
        this.rotate = rotate;
    }

    public Bitmap getNailBitmap() {
        return nailBitmap;
    }

    public void setNailBitmap(Bitmap nailBitmap) {
        this.nailBitmap = nailBitmap;
    }

    public Integer getRotate() {
        return rotate;
    }

    public void setRotate(Integer rotate) {
        this.rotate = rotate;
    }
}
