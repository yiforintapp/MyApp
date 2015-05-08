
package com.leo.appmaster.quickgestures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;

import com.leo.appmaster.AppMasterPreference;

public class QuickGestureManager {
    public static List<String> getFreeDisturbAppName(Context context) {
        List<String> packageNames = new ArrayList<String>();
        String packageName = AppMasterPreference.getInstance(context)
                .getFreeDisturbAppPackageName();
        if (!AppMasterPreference.PREF_QUICK_GESTURE_FREE_DISTURB_APP_PACKAGE_NAME
                .equals(packageName)) {
            String[] names = packageName.split(";");
            packageNames = Arrays.asList(names);
        }
        return packageNames;
    }
}
