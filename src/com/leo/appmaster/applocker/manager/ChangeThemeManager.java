package com.leo.appmaster.applocker.manager;

import com.leo.appmaster.R;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;

public class ChangeThemeManager {
    public static final int VERSION_CODE_NEED_CHANGE_TO_CHRISMAS_THEME = 62;
    public static final int BG_LOCKSCREEN_PASSWORD_NUM = 1;
    public static final int BG_LOCKSCREEN_GESTURE_DOT = 2;
    public static final int BG_HOME_TAB = 3;
    public static final int BG_HOME_UPARROW = 4;
    
    public static Drawable getChrismasThemeDrawbleBySlotId (int slotId, Context context) {
        //获取versionCode
        PackageInfo info = null;
        try {
            info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_CONFIGURATIONS);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        int versionCode = info.versionCode;
        //根据slotId获取对应的圣诞节元素的drawable
        if(versionCode != VERSION_CODE_NEED_CHANGE_TO_CHRISMAS_THEME) {
            return null;
        } else {
            Drawable drawableForReturn = null;
            switch (slotId) {
                case BG_LOCKSCREEN_PASSWORD_NUM:
                    drawableForReturn = context.getResources().getDrawable(R.drawable.gesture_chrismas_dot1);
                    break;
                case BG_LOCKSCREEN_GESTURE_DOT:
                    drawableForReturn = context.getResources().getDrawable(R.drawable.gesture_chrismas_dot1);
                    break;
                case BG_HOME_TAB:
                    drawableForReturn = context.getResources().getDrawable(R.drawable.gesture_chrismas_dot1);
                    break;
                default:
                    break;
            }
            return drawableForReturn;
        }
    }
}
