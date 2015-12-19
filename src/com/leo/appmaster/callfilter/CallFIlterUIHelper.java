package com.leo.appmaster.callfilter;

import java.util.ArrayList;

import com.leo.appmaster.R;
import com.leo.appmaster.ui.dialog.LEOChoiceDialog;
import com.leo.appmaster.ui.dialog.LEOWithSIngleCheckboxDialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

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
