
package com.leo.appmaster.applocker.model;

import com.leo.appmaster.applocker.DayView;
import com.leo.appmaster.utils.LeoLog;

public class TimeLock {
    public long id;
    public String name;
    public TimePoint time;
    public int lockModeId;
    public String lockModeName;
    public RepeatTime repeatMode;
    public boolean using;
    public boolean selected;

    public TimeLock() {

    }

    public TimeLock(long id, String name, String time, int lockModeId, String lockModeName,
            byte repeatMode) {
        super();
        this.id = id;
        this.name = name;
        this.time = new TimePoint(time);
        this.lockModeId = lockModeId;
        this.lockModeName = lockModeName;
        this.repeatMode = new RepeatTime(repeatMode);
    }

    public TimeLock(long id, String name, TimePoint time, int lockModeId, String lockModeName,
            byte repeatMode) {
        super();
        this.name = name;
        this.time = time;
        this.lockModeId = lockModeId;
        this.lockModeName = lockModeName;
        this.repeatMode = new RepeatTime(repeatMode);
    }

    public TimeLock(long id, String name, TimePoint time, int lockModeId, RepeatTime repeatMode) {
        super();
        this.name = name;
        this.time = time;
        this.lockModeId = lockModeId;
        this.repeatMode = repeatMode;
    }

    public static class TimePoint {
        public short hour;
        public short minute;

        public TimePoint(String timePoint) {
            if (timePoint != null) {
                int sIndex = timePoint.indexOf(":");
                if (sIndex != -1) {
                    hour = (short) Integer.parseInt(timePoint.substring(0, sIndex));
                    minute = (short) Integer.parseInt(timePoint.substring(sIndex + 1));
                }
            }
        }

        public TimePoint(short hour, short minute) {
            this.hour = hour;
            this.minute = minute;
        }

        @Override
        public String toString() {
            String resault;
            String temp = hour + "";
            if (temp.length() == 1) {
                temp = "0" + temp;
            }
            resault = temp + ":";

            temp = minute + "";
            if (temp.length() == 1) {
                temp = "0" + temp;
            }
            resault += temp;
            return resault;
        }
    }

    public static class RepeatTime {
        private static final String TAG = "RepeatTime";
        public byte repeatSet = 0;

        public RepeatTime(byte repeatSet) {

            this.repeatSet = repeatSet;
        }

        public void addRepeatPoint(byte dayOfWeek) {
            if (dayOfWeek < 1 || dayOfWeek > 7) {
                LeoLog.e(TAG, "addRepeatPoint: can not < 1 or > 7");
                return;
            }

            repeatSet |= 1 << (dayOfWeek - 1);
        }

        public void removeRepatePoint(byte dayOfWeek) {
            if (dayOfWeek < 1 || dayOfWeek > 7) {
                LeoLog.e(TAG, "removeRepatePoint: can not < 0 or > 7");
                return;
            }

            repeatSet &= ~(1 << (dayOfWeek - 1));

        }

        public boolean containDayOfWeek(byte dayOfWeek) {
            if (dayOfWeek < 1 || dayOfWeek > 7) {
                LeoLog.e(TAG, "containDayOfWeek: can not < 0 or > 7");
                return false;
            }

            return (repeatSet & (1 << (dayOfWeek - 1))) != 0;
        }

        public byte[] getAllRepeatDayOfWeek() {
            String repeatPoints = "";
            if (containDayOfWeek((byte) 1)) {
                repeatPoints += "1";
            }
            if (containDayOfWeek((byte) 2)) {
                repeatPoints += "2";
            }
            if (containDayOfWeek((byte) 3)) {
                repeatPoints += "3";
            }
            if (containDayOfWeek((byte) 4)) {
                repeatPoints += "4";
            }
            if (containDayOfWeek((byte) 5)) {
                repeatPoints += "5";
            }
            if (containDayOfWeek((byte) 6)) {
                repeatPoints += "6";
            }
            if (containDayOfWeek((byte) 7)) {
                repeatPoints += "7";
            }
            byte[] repeats = new byte[repeatPoints.length()];
            for (int i = 0; i < repeats.length; i++) {
                repeats[i] = (byte) Integer.parseInt(repeatPoints.substring(i, i + 1));
            }
            return repeats;
        }

        public int toInt() {
            return repeatSet;
        }

        public String toString() {
            String resault = "";
            byte[] days = getAllRepeatDayOfWeek();
            if (days.length == 0) {
                return "";
            } else {
                for (byte b : days) {
                    resault += DayView.days[b - 1] + ", ";
                }
                return resault.substring(0, resault.length() - 2);
            }
        }
    }
}
