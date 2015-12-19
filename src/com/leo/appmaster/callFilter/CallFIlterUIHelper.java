package com.leo.appmaster.callFilter;

import com.leo.appmaster.R;
import com.leo.appmaster.ui.dialog.LEOWithSIngleCheckboxDialog;

import android.app.Dialog;
import android.content.Context;

public class CallFIlterUIHelper {
//    private static LEOWithSIngleCheckboxDialog mConfirmRemoveFromBlacklistDialog;
    
    
    
    public static Dialog getConfirmRemoveFromBlacklistDialog(Context context) {
        LEOWithSIngleCheckboxDialog dialog = new LEOWithSIngleCheckboxDialog(context);
        dialog.setCheckboxText(context.getResources().getString(R.string.call_filter_remove_from_blacklist_checkbox_text));
        dialog.setTitle(context.getResources().getString(R.string.call_filter_remove_from_blacklist_checkbox_title));
        return dialog;
    }
    
//    public static Dialog getConfirmRemoveFromBlacklistDialog() {
//        return null;
//    }
    
}
