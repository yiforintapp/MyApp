
package com.leo.appmaster.intruderprotection;

import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;

public class CameraUtils {
    public static final int FRONT_FACING_ONLY = 1;
    public static final int BACK_FACING_ONLY = 2;
    public static final int FRONT_AND_BACK = 3;
    public static final int NO_FRONT_OR_BACK = 0;

    public static int checkCameraFacing() {
        boolean hasFront = false;
        boolean hasBack = false;
        try {
            CameraInfo info = new CameraInfo();
            int cameraCount = Camera.getNumberOfCameras();
            for (int i = 0; i < cameraCount; i++) {
                Camera.getCameraInfo(i, info);
                if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                    hasFront = true;
                }
                if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
                    hasBack = true;
                }
            }
        } catch (Throwable t) {
        }
        if (hasBack && hasFront) {
            return FRONT_AND_BACK;
        } else if (!hasBack && !hasFront) {
            return NO_FRONT_OR_BACK;
        } else if (hasBack && !hasFront) {
            return BACK_FACING_ONLY;
        } else if (!hasBack && hasFront) {
            return FRONT_FACING_ONLY;
        } else {
            return -1;
        }
    }
}
