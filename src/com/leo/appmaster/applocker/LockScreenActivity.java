package com.leo.appmaster.applocker;

import com.leo.appmaster.R;
import com.leo.appmaster.applocker.logic.LockHandler;
import com.leo.appmaster.fragment.GestureLockFragment;
import com.leo.appmaster.fragment.LockFragment;
import com.leo.appmaster.fragment.PasswdLockFragment;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.FastBlur;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.RelativeLayout;

public class LockScreenActivity extends FragmentActivity {

	public static String EXTRA_UNLOCK_FROM = "extra_unlock_from";
	public static String EXTRA_UKLOCK_TYPE = "extra_unlock_type";

	int mFrom;

	private CommonTitleBar mTtileBar;
	private LockFragment mFragment;

	private Bitmap mAppBaseInfoLayoutbg;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lock_setting);

		handleIntent();

		initUI();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		Log.e("xxxx", "onNewIntent");
		super.onNewIntent(intent);
	}

	private void handleIntent() {
		Intent intent = getIntent();
		int type = intent.getIntExtra(EXTRA_UKLOCK_TYPE,
				LockFragment.LOCK_TYPE_PASSWD);
		if (type == LockFragment.LOCK_TYPE_PASSWD) {
			mFragment = new PasswdLockFragment();
		} else {
			mFragment = new GestureLockFragment();
		}
		mFrom = intent.getIntExtra(EXTRA_UNLOCK_FROM, LockFragment.FROM_SELF);

		if (mFrom == LockFragment.FROM_OTHER) {
			BitmapDrawable bd = (BitmapDrawable) AppUtil.getDrawable(
					getPackageManager(),
					intent.getStringExtra(LockHandler.EXTRA_LOCKED_APP_PKG));

			setAppInfoBackground(bd);
		}

		mFragment.setFrom(mFrom);
		mFragment.setPackage(intent
				.getStringExtra(LockHandler.EXTRA_LOCKED_APP_PKG));
	}

	private void setAppInfoBackground(Drawable drawable) {
		int h = drawable.getIntrinsicHeight() * 9 / 10;
		int w = h * 3 / 5;
		mAppBaseInfoLayoutbg = Bitmap.createBitmap(w, h,
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(mAppBaseInfoLayoutbg);
		canvas.drawColor(Color.WHITE);
		drawable.setBounds(-(drawable.getIntrinsicWidth() - w) / 2,
				-(drawable.getIntrinsicHeight() - h) / 2,
				(drawable.getIntrinsicWidth() - w) / 2 + w,
				(drawable.getIntrinsicHeight() - h) / 2 + h);
		drawable.draw(canvas);
		// canvas.drawColor(Color.argb(60, 0, 0, 0));
		mAppBaseInfoLayoutbg = FastBlur.doBlur(mAppBaseInfoLayoutbg, 25, true);

		RelativeLayout layout = (RelativeLayout) findViewById(R.id.activity_lock_layout);

		layout.setBackgroundDrawable(new BitmapDrawable(mAppBaseInfoLayoutbg));

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mAppBaseInfoLayoutbg != null) {
			mAppBaseInfoLayoutbg.recycle();
			mAppBaseInfoLayoutbg = null;
		}
	}

	private void initUI() {
		mTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
		mTtileBar.setTitle(R.string.app_lock);

		if (mFrom == LockFragment.FROM_SELF) {
			mTtileBar.openBackView();
		}

		FragmentManager fm = getSupportFragmentManager();

		FragmentTransaction tans = fm.beginTransaction();
		tans.replace(R.id.fragment_contain, mFragment);
		tans.commit();
	}

	@Override
	public void onBackPressed() {
		if (mFrom == LockFragment.FROM_SELF) {
			super.onBackPressed();
		} else {
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			this.startActivity(intent);
			finish();
		}
	}
}
