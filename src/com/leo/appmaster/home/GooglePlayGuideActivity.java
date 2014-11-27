package com.leo.appmaster.home;

import com.leo.appmaster.R;
import com.leo.appmaster.animation.AnimationListenerAdapter;
import com.leo.appmaster.sdk.BaseActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

public class GooglePlayGuideActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.layout_googelplay_guide);
	}

	@Override
	protected void onResume() {
		openGooglePlayGuide();
		super.onResume();
	}

	private void openGooglePlayGuide() {

		final ImageView iv = (ImageView) findViewById(R.id.iv_animator);
		AnimationSet as = new AnimationSet(true);
		as.setDuration(1000);
		TranslateAnimation ta = new TranslateAnimation(0f, 0f, 0f, -500f);
		as.addAnimation(ta);
		AlphaAnimation aa = new AlphaAnimation(1f, 0f);
		as.addAnimation(aa);
		ta.setRepeatCount(1);
		aa.setRepeatCount(1);
		as.setFillEnabled(true);
		as.setFillBefore(true);
		as.setAnimationListener(new AnimationListenerAdapter() {
			@Override
			public void onAnimationEnd(Animation animation) {
				ScaleAnimation sa = new ScaleAnimation(0f, 1f, 0f, 1f, iv
						.getWidth() / 2, iv.getHeight() / 2);
				AlphaAnimation aa = new AlphaAnimation(0f, 1f);
				AnimationSet as2 = new AnimationSet(true);
				as2.setDuration(1000);
				as2.addAnimation(sa);
				as2.addAnimation(aa);
				as2.setAnimationListener(new AnimationListenerAdapter() {
					@Override
					public void onAnimationEnd(Animation animation) {
						iv.postDelayed(new Runnable() {
							@Override
							public void run() {
								GooglePlayGuideActivity.this.finish();
							}
						}, 300);
					}
				});
				iv.startAnimation(as2);
			}
		});
		iv.startAnimation(as);
	}
}
