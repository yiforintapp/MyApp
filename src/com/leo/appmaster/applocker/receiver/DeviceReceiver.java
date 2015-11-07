package com.leo.appmaster.applocker.receiver;

import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.DeviceAdminEvent;
import com.leo.appmaster.eventbus.event.EventId;
import com.leo.appmaster.utils.LeoLog;

import android.app.admin.DeviceAdminReceiver;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DeviceReceiver extends BroadcastReceiver {

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

}
