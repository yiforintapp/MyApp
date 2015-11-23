package com.leo.appmaster.engine;

import java.util.HashMap;

import com.leo.appmaster.engine.BatteryInfoProvider.DrainType;
import com.leo.appmaster.utils.AppUtil;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.BatteryStats.Uid;

public class BatteryComsuption implements Comparable<BatteryComsuption> {

	private final Context mContext;
	private final HashMap<String, UidToDetail> mUidCache = new HashMap<String, UidToDetail>();
	private String label;
	private Drawable icon;
	private Uid uidObj;
	private double value;
	private double[] values;
	long usageTime;
	long cpuTime;
	long gpsTime;
	long wifiRunningTime;
	long cpuFgTime;
	long wakeLockTime;
	long tcpBytesReceived;
	long tcpBytesSent;
	private double percent;
	double noCoveragePercent;
	private String defaultPackageName;
	private DrainType drainType;

	static class UidToDetail {
		String name;
		String packageName;
		Drawable icon;
	}

	public BatteryComsuption(Context context, String pkgName, double time) {
		mContext = context.getApplicationContext();
		value = time;
		drainType = DrainType.APP;
		getQuickNameIcon(pkgName);
	}

	public BatteryComsuption(Context context,
			BatteryInfoProvider.DrainType type, Uid uid, double[] values) {
		mContext = context;
		this.values = values;
		this.drainType = type;
		if (values != null)
			value = values[0];

		uidObj = uid;

		if (uid != null) {
			getQuickNameIconForUid(uid);
		}
	}

	public DrainType getDrainType() {
		return drainType;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public double[] getValues() {
		return values;
	}

	public String getDefaultPackageName() {
		return defaultPackageName;
	}

	public Drawable getIcon() {
		return icon;
	}

	public void setIcon(Drawable icon) {
		this.icon = icon;
	}

	public String getName() {
		return label;
	}

	public void setName(String name) {
		this.label = name;
	}

	public void setPercent(double percent) {
		this.percent = percent;
	}

	public double getPercentOfTotal() {
		return percent;
	}

	@Override
	public int compareTo(BatteryComsuption other) {
		return (int) (other.getValue() - getValue());
	}

	private void getQuickNameIcon(String pkgName) {
		PackageManager pm = mContext.getPackageManager();
		try {
			// 统一使用AppUtil.getDrawable获取app图标
			// icon = appInfo.loadIcon(pm);// pm.getApplicationIcon(appInfo);
			icon = AppUtil.getAppIcon(pm, pkgName);
			label = AppUtil.getAppLabel(pm, pkgName);
			defaultPackageName = pkgName;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void getQuickNameIconForUid(Uid uidObj) {
		final int uid = uidObj.getUid();
		final String uidString = Integer.toString(uid);
		if (mUidCache.containsKey(uidString)) {
			UidToDetail utd = mUidCache.get(uidString);
			defaultPackageName = utd.packageName;
			label = utd.name;
			icon = utd.icon;
			return;
		}
		PackageManager pm = mContext.getPackageManager();
		String[] packages = pm.getPackagesForUid(uid);
		if (packages == null) {
			if (uid == 0) {
				drainType = DrainType.KERNEL;
			} else if ("mediaserver".equals(label)) {
				drainType = DrainType.MEDIASERVER;
			}
			return;
		}

		getNameIcon();
	}

	/**
	 * Sets name and icon
	 * 
	 *            Uid of the application
	 */
	private void getNameIcon() {
		PackageManager pm = mContext.getPackageManager();
		final int uid = uidObj.getUid();
		final Drawable defaultActivityIcon = pm.getDefaultActivityIcon();
		String[] packages = pm.getPackagesForUid(uid);
		if (packages == null) {
			label = Integer.toString(uid);
			return;
		}

		String[] packageLabels = new String[packages.length];
		System.arraycopy(packages, 0, packageLabels, 0, packages.length);

		for (int i = 0; i < packageLabels.length; i++) {

			try {
				ApplicationInfo ai = pm.getApplicationInfo(packageLabels[i], 0);
				CharSequence label = ai.loadLabel(pm);
				if (label != null) {
					packageLabels[i] = label.toString();
				}
				if (ai.icon != 0) {
					defaultPackageName = packages[i];
					// 统一使用AppUtil.getDrawable获取app图标
					// icon = ai.loadIcon(pm);
					icon = AppUtil.getAppIcon(pm, defaultPackageName);
					break;
				}
			} catch (NameNotFoundException e) {
			}
		}
		if (icon == null)
			icon = defaultActivityIcon;

		if (packageLabels.length == 1) {
			label = packageLabels[0];
		} else {

			for (String pkgName : packages) {
				try {
					final PackageInfo pi = pm.getPackageInfo(pkgName, 0);
					if (pi.sharedUserLabel != 0) {
						final CharSequence nm = pm.getText(pkgName,
								pi.sharedUserLabel, pi.applicationInfo);
						if (nm != null) {
							label = nm.toString();
							if (pi.applicationInfo.icon != 0) {
								defaultPackageName = pkgName;
								// 统一使用AppUtil.getDrawable获取app图标
								// icon = pi.applicationInfo.loadIcon(pm);
								icon = AppUtil.getAppIcon(pm, pkgName);
							}
							break;
						}
					}
				} catch (PackageManager.NameNotFoundException e) {
				}
			}
		}
		final String uidString = Integer.toString(uidObj.getUid());
		UidToDetail utd = new UidToDetail();
		utd.name = label;
		utd.icon = icon;
		utd.packageName = defaultPackageName;
		mUidCache.put(uidString, utd);
	}
}