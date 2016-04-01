package com.leo.appmaster.bootstrap;

import com.leo.appmaster.ThreadManager;

/**
 * Created by Jasper on 2016/3/30.
 */
public class ForegroundDelayBootstrap extends BootstrapGroup {
    private static final String TAG = "BackgroundDelayBootstrap";

    /**
     * 十秒延时
     */
    private static final long DELAY = 5 * 1000;

    /**
     * 10秒延时
     */
    private static final int STEP_CORE_DELAY = STEP_FOREGROUND_DELAY + 1;

    private static final int[] STEPS = { STEP_CORE_DELAY };

    ForegroundDelayBootstrap() {
        super();
        mStepIds = STEPS;
    }

    @Override
    protected Bootstrap createBootstrap(int stepId) {
        Bootstrap strap = null;
        switch (stepId) {
            case STEP_CORE_DELAY:
                strap = new InitCoreDelayBootstrap();
                break;
            default:
                break;
        }
        return strap;
    }

    @Override
    public String getClassTag() {
        return TAG;
    }

    @Override
    public final void execute() {
        ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                strap();
            }
        }, DELAY);
    }
}
