package com.leo.appmaster;

import com.leo.appmaster.engine.AppLoadEngine;
import com.leoers.leoanalytics.LeoStat;

import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

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

        iniLeoSdk();
   }
   
   
   @Override
   public void onTerminate() {
       super.onTerminate();
       unregisterReceiver(mAppsEngine);
   }
   
   private void iniLeoSdk() {
       LeoStat.init(getApplicationContext(), "1", "applocker");
//       LeoStat.setUpdateService();  //暂时 不进行启动升级检测
   }

}