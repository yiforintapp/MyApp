
package com.leo.appmaster.privacy;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;

import com.leo.appmaster.R;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.EventId;
import com.leo.appmaster.eventbus.event.PrivacyLevelChangeEvent;
import com.leo.appmaster.home.HomeColorUtil;
import com.leo.appmaster.home.HomeColorUtil.ColorHolder;
import com.leo.appmaster.privacycontact.PrivacyContactManager;
import com.leo.appmaster.sdk.SDKWrapper;

public class PrivacyHelper {

    public enum Level {
        LEVEL_ONE, LEVEL_TWO, LEVEL_THREE, LEVEL_FOUR, LEVEL_FIVE
    }
    public static final int VARABLE_ALL = -1;
    public static final int VARABLE_APP_LOCK = 0;
    public static final int VARABLE_HIDE_PIC = 1;
    public static final int VARABLE_HIDE_VIDEO = 2;
    public static final int VARABLE_PRIVACY_CONTACT = 3;
    
    
    private final static int[][] mPrivacyPercents = {
            {
                    0, 0, 5, 10, 15
            }, // app lock
            {
                    0, 0, 5, 10, 15
            }, // image hide
            {
                    0, 0, 5, 10, 15
            }, // video hide
            {
                    0, 0, 1, 3, 8
            }
            // privacy contacts
    };

    private static PrivacyHelper sInstance;

    private Context mContext;

    private boolean mIsComputing = false;
    
    private boolean mFirstCompute = true;
    
    /** Flag for play animation */
    private boolean mDirty = true;

    private Level mPrivacyLevel = Level.LEVEL_ONE;

    private PrivacyVariable[] mVariables;
    
    private ComputeRunnable mComputeRunnable;
    
    private ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private PrivacyHelper(Context context) {
        mContext = context.getApplicationContext();
        mComputeRunnable = new ComputeRunnable();
        mVariables = new PrivacyVariable[mPrivacyPercents.length];
        for (int i = 0; i < mPrivacyPercents.length; i++) {
            PrivacyVariable variable = new PrivacyVariable();
            variable.levelOnePercent = mPrivacyPercents[i][0];
            variable.levelTwoPercent = mPrivacyPercents[i][1];
            variable.levelThreePercent = mPrivacyPercents[i][2];
            variable.levelFourPercent = mPrivacyPercents[i][3];
            variable.levelFivePercent = mPrivacyPercents[i][4];
            mVariables[i] = variable;
        }
    }

