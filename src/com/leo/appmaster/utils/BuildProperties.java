
package com.leo.appmaster.utils;

//引用到的工具类
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import com.android.internal.app.IAppOpsCallback;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.content.Context;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;

public class BuildProperties {
    private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";
    private final Properties properties;

    private BuildProperties() throws IOException {
        properties = new Properties();
        properties
                .load(new FileInputStream(new File(Environment.getRootDirectory(), "build.prop")));
    }

    public boolean containsKey(final Object key) {
        return properties.containsKey(key);
    }

    public boolean containsValue(final Object value) {
        return properties.containsValue(value);
    }

    public Set<Entry<Object, Object>> entrySet() {
        return properties.entrySet();
    }

    public String getProperty(final String name) {
        return properties.getProperty(name);
    }

    public String getProperty(final String name, final String defaultValue) {
        return properties.getProperty(name, defaultValue);
    }

    public boolean isEmpty() {
        return properties.isEmpty();
    }

    public Enumeration<Object> keys() {
        return properties.keys();
    }

    public Set<Object> keySet() {
        return properties.keySet();
    }

    public int size() {
        return properties.size();
    }

    public Collection<Object> values() {
        return properties.values();
    }

    public static BuildProperties newInstance() throws IOException {
        return new BuildProperties();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean checkOp(Context context, int op) {
        final int version = Build.VERSION.SDK_INT;

        if (version >= 19) {
            AppOpsManager manager = (AppOpsManager) context
                    .getSystemService(Context.APP_OPS_SERVICE);
            // try {
            AppOpsManager method = null;
            try {
                method = (AppOpsManager) invokePrivateMethod(manager, "checkOp");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
//            method.checkOp(op, Binder.getCallingUid(), context.getPackageName());
            if (AppOpsManager.MODE_ALLOWED == (Integer) manager.checkOp(op,
                    Binder.getCallingUid(), context.getPackageName())) {
                return true;
            } else {
                return false;
            }
            // } catch (Exception e) {
            // // Flog.e(e.getMessage());
            // }
        } else {
            // Flog.e("Below API 19 cannot invoke!");
        }
        return false;
    }
    public static Object invokePrivateMethod(Object obj, String methodName) throws Exception {
        Object value = null;
        Class<?> cls = obj.getClass();
        Method method = cls.getDeclaredMethod(methodName, (Class[]) null);
        method.setAccessible(true);
        value = method.invoke(obj, (Object[]) null);
        return value;
    }

    /**
     * 判断MIUI的悬浮窗权限
     * 
     * @param context
     * @return
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean isMiuiFloatWindowOpAllowed(Context context) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 19) {
            checkOp(context, AppOpsManager.OP_SYSTEM_ALERT_WINDOW);
        } else {
            if ((context.getApplicationInfo().flags & 1 << 27) == 1) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    // 判断系统类型
    public static boolean isMIUI() {
        try {
            final BuildProperties prop = BuildProperties.newInstance();
            return prop.getProperty(KEY_MIUI_VERSION_CODE, null) != null
                    || prop.getProperty(KEY_MIUI_VERSION_NAME, null) != null
                    || prop.getProperty(KEY_MIUI_INTERNAL_STORAGE, null) != null;
        } catch (final IOException e) {
            return false;
        }
    }
}
