package com.leo.appmaster;

import android.util.Log;

/**
 * Use debug configurations for debug and release configurations for release.
 */
public class AppMasterConfig {


     /*-----    Debug configurations   ------ */
	public static final boolean LOGGABLE = true;
	
	public static final int SDK_LOG_LEVER = Log.DEBUG;
	
	
	public static final int TIME_2_HOUR = 2 * 60 * 60 * 1000;
	public static final int TIME_12_HOUR = 12 * 60 * 60 * 1000;
    /*-----    Debug configurations  end  ------ */
	
	
	
//	/*-----     Release configurations   ------*/
//    public static final boolean LOGGABLE = false;
//    
//    public static final int SDK_LOG_LEVER = Log.ERROR;
//    
//    
//    public static final int TIME_2_HOUR = 2 * 60 * 60 * 1000;
//    public static final int TIME_12_HOUR = 12 * 60 * 60 * 1000;     
//     /*-----     Release configurations end   ------*/

}
