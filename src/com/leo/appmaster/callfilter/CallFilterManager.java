package com.leo.appmaster.callfilter;

import android.content.Context;

/**
 * Created by runlee on 15-12-18.
 */
public class CallFilterManager {
    private static CallFilterManager mInstance;
    private Context mContext;
    /**
     * 拨出电话
     */
    private boolean mIsComingOut = false;

    public boolean isComingOut() {
        return mIsComingOut;
    }

    public void setIsComingOut(boolean isComingOut) {
        this.mIsComingOut = isComingOut;
    }

    public static synchronized CallFilterManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new CallFilterManager(context.getApplicationContext());
        }
        return mInstance;
    }

    private CallFilterManager(Context context) {
        mContext = context;
    }
}
