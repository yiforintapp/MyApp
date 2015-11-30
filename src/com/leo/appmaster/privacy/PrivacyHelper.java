
package com.leo.appmaster.privacy;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Pair;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.service.StatusBarEventService;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.SecurityScoreEvent;
import com.leo.appmaster.home.HomeColor;
import com.leo.appmaster.mgr.IntrudeSecurityManager;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.Manager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.PrivacyDataManager;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.sdk.push.PushNotification;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;
import com.leo.appmaster.utils.TimeUtil;
import com.leo.appmater.globalbroadcast.LeoGlobalBroadcast;
import com.leo.appmater.globalbroadcast.ScreenOnOffListener;

public class PrivacyHelper implements Manager.SecurityChangeListener {
    private static final String TAG = "PrivacyHelper";

    private static final int CHECK_TIME = 60 * 1000;

    public enum Level {
        LEVEL_ONE(100, 100),
        LEVEL_TWO(99, 81),
        LEVEL_THREE(61, 80),
        LEVEL_FOUR(41, 60),
        LEVEL_FIVE(21, 40),
        LEVEL_SIX(0, 20);

        public int top;
        public int bottom;

        private Level(int top, int bottom) {
            this.top = top;
            this.bottom = bottom;
        }
    }

    public static final int PRIVACY_NONE = -1;
    public static final int PRIVACY_APP_LOCK = 0;
    public static final int PRIVACY_HIDE_PIC = 1;
    public static final int PRIVACY_HIDE_VID = 2;

    private static final String[] SCORE_MGR = {
            MgrContext.MGR_APPLOCKER,
            MgrContext.MGR_PRIVACY_DATA,
            MgrContext.MGR_INTRUDE_SECURITY,
            MgrContext.MGR_LOST_SECURITY
    };

    private static PrivacyHelper sInstance;

    private Context mContext;

    private Level mPrivacyLevel = Level.LEVEL_ONE;

    private ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private int mSecurityScore;
    private HashMap<String, Integer> mScoreMap;
    private HashMap<String, Integer> mDecScoreMap;

    private Runnable mCheckScoreTask;
    private Future mCheckScoreFuture;

    private Future mDalayNotifyFuture;
    private Runnable mDelayNotifyTask;

    private long mLastScanTs;

    private PrivacyHelper(Context context) {
        mContext = context.getApplicationContext();
        mScoreMap = new HashMap<String, Integer>();
        mDecScoreMap = new HashMap<String, Integer>();
        for (String mgr : SCORE_MGR) {
            Manager manager = MgrContext.getManager(mgr);
            if (manager != null) {
                manager.registerSecurityListener(this);
                mScoreMap.put(mgr, 0);
            }
            mDecScoreMap.put(mgr, 0);
        }
    }

