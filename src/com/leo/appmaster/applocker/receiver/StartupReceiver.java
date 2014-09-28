package com.leo.appmaster.applocker.receiver;

import com.leo.appmaster.applocker.AppLockerPreference;
import com.leo.appmaster.applocker.service.LockService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StartupReceiver extends BroadcastReceiver {

	private static final String TAG = "StartupReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Log.e(TAG, action);
		if (action.equals(Intent.ACTION_BOOT_COMPLETED)
				|| action.equals(Intent.ACTION_USER_PRESENT)) {
			if (AppLockerPreference.getInstance(context).getLockType() != AppLockerPreference.LOCK_TYPE_NONE) {
				Intent serviceIntent = new Intent(context, LockService.class);
				serviceIntent.putExtra(LockService.EXTRA_STARTUP_FROM, action);
				context.startService(serviceIntent);
			}
		}
	}

}
