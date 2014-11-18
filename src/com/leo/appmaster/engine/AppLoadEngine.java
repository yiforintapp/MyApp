package com.leo.appmaster.engine;

import java.lang.reflect.Method;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageStats;
import android.content.pm.ResolveInfo;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.SDKWrapper;
import com.leo.appmaster.applocker.AppLockListActivity;
import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.applocker.LockSettingActivity;
import com.leo.appmaster.fragment.LockFragment;
import com.leo.appmaster.home.HomeActivity;
import com.leo.appmaster.lockertheme.LockerTheme;
import com.leo.appmaster.model.AppDetailInfo;
import com.leo.appmaster.model.BaseInfo;
import com.leo.appmaster.model.CacheInfo;
import com.leo.appmaster.ui.dialog.LEOThreeButtonDialog;
import com.leo.appmaster.ui.dialog.LEOThreeButtonDialog.OnDiaogClickListener;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.TextFormater;
import com.leoers.leoanalytics.LeoStat;

public class AppLoadEngine extends BroadcastReceiver {
	/**
	 * Resister this listener to receive application changed events
	 */
	public interface AppChangeListener {

		final static int TYPE_NONE = -1;
		/**
		 * Applications added
		 */
		public final static int TYPE_ADD = 0;
		/**
		 * Applications uninstalled
		 */
		public final static int TYPE_REMOVE = 1;
		/**
		 * Application updated
		 */
		public final static int TYPE_UPDATE = 2;
		/**
		 * Applications available, for those applications installed in external
		 * storage.
		 */
		public final static int TYPE_AVAILABLE = 3;
		/**
		 * Applications unavailable, for those applications installed in
		 * external storage.
		 */
		public final static int TYPE_UNAVAILABLE = 4;
		/**
		 * Applications unavailable, for those applications installed in
		 * external storage.
		 */
		public final static int TYPE_LOCAL_CHANGE = 5;

		/**
		 * Called when applications changed, see
		 * {@link #registerAppChangeListener(AppChangeListener)}
		 * 
		 * @param changes
		 *            a list of changed applications
		 * @param type
		 *            we have 5 change types currently, {@link #TYPE_ADD},
		 *            {@link #TYPE_REMOVE}, {@link #TYPE_UPDATE}
		 */
		public void onAppChanged(final ArrayList<AppDetailInfo> changes,
				final int type);
	}

	public static final String ACTION_RECOMMEND_LIST_CHANGE = "com.leo.appmaster.RECOMMEND_LIST_CHANGE";

	private static AppLoadEngine mInstance;
	private Context mContext;
	private PackageManager mPm;
	private CountDownLatch mLatch;
	private boolean mInit;
	private boolean mAppsLoaded = false;

	private ArrayList<AppChangeListener> mListeners;

	private static final Object mLock = new Object();
	private static final HandlerThread sWorkerThread = new HandlerThread(
			"apps-data-manager");
	static {
		sWorkerThread.start();
	}
	private static final Handler sWorker = new Handler(
			sWorkerThread.getLooper());

	/*
	 * do not change this data structure, because it is thread-safety
	 */

	private ConcurrentHashMap<String, AppDetailInfo> mAppDetails;

	private final static String[] sLocalLockArray = new String[] {
			"com.whatsapp", "com.android.gallery3d", "com.android.mms",
			"com.tencent.mm", "com.android.contacts", "com.facebook.katana",
			"com.mxtech.videoplayer.ad", "com.facebook.orca",
			"com.mediatek.filemanager", "com.sec.android.gallery3d",
			"com.android.settings", "com.android.email",
			"com.android.providers.downloads.ui",
			"com.sec.android.app.myfiles", "com.android.vending",
			"com.google.android.youtube", "com.mediatek.videoplayer",
			"com.android.calendar", "com.google.android.talk",
			"com.viber.voip", "com.android.soundrecorder",
			"com.sec.android.app.videoplayer", "com.tencent.mobileqq",
			"jp.naver.line.android", "com.tencent.qq", "com.google.plus",
			"com.tencent.mm", "com.google.android.videos",
			"com.android.dialer", "com.samsung.everglades.video",
			"com.appstar.callrecorder", "com.sec.android.app.voicerecorder",
			"com.htc.soundrecorder", "com.twitter.android" };

	private List<String> mRecommendLocklist;

