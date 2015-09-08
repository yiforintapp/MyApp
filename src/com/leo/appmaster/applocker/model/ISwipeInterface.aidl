// ISwipeService.aidl
package com.leo.appmaster.applocker.model;

import com.leo.appmaster.applocker.model.LockMode;

// Declare any non-default types here with import statements

interface ISwipeInterface {
    // iswipe 消失
    void onISwipeDismiss();
    // 获取当前的情景模式
    List<LockMode> getLockModeList();
}
