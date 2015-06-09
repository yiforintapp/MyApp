
package com.leo.appmaster.quickgestures.tools;

import java.util.ArrayList;
import java.util.List;

import com.leo.appmaster.utils.LeoLog;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * this for quick gesture item,match every icon for round color bg
 * 
 * @author zhangwenyang
 */

public class ColorMatcher {

    private List<BaseColorItem> colorBase;

    static int[] cachedTargetLab = new int[3];

    public ColorMatcher() {
        this.colorBase = new ArrayList<BaseColorItem>();
    }

    public void addBitmapSample(Bitmap bmp) {
        this.colorBase.add(new BaseColorItem(bmp));
    }
    
    public void clearItem(){
        colorBase.clear();
        clearCachedTargetLab();
    }

    public Bitmap getMatchedBitmap(Bitmap target) {

        BaseColorItem item = this.getMatchedItem(target);

        return item.getBitmap();

    }

    public Bitmap getMatchedBitmap(Drawable target) {
        Bitmap targetBmp = drawableToBitmap(target);

        BaseColorItem item = this.getMatchedItem(targetBmp);
        if (item != null) {
            return item.getBitmap();
        }
//        targetBmp.recycle();
        return null;

    }

    public Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public void clear() {
        colorBase.clear();
    }

    private BaseColorItem getMatchedItem(Bitmap bmp) {
        int bestScore = -1;
        BaseColorItem bestItem = null;
        clearCachedTargetLab();
        for (int i = 0; i < this.colorBase.size(); i++) {
            BaseColorItem item = this.colorBase.get(i);
            int score = item.getMatchScore(bmp);
            // first time
            if (bestScore == -1) {
                bestScore = score;
                bestItem = item;
            }
            if (score < bestScore) {
                bestScore = score;
                bestItem = item;
            }
        }
        clearCachedTargetLab();
        LeoLog.d("getMatch",
                Integer.toString(this.colorBase.indexOf(bestItem)));
        return bestItem;

    }

    static void clearCachedTargetLab() {
        for (int i = 0; i < cachedTargetLab.length; i++) {
            cachedTargetLab[i] = -1;
        }
    }

}
