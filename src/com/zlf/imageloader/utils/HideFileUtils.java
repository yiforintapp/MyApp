
package com.zlf.imageloader.utils;

import android.provider.MediaStore;

public class HideFileUtils {

    public final static String[] STORE_HIDEIMAGES = new String[] {
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns._ID, //
    };

    /**
     * Get the count of hidden pictures
     */
//    public static int getHidePhotoCount(Context context) {
//        Uri uri = Files.getContentUri("external");
//        String selection = null;
//        Cursor cursor = null;
//        try {
//            selection = MediaColumns.DATA
//                    + " LIKE '%.leotmi'";
//            cursor = context.getContentResolver().query(uri, STORE_HIDEIMAGES, selection, null,
//                    MediaColumns.DATE_ADDED + " desc");
//            int newHideCount = cursor.getCount();
//
//            selection = MediaColumns.DATA + " LIKE '%.leotmp'";
//            cursor = context.getContentResolver().query(uri, STORE_HIDEIMAGES, selection, null,
//                    MediaColumns.DATE_ADDED + " desc");
//            Cursor cur = context.getContentResolver().query(Constants.IMAGE_HIDE_URI, new String[]{"image_path"}, null, null, null);
//            int unhideDbCount=cur.getCount();
//            int oldHideCount = cursor.getCount();
//            if (oldHideCount > 0) {
//                replaceOldHideImages(context);
//            }
//            return newHideCount + oldHideCount-unhideDbCount;
//        } catch (Exception e) {
//
//        } finally {
//            if (cursor != null) {
//                cursor.close();
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
//        if (cursor == null) {
//            return;
//        }
//
//        if(cursor.getCount() == 0) {
//            cursor.close();
//            return;
//        }
//
//        final String[] paths = FileOperationUtil.getSdCardPaths(context);
//
//        new Thread(new Runnable() {
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
//                                        LeoLog.e("RenameFile", "make dir " + temp
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
//                                    LeoLog.e("replaceOldHideImages", "file not exists: " + path);
//                                }
//                            } else {
//                                continue;
//                            }
//                        } catch (Exception e) {
//
//                        }
//                    }
//                }
//                cursor.close();
//            }
//        }).start();
//
//    }

    /**
     * Get the count of hidden videos
     */
//    public static int getVideoInfo(Context context) {
//        Uri uri = Files.getContentUri("external");
//        String selection = MediaColumns.DATA + " LIKE '%.leotmv'";
//        Cursor cursor = null;
//        try {
//            cursor = context.getContentResolver().query(uri, null, selection, null,
//                    MediaColumns.DATE_MODIFIED + " desc");
//
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
