package com.leo.appmaster.intruderprotection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Environment;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.Constants;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.IntruderPhotoInfo;
import com.leo.appmaster.applocker.receiver.DeviceReceiver;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.mgr.IntrudeSecurityManager;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.PrivacyDataManager;
import com.leo.appmaster.utils.BitmapUtils;
import com.leo.appmaster.utils.FileOperationUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by chenfs on 16-3-15.
 */
public class SystLockStatusReceiver extends BroadcastReceiver {
//    private PrivacyDataManager mPDManager;
//    private IntrudeSecurityManager mISManager;
private LockManager mLockManager;

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || Intent.ACTION_USER_PRESENT.equals(action)) {
            if (mLockManager == null) {
                mLockManager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
            }
//            mLockManager.filterPackage(context.getPackageName(),1000);
            if (IntrudeSecurityManager.sHasTakenWhenUnlockSystemLock && IntrudeSecurityManager.sHasPicTakenAtSystemLockSaved) {
                IntrudeSecurityManager.sHasPicShowedWhenUserPresent = true;
                final Intent intent2 = new Intent(AppMasterApplication.getInstance(), IntruderCatchedActivity.class);
                intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent2.putExtra("pkgname", "from_systemlock");
                IntrudeSecurityManager.sHasTakenWhenUnlockSystemLock = false;
                ThreadManager.executeOnAsyncThreadDelay(new Runnable() {
                    @Override
                    public void run() {
                        mLockManager.filterPackage(context.getPackageName(), 1000);
                        context.startActivity(intent2);
                    }
                }, DeviceReceiver.NORMAL_TIME);
            } else if(IntrudeSecurityManager.sHasTakenWhenUnlockSystemLock && !IntrudeSecurityManager.sHasPicTakenAtSystemLockSaved){
                IntrudeSecurityManager.sHasPicShowedWhenUserPresent = false;
            }
            IntrudeSecurityManager.sFailTimesAtSystLock = 0;
        }

    }

}
