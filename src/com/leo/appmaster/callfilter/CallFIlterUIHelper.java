package com.leo.appmaster.callfilter;

import java.util.ArrayList;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;
import com.leo.appmaster.intruderprotection.IntruderprotectionActivity;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOChoiceDialog;
import com.leo.appmaster.ui.dialog.LEOWithSIngleCheckboxDialog;
import com.leo.appmaster.ui.dialog.MultiChoicesWitchSummaryDialog;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class CallFIlterUIHelper {
//    private static LEOWithSIngleCheckboxDialog mConfirmRemoveFromBlacklistDialog;
    private Context mContext;
    private static CallFIlterUIHelper mInstance = null;

    public  LEOWithSIngleCheckboxDialog getConfirmRemoveFromBlacklistDialog() {
        LEOWithSIngleCheckboxDialog dialog = new LEOWithSIngleCheckboxDialog(mContext);
        dialog.setCheckboxText(mContext.getResources().
                getString(R.string.call_filter_remove_from_blacklist_checkbox_text));
        dialog.setTitle(mContext.getResources().
                getString(R.string.call_filter_remove_from_blacklist_checkbox_title));
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }
    
    public LEOChoiceDialog getCallHandleDialog(String title) {
        LEOChoiceDialog dialog = new LEOChoiceDialog(mContext);
        ArrayList<String> list = new ArrayList<String>();
        list.add("删除拦截记录");
        list.add("移出黑名单");
        list.add("标记");
        dialog.setNeedCheckbox(false);
        dialog.setTitle(title);
        dialog.setItemsWithDefaultStyle(list, 0);
        return dialog;
    }

    public MultiChoicesWitchSummaryDialog getCallHandleDialogWithSummary(String title) {
        MultiChoicesWitchSummaryDialog dialog = new MultiChoicesWitchSummaryDialog(mContext);
        dialog.setTitle(title);
        dialog.setContent("检测到您的通话小于5妙，xxxx");
        String[] itemContent = {"骚扰电话", "广告推销", "诈骗电话"};
        dialog.fillData(itemContent, 0);
        return dialog;
    }
//    public static Dialog getConfirmRemoveFromBlacklistDialog() {
//        return null;
//    }
    public static synchronized CallFIlterUIHelper getInstance(Context ctx) {
        if (mInstance == null) {
            mInstance = new CallFIlterUIHelper(ctx);
        }
        return mInstance;
    }
    
    public LEOAlarmDialog getConfirmClearAllRecordDialog() {
        LEOAlarmDialog dialog = new LEOAlarmDialog(mContext);
        dialog.setContentVisiable(false);
        dialog.setTitle("确定清空所有记录吗");
        dialog.setDialogIconVisibility(false);
        return dialog;
    }

    private CallFIlterUIHelper(Context ctx) {
        mContext = ctx;
    }
    
    public void showReceiveCallNotification() {
        Intent intent = new Intent(AppMasterApplication.getInstance(), IntruderprotectionActivity.class);
        PendingIntent pendingIntent= PendingIntent.getActivity(AppMasterApplication.getInstance(), 1, intent, Notification.FLAG_AUTO_CANCEL);
        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);  
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);  
        mBuilder.setContentTitle("拦截到以下来电")
        .setContentText("13510261550")
        .setContentIntent(pendingIntent) //设置通知栏点击意图  
        .setTicker("拦截到以下来电")
        .setWhen(System.currentTimeMillis())  
        .setPriority(Notification.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .setSmallIcon(R.drawable.ic_launcher);
        mNotificationManager.notify(1, mBuilder.build());
    }
    
}
