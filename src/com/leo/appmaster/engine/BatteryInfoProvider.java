package com.leo.appmaster.engine;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.os.BatteryStats;
import android.os.Parcel;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.telephony.SignalStrength;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.app.IBatteryStats;
import com.android.internal.os.BatteryStatsImpl;
import com.android.internal.os.PowerProfile;
import com.leo.appmaster.applocker.model.ProcessAdj;
import com.leo.appmaster.applocker.model.ProcessDetector;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.BatteryUtils;
import com.leo.appmaster.utils.LeoLog;
import com.leo.imageloader.utils.IoUtils;

public class BatteryInfoProvider {
	private static final String TAG = "BatteryInfo";
	private static final boolean DEBUG = true;

	public static final int MSG_UPDATE_NAME_ICON = 1;
	private static final int MIN_POWER_THRESHOLD = 5;

	private IBatteryStats mBatteryInfo;
	private int mStatsType = BatteryStats.STATS_SINCE_UNPLUGGED;
	private PowerProfile mPowerProfile;
	private static BatteryStatsImpl mStats;
	private double mMinPercentOfTotal = 0;
	private long mStatsPeriod = 0;
	public double mMaxPower = 1;
	private double mTotalPower;
	private double mWifiPower;
	private double mBluetoothPower;

	private long mAppWifiRunning;

	private final List<BatteryComsuption> mUsageList = new ArrayList<BatteryComsuption>();
	private final List<BatteryComsuption> mWifiSippers = new ArrayList<BatteryComsuption>();
	private final List<BatteryComsuption> mBluetoothSippers = new ArrayList<BatteryComsuption>();
	private Context mContext;
	public int testType;

	/* 3.3 增加最后一个策略，使用PS命令获取app电量消耗 */
	private ArrayList<ProcessDetector.ProcessFilter> mFilters;
	private static final String PS = "ps";
	private static final String SHELL = "sh";
	private static final String APP_PREFIX_1 = "u0";
	private static final String APP_PREFIX_2 = "app";
	private static final boolean DBG = true;
	protected static final String REGEX_SPACE = "\\s+";
	private static final int INDEX_USER = 0;
	private static final int INDEX_PID = 1;
	private static final int INDEX_PPID = 2;
	private static final int INDEX_PROCESS_NAME = 8;

	public enum DrainType {
		IDLE, CELL, PHONE, WIFI, BLUETOOTH, SCREEN, APP, KERNEL, MEDIASERVER;
	}

	public BatteryInfoProvider(Context context) {
		testType = 1;
		mContext = context.getApplicationContext();
		mBatteryInfo = IBatteryStats.Stub.asInterface(ServiceManager
				.getService("batteryinfo"));
		mPowerProfile = new PowerProfile(context);
	}

	public void setMinPercentOfTotal(double minPercentOfTotal) {
		this.mMinPercentOfTotal = minPercentOfTotal;
	}

	private List<ProcessDetector.ProcessFilter> getBatteryFilters() {
		if (mFilters == null) {
			mFilters = new ArrayList<ProcessDetector.ProcessFilter>();
			mFilters.add(new BatteryProcessFilter());
		}
		return mFilters;
	}

	public double getTotalPower() {
		return mTotalPower;
	}

	public String getStatsPeriod() {
		return BatteryUtils.formatElapsedTime(mContext, mStatsPeriod);
	}

	public List<BatteryComsuption> getBatteryStats() {

		if (mStats == null) {
			mStats = load();
		}

		if (mStats == null) {
			LeoLog.d(TAG, "mStats == null, get apps from getAppListCpuTime()");
			return getAppListCpuTime();
		}

		mMaxPower = 0;
		mTotalPower = 0;
		mWifiPower = 0;
		mBluetoothPower = 0;
		mAppWifiRunning = 0;

		mUsageList.clear();
		mWifiSippers.clear();
		mBluetoothSippers.clear();
		processAppUsage();
		processMiscUsage();

		final List<BatteryComsuption> list = new ArrayList<BatteryComsuption>();

		Collections.sort(mUsageList);
		LeoLog.d(TAG, "mUsageList size = " + mUsageList.size());
		for (BatteryComsuption sipper : mUsageList) {
			if (sipper.getDefaultPackageName() == null
					|| sipper.getIcon() == null) {
				continue;
			}
			if (sipper.getValue() < MIN_POWER_THRESHOLD)
				continue;
			final double percentOfTotal = ((sipper.getValue() / mTotalPower) * 100);
			sipper.setPercent(percentOfTotal);
			if (percentOfTotal < mMinPercentOfTotal)
				continue;
			list.add(sipper);
		}

		LeoLog.d(TAG, "list size = " + list.size());

		if (list.size() <= 1) {
			return getAppListCpuTime();
		}

		return list;
	}

