package com.leo.appmaster.utils;

import android.content.Context;

import com.leo.appmaster.sdk.SDKWrapper;
import com.leoers.leoanalytics.LeoStat;

public class LoadFailUtils {
    
    public static void sendLoadFail(Context context, String category) {
        if(NetWorkUtil.isNetworkAvailable(context)) {
            SDKWrapper.addEvent(context, LeoStat.P1, "load_failed", category);
        }
    }

}
