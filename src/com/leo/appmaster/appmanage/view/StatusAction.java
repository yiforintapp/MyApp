package com.leo.appmaster.appmanage.view;

import com.leo.appmaster.R;
import com.leo.appmaster.model.BaseInfo;
import com.leo.appmaster.utils.LeoLog;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;

public class StatusAction implements DecorateAction {
	private static final String TAG = "StatusAction";

	private int mAlpha = 255;

	public void setNewInstallPromptOffsetX(int mNewInstallPromptOffsetX) {
	}

	public void setNewInstallPromptOffsetY(int mNewInstallPromptOffsetY) {
	}

	@Override
	public int getActionType() {
		return ACTION_STATUS;
	}

	@Override
	public void draw(Canvas canvas, View view) {
		Object info = view.getTag();
		if (info instanceof BaseInfo) {
			// if (item.status == BaseItemInfo.STATUS_NORMAL)
			// return;
			//
			int transitionX = view.getScrollX();

			LeoLog.i(TAG, "transitionX " + transitionX);

			int transitionY = view.getScrollY();
			//
			// int iconSize = (int) (Utilities.getAppIconSize() *
			// LauncherPreferenceHelper.iconScale);
			final Resources res = view.getContext().getResources();
			Drawable indicator = null;
			//
			// int drawableW = 0;
			// int drawableH = 0;
			// switch (item.status) {
			// case BaseItemInfo.STATUS_NOTDOWNLOAD:
			// case BaseItemInfo.STATUS_DOWNLOADED:
			// case BaseItemInfo.STATUS_DOWNLOADING:
			indicator = res.getDrawable(R.drawable.app_status_not_download);
			// drawableW = (int) (indicator.getIntrinsicWidth() *
			// LauncherPreferenceHelper.iconScale);
			// drawableH = (int) (indicator.getIntrinsicHeight() *
			// LauncherPreferenceHelper.iconScale);
			int drawableW = indicator.getIntrinsicWidth();
			int drawableH = indicator.getIntrinsicHeight();
			// transitionX += (width + iconSize - drawableW) / 2;
			//
			// LogEx.i(TAG, "width " + width + ", drawableW = " + drawableW);
			// LogEx.i(TAG, "iconSize " + iconSize);
			//
			// transitionY += 0;
			// break;
			// case BaseItemInfo.STATUS_NOTRUN:
			// indicator = res.getDrawable(R.drawable.app_status_new);
			// drawableW = (int) (indicator.getIntrinsicWidth() *
			// LauncherPreferenceHelper.iconScale);
			// drawableH = (int) (indicator.getIntrinsicHeight() *
			// LauncherPreferenceHelper.iconScale);
			// transitionX += (width - iconSize) / 2
			// + mNewInstallPromptOffsetX;
			// transitionY += mNewInstallPromptOffsetY;
			// break;
			// }

			if (indicator != null) {
				canvas.save();
				canvas.translate(transitionX, transitionY);
				indicator.setBounds(0, 0, drawableW, drawableH);
				indicator.setAlpha(mAlpha);
				indicator.draw(canvas);
				canvas.restore();
			}
		}
	}

	@Override
	public boolean onTouchEventCheck(MotionEvent event) {
		return false;
	}

	@Override
	public void setAlpha(int alpha) {
		mAlpha = alpha;
	}
}
