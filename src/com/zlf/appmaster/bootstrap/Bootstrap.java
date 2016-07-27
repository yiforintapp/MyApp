package com.zlf.appmaster.bootstrap;

import android.os.SystemClock;
import android.support.v4.util.LongSparseArray;
import android.widget.Toast;

import com.zlf.appmaster.AppMasterApplication;
import com.zlf.appmaster.AppMasterConfig;
import com.zlf.appmaster.ThreadManager;
import com.zlf.appmaster.bootstrap.BootstrapGroup;
import com.zlf.appmaster.bootstrap.ForegroundBootstrap;
import com.zlf.appmaster.bootstrap.ForegroundDelayBootstrap;
import com.zlf.appmaster.utils.LeoLog;

/**
 * 启动程序抽象类
 *
 * @author Jasper
 */
public abstract class Bootstrap {
    private static final String TAG = "Bootstrap";

    /**
     * 前台任务
     */
    public static final int STEP_FOREGROUND = 100;
    /**
     * 后台任务
     */
    public static final int STEP_BACKGROUND = STEP_FOREGROUND + 100;
    /**
     * 前台延时任务
     */
    public static final int STEP_FOREGROUND_DELAY = STEP_BACKGROUND + 100;
    /**
     * 后台延时任务
     */
    public static final int STEP_BACKGROUND_DELAY = STEP_FOREGROUND_DELAY + 100;

    private static final int TIME_UI_ALARM = 300;
    private static final int TIME_ASYNC_ALARM = 10 * 1000;

    public int[] mStepIds;
    protected int mStepId;

    protected AppMasterApplication mApp;

    protected LongSparseArray<Bootstrap> mStrapArray;

    Bootstrap() {
        mApp = AppMasterApplication.getInstance();
        mStrapArray = new LongSparseArray<Bootstrap>();
    }

    public Bootstrap getBootstrap(int stepId) {
        Bootstrap step = mStrapArray.get(stepId);
        if (step != null) return step;

        step = createBootstrap(stepId);
        if (step == null) {
            throw new RuntimeException("create strap failed. strap is null.");
        }
        step.mStepId = stepId;
        mStrapArray.put(stepId, step);

        return step;
    }

    /**
     * 创建单独启动任务
     *
     * @param stepId
     * @return
     */
    protected Bootstrap createBootstrap(int stepId) {
        Bootstrap step = null;
        switch (stepId) {
            case STEP_FOREGROUND:
                step = new ForegroundBootstrap();
                break;
            case STEP_BACKGROUND:
                step = new com.zlf.appmaster.bootstrap.BackgroundBootstrap();
                break;
            case STEP_BACKGROUND_DELAY:
                step = new com.zlf.appmaster.bootstrap.BackgroundDelayBootstrap();
                break;
            case STEP_FOREGROUND_DELAY:
                step = new ForegroundDelayBootstrap();
                break;
            default:
                break;
        }
        return step;
    }

    protected final void strap() {
        long start = SystemClock.elapsedRealtime();
        try {
            doStrap();
        } catch (Exception e) {
            e.printStackTrace();
        }
        long cost = SystemClock.elapsedRealtime() - start;
        if (!(this instanceof BootstrapGroup)) {
            LeoLog.i(TAG, "-->" + getClassTag() + " cost: " + cost);
            if (ThreadManager.isUiThread()) {
                if (cost > TIME_UI_ALARM && AppMasterConfig.LOGGABLE) {
                    String msg = getClassTag() + "耗时超过 " + TIME_UI_ALARM + " ms, 需要优化";
                    toast(msg);
                }
            } else {
                if (cost > TIME_ASYNC_ALARM && AppMasterConfig.LOGGABLE) {
                    String msg = getClassTag() + "耗时超过 " + TIME_ASYNC_ALARM + " ms, 请检查代码逻辑";
                    toast(msg);
                }
            }
        }
    }

    private void toast(final String msg) {
        if (AppMasterApplication.sScreenWidth <= 320) {
            return;
        }
        ThreadManager.executeOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(mApp, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    protected abstract boolean doStrap();

    public abstract String getClassTag();

    public void execute() {
        strap();
    }
}
