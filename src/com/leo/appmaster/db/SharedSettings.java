package com.leo.appmaster.db;

import android.content.Context;
import android.content.SharedPreferences;

import com.leo.appmaster.ThreadManager;

import java.util.concurrent.Executor;

/**
 * Created by Jasper on 2016/3/26.
 */
public class SharedSettings extends ISettings {
    private SharedPreferences preferences;
    private Executor mSerialExecutor;

    SharedSettings() {
        mSerialExecutor = ThreadManager.newSerialExecutor();
        preferences = mContext.getSharedPreferences("shared_settings", Context.MODE_PRIVATE);
    }

    @Override
    public void set(String key, String value) {
        commitAsync(preferences.edit().putString(key, value));
    }

    @Override
    public String get(String key, String def) {
        return preferences.getString(key, def);
    }

    public void commitAsync(final SharedPreferences.Editor editor) {
        if (editor == null)
            return;

        mSerialExecutor.execute(new Runnable() {
            @Override
            public void run() {
                editor.commit();
            }
        });
    }
}
