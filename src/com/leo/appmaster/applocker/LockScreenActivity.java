package com.leo.appmaster.applocker;

import java.util.ArrayList;
import java.util.List;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.logic.LockHandler;
import com.leo.appmaster.fragment.GestureLockFragment;
import com.leo.appmaster.fragment.LockFragment;
import com.leo.appmaster.fragment.PasswdLockFragment;
import com.leo.appmaster.home.HomeActivity;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.LeoPopMenu;
import com.leo.appmaster.ui.dialog.LeoDoubleLinesInputDialog;
import com.leo.appmaster.ui.dialog.LeoDoubleLinesInputDialog.OnDiaogClickListener;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.FastBlur;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class LockScreenActivity extends FragmentActivity implements
		OnClickListener, OnDiaogClickListener {

	public static String EXTRA_UNLOCK_FROM = "extra_unlock_from";
	public static String EXTRA_UKLOCK_TYPE = "extra_unlock_type";
	public static String EXTRA_FROM_ACTIVITY = "extra_form_activity";
    public static String EXTRA_LOCK_TITLE = "extra_lock_title";
	
	int mFrom;
	private CommonTitleBar mTtileBar;
	private LockFragment mFragment;
	private Bitmap mAppBaseInfoLayoutbg;
	private LeoPopMenu mLeoPopMenu;
	private LeoDoubleLinesInputDialog mDialog;
	private EditText mEtQuestion, mEtAnwser;
	private String mLockTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lock_setting);
		handleIntent();
		initUI();
	}

	@Override
	protected void onNewIntent(Intent intent) {
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
		mLockTitle = intent.getStringExtra(EXTRA_LOCK_TITLE);
		mFragment.setFrom(mFrom);
		mFragment.setPackage(intent
				.getStringExtra(LockHandler.EXTRA_LOCKED_APP_PKG));
		mFragment.setActivity(intent.getStringExtra(EXTRA_FROM_ACTIVITY));
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
		canvas.drawColor(Color.argb(70, 0, 0, 0));
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

		if (AppMasterPreference.getInstance(this).hasPswdProtect()) {
			mTtileBar.setOptionImage(R.drawable.setting_btn);
			mTtileBar.setOptionImageVisibility(View.VISIBLE);
			mTtileBar.setOptionListener(this);
		}

		if (mFrom == LockFragment.FROM_SELF) {
			mTtileBar.openBackView();
			if (TextUtils.isEmpty(mLockTitle)) {
		         mTtileBar.setTitle(R.string.app_lock);
			} else {
	              mTtileBar.setTitle(mLockTitle);
			}
		} else {
			mTtileBar.setBackArrowVisibility(View.GONE);
			mTtileBar.setTitle(R.string.app_name);
		}
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction tans = fm.beginTransaction();
		tans.replace(R.id.fragment_contain, mFragment);
		tans.commit();
	}

	private void findPasswd() {
		mDialog = new LeoDoubleLinesInputDialog(this);
		mDialog.setTitle(R.string.pleas_input_anwser);
		mDialog.setFirstHead(R.string.passwd_question);
		mDialog.setSecondHead(R.string.passwd_anwser);
		mDialog.setOnClickListener(this);
		mEtQuestion = mDialog.getFirstEditText();
		mEtAnwser = mDialog.getSecondEditText();
		mEtQuestion.setFocusable(false);
		mEtQuestion.setText(AppMasterPreference.getInstance(this)
				.getPpQuestion());
		mDialog.show();
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_option_image:
			if (mLeoPopMenu == null) {
				mLeoPopMenu = new LeoPopMenu();
				mLeoPopMenu.setPopItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						if (position == 0) {
							findPasswd();
						}
						mLeoPopMenu.dismissSnapshotList();
					}
				});
			}
			mLeoPopMenu.setPopMenuItems(getPopMenuItems());
			mLeoPopMenu.showPopMenu(this,
					mTtileBar.findViewById(R.id.tv_option_image), null, null);
			break;

		default:
			break;
		}

	}

	private List<String> getPopMenuItems() {
		List<String> listItems = new ArrayList<String>();
		Resources resources = AppMasterApplication.getInstance().getResources();
		if (AppMasterPreference.getInstance(this).getLockType() == AppMasterPreference.LOCK_TYPE_GESTURE) {
			listItems.add(resources.getString(R.string.find_gesture));
		} else if (AppMasterPreference.getInstance(this).getLockType() == AppMasterPreference.LOCK_TYPE_PASSWD) {
			listItems.add(resources.getString(R.string.find_passwd));
		}
		return listItems;
	}

	@Override
	public void onClick(int which) {
		if (which == 1) {// make sure
			String anwser = AppMasterPreference.getInstance(this).getPpAnwser();
			if (anwser.equals(mEtAnwser.getText().toString())) {
				// goto reset passwd
				Intent intent = new Intent(this, LockSettingActivity.class);
				intent.putExtra(LockSettingActivity.RESET_PASSWD_FLAG, true);
				this.startActivity(intent);

			} else {
				Toast.makeText(this, R.string.reinput_anwser, 0).show();
				mEtAnwser.setText("");
			}
		} else if (which == 0) { // cancel
			mDialog.dismiss();
		}

	}
}
