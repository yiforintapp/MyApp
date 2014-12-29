
package com.leo.appmaster.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.leo.appmaster.Constants;
import com.leo.appmaster.imagehide.PhotoAibum;
import com.leo.appmaster.imagehide.PhotoItem;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.MediaColumns;

public class FileOperationUtil {

    static final String[] STORE_IMAGES = {
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media._ID, //
            MediaStore.Images.Media.BUCKET_ID, // dir id
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
    // dir name
    };

    public static Comparator<PhotoAibum> mFolderCamparator = new Comparator<PhotoAibum>() {

        public final int compare(PhotoAibum a, PhotoAibum b) {
            if (a.getLastmodified().before(b.getLastmodified()))
                return 1;
            if (a.getLastmodified().after(b.getLastmodified()))
                return -1;
            return 0;
        }
    };

    public static String getNameFromFilepath(String filepath) {
        String filename;
        int pos = filepath.lastIndexOf('/');
        if (pos != -1) {
            filename = filepath.substring(pos + 1);
            return filename;
        }
        return "";
    }

    public static String getNoExtNameFromHideFilepath(String filepath) {
        String filename;
        int pos = filepath.lastIndexOf('/');
        if (pos > -1) {
            filename = filepath.substring(pos + 1);
            if (filename.startsWith(".")) {
                filename = filename.substring(1);
                int index = filename.indexOf(".");
                if (index > 0) {
                    filename = filename.substring(0, index);
                }
            } else {
                int index = filename.indexOf(".");
                if (index > 0) {
                    filename = filename.substring(0, index);
                }
            }
            return filename;
        }
        return "";
    }

    public static String getDirPathFromFilepath(String filepath) {
        if(filepath != null) {
            int pos = filepath.lastIndexOf('/');
            if (pos != -1) {
                return filepath.substring(0, pos);
            }
        }
        return "";
    }

    public static String makePath(String path1, String path2) {
        if (path1.endsWith(File.separator))
            return path1 + path2;

        return path1 + File.separator + path2;
    }

    public static String getSdDirectory() {
        return Environment.getExternalStorageDirectory().getPath();
    }

    public static String getDirNameFromFilepath(String path) {
        String dirName;
        int pos = path.lastIndexOf('/');
        if (pos != -1) {
            dirName = path.substring(0, pos);
            pos = dirName.lastIndexOf('/');
            dirName = dirName.substring(pos + 1);
            return dirName;
        }
        return "";
    }

    /**
     * rename a file
     * 
     * @param filePath
     * @param newName
     * @return
     */
    public static boolean RenameFile(String filePath, String newName) {
        if (filePath == null || newName == null) {
            LeoLog.e("RenameFile", "Rename: null parameter");
            return false;
        }

        File file = new File(filePath);
        String newPath = FileOperationUtil.makePath(
                FileOperationUtil.getDirPathFromFilepath(filePath), newName);
        LeoLog.e("RenameFile", "newPath=" + newPath);
        try {
            if (file.isFile()) {
                boolean ret = file.renameTo(new File(newPath));
                LeoLog.e("RenameFile", ret + " to rename file");
                return ret;
            } else {
                return false;
            }
        } catch (SecurityException e) {
            LeoLog.e("RenameFile", "Fail to rename file," + e.toString());
        }
        return false;
    }

    /**
     * @param filePath
     * @return
     */
    public static boolean DeleteFile(String filePath) {
        if (filePath == null) {
            LeoLog.e("DeleteFile", "Rename: null parameter");
            return false;
        }

        File file = new File(filePath);
        try {
            if (file.isFile()) {
                boolean ret = file.delete();
                LeoLog.e("DeleteFile", ret + " to rename file");
                return ret;
            } else {
                return false;
            }
        } catch (SecurityException e) {
            LeoLog.e("DeleteFile", "Fail to rename file," + e.toString());
        }
        return false;
    }

    public static void deleteFileMediaEntry(String imagePath, Context context) {
        String params[] = new String[] {
            imagePath
        };
        Uri uri = Files.getContentUri("external");
        context.getContentResolver().delete(uri, MediaColumns.DATA + " LIKE ?", params);
    }

    public static Uri saveFileMediaEntry(String imagePath, Context context) {
        ContentValues v = new ContentValues();
        File f = new File(imagePath);
        v.put(MediaColumns.TITLE, f.getName());
        v.put(MediaColumns.DISPLAY_NAME, f.getName());
        v.put(MediaColumns.SIZE, f.length());
        f = null;

        v.put(MediaColumns.DATA, imagePath);
        ContentResolver c = context.getContentResolver();
        Uri uri = Files.getContentUri("external");
        return c.insert(uri, v);
    }

    public static void deleteImageMediaEntry(String imagePath, Context context) {
        String params[] = new String[] {
            imagePath
        };
        context.getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Images.Media.DATA + " LIKE ?", params);
    }
    public static void deleteVideoMediaEntry(String videoPath, Context context) {
        String params[] = new String[] {
                videoPath
        };
        Uri uri = Files.getContentUri("external");
        String selection =Constants.VIDEO_FORMAT;
        context.getContentResolver().delete(uri, MediaStore.Files.FileColumns.DATA+" LIKE ?",params);
    }

    public static Uri saveImageMediaEntry(String imagePath, Context context) {
        ContentValues v = new ContentValues();
        v.put(Images.Media.MIME_TYPE, "image/jpeg");

        File f = new File(imagePath);
        File parent = f.getParentFile();
        String path = parent.toString().toLowerCase();
        String name = parent.getName().toLowerCase();
        v.put(Images.Media.TITLE, f.getName());
        v.put(Images.Media.DISPLAY_NAME, f.getName());
        v.put(Images.Media.BUCKET_ID, path.hashCode());
        v.put(Images.Media.BUCKET_DISPLAY_NAME, name);
        v.put(Images.Media.SIZE, f.length());
        f = null;

        v.put(MediaStore.Images.Media.DATA, imagePath);
        ContentResolver c = context.getContentResolver();
        return c.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, v);
    }

    /*
     * get image folder list
     */
    public static List<PhotoAibum> getPhotoAlbum(Context context) {
        List<PhotoAibum> aibumList = new ArrayList<PhotoAibum>();
        Cursor cursor = MediaStore.Images.Media.query(
                context.getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, STORE_IMAGES, null,
                MediaColumns.DATE_MODIFIED + " desc");
        Map<String, PhotoAibum> countMap = new HashMap<String, PhotoAibum>();
        PhotoAibum pa = null;
        while (cursor.moveToNext()) {
            String path = cursor.getString(1);
            String dir_id = cursor.getString(3);
            String dir = cursor.getString(4);
            if (!countMap.containsKey(dir_id)) {
                pa = new PhotoAibum();
                pa.setName(dir);
                pa.setCount("1");
                pa.setDirPath(FileOperationUtil.getDirPathFromFilepath(path));
                pa.getBitList().add(new PhotoItem(path));
                countMap.put(dir_id, pa);
            } else {
                pa = countMap.get(dir_id);
                pa.setCount(String.valueOf(Integer.parseInt(pa.getCount()) + 1));
                pa.getBitList().add(new PhotoItem(path));
            }
        }
        cursor.close();
        Iterable<String> it = countMap.keySet();
        for (String key : it) {
            aibumList.add(countMap.get(key));
        }
        Collections.sort(aibumList, FileOperationUtil.mFolderCamparator);
        return aibumList;
    }

}
