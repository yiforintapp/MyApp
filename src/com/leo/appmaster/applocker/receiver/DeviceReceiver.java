package com.leo.appmaster.applocker.receiver;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Environment;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.Constants;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.IntruderPhotoInfo;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.DeviceAdminEvent;
import com.leo.appmaster.eventbus.event.EventId;
import com.leo.appmaster.intruderprotection.CameraSurfacePreview;
import com.leo.appmaster.intruderprotection.IntruderCatchedActivity;
import com.leo.appmaster.intruderprotection.WaterMarkUtils;
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

public class DeviceReceiver extends DeviceAdminReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		LeoLog.d("STONE_ADMIN", intent.getAction());
		if(action.equals(DeviceAdminReceiver.ACTION_DEVICE_ADMIN_ENABLED)){
			LeoLog.d("stone_admin", "ACTION_DEVICE_ADMIN_ENABLED");
			LeoEventBus.getDefaultBus().post(
					new DeviceAdminEvent(EventId.EVENT_DEVICE_ADMIN_ENABLE, "useless"));
		}
		if(action.equals(DeviceAdminReceiver.ACTION_DEVICE_ADMIN_DISABLED)){
			LeoLog.d("stone_admin", "ACTION_DEVICE_ADMIN_DISABLED");
			LeoEventBus.getDefaultBus().post(
					new DeviceAdminEvent(EventId.EVENT_DEVICE_ADMIN_DISABLE, "useless"));
		}
	}

	public static final ComponentName getComponentName(Context context) {
		return new ComponentName(context, DeviceReceiver.class);
	}


	public static boolean isActive(Context context) {
		return ((DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE)).isAdminActive(getComponentName(context));
	}
}
