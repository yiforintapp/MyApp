package com.leo.appmaster;

import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;

import com.leo.appmaster.engine.AppLoadEngine;
import com.leoers.leoanalytics.LeoStat;

public class AppMasterApplication extends Application {

    
    private AppLoadEngine mAppsEngine;
    
    private static AppMasterApplication mInstance;
    
   @Override
   public void onCreate() {
       super.onCreate();       
       mInstance = this;
       mAppsEngine = AppLoadEngine.getInstance(this);
       mAppsEngine.preloadAllBaseInfo();
        // Register intent receivers
       IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        registerReceiver(mAppsEngine, filter);
        
        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
        filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
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
		// LeoStat.setUpdateService(); //暂时 不进行启动升级检测
	}

	public static AppMasterApplication getInstance() {
		return mInstance;
	}

}
