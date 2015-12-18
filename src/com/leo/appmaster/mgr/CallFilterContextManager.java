package com.leo.appmaster.mgr;

/**
 * Created by runlee on 15-12-18.
 */
public abstract class CallFilterContextManager extends Manager {
    @Override
    public void onDestory() {

    }

    @Override
    public String description() {
        return MgrContext.MGR_CALL_FILTER;
    }
}
