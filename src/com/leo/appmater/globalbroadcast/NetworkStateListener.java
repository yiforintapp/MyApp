
package com.leo.appmater.globalbroadcast;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

public abstract class NetworkStateListener extends BroadcastListener {

    @Override
    public final void onEvent(String action) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            onNetworkStateChange(mIntent);
        }
    }

    @Override
    protected final IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        return filter;
    }

    public abstract void onNetworkStateChange(Intent intent);

}
