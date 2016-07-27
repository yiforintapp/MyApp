package com.zlf.appmaster.ui.dialog;


import android.app.Dialog;
import android.content.Context;

public class LEOBaseDialog extends Dialog {

	public LEOBaseDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	}

	public LEOBaseDialog(Context context, int theme) {
		super(context, theme);
	}

	public LEOBaseDialog(Context context) {
		super(context);
	}	
}
