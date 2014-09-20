package com.leo.applocker.receiver;

import com.leo.applocker.service.LockService;

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
		if(action.equals(Intent.ACTION_BOOT_COMPLETED) || action.equals(Intent.ACTION_USER_PRESENT)) {
			Intent serviceIntent = new Intent(context, LockService.class);
			serviceIntent.putExtra(LockService.EXTRA_STARTUP_FROM, action);
			context.startService(serviceIntent);
		}
	}

}
