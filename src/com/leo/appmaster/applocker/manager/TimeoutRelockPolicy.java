
package com.leo.appmaster.applocker.manager;

import java.util.HashMap;

import android.content.Context;

import com.leo.appmaster.AppMasterPreference;

public class TimeoutRelockPolicy implements ILockPolicy {

    Context mContext;

    private HashMap<String, UnlockTimeHolder> mLockapp = new HashMap<String, UnlockTimeHolder>();

    public TimeoutRelockPolicy(Context mContext) {
        super();
        this.mContext = mContext.getApplicationContext();

    }

    public int getRelockTime() {
        return AppMasterPreference.getInstance(mContext).getRelockTimeout();
    }

    @Override
    public boolean onHandleLock(String pkg) {
        long curTime = System.nanoTime() / 1000000;
        if (mLockapp.containsKey(pkg)) {
            long lastLockTime = mLockapp.get(pkg).lastUnlockTime;
            if ((curTime - lastLockTime) < getRelockTime())
                return true;
        }
        // else {
        // UnlockTimeHolder holder = new UnlockTimeHolder();
        // holder.lastUnlockTime = curTime;
        // holder.secondUnlockTime = 0;
        // holder.firstUnlockTime = 0;
        // mLockapp.put(pkg, holder);
        // }
        return false;
    }

    public boolean inRelockTime(String pkg) {
        long curTime = System.nanoTime() / 1000000;
        if (mLockapp.containsKey(pkg)) {
            long lastLockTime = mLockapp.get(pkg).lastUnlockTime;
            if ((curTime - lastLockTime) < getRelockTime())
                return true;
        }
        return false;
    }

    public void clearLockApp() {
        mLockapp.clear();
    }

    @Override
    public void onUnlocked(String pkg) {
        long curTime = System.nanoTime() / 1000000;
        UnlockTimeHolder holder = mLockapp.get(pkg);
        if (holder == null) {
            holder = new UnlockTimeHolder();
            holder.lastUnlockTime = curTime;
            holder.secondUnlockTime = 0;
            mLockapp.put(pkg, holder);
        } else {
            holder.secondUnlockTime = holder.lastUnlockTime;
            holder.lastUnlockTime = curTime;
        }

    }

    private static class UnlockTimeHolder {
        long secondUnlockTime;
        long lastUnlockTime;
    }

}
