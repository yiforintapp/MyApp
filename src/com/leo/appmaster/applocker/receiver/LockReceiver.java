package com.leo.appmaster.applocker.receiver;

import java.util.Calendar;
import java.util.Date;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.AppLockListActivity;
import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.applocker.LockSettingActivity;
import com.leo.appmaster.applocker.service.LockService;
import com.leo.appmaster.fragment.LockFragment;
import com.leo.appmaster.utils.LeoLog;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class LockReceiver extends BroadcastReceiver {

	public static final String ALARM_LOCK_ACTION = "com.leo.appmaster.alarmlock";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (ALARM_LOCK_ACTION.equals(action)) {

			AppMasterPreference pref = AppMasterPreference.getInstance(context);
			if (pref.getRecommendLockPercent() >= 0.5f) {
				intent = new Intent();
				intent.setAction(LockReceiver.ALARM_LOCK_ACTION);

				Calendar calendar = Calendar.getInstance();
				calendar.setTime(new Date());
				pref.setLastAlarmSetTime(calendar.getTimeInMillis());
				calendar.add(Calendar.MINUTE, 3);
				PendingIntent pi = PendingIntent.getBroadcast(context, 0,
						intent, PendingIntent.FLAG_UPDATE_CURRENT);
				AlarmManager am = (AlarmManager) context
						.getSystemService(Context.ALARM_SERVICE);
				am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
				return;
			} else {

				LeoLog.e("LockReceiver", "showNotification");
				showNotification(context);
				AppMasterPreference.getInstance(context).setReminded(true);
			}

		} else if (action.equals(Intent.ACTION_BOOT_COMPLETED)
				|| action.equals(Intent.ACTION_USER_PRESENT)
				|| "com.leo.appmaster.restart".equals(action)) {
			if (AppMasterPreference.getInstance(context).getLockType() != AppMasterPreference.LOCK_TYPE_NONE) {
				Intent serviceIntent = new Intent(context, LockService.class);
				serviceIntent.putExtra(LockService.EXTRA_STARTUP_FROM, action);
				context.startService(serviceIntent);
			}
		}
	}

	private void showNotification(Context ctx) {
		NotificationManager nm = (NotificationManager) ctx
				.getSystemService(Context.NOTIFICATION_SERVICE);
		String content = ctx.getString(R.string.lock_remind);
		Intent intent;
		AppMasterPreference pref = AppMasterPreference.getInstance(ctx);
		if (pref.getLockType() == AppMasterPreference.LOCK_TYPE_NONE) {
			intent = new Intent(ctx, LockSettingActivity.class);
			intent.putExtra(LockScreenActivity.EXTRA_UNLOCK_FROM,
					LockFragment.FROM_SELF_HOME);
			intent.putExtra(LockScreenActivity.EXTRA_TO_ACTIVITY,
					AppLockListActivity.class.getName());
		} else {
			intent = new Intent(ctx, LockScreenActivity.class);
			intent.putExtra(LockScreenActivity.EXTRA_TO_ACTIVITY,
					AppLockListActivity.class.getName());
			int lockType = pref.getLockType();
			if (lockType == AppMasterPreference.LOCK_TYPE_PASSWD) {
				intent.putExtra(LockScreenActivity.EXTRA_UKLOCK_TYPE,
						LockFragment.LOCK_TYPE_PASSWD);
			} else {
				intent.putExtra(LockScreenActivity.EXTRA_UKLOCK_TYPE,
						LockFragment.LOCK_TYPE_GESTURE);
			}
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
			intent.putExtra(LockScreenActivity.EXTRA_UNLOCK_FROM,
					LockFragment.FROM_SELF_HOME);
		}

		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		Notification notification = new Notification(R.drawable.ic_launcher,
				content, System.currentTimeMillis());
		notification.largeIcon = BitmapFactory.decodeResource(
				ctx.getResources(), R.drawable.ic_launcher);
		notification.tickerText = content;
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		notification.setLatestEventInfo(ctx, "App Master", content,
				pendingIntent);
		nm.notify(0, notification);
	}
}
