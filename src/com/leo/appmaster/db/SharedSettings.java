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
    public void setBoolean(String key, boolean value) {
        commitAsync(key, String.valueOf(value));
    }

    @Override
    public void setInteger(String key, int value) {
        commitAsync(key, String.valueOf(value));
    }

    @Override
    public void setDouble(String key, double value) {
        commitAsync(key, String.valueOf(value));
    }

    @Override
    public void setFloat(String key, float value) {
        commitAsync(key, String.valueOf(value));
    }

    @Override
    public void setLong(String key, long value) {
        commitAsync(key, String.valueOf(value));
    }

    @Override
    public void setString(String key, String value) {
        commitAsync(key, value);
    }

    @Override
    public boolean getBoolean(String key, boolean def) {
        try {
            String v = (String)mValues.get(key);
            return v != null ? Boolean.parseBoolean(v) : def;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return def;
    }

    @Override
    public int getInteger(String key, int def) {
        try {
            String v = (String)mValues.get(key);
            return v != null ? Integer.parseInt(v) : def;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return def;
    }

    @Override
    public long getLong(String key, long def) {
        try {
            String v = (String)mValues.get(key);
            return v != null ? Long.parseLong(v) : def;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return def;
    }

    @Override
    public float getFloat(String key, float def) {
        try {
            String v = (String)mValues.get(key);
            return v != null ? Float.parseFloat(v) : def;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return def;
    }

    @Override
    public double getDouble(String key, double def) {
        try {
            String v = (String)mValues.get(key);
            return v != null ? Double.parseDouble(v) : def;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return def;
    }

    @Override
    public String getString(String key, String def) {
        String v = (String)mValues.get(key);
        return v != null ? v : def;
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
                if (mValues.containsKey(key)) {
                    continue;
                }
                String value = String.valueOf(object);
                mValues.put(key, value);
                editor.putString(key, value);
            }
        }
        if (editor != null) {
            final SharedPreferences.Editor finalEditor = editor;
            mSerialExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    finalEditor.commit();
                }
            });
        }
        if (listener != null) {
            listener.onBundleSaved();
        }
    }

    public void commitAsync(final String key, final String value) {
        if (key == null || value == null) {
            return;
        }
        mValues.put(key, value);
        mSerialExecutor.execute(new Runnable() {
            @Override
            public void run() {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(key, value);
                editor.commit();
            }
        });
    }
}
