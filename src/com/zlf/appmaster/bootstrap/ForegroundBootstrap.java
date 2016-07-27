package com.zlf.appmaster.bootstrap;

import com.zlf.appmaster.bootstrap.InitCoreBootstrap;

/**
 * 前台任务组
 * 1、核心业务初始化
 * 2....
 * @author Jasper
 *
 */
public class ForegroundBootstrap extends com.zlf.appmaster.bootstrap.BootstrapGroup {
    private static final String TAG = "ForegroundBootstrap";
    
    /**
     * 核心业务初始化
     */
    private static final int STEP_INIT_CORE = STEP_FOREGROUND + 1;
    
    private static final int[] STEPS = { STEP_INIT_CORE };
    
    ForegroundBootstrap() {
        super();
        mStepIds = STEPS;
        
    }

    @Override
    public com.zlf.appmaster.bootstrap.Bootstrap createBootstrap(int stepId) {
        com.zlf.appmaster.bootstrap.Bootstrap strap = null;
        switch (stepId) {
            case STEP_INIT_CORE:
                strap = new InitCoreBootstrap();
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
        super.execute();
    }

}
