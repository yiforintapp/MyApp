package com.leo.appmaster.privacy;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.Intent;
import android.util.Pair;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.db.LeoPreference;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.home.HomeColor;
import com.leo.appmaster.imagehide.PhotoItem;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.Manager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.PrivacyDataManager;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;
import com.leo.appmaster.utils.TimeUtil;
import com.leo.appmaster.videohide.VideoItemBean;
import com.leo.appmater.globalbroadcast.LeoGlobalBroadcast;
import com.leo.appmater.globalbroadcast.ScreenOnOffListener;

public class PrivacyHelper implements Manager.SecurityChangeListener {
    private static final String TAG = "PrivacyHelper";

//    private static final int CHECK_TIME = 60 * 1000;
    private static final int CHECK_TIME = 20 * 1000;

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

    private ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private HashMap<String, Integer> mScoreMap;
    private HashMap<String, Integer> mDecScoreMap;

    private Runnable mCheckScoreTask;
    private Future mCheckScoreFuture;

    private Future mDalayNotifyFuture;
    private Runnable mDelayNotifyTask;

    private long mLastScanTs;

    private static Privacy sLockPrivacy = new LockPrivacy();
    private static Privacy sImagePrivacy = new ImagePrivacy();
    private static Privacy sVideoPrivacy = new VideoPrivacy();

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

    public static Privacy getAppPrivacy() {
        return getPrivacy(PRIVACY_APP_LOCK);
    }

    public static Privacy getImagePrivacy() {
        return getPrivacy(PRIVACY_HIDE_PIC);
    }

    public static Privacy getVideoPrivacy() {
        return getPrivacy(PRIVACY_HIDE_VID);
    }

