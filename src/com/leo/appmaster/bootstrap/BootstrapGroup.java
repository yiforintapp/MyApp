package com.leo.appmaster.bootstrap;

/**
 * 启动程序分组
 * @author Jasper
 *
 */
public class BootstrapGroup extends Bootstrap {
    private static final String TAG = "BootstrapGroup";
    
    public BootstrapGroup() {
        super();
    }
    
    @Override
    protected boolean doStrap() {
        if (mStepIds != null) {
            for (int i : mStepIds) {
                getBootstrap(i).execute();
            }
            return true;
        }
        return false;
    }

    @Override
    public String getClassTag() {
        return TAG;
    }
    
}
