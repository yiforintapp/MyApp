
package com.leo.appmaster.update;

public interface OnStateChangeListener {
    public void onProgress(int complete, int total);

    public void onChangeState(int type, int param);
}