    public static Privacy getPrivacy(int privacy) {
        switch (privacy) {
            case PRIVACY_APP_LOCK:
                return sLockPrivacy;
            case PRIVACY_HIDE_PIC:
                return sImagePrivacy;
            case PRIVACY_HIDE_VID:
                return sVideoPrivacy;
            default:
                break;
        }

        return null;
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

    public void initPrivacyStatus() {
        boolean isScreenOn = ScreenOnOffListener.isScreenOn();
        LeoLog.d(TAG, "<ls> initPrivacyStatus...isScreenOn: " + isScreenOn);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                setPrivacyListAndCount();
            }
        });
        if (isScreenOn) {
            startIntervalScanner(CHECK_TIME);
        }
        ScreenOnOffListener.addListener(new ScreenOnOffListener.ScreenChangeListener() {
            @Override
            public void onScreenChanged(Intent intent) {
                handleScreenIntent(intent);
            }
        });
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
        LeoLog.d(TAG, "<ls> onSecurityChange, description: " + description);
        setPrivacyListAndCount();
    }

    private class ScoreTimerTask implements Runnable {
        @Override
        public void run() {
            LeoLog.i(TAG, "<ls> ScoreTimerTask, start to check.");
//            if (AppMasterApplication.getInstance().isHomeOnTopAndBackground()) {
//                LeoLog.i(TAG, "<ls> home is ontop and background, so donot scan.");
//                return;
//            }
            long currentTs = System.currentTimeMillis();
            mLastScanTs = currentTs;

            setPrivacyListAndCount();

            checkOrNotifyPrivacy(PRIVACY_APP_LOCK);
            checkOrNotifyPrivacy(PRIVACY_HIDE_PIC);
            checkOrNotifyPrivacy(PRIVACY_HIDE_VID);
        }
    }

    private void setPrivacyListAndCount() {
        LockManager lm = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
        List<AppItemInfo> appList = lm.getNewAppList();
        sLockPrivacy.setNewList(appList);
        sLockPrivacy.setProceedCount(lm.getLockedAppCount());
        sLockPrivacy.setTotalCount(lm.getAllAppCount());
        LeoLog.d(TAG, "<ls> set privacy: " + sLockPrivacy.toString());

        PrivacyDataManager pdm = (PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA);
        List<PhotoItem> picList = pdm.getAddPic();
        sImagePrivacy.setNewList(picList);
        sImagePrivacy.setProceedCount(pdm.getHidePicsNum());
        sImagePrivacy.setTotalCount(pdm.getNormalPicsNum());
        LeoLog.d(TAG, "<ls> set privacy: " + sImagePrivacy.toString());

        List<VideoItemBean> vidList = pdm.getAddVid();
        sVideoPrivacy.setNewList(vidList);
        sVideoPrivacy.setProceedCount(pdm.getHideVidsNum());
        sVideoPrivacy.setTotalCount(pdm.getNormalVidsNum());
        LeoLog.d(TAG, "<ls> set privacy: " + sVideoPrivacy.toString());
    }

    private class DelayTimerTask implements Runnable {
        int privacyType;
        DelayTimerTask(int privacyType) {
            this.privacyType = privacyType;
        }

        @Override
        public void run() {
            long lastNotify = LeoPreference.getInstance().getLong(PrefConst.KEY_NOTIFY_TIME, 0);
            long currentTs = System.currentTimeMillis();
            boolean isSameDay = TimeUtil.isSameDay(lastNotify, currentTs);
            if (!isSameDay) {
                LeoLog.i(TAG, "DelayTimerTask, start to do delay timer.");
                notifyDecreasePrivacy(privacyType);
            } else {
                LeoLog.i(TAG, "DelayTimerTask, already notify.");
            }
        }
    }

    private void checkOrNotifyPrivacy(int privacyType) {
        if (!AppUtil.notifyAvailable()) {
            return;
        }

        long currentTs = System.currentTimeMillis();
        Privacy privacy = getPrivacy(privacyType);
        if (privacy == null || !privacy.isNotifyOpen()) {
            return;
        }

        int limit = privacy.getPrivacyLimit();
        int addedCount = privacy.getNewCount();
        if (addedCount >= limit && privacy.getStatus() == Privacy.STATUS_NEW_ADD) {
            // 命中通知逻辑
            long lastNotify = LeoPreference.getInstance().getLong(PrefConst.KEY_NOTIFY_TIME, 0);
            AppMasterApplication application = AppMasterApplication.getInstance();
            boolean isForeground = application.isVisible();
            boolean isSameDay = TimeUtil.isSameDay(lastNotify, currentTs);
            LeoLog.i(TAG, "checkOrNotifyDecScore, isForeground: " + isForeground + ", isSameDay: " + isSameDay);
            if (!isForeground && !isSameDay) {
                int currHour = TimeUtil.getHourOfDay(currentTs);
                if (currHour > 7) {
                    // 当前时间大于7点钟，弹出通知
                    notifyDecreasePrivacy(privacyType);
                } else {
                    // 当前时间小于7点钟，延时7个小时再弹通知
                    long hour7 = 7 * 60 * 60 * 1000;
                    if (mDalayNotifyFuture != null) {
                        mDalayNotifyFuture.cancel(false);
                    }
                    mDelayNotifyTask = new DelayTimerTask(privacyType);
                    mDalayNotifyFuture = ThreadManager.getAsyncExecutor().schedule(
                            mDelayNotifyTask, hour7, TimeUnit.MILLISECONDS);
                }

            }
        }
    }

    private void notifyDecreasePrivacy(int privacyType) {
        long currentTs = System.currentTimeMillis();
        LeoPreference.getInstance().putLong(PrefConst.KEY_NOTIFY_TIME, currentTs);

        Privacy privacy = getPrivacy(privacyType);
        if (privacy == null) {
            return;
        }
        privacy.showNotification();
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
        return 0;
    }

    public int getSecurityScore(String mgr) {
        Integer score = mScoreMap.get(mgr);
        if (score == null) {
            return 0;
        }

        return score.intValue();
    }

}
