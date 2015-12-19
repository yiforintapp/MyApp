package com.leo.appmaster.callfilter;

import com.leo.appmaster.R;
import com.leo.appmaster.ui.dialog.LEOWithSIngleCheckboxDialog;

import android.app.Dialog;
import android.content.Context;

public class CallFIlterUIHelper {
//    private static LEOWithSIngleCheckboxDialog mConfirmRemoveFromBlacklistDialog;
    private Context mContext;
    private static CallFIlterUIHelper mInstance = null;

    public  LEOWithSIngleCheckboxDialog getOneChioseDialog() {
        LEOWithSIngleCheckboxDialog dialog = new LEOWithSIngleCheckboxDialog(mContext);
        dialog.setCheckboxText(mContext.getResources().
                getString(R.string.call_filter_remove_from_blacklist_checkbox_text));
        dialog.setTitle(mContext.getResources().
                getString(R.string.call_filter_remove_from_blacklist_checkbox_title));
        dialog.setCanceledOnTouchOutside(true);
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

    private CallFIlterUIHelper(Context ctx) {
        mContext = ctx;
    }
}
