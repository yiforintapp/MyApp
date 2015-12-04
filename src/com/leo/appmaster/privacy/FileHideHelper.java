
package com.leo.appmaster.privacy;

import java.io.File;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.MediaColumns;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.Constants;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.utils.FileOperationUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.imageloader.utils.IoUtils;

public class FileHideHelper {

    public final static String[] STORE_HIDEIMAGES = new String[]{
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns._ID, //
    };

    public static int getAllPhotoCount(Context context) {
        Cursor cursor = null;
        try {
            cursor = MediaStore.Images.Media.query(
                    context.getContentResolver(),
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, FileOperationUtil.STORE_IMAGES,
                    null, MediaColumns.DATE_MODIFIED + " desc");

            return cursor.getCount();
        } catch (Exception e) {

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return 0;
    }

    public static int getAllVideoCount(Context context) {
        Uri uri = Files.getContentUri("external");
        String selection = Constants.VIDEO_FORMAT;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, null, selection, null, null);
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
     * Get the count of hidden pictures
     */
//    public static int getHidePhotoCount(Context context) {
//        Uri uri = Files.getContentUri("external");
//        String selection = null;
//        Cursor cursor = null;
//        Cursor cur = null;
//        try {
//            selection = MediaColumns.DATA
//                    + " LIKE '%.leotmi'";
//            cursor = context.getContentResolver().query(uri, STORE_HIDEIMAGES, selection, null,
//                    MediaColumns.DATE_ADDED + " desc");
//            int newHideCount = cursor.getCount();
//            cursor.close();
//
//            selection = MediaColumns.DATA + " LIKE '%.leotmp'";
//            cursor = context.getContentResolver().query(uri, STORE_HIDEIMAGES, selection, null,
//                    MediaColumns.DATE_ADDED + " desc");
//            cur = context.getContentResolver().query(Constants.IMAGE_HIDE_URI, new String[] {
//                    "image_path"
//            }, null, null, null);
//            int unhideDbCount = cur.getCount();
//            int oldHideCount = cursor.getCount();
//            if (oldHideCount > 0) {
//                replaceOldHideImages(context);
//            }
//            return newHideCount + oldHideCount - unhideDbCount;
//        } catch (Exception e) {
//
//        } finally {
//            if (cursor != null) {
//                cursor.close();
//            }
//            if (cur != null) {
//                cur.close();
//            }
//        }
//
//        return 0;
//    }
//    private static void replaceOldHideImages(final Context context) {
//        Uri uri = Files.getContentUri("external");
//        String selection = MediaColumns.DATA + " LIKE '%.leotmp'";
//        final Cursor cursor = context.getContentResolver().query(uri, STORE_HIDEIMAGES, selection,
//                null,
//                MediaColumns.DATE_ADDED + " desc");
//        if (cursor == null)
//            return;
//
//        if (cursor.getCount() == 0) {
//            cursor.close();
//            return;
//        }
//        final String[] paths = FileOperationUtil.getSdCardPaths(context);
//        new ThreadManager();
//        ThreadManager.executeOnAsyncThread(new Runnable() {
//            @Override
//            public void run() {
//                while (cursor.moveToNext()) {
//                    String path = cursor.getString(1);
//                    String newPath = null;
//                    String dirPath = FileOperationUtil.getDirPathFromFilepath(path);
//                    String fileName = FileOperationUtil.getNameFromFilepath(path);
//                    if (fileName.endsWith(".leotmp")) {
//                        if (fileName.startsWith(".")) {
//                            fileName = fileName.substring(1);
//                        }
//                        int position = 0;
//                        if (path.startsWith(paths[0])) {
//                            position = 0;
//                        } else if (path.startsWith(paths[1])) {
//                            position = 1;
//                        } else {
//                            continue;
//                        }
//
//                        String sdcarPath = paths[position];
//                        newPath = dirPath + File.separator + fileName.replace(".leotmp", ".leotmi");
//                        newPath = sdcarPath + File.separator + FileOperationUtil.SDCARD_DIR_NAME
//                                + newPath.replace(sdcarPath, "");
//
//                        File file = new File(path);
//                        try {
//                            if (file.isFile()) {
//                                String newFileDir = newPath.substring(0,
//                                        newPath.lastIndexOf(File.separator));
//                                File temp = new File(newFileDir);
//                                LeoLog.d("RenameFile", "fileDir = " + newFileDir);
//                                if (temp.exists()) {
//                                    LeoLog.d("RenameFile", temp + "    exists");
//                                } else {
//                                    LeoLog.d("RenameFile", temp + "  not   exists");
//                                    boolean mkRet = temp.mkdirs();
//                                    if (mkRet) {
//                                        LeoLog.w("RenameFile", "make dir " + temp
//                                                + "  successfully");
//                                    } else {
//                                        LeoLog.d("RenameFile", "make dir " + temp
//                                                + "  unsuccessfully");
//                                        newPath = newPath.replace(FileOperationUtil.SDCARD_DIR_NAME
//                                                + File.separator, "");
//                                    }
//                                }
//                                boolean ret = file.renameTo(new File(newPath));
//                                if (ret) {
//                                    FileOperationUtil.saveFileMediaEntry(newPath,
//                                            context);
//                                    FileOperationUtil.deleteImageMediaEntry(
//                                            path, context);
//                                } else {
//                                    LeoLog.w("replaceOldHideImages", "file not exists: " + path);
//                                }
//                            } else {
//                                continue;
//                            }
//                        } catch (Exception e) {
//
//                        } finally {
//                            IoUtils.closeSilently(cursor);
//                        }
//                    }
//                }
//            }
//        });
//
//    }

    /**
     * Get the count of hidden videos
     */
//    public static int getHideVideoCount(Context context) {
//        Uri uri = Files.getContentUri("external");
//        String selection = MediaColumns.DATA + " LIKE '%.leotmv'";
//        Cursor cursor = null;
//        try {
//            cursor = context.getContentResolver().query(uri, null, selection, null, null);
//            return cursor.getCount();
//        } catch (Exception e) {
//        } finally {
//            if (cursor != null) {
//                cursor.close();
//            }
//        }
//
//        return 0;
//    }

}
