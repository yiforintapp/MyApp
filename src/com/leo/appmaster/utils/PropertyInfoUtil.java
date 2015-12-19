package com.leo.appmaster.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.text.format.Formatter;

public class PropertyInfoUtil {
    public static String getAvailMemory(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        MemoryInfo mi = new MemoryInfo();
        am.getMemoryInfo(mi);
      return Formatter.formatFileSize(context, mi.availMem);
    }
    public static String getTotalMemory(Context context) {
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
            LeoLog.i("PropertyInfoUtil", str2+" : "+num + "\t");
        }
        initial_memory = Integer.valueOf(arrayOfString[1]).intValue() * 1024;
        localBufferedReader.close();

        } catch (IOException e) {
        }
        return Formatter.formatFileSize(context, initial_memory);
    }
}
