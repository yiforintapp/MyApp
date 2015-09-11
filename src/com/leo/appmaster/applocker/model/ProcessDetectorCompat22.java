package com.leo.appmaster.applocker.model;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Process;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.utils.LeoLog;

public class ProcessDetectorCompat22 extends ProcessDetector {
    private static final String TAG = "ProcessDetectorCompat22"; 
    private static final String OOM_SCORE = "oom_score";
    
    // oom_score收不到文件监控消息，需要按频率扫描
    private static final int WAIT_HOME_TIMEOUT = 0;
    private static final int MAX_SCORE = 80;
    private static int MIN_SCORE = 30;
    // min_score被减小的值
    private static final int MIN_DIFF_DEC = 15;
    // 最小diff上限
    private static final int MIN_DIFF_UP_LIMIT = MAX_SCORE - MIN_SCORE;
    
    // zygote进程的最大值，父进程大于这个值便不是zygote孵化
    private static final int MAX_ZYGOTE = 1000;
    
    private static int mForegroundScore = 0;
    
    static {
        AppMasterApplication context = AppMasterApplication.getInstance();
        MIN_SCORE = AppMasterPreference.getInstance(context).getForegroundMinScore();
    }
    
    /**
     * 设置oom_score值，后续作为参考值, leo到前台后会触发设置
     */
    public static void setForegroundScore() {
        ThreadManager.executeOnAsyncThread(new Runnable() {

            @Override
            public void run() {
                int score = getOomScore(Process.myPid());
                score = score > MAX_SCORE ? MAX_SCORE : score;

                score /= 2;
                MIN_SCORE = score / 2;

                AppMasterApplication context = AppMasterApplication.getInstance();
                AppMasterPreference.getInstance(context).setForegroundMinScore(MIN_SCORE);

                mForegroundScore = score;
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
    }

    @Override
    public int getTimeoutMs(ProcessAdj proAdj) {
        if (isHomePackage(proAdj)) {
            return WAIT_HOME_TIMEOUT;
        }
        return super.getTimeoutMs(proAdj);
    }

    @Override
    public String getObservePath(ProcessAdj processAdj) {
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
    protected ProcessAdj filterForegroundProcess(List<ProcessAdj> result) {
        if (result == null || result.isEmpty()) return null;
        
        if (mForegroundScore == 0) {
            result.clear();
            return null;
        }
        Context context = AppMasterApplication.getInstance();
        ProcessAdj minAdj = null;
        int minDiff = Integer.MAX_VALUE;
        for (ProcessAdj processAdj : result) {
            if (processAdj.oomAdj > 0 && processAdj.ppid < MAX_ZYGOTE) {
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
    
    private boolean isOOMScoreAdjModified(ProcessAdj adj) {
        String path = "/proc/" + adj.pid + File.separator + "oom_adj";
        File file = new File(path);
        
        if (!file.exists()) return false;
        
        long lastModify = file.lastModified();
        
        long current = System.currentTimeMillis();
        
        // 差值小于2秒
        return Math.abs(current - lastModify) < 1000;
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
            close(baos);
            close(fis);
        }
        return PERMISSION_DENY;
    }

}
