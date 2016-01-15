package com.leo.appmaster.battery;

import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.utils.LeoLog;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by stone on 16/1/15.
 */
public class RemainTimeHelper {

    private static final String TAG = "RemainTimeHelper";

    // 当充电数据校验了1000次之后算稳定了，或者更多
    private static final int MAX_ESTIMATE_COUNT = 1000;
    // 计算电量步长
    private static final int STEP = 3;

    private static final String RT_PREFERENCE_KEY = "battery_charge_time_for_";
    private static final int MILLES_IN_SECOND = 1000;

    // TODO 需要比较准确的初始化值
    private static final int[] INITIAL_DATA = {
            100, 100, 100, 100, 100, 100, 100, 100, 100, 100,
            100, 100, 100, 100, 100, 100, 100, 100, 100, 100,
            100, 100, 100, 100, 100, 100, 100, 100, 100, 100,
            100, 100, 100, 100, 100, 100, 100, 100, 100, 100,
            100, 100, 100, 100, 100, 100, 100, 100, 100, 100,

            100, 100, 100, 100, 100, 100, 100, 100, 100, 100,
            100, 100, 100, 100, 100, 100, 100, 100, 100, 100,
            100, 100, 100, 100, 100, 100, 100, 100, 100, 100,
            100, 100, 100, 100, 100, 100, 100, 100, 100, 100,
            100, 100, 100, 100, 100, 100, 100, 100, 100, 0
    };

    private ArrayList<PeriodUnit> mPreferenceList;
    private HashMap<Integer, Integer> mCurrentMap;

    private int mScale;

    static class PeriodUnit{
        // 充电到下一level的时间，单位为秒
        public int second;
        // 矫正次数
        public int count = 0;
        public String pack(){
            return second+","+count;
        }
        public static PeriodUnit fromString(String data){
            String[] elements = data.split(",");
            PeriodUnit pu = new PeriodUnit();
            try {
                pu.second = Integer.parseInt(elements[0]);
                pu.count = Integer.parseInt(elements[1]);
            } catch (NumberFormatException e) {
                pu = null;
            }
            return pu;
        }
    }

    public RemainTimeHelper(int scale) {
        mScale = scale;
        mPreferenceList = new ArrayList<PeriodUnit>();
        loadPreferenceList(scale);
        mCurrentMap = new HashMap<Integer, Integer>();
    }

    private void loadPreferenceList(int scale) {
        for (int i=0; i<scale; i++) {
            PeriodUnit pu = new PeriodUnit();
            String data = PreferenceTable.getInstance().getString(RT_PREFERENCE_KEY + i);
            if (data == null || data.length()<=0) {
                pu.count = 1;
                pu.second = INITIAL_DATA[i];
            } else {
                pu = PeriodUnit.fromString(data);
            }
            mPreferenceList.add(pu);
        }
    }

    private void updatePreferenceList(int level, int period) {
        if (mPreferenceList.get(level).count <= MAX_ESTIMATE_COUNT) {
            PeriodUnit pu = mPreferenceList.get(level);
            pu.second = (pu.second*pu.count+period)/(pu.count+1);
            pu.count += 1;
            PreferenceTable.getInstance().putString(RT_PREFERENCE_KEY+level, pu.pack());
        }
    }

    /**
     * 计算仍需充电时间，切记输入为毫秒输出为秒
     * @param l1 上一个battery event的电池level
     * @param l2 本次battery event的电池level
     * @param period 从l1到l2所需要的时间，单位毫秒
     * @return 返回预估的需充电时间，单位为秒
     */
    public int getEstimatedTime(int l1, int l2, long period) {
        int periodInSecond = (int) (period/MILLES_IN_SECOND);
        LeoLog.d(TAG, "l1="+l1+"; l2="+l2+"; period_sec="+periodInSecond);
        if (periodInSecond == 0 || l1 < 0 || l1 >= l2) {
            int remainTime = getTimeFromPreference(l2);
            LeoLog.d(TAG, "preference remainTime = " + remainTime);
            return remainTime;
        } else {
            int min = Math.min(0, l2-STEP);
            int totalPeriod = 0;
            for (int i=min;i<l2;i++) {
                if (i==l2-1) {
                    totalPeriod += periodInSecond;
                    continue;
                }
                if (mCurrentMap.get(i) != null) {
                    totalPeriod += mCurrentMap.get(i);
                } else {
                    totalPeriod += mPreferenceList.get(i).second;
                }
            }
            if (l2-l1==1) {
                // 满足这个才有参考意义
                updatePreferenceList(l1, periodInSecond);
            }
            int remainTime = (mScale-l2)*totalPeriod/(l2-min);
            LeoLog.d(TAG, "calculated remainTime = " + remainTime);
            return remainTime;
        }
    }

    private int getTimeFromPreference(int level) {
        if (level < mPreferenceList.size()) {
            int time = 0;
            for (int i=level;i<mPreferenceList.size();i++) {
                time += mPreferenceList.get(i).second;
            }
            return time;
        } else {
            return (mScale-level)*mPreferenceList.get(0).second;
        }
    }

}
