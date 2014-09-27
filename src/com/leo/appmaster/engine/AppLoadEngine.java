package com.leo.appmaster.engine;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.leo.appmaster.model.AppDetailInfo;
import com.leo.appmaster.model.CacheInfo;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.TextFormater;

public class AppLoadEngine {
	private static final String TAG = "app engine";

	public interface IAppLoadListener {
		void onLoadFinsh(List<AppDetailInfo> list);
	}

	private static AppLoadEngine mInstance;
	private Context mContext;
	private PackageManager mPm;
	private CountDownLatch mLatch;
	private Handler mHandler;
	private boolean mInit;

	private IAppLoadListener mLoadListener;

	private static final int MSG_LOAD_BASE_INFO_FINISH = 1000;

	@SuppressLint("HandlerLeak")
	private class LoadHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_LOAD_BASE_INFO_FINISH:
				Log.d(TAG, "load app finished");
				if (mLoadListener != null) {
					Set<Entry<String, AppDetailInfo>> set = mAppDetails
							.entrySet();
					ArrayList<AppDetailInfo> list = new ArrayList<AppDetailInfo>();
					for (Entry<String, AppDetailInfo> entry : set) {
						list.add(entry.getValue());
					}
					mLoadListener.onLoadFinsh(list);
				}
				break;

			default:
				break;
			}
		}
	}

	/*
	 * do not change this data structure, because it is thread-safety
	 */

	private ConcurrentHashMap<String, AppDetailInfo> mAppDetails;

	private AppLoadEngine() {
		mAppDetails = new ConcurrentHashMap<String, AppDetailInfo>();
	}

	public static synchronized AppLoadEngine getInstance() {
		if (mInstance == null) {
			mInstance = new AppLoadEngine();
		}
		return mInstance;
	}

	/**
	 * call from main thread
	 * 
	 * @param ctx
	 */
	public void init(Context ctx) {
		if (!mInit) {
			mInit = true;
			mContext = ctx;
			mHandler = new LoadHandler();
			mPm = mContext.getPackageManager();
			mLatch = new CountDownLatch(1);
		}
	}

	public void setLoadListener(IAppLoadListener listener) {
		mLoadListener = listener;
	}

	public void loadAllBaseInfo() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				loadAllPkgInfo();
				loadPowerComsuInfo();
				mHandler.sendEmptyMessage(MSG_LOAD_BASE_INFO_FINISH);
			}
		}).start();
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
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> apps = mPm.queryIntentActivities(mainIntent, 0);
		AppDetailInfo appInfo;
		for (ResolveInfo resolveInfo : apps) {

			appInfo = new AppDetailInfo();
			ApplicationInfo applicationInfo = resolveInfo.activityInfo.applicationInfo;
			String packageName = applicationInfo.packageName;

			// first fill base info
			appInfo.setPkg(packageName);
			appInfo.setAppLabel(applicationInfo.loadLabel(mPm).toString());
			appInfo.setAppIcon(applicationInfo.loadIcon(mPm));
			appInfo.setSystemApp(AppUtil.isSystemApp(applicationInfo));
			appInfo.setInSdcard(AppUtil.isInstalledInSDcard(applicationInfo));
			appInfo.setUid(applicationInfo.uid);

			mAppDetails.put(packageName, appInfo);
		}
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
			info.getPermissionInfo().setPermissions(packangeInfo.permissions);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void loadTrafficInfo(String pkgName) {
		AppDetailInfo info = mAppDetails.get(pkgName);
		if (info != null) {
			long received = TrafficStats.getUidRxBytes(info.getUid());
			long transmitted = TrafficStats.getUidTxBytes(info.getUid());
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

	public void removeListener() {
		mLoadListener = null;
	}

}
