package com.zlf.appmaster.mgr;

import android.text.TextUtils;

import java.util.HashMap;


/**
 * 服务上下文
 * Created by Jasper on 2015/9/28.
 */
public class MgrContext {

    /**
     * 应用锁
     */
    public static final String MGR_APPLOCKER = "mgr_applocker";

    /**
     * WIFI安全
     */
    public static final String MGR_WIFI_SECURITY = "mgr_wifi_security";

    /**
     * 手机防盗
     */
    public static final String MGR_LOST_SECURITY = "mgr_lost_security";

    /**
     * 入侵者防护
     */
    public static final String MGR_INTRUDE_SECURITY = "mgr_intrude_security";

    /**
     * 应用管理
     */
    public static final String MGR_THIRD_APP = "mgr_third_app";

    /**
     * 设备管理
     */
    public static final String MGR_DEVICE = "mgr_device";

    /**
     * 隐私联系人, 隐私通话、隐私短信
     */
    public static final String MGR_PRIVACY_CONTACT = "mgr_privacy_contact";

    /**
     * 隐私数据, 图片、视频、账号
     */
    public static final String MGR_PRIVACY_DATA = "mgr_privacy_data";

    public static final String MGR_CALL_FILTER = "mgr_call_filter";

    /**
     * 电池管理
     * */
    public static final String MGR_BATTERY = "mgr_battery";

    private static HashMap<String, com.zlf.appmaster.mgr.Manager> sManagerMap = new HashMap<String, com.zlf.appmaster.mgr.Manager>();

    static {

    }

    /**
     * 获取manager
     *
     * @param mgr
     * @return
     */
    public static com.zlf.appmaster.mgr.Manager getManager(String mgr) {
        com.zlf.appmaster.mgr.Manager manager = sManagerMap.get(mgr);
        if (manager != null) return manager;

        if (MGR_DEVICE.equals(mgr)) {
//            manager = new DeviceManagerImpl();
        }

        if (manager != null) {
            addManager(mgr, manager);
        }
        return manager;
    }

    /**
     * 添加manager
     *
     * @param key
     * @param mgr
     */
    public static void addManager(String key, com.zlf.appmaster.mgr.Manager mgr) {
        if (TextUtils.isEmpty(key) || mgr == null) return;

        synchronized (sManagerMap) {
            sManagerMap.put(key, mgr);
        }
    }

    /**
     * 移除manager
     *
     * @param key
     */
    public static void removeManager(String key) {
        com.zlf.appmaster.mgr.Manager manager = null;
        synchronized (sManagerMap) {
            sManagerMap.remove(key);
        }
        if (manager == null) return;

        manager.onDestory();
    }
}
