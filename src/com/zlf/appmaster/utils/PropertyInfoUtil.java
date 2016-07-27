package com.zlf.appmaster.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class PropertyInfoUtil {
    public static long getAvailMemory(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        MemoryInfo mi = new MemoryInfo();
        am.getMemoryInfo(mi);
        return mi.availMem;
//      return Formatter.formatFileSize(context, mi.availMem);
    }

//    @SuppressLint("NewApi")
//	public static long getTotalMemory2(Context context) {
//        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//        MemoryInfo mi = new MemoryInfo();
//        am.getMemoryInfo(mi);
//        return mi.totalMem;
////      return Formatter.formatFileSize(context, mi.availMem);
//    }

    public static long getTotalMemory(Context context) {
        String str1 = "/proc/meminfo";
        String str2;
        String[] arrayOfString;
        long initial_memory = 0;
        try {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(
                    localFileReader, 8192);
            str2 = localBufferedReader.readLine();
            arrayOfString = str2.split("\\s+");
            for (String num : arrayOfString) {
                com.zlf.appmaster.utils.LeoLog.i("PropertyInfoUtil", str2 + " : " + num + "\t");
            }
            int memory = Integer.valueOf(arrayOfString[1]).intValue();
            initial_memory = memory;
            localBufferedReader.close();

        } catch (IOException e) {
        }
        return initial_memory;
//        return Formatter.formatFileSize(context, initial_memory);
    }
}
