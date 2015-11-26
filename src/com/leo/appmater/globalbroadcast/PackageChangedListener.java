
package com.leo.appmater.globalbroadcast;

import android.content.Intent;
import android.content.IntentFilter;

import com.leo.appmaster.utils.LeoLog;

public class PackageChangedListener extends BroadcastListener {

    public static final String TAG = "PACKAGE CHANGED";

    public final void onEvent(String action) {
        if (Intent.ACTION_PACKAGE_ADDED.equals(action)
                || Intent.ACTION_PACKAGE_REMOVED.equals(action)
                || Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
            onPackageChanged(mIntent);
        }
    }

    @Override
    protected final IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        return filter;
    }

    /**
     * added, changed, removed
     */
    public void onPackageChanged(Intent intent) {
        final String packageName = intent.getData().getSchemeSpecificPart();
        LeoLog.d(TAG, "onPackageChanged: " + intent.getAction() + "            " + packageName);
    }
}