	private long getAppProcessTime(int pid) {
		FileInputStream in = null;
		String ret = null;
		try {
			in = new FileInputStream("/proc/" + pid + "/stat");
			byte[] buffer = new byte[1024];
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			int len = 0;
			while ((len = in.read(buffer)) != -1) {
				os.write(buffer, 0, len);
			}
			ret = os.toString();
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		if (ret == null) {
			return 0;
		}

		String[] s = ret.split(" ");
		if (s == null || s.length < 17) {
			return 0;
		}

		final long utime = string2Long(s[13]);
		final long stime = string2Long(s[14]);
		final long cutime = string2Long(s[15]);
		final long cstime = string2Long(s[16]);

		return utime + stime + cutime + cstime;
	}

	private long string2Long(String s) {
		try {
			return Long.parseLong(s);
		} catch (NumberFormatException e) {
		}
		return 0;
	}

	private List<BatteryComsuption> getAppListCpuTime() {
		testType = 2;
		final List<BatteryComsuption> list = new ArrayList<BatteryComsuption>();

		long totalTime = 0;
		ActivityManager am = (ActivityManager) mContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();

		HashMap<String, BatteryComsuption> templist = new HashMap<String, BatteryComsuption>();

		// stone: magic number 5, to be decided
		if (runningApps.size() > 5) {
			for (RunningAppProcessInfo info : runningApps) {
				final long time = getAppProcessTime(info.pid);
				String[] pkgNames = info.pkgList;
				if (pkgNames == null) {
					if (templist.containsKey(info.processName)) {
						BatteryComsuption sipper = templist.get(info.processName);
						sipper.setValue(sipper.getValue() + time);
					} else {
						templist.put(info.processName, new BatteryComsuption(
								mContext, info.processName, time));
					}
					totalTime += time;
				} else {
					for (String pkgName : pkgNames) {
						if (templist.containsKey(pkgName)) {
							BatteryComsuption sipper = templist.get(pkgName);
							sipper.setValue(sipper.getValue() + time);
						} else {
							templist.put(pkgName, new BatteryComsuption(mContext,
									pkgName, time));
						}
						totalTime += time;
					}
				}
			}
		}else{
			HashMap<String, Long> tmpConsumtion = new HashMap<String, Long>();
			List<ProcessAdj> allProcesses =  getProcessWithPS();
			for (ProcessAdj processAdj: allProcesses) {
				long consumption = getAppProcessTime(processAdj.pid);
				if (tmpConsumtion.containsKey(processAdj.pkg)) {
					tmpConsumtion.put(processAdj.pkg, tmpConsumtion.get(processAdj.pkg)+consumption);
				} else {
					tmpConsumtion.put(processAdj.pkg, consumption);
				}
			}

			templist.clear();
			totalTime = 0;
			for (String key: tmpConsumtion.keySet()) {
				LeoLog.d(TAG, key + " : " + tmpConsumtion.get(key));
				long time = tmpConsumtion.get(key);
				totalTime += time;
				BatteryComsuption batteryComsuption = new BatteryComsuption(mContext, key, time);
				if (batteryComsuption != null &&
						batteryComsuption.getName() != null &&
						batteryComsuption.getIcon() != null) {
					templist.put(key, batteryComsuption);
				}
			}
		}

		if (totalTime == 0)
			totalTime = 1;

		list.addAll(templist.values());
		for (int i = list.size() - 1; i >= 0; i--) {
			BatteryComsuption sipper = list.get(i);
			double percentOfTotal = sipper.getValue() * 100 / totalTime;
			if (percentOfTotal < mMinPercentOfTotal) {
				list.remove(i);
			} else {
				sipper.setPercent(percentOfTotal);
			}
		}

		Collections.sort(list, new Comparator<BatteryComsuption>() {
			@Override
			public int compare(BatteryComsuption object1,
					BatteryComsuption object2) {
				double d1 = object1.getPercentOfTotal();
				double d2 = object2.getPercentOfTotal();
				if (d1 - d2 < 0) {
					return 1;
				} else if (d1 - d2 > 0) {
					return -1;
				} else {
					return 0;
				}
			}
		});

		return list;
	}


	private static class BatteryProcessFilter implements ProcessDetector.ProcessFilter {

		@Override
		public boolean filterProcess(ProcessAdj processAdj) {
			if (processAdj.pkg.equals(PS) || processAdj.pkg.equals(SHELL)) {
				return true;
			}
			if (AppUtil.belongToLeoFamily(processAdj.pkg)) {
				return true;
			}
			// only check app stuff
			if (processAdj.user.startsWith(APP_PREFIX_1) || processAdj.user.startsWith(APP_PREFIX_2)) {
				return false;
			}
			return true;
		}

	}

	public List<ProcessAdj> getProcessWithPS() {

		Process p = null;
		InputStream is = null;
		BufferedReader br = null;
		ArrayList<ProcessAdj> allProcess = new ArrayList<ProcessAdj>();
		try {
			p = Runtime.getRuntime().exec(PS);
			is = p.getInputStream();

			br = new BufferedReader(new InputStreamReader(is));
			String line = null;

			int zygoteId = 0;
			while ((line = br.readLine()) != null) {
				ProcessAdj pair = getProcessAdjByFormatedLine(line, zygoteId);
				if (pair != null) {
					allProcess.add(pair);
				}
			}
		} catch (Exception e) {
			LeoLog.e(TAG, "getForegroundProcess ex  " + e.getMessage());
		} finally {
			IoUtils.closeSilently(br);
			IoUtils.closeSilently(is);

			if (p != null) {
				p.destroy();
			}
		}

		return allProcess;
	}



	protected ProcessAdj getProcessAdjByFormatedLine(String line, int zygoteId) {
		if (TextUtils.isEmpty(line)) return null;

		Pattern pattern = Pattern.compile(REGEX_SPACE);
		Matcher matcher = pattern.matcher(line);

		if (matcher.find()) {
			line = matcher.replaceAll(",");
		}

		if (DBG) {
			LeoLog.i(TAG, line);
		}

		String[] array = line.split(",");
		if (array == null || array.length == 0) return null;

		if (array.length <= INDEX_PROCESS_NAME) return null;

		if (DBG) {
			LeoLog.i(TAG, "array length: " + array.length);
		}

		ProcessAdj processAdj = new ProcessAdj();

		String user = array[INDEX_USER];
		processAdj.user = user;

		String ppidStr = array[INDEX_PPID];
		int ppid = Integer.parseInt(ppidStr);
		if (zygoteId != 0 && ppid != zygoteId) return null;


		String processName = array[INDEX_PROCESS_NAME];
		processAdj.pkg = processName;
		for (ProcessDetector.ProcessFilter filter : getBatteryFilters()) {
			if (filter.filterProcess(processAdj)) return null;
		}

		String processIdStr = array[INDEX_PID];
		if (TextUtils.isEmpty(processName)) return null;

		int processId = 0;
		try {
			processId = Integer.parseInt(processIdStr);
		} catch (Exception e) {
		}
		if (processId == 0) return null;

		int oomAdj = 0; // getOomScoreAdj(processId);

		// 解决一些前台进程的进程名是子进程的问题,com.mobisystems.office:browser
		if (processName.contains(":")) {
			processName = processName.substring(0, processName.indexOf(":"));
		}
		processAdj.oomAdj = oomAdj;
		processAdj.pid = processId;
		processAdj.pkg = processName;
		processAdj.ppid = ppid;

		if (DBG) {
			LeoLog.i(TAG, "inner: " + processAdj.toString());
		}

		return processAdj;
	}

	private void processMiscUsage() {
		final int which = mStatsType;
		long uSecTime = SystemClock.elapsedRealtime() * 1000;
		final long uSecNow = mStats.computeBatteryRealtime(uSecTime, which);
		final long timeSinceUnplugged = uSecNow;
		if (DEBUG) {
			Log.i(TAG, "Uptime since last unplugged = "
					+ (timeSinceUnplugged / 1000));
		}

		addPhoneUsage(uSecNow);
		addScreenUsage(uSecNow);
		addWiFiUsage(uSecNow);
		addBluetoothUsage(uSecNow);
		addIdleUsage(uSecNow); // Not including cellular idle power
		addRadioUsage(uSecNow);
	}

	private void addPhoneUsage(long uSecNow) {
		long phoneOnTimeMs = mStats.getPhoneOnTime(uSecNow, mStatsType) / 1000;
		double phoneOnPower = mPowerProfile
				.getAveragePower(PowerProfile.POWER_RADIO_ACTIVE)
				* phoneOnTimeMs / 1000;
		addEntry(DrainType.PHONE, phoneOnTimeMs, phoneOnPower);
	}

	private void addScreenUsage(long uSecNow) {
		double power = 0;
		long screenOnTimeMs = mStats.getScreenOnTime(uSecNow, mStatsType) / 1000;
		power += screenOnTimeMs
				* mPowerProfile.getAveragePower(PowerProfile.POWER_SCREEN_ON);
		final double screenFullPower = mPowerProfile
				.getAveragePower(PowerProfile.POWER_SCREEN_FULL);
		for (int i = 0; i < BatteryStats.NUM_SCREEN_BRIGHTNESS_BINS; i++) {
			double screenBinPower = screenFullPower * (i + 0.5f)
					/ BatteryStats.NUM_SCREEN_BRIGHTNESS_BINS;
			long brightnessTime = mStats.getScreenBrightnessTime(i, uSecNow,
					mStatsType) / 1000;
			power += screenBinPower * brightnessTime;
			if (DEBUG) {
				LeoLog.i(TAG, "Screen bin power = " + (int) screenBinPower
						+ ", time = " + brightnessTime);
			}
		}
		power /= 1000; // To seconds
		addEntry(DrainType.SCREEN, screenOnTimeMs, power);
	}

	private void addWiFiUsage(long uSecNow) {
		if (!versionValid()) {// less than 2.3.3
			return;
		}

		long onTimeMs = mStats.getWifiOnTime(uSecNow, mStatsType) / 1000;
		long runningTimeMs = mStats.getGlobalWifiRunningTime(uSecNow,
				mStatsType) / 1000;
		if (DEBUG)
			LeoLog.i(TAG, "WIFI runningTime=" + runningTimeMs
					+ " app runningTime=" + mAppWifiRunning);
		runningTimeMs -= mAppWifiRunning;
		if (runningTimeMs < 0)
			runningTimeMs = 0;
		double wifiPower = (onTimeMs * 0
				* mPowerProfile.getAveragePower(PowerProfile.POWER_WIFI_ON) + runningTimeMs
				* mPowerProfile.getAveragePower(PowerProfile.POWER_WIFI_ON)) / 1000;
		if (DEBUG)
			LeoLog.i(TAG, "WIFI power=" + wifiPower + " from procs=" + mWifiPower);
		BatteryComsuption bs = addEntry(DrainType.WIFI, runningTimeMs,
				wifiPower + mWifiPower);
		aggregateSippers(bs, mWifiSippers, "WIFI");
	}

	private void addBluetoothUsage(long uSecNow) {
		long btOnTimeMs = mStats.getBluetoothOnTime(uSecNow, mStatsType) / 1000;
		double btPower = btOnTimeMs
				* mPowerProfile
						.getAveragePower(PowerProfile.POWER_BLUETOOTH_ON)
				/ 1000;
		int btPingCount = mStats.getBluetoothPingCount();
		btPower += (btPingCount * mPowerProfile
				.getAveragePower(PowerProfile.POWER_BLUETOOTH_AT_CMD)) / 1000;
		BatteryComsuption bs = addEntry(DrainType.BLUETOOTH, btOnTimeMs,
				btPower + mBluetoothPower);
		aggregateSippers(bs, mBluetoothSippers, "Bluetooth");
	}

	private void addIdleUsage(long uSecNow) {
		long idleTimeMs = (uSecNow - mStats
				.getScreenOnTime(uSecNow, mStatsType)) / 1000;
		double idlePower = (idleTimeMs * mPowerProfile
				.getAveragePower(PowerProfile.POWER_CPU_IDLE)) / 1000;
		addEntry(DrainType.IDLE, idleTimeMs, idlePower);
	}

	private void addRadioUsage(long uSecNow) {
		double power = 0;
		// final int BINS = BatteryStats.NUM_SIGNAL_STRENGTH_BINS;
		final int BINS = SignalStrength.NUM_SIGNAL_STRENGTH_BINS;
		long signalTimeMs = 0;
		for (int i = 0; i < BINS; i++) {
			long strengthTimeMs = mStats.getPhoneSignalStrengthTime(i, uSecNow,
					mStatsType) / 1000;
			power += strengthTimeMs
					/ 1000
					* mPowerProfile.getAveragePower(
							PowerProfile.POWER_RADIO_ON, i);
			signalTimeMs += strengthTimeMs;
		}
		long scanningTimeMs = mStats.getPhoneSignalScanningTime(uSecNow,
				mStatsType) / 1000;
		power += scanningTimeMs
				/ 1000
				* mPowerProfile
						.getAveragePower(PowerProfile.POWER_RADIO_SCANNING);
		BatteryComsuption bs = addEntry(DrainType.CELL, signalTimeMs, power);
		if (signalTimeMs != 0) {
			bs.noCoveragePercent = mStats.getPhoneSignalStrengthTime(0,
					uSecNow, mStatsType) / 1000 * 100.0 / signalTimeMs;
		}
	}

	private void aggregateSippers(BatteryComsuption bs,
			List<BatteryComsuption> from, String tag) {
		for (int i = 0; i < from.size(); i++) {
			BatteryComsuption wbs = from.get(i);
			if (DEBUG)
				LeoLog.i(TAG, tag + " adding sipper " + wbs + ": cpu="
						+ wbs.cpuTime);
			bs.cpuTime += wbs.cpuTime;
			bs.gpsTime += wbs.gpsTime;
			bs.wifiRunningTime += wbs.wifiRunningTime;
			bs.cpuFgTime += wbs.cpuFgTime;
			bs.wakeLockTime += wbs.wakeLockTime;
			bs.tcpBytesReceived += wbs.tcpBytesReceived;
			bs.tcpBytesSent += wbs.tcpBytesSent;
		}
	}

	private BatteryComsuption addEntry(DrainType drainType, long time,
			double power) {
		if (power > mMaxPower)
			mMaxPower = power;
		mTotalPower += power;
		BatteryComsuption bs = new BatteryComsuption(mContext, drainType, null,
				new double[] { power });
		bs.usageTime = time;
		mUsageList.add(bs);
		return bs;
	}

	private boolean versionValid() {
		return android.os.Build.VERSION.SDK_INT >= 10;// less than 2.3.3
	}

	private void processAppUsage() {
//		SensorManager sensorManager = (SensorManager) mContext
//				.getSystemService(Context.SENSOR_SERVICE);
//		final int which = mStatsType;
//		final int speedSteps = mPowerProfile.getNumSpeedSteps();
//		final double[] powerCpuNormal = new double[speedSteps];
//		final long[] cpuSpeedStepTimes = new long[speedSteps];
//		for (int p = 0; p < speedSteps; p++) {
//			powerCpuNormal[p] = mPowerProfile.getAveragePower(
//					PowerProfile.POWER_CPU_ACTIVE, p);
//		}
//
//		final double averageCostPerByte = getAverageDataCost();
//		long uSecTime = mStats.computeBatteryRealtime(
//				SystemClock.elapsedRealtime() * 1000, which);
//		mStatsPeriod = uSecTime;
//		SparseArray<? extends Uid> uidStats = mStats.getUidStats();
//		final int NU = uidStats.size();
//		for (int iu = 0; iu < NU; iu++) {
//			Uid u = uidStats.valueAt(iu);
//			double power = 0;
//			double highestDrain = 0;
//			String packageWithHighestDrain = null;
//			Map<String, ? extends BatteryStats.Uid.Proc> processStats = u
//					.getProcessStats();
//			long cpuTime = 0;
//			long cpuFgTime = 0;
//			long wakelockTime = 0;
//			long gpsTime = 0;
//			if (processStats.size() > 0) {
//				// 1, Process CPU time
//				for (Map.Entry<String, ? extends BatteryStats.Uid.Proc> ent : processStats
//						.entrySet()) {
//					if (DEBUG)
//						LeoLog.i(TAG, "Process name = " + ent.getKey());
//
//					Uid.Proc ps = ent.getValue();
//					final long userTime = ps.getUserTime(which);
//					final long systemTime = ps.getSystemTime(which);
//					final long foregroundTime = ps.getForegroundTime(which);
//					cpuFgTime += foregroundTime * 10; // convert to millis
//					final long tmpCpuTime = (userTime + systemTime) * 10; // convert
//																		
//					int totalTimeAtSpeeds = 0;
//					// Get the total first
//					for (int step = 0; step < speedSteps; step++) {
//						cpuSpeedStepTimes[step] = ps.getTimeAtCpuSpeedStep(
//								step, which);
//						totalTimeAtSpeeds += cpuSpeedStepTimes[step];
//					}
//					if (totalTimeAtSpeeds == 0)
//						totalTimeAtSpeeds = 1;
//					// Then compute the ratio of time spent at each speed
//					double processPower = 0;
//					for (int step = 0; step < speedSteps; step++) {
//						double ratio = (double) cpuSpeedStepTimes[step]
//								/ totalTimeAtSpeeds;
//						processPower += ratio * tmpCpuTime
//								* powerCpuNormal[step];
//					}
//					cpuTime += tmpCpuTime;
//					power += processPower;
//					if (packageWithHighestDrain == null
//							|| packageWithHighestDrain.startsWith("*")) {
//						highestDrain = processPower;
//						packageWithHighestDrain = ent.getKey();
//					} else if (highestDrain < processPower
//							&& !ent.getKey().startsWith("*")) {
//						highestDrain = processPower;
//						packageWithHighestDrain = ent.getKey();
//					}
//				}
//			}
//			if (cpuFgTime > cpuTime) {
//				if (DEBUG && cpuFgTime > cpuTime + 10000) {
//					LeoLog.i(TAG,
//							"WARNING! Cputime is more than 10 seconds behind Foreground time");
//				}
//				cpuTime = cpuFgTime; // Statistics may not have been gathered
//										// yet.
//			}
//			power /= 1000;
//
//			// 2, Process wake lock usage
//			Map<String, ? extends BatteryStats.Uid.Wakelock> wakelockStats = u
//					.getWakelockStats();
//			for (Map.Entry<String, ? extends BatteryStats.Uid.Wakelock> wakelockEntry : wakelockStats
//					.entrySet()) {
//				Uid.Wakelock wakelock = wakelockEntry.getValue();
//				// Only care about partial wake locks since full wake locks are
//				// canceled when the user turns the screen off.
//				BatteryStats.Timer timer = wakelock
//						.getWakeTime(BatteryStats.WAKE_TYPE_PARTIAL);
//				if (timer != null) {
//					wakelockTime += timer.getTotalTimeLocked(uSecTime, which);
//				}
//			}
//			wakelockTime /= 1000; // convert to millis
//			// Add cost of holding a wake lock
//			power += (wakelockTime * mPowerProfile
//					.getAveragePower(PowerProfile.POWER_CPU_AWAKE)) / 1000;
//
//			// 3, Add cost of data traffic
//			long tcpBytesReceived = u.getTcpBytesReceived(mStatsType);
//			long tcpBytesSent = u.getTcpBytesSent(mStatsType);
//			power += (tcpBytesReceived + tcpBytesSent) * averageCostPerByte;
//
//			// 4, Add cost of keeping WIFI running.
//			if (versionValid()) {
//				long wifiRunningTimeMs = u.getWifiRunningTime(uSecTime, which) / 1000;
//				mAppWifiRunning += wifiRunningTimeMs;
//				power += (wifiRunningTimeMs * mPowerProfile
//						.getAveragePower(PowerProfile.POWER_WIFI_ON)) / 1000;
//			}
//
//			// 5, Process Sensor usage
//			Map<Integer, ? extends BatteryStats.Uid.Sensor> sensorStats = u
//					.getSensorStats();
//			for (Map.Entry<Integer, ? extends BatteryStats.Uid.Sensor> sensorEntry : sensorStats
//					.entrySet()) {
//				Uid.Sensor sensor = sensorEntry.getValue();
//				int sensorType = sensor.getHandle();
//				BatteryStats.Timer timer = sensor.getSensorTime();
//				long sensorTime = timer.getTotalTimeLocked(uSecTime, which) / 1000;
//				double multiplier = 0;
//				switch (sensorType) {
//				case Uid.Sensor.GPS:
//					multiplier = mPowerProfile
//							.getAveragePower(PowerProfile.POWER_GPS_ON);
//					gpsTime = sensorTime;
//					break;
//				default:
//					android.hardware.Sensor sensorData = sensorManager
//							.getDefaultSensor(sensorType);
//					if (sensorData != null) {
//						multiplier = sensorData.getPower();
//						if (DEBUG) {
//							LeoLog.i(TAG, "Got sensor " + sensorData.getName()
//									+ " with power = " + multiplier);
//						}
//					}
//				}
//				power += (multiplier * sensorTime) / 1000;
//			}
//
//			if (DEBUG)
//				LeoLog.i(TAG, "UID " + u.getUid() + ": power=" + power);
//
//			// Add the app to the list if it is consuming power
//			if (power != 0) {
//				BatteryComsuption app = new BatteryComsuption(mContext,
//						DrainType.APP, u, new double[] { power });
//				app.cpuTime = cpuTime;
//				app.gpsTime = gpsTime;
//				// app.wifiRunningTime = wifiRunningTimeMs;
//				app.cpuFgTime = cpuFgTime;
//				app.wakeLockTime = wakelockTime;
//				app.tcpBytesReceived = tcpBytesReceived;
//				app.tcpBytesSent = tcpBytesSent;
//				if (u.getUid() == Process.WIFI_UID) {
//					mWifiSippers.add(app);
//				} else if (u.getUid() == Process.BLUETOOTH_GID) {
//					mBluetoothSippers.add(app);
//				} else {
//					mUsageList.add(app);
//				}
//			}
//			if (u.getUid() == Process.WIFI_UID) {
//				mWifiPower += power;
//			} else if (u.getUid() == Process.BLUETOOTH_GID) {
//				mBluetoothPower += power;
//			} else {
//				if (power > mMaxPower)
//					mMaxPower = power;
//				mTotalPower += power;
//			}
//
//			if (DEBUG)
//				LeoLog.i(TAG, "Added power = " + power);
//		}
	}
	
	private double getAverageDataCost() {
//		final long WIFI_BPS = 1000000; // TODO: Extract average bit rates from
//										// system
//		final long MOBILE_BPS = 200000; // TODO: Extract average bit rates from
//										// system
//		final double WIFI_POWER = mPowerProfile
//				.getAveragePower(PowerProfile.POWER_WIFI_ACTIVE) / 3600;
//		final double MOBILE_POWER = mPowerProfile
//				.getAveragePower(PowerProfile.POWER_RADIO_ACTIVE) / 3600;
//
//		// �����ֽ�����
//		final long mobileData = mStats.getMobileTcpBytesReceived(mStatsType)
//				+ mStats.getMobileTcpBytesSent(mStatsType);
//		final long wifiData = mStats.getTotalTcpBytesReceived(mStatsType)
//				+ mStats.getTotalTcpBytesSent(mStatsType) - mobileData;
//		// ����ʱ��(����)
//		final long radioDataUptimeMs = mStats.getRadioDataUptime() / 1000;
//		// ������(bps)
//		final long mobileBps = radioDataUptimeMs != 0 ? mobileData * 8 * 1000
//				/ radioDataUptimeMs : MOBILE_BPS;
//
//		// ÿ��ÿ�ֽڵ�����
//		double mobileCostPerByte = MOBILE_POWER / (mobileBps / 8);
//		// wifiÿ��ÿ�ֽڵ�����
//		double wifiCostPerByte = WIFI_POWER / (WIFI_BPS / 8);
//
//		// ƽ������
//		if (wifiData + mobileData != 0) {
//			return (mobileCostPerByte * mobileData + wifiCostPerByte * wifiData)
//					/ (mobileData + wifiData);
//		} else {
//			return 0;
//		}
	    return 0;
	}

	private BatteryStatsImpl load() {
		if (mBatteryInfo == null)
			return null;
		BatteryStatsImpl mStats = null;
		try {

			byte[] data = mBatteryInfo.getStatistics();
			Parcel parcel = Parcel.obtain();
			parcel.unmarshall(data, 0, data.length);
			parcel.setDataPosition(0);
			mStats = BatteryStatsImpl.CREATOR.createFromParcel(parcel);
			if (versionValid()) {
				mStats.distributeWorkLocked(BatteryStats.STATS_SINCE_CHARGED);
			}
		} catch (Exception e) {
			LeoLog.e(TAG, "RemoteException:", e);
		} catch (Error e) {
			LeoLog.e(TAG, "Error:", e);
		}
		return mStats;
	}
}
