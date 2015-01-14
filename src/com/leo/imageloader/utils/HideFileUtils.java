
package com.leo.imageloader.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.MediaColumns;

public class HideFileUtils {

    public final static String[] STORE_HIDEIMAGES = new String[] {
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns._ID, //
    };

    /**
     * Get the count of hidden pictures
     */
    public static int getHidePhotoCount(Context context) {
        Uri uri = Files.getContentUri("external");
        String selection = MediaColumns.DATA + " LIKE '%.leotmp'" + " or " + MediaColumns.DATA
                + " LIKE '%.leotmi'";
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, STORE_HIDEIMAGES, selection, null,
                    MediaColumns.DATE_ADDED + " desc");
            return cursor.getCount();
        } catch (Exception e) {

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return 0;
    }

    /**
     * Get the count of hidden videos
     */
    public static int getVideoInfo(Context context) {
        Uri uri = Files.getContentUri("external");
        String selection = MediaColumns.DATA + " LIKE '%.leotmv'";
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, null, selection, null,
                    MediaColumns.DATE_MODIFIED + " desc");

            return cursor.getCount();
        } catch (Exception e) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return 0;
    }

}
