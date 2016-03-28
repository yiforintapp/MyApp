package com.leo.appmaster.intruderprotection;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import com.leo.appmaster.R;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;

/**
 * Created by chenfs on 16-3-18.
 */
public class ShowAboutIntruderDialogHelper {

    public static Dialog showAskOpenDeviceAdminDialog(Context context, DialogInterface.OnClickListener rightButtonListener) {
        LEOAlarmDialog dialog = new LEOAlarmDialog(context);
        dialog.setTitle(R.string.intruder_setting_title_1);
        dialog.setContent(context.getString(R.string.intruder_device_admin_guide_content));
        dialog.setRightBtnListener(rightButtonListener);
        dialog.show();
        return dialog;
    }

    public static Dialog showForbitDialog(Context context, DialogInterface.OnClickListener rightButtonListener) {
        LEOAlarmDialog dialog = new LEOAlarmDialog(context);
        dialog.setContent(context.getResources().getString(R.string.intruderprotection_forbit_content));
        dialog.setRightBtnStr(context.getResources().getString(R.string.secur_help_feedback_tip_button));
        dialog.setLeftBtnStr(context.getResources().getString(R.string.no_image_hide_dialog_button));
        dialog.setRightBtnListener(rightButtonListener);
        dialog.show();
        return dialog;
    }

    public static Dialog showConfirmCloseSysDialog(Context context, DialogInterface.OnClickListener rightButtonListener) {
        LEOAlarmDialog dialog = new LEOAlarmDialog(context);
        dialog.setTitle(R.string.intruder_setting_title_1);
        dialog.setContent(context.getString(R.string.intruder_systemlock_alarm));
        dialog.setRightBtnListener(rightButtonListener);
        dialog.show();
        return dialog;
    }

}
