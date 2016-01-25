
package com.leo.appmaster.sdk.update;

public interface OnStateChangeListener {
    public void onShowProgressOnStatusBar();

    public void onProgress(int complete, int total);

    public void onChangeState(int type, int param);
    
    public void onNotifyUpdateChannel(int channel);
}
