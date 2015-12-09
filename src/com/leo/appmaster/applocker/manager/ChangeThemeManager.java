package com.leo.appmaster.applocker.manager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import com.leo.appmaster.R;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;

public class ChangeThemeManager {
    private static final String TAG = "ChangeThemeManager";
    public static final int VERSION_CODE_NEED_CHANGE_TO_CHRISMAS_THEME = 63;
    //换装的slotId
    public static final int BG_LOCKSCREEN_PASSWORD_NUM = 1;
    public static final int BG_LOCKSCREEN_GESTURE_DOT = 2;
    public static final int ICON_HOME_UP_ARROW = 7;
    public static final int BG_LOCKSCREEN_WHOLE = 6;
    public static final int BG_HOME_TAB1 = 31;
    public static final int BG_HOME_TAB2 = 32;
    public static final int BG_HOME_TAB3 = 33;
    public static final int BG_HOME_TAB4 = 34;
    public static final int BG_HOME_UPARROW = 4;
    public static final int BG_HOME_ASIDE_FRAGMENT = 5;
    public static final int BG_HOME_MORE_FRAGMENT_LABEL = 8;
    //
    public static final int COUNT_LOCKSCREEN_DOT_THEME_BG = 10;
    public static final String DATE_FORMAT = "yyyy-MM-dd-HH:mm:ss";
    public static final String DATE_LOCK_BEFORE = "2015-12-24-00:00:00";
    public static final String DATE_LOCK_AFTER = "2015-12-26-00:00:00";
    public static final String DATE_HOME_BEFORE = "2015-12-14-00:00:00";
    public static final String DATE_HOME_AFTER = "2015-12-27-00:00:00";
    private static PreferenceTable mPt = PreferenceTable.getInstance();
    private static final int[] LOCKSCREEN_DOT_DRAWABLE_ID = {
        R.drawable.gesture_chrismas_dot1, R.drawable.gesture_chrismas_dot2, R.drawable.gesture_chrismas_dot3,
        R.drawable.gesture_chrismas_dot4, R.drawable.gesture_chrismas_dot5, R.drawable.gesture_chrismas_dot6,
        R.drawable.gesture_chrismas_dot7, R.drawable.gesture_chrismas_dot8, R.drawable.gesture_chrismas_dot9,
        R.drawable.gesture_chrismas_dot10};
    
    public static Drawable getChrismasThemeDrawbleBySlotId (int slotId, Context context) {
        //获取versionCode
        if (!mPt.getBoolean(PrefConst.KEY_HOME_NEED_CHANGE_TO_CHRISMAS_THEME, true) && !mPt.getBoolean(PrefConst.KEY_LOCK_NEED_CHANGE_TO_CHRISMAS_THEME, true)) {
            return null;
        }
        PackageInfo info = null;
        try {
            info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_CONFIGURATIONS);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        int versionCode = info.versionCode;
        if(versionCode != VERSION_CODE_NEED_CHANGE_TO_CHRISMAS_THEME ) {
            return null;
        } else {
            //对比时间
            Date now = new Date();
            SimpleDateFormat sdf=new SimpleDateFormat(DATE_FORMAT);
            Date homeChrismasThemeBefore = null;
            Date homeChrismasThemeAfter = null;
            Date lockScreenChrismasThemeBefore = null;
            Date lockScreenChrismasThemeAfter = null;
            try {
                homeChrismasThemeBefore=sdf.parse(DATE_HOME_BEFORE);
                homeChrismasThemeAfter=sdf.parse(DATE_HOME_AFTER);
                lockScreenChrismasThemeBefore=sdf.parse(DATE_LOCK_BEFORE);
                lockScreenChrismasThemeAfter=sdf.parse(DATE_LOCK_AFTER);
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
            LeoLog.i(TAG, "homeChrismasThemeBefore = "+homeChrismasThemeBefore);
            LeoLog.i(TAG, "homeChrismasThemeAfter = "+homeChrismasThemeAfter);
            LeoLog.i(TAG, "lockScreenChrismasThemeBefore = "+lockScreenChrismasThemeBefore);
            LeoLog.i(TAG, "lockScreenChrismasThemeAfter = "+lockScreenChrismasThemeAfter);
            LeoLog.i(TAG, "now = "+now);
            switch (slotId) {
                case BG_LOCKSCREEN_PASSWORD_NUM:
                case BG_LOCKSCREEN_GESTURE_DOT:
                case BG_LOCKSCREEN_WHOLE:
                    if (now.after(lockScreenChrismasThemeAfter)) {
                        mPt.putBoolean(PrefConst.KEY_LOCK_NEED_CHANGE_TO_CHRISMAS_THEME, false);
                        return null;
                    } else if (now.before(lockScreenChrismasThemeBefore)) {
                        return null;
                    }
                    break;
                case ICON_HOME_UP_ARROW:
                case BG_HOME_TAB1:
                case BG_HOME_TAB2:
                case BG_HOME_TAB3:
                case BG_HOME_TAB4:
                case BG_HOME_UPARROW:
                case BG_HOME_ASIDE_FRAGMENT:
                case BG_HOME_MORE_FRAGMENT_LABEL:
                    if (now.after(homeChrismasThemeAfter)) {
                        mPt.putBoolean(PrefConst.KEY_HOME_NEED_CHANGE_TO_CHRISMAS_THEME, false);
                        return null;
                    } else if (now.before(homeChrismasThemeBefore)) {
                        return null;
                    }
                    break;
                default:
                    break;
            }
            //所有条件满足，选择并返回drawable
            Drawable drawableForReturn = null;
            switch (slotId) {
                case BG_LOCKSCREEN_PASSWORD_NUM:
                case BG_LOCKSCREEN_GESTURE_DOT:
                    drawableForReturn = randomADrawable(context);
                    break;
                case BG_HOME_TAB1:
                    drawableForReturn = context.getResources().getDrawable(R.drawable.gesture_chrismas_dot1);
                    break;
                case BG_HOME_TAB2:
                    drawableForReturn = context.getResources().getDrawable(R.drawable.gesture_chrismas_dot2);
                    break;
                case BG_HOME_TAB3:
                    drawableForReturn = context.getResources().getDrawable(R.drawable.gesture_chrismas_dot3);
                    break;
                case BG_HOME_TAB4:
                    drawableForReturn = context.getResources().getDrawable(R.drawable.gesture_chrismas_dot4);
                    break;
                case BG_HOME_ASIDE_FRAGMENT:
                    drawableForReturn = context.getResources().getDrawable(R.drawable.gesture_chrismas_dot1);
                    break;
                case BG_LOCKSCREEN_WHOLE:
                    drawableForReturn = context.getResources().getDrawable(R.drawable.lockscreen_chrismas_bg);
                    break;
                case ICON_HOME_UP_ARROW:
                    drawableForReturn = context.getResources().getDrawable(R.drawable.gesture_chrismas_dot1);
                    break;
                case BG_HOME_MORE_FRAGMENT_LABEL:
                    drawableForReturn = context.getResources().getDrawable(R.drawable.gesture_chrismas_dot1);
                    break;
                default:
                    break;
            }
            return drawableForReturn;
        }
    }

    private static Drawable randomADrawable(Context context) {
        Random ran = new Random();
        int nextInt = ran.nextInt(10);
        return context.getResources().getDrawable(LOCKSCREEN_DOT_DRAWABLE_ID[nextInt]);
    }
}