    public static synchronized PrivacyHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new PrivacyHelper(context);
        }
        return sInstance;
    }

    /**
     * 重置后台减少的分值，进入Home调用
     */
    public void resetDecScore() {
        for (String s : mDecScoreMap.keySet()) {
            mDecScoreMap.put(s, 0);
        }
    }

    /**
     * 开启定时扫描逻辑
     *
     * @param initialDelay
     */
    public void startIntervalScanner(long initialDelay) {
        LeoLog.i(TAG, "startIntervalScanner......initialDelay: " + initialDelay);
        if (mCheckScoreFuture != null) {
            mCheckScoreFuture.cancel(false);
        }
        mCheckScoreTask = new ScoreTimerTask();
        mCheckScoreFuture = ThreadManager.getAsyncExecutor().scheduleAtFixedRate(
                mCheckScoreTask, initialDelay, CHECK_TIME, TimeUnit.MILLISECONDS);
    }

    /**
     * 停止定时扫描逻辑
     */
    public void stopIntervalScanner() {
        LeoLog.i(TAG, "stopIntervalScanner......");
        if (mCheckScoreFuture != null) {
            mCheckScoreFuture.cancel(false);
            mCheckScoreFuture = null;
            mCheckScoreTask = null;
        }
    }

    /**
     * 执行一次扫描
     */
    public void scanOneTimeSilenty() {
        LeoLog.i(TAG, "scanOneTimeSilenty......");
        long lastScanTs = mLastScanTs;
        long currentTs = System.currentTimeMillis();
        if (currentTs - lastScanTs > CHECK_TIME || currentTs < lastScanTs) {
            // 1分钟之后，或者时间往前调整，需要扫描
            if (mCheckScoreTask == null) {
                mCheckScoreTask = new ScoreTimerTask();
            }
            ThreadManager.executeOnAsyncThread(mCheckScoreTask);
        } else {
            LeoLog.i(TAG, "scanOneTimeSilenty, interval not hit, so donot scan......");
        }
    }

    public void caculateSecurityScore() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int totalScore = 0;
                IntrudeSecurityManager ism = null;
                for (String mgr : SCORE_MGR) {
                    Manager manager = MgrContext.getManager(mgr);
                    if (mgr.equals(MgrContext.MGR_INTRUDE_SECURITY)) {
                        ism = (IntrudeSecurityManager) manager;
                    }
                    long start = 0;
                    if (manager != null) {
                        start = SystemClock.elapsedRealtime();
                        int score = manager.getSecurityScore();
                        LeoLog.i(TAG, "caculateSecurityScore, mgr: " + mgr + " | score: " + score +
                                " | cost: " + (SystemClock.elapsedRealtime() - start));
                        mScoreMap.put(mgr, score);
                        totalScore += score;
                    }
                }

                if (addIntruderSocreAutoly()) {
                    // 1.入侵者未开启   2.入侵者不可用   3.入侵者的分数还未增加
                    totalScore += ism.getMaxScore();
                    mScoreMap.put(MgrContext.MGR_INTRUDE_SECURITY, ism.getMaxScore());
                }

                if (mSecurityScore != totalScore) {
                    mSecurityScore = totalScore;
                    logScore();
                    LeoEventBus.getDefaultBus().post(new SecurityScoreEvent(totalScore));
                }
            }
        });
        startIntervalScanner(CHECK_TIME);
        ScreenOnOffListener listener = new ScreenOnOffListener() {
            @Override
            public void onScreenChanged(Intent intent) {
                super.onScreenChanged(intent);
                handleScreenIntent(intent);
            }
        };
        LeoGlobalBroadcast.registerBroadcastListener(listener);
    }

    private void handleScreenIntent(Intent intent) {
        if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
            stopIntervalScanner();
        } else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
            startIntervalScanner(0);
        }
    }

    @Override
    public void onSecurityChange(final String description, int securityScore) {
        synchronized (mScoreMap) {
            int oldScore = mScoreMap.get(description);
            LeoLog.i(TAG, "onSecurityChange, desc: " + description + " | oldScore :" + oldScore + " | score: " + securityScore);
            checkOrNotifyDecScore(description, securityScore);
            if (oldScore != securityScore) {
                mScoreMap.put(description, securityScore);
                mSecurityScore = calculateScore();
                logScore();

                LeoEventBus.getDefaultBus().post(new SecurityScoreEvent(mSecurityScore));
            }
        }
    }

    private boolean addIntruderSocreAutoly() {
        boolean intruderAdded = PreferenceTable.getInstance().getBoolean(
                PrefConst.KEY_INTRUDER_ADDED, false);
        IntrudeSecurityManager ism = (IntrudeSecurityManager) MgrContext.getManager(MgrContext.MGR_INTRUDE_SECURITY);
        return !ism.getIntruderMode() && !ism.getIsIntruderSecurityAvailable() && intruderAdded;
    }

    private class ScoreTimerTask implements Runnable {
        @Override
        public void run() {
            LeoLog.i(TAG, "ScoreTimerTask, start to check.");
            if (AppMasterApplication.getInstance().isHomeOnTopAndBackground()) {
                LeoLog.i(TAG, "home is ontop and background, so donot scan.");
                return;
            }
            long currentTs = System.currentTimeMillis();
            mLastScanTs = currentTs;
            for (String mgr : SCORE_MGR) {
                Manager manager = MgrContext.getManager(mgr);
                if (manager != null) {
                    int score = manager.getSecurityScore();
                    if (mgr.equals(MgrContext.MGR_INTRUDE_SECURITY) && addIntruderSocreAutoly()) {
                        IntrudeSecurityManager ism = (IntrudeSecurityManager) manager;
                        score = ism.getMaxScore();
                    }
                    LeoLog.i(TAG, "ScoreTimerTask, " + mgr + " score is changed.");
                    onSecurityChange(mgr, score);
                }
            }
        }
    }

    private class DelayTimerTask implements Runnable {

        @Override
        public void run() {
            long lastNotify = PreferenceTable.getInstance().getLong(PrefConst.KEY_NOTIFY_TIME, 0);
            long currentTs = System.currentTimeMillis();
            boolean isSameDay = TimeUtil.isSameDay(lastNotify, currentTs);
            if (!isSameDay) {
                LeoLog.i(TAG, "DelayTimerTask, start to do delay timer.");
                notifyDecreaseScore();
            } else {
                LeoLog.i(TAG, "DelayTimerTask, already notify.");
            }
        }
    }

    private void checkOrNotifyDecScore(String description, int securityScore) {
        long currentTs = System.currentTimeMillis();

        long lastDecTs = PreferenceTable.getInstance().getLong(PrefConst.KEY_DECREASE_TIME, 0);
        if (!TimeUtil.isSameDay(currentTs, lastDecTs)) {
            LeoLog.i(TAG, "checkOrNotifyDecScore, not in the same day.");
            // 跟之前的时间不在同一天，清0减少的分数
            resetDecScore();
            PreferenceTable.getInstance().putLong(PrefConst.KEY_DECREASE_TIME, currentTs);
        }
        int oldScore = mScoreMap.get(description);
        int decScore = oldScore - securityScore;

        decScore = decScore < 0 ? 0 : decScore;

        int oldDecScore = mDecScoreMap.get(description);
        decScore += oldDecScore;

        mDecScoreMap.put(description, decScore);

        int totalDecScore = 0;
        for (String key : mDecScoreMap.keySet()) {
            totalDecScore += mDecScoreMap.get(key);
        }

        int scoreAfterDec = mSecurityScore - decScore;
        LeoLog.i(TAG, "checkOrNotifyDecScore, totalDecScore: " + totalDecScore + ", scoreAfterDec: " + scoreAfterDec);
        if (scoreAfterDec < 70 || totalDecScore > 20) {
            long lastNotify = PreferenceTable.getInstance().getLong(PrefConst.KEY_NOTIFY_TIME, 0);
            AppMasterApplication application = AppMasterApplication.getInstance();
            boolean isForeground = application.isVisible();
            boolean isSameDay = TimeUtil.isSameDay(lastNotify, currentTs);
            LeoLog.i(TAG, "checkOrNotifyDecScore, isForeground: " + isForeground + ", isSameDay: " + isSameDay);
            if (!isForeground && !isSameDay) {
                int currHour = TimeUtil.getHourOfDay(currentTs);
                if (currHour > 7) {
                    // 当前时间大于7点钟，弹出通知
                    notifyDecreaseScore();
                } else {
                    // 当前时间小于7点钟，延时7个小时再弹通知
                    long hour7 = 7 * 60 * 60 * 1000;
                    if (mDalayNotifyFuture != null) {
                        mDalayNotifyFuture.cancel(false);
                    }
                    mDelayNotifyTask = new DelayTimerTask();
                    mDalayNotifyFuture = ThreadManager.getAsyncExecutor().schedule(
                            mDelayNotifyTask, hour7, TimeUnit.MILLISECONDS);
                }

            }

        }
    }

    private void notifyDecreaseScore() {
        long currentTs = System.currentTimeMillis();
        PreferenceTable.getInstance().putLong(PrefConst.KEY_NOTIFY_TIME, currentTs);

        PreferenceTable preferenceTable = PreferenceTable.getInstance();
        boolean notifyApp = preferenceTable.getBoolean(PrefConst.KEY_NOTIFY_APP, true);
        boolean notifyPic = preferenceTable.getBoolean(PrefConst.KEY_NOTIFY_PIC, true);
        boolean notifyVid = preferenceTable.getBoolean(PrefConst.KEY_NOTIFY_VID, true);

        LeoLog.i(TAG, "notifyDecreaseScore, start to notify, notifyApp: " + notifyApp + " | notifyPic: "
                + notifyPic + " | notifyVid: " + notifyVid);
        if (!notifyApp && !notifyPic && !notifyVid) return;

        AppMasterApplication application = AppMasterApplication.getInstance();
        int incApp = 0;
        int incAppScore = 0;
        if (notifyApp) {
            LockManager lm = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
            List<AppItemInfo> appList = lm.getNewAppList();
            incApp = appList.size();
            int maxNum = LockManager.MAX_SCORE / LockManager.SPA;
            incAppScore = incApp < maxNum ? incApp * LockManager.SPA : LockManager.MAX_SCORE;
        }
        int incPic = 0;
        int incPicScore = 0;
        PrivacyDataManager pdm = (PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA);
        if (notifyPic) {
            incPic = pdm.getAddPicNum();
            int maxNum = PrivacyDataManager.MAX_PIC_SCORE / PrivacyDataManager.SPA_PIC;
            incPicScore = incPic < maxNum ? incPic * PrivacyDataManager.SPA_PIC : PrivacyDataManager.MAX_PIC_SCORE;
        }
        int incVid = 0;
        int incVidScore = 0;
        if (notifyVid) {
            incVid = pdm.getAddVidNum();
            int maxNum = PrivacyDataManager.MAX_VID_SCORE / PrivacyDataManager.SPA_VID;
            incVidScore = incVid < maxNum ? incVid * PrivacyDataManager.SPA_VID : PrivacyDataManager.MAX_VID_SCORE;
        }

        LeoLog.i(TAG, "notifyDecreaseScore, incApp: " + incApp + " | incPic: " + incPic + " | incVid: " + incVid);

        if (incApp == 0 && incPic == 0 && incVid == 0) return;

        int type = incAppScore > incPicScore ? PRIVACY_APP_LOCK : PRIVACY_HIDE_PIC;
        int incOne = type == PRIVACY_APP_LOCK ? incAppScore : incPicScore;
        type = incOne > incVidScore ? type : PRIVACY_HIDE_VID;
        String content = null;
        int iconId;
        if (type == PRIVACY_APP_LOCK) {
            content = application.getString(R.string.scan_notify_app, incApp);
            iconId = R.drawable.noti_lock;
        } else if (type == PRIVACY_HIDE_PIC) {
            content = application.getString(R.string.scan_notify_pic, incPic);
            iconId = R.drawable.noti_pic;
        } else {
            content = application.getString(R.string.scan_notify_vid, incVid);
            iconId = R.drawable.noti_video;
        }
        String title = application.getString(R.string.scan_notify);
        // 弹通知栏提醒
        PushNotification pushNotification = new PushNotification(mContext);
        Intent intent = new Intent(mContext, StatusBarEventService.class);
        intent.putExtra(Constants.PRIVACY_ENTER_SCAN_TYPE, type);
        intent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE, StatusBarEventService.EVENT_PRIVACY_STATUS);
        pushNotification.showNotification(intent, title, content,
                iconId, pushNotification.NOTI_PRIVACYSTATUS);
    }

    private int calculateScore() {
        int score = 0;
        for (String key : mScoreMap.keySet()) {
            score += mScoreMap.get(key);
        }

        return score;
    }

    public Level getPrivacyLevel() {
        return mPrivacyLevel;
    }

    public void setDirty(boolean dirty) {
    }

    public void computePrivacyLevel(final int variable) {
    }

    public Pair<Integer, Integer> getColorPairByScore(int score) {
        Pair<Integer, Integer> pair = null;
        if (score >= 0 && score <= 20) {
            pair = new Pair<Integer, Integer>(HomeColor.sLevel6ColorUp, HomeColor.sLevel6ColorDown);
        } else if (score > 20 && score <= 40) {
            pair = new Pair<Integer, Integer>(HomeColor.sLevel5ColorUp, HomeColor.sLevel5ColorDown);
        } else if (score > 40 && score <= 60) {
            pair = new Pair<Integer, Integer>(HomeColor.sLevel4ColorUp, HomeColor.sLevel4ColorDown);
        } else if (score > 60 && score <= 80) {
            pair = new Pair<Integer, Integer>(HomeColor.sLevel3ColorUp, HomeColor.sLevel3ColorDown);
        } else if (score > 80 && score <= 99) {
            pair = new Pair<Integer, Integer>(HomeColor.sLevel2ColorUp, HomeColor.sLevel2ColorDown);
        } else {
            pair = new Pair<Integer, Integer>(HomeColor.sLevel1ColorUp, HomeColor.sLevel1ColorDown);
        }

        return pair;
    }

    public Pair<Integer, Integer> getNextPair(int score, boolean increase) {
        Pair<Integer, Integer> pair = null;
        if (increase) {
            if (score >= 0 && score <= 20) {
                pair = new Pair<Integer, Integer>(HomeColor.sLevel5ColorUp, HomeColor.sLevel5ColorDown);
            } else if (score > 20 && score <= 40) {
                pair = new Pair<Integer, Integer>(HomeColor.sLevel4ColorUp, HomeColor.sLevel4ColorDown);
            } else if (score > 40 && score <= 60) {
                pair = new Pair<Integer, Integer>(HomeColor.sLevel3ColorUp, HomeColor.sLevel3ColorDown);
            } else if (score > 60 && score <= 80) {
                pair = new Pair<Integer, Integer>(HomeColor.sLevel2ColorUp, HomeColor.sLevel2ColorDown);
            } else if (score > 80 && score <= 99) {
                pair = new Pair<Integer, Integer>(HomeColor.sLevel1ColorUp, HomeColor.sLevel1ColorDown);
            } else {
                pair = new Pair<Integer, Integer>(HomeColor.sLevel1ColorUp, HomeColor.sLevel1ColorDown);
            }
        } else {
            if (score >= 0 && score <= 20) {
                pair = new Pair<Integer, Integer>(HomeColor.sLevel6ColorUp, HomeColor.sLevel6ColorDown);
            } else if (score > 20 && score <= 40) {
                pair = new Pair<Integer, Integer>(HomeColor.sLevel6ColorUp, HomeColor.sLevel6ColorDown);
            } else if (score > 40 && score <= 60) {
                pair = new Pair<Integer, Integer>(HomeColor.sLevel5ColorUp, HomeColor.sLevel5ColorDown);
            } else if (score > 60 && score <= 80) {
                pair = new Pair<Integer, Integer>(HomeColor.sLevel4ColorUp, HomeColor.sLevel4ColorDown);
            } else if (score > 80 && score <= 99) {
                pair = new Pair<Integer, Integer>(HomeColor.sLevel3ColorUp, HomeColor.sLevel3ColorDown);
            } else {
                pair = new Pair<Integer, Integer>(HomeColor.sLevel2ColorUp, HomeColor.sLevel2ColorDown);
            }
        }

        return pair;
    }

    public float getGradientProgress(int score, boolean increase) {
        int scoreSimple = score % 20;
        if (score == 100 || score == 80 || score == 60 || score == 40 || score == 20) {
            scoreSimple = 20;
        }
        float ratio = 1f - (float) scoreSimple / 20f;
        if (increase) {
            ratio = (float) scoreSimple / 20f;
        }

        return ratio;
    }

    public float getGradientProgress(int score, int next) {
        if (score > next) {
            int temp = score;
            score = next;
            next = temp;
        }
        return ((float) score) / ((float) next);
//        int scoreSimple = score % 20;
//        if (score == 100 || score == 80 || score == 60 || score == 40 || score == 20) {
//            scoreSimple = 20;
//        }
//        float ratio = 1f - (float) scoreSimple / 20f;
//        if (increase) {
//            ratio = (float) scoreSimple / 20f;
//        }
//
//        return ratio;
    }

    public Level getPrivacyLevel(int score) {
        score = score < 0 ? 0 : score;
        score = score > 100 ? 100 : score;
        Level level = null;
        if (score >= 0 && score <= 20) {
            level = Level.LEVEL_SIX;
        } else if (score > 20 && score <= 40) {
            level = Level.LEVEL_FIVE;
        } else if (score > 40 && score <= 60) {
            level = Level.LEVEL_FOUR;
        } else if (score > 60 && score <= 80) {
            level = Level.LEVEL_THREE;
        } else if (score > 80 && score <= 99) {
            level = Level.LEVEL_TWO;
        } else {
            level = Level.LEVEL_ONE;
        }

        return level;
    }

    public boolean isSameLevel(int score, int other) {
        Level level = getPrivacyLevel(score);
        Level level1 = getPrivacyLevel(other);

        return level == level1;
    }

    public int getSecurityScore() {
        return mSecurityScore;
    }

    public void increaseScore(String mgr, int securityScore) {
        LeoLog.i(TAG, "increaseScore, mgr: " + mgr + " | securityScore: " + securityScore);
        synchronized (mScoreMap) {
            int newScore = mScoreMap.get(mgr);
            newScore += securityScore;

            mScoreMap.put(mgr, newScore);
            mSecurityScore = calculateScore();
        }
        logScore();
    }

    public void logScore() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        int i = mScoreMap.size();
        for (String key : mScoreMap.keySet()) {
            int score = mScoreMap.get(key);
            stringBuilder.append("(")
                    .append(key)
                    .append(",")
                    .append(score)
                    .append(")");
            if (i > 1) {
                stringBuilder.append("-");
            }
            i--;
        }
        stringBuilder.append("]");

        LeoLog.i("logScore", stringBuilder.toString());
    }

}
