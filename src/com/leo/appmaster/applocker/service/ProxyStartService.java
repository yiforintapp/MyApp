package com.leo.appmaster.applocker.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.text.TextUtils;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.applocker.model.ProcessAdj;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.utils.LeoLog;
import com.leo.imageloader.utils.IoUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by runlee on 16-1-16.
 */
public class ProxyStartService extends Service {
    private static final String TAG = "ProxyStartService";
    private static final String PS = "ps";
    private static final String THEME_PS_FILTER = "com.leo.theme";
    private static final boolean DBG = false;
    protected static final String REGEX_SPACE = "\\s+";
    private static final int INDEX_USER = 0;
    private static final int INDEX_PID = 1;
    private static final int INDEX_PPID = 2;
    private static final int INDEX_PROCESS_NAME = 8;
    public static final int NEW_THEME_MIN_VERCODE = 14;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LeoLog.d(TAG, "PG_ProxyStartService---start service...");
        this.stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 是否有新版主题存活
     *
     * @return
     */
    public static List<AppItemInfo> isNewThemActive() {
        List<AppItemInfo> apps = getActiveThemeProc();
        if (apps == null || apps.size() < 1) {
            return null;
        }
        return apps;
    }

    /**
     * 获取存活的新版主题（1.3版本以上）包信息
     *
     * @return
     */
    public static List<AppItemInfo> getActiveThemeProc() {
        List<AppItemInfo> themes = null;
        List<ProcessAdj> themeProcs = getProcessWithPS(THEME_PS_FILTER);
        Context context = AppMasterApplication.getInstance();
        PackageManager pm = context.getPackageManager();
        if (themeProcs.isEmpty()) {
            return themes;
        }
        themes = new ArrayList<AppItemInfo>();
        for (ProcessAdj proc : themeProcs) {
            String pkgName = proc.pkg;
            if (TextUtils.isEmpty(pkgName)) {
                continue;
            }
            int verCode;
            String packageName;
            try {
                PackageInfo info = pm.getPackageInfo(pkgName, 0);
                verCode = info.versionCode;
                if (verCode < NEW_THEME_MIN_VERCODE) {
                    continue;
                }
                packageName = info.packageName;
                AppItemInfo appInfo = new AppItemInfo();
                appInfo.packageName = packageName;
                appInfo.versionCode = verCode;
                LeoLog.d(TAG, "PG_getActiveThemeProc---packageName:" + packageName + ",verCode:" + verCode);
                themes.add(appInfo);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

        }
        return themes;
    }

    /**
     * 获取存活的主题进程信息
     *
     * @param pkgName
     * @return
     */
    private static List<ProcessAdj> getProcessWithPS(String pkgName) {

        Process p = null;
        InputStream is = null;
        BufferedReader br = null;
        ArrayList<ProcessAdj> allProcess = new ArrayList<ProcessAdj>();
        try {
            p = Runtime.getRuntime().exec(PS);
            is = p.getInputStream();

            br = new BufferedReader(new InputStreamReader(is));
            String line = null;
            int zygoteId = 0;
            while ((line = br.readLine()) != null) {
                ProcessAdj pair = getProcessAdjByFormatedLine(line, zygoteId, pkgName);
                if (pair != null) {
                    allProcess.add(pair);
                }
            }
        } catch (Exception e) {
            LeoLog.e(TAG, "PG_getForegroundProcess ex  " + e.getMessage());
        } finally {
            IoUtils.closeSilently(br);
            IoUtils.closeSilently(is);

            if (p != null) {
                p.destroy();
            }
        }

        return allProcess;
    }

    /**
     * 获取所有指定存活进程
     *
     * @param line
     * @param zygoteId
     * @param pkgName
     * @return
     */
    private static ProcessAdj getProcessAdjByFormatedLine(String line, int zygoteId, String pkgName) {
        if (TextUtils.isEmpty(line)) return null;

        Pattern pattern = Pattern.compile(REGEX_SPACE);
        Matcher matcher = pattern.matcher(line);

        if (matcher.find()) {
            line = matcher.replaceAll(",");
        }

        if (DBG) {
            LeoLog.i(TAG, line);
        }

        String[] array = line.split(",");
        if (array == null || array.length == 0) return null;

        if (array.length <= INDEX_PROCESS_NAME) return null;

        if (DBG) {
            LeoLog.i(TAG, "PG_array length: " + array.length);
        }

        ProcessAdj processAdj = new ProcessAdj();

        String user = array[INDEX_USER];
        processAdj.user = user;

        String ppidStr = array[INDEX_PPID];
        int ppid = Integer.parseInt(ppidStr);
        if (zygoteId != 0 && ppid != zygoteId) return null;


        String processName = array[INDEX_PROCESS_NAME];
        processAdj.pkg = processName;

        String processIdStr = array[INDEX_PID];
        if (TextUtils.isEmpty(processName)) return null;

        int processId = 0;
        try {
            processId = Integer.parseInt(processIdStr);
        } catch (Exception e) {
        }
        if (processId == 0) return null;

        int oomAdj = 0; //

        // 解决一些前台进程的进程名是子进程的问题,com.mobisystems.office:browser
        if (processName.contains(":")) {
            processName = processName.substring(0, processName.indexOf(":"));
        }
        processAdj.oomAdj = oomAdj;
        processAdj.pid = processId;
        processAdj.pkg = processName;
        processAdj.ppid = ppid;
        if (TextUtils.isEmpty(processName)
                || (!TextUtils.isEmpty(pkgName) && !processName.contains(pkgName))) {
            return null;
        }
        LeoLog.i(TAG, "PG_inner: " + processAdj.toString());
        return processAdj;
    }

}
