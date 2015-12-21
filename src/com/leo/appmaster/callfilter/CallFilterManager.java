package com.leo.appmaster.callfilter;

import android.content.Context;

/**
 * Created by runlee on 15-12-18.
 */
public class CallFilterManager {
    private static CallFilterManager mInstance;
    private Context mContext;

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
