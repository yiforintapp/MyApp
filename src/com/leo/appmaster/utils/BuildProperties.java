
package com.leo.appmaster.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.sdk.SDKWrapper;

/**
 * 判断小米系统工具类
 * 
 * @author run
 */
public class BuildProperties {
    private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";
    private static final String sMake = Build.MANUFACTURER.toLowerCase();
    private static final String mModel = Build.MODEL.toLowerCase();
    public static final String I_STYLE_MODEL = "i-mobile I-STYLE 217";
    public static final String[] filterPhoneMode={"SM-N9150"};
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
        AppOpsManager manager = (AppOpsManager) context
                .getSystemService(Context.APP_OPS_SERVICE);
        @SuppressWarnings("unused")
        AppOpsManager method = null;
        try {
            method = (AppOpsManager) invokePrivateMethod(manager, "checkOp");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (AppOpsManager.MODE_ALLOWED == (Integer) manager.checkOp(op,
                Binder.getCallingUid(), context.getPackageName())) {
            return true;
        } else {
            return false;
        }
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
     * 判断悬浮窗权限
     * 
     * @param context
     * @return
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean isFloatWindowOpAllowed(Context context) {
        String string;
        int n;
        if (Build.VERSION.SDK_INT >= 19) {
            return checkOp(context, AppOpsManager.OP_SYSTEM_ALERT_WINDOW);
        }
        if (((n = (-28 + (string = Integer
                .toBinaryString((int) (context.getApplicationInfo().flags))).length())) < 0)
                || (string.charAt(n) != '1'))
            return false;
        return true;

    }

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

    /**
     * 本机品牌名称识别
     * 
     * @param brandName品牌名称
     * @return
     */
    public static boolean checkPhoneBrand(String brandName) {
        return sMake.equalsIgnoreCase(brandName);
    }

    /**
     * 详细机型识别
     * 
     * @param brandName(品牌：那个厂家的手机)
     * @param phone(机型)
     * @return
     */
    public static boolean detailMdelDistinguish(String brandName, String phone) {
        if ((!(sMake.equalsIgnoreCase(brandName)))
                || (!(mModel.contains((CharSequence) (phone))))) {
            return false;
        }
        return true;
    }

    public static boolean checkIsAppointPhone(Object object, Object object2) {
        if (object != null)
            return object.equals(object2);
        if (object2 != null)
            return false;
        return true;
    }

    public static String getSystemProperty(String string) {
        try {
            String string2 = (String) (Class.forName((String) ("android.os.SystemProperties"))
                    .getMethod("get", new Class[] {
                            String.class
                    }).invoke((Object) (null), new Object[] {
                    string
            }));
            return string2;
        } catch (Exception var1_2) {
            return null;
        }
    }

    public static boolean isPackageInfo(Context context, String string) {
        if (checkPackgeInfo(context, string) == null)
            return false;
        return true;
    }

    public static PackageInfo checkPackgeInfo(Context context, String string) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(string, 0);
            return packageInfo;
        } catch (Exception exception) {
            return null;
        }
    }

    // HUAWEI

    public static boolean checkIsHuaWeiPhone() {
        if ((!(checkIsAppointPhone((Object) (getSystemProperty("ro.build.version.emui")),
                (Object) ("EmotionUI_2.3")))) && (!(Build.DISPLAY.startsWith("EMUI2.3"))))
            return false;
        return true;
    }

    public static boolean isHuaWeiTipPhone(Context context) {
        return isPackageInfo(context, "com.huawei.systemmanager");
    }

    public static void startHuaWeiSysManageIntent(Context context) {
        try {
            LockManager.getInstatnce().timeFilterSelf();
            Intent intent = new Intent();
            String string = (checkIsHuaWeiPhone()) ? ("com.huawei.systemmanager.SystemManagerMainActivity")
                    : ("com.huawei.notificationmanager.ui.NotificationManagmentActivity");
            intent.setClassName("com.huawei.systemmanager", string);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                LockManager.getInstatnce().addFilterLockPackage("com.huawei.systemmanager", false);
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                SDKWrapper.addEvent(context, SDKWrapper.P1, "qs_open_error", "reason_"
                        + BuildProperties.getPoneModel());
            }
            return;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    public static void isToHuaWeiSystemManager(Context context) {
        startHuaWeiSysManageIntent(context);
    }

    public static String getPoneModel() {
        return android.os.Build.MODEL;
    }

    public static boolean isOppoOs() {
        if (TextUtils.isEmpty((CharSequence) (getSystemProperty("ro.build.version.opporom"))))
            return false;
        return true;
    }

    // back:{true,false}(ture:sucessful,false:failure)
    public static boolean startOppoManageIntent(Context context) {
        LockManager.getInstatnce().timeFilterSelf();
        Intent intent = new Intent();
        intent.setClassName("com.oppo.safe",
                "com.oppo.safe.permission.PermissionTopActivity");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }

}
