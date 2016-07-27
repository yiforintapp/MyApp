package com.zlf.appmaster.utils;

import android.app.Notification;

import java.lang.reflect.Field;

public class NotificationUtil {

    public static void setBigIcon(Notification notify, int icon) {
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$id");
            Field field = clazz.getField("icon");
            field.setAccessible(true);
            int id_icon = field.getInt(null);
            if (id_icon > 0 && notify.contentView != null) {
                notify.contentView.setImageViewResource(id_icon, icon);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
