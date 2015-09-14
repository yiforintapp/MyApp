
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
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;

import com.leo.appmaster.Constants;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.applocker.receiver.DeviceReceiver;
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
    private static final String KEY_LENOVO_VERSION_ROM_NAME = "ro.lenovo.lvp.version";
    public static final String I_STYLE_MODEL = "i-mobile I-STYLE 217";// 解锁等待界面动画执行过快机型
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
        Method method = null;
        try {
            method = cls.getDeclaredMethod(methodName, (Class[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    // 是否为MIUI系统
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

    // 是否为MIUIV5系统
    public static boolean isMiuiV5() {
        String string = getSystemProperty("ro.miui.ui.version.name");
        if ((string == null) || (!(string.equalsIgnoreCase("V5"))))
            return false;
        return true;
    }

    // 是否为MIUIV6系统
    public static boolean isMiuiV6() {
        String string = getSystemProperty("ro.miui.ui.version.name");
        if ((string == null) || (!(string.equalsIgnoreCase("V6"))))
            return false;
        return true;
    }

    /**
     * 本机制造商名称识别
     * 
     * @param brandName品牌名称
     * @return
     */
    public static boolean checkPhoneBrand(String brandName) {
        return Build.MANUFACTURER.toLowerCase().equalsIgnoreCase(brandName);
    }

    // 检查本机是否为指定品牌
    public static boolean checkPhoneBrandForSelf(String brandName) {
        // Log.e(Constants.RUN_TAG, "手机品牌："+Build.BRAND.toLowerCase());
        return Build.BRAND.toLowerCase().equalsIgnoreCase(brandName);
    }

    /**
     * 详细机型识别
     * 
     * @param brandName(品牌：那个厂家的手机)
     * @param phone(机型)
     * @return
     */
    public static boolean detailMdelDistinguish(String brandName, String phone) {
        if ((!(Build.MANUFACTURER.toLowerCase().equalsIgnoreCase(brandName)))
                || (!(Build.MODEL.toLowerCase().contains((CharSequence) (phone))))) {
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
        String systemProperty = getSystemProperty("ro.build.version.emui");
        String emotion23 = "EmotionUI_2.3";

        boolean isEmotion23 = checkIsAppointPhone(systemProperty, emotion23);
        if (!isEmotion23 && !(Build.DISPLAY.startsWith("EMUI2.3"))) {
            return false;
        }
        // if ((!(checkIsAppointPhone((Object)
        // (getSystemProperty("ro.build.version.emui")),
        // (Object) ("EmotionUI_2.3")))) &&
        // (!(Build.DISPLAY.startsWith("EMUI2.3"))))
        // return false;
        return true;
    }

    /**
     * 是否是华为EmotionUI_3.0以上系统
     * 
     * @return
     */
    public static boolean checkIsHuaWeiEmotion31() {
        String systemProperty = getSystemProperty("ro.build.version.emui");
        String emotion3 = "EmotionUI_3.";
        if (systemProperty != null && systemProperty.startsWith(emotion3)) {
            return true;
        }

        return false;
    }

    public static boolean isHuaWeiTipPhone(Context context) {
        return isPackageInfo(context, "com.huawei.systemmanager");
    }

    public static void startHuaWeiSysManageIntent(Context context) {
        try {
            LockManager.getInstatnce().timeFilterSelf();
            Intent intent = new Intent();
            String className = null;
            // Log.e(Constants.RUN_TAG,"checkIsHuaWeiEmotion31()="+checkIsHuaWeiEmotion31());
            // Log.e(Constants.RUN_TAG,"checkIsHuaWeiPhone()="+checkIsHuaWeiPhone());
            if (checkIsHuaWeiEmotion31()) {
                className = "com.huawei.systemmanager.addviewmonitor.AddViewMonitorActivity";
                // Log.e(Constants.RUN_TAG, "华为P8悬浮窗跳转方式1");
            } else if (checkIsHuaWeiPhone()) {
                className = "com.huawei.systemmanager.SystemManagerMainActivity";
                // Log.e(Constants.RUN_TAG, "华为悬浮窗跳转方式2");
            } else {
                className = "com.huawei.notificationmanager.ui.NotificationManagmentActivity";
                // Log.e(Constants.RUN_TAG, "华为悬浮窗跳转方式3");
            }
            intent.setClassName("com.huawei.systemmanager", className);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                LockManager.getInstatnce().addFilterLockPackage("com.huawei.systemmanager", false);
                context.startActivity(intent);
                // Log.e(Constants.RUN_TAG, "华为P6,P8进入权限管理界面");
            } catch (Exception e) {
                SDKWrapper.addEvent(context, SDKWrapper.P1, "qs_open_error", "reason_"
                        + BuildProperties.getPoneModel());
                className = "com.huawei.notificationmanager.ui.NotificationManagmentActivity";
                intent.setClassName("com.huawei.systemmanager", className);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    context.startActivity(intent);
                    // Log.e(Constants.RUN_TAG, "华为P7进入权限管理界面");
                } catch (Exception e1) {
                }
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

    public static boolean isLava504Q() {
        return TextUtils.equals("504Q+", android.os.Build.MODEL);
    }

    public static boolean isGTS5282() {
        return TextUtils.equals("GT-S5282", android.os.Build.MODEL);
    }

    public static boolean isYiJia() {
        // 说明：一加手机在4.4系统以下，无需手动开启悬浮窗
        if (TextUtils.isEmpty((CharSequence) (getSystemProperty("ro.build.version.opporom")))
                || Build.VERSION.SDK_INT < 19)
            return false;
        return true;
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

    // 判断本机是否为指定机型
    public static boolean checkPhoneModel(String phoneModel) {
        // Log.e(Constants.RUN_TAG,
        // "传入机型："+phoneModel.toLowerCase()+",本机："+getPoneModel().toLowerCase());
        return getPoneModel().toLowerCase().equalsIgnoreCase(phoneModel.toLowerCase());
    }

    /**
     * 输入指定分辨率判断本机是否为指定分辨率
     * 
     * @param height 屏幕高
     * @param width 屏幕宽
     * @return
     */
    public static boolean phoneDensity(Context context, int height, int width) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int windowHeight = wm.getDefaultDisplay().getHeight();
        int windowWidth = wm.getDefaultDisplay().getWidth();
        if (height > 0 && width > 0) {
            if ((windowHeight == height) && (windowWidth == width)) {
                return true;
            }
        }
        return false;
    }

    /* 判断是否为联想的机型 */
    public static boolean isLenoveModel() {
        if (TextUtils.isEmpty((CharSequence) (getSystemProperty(KEY_LENOVO_VERSION_ROM_NAME))))
            return false;
        return true;
    }
}
