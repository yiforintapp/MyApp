package com.leo.appmaster.utils;

import java.lang.reflect.Field;

import android.app.Notification;

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
