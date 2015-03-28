package com.leo.appmaster.appmanage.view;

import com.leo.appmaster.appmanage.AppInfoBaseLayout;

import android.content.Context;
import android.os.Parcelable;
import android.util.AttributeSet;

public class AppPermissionInfoLayout extends AppInfoBaseLayout {

	public AppPermissionInfoLayout(Context context) {
		super(context);
	}

	public AppPermissionInfoLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
	}
	
    @Override
    public void onRestoreInstanceState(Parcelable state) {
        try {
            super.onRestoreInstanceState(state);
        } catch (Exception e) {
            
        }
    }

}