    public static synchronized PrivacyHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new PrivacyHelper(context);
        }
        return sInstance;
    }

    public Level getPrivacyLevel() {
        return mPrivacyLevel;
    }

    public String getLevelDescription(Level level) {
        switch (level) {
            case LEVEL_ONE:
                return mContext.getString(R.string.privacy_level_1);
            case LEVEL_TWO:
                return mContext.getString(R.string.privacy_level_2);
            case LEVEL_THREE:
                return mContext.getString(R.string.privacy_level_3);
            case LEVEL_FOUR:
                return mContext.getString(R.string.privacy_level_4);
            case LEVEL_FIVE:
                return mContext.getString(R.string.privacy_level_5);
            default:
                return mContext.getString(R.string.privacy_level_1);
        }
    }

    public ColorHolder getCurLevelColor() {
        if (mPrivacyLevel == Level.LEVEL_ONE) {
            return HomeColorUtil.sColorRed;
        } else if (mPrivacyLevel == Level.LEVEL_TWO) {
            return HomeColorUtil.sColorOrange;
        } else if (mPrivacyLevel == Level.LEVEL_THREE) {
            return HomeColorUtil.sColorGolden;
        } else if (mPrivacyLevel == Level.LEVEL_FOUR) {
            return HomeColorUtil.sColorGreen;
        } else if (mPrivacyLevel == Level.LEVEL_FIVE) {
            return HomeColorUtil.sColorBlue;
        } else {
            return HomeColorUtil.sColorBlue;
        }
    }
    
    public boolean isVariableActived(int variable) {
        if(variable >= 0 && variable < mVariables.length) {
            return mVariables[variable].isProtected();
        }
        return false;
    }
    
    public boolean isDirty() {
        return mDirty;
    }
    
    public void setDirty(boolean dirty) {
        mDirty = dirty;
    }

    /**
     * Recompute privacy level when privacy data changed.
     * 
     * @param variable  which data changed, must be one of (
     *            {@link #VARABLE_APP_LOCK}, {@link #VARABLE_HIDE_PIC},
     *            {@link #VARABLE_HIDE_VIDEO}, {@link #VARABLE_PRIVACY_CONTACT}
     *            ); or -1 if all data changed.
     */
    public void computePrivacyLevel(final int variable) {
        if (mIsComputing) {
            return;
        }
        mIsComputing = true;
        mComputeRunnable.setVariable(variable);
        mExecutor.execute(mComputeRunnable);
        }

    private void computePrivacyLevelInternal(int variable) {
        loadVariable(variable);
        Level level = Level.LEVEL_ONE;
        if (matchLevelFive()) {
            level = Level.LEVEL_FIVE;
        } else if (matchLevelFour()) {
            level = Level.LEVEL_FOUR;
        } else if (matchLevelThree()) {
            level = Level.LEVEL_THREE;
        } else if (matchLevelTwo()) {
            level = Level.LEVEL_TWO;
        } else {
            level = Level.LEVEL_ONE;
        }
        mIsComputing = false;
        if(mPrivacyLevel != level) {
            mPrivacyLevel = level;
            LeoEventBus.getDefaultBus().post(
                    new PrivacyLevelChangeEvent(EventId.EVENT_PRIVACY_LEVEL_COMPUTED));
            if(!mFirstCompute) {
                SDKWrapper.addEvent(mContext, SDKWrapper.P1, "prilevel", mPrivacyLevel.ordinal() + 1 + "");
                setDirty(true);
            }
        }
        mFirstCompute = false;
    }
    
    private void loadVariable(int variable) {
        switch (variable) {
            case VARABLE_APP_LOCK:
                loadAppLockVariable();
                break;
            case VARABLE_HIDE_PIC:
                loadHidePicVariable();
                break;
            case VARABLE_HIDE_VIDEO:
                loadHideVideoVariable();
                break;
            case VARABLE_PRIVACY_CONTACT:
                loadPrivacyContactVariable();
                break;
            default:
                loadAppLockVariable();
                loadHidePicVariable();
                loadHideVideoVariable();
                loadPrivacyContactVariable();
                break;
        }

    }
    
    private void loadAppLockVariable() {
        LockManager lm = LockManager.getInstatnce();
        mVariables[VARABLE_APP_LOCK].totalCount = lm.getAllAppCount();
        mVariables[VARABLE_APP_LOCK].privacyCount = lm.getLockedAppCount();
    }
    
    private void loadHidePicVariable() {
        mVariables[VARABLE_HIDE_PIC].privacyCount = FileHideHelper.getHidePhotoCount(mContext);
        if(mVariables[VARABLE_HIDE_PIC].privacyCount > 0) {
            mVariables[VARABLE_HIDE_PIC].totalCount = FileHideHelper.getAllPhotoCount(mContext);
            mVariables[VARABLE_HIDE_PIC].totalCount += mVariables[VARABLE_HIDE_PIC].privacyCount;
        }
    }
    
    private void loadHideVideoVariable() {
        mVariables[VARABLE_HIDE_VIDEO].privacyCount = FileHideHelper.getHideVideoCount(mContext);
        if(mVariables[VARABLE_HIDE_VIDEO].privacyCount > 0) {
            mVariables[VARABLE_HIDE_VIDEO].totalCount = FileHideHelper.getAllVideoCount(mContext);
            mVariables[VARABLE_HIDE_VIDEO].totalCount += mVariables[VARABLE_HIDE_VIDEO].privacyCount;
        }
    }
    
    private void loadPrivacyContactVariable() {
        PrivacyContactManager pcm = PrivacyContactManager.getInstance(mContext);
        mVariables[VARABLE_PRIVACY_CONTACT].totalCount = pcm.getAllContactsCount();
        mVariables[VARABLE_PRIVACY_CONTACT].privacyCount = pcm.getPrivacyContactsCount();
    }

    private boolean matchLevelTwo() {
        for (PrivacyVariable variable : mVariables) {
            if (variable.isProtected()) {
                return true;
            }
        }
        return false;
    }

    private boolean matchLevelThree() {
        int unprotectCount = 0;
        int matchCount = 0;
        for (PrivacyVariable variable : mVariables) {
            if (!variable.isProtected()) {
                unprotectCount++;
                if (unprotectCount > 1) {
                    return false;
                }
                continue;
            } else if (variable.matchLevel(Level.LEVEL_THREE)) {
                matchCount++;
            }
        }
        return matchCount > 0;
    }

    private boolean matchLevelFour() {
        int unprotectCount = 0;
        int matchCount = 0;
        for (PrivacyVariable variable : mVariables) {
            if (!variable.isProtected()) {
                unprotectCount++;
                if (unprotectCount > 1) {
                    return false;
                }
                continue;
            } else if (variable.matchLevel(Level.LEVEL_FOUR)) {
                matchCount++;
            }
        }
        return matchCount > 0;
    }

    private boolean matchLevelFive() {
        for (PrivacyVariable variable : mVariables) {
            if (!variable.isProtected() || !variable.matchLevel(Level.LEVEL_FIVE)) {
                return false;
            }
        }
        return true;
    }

    private class ComputeRunnable implements Runnable {
        
        private int mVariable = VARABLE_ALL;
        
        @Override
        public void run() {
            computePrivacyLevelInternal(mVariable);
        }
        
        public void setVariable(int variable) {
            mVariable = variable;
        }       
    }
    
}
