package com.leo.appmaster;

import com.leo.appmaster.engine.AppLoadEngine;

import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;

public class AppMasterApplication extends Application {
    
    private AppLoadEngine mAppsEngine;
    
   @Override
   public void onCreate() {
       super.onCreate();
       
       mAppsEngine = AppLoadEngine.getInstance(this);
       mAppsEngine.preloadAllBaseInfo();
        // Register intent receivers
       IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");
        registerReceiver(mAppsEngine, filter);
   }
   
   
   @Override
   public void onTerminate() {
       super.onTerminate();
       unregisterReceiver(mAppsEngine);
   }

}