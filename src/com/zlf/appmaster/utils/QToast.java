package com.zlf.appmaster.utils;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

//import android.os.Handler;

/**
 * 封装Toast 同一时刻只显示一个Toast
 * @author Yushian
 *
 */
public class QToast {
	private static Toast mToast;
	
	/**
	 * 短时间显示
	 */
	public static void showShortTime(Context mContext, int resId) {
		show(mContext,resId, Toast.LENGTH_SHORT);
	}
	
	public static void showShortTime(Context mContext, String text) {
		show(mContext,text, Toast.LENGTH_SHORT);
	}
    /**
     * 长时间提示
     */
    public static void showLongTime(Context mContext, int resId) {
        show(mContext,resId, Toast.LENGTH_LONG);
    }

    public static void showLongTime(Context mContext, String text) {
        show(mContext,text, Toast.LENGTH_LONG);
    }

    public static void show(Context mContext, String text, int duration) {
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }

        if(mToast == null) {
            mToast = Toast.makeText(mContext.getApplicationContext(), text,duration);
        } else {
            mToast.setText(text);
            mToast.setDuration(duration);
        }
        mToast.show();
        if (Looper.myLooper() == null) {
            Looper.loop();
        }
    }

    public static void show(Context mContext, int resId, int duration) {
        show(mContext, mContext.getResources().getString(resId), duration);
    }

    public static void cancel(){
        if (mToast != null){
            mToast.cancel();
        }
    }
}
