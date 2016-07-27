package com.zlf.appmaster.mgr;

import android.content.Context;

import com.zlf.appmaster.AppMasterApplication;
import com.zlf.appmaster.utils.LeoLog;

import java.util.ArrayList;
import java.util.List;

/**
 * 模块管理基类
 * Created by Jasper on 2015/9/28.
 */
public abstract class Manager {
    private static final byte[] LOCK = new byte[1];

    /**
     * 安全等级发生变化
     */
    public interface SecurityChangeListener {
        /**
         * @param description 接口描述
         * @param securityScore 当前模块的安全等级得分
         */
        public void onSecurityChange(String description, int securityScore);
    }

    protected Context mContext;
    protected List<SecurityChangeListener> mListeners = new ArrayList<SecurityChangeListener>();

    Manager() {
        mContext = AppMasterApplication.getInstance();
        mListeners = new ArrayList<SecurityChangeListener>();
    }

    public abstract void onDestory();

    public abstract String description();

    /**
     * 获取当前模块的最大上限值
     * @return
     */
    public int getMaxScore() {
        return 0;
    }

    /**
     * 获取当前模块的安全得分
     *
     * @return
     */
    public int getSecurityScore() {
        return 0;
    }

    /**
     * 忽略所有隐私
     *
     * @return 当前得分
     */
    public int ignore() {
        return 0;
    }

    public void registerSecurityListener(SecurityChangeListener listener) {
        synchronized (LOCK) {
            if (!mListeners.contains(listener)) {
                mListeners.add(listener);
            }
        }
    }

    public void unregisterSecurityListener(SecurityChangeListener listener) {
        synchronized (LOCK) {
            if (mListeners.contains(listener)) {
                mListeners.remove(listener);
            }
        }
    }

    public void notifySecurityChange() {
        LeoLog.d("monitorMedia", "notifySecurityChange!!!");
        for (SecurityChangeListener listener : mListeners) {
//            int score = getSecurityScore();
            listener.onSecurityChange(description(), 0);
        }
    }
}