	private AppLoadEngine(Context context) {
		mContext = context.getApplicationContext();
		mPm = mContext.getPackageManager();
		mLatch = new CountDownLatch(1);
		mAppDetails = new ConcurrentHashMap<String, AppDetailInfo>();
		mListeners = new ArrayList<AppChangeListener>(1);

		List<String> list = AppMasterPreference.getInstance(mContext)
				.getRecommendList();
		if (list.get(0).equals("")) {
			mRecommendLocklist = Arrays.asList(sLocalLockArray);
		} else {
			mRecommendLocklist = list;
		}
	}

	public List<String> getRecommendLockList() {
		return mRecommendLocklist;
	}

	public void updateRecommendLockList(List<String> list) {
		mRecommendLocklist = list;
		Collection<AppDetailInfo> collection = mAppDetails.values();
		for (AppDetailInfo appDetailInfo : collection) {
			appDetailInfo.topPos = mRecommendLocklist.indexOf(appDetailInfo
					.getPkg());
		}
		AppMasterPreference.getInstance(mContext).setRecommendList(list);
	}

	public static synchronized AppLoadEngine getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new AppLoadEngine(context);
		}
		return mInstance;
	}

	public void registerAppChangeListener(AppChangeListener aListener) {
		if (mListeners.contains(aListener))
			return;
		mListeners.add(aListener);
	}

	public void unregisterAppChangeListener(AppChangeListener aListener) {
		mListeners.remove(aListener);
	}

	public void clearAllListeners() {
		mListeners.clear();
	}

	private void notifyChange(ArrayList<AppDetailInfo> changed, int type) {
		for (AppChangeListener listener : mListeners) {
			listener.onAppChanged(changed, type);
		}
	}

	public void preloadAllBaseInfo() {
		sWorker.post(new Runnable() {
			@Override
			public void run() {
				loadAllPkgInfo();
			}
		});
	}

	public ArrayList<AppDetailInfo> getAllPkgInfo() {
		loadAllPkgInfo();
		ArrayList<AppDetailInfo> dataList = new ArrayList<AppDetailInfo>();
		for (AppDetailInfo app : mAppDetails.values()) {
			if (!app.getPkg().startsWith("com.leo.theme")) {
				dataList.add(app);
			}
		}

		Collections.sort(dataList, new AppComparator());

		return dataList;
	}

	public AppDetailInfo loadAppDetailInfo(String pkgName) {
		mLatch = new CountDownLatch(1);
		loadTrafficInfo(pkgName);
		loadPermissionInfo(pkgName);
		loadCacheInfo(pkgName);
		try {
			mLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return mAppDetails.get(pkgName);
	}

	private void loadAllPkgInfo() {
		synchronized (mLock) {
			if (!mAppsLoaded) {
				Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
				mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
				List<ResolveInfo> apps = mPm.queryIntentActivities(mainIntent,
						0);
				AppMasterPreference pre = AppMasterPreference
						.getInstance(mContext);
				List<String> themeList = new ArrayList<String>(
						pre.getHideThemeList());
				
				for (ResolveInfo resolveInfo : apps) {
					ApplicationInfo applicationInfo = resolveInfo.activityInfo.applicationInfo;
					String packageName = applicationInfo.packageName;
					AppDetailInfo appInfo = new AppDetailInfo();
					loadAppInfoOfPackage(packageName, applicationInfo, appInfo);
					try {
						appInfo.installTime = mPm
								.getPackageInfo(packageName, 0).firstInstallTime;
					} catch (NameNotFoundException e) {
						e.printStackTrace();
					}

					if (!isThemeApk(packageName)) {
						mAppDetails.put(packageName, appInfo);
					} else {
						if (!themeList.contains(packageName)) {
							themeList.add(packageName);
						}
					}
				}
				pre.setHideThemeList(themeList);
				mAppsLoaded = true;
			}
		}
	}

	private void loadAppInfoOfPackage(String packageName,
			ApplicationInfo applicationInfo, AppDetailInfo appInfo) {
		// first fill base info
		try {
			PackageInfo pInfo = mPm.getPackageInfo(packageName, 0);
			appInfo.setVersionCode(pInfo.versionCode);
			appInfo.setVersionName(pInfo.versionName);
		} catch (NameNotFoundException e) {
		}
		appInfo.setPkg(packageName);
		appInfo.setAppLabel(applicationInfo.loadLabel(mPm).toString().trim());
		appInfo.setAppIcon(applicationInfo.loadIcon(mPm));
		appInfo.setSystemApp(AppUtil.isSystemApp(applicationInfo));
		appInfo.setInSdcard(AppUtil.isInstalledInSDcard(applicationInfo));
		appInfo.setUid(applicationInfo.uid);
		appInfo.setSourceDir(applicationInfo.sourceDir);
		appInfo.topPos = mRecommendLocklist.indexOf(packageName);
	}

	private void loadPowerComsuInfo() {
		BatteryInfoProvider provider = new BatteryInfoProvider(mContext);
		List<BatteryComsuption> list = provider.getBatteryStats();
		for (BatteryComsuption batterySipper : list) {
			String packageName = batterySipper.getDefaultPackageName();
			if (packageName != null && mAppDetails.containsKey(packageName)) {
				mAppDetails.get(packageName).setPowerComsuPercent(
						batterySipper.getPercentOfTotal());
			}
		}
	}

	private void loadCacheInfo(String pkgName) {
		AppDetailInfo info = mAppDetails.get(pkgName);
		getCacheInfo(pkgName, info.getCacheInfo());
	}

	private void loadPermissionInfo(String pkgName) {
		PackageInfo packangeInfo;
		AppDetailInfo info = mAppDetails.get(pkgName);
		try {
			packangeInfo = mPm.getPackageInfo(pkgName,
					PackageManager.GET_PERMISSIONS);
			// info.getPermissionInfo().setPermissions(packangeInfo.permissions);

			info.getPermissionInfo().setPermissionList(
					packangeInfo.requestedPermissions);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void loadTrafficInfo(String pkgName) {
		AppDetailInfo info = mAppDetails.get(pkgName);
		if (info != null) {
			long received = TrafficStats.getUidRxBytes(info.getUid());
			if (received < 0)
				received = 0;
			long transmitted = TrafficStats.getUidTxBytes(info.getUid());
			if (transmitted < 0)
				transmitted = 0;
			info.getTrafficInfo().setTransmittedData(transmitted);
			info.getTrafficInfo().setReceivedData(received);
		}

	}

	private void getCacheInfo(String pkg, final CacheInfo cacheInfo) {
		try {
			Method method = PackageManager.class.getMethod(
					"getPackageSizeInfo", new Class[] { String.class,
							IPackageStatsObserver.class });
			method.invoke(mPm, new Object[] { pkg,
					new IPackageStatsObserver.Stub() {
						@Override
						public void onGetStatsCompleted(PackageStats pStats,
								boolean succeeded) throws RemoteException {
							long cacheSize = pStats.cacheSize;
							long codeSize = pStats.codeSize;
							long dataSize = pStats.dataSize;

							cacheInfo.setCacheSize(TextFormater
									.dataSizeFormat(cacheSize));
							cacheInfo.setCodeSize(TextFormater
									.dataSizeFormat(codeSize));
							cacheInfo.setDataSize(TextFormater
									.dataSizeFormat(dataSize));

							mLatch.countDown();
						}
					} });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Call from the handler for ACTION_PACKAGE_ADDED, ACTION_PACKAGE_REMOVED
	 * and ACTION_PACKAGE_CHANGED.
	 */
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		if (Intent.ACTION_PACKAGE_REMOVED.equals(action)
				|| Intent.ACTION_PACKAGE_ADDED.equals(action)
				|| Intent.ACTION_PACKAGE_CHANGED.equals(action)) {

			final String packageName = intent.getData().getSchemeSpecificPart();
			final boolean replacing = intent.getBooleanExtra(
					Intent.EXTRA_REPLACING, false);
			int op = AppChangeListener.TYPE_NONE;

			if (packageName == null || packageName.length() == 0) {
				// they sent us a bad intent
				return;
			}
			if (Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
				op = AppChangeListener.TYPE_UPDATE;
			} else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
				if (!replacing) {
					op = AppChangeListener.TYPE_REMOVE;
					checkUnlockWhenRemove(packageName);
					checkThemeWhenRemove(packageName);
				}
				// else, we are replacing the package, so a PACKAGE_ADDED will
				// be sent
				// later, we will update the package at this time
			} else if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
				if (!replacing) {
					op = AppChangeListener.TYPE_ADD;
					showLockTip(packageName);

					if (isThemeApk(packageName)) {

						AppMasterPreference pre = AppMasterPreference
								.getInstance(mContext);
						List<String> themeList = new ArrayList<String>(
								pre.getHideThemeList());
						if (!themeList.contains(packageName)) {
							themeList.add(packageName);
						}
						pre.setHideThemeList(themeList);
						return;
					}
				} else {
					op = AppChangeListener.TYPE_UPDATE;
				}
			}

			if (op != AppChangeListener.TYPE_NONE) {
				enqueuePackageUpdated(new PackageUpdatedTask(op,
						new String[] { packageName }));
			}
		} else if (Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE.equals(action)) {
			// First, schedule to add these apps back in.
			String[] packages = intent
					.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
			enqueuePackageUpdated(new PackageUpdatedTask(
					AppChangeListener.TYPE_AVAILABLE, packages));
		} else if (Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE
				.equals(action)) {
			String[] packages = intent
					.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
			enqueuePackageUpdated(new PackageUpdatedTask(
					AppChangeListener.TYPE_UNAVAILABLE, packages));
		} else if (Intent.ACTION_LOCALE_CHANGED.equals(action)) {
			enqueuePackageUpdated(new PackageUpdatedTask(
					AppChangeListener.TYPE_LOCAL_CHANGE, new String[] {}));
		} else if (ACTION_RECOMMEND_LIST_CHANGE.equals(action)) {
			updateRecommendLockList(intent
					.getStringArrayListExtra(Intent.EXTRA_PACKAGES));
		}
	}

	private void checkThemeWhenRemove(final String packageName) {
		sWorker.post(new Runnable() {
			@Override
			public void run() {
				AppMasterPreference pre = AppMasterPreference
						.getInstance(mContext);
				List<String> themeList = new ArrayList<String>(pre
						.getHideThemeList());
				if (themeList.contains(packageName)) {
					LeoLog.d("checkThemeWhenRemove", "packageName = "
							+ packageName);
					themeList.remove(packageName);
				}
				pre.setHideThemeList(themeList);
			}
		});
	}

	private void checkUnlockWhenRemove(final String packageName) {
		sWorker.post(new Runnable() {
			@Override
			public void run() {
				AppMasterPreference pre = AppMasterPreference
						.getInstance(mContext);
				List<String> lockList = new ArrayList<String>(pre
						.getLockedAppList());
				if (lockList.contains(packageName)) {
					lockList.remove(packageName);
				}
				pre.setLockedAppList(lockList);
			}
		});
	}

	private void showLockTip(final String packageName) {

		if (packageName.startsWith("com.leo.theme")) {
			return;
		}

		if (AppMasterPreference.getInstance(mContext).isNewAppLockTip()) {
			sWorker.postDelayed(new Runnable() {
				@Override
				public void run() {
					// for (String str : sLocalLockArray) {
					// if (str.equals(packageName)) {
					LEOThreeButtonDialog dialog = new LEOThreeButtonDialog(
							mContext);
					dialog.setTitle(R.string.app_name);
					String tip = mContext.getString(
							R.string.new_install_lock_remind,
							AppUtil.getAppLabel(packageName, mContext));
					dialog.setContent(tip);
					dialog.setOnClickListener(new OnDiaogClickListener() {
						@Override
						public void onClick(int which) {
							AppMasterPreference pre = AppMasterPreference
									.getInstance(mContext);
							Intent intent = null;
							if (which == 0) {
								List<String> lockList = new ArrayList<String>(
										pre.getLockedAppList());
								lockList.add(packageName);
								pre.setLockedAppList(lockList);

								if (pre.getLockType() == AppMasterPreference.LOCK_TYPE_NONE) {
									intent = new Intent(mContext,
											LockSettingActivity.class);
									intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									mContext.startActivity(intent);
								}
							} else if (which == 1) {
								if (pre.getLockType() == AppMasterPreference.LOCK_TYPE_NONE) {
									intent = new Intent(mContext,
											LockSettingActivity.class);
									intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									mContext.startActivity(intent);
								} else {
									intent = new Intent(mContext,
											LockScreenActivity.class);
									intent.putExtra(
											LockScreenActivity.EXTRA_TO_ACTIVITY,
											AppLockListActivity.class.getName());
									int lockType = pre.getLockType();
									if (lockType == AppMasterPreference.LOCK_TYPE_PASSWD) {
										intent.putExtra(
												LockScreenActivity.EXTRA_UKLOCK_TYPE,
												LockFragment.LOCK_TYPE_PASSWD);
									} else {
										intent.putExtra(
												LockScreenActivity.EXTRA_UKLOCK_TYPE,
												LockFragment.LOCK_TYPE_GESTURE);
									}
									intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
											| Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
									intent.putExtra(
											LockScreenActivity.EXTRA_UNLOCK_FROM,
											LockFragment.FROM_SELF_HOME);

									mContext.startActivity(intent);
								}
								SDKWrapper.addEvent(mContext, LeoStat.P1, "lock_enter", "sug_lock_more");

							} else if (which == 2) {

							}
						}
					});
					dialog.getWindow().setType(
							WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
					dialog.show();
					// break;
					// }
					// }

				}
			}, 5000);
		}
	}

	void enqueuePackageUpdated(PackageUpdatedTask task) {
		sWorker.post(task);
	}

	private class PackageUpdatedTask implements Runnable {
		int mOp;
		String[] mPackages;

		public PackageUpdatedTask(int op, String[] packages) {
			mOp = op;
			mPackages = packages;
		}

		public void run() {
			final ArrayList<AppDetailInfo> changedFinal = new ArrayList<AppDetailInfo>(
					1);
			final String[] packages = mPackages;
			final int N = packages.length;
			switch (mOp) {
			case AppChangeListener.TYPE_ADD:
			case AppChangeListener.TYPE_AVAILABLE:
				for (int i = 0; i < N; i++) {
					Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
					mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
					mainIntent.setPackage(packages[i]);
					List<ResolveInfo> apps = mPm.queryIntentActivities(
							mainIntent, 0);
					if (apps.size() > 0) {
						ApplicationInfo applicationInfo = apps.get(0).activityInfo.applicationInfo;
						AppDetailInfo appInfo = new AppDetailInfo();
						loadAppInfoOfPackage(packages[i], applicationInfo,
								appInfo);
						mAppDetails.put(packages[i], appInfo);
						changedFinal.add(appInfo);
					}
				}
				break;
			case AppChangeListener.TYPE_UPDATE:
				for (int i = 0; i < N; i++) {
					AppDetailInfo appInfo = mAppDetails.get(packages[i]);
					if (appInfo != null) {
						Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
						mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
						mainIntent.setPackage(packages[i]);
						List<ResolveInfo> apps = mPm.queryIntentActivities(
								mainIntent, 0);
						if (apps.size() > 0) {
							ApplicationInfo applicationInfo = apps.get(0).activityInfo.applicationInfo;
							loadAppInfoOfPackage(packages[i], applicationInfo,
									appInfo);
							changedFinal.add(appInfo);
						}
					}
				}
				break;
			case AppChangeListener.TYPE_REMOVE:
			case AppChangeListener.TYPE_UNAVAILABLE:
				for (int i = 0; i < N; i++) {
					AppDetailInfo appInfo = mAppDetails.remove(packages[i]);
					changedFinal.add(appInfo);
				}
				break;
			case AppChangeListener.TYPE_LOCAL_CHANGE:
				mAppDetails.clear();
				mAppsLoaded = false;
				loadAllPkgInfo();
				break;

			}

			if (!changedFinal.isEmpty()) {
				notifyChange(changedFinal, mOp);
			}
		}
	}

	public boolean isThemeApk(final String pkg) {
		if (pkg.startsWith("com.leo.theme")) {
			return true; // add app list
		} else {
			return false;
		}
		
	}

	public static class AppComparator implements Comparator<BaseInfo> {

		@Override
		public int compare(BaseInfo lhs, BaseInfo rhs) {
			if (lhs.topPos > -1 && rhs.topPos < 0) {
				return -1;
			} else if (lhs.topPos < 0 && rhs.topPos > -1) {
				return 1;
			} else if (lhs.topPos > -1 && rhs.topPos > -1) {
				return lhs.topPos - rhs.topPos;
			}
			return Collator.getInstance().compare(
					trimString(lhs.getAppLabel()),
					trimString(rhs.getAppLabel()));
		}

		private String trimString(String s) {
			return s.replaceAll("\u00A0", "").trim();
		}

	}

}
