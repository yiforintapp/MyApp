
package com.zlf.appmater.globalbroadcast;

import android.content.Intent;
import android.content.IntentFilter;

public abstract class BroadcastListener {

    protected Intent mIntent;

    protected void setIntent(Intent it) {
        mIntent = it;
    }

    protected abstract void onEvent(String action);

    protected abstract IntentFilter getIntentFilter();

}
