package com.leo.appmaster.intruderprotection;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.mgr.IntrudeSecurityManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.utils.PrefConst;

/**
 * Created by chenfs on 16-3-14.
 */
public class SystLockAdminReceiver extends DeviceAdminReceiver {
    public static final String PASSWORD_FAILED_TIMES_OUT_OF_LIMITS = "com.leo.appmaster.action.FAILED_PASSWORD_ATTEMPTS_OUT_OF_LIMITS";

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Toast.makeText(context,intent.getAction() + "",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPasswordChanged(Context context, Intent intent) {
        super.onPasswordChanged(context, intent);
    }

    @Override
    public void onPasswordExpiring(Context context, Intent intent) {
        super.onPasswordExpiring(context, intent);
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        return super.onDisableRequested(context, intent);
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);
        //
        // ComponentName cn = new ComponentName(context,
        // LockScreenStatusReceiver.class);
        // DevicePolicyManager mgr = (DevicePolicyManager)
        // context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        // int currentFailedPasswordAttempts =
        // mgr.getCurrentFailedPasswordAttempts();
        // mgr.setPasswordQuality(cn,
        // DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC);
        //
        // onPasswordChanged(context, intent);
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        // Toast.makeText(context, "onDisabled!!!", 1).show();
        // Log.i("pp", "disabled" + intent.getAction());
        super.onDisabled(context, intent);
    }

    @Override
    public void onPasswordFailed(Context context, Intent intent) {

//		DevicePolicyManager mgr = (DevicePolicyManager) context
        try {
            DevicePolicyManager mgr = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            Toast.makeText(context, mgr.isActivePasswordSufficient() + " : sufficient?" , 1).show();
            int sd = mgr.getCurrentFailedPasswordAttempts();
            IntrudeSecurityManager ISManager = (IntrudeSecurityManager) MgrContext.getManager(MgrContext.MGR_INTRUDE_SECURITY);
            if (sd >= ISManager.getTimesForTakePhoto()) {
                PreferenceTable.getInstance().putBoolean(PrefConst.KEY_NEED_TAKE_PICTURE_WHEN_USER_PRESENT,true);
                Toast.makeText(context, "dd要开始抓拍了---" + sd, 0).show();
            }

        } catch (Exception e) {
            Toast.makeText(context, e.toString() , 1).show();
        }
    }

    public static final ComponentName getComponentName(Context context) {
        return new ComponentName(context, SystLockAdminReceiver.class);
    }

    @Override
    public void onPasswordSucceeded(Context context, Intent intent) {
        // Log.i("pp", "succeeded");
//        Toast.makeText(context, "onPasswordSucceeded!!!", 1).show();

//
//		// final WindowManager.LayoutParams params = new LayoutParams();
//		// params.height = WindowManager.LayoutParams.WRAP_CONTENT;
//		// params.width = WindowManager.LayoutParams.WRAP_CONTENT;
//		// // params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
//		// // | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
//		// params.flags =
//		// WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|
//		// WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
//		// params.format = PixelFormat.TRANSLUCENT;
//		// // params.windowAnimations = android.R.style.Animation_Toast;
//		// params.type = WindowManager.LayoutParams.TYPE_TOAST;

//        try {
//            WindowManager mWM = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
//            int i = getFrontCameraId();
//            Camera mCamera = safeCameraOpen(i);
//            CameraPreview mPreview = new CameraPreview(context, mCamera);
//            FrameLayout mRootView = new FrameLayout(context);
//            WindowManager.LayoutParams localLayoutParams = new WindowManager.LayoutParams();
////			localLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
////			localLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
//            localLayoutParams.height = 100;
//            localLayoutParams.width = 100;
////	         params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
////	         | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
//            localLayoutParams.flags =
//                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|
//                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
//            localLayoutParams.format = PixelFormat.TRANSLUCENT;
////	         params.windowAnimations = android.R.style.Animation_Toast;
//            localLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
//            localLayoutParams.screenOrientation = 10;
//            localLayoutParams.gravity = 51;
//            mRootView.addView(mPreview);
//            mWM.addView(mRootView, localLayoutParams);
////			new Thread(new Runnable() {
////
////				@Override
////				public void run() {
////					mPreview.
////				}
////			});
//        } catch (Throwable e) {
//            Toast.makeText(context, e.toString(), 1).show();
//        }

    }


    public static int getFrontCameraId() {
        int i = Camera.getNumberOfCameras();
        for (int j = 0; j < i; j++) {
            Camera.CameraInfo localCameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(j, localCameraInfo);
            if (localCameraInfo.facing == 1)
                return j;
        }
        return -1;
    }

    private Camera safeCameraOpen(int paramInt) {
        try {
            Camera localCamera = Camera.open(paramInt);
            return localCamera;
        } catch (Exception localException) {
            localException.printStackTrace();
        }
        return null;
    }

    public static final boolean isActive(Context context) {
        return ((DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE)).isAdminActive(getComponentName(context));
    }
}
