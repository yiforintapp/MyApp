package com.zlf.appmaster.applocker.model;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Process;
import android.text.TextUtils;

import com.zlf.appmaster.AppMasterApplication;
import com.zlf.appmaster.AppMasterPreference;
import com.zlf.appmaster.ThreadManager;
import com.zlf.appmaster.utils.LeoLog;
import com.zlf.imageloader.utils.IoUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProcessDetectorCompat22 extends com.zlf.appmaster.applocker.model.ProcessDetector {
    private static final String TAG = "ProcessDetectorCompat22"; 
    private static final String OOM_SCORE = "oom_score";

    private static final boolean DBG = false;

    private static final int INDEX_USER = 0;
    private static final int INDEX_PID = 1;
    private static final int INDEX_PPID = 2;
    private static final int INDEX_STATE = 5;
    private static final int INDEX_PROCESS_NAME = 9;

    // oom_score收不到文件监控消息，需要按频率扫描
    private static final int WAIT_HOME_TIMEOUT = 0;
    private static final int MAX_SCORE = 100;
    private static final int MIN_SCORE = 10;
    // 最小diff上限
    private static final int MIN_DIFF_UP_LIMIT = MAX_SCORE - MIN_SCORE;

    private static final String PS = "ps -P";
    protected String mPsCmd = PS;
    private static int mForegroundScore = 0;

    /**
     * 设置oom_score值，后续作为参考值, leo到前台后会触发设置
     */
    public static void setForegroundScore() {
        ThreadManager.executeOnAsyncThread(new Runnable() {

            @Override
            public void run() {
                int score = getOomScore(Process.myPid());
                score = score > MAX_SCORE ? MAX_SCORE : score;

                mForegroundScore = score;
                Context context = AppMasterApplication.getInstance();
                AppMasterPreference.getInstance(context).setForegroundScore(mForegroundScore);
                LeoLog.i(TAG, "setForegroundScore async, score: " + mForegroundScore);
            }
        });
    }

    private static void setForegroundScore(int score) {
        score = score > MAX_SCORE ? MAX_SCORE : score;
        score = score < MIN_SCORE ? MIN_SCORE : score;
        if (score > mForegroundScore && mForegroundScore > 0) {
            score = (mForegroundScore + score) / 2;
        }

        if (score < mForegroundScore) {
            Context context = AppMasterApplication.getInstance();
            AppMasterPreference.getInstance(context).setForegroundScore(score);
            mForegroundScore = score;
        }
        
        LeoLog.i(TAG, "setForegroundScore, score: " + mForegroundScore);
    }

    public ProcessDetectorCompat22() {
        super();
        
        Context context = AppMasterApplication.getInstance();
        mForegroundScore = AppMasterPreference.getInstance(context).getForegroundScore();
        mPsCmd = PS;
    }

    @Override
    public int getTimeoutMs(com.zlf.appmaster.applocker.model.ProcessAdj proAdj) {
        if (isHomePackage(proAdj)) {
            return WAIT_HOME_TIMEOUT;
        }
        return super.getTimeoutMs(proAdj);
    }

    @Override
    public String getObservePath(com.zlf.appmaster.applocker.model.ProcessAdj processAdj) {
        return "/proc/" + processAdj.pid;
    }

    @Override
    public boolean ready() {
        return mForegroundScore != 0;
    }

    @Override
    public boolean isOOMScoreMode() {
        return true;
    }

    @Override
    public String getProcessAdjPath(int pid) {
        return "/proc/" + pid + File.separator + OOM_SCORE;
    }

    @Override
    protected com.zlf.appmaster.applocker.model.ProcessAdj getProcessAdjByFormatedLine(String line, int zygoteId) {
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
            LeoLog.i(TAG, "array length: " + array.length);
        }

        com.zlf.appmaster.applocker.model.ProcessAdj processAdj = new com.zlf.appmaster.applocker.model.ProcessAdj();

        String user = array[INDEX_USER];
        processAdj.user = user;

        String ppidStr = array[INDEX_PPID];
        int ppid = Integer.parseInt(ppidStr);
        if (zygoteId != 0 && ppid != zygoteId) return null;


        String processName = array[INDEX_PROCESS_NAME];
        processAdj.pkg = processName;
        for (ProcessFilter filter : mFilters) {
            if (filter.filterProcess(processAdj)) return null;
        }

        String processIdStr = array[INDEX_PID];
        if (TextUtils.isEmpty(processName)) return null;

        int processId = 0;
        try {
            processId = Integer.parseInt(processIdStr);
        } catch (Exception e) {
        }
        if (processId == 0) return null;

        String state = array[INDEX_STATE];
        if (isOOMScoreMode() && !"fg".equals(state)) return null;

        int oomAdj = getOomScoreAdj(processId);

        // 解决一些前台进程的进程名是子进程的问题,com.mobisystems.office:browser
        if (processName.contains(":")) {
            processName = processName.substring(0, processName.indexOf(":"));
        }
        processAdj.oomAdj = oomAdj;
        processAdj.pid = processId;
        processAdj.pkg = processName;
        processAdj.ppid = ppid;

        if (DBG) {
            LeoLog.i(TAG, processAdj.toString());
        }

        return processAdj;
    }

    @Override
    protected String getPsCmd() {
        return PS;
    }

    @Override
    protected com.zlf.appmaster.applocker.model.ProcessAdj filterForegroundProcess(List<com.zlf.appmaster.applocker.model.ProcessAdj> result) {
        if (result == null || result.isEmpty()) return null;
        
        if (mForegroundScore == 0) {
            result.clear();
            return null;
        }
        filterSamePkgList(result);
        Context context = AppMasterApplication.getInstance();
        com.zlf.appmaster.applocker.model.ProcessAdj minAdj = null;
        int minDiff = Integer.MAX_VALUE;
        for (com.zlf.appmaster.applocker.model.ProcessAdj processAdj : result) {
            if (processAdj.oomAdj > 0) {
                if (minAdj != null && processAdj.oomAdj > minAdj.oomAdj) continue;
                
                int diff = Math.abs(processAdj.oomAdj - mForegroundScore);
                if (diff < minDiff ||
                        (minAdj != null && processAdj.oomAdj < minAdj.oomAdj)) {
                    Intent intent = new Intent();
                    intent.setPackage(processAdj.pkg);
                    intent.setAction(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, 0);
                    if (list == null || list.isEmpty())
                        continue;

                    minDiff = diff;
                    minAdj = processAdj;
                }
            }
        }
        
        result.clear();
        if (minAdj != null && minDiff < MIN_DIFF_UP_LIMIT 
                && minAdj.oomAdj < MAX_SCORE && minAdj.oomAdj > MIN_SCORE) {
            setForegroundScore(minAdj.oomAdj);
            return minAdj;
        }
        
        return null;
    }

    public static int getOomScore(int pid) {
        String path = "/proc/" + pid + File.separator + OOM_SCORE;
        
        FileInputStream fis = null;
        ByteArrayOutputStream baos = null;
        try {
            fis = new FileInputStream(path);
            baos = new ByteArrayOutputStream();
            
            byte[] buffer = new byte[1024];
            int len;  
            while ((len = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, len);  
            }  
            String str = baos.toString();  
            
            return Integer.parseInt(str.trim());
        } catch (Exception e) {
            LeoLog.e(TAG, "getAdjByProcessPath ex path: " + path  + " | " + e.getMessage());
        } finally {
            IoUtils.closeSilently(baos);
            IoUtils.closeSilently(fis);
        }
        return PERMISSION_DENY;
    }

}
