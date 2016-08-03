package com.zlf.appmaster.utils;

import android.util.Log;

/**
 * 封装log，以便输出控制
 * @author Yushian
 *
 */
public final class QLog {
	public final static String TAG = "charles";
	private static boolean logFlag = true;
	private final static int logLevel = Log.VERBOSE;
	
	//UrlConstants 会处理
	public static void setLogFlag(Boolean flag){
		logFlag = flag;
	}
	
	
	/**
	 * 快速打印
	 * @param msg
	 */
	public static void q(String msg) {
		if (logFlag) {
			if (logLevel <= Log.INFO) {
				Log.i("QiNiu QuickPrint", msg);
			}
		}
	}
	
	/** 
     * The Log Level:i 
     * @param tag,msg 
     */  
    public static void i(String tag, String msg){
    	if (logFlag) {
			if (logLevel <= Log.INFO) {
				Log.i(tag, msg);
			}
		} 
    } 
    
	/**
     * The Log Level:d
     * @param tag,msg
     */   
    public static void d(String tag, String msg) {
    	if (logFlag) {
			if (logLevel <= Log.DEBUG) {
				Log.i(tag, msg);
			}
		}
	}
    
    /** 
     * The Log Level:v 
     * @param tag,msg
     */  
    public static void v(String tag, String msg){
    	if (logFlag) {
			if (logLevel <= Log.VERBOSE) {
				Log.v(tag, msg);
			}
		} 
    } 
    
    /** 
     * The Log Level:w
     * @param tag,msg
     */  
    public static void w(String tag, String msg){
		if (logLevel <= Log.WARN) {
			Log.w(tag, msg);
		}
    } 
    
    /** 
     * The Log Level:e
     * @param tag,msg
     */  
    public static void e(String tag, String msg){
		if (logLevel <= Log.ERROR) {
			Log.e(tag, msg);
		}
    } 
}
