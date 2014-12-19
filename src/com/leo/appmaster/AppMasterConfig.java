package com.leo.appmaster;

import android.util.Log;

/**
 * Use debug configurations for debug and release configurations for release.
 */
public class AppMasterConfig {


     /*-----    Debug configurations   ------ */
	public static final boolean LOGGABLE = true;
	
	public static final int SDK_LOG_LEVER = Log.DEBUG;	
	
	/*
	 * RECOMMEND URL
	 */
	public static final String APP_RECOMMEND_URL = "http://api.leostat.com/appmaster/apprecommend/list";
	public static final String CHECK_NEW_BUSINESS_APP = "http://api.leostat.com/appmaster/apprecommend/checkappupdate";

	public static final int TIME_2_HOUR = 2 * 60 * 60 * 1000;
	public static final int TIME_12_HOUR = 12 * 60 * 60 * 1000;
//	public static final int TIME_2_HOUR = 2 * 60 * 1000;
//	public static final int TIME_12_HOUR = 2 * 60 * 1000;
    /*-----    Debug configurations  end  ------ */
	
	
	
	
//	/*-----     Release configurations   ------*/
//    public static final boolean LOGGABLE = false;
//    
//    public static final int SDK_LOG_LEVER = Log.ERROR;
//	
//	   /*
//     * RECOMMEND URL
//     */
//    public static final String APP_RECOMMEND_URL = "http://192.168.1.201:8080/leo/appmaster/apprecommend/list";
//    public static final String CHECK_NEW_BUSINESS_APP = "http://192.168.1.201:8080/leo/appmaster/apprecommend/checkappupdate";
//    
//    
//    public static final int TIME_2_HOUR = 2 * 60 * 60 * 1000;
//    public static final int TIME_12_HOUR = 12 * 60 * 60 * 1000;     
//     /*-----     Release configurations end   ------*/

}
