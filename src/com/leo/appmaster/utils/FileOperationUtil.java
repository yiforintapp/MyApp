package com.leo.appmaster.utils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.MediaColumns;

public class FileOperationUtil {

	public static final String SDCARD_DIR_NAME = "PravicyLock";

	static final String[] STORE_IMAGES = {
			MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA,
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
		if (filepath != null) {
			String filename;
			int pos = filepath.lastIndexOf('/');
			if (pos != -1) {
				filename = filepath.substring(pos + 1);
				return filename;
			}
		}
		return "";
	}

	public static String getNoExtNameFromHideFilepath(String filepath) {
		if (filepath != null) {
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
		}
		return "";
	}

	public static String getDirPathFromFilepath(String filepath) {
		if (filepath != null) {
			int pos = filepath.lastIndexOf('/');
			if (pos != -1) {
				return filepath.substring(0, pos);
			}
		}
		return "";
	}

	public static String makePath(String path1, String path2) {
		if (path1 != null && path2 != null) {
			if (path1.endsWith(File.separator)) {
				return path1 + path2;
			}
			return path1 + File.separator + path2;
		}
		return "";
	}

	public static String getSdDirectory() {
		return Environment.getExternalStorageDirectory().getPath();
	}

	public static String getDirNameFromFilepath(String path) {
		if (path != null) {
			String dirName;
			int pos = path.lastIndexOf('/');
			if (pos != -1) {
				dirName = path.substring(0, pos);
				pos = dirName.lastIndexOf('/');
				dirName = dirName.substring(pos + 1);
				return dirName;
			}
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
	public static boolean renameFile(String filePath, String newName) {
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

	public static void deleteFileMediaEntry(String imagePath, Context context) {
		if (imagePath != null) {
			String params[] = new String[] { imagePath };
			Uri uri = Files.getContentUri("external");
			context.getContentResolver().delete(uri,
					MediaColumns.DATA + " LIKE ?", params);
		}
	}

	/**
	 * cut a file to our special dir in the same sdcard
	 * 
	 * @param filePath
	 * @param newName
	 * @return
	 */
	public static synchronized String hideImageFile(Context ctx,
			String filePath, String newName) {
		if (filePath == null || newName == null) {
			LeoLog.e("RenameFile", "Rename: null parameter");
			return null;
		}

		String[] paths = getSdCardPaths(ctx);
		int position = 0;
		if (filePath.startsWith(paths[0])) {
			position = 0;
		} else if (filePath.startsWith(paths[1])) {
			position = 1;
		} else {
			return null;
		}

		File file = new File(filePath);
		String newPath = FileOperationUtil.makePath(paths[position],
				FileOperationUtil.getDirPathFromFilepath(filePath), newName);

		try {
			if (file.isFile()) {
				String newFileDir = newPath.substring(0,
						newPath.lastIndexOf(File.separator));
				File temp = new File(newFileDir);
				LeoLog.d("RenameFile", "fileDir = " + newFileDir);
				if (temp.exists()) {
					LeoLog.d("RenameFile", temp + "    exists");
				} else {
					LeoLog.d("RenameFile", temp + "  not   exists");
					boolean mkRet = temp.mkdirs();
					if (mkRet) {
						LeoLog.e("RenameFile", "make dir " + temp
								+ "  successfully");
					} else {
						LeoLog.d("RenameFile", "make dir " + temp
								+ "  unsuccessfully");
						return null;
					}
				}
				boolean ret = file.renameTo(new File(newPath));
				LeoLog.d("RenameFile", ret + " : rename file " + filePath
						+ " to " + newPath);
				return ret ? newPath : null;
			} else {
				return null;
			}
		} catch (SecurityException e) {
			LeoLog.e("RenameFile", "Fail to rename file," + e.toString());
		}
		return null;
	}

	public static synchronized String unhideImageFile(Context ctx,
			String filePath) {
		if (filePath == null || !filePath.endsWith(".leotmp")) {
			LeoLog.e("RenameFile", "Rename: null parameter");
			return null;
		}

		String newPath = null;
		boolean newHided = false;
		if (filePath.contains(SDCARD_DIR_NAME)) {
			newHided = true;
			newPath = filePath.replace(".leotmp", "").replace(
					SDCARD_DIR_NAME + File.separator, "");

		} else {
			newHided = false;
			newPath = filePath.replace(".leotmp", "");
		}
		String fileName = getNameFromFilepath(newPath);
		String fileDir = newPath.replace(fileName, "");
		if (fileName.startsWith(".")) {
			fileName = fileName.substring(1);
			newPath = fileDir + fileName;
		}

		File file = new File(filePath);
		if (file.isFile()) {
			String newFileDir = null;
			if (newHided) {
				newFileDir = newPath.substring(0,
						newPath.lastIndexOf(File.separator)).replace(
						SDCARD_DIR_NAME + File.separator, "");
			} else {
				newFileDir = newPath.substring(0,
						newPath.lastIndexOf(File.separator));
			}

			File temp = new File(newFileDir);
			if (temp.exists()) {
				LeoLog.d("unhideImageFile", temp + "    exists");
			} else {
				LeoLog.d("unhideImageFile", temp + "  not   exists");
				boolean mkRet = temp.mkdirs();
				if (mkRet) {
					LeoLog.d("unhideImageFile", "make dir " + temp
							+ "  successfully");
				} else {
					LeoLog.d("unhideImageFile", "make dir " + temp
							+ "  unsuccessfully");
					return null;
				}
			}
			boolean ret = file.renameTo(new File(newPath));
			LeoLog.e("unhideImageFile", ret + " : rename file " + filePath
					+ " to " + newPath);
			return ret ? newPath : null;
		} else {
			return null;
		}
	}

	/**
	 * 
	 * @param sdcarPath
	 * @param dirPathFromFilepath
	 * @param newName
	 * @return
	 */
	private static String makePath(String sdcarPath,
			String dirPathFromFilepath, String newName) {

		if (!dirPathFromFilepath.startsWith(sdcarPath)) {
			return null;
		} else {
		    dirPathFromFilepath = dirPathFromFilepath.replaceAll(SDCARD_DIR_NAME + File.separator,
                    "");
		    
			String target = sdcarPath + File.separator + SDCARD_DIR_NAME
					+ dirPathFromFilepath.replace(sdcarPath, "");

			if (target.endsWith(File.separator)) {
				return target + newName;
			} else {
				return target + File.separator + newName;
			}
		}
	}

	/**
	 * @param filePath
	 * @return
	 */
	public static boolean deleteFile(String filePath) {
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
		Uri result = null;
		try {
			result = c.insert(uri, v);
		} catch (Exception e) {

		}
		return result;
	}

	public static void deleteImageMediaEntry(String imagePath, Context context) {
		String params[] = new String[] { imagePath };
		context.getContentResolver().delete(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				MediaStore.Images.Media.DATA + " LIKE ?", params);
	}

	public static void deleteVideoMediaEntry(String videoPath, Context context) {
		String params[] = new String[] { videoPath };
		Uri uri = Files.getContentUri("external");
		String selection = Constants.VIDEO_FORMAT;
		context.getContentResolver().delete(uri,
				MediaStore.Files.FileColumns.DATA + " LIKE ?", params);
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
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, STORE_IMAGES,
				null, MediaColumns.DATE_MODIFIED + " desc");
		if (cursor != null) {
			Map<String, PhotoAibum> countMap = new HashMap<String, PhotoAibum>();
			PhotoAibum pa = null;
			try {
				while (cursor.moveToNext()) {
					String path = cursor.getString(1);
					String dir_id = cursor.getString(3);
					String dir = cursor.getString(4);
					if (!countMap.containsKey(dir_id)) {
						pa = new PhotoAibum();
						pa.setName(dir);
						pa.setCount("1");
						pa.setDirPath(FileOperationUtil
								.getDirPathFromFilepath(path));
						pa.getBitList().add(new PhotoItem(path));
						countMap.put(dir_id, pa);
					} else {
						pa = countMap.get(dir_id);
						pa.setCount(String.valueOf(Integer.parseInt(pa
								.getCount()) + 1));
						pa.getBitList().add(new PhotoItem(path));
					}
				}
			} catch (Exception e) {

			} finally {
				cursor.close();
			}
			Iterable<String> it = countMap.keySet();
			for (String key : it) {
				aibumList.add(countMap.get(key));
			}
			Collections.sort(aibumList, FileOperationUtil.mFolderCamparator);
		}

		return aibumList;
	}

	// 获取所有内置/外置SDCARD路径
	public static String[] getSdCardPaths(Context ctx) {
		StorageManager storageManager = (StorageManager) ctx
				.getSystemService(Context.STORAGE_SERVICE);
		Method method = null;
		try {
			method = StorageManager.class.getMethod("getVolumePaths");
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}

		if (method != null) {
			try {
				String[] storagePathListt = (String[]) method
						.invoke(storageManager);
				return storagePathListt;
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	// 判断sdcard是否挂载上，返回值为true证明挂载上了，否则不存在
	public boolean checkSDCardMount(Context ctx, String mountPoint) {
		StorageManager storageManager = (StorageManager) ctx
				.getSystemService(Context.STORAGE_SERVICE);
		if (mountPoint == null) {
			return false;
		}
		String state = null;

		Method method = null;
		try {
			method = StorageManager.class.getMethod("getVolumeState",
					String.class);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}

		if (method != null) {
			try {
				state = (String) method.invoke(storageManager, mountPoint);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		if (state == null) {
			return false;
		} else {
			return Environment.MEDIA_MOUNTED.equals(state);
		}

	}

}
