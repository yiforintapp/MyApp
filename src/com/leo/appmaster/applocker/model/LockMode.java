
package com.leo.appmaster.applocker.model;

import java.util.List;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public class LockMode {
    /**
     * 访客模式
     */
    public static final int MODE_VISITOR = 1;
    /**
     * 
     */
    public static final int MODE_OFFICE = 2;
    /**
     * 家庭模式
     */
    public static final int MODE_FAMILY = 3;
    /**
     * 其它模式
     */
    public static final int MODE_OTHER = -1;
    
    public int modeId;
    public String modeName;
    // public Bitmap modeIcon;
//    public int modeIconId;
    public List<String> lockList;
    /**
     * 0: unlock all; 1: visitor mode; 2: office mode; 3: family mode; -1: other
     */
    public int defaultFlag;
    public boolean isCurrentUsed;
    public boolean haveEverOpened;
    public boolean selected;

    public LockMode() {
    }
    
    public Drawable getModeDrawable() {
        int drawableId = 0;
        switch (defaultFlag) {
            case MODE_VISITOR:
                drawableId = R.drawable.lock_mode_visitor;
                break;
            case MODE_FAMILY:
                drawableId = R.drawable.lock_mode_family;
                break;
            case MODE_OTHER:
            default:
                drawableId = R.drawable.lock_mode_default;
                break;
        }
        
        Context ctx = AppMasterApplication.getInstance();
        return ctx.getResources().getDrawable(drawableId);
    }
}
