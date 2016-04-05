package com.leo.appmaster.bootstrap;

import com.leo.appmaster.ThreadManager;

/**
 * 后台任务初始化组
 * 1、初始化部分异步任务
 * 2、闪屏相关初始化
 * 3....
 * @author Jasper
 *
 */
public class BackgroundBootstrap extends BootstrapGroup {
    private static final String TAG = "BackgroundBootstrap";
    
    /**
     * 初始化程序，原Application的startInitTask
     */
    private static final int STEP_INIT = STEP_BACKGROUND + 1;
    

    
    /**
     * 启动步骤分组，越前面优先级越高
     */
    private static final int[] STEPS = {STEP_INIT};

    BackgroundBootstrap() {
        super();
        mStepIds = STEPS;
        
    }
    
    @Override
    protected Bootstrap createBootstrap(int stepId) {
        Bootstrap strap = null;
        switch (stepId) {
            case STEP_INIT:
                strap = new InitAsyncBootstrap();
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
        ThreadManager.executeOnAsyncThread(new Runnable() {

            @Override
            public void run() {
                strap();
            }
        });
    }

}
