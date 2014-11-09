package com.leo.appmaster.backup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;
import com.leo.appmaster.SDKWrapper;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.engine.AppLoadEngine.AppChangeListener;
import com.leo.appmaster.model.AppDetailInfo;
import com.leo.appmaster.utils.LeoLog;
import com.leoers.leoanalytics.LeoStat;

public class AppBackupRestoreManager implements AppChangeListener {

	public static final int FAIL_TYPE_NONE = -1;
	public static final int FAIL_TYPE_FULL = 0;
	public static final int FAIL_TYPE_SDCARD_UNAVAILABLE = 1;
	public static final int FAIL_TYPE_SOURCE_NOT_FOUND = 2;
	public static final int FAIL_TYPE_OTHER = 3;
	public static final int FAIL_TYPE_CANCELED = 4;

	public static final String BACKUP_PATH = "appmaster/backup/";
	private static final String INSTALL_PACKAGE = "com.android.packageinstaller";
	private static final String PATH_ASSETMANAGER = "android.content.res.AssetManager";
	private static final String METHOD_ADD_ASSET = "addAssetPath";
	private static final String DATA_TYPE = "application/vnd.android.package-archive";

	private static final long sKB = 1024;
	private static final long sMB = sKB * sKB;
	private static final long sGB = sMB * sKB;

	private static final String sUnitB = "B";
	private static final String sUnitKB = "KB";
	private static final String sUnitMB = "MB";
	private static final String sUnitGB = "GB";

	private static final DecimalFormat sFormat = new DecimalFormat("#.0");

	public interface AppBackupDataListener {
		public void onDataReady();

		public void onDataUpdate();

		public void onBackupProcessChanged(int doneNum, int totalNum,
				String currentApp);

		public void onBackupFinish(boolean success, int successNum,
				int totalNum, String message);

		public void onApkDeleted(boolean success);
	}

