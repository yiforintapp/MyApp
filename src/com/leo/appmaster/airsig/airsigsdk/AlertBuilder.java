package com.leo.appmaster.airsig.airsigsdk;

import com.leo.appmaster.R;
import com.leo.appmaster.airsig.airsigui.AnimatedGifImageView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class AlertBuilder {

	AlertDialog.Builder mBuilder;
	AlertDialog mAlert;
	TextView mTitle, mDesc;
	AnimatedGifImageView mImageView;
	Button mPositiveButton, mNegativeButton;
	
	@SuppressLint("InflateParams")
	public AlertBuilder(Activity activity) {
		mBuilder = new AlertDialog.Builder(activity);
		
		ViewGroup alertBodyView = (ViewGroup) activity.getLayoutInflater().inflate(R.layout.airsig_alert, null);
		mTitle = (TextView) alertBodyView.findViewById(R.id.textTitle);
		mDesc = (TextView) alertBodyView.findViewById(R.id.textDesc);
		mImageView = (AnimatedGifImageView) alertBodyView.findViewById(R.id.GifImageView);
		mPositiveButton = (Button) alertBodyView.findViewById(R.id.buttonPositive);
		mNegativeButton = (Button) alertBodyView.findViewById(R.id.buttonNegative);
		mBuilder.setView(alertBodyView);
	}
	
	public void setTitle(String string) {
		if (null != string && string.length() > 0) {
			mTitle.setText(string);
			mTitle.setVisibility(View.VISIBLE);
		} else {
			mTitle.setVisibility(View.GONE);
		}
	}
	
	public void setDetailedMessage(String string) {
		if (null != string && string.length() > 0) {
			mDesc.setText(string);
			mDesc.setVisibility(View.VISIBLE);
		} else {
			mDesc.setVisibility(View.GONE);
		}
	}
	
	public void setImageResource(int resourceId) {
		mImageView.setImageResource(resourceId);
		mImageView.setVisibility(View.VISIBLE);
	}
	
	public void setGifResource(int resourceId) {
		mImageView.setVisibility(View.VISIBLE);
		mImageView.setAnimatedGif(resourceId, AnimatedGifImageView.TYPE.FIT_CENTER);
	}
	
	public void setPositiveButton(String title, final Button.OnClickListener clickListener) {
		if (title == null || title.length() == 0) {
			mPositiveButton.setVisibility(View.GONE);
			return;
		}
		mPositiveButton.setText(title);
		mPositiveButton.setVisibility(View.VISIBLE);
		mPositiveButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				mAlert.dismiss();
				if (null != clickListener) {
					clickListener.onClick(mPositiveButton);
				}
			}
		});
	}
	
	public void setNegativeButton(String title, final Button.OnClickListener clickListener) {
		if (title == null || title.length() == 0) {
			mNegativeButton.setVisibility(View.GONE);
			return;
		}
		mNegativeButton.setText(title);
		mNegativeButton.setVisibility(View.VISIBLE);
		mNegativeButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				mAlert.dismiss();
				if (null != clickListener) {
					clickListener.onClick(mNegativeButton);
				}
			}
		});
	}
	
	public void setCancelable(Boolean cancelable) {
		mBuilder.setCancelable(cancelable);
	}
	
	public void show() {
		mAlert = mBuilder.create();
		mAlert.show();
		
		try {
			int divierId = mAlert.getContext().getResources().getIdentifier("android:id/titleDivider", null, null);
            View divider = mAlert.findViewById(divierId);
            divider.setBackgroundColor(Color.TRANSPARENT);
		} catch (Exception e) { }
	}
}
