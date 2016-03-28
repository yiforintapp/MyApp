package com.leo.appmaster.airsig.airsigsdk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils.TruncateAt;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.leo.appmaster.R;

public class VerifyActivity extends Activity implements VerifyFragment.OnVerificationResultListener {
	
	// Keys for configure the activity
	public static final String kTargetActionIndexes = "The target action indexes";
	public static final String kDisableBackupSolutionButton = "Disable backup solution button";
	public static final String kBackupSolutionTitle = "The title for backup solution button";
	public static final String kDisableBackButton = "Disable the back button to dismiss this activity";
	public static final String kResultEventName = "The event name for receive verify result from local broadcast";
	
	public static final String DEFAULT_RESULT_EVENT_NAME = "AirSig Verify Result";
	
	// Keys for receive data from this activity
	public static final String kVerifyResult = "The verification result";
	public static final String kVerifyTooManyFails = "There are too many fails";
	public static final String kGoToBackSolution = "Backup solution clicked";

	// Settings
	private boolean mDisableBackupSolutionButton = false;
	private String mBackupSolutionTitle = null; 
	private boolean mDisableBackButton = false;
	private String mResultEventName = DEFAULT_RESULT_EVENT_NAME;
	
	private VerifyFragment mVerifyFragment;
	
	@SuppressLint("InflateParams")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		setContentView(R.layout.airsig_activity_verify);
		
		// customize action bar: 
		// 1. no App icon 
		// 2. back button
		if (null != getActionBar()) {
			getActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));  
			getActionBar().setDisplayHomeAsUpEnabled(true);
			getActionBar().setDisplayShowTitleEnabled(false);
			getActionBar().setDisplayShowCustomEnabled(true);
			TextView title = new TextView(getApplicationContext());
			ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			title.setLayoutParams(lp);
			title.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
			title.setEllipsize(TruncateAt.END);
			title.setTextColor(getResources().getColor(R.color.airsig_actionbar_title));
			title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.airsig_actionbar_title_textsize));
			title.setText(getResources().getString(R.string.airsig_verify_title));
			getActionBar().setCustomView(title);
		}
		
		// Settings & private data
		Bundle bundle = this.getIntent().getExtras();
		int[] targetActionIndexes = bundle.getIntArray(kTargetActionIndexes);
		mDisableBackupSolutionButton = bundle.getBoolean(kDisableBackupSolutionButton, false);
		mBackupSolutionTitle = bundle.getString(kBackupSolutionTitle);
		mDisableBackButton = bundle.getBoolean(kDisableBackButton, false);
		if (bundle.containsKey(kResultEventName)) {
			mResultEventName = bundle.getString(kResultEventName);
		}
		
		// Verify fragment
		mVerifyFragment = (VerifyFragment) getFragmentManager().findFragmentById(R.id.verifyView);
		mVerifyFragment.setTargetActionIndexes(targetActionIndexes);
		
		// Buttons
		// close button
		if (mDisableBackButton) {
			getActionBar().setDisplayHomeAsUpEnabled(false);
		} else {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}
	
	@Override
    public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
	@Override 
	public void onBackPressed() {
		// leave
		finish(false, false, false);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (mDisableBackupSolutionButton) {
		} else {
			MenuItem item;
			if (mBackupSolutionTitle != null && mBackupSolutionTitle.length() > 0) {
				item = menu.add(mBackupSolutionTitle);
			} else {
				item = menu.add(getResources().getString(R.string.airsig_verify_forgot_signature));
			}
			
			item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
			item.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			    @Override
			    public boolean onMenuItemClick (MenuItem item) {
			    	// forget password
					finish(false, false, true);
					return true;
			    }
			});
		}
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		super.onOptionsItemSelected(item);
		
		if (android.R.id.home == item.getItemId()) {
			// leave
			finish(false, false, false);
		}
		
		return true;
	}
	
	@Override
	public void onVerified(boolean result, boolean foreverBlocked) {
		if (result) {
			finish(true, false, false);
		} else if (foreverBlocked) {
			finish(false, true, true);
		}
	}
	
	private void finish(final boolean result, final boolean tooManyFails, final boolean goToBackupSolution) {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Intent intent = new Intent(mResultEventName);
				intent.putExtra(kVerifyResult, result);
				intent.putExtra(kVerifyTooManyFails, tooManyFails);
				intent.putExtra(kGoToBackSolution, goToBackupSolution);
				LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
				finish();
			}
		});
	}
}
