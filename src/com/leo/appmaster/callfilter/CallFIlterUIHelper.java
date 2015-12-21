package com.leo.appmaster.callfilter;

import java.util.ArrayList;

import com.leo.appmaster.R;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOChoiceDialog;
import com.leo.appmaster.ui.dialog.LEOWithSIngleCheckboxDialog;
import com.leo.appmaster.ui.dialog.MultiChoicesWitchSummaryDialog;

import android.content.Context;

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
}
