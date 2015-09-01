package com.leo.appmaster.bootstrap;

/**
 * 后台延时初始化组
 * 1、检查更新
 * 2...
 * @author Jasper
 *
 */
public class BackgroundDelayBootstrap extends BootstrapGroup {
    private static final String TAG = "BackgroundDelayBootstrap";
    
    /**
     * 十秒延时
     */
    private static final long DELAY = 10 * 1000;
    
    /**
     * 10秒延时
     */
    private static final int STEP_CHECK_NEW = STEP_BACKGROUND_DELAY + 1;
    
    private static final int[] STEPS = { STEP_CHECK_NEW };

    public BackgroundDelayBootstrap() {
        super();
        mStepIds = STEPS;
    }
    
    @Override
    protected Bootstrap createBootstrap(int stepId) {
        Bootstrap strap = null;
        switch (stepId) {
            case STEP_CHECK_NEW:
                strap = new CheckNewBootstrap();
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
        mApp.postInAppThreadPool(new Runnable() {
            
            @Override
            public void run() {
                strap();
            }
        }, DELAY);
    }
    
}
