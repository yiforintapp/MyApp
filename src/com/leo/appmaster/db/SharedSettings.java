package com.leo.appmaster.db;

import android.content.Context;
import android.content.SharedPreferences;

import com.leo.appmaster.ThreadManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Created by Jasper on 2016/3/26.
 */
public class SharedSettings extends ISettings {
    private SharedPreferences preferences;
    private Executor mSerialExecutor;

    private Map<String, Object> mValues;

    SharedSettings() {
        mSerialExecutor = ThreadManager.newSerialExecutor();
        preferences = mContext.getSharedPreferences("shared_settings", Context.MODE_PRIVATE);

        mValues = new HashMap<String, Object>();
        mValues.putAll(preferences.getAll());
    }

    @Override
    public void set(String key, String value) {
        mValues.put(key, value);
        commitAsync(preferences.edit().putString(key, value));
    }

    @Override
    public String get(String key, String def) {
        Object object = mValues.get(key);
        if (object != null) {
            return String.valueOf(object);
        }
        return preferences.getString(key, def);
    }

    @Override
    public void setBundleMap(Map<String, Object> map, OnBundleSavedListener listener) {
        if (map == null || map.size() == 0) {
            return;
        }

        SharedPreferences.Editor editor = null;
        for (String key : map.keySet()) {
            Object object = map.get(key);
            if (object != null) {
                if (editor == null) {
                    editor = preferences.edit();
                }
                String value = String.valueOf(object);
                mValues.put(key, value);
                editor.putString(key, value);
            }
        }
        if (editor != null) {
            commitAsync(editor);
        }
        if (listener != null) {
            listener.onBundleSaved();
        }
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
