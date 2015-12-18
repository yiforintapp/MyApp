package com.leo.appmaster.mgr;

import java.util.HashMap;

import android.text.TextUtils;

import com.leo.appmaster.mgr.impl.CallFilterContextManagerImpl;
import com.leo.appmaster.mgr.impl.DeviceManagerImpl;
import com.leo.appmaster.mgr.impl.IntrudeSecurityManagerImpl;
import com.leo.appmaster.mgr.impl.LockManagerImpl;
import com.leo.appmaster.mgr.impl.LostSecurityManagerImpl;
import com.leo.appmaster.mgr.impl.PrivacyContactManagerImpl;
import com.leo.appmaster.mgr.impl.PrivacyDataManagerImpl;
import com.leo.appmaster.mgr.impl.ThridAppManagerImpl;
import com.leo.appmaster.mgr.impl.WifiSecurityManagerImpl;


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

    private static HashMap<String, Manager> sManagerMap = new HashMap<String, Manager>();

    static {

    }

    /**
     * 获取manager
     *
     * @param mgr
     * @return
     */
    public static Manager getManager(String mgr) {
        Manager manager = sManagerMap.get(mgr);
        if (manager != null) return manager;

        if (MGR_APPLOCKER.equals(mgr)) {
            manager = new LockManagerImpl();
        } else if (MGR_PRIVACY_DATA.equals(mgr)) {
            manager = new PrivacyDataManagerImpl();
        } else if (MGR_PRIVACY_CONTACT.equals(mgr)) {
            manager = new PrivacyContactManagerImpl();
        } else if (MGR_DEVICE.equals(mgr)) {
            manager = new DeviceManagerImpl();
        } else if (MGR_THIRD_APP.equals(mgr)) {
            manager = new ThridAppManagerImpl();
        } else if (MGR_INTRUDE_SECURITY.equals(mgr)) {
            manager = new IntrudeSecurityManagerImpl();
        } else if (MGR_WIFI_SECURITY.equals(mgr)) {
            manager = new WifiSecurityManagerImpl();
        } else if (MGR_LOST_SECURITY.equals(mgr)) {
            manager = new LostSecurityManagerImpl();
        } else if(MGR_CALL_FILTER.equals(mgr)){
            manager = new CallFilterContextManagerImpl();
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
    public static void addManager(String key, Manager mgr) {
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
        Manager manager = null;
        synchronized (sManagerMap) {
            sManagerMap.remove(key);
        }
        if (manager == null) return;

        manager.onDestory();
    }
}
