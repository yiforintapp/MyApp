package com.leo.appmaster.utils;

import android.content.Context;

import com.leo.appmaster.sdk.SDKWrapper;


public class LoadFailUtils {
    
    public static void sendLoadFail(Context context, String category) {
        Context ctx = context.getApplicationContext();
        if(NetWorkUtil.isNetworkAvailable(ctx)) {
            SDKWrapper.addEvent(ctx, SDKWrapper.P1, "load_failed", category);
        }
    }

}
