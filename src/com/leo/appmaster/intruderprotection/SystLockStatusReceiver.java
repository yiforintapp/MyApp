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
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.mgr.IntrudeSecurityManager;
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
    private PrivacyDataManager mPDManager;
    private IntrudeSecurityManager mISManager;
    @Override
    public void onReceive(Context context, Intent intent) {
        mISManager = (IntrudeSecurityManager) MgrContext.getManager(MgrContext.MGR_INTRUDE_SECURITY);
        mPDManager = (PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA);
        Toast.makeText(context, "status receiver" + intent.getAction(), Toast.LENGTH_SHORT).show();
        PreferenceTable pt = PreferenceTable.getInstance();
        boolean value = pt.getBoolean(PrefConst.KEY_NEED_TAKE_PICTURE_WHEN_USER_PRESENT, false);
        if (value) {
            Toast.makeText(context, "to add view 1", Toast.LENGTH_SHORT).show();
            pt.putBoolean(PrefConst.KEY_NEED_TAKE_PICTURE_WHEN_USER_PRESENT, false);
//            ThreadManager.executeOnAsyncThreadDelay(new Runnable() {
//                @Override
//                public void run() {
                    try {
                        final Context ctx = AppMasterApplication.getInstance();
                        Toast.makeText(ctx, "to add view 2", Toast.LENGTH_SHORT).show();
                        final CameraSurfacePreview cfp = new CameraSurfacePreview(ctx);
                        final WindowManager mWM = (WindowManager) AppMasterApplication.getInstance().getSystemService(Context.WINDOW_SERVICE);
                        final FrameLayout mRootView = new FrameLayout(ctx);
                        WindowManager.LayoutParams localLayoutParams = new WindowManager.LayoutParams();
//			localLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
//			localLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
                        localLayoutParams.height = 100;
                        localLayoutParams.width = 100;
//	         params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
//	         | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
                        localLayoutParams.flags =
                                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|
                                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
//	         params.windowAnimations = android.R.style.Animation_Toast;
                        localLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
                        localLayoutParams.screenOrientation = 10;
                        localLayoutParams.gravity = 51;
                        mRootView.addView(cfp);
                        mWM.addView(mRootView, localLayoutParams);
                        ThreadManager.executeOnAsyncThreadDelay(new Runnable() {
                            @Override
                            public void run() {
                                cfp.takePicture(new Camera.PictureCallback() {
                                    @Override
                                    public void onPictureTaken(byte[] data, Camera camera) {
                                        Bitmap bitmapt = null;
                                        try {
                                            bitmapt = BitmapUtils.bytes2BimapWithScale(data, ctx);
                                        } catch (Throwable e) {
                                        }
                                        //旋转原始bitmap到正确的方向

                                        Matrix m = new Matrix();
                                        int orientation = cfp.getCameraOrientation();
                                        mWM.removeView(mRootView);
                                        m.setRotate(180 - orientation, (float) bitmapt.getWidth() / 2, (float) bitmapt.getHeight() / 2);
                                        bitmapt = Bitmap.createBitmap(bitmapt, 0, 0, bitmapt.getWidth(), bitmapt.getHeight(), m, true);
                                        String timeStamp = new SimpleDateFormat(Constants.INTRUDER_PHOTO_TIMESTAMP_FORMAT).format(new Date());

                                        //添加水印
                                        bitmapt = WaterMarkUtils.createIntruderPhoto(bitmapt, timeStamp, "com.leo.appmaster", ctx);

                                        //将bitmap压缩并保存
                                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                        bitmapt.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                                        byte[] finalBytes = baos.toByteArray();
                                        File photoSavePath = getPhotoSavePath();
                                        if (photoSavePath == null) {
                                            return;
                                        }
                                        String finalPicPath = "";
                                        try {
                                            LeoLog.i("poha", photoSavePath + "::save Path");
                                            FileOutputStream fos = new FileOutputStream(photoSavePath);
                                            fos.write(finalBytes);
                                            fos.close();
                                            // 隐藏图片

                                            finalPicPath = mPDManager.onHidePic(photoSavePath.getPath(), null);
                                            FileOperationUtil.saveFileMediaEntry(finalPicPath, ctx);
                                            FileOperationUtil.deleteImageMediaEntry(photoSavePath.getPath(), ctx);
                                            PrivacyDataManager pdm = (PrivacyDataManager) MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA);
                                            pdm.notifySecurityChange();
                                        } catch (Exception e) {
                                            LeoLog.i("poha", "exception!!   ..." + e.toString());
                                            return;
                                        }

                                        IntruderPhotoInfo info = new IntruderPhotoInfo(finalPicPath, "com.leo.appmaster", timeStamp);
                                        mISManager.insertInfo(info);
                                    }
                                });
                            }
                        }, 500);

                    } catch (Throwable e) {
                        Toast.makeText(AppMasterApplication.getInstance(), e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
//            }, 300);
        }

    private File getPhotoSavePath() {
        File picDir = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath());
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH).format(new Date());
        File dir = new File(picDir.getPath() + File.separator + "IntruderP");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir + File.separator + "IMAGE_" + timeStamp + ".jpg");
    }
}
