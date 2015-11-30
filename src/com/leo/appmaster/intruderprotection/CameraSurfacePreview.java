
package com.leo.appmaster.intruderprotection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.Build;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;

@SuppressWarnings("deprecation")
public class CameraSurfacePreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
//    private boolean mIsActive = false;
    private boolean mIsinited = false;
    private Camera mCamera;
    private boolean mCanTake = true;
    private PictureCallback mPendingCallback;
    private int mCameraOrientation;
    private boolean mIsTimeOut = false;
    
    public CameraSurfacePreview(Context context) {
        super(context);
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
    
    public boolean getIsTimeOut() {
        return mIsTimeOut;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //创建时初始化，camera的初始化容易异常，使用try catch
            ThreadManager.executeOnAsyncThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        init();
                    } catch (Exception e) {
                        LeoLog.i("poha", "exception in the whole init :" + e.toString());
                    }
                }
            });
    }

    public int getCameraOrientation() {
        return mCameraOrientation;
    }
    
    private void selectPictureSize() {
        Parameters parameters = mCamera.getParameters();
        List<Size> Sizes = parameters.getSupportedPictureSizes();
//        for(int i = 0; i < Sizes.size(); i++) {
//            LeoLog.i("poha", "照相机支持的分辨率： " + "height :" + Sizes.get(i).height + "  width : "+ Sizes.get(i).width);
//        }
       // 宽高差距大接近屏幕差距  //尺寸最好在1280以下
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        float HWRate = (float)height / (float)width;
        int finalIndex = 0;
        ArrayList<Integer> indexsWhichWidthLessThan1280 = new ArrayList<Integer>();
        
        for (int i = 0; i < Sizes.size(); i++) {
            if (Sizes.get(i).width <= 1280 && Sizes.get(i).width >= 800) {
                indexsWhichWidthLessThan1280.add(i);
            } 
        }
        
        if(indexsWhichWidthLessThan1280.size() == 0) {
            int normalQualityLevel = Sizes.size() / 2;
            if(Sizes.get(normalQualityLevel).height > 1280 || (Math.max(Sizes.get(normalQualityLevel).height, Sizes.get(normalQualityLevel).width) / Math.min(Sizes.get(normalQualityLevel).height, Sizes.get(normalQualityLevel).width) < 1.5)) {
                if((normalQualityLevel - 1 >= 0) && (Sizes.get(normalQualityLevel - 1).height < Sizes.get(normalQualityLevel).height)) {
                    normalQualityLevel -- ;
                } else if ((normalQualityLevel + 1 <= Sizes.size()) && (Sizes.get(normalQualityLevel + 1).height < Sizes.get(normalQualityLevel).height)) {
                    normalQualityLevel ++ ;
                }
            }
            finalIndex = normalQualityLevel;
        } else {
            int tempFitestRateIndex = 0;
            for (int j = 0; j < indexsWhichWidthLessThan1280.size(); j++) {
                float rate = (float)Sizes.get(indexsWhichWidthLessThan1280.get(j)).width / (float)Sizes.get(indexsWhichWidthLessThan1280.get(j)).height;
                LeoLog.i("poha", "1280以下： " +indexsWhichWidthLessThan1280.get(j)+ "   height :" + Sizes.get(indexsWhichWidthLessThan1280.get(j)).height + "  width : "+ Sizes.get(indexsWhichWidthLessThan1280.get(j)).width +"rate = "+rate +"HWRate = "+HWRate);
                if (Math.abs(rate - HWRate) < Math.abs(((float)Sizes.get(tempFitestRateIndex).width / (float)Sizes.get(tempFitestRateIndex).height) - HWRate)) {
                    tempFitestRateIndex = indexsWhichWidthLessThan1280.get(j);
                }
            }
            finalIndex = tempFitestRateIndex;
        }
        parameters.setPictureSize(Sizes.get(finalIndex).width,Sizes.get(finalIndex).height);  // 使用中等档次的照相品质
        mCamera.setParameters(parameters);
        Size pictureSize = parameters.getPictureSize();
        LeoLog.i("poha", "照相机实际的分辨率： " + "height :" + pictureSize.height + "  width : "+ pictureSize.width);
    }
    
    
    @SuppressWarnings("deprecation")
    public void init() {
        LeoLog.i("poha", "start init ..");
        long tbefore = System.currentTimeMillis();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            return;
        }
        int checkCameraFacing = CameraUtils.checkCameraFacing();
        // 打开摄像头，默认优先级是：前置，后置 都没有 直接返回
        if ((checkCameraFacing == CameraUtils.FRONT_AND_BACK)
                || (checkCameraFacing == CameraUtils.FRONT_FACING_ONLY)) {
            mCamera = Camera.open(CameraInfo.CAMERA_FACING_FRONT);
        } 
        else {
            return;
        }
            //设置照相机的参数
            mCamera.setDisplayOrientation(90);          //预览时的角度
            selectPictureSize();
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(CameraInfo.CAMERA_FACING_FRONT, info);
            mCameraOrientation = info.orientation;
            LeoLog.i("poha", "前置照相机  orientation = "+info.orientation);
            try {
                mCamera.setPreviewDisplay(mHolder);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            mCamera.startPreview();
            mIsinited = true;
            if(mPendingCallback != null) {
                LeoLog.i("poha", "last time try to take picture, but not init, try to take picture after 1000ms ");
                ThreadManager.executeOnAsyncThreadDelay(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            LeoLog.i("poha", "take pic after daley");
                            mCamera.takePicture(null, null, mPendingCallback);
                        } catch (Throwable e) {
                            LeoLog.i("poha", "Fail to takePic  :"+e.getMessage());
                        }
                        mPendingCallback = null;
                        mIsTimeOut = false;
                    }
                }, 1000);
            }
        LeoLog.i("poha", "init 耗时 = "+ (System.currentTimeMillis() - tbefore));
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (!mIsinited)
            return;
        try {
            mCamera.startPreview();
        } catch (Exception e) {
        }
    }
    

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        LeoLog.i("poha", "surface Destoryed! Camera release");
        release();
    }

    public void takePicture(PictureCallback imageCallback) {
        LeoLog.i("poha", "mCanTake : " + mCanTake);
        if(!mIsinited) {
            mIsTimeOut = true;
            LeoLog.i("poha", "!mIsinited!!!!!") ;
            mPendingCallback = imageCallback;
            return;
        }
            mCamera.takePicture(null, null, imageCallback);
    }

    public void release() {
        if (mCamera != null) {
            mIsinited = false;
            try {
                mCamera.stopPreview();
                mCamera.release();
                LeoLog.i("poha", "Camera release");
            } catch (Exception e) {
                
            }
            mCamera = null;
        }
        mPendingCallback = null;
    }
}
