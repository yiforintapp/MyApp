package com.leo.appmaster.utils;

import java.text.DecimalFormat;

import android.util.Log;

public class TextFormater {
	
	//已KB为单位
	public static String dataSizeFormat(long size) {
		DecimalFormat formater = new DecimalFormat("####.00");
		size = size <<10;
		if (size < 1024) {
			return size + "byte";
		} else if (size < (1 << 20)) // 左移20位，相当于1024 * 1024
		{
			float kSize = (float) size / 1024 ; // 右移10位，相当于除以1024
			return formater.format(kSize) + "KB";
		} else if (size < (1 << 30)) // 左移30位，相当于1024 * 1024 * 1024
		{
			float mSize = (float) size / (1024 * 1024); // 右移20位，相当于除以1024再除以1024
			return formater.format(mSize) + "MB";
		}/*
		 * else if (size < (double)(1 << 40)) { float gSize = size >> 30; return
		 * formater.format(gSize) + "GB"; } else { return "size : error"; }
		 */else {
			float gSize = (float) size / (1024 * 1024 * 1024);
			return formater.format(gSize) + "GB";
		}
	}

	public static String getSizeFromKB(long kSize) {
		return dataSizeFormat(kSize << 10);
	}

}
