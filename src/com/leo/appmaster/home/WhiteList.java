
package com.leo.appmaster.home;

import java.util.HashMap;
import java.util.Map;

import com.leo.appmaster.AppMasterApplication;

import android.content.Context;
/**
 * 白名单
 * @author run
 *
 */
public abstract class WhiteList {
    public static Map<Integer, WhiteList> mWhiteList;
    public int mListId;
    protected int[] mLists;
    protected Context mContext;

    protected abstract boolean doHandler();

    public WhiteList() {
        mContext=AppMasterApplication.getInstance();
        mWhiteList = new HashMap<Integer, WhiteList>();
    }

    protected WhiteList createWhiteListHandler(int flag) {
        return null;
    }

    protected WhiteList getWhiteList(Integer flag) {
        WhiteList wl = null;
        if (!isEmptyWhiteList(mWhiteList)) {
            wl = mWhiteList.get(flag);
            if (wl != null)
                return wl;
        }
        wl = createWhiteListHandler(flag);
        wl.mListId = flag;
        mWhiteList.put(flag, wl);
        return wl;
    }

    private boolean isEmptyWhiteList(Map<Integer, WhiteList> list) {
        return list.isEmpty();
    }

    public void executeGuide() {
        doHandler();
    }
}
