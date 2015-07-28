
package com.leo.appmaster.sdk.push;

public interface OnPushListener {
    public void onPush(String id, String title, String content, int showType);
}
