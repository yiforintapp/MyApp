
package com.zlf.appmaster.utils;

import java.io.File;

public class RootChecker {

    private static final String kSuSearchPaths[] = {
            "/system/bin/", "/system/xbin/", "/system/sbin/", "/sbin/", "/vendor/bin/"
    };

    public static boolean isRoot() {
        // First, check build tag
        String buildTags = android.os.Build.TAGS;
        if(buildTags != null && buildTags.contains("test-keys")) {
            return true;
        }
        // Second, check superuser app
        try {
            File file = new File("system/app/Superuser.apk");
            if(file.exists()) {
                return true;
            }
        } catch (Exception e) {            
        }
        // Finally, check su
        try {           
            File f = null;
            for (int i = 0; i < kSuSearchPaths.length; i++) {
                f = new File(kSuSearchPaths[i] + "su");
                if (f != null && f.exists()) {
                    return true;
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

}