	private class SDCardReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
				onSDCardChange(true);
			} else {
				onSDCardChange(false);
			}
		}
	}

	private ExecutorService mExecutorService = Executors
			.newSingleThreadExecutor();

	private SDCardReceiver mSDReceiver;

	private AppBackupDataListener mBackupListener;

	private ArrayList<AppDetailInfo> mSavedList;
	private ArrayList<AppDetailInfo> mBackupList;

	private boolean mDataReady = false;

	private boolean mBackupCanceled = false;

	private PackageManager mPackageManager;

	public AppBackupRestoreManager(Context context,
			AppBackupDataListener listener) {
		mPackageManager = context.getPackageManager();
		mBackupListener = listener;
		mSavedList = new ArrayList<AppDetailInfo>();
		mBackupList = new ArrayList<AppDetailInfo>();
		AppLoadEngine.getInstance(context).registerAppChangeListener(this);

		mSDReceiver = new SDCardReceiver();
		IntentFilter intentFilter = new IntentFilter(
				Intent.ACTION_MEDIA_MOUNTED);
		intentFilter.setPriority(1000);
		intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
		intentFilter.addAction(Intent.ACTION_MEDIA_SHARED);
		intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
		intentFilter.addDataScheme("file");
		context.registerReceiver(mSDReceiver, intentFilter);
	}

	public void prepareDate() {
		mExecutorService.execute(new Runnable() {
			@Override
			public void run() {
				getBackupList();
				if (mBackupListener != null) {
					mBackupListener.onDataReady();
				}
			}
		});
	}

	public void backupApps(final ArrayList<AppDetailInfo> apps) {
		mBackupCanceled = false;
		String backupPath = getBackupPath();
		final int totalNum = apps.size();
		if (backupPath == null) {
			mBackupListener.onBackupFinish(false, 0, totalNum,
					getFailMessage(FAIL_TYPE_SDCARD_UNAVAILABLE));
		} else {
			mExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					int doneNum = 0;
					int successNum = 0;
					int failType = FAIL_TYPE_NONE;
					boolean success = true;
					for (AppDetailInfo app : apps) {
						if (mBackupCanceled) {
							failType = FAIL_TYPE_CANCELED;
							success = false;
							break;
						}
						if (mBackupListener != null) {
							mBackupListener.onBackupProcessChanged(doneNum,
									totalNum, app.getAppLabel());
						}
						doneNum++;
						failType = tryBackupApp(app);
						if (failType == FAIL_TYPE_NONE) {
							successNum++;
						} else if (failType == FAIL_TYPE_SDCARD_UNAVAILABLE
								|| failType == FAIL_TYPE_FULL) {
							success = false;
							break;
						}
					}
					if (successNum == 0) {
						success = false;
					}
					if (mBackupListener != null) {
						if (doneNum == totalNum) {
							mBackupListener.onBackupProcessChanged(doneNum,
									totalNum, null);
						}
						mBackupListener.onBackupFinish(success, successNum,
								totalNum, getFailMessage(failType));
					}
				}
			});
		}
	}

	public void cancelBackup() {
		mBackupCanceled = true;
	}

	public void deleteApp(final AppDetailInfo app) {
		mExecutorService.execute(new Runnable() {
			@Override
			public void run() {
				File apkFile = new File(app.getSourceDir());
				boolean success = false;
				if (apkFile.exists()) {
					success = apkFile.delete();
				} else {
					success = true;
				}
				if (success) {
					mSavedList.remove(app);
					String pName = app.getPkg();
					for (AppDetailInfo a : mBackupList) {
						if (pName.equals(a.getPkg())) {
							a.isBackuped = false;
							break;
						}
					}
				}
				if (mBackupListener != null) {
					mBackupListener.onApkDeleted(success);
				}
			}
		});
	}

	public void restoreApp(Context context, AppDetailInfo app) {
		Intent intent = new Intent();
		intent.setDataAndType(Uri.fromFile(new File(app.getSourceDir())),
				DATA_TYPE);
		try {
			// check android package installer
			mPackageManager.getPackageInfo(INSTALL_PACKAGE, 0);
			intent.setPackage(INSTALL_PACKAGE);
		} catch (NameNotFoundException e) {
		}
		context.startActivity(intent);
		SDKWrapper.addEvent(context, LeoStat.P1, "backup",
				"recover: " + app.getPkg());
	}

	public void checkDataUpdate() {
		if (mDataReady) {
			mExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					ArrayList<AppDetailInfo> deleteSavedList = new ArrayList<AppDetailInfo>();
					for (AppDetailInfo app : mSavedList) {
						File apkFile = new File(app.getSourceDir());
						if (!apkFile.isFile() || !apkFile.exists()) {
							deleteSavedList.add(app);
							for (AppDetailInfo a : mBackupList) {
								if (app.getPkg().equals(a.getPkg())) {
									a.isBackuped = false;
								}
							}
						}
					}
					if (deleteSavedList.size() > 0) {
						mSavedList.removeAll(deleteSavedList);
						if (mBackupListener != null) {
							mBackupListener.onDataUpdate();
						}
					}
				}
			});
		}
	}

	public String getInstalledAppSize() {
		int installedSize = mBackupList.size();
		Resources res = AppMasterApplication.getInstance().getResources();
		String tips = String.format(res.getString(R.string.installed_app_size),
				installedSize);
		return tips;
	}

	public String getApkSize(AppDetailInfo app) {
		String s = AppMasterApplication.getInstance().getString(
				R.string.apk_size);
		File file = new File(app.getSourceDir());
		if (file.isFile() && file.exists()) {
			long size = file.length();
			return String.format(s, convertToSizeString(size));
		}
		return String.format(s, "0");
	}

	public String getAvaiableSizeString() {
		String tips = AppMasterApplication.getInstance().getString(
				R.string.storage_size);
		if (isSDReady()) {
			return String.format(tips, convertToSizeString(getAvaiableSize()));
		}
		return String.format(tips, AppMasterApplication.getInstance()
				.getString(R.string.unavailable));
	}

	private long getAvaiableSize() {
		File path = Environment.getExternalStorageDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize;
	}

	private String convertToSizeString(long size) {
		String sSize = null;
		if (size > sGB) {
			sSize = sFormat.format((float) size / sGB) + sUnitGB;
		} else if (size > sMB) {
			sSize = sFormat.format((float) size / sMB) + sUnitMB;
		} else if (size > sKB) {
			sSize = sFormat.format((float) size / sKB) + sUnitKB;
		} else {
			sSize = size + sUnitB;
		}
		return sSize;
	}

	private int tryBackupApp(AppDetailInfo app) {
		File apkFile = new File(app.getSourceDir());
		if (apkFile.exists() == false) {
			return FAIL_TYPE_SOURCE_NOT_FOUND;
		}
		String dest = getBackupPath();
		if (dest == null) {
			return FAIL_TYPE_SDCARD_UNAVAILABLE;
		}
		if (apkFile.length() > getAvaiableSize()) {
			return FAIL_TYPE_FULL;
		}
		FileInputStream in = null;
		try {
			in = new FileInputStream(apkFile);
		} catch (FileNotFoundException e) {
			return FAIL_TYPE_SOURCE_NOT_FOUND;
		}
		String pName = app.getPkg();
		// do file copy operation
		byte[] c = new byte[1024 * 5];
		int slen;
		FileOutputStream out = null;
		try {
			dest += pName + ".apk";
			out = new FileOutputStream(dest);
			while ((slen = in.read(c, 0, c.length)) != -1) {
				if (mBackupCanceled) {
					break;
				}
				out.write(c, 0, slen);
			}
		} catch (IOException e) {
			return FAIL_TYPE_OTHER;
		} finally {
			if (out != null)
				try {
					out.close();
				} catch (IOException e) {
					return FAIL_TYPE_OTHER;
				}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					return FAIL_TYPE_OTHER;
				}
			}
			if (mBackupCanceled) {
				File f = new File(dest);
				if (f.exists()) {
					f.delete();
				}
				return FAIL_TYPE_CANCELED;
			}
		}

		AppDetailInfo newApp = null;
		boolean add = true;
		for (AppDetailInfo appInfo : mSavedList) {
			if (pName.equals(appInfo.getPkg())) {
				newApp = appInfo;
				add = false;
				break;
			}
		}
		if (newApp == null) {
			newApp = new AppDetailInfo();
		}
		newApp.setAppLabel(app.getAppLabel());
		newApp.setAppIcon(app.getAppIcon());
		newApp.setPkg(app.getPkg());
		newApp.setVersionCode(app.getVersionCode());
		newApp.setVersionName(app.getVersionName());
		newApp.setSourceDir(dest);
		app.isBackuped = true;
		app.isChecked = false;
		if (add) {
			mSavedList.add(newApp);
		}
		return FAIL_TYPE_NONE;
	}

	public synchronized ArrayList<AppDetailInfo> getBackupList() {
		if (mBackupList.isEmpty()) {
			getRestoreList();
			ArrayList<AppDetailInfo> allApps = AppLoadEngine.getInstance(null)
					.getAllPkgInfo();
			for (AppDetailInfo app : allApps) {
				if (app.isSystemApp()) {
					continue;
				}
				String pName = app.getPkg();
				int versionCode = app.getVersionCode();
				for (AppDetailInfo a : mSavedList) { // check if already
														// backuped
					if (pName.equals(a.getPkg())
							&& versionCode == a.getVersionCode()) {
						app.isBackuped = true;
						break;
					}
				}
				mBackupList.add(app);
			}

		}

		Collections.sort(mBackupList, new Comparator<AppDetailInfo>() {
			@Override
			public int compare(AppDetailInfo lhs, AppDetailInfo rhs) {
				if (lhs.isBackuped && !rhs.isBackuped) {
					return 1;
				}
				if (!lhs.isBackuped && rhs.isBackuped) {
					return -1;
				}
				return 0;
			}
		});
		mDataReady = true;
		return mBackupList;
	}

	public synchronized ArrayList<AppDetailInfo> getRestoreList() {

		try {
			if (!mSavedList.isEmpty()) {
				return mSavedList;
			}
			String path = getBackupPath();
			if (path != null) {
				File backupDir = new File(path);
				File[] fs = backupDir.listFiles();
				for (File f : fs) {
					String fPath = f.getAbsolutePath();
					if (f.isFile() && fPath.endsWith(".apk")) {
						PackageInfo pInfo = mPackageManager
								.getPackageArchiveInfo(fPath, 0);
						if (pInfo != null) {
							AppDetailInfo app = new AppDetailInfo();
							Resources res = getResources(fPath);
							ApplicationInfo appInfo = pInfo.applicationInfo;
							String label = null;
							Drawable icon = null;
							if (res != null) {
								try {
									label = res.getString(appInfo.labelRes);
									icon = res.getDrawable(appInfo.icon);
								} catch (Exception e) {

								}
							}
							if (label == null) {
								label = mPackageManager.getApplicationLabel(
										appInfo).toString();
							}
							if (icon == null) {
								icon = mPackageManager
										.getApplicationIcon(appInfo);
							}
							app.setAppLabel(label);
							app.setAppIcon(icon);
							app.setSourceDir(fPath);
							app.setPkg(appInfo.packageName);
							app.setVersionCode(pInfo.versionCode);
							app.setVersionName(pInfo.versionName);
							mSavedList.add(app);
						}
					}
				}
			}
		} catch (Exception e) {
			LeoLog.e("Exception", e.getMessage());
		}

		return mSavedList;
	}

	private Resources getResources(String apkPath) {
		try {
			Class assetMagCls = Class.forName(PATH_ASSETMANAGER);
			Constructor assetMagCt = assetMagCls.getConstructor((Class[]) null);
			Object assetMag = assetMagCt.newInstance((Object[]) null);
			Class[] typeArgs = new Class[1];
			typeArgs[0] = String.class;
			Method assetMag_addAssetPathMtd = assetMagCls.getDeclaredMethod(
					METHOD_ADD_ASSET, typeArgs);
			Object[] valueArgs = new Object[1];
			valueArgs[0] = apkPath;
			assetMag_addAssetPathMtd.invoke(assetMag, valueArgs);
			Resources res = AppMasterApplication.getInstance().getResources();
			typeArgs = new Class[3];
			typeArgs[0] = assetMag.getClass();
			typeArgs[1] = res.getDisplayMetrics().getClass();
			typeArgs[2] = res.getConfiguration().getClass();
			Constructor resCt = Resources.class.getConstructor(typeArgs);
			valueArgs = new Object[3];
			valueArgs[0] = assetMag;
			valueArgs[1] = res.getDisplayMetrics();
			valueArgs[2] = res.getConfiguration();
			res = (Resources) resCt.newInstance(valueArgs);
			return res;
		} catch (Exception e) {
			return null;
		}
	}

	private boolean isSDReady() {
		return Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState());
	}

	private String getBackupPath() {
		if (isSDReady()) {
			String path = Environment.getExternalStorageDirectory()
					.getAbsolutePath();
			if (!path.endsWith(File.separator)) {
				path += File.separator;
			}
			path += BACKUP_PATH;
			File backupDir = new File(path);
			if (!backupDir.exists()) {
				boolean success = backupDir.mkdirs();
				if (!success) {
					return null;
				}
			}
			return path;
		}
		return null;
	}

	private String getFailMessage(int failType) {
		switch (failType) {
		case FAIL_TYPE_CANCELED:
			return AppMasterApplication.getInstance().getString(
					R.string.bakup_fail_cancel);
		case FAIL_TYPE_FULL:
			return AppMasterApplication.getInstance().getString(
					R.string.bakup_fail_full);
		case FAIL_TYPE_SDCARD_UNAVAILABLE:
			return AppMasterApplication.getInstance().getString(
					R.string.bakup_fail_unavailable);
		}
		return "sdcard/" + BACKUP_PATH;
	}

	public void onDestory(Context context) {
		mBackupListener = null;
		mSavedList.clear();
		mBackupList.clear();
		AppLoadEngine.getInstance(null).unregisterAppChangeListener(this);
		context.unregisterReceiver(mSDReceiver);
	}

	@Override
	public void onAppChanged(ArrayList<AppDetailInfo> changes, int type) {
		if (type == AppChangeListener.TYPE_ADD
				|| type == AppChangeListener.TYPE_AVAILABLE) {
			mBackupList.addAll(changes);
		} else if (type == AppChangeListener.TYPE_REMOVE
				|| type == AppChangeListener.TYPE_UNAVAILABLE) {
			mBackupList.removeAll(changes);
		} else if (type == AppChangeListener.TYPE_UPDATE) {
			for (AppDetailInfo app : changes) {
				if (app.isBackuped) {
					String pkg = app.getPkg();
					int vCode = app.getVersionCode();
					for (AppDetailInfo a : mSavedList) {
						if (pkg.equals(a.getPkg())
								&& vCode != a.getVersionCode()) {
							app.isBackuped = false;
							break;
						}
					}
				}
			}
		}
		mBackupListener.onDataUpdate();
	}

	private void onSDCardChange(boolean mounted) {
		String backupPath = getBackupPath();
		if (backupPath == null) {
			mSavedList.clear();
			for (AppDetailInfo app : mBackupList) {
				app.isBackuped = false;
			}
			mBackupListener.onDataUpdate();
		} else {
			mExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					mBackupList.clear();
					mSavedList.clear();
					mDataReady = false;
					getBackupList();
					if (mBackupListener != null) {
						mBackupListener.onDataUpdate();
					}
				}
			});
		}
	}

}
